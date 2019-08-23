package com.metsci.glimpse.util.buffer;

import static com.metsci.glimpse.util.buffer.DirectBufferDealloc.deallocateDirectBuffer;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

public class DirectBufferDeallocTest
{

    @Test
    void directBufferDeallocShouldNotBeANoop( )
    {
        assertFalse( DirectBufferDealloc.impl instanceof DirectBufferDealloc.Impl0, "DirectBufferDealloc impl is a NOOP" );
    }

    @Test
    void directBufferDeallocShouldNotThrow( )
    {
        var buffer = ByteBuffer.allocateDirect( 100 * Float.SIZE ).asFloatBuffer( );
        deallocateDirectBuffer( buffer );
    }

}
