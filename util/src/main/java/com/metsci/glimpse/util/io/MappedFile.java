package com.metsci.glimpse.util.io;

import static java.lang.String.*;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.FileChannel;

import sun.misc.Cleaner;
import sun.misc.Unsafe;
import sun.nio.ch.DirectBuffer;
import sun.nio.ch.FileChannelImpl;

/**
 * Represents a file that gets memory-mapped, in its entirety, even if it is larger than 2GB.
 * <p>
 * The NIO Buffer APIs do not support 64-bit indexing, so it is not possible to access a large
 * file through a single Buffer. Instead, this class does a single memory-map call for the whole
 * file, and then creates Buffer objects, as needed, for slices of the memory block. Buffer
 * object creation is cheap.
 */
@SuppressWarnings( "restriction" )
public class MappedFile
{

    protected final File file;
    protected final boolean writable;
    protected final ByteOrder byteOrder;

    protected final FileDescriptor fd;
    protected final long address;
    protected final long size;

    /**
     * Cleaners serve the same purpose as finalize() methods, with 2 subtle differences:
     * <ol>
     * <li>finalize() methods are better when resource disposal is non-trivial and/or slow
     * <li>The JVM does a better job of running Cleaners promptly
     * </ol>
     * <p>
     * When the enclosing Object is ready to be GC-ed, the Cleaner gets magically triggered
     * via the JVM's PhantomReference mechanism.
     * <p>
     * In Java 9, the Cleaner class is reportedly moving out of the sun.misc package and into
     * a java.lang package.
     */
    protected final Cleaner cleaner;


    public MappedFile( File file, ByteOrder byteOrder ) throws IOException
    {
        this( file, byteOrder, false, -1L );
    }

    public MappedFile( File file, ByteOrder byteOrder, long size ) throws IOException
    {
        this( file, byteOrder, true, size );
    }

    public MappedFile( File file, ByteOrder byteOrder, boolean writable, long size ) throws IOException
    {
        if ( writable && size < 0 )
        {
            throw new IllegalArgumentException( "Illegal size for writable file: size = " + size );
        }

        this.file = file;
        this.writable = writable;
        this.byteOrder = byteOrder;

        String rafMode = ( this.writable ? "rw" : "r" );
        try ( RandomAccessFile raf = new RandomAccessFile( file, rafMode ) )
        {
            if ( this.writable )
            {
                this.size = size;
                raf.setLength( this.size );
            }
            else
            {
                this.size = raf.length( );
            }

            this.address = memmap( raf.getChannel( ), 0, this.size, this.writable );

            this.fd = duplicateForMapping( raf.getFD( ) );
            Runnable unmapper = createUnmapper( this.address, this.size, 0, this.fd );
            this.cleaner = Cleaner.create( this, unmapper );
        }
    }

    public File file( )
    {
        return this.file;
    }

    public boolean writable( )
    {
        return this.writable;
    }

    public ByteOrder byteOrder( )
    {
        return this.byteOrder;
    }

    public long size( )
    {
        return this.size;
    }

    public void copyTo( long position, int size, ByteBuffer dest )
    {
        if ( dest.isDirect( ) && dest instanceof DirectBuffer )
        {
            // This block does all the same steps as the block below,
            // but without creating a temporary slice buffer

            if ( position < 0 || position + size > this.size )
            {
                throw new RuntimeException( format( "Slice falls outside bounds of file: slice-position = %d, slice-size = %d, file-size = %d", position, size, this.size ) );
            }

            if ( dest.isReadOnly( ) )
            {
                throw new ReadOnlyBufferException( );
            }

            if ( dest.remaining( ) < size )
            {
                throw new BufferOverflowException( );
            }

            long sAddr = this.address + position;
            long dAddr = ( ( DirectBuffer ) dest ).address( ) + dest.position( );
            unsafe.copyMemory( sAddr, dAddr, size );
            dest.position( dest.position( ) + size );
        }
        else
        {
            dest.put( this.slice( position, size ) );
        }
    }

    public MappedByteBuffer slice( long position, int size )
    {
        if ( position < 0 || position + size > this.size )
        {
            throw new RuntimeException( format( "Slice falls outside bounds of file: slice-position = %d, slice-size = %d, file-size = %d", position, size, this.size ) );
        }

        MappedByteBuffer buffer = asDirectBuffer( this.address + position, size, this.fd, this, this.writable );
        buffer.order( this.byteOrder );
        return buffer;
    }

