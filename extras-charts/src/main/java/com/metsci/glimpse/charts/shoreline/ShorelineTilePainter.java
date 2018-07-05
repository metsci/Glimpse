package com.metsci.glimpse.charts.shoreline;

import static com.metsci.glimpse.util.GeneralUtils.clamp;
import static com.metsci.glimpse.util.logging.LoggerUtils.logFine;
import static com.metsci.glimpse.util.logging.LoggerUtils.logInfo;
import static java.lang.Math.max;
import static java.lang.Math.toDegrees;
import static java.util.Arrays.binarySearch;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.google.common.io.CountingInputStream;
import com.metsci.glimpse.charts.bathy.TileKey;
import com.metsci.glimpse.charts.bathy.TilePainter;
import com.metsci.glimpse.painter.shape.PolygonPainter;
import com.metsci.glimpse.painter.shape.PolygonPainter.TessellatedPolygon;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.polygon.Polygon;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.vector.Vector2d;

public class ShorelineTilePainter extends TilePainter<TessellatedPolygon>
{
    private static final Logger LOGGER = Logger.getLogger( ShorelineTilePainter.class.getName( ) );

    protected final File file;
    protected final double[] lengthScale;
    protected final Map<MultiLevelKey, Long> keys;
    protected final PolygonPainter painter;

    protected Set<TileKey> loadedGroups;
    protected float[] landColor;

    public ShorelineTilePainter( GeoProjection projection, File file ) throws IOException
    {
        super( projection );
        this.file = file;
        loadedGroups = new HashSet<>( );
        lengthScale = loadLengthScale( file );
        keys = loadOffsets( file );
        logInfo( LOGGER, "Found %,d files in %s", keys.size( ), file );

        painter = new PolygonPainter( );
        painter.displayTimeRange( Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY );
        addPainter( painter );
        setLandColor( GlimpseColor.getBlack( ) );
    }

    public void setLandColor( float[] landColor )
    {
        this.landColor = landColor;
        for ( TileKey groupId : loadedGroups )
        {
            painter.setFillColor( groupId, landColor );
        }
    }

    @SuppressWarnings( "unused" )
    protected double[] loadLengthScale( File file ) throws IOException
    {
        try (RandomAccessFile rf = new RandomAccessFile( file, "r" ))
        {
            int version = rf.readByte( );
            long dataStart = rf.readLong( );
            int numLevels = rf.readInt( );
            double[] levels = new double[numLevels];
            for ( int i = 0; i < levels.length; i++ )
            {
                levels[i] = rf.readFloat( );
            }

            return levels;
        }
    }

    protected Map<MultiLevelKey, Long> loadOffsets( File file ) throws IOException
    {
        try (CountingInputStream cis = new CountingInputStream( new BufferedInputStream( new FileInputStream( file ) ) ))
        {
            DataInputStream dis = new DataInputStream( cis );

            // version
            dis.readByte( );
            long dataStart = dis.readLong( );

            // read levels
            int nLevels = dis.readInt( );
            for ( int i = 0; i < nLevels; i++ )
            {
                dis.readFloat( );
            }

            Map<MultiLevelKey, Long> offsets = new HashMap<>( );
            while ( cis.getCount( ) < dataStart )
            {
                int level = dis.readByte( );
                float minLat = ( float ) toDegrees( dis.readFloat( ) );
                float maxLat = ( float ) toDegrees( dis.readFloat( ) );
                float minLon = ( float ) toDegrees( dis.readFloat( ) );
                float maxLon = ( float ) toDegrees( dis.readFloat( ) );
                long offset = dis.readLong( );
                offsets.put( new MultiLevelKey( level, minLat, maxLat, minLon, maxLon ), offset );
            }

            return offsets;
        }
    }

    @Override
    protected TessellatedPolygon loadTileData( TileKey key )
    {
        try (RandomAccessFile rf = new RandomAccessFile( file, "r" ))
        {
            return readTile( rf, key );
        }
        catch ( IOException ex )
        {
            throw new RuntimeException( ex );
        }
    }