    public void force( )
    {
        if ( this.writable )
        {
            force( this.fd, this.address, this.size );
        }
    }

    /**
     * <strong>IMPORTANT:</strong> This method must not be called while slices of this MappedFile are
     * still in use. If a slice is used after its MappedFile has been disposed, behavior is undefined.
     */
    public void dispose( )
    {
        this.cleaner.clean( );
    }


    // Lots of verbose code to get access to various JVM-internal functionality

    protected static final Unsafe unsafe;
    protected static final int pageSize;
    static
    {
        try
        {
            // Should work on more platforms
            Constructor<Unsafe> unsafeConstructor = Unsafe.class.getDeclaredConstructor( );
            unsafeConstructor.setAccessible( true );
            unsafe = unsafeConstructor.newInstance( );

            // May not work on as many platforms
            //Field field = Unsafe.class.getDeclaredField( "theUnsafe" );
            //field.setAccessible( true );
            //unsafe = ( Unsafe ) field.get( null );

            pageSize = unsafe.pageSize( );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Cannot access " + Unsafe.class.getName( ), e );
        }
    }

    /**
     * long map0( int mode, long position, long size )
     */
    protected static final Method FileChannelImpl_map0;
    static
    {
        try
        {
            FileChannelImpl_map0 = FileChannelImpl.class.getDeclaredMethod( "map0", int.class, long.class, long.class );
            FileChannelImpl_map0.setAccessible( true );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Cannot access " + FileChannelImpl.class.getName( ) + ".map0()", e );
        }
    }

    protected static long memmap( FileChannel fileChannel, long position, long size, boolean writable ) throws RuntimeException
    {
        if ( ( position % pageSize ) != 0 )
        {
            throw new RuntimeException( format( "Memmap position is not divisible by pageSize: position = %d, pageSize = %d", position, pageSize ) );
        }

        try
        {
            int mapMode = ( writable ? 1 : 0 );
            return ( ( long ) FileChannelImpl_map0.invoke( fileChannel, mapMode, position, size ) );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Failed to memmap file", e );
        }
    }

    /**
     * FileDispatcherImpl( )
     */
    protected static final Constructor<?> FileDispatcherImpl_init;
    static
    {
        try
        {
            Class<?> clazz = Class.forName( "sun.nio.ch.FileDispatcherImpl" );
            FileDispatcherImpl_init = clazz.getDeclaredConstructor( );
            FileDispatcherImpl_init.setAccessible( true );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Cannot access sun.nio.ch.FileDispatcherImpl.<init>()", e );
        }
    }

    /**
     * FileDescriptor duplicateForMapping( FileDescriptor fd )
     */
    protected static final Method FileDispatcher_duplicateForMapping;
    static
    {
        try
        {
            Class<?> clazz = Class.forName( "sun.nio.ch.FileDispatcher" );
            FileDispatcher_duplicateForMapping = clazz.getDeclaredMethod( "duplicateForMapping", FileDescriptor.class );
            FileDispatcher_duplicateForMapping.setAccessible( true );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Cannot access sun.nio.ch.FileDispatcher.duplicateForMapping()", e );
        }
    }

    protected static FileDescriptor duplicateForMapping( FileDescriptor fd ) throws IOException
    {
        try
        {
            Object fileDispatcher = FileDispatcherImpl_init.newInstance( );
            return ( ( FileDescriptor ) FileDispatcher_duplicateForMapping.invoke( fileDispatcher, fd ) );
        }
        catch ( InvocationTargetException e )
        {
            Throwable e2 = e.getTargetException( );
            if ( e2 instanceof IOException )
            {
                throw ( ( IOException ) e2 );
            }
            else
            {
                throw new RuntimeException( "Failed to duplicate file descriptor", e2 );
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Failed to duplicate file descriptor", e );
        }
    }

    /**
     * Unmapper( long address, long size, int capacity, FileDescriptor fd )
     */
    protected static final Constructor<?> Unmapper_init;
    static
    {
        try
        {
            Class<?> clazz = Class.forName( "sun.nio.ch.FileChannelImpl$Unmapper" );
            Unmapper_init = clazz.getDeclaredConstructor( long.class, long.class, int.class, FileDescriptor.class );
            Unmapper_init.setAccessible( true );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Cannot access sun.nio.ch.FileChannelImpl$Unmapper.<init>()", e );
        }
    }

    protected static Runnable createUnmapper( long address, long size, int capacity, FileDescriptor fd )
    {
        try
        {
            return ( ( Runnable ) Unmapper_init.newInstance( address, size, capacity, fd ) );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Failed to create Unmapper", e );
        }
    }

    /**
     * DirectByteBuffer( int cap, long addr, FileDescriptor fd, Runnable unmapper )
     */
    protected static final Constructor<?> DirectByteBuffer_init;
    static
    {
        try
        {
            Class<?> clazz = Class.forName( "java.nio.DirectByteBuffer" );
            DirectByteBuffer_init = clazz.getDeclaredConstructor( int.class, long.class, FileDescriptor.class, Runnable.class );
            DirectByteBuffer_init.setAccessible( true );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Cannot access java.nio.DirectByteBuffer.<init>()", e );
        }
    }

    /**
     * DirectByteBufferR( int cap, long addr, FileDescriptor fd, Runnable unmapper )
     */
    protected static final Constructor<?> DirectByteBufferR_init;
    static
    {
        try
        {
            Class<?> clazz = Class.forName( "java.nio.DirectByteBufferR" );
            DirectByteBufferR_init = clazz.getDeclaredConstructor( int.class, long.class, FileDescriptor.class, Runnable.class );
            DirectByteBufferR_init.setAccessible( true );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Cannot access java.nio.DirectByteBufferR.<init>()", e );
        }
    }

    /**
     * Object att
     */
    protected static final Field DirectByteBuffer_att;
    static
    {
        try
        {
            Class<?> clazz = Class.forName( "java.nio.DirectByteBuffer" );
            DirectByteBuffer_att = clazz.getDeclaredField( "att" );
            DirectByteBuffer_att.setAccessible( true );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Cannot access java.nio.DirectByteBuffer.att", e );
        }
    }

    /**
     * The {@code fd} arg is used in {@link MappedByteBuffer#isLoaded()}, {@link MappedByteBuffer#load()},
     * and {@link MappedByteBuffer#force()}.
     * <p>
     * The {@code attachment} arg will be stored by strong-reference in the returned buffer -- and therefore
     * won't be garbage-collected until the returned buffer has been garbage-collected.
     */
    protected static MappedByteBuffer asDirectBuffer( long address, int capacity, FileDescriptor fd, Object attachment, boolean writable )
    {
        try
        {
            Constructor<?> init = ( writable ? DirectByteBuffer_init : DirectByteBufferR_init );
            MappedByteBuffer buffer = ( MappedByteBuffer ) init.newInstance( capacity, address, fd, null );
            DirectByteBuffer_att.set( buffer, attachment );
            return buffer;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Failed to create ByteBuffer", e );
        }
    }

    /**
     * long force0( FileDescriptor fd, long address, long length )
     */
    protected static final Method MappedByteBuffer_force0;
    static
    {
        try
        {
            MappedByteBuffer_force0 = MappedByteBuffer.class.getDeclaredMethod( "force0", FileDescriptor.class, long.class, long.class );
            MappedByteBuffer_force0.setAccessible( true );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Cannot access " + MappedByteBuffer.class.getName( ) + ".force0()", e );
        }
    }

    protected static void force( FileDescriptor fd, long address, long length ) throws RuntimeException
    {
        if ( length != 0 )
        {
            try
            {
                MappedByteBuffer buffer = asDirectBuffer( address, 1, fd, null, true );

                long offsetIntoPage = address % pageSize;
                if ( offsetIntoPage < 0 )
                {
                    offsetIntoPage += pageSize;
                }

                long pageStart = address - offsetIntoPage;
                long lengthFromPageStart = length + offsetIntoPage;

                MappedByteBuffer_force0.invoke( buffer, fd, pageStart, lengthFromPageStart );
            }
            catch ( Exception e )
            {
                throw new RuntimeException( "Failed to force mapped file contents to storage device", e );
            }
        }
    }

}