    protected TessellatedPolygon readTile( RandomAccessFile rf, TileKey key ) throws IOException
    {
        long offset = keys.get( key );
        logFine( LOGGER, "Reading tile at offset %,d", offset );
        rf.seek( offset );

        int numVertices = rf.readInt( );
        logFine( LOGGER, "Reading polygon with %,d vertices in %s", numVertices, key );
        ByteBuffer bbuf = ByteBuffer.allocate( numVertices * 2 * Float.BYTES );

        bbuf.rewind( );
        rf.getChannel( ).read( bbuf );
        bbuf.rewind( );
        FloatBuffer buf = bbuf.asFloatBuffer( );

        float[] verts = new float[buf.limit( )];
        for ( int j = 0; j < verts.length; j += 2 )
        {
            float lat = buf.get( );
            float lon = buf.get( );
            if ( lon == -180 )
            {
                lon += 1e-3;
            }
            else if ( lon == 180 )
            {
                lon -= 1e-3;
            }
            Vector2d v = projection.project( LatLonGeo.fromRad( lat, lon ) );
            verts[j] = ( float ) v.getX( );
            verts[j + 1] = ( float ) v.getY( );
        }

        return new TessellatedPolygon( new Polygon( ), verts );
    }

    @Override
    protected void replaceTileData( Collection<Entry<TileKey, TessellatedPolygon>> tileData )
    {
        for ( TileKey old : loadedGroups )
        {
            painter.setFill( old, false );
        }

        for ( Entry<TileKey, TessellatedPolygon> e : tileData )
        {
            TileKey groupId = e.getKey( );
            if ( loadedGroups.contains( groupId ) )
            {
                painter.setFill( groupId, true );
            }
            else
            {
                loadedGroups.add( groupId );
                configurePainter( groupId );
                TessellatedPolygon p = e.getValue( );
                painter.addPolygon( groupId, p, p, 0 );
            }
        }
    }

    protected void configurePainter( Object groupId )
    {
        painter.setFill( groupId, true );
        painter.setFillColor( groupId, landColor );
        painter.setShowLines( groupId, false );
    }

    @Override
    protected Collection<TileKey> getVisibleTiles( Rectangle2D bounds, Stream<Entry<TileKey, Area>> keys )
    {
        int level = getLevel( bounds );
        keys = keys.filter( e -> ( ( MultiLevelKey ) e.getKey( ) ).level == level );
        return super.getVisibleTiles( bounds, keys );
    }

    protected int getLevel( Rectangle2D bounds )
    {
        LatLonGeo c = projection.unproject( bounds.getMinX( ) + bounds.getWidth( ) / 2, bounds.getMinY( ) + bounds.getHeight( ) / 2 );
        LatLonGeo a = projection.unproject( bounds.getMinX( ) + bounds.getWidth( ) / 2, bounds.getMaxY( ) );
        LatLonGeo b = projection.unproject( bounds.getMaxX( ), bounds.getMinY( ) + bounds.getHeight( ) / 2 );

        double dist = max( c.getDistanceTo( a ), c.getDistanceTo( b ) );
        int idx = binarySearch( lengthScale, dist );
        if ( idx < 0 )
        {
            idx = -idx - 1;
        }

        idx = clamp( idx, 0, lengthScale.length - 1 );
        return idx;
    }

    @Override
    protected Collection<TileKey> allKeys( )
    {
        return Collections.unmodifiableCollection( keys.keySet( ) );
    }

    protected static class MultiLevelKey extends TileKey
    {
        public final int level;

        public MultiLevelKey( int level, double minLat, double maxLat, double minLon, double maxLon )
        {
            super( minLat, maxLat, minLon, maxLon );
            this.level = level;
        }

        @Override
        public int hashCode( )
        {
            return super.hashCode( ) * 31 + level;
        }

        @Override
        public boolean equals( Object obj )
        {
            return super.equals( obj ) && obj instanceof MultiLevelKey && ( ( MultiLevelKey ) obj ).level == level;
        }
    }
}
