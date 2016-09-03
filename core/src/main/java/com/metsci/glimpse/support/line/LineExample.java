package com.metsci.glimpse.support.line;

import static com.jogamp.common.nio.Buffers.SIZEOF_FLOAT;
import static com.metsci.glimpse.gl.util.GLUtils.genBuffer;
import static com.metsci.glimpse.support.FrameUtils.disposeOnWindowClosing;
import static com.metsci.glimpse.support.FrameUtils.newFrame;
import static com.metsci.glimpse.support.FrameUtils.showFrameCentered;
import static com.metsci.glimpse.support.FrameUtils.stopOnWindowClosing;
import static com.metsci.glimpse.util.GeneralUtils.floats;
import static java.lang.Math.sqrt;
import static java.lang.System.currentTimeMillis;
import static javax.media.opengl.GL.GL_ARRAY_BUFFER;
import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_MAP_UNSYNCHRONIZED_BIT;
import static javax.media.opengl.GL.GL_MAP_WRITE_BIT;
import static javax.media.opengl.GL.GL_ONE;
import static javax.media.opengl.GL.GL_ONE_MINUS_SRC_ALPHA;
import static javax.media.opengl.GL.GL_SRC_ALPHA;
import static javax.media.opengl.GL2ES2.GL_STREAM_DRAW;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

import java.nio.FloatBuffer;
import java.util.Random;

import javax.media.opengl.GL2ES2;
import javax.media.opengl.GLAnimatorControl;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.base.GlimpsePainter2D;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.plot.Plot2D;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.settings.SwingLookAndFeel;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;
import com.metsci.glimpse.support.swing.SwingEDTAnimator;

public class LineExample
{

    public static double distance( double x0, double y0, double x1, double y1 )
    {
        double dx = x1 - x0;
        double dy = y1 - y0;
        return sqrt( dx*dx + dy*dy );
    }

    public static void put1f( FloatBuffer buffer, double a )
    {
        buffer.put( ( float ) a );
    }

    public static void put2f( FloatBuffer buffer, double a, double b )
    {
        buffer.put( ( float ) a )
              .put( ( float ) b );
    }


    public static void main( String[] args )
    {
        final Plot2D plot = new Plot2D( "" );
        plot.setAxisSizeZ( 0 );
        plot.setTitleHeight( 0 );
        plot.lockAspectRatioXY( 1.0 );

        plot.getLayoutCenter( ).addPainter( new BackgroundPainter( ) );



        plot.getLayoutCenter( ).addPainter( new GlimpsePainter2D( )
        {
            LineStyle style = new LineStyle( );
            LineProgram prog = null;

            int xyVbo = 0;
            int cumulativeDistanceVbo = 0;

            {
                style.rgba = floats( 0.7f, 0, 0, 1 );
                style.thickness_PX = 4;
                style.stippleEnable = true;
                style.stippleScale = 2;
                style.stipplePattern = 0b0001010111111111;
            }

            public void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis2D axis )
            {
                GL2ES2 gl = context.getGL( ).getGL2ES2( );


                if ( prog == null )
                {
                    prog = new LineProgram( gl );
                }

                if ( xyVbo == 0 )
                {
                    xyVbo = genBuffer( gl );
                }

                if ( cumulativeDistanceVbo == 0 )
                {
                    cumulativeDistanceVbo = genBuffer( gl );
                }



                gl.glBlendFuncSeparate( GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA );
                gl.glEnable( GL_BLEND );


                prog.begin( gl );
                try
                {
                    prog.setViewport( gl, bounds );
                    prog.setAxisOrtho( gl, axis );
                    prog.setStyle( gl, style );




                    int maxVertices = 1024;


                    int xyMaxBytes = maxVertices * 2 * SIZEOF_FLOAT;
                    gl.glBindBuffer( GL_ARRAY_BUFFER, xyVbo );
                    gl.glBufferData( GL_ARRAY_BUFFER, xyMaxBytes, null, GL_STREAM_DRAW );
                    FloatBuffer xyBuffer = gl.glMapBufferRange( GL_ARRAY_BUFFER, 0, xyMaxBytes, GL_MAP_WRITE_BIT | GL_MAP_UNSYNCHRONIZED_BIT ).asFloatBuffer( );

                    int cumulativeDistanceMaxBytes = maxVertices * 1 * SIZEOF_FLOAT;
                    gl.glBindBuffer( GL_ARRAY_BUFFER, cumulativeDistanceVbo );
                    gl.glBufferData( GL_ARRAY_BUFFER, cumulativeDistanceMaxBytes, null, GL_STREAM_DRAW );
                    FloatBuffer cumulativeDistanceBuffer = gl.glMapBufferRange( GL_ARRAY_BUFFER, 0, cumulativeDistanceMaxBytes, GL_MAP_WRITE_BIT | GL_MAP_UNSYNCHRONIZED_BIT ).asFloatBuffer( );


                    Random r = new Random( currentTimeMillis( ) / 750 );
                    for ( int i = 0; i < 20; i++ )
                    {
                        double x0 = 2 + 6*r.nextDouble( );
                        double y0 = 2 + 6*r.nextDouble( );

                        double x1 = x0 + ( -1 + 2*r.nextDouble( ) );
                        double y1 = y0 + ( -1 + 2*r.nextDouble( ) );

                        double x2 = x1 + ( -1 + 2*r.nextDouble( ) );
                        double y2 = y1 + ( -1 + 2*r.nextDouble( ) );

                        put2f( xyBuffer, x0, y0 );
                        put2f( xyBuffer, x1, y1 );
                        put1f( cumulativeDistanceBuffer, 0 );
                        put1f( cumulativeDistanceBuffer, distance( x0, y0, x1, y1 ) );

                        put2f( xyBuffer, x1, y1 );
                        put2f( xyBuffer, x2, y2 );
                        put1f( cumulativeDistanceBuffer, distance( x0, y0, x1, y1 ) );
                        put1f( cumulativeDistanceBuffer, distance( x0, y0, x1, y1 ) + distance( x1, y1, x2, y2 ) );
                    }


                    int numVertices = xyBuffer.duplicate( ).flip( ).remaining( ) / 2;

                    gl.glBindBuffer( GL_ARRAY_BUFFER, xyVbo );
                    gl.glUnmapBuffer( GL_ARRAY_BUFFER );

                    gl.glBindBuffer( GL_ARRAY_BUFFER, cumulativeDistanceVbo );
                    gl.glUnmapBuffer( GL_ARRAY_BUFFER );


                    prog.draw( gl, xyVbo, cumulativeDistanceVbo, 0, numVertices );
                }
                finally
                {
                    prog.end( gl );
                }
            }
        } );



        plot.getLayoutCenter( ).addPainter( new GlimpsePainter2D( )
        {
            LineStyle style = new LineStyle( );
            LineProgram prog = null;

            int xyVbo = 0;
            int cumulativeDistanceVbo = 0;

            {
                style.rgba = GlimpseColor.getBlack( );
                style.thickness_PX = 1;
            }

            public void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis2D axis )
            {
                GL2ES2 gl = context.getGL( ).getGL2ES2( );


                if ( prog == null )
                {
                    prog = new LineProgram( gl );
                }

                if ( xyVbo == 0 )
                {
                    xyVbo = genBuffer( gl );
                }

                if ( cumulativeDistanceVbo == 0 )
                {
                    cumulativeDistanceVbo = genBuffer( gl );
                }



                gl.glBlendFuncSeparate( GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA );
                gl.glEnable( GL_BLEND );


                prog.begin( gl );
                try
                {
                    prog.setViewport( gl, bounds );
                    prog.setPixelOrtho( gl, bounds );
                    prog.setStyle( gl, style );

                    float inset_PX = 0.5f * style.thickness_PX;
                    float xLeft_PX = inset_PX;
                    float xRight_PX = bounds.getWidth( ) - inset_PX;
                    float yBottom_PX = inset_PX;
                    float yTop_PX = bounds.getHeight( ) - inset_PX;



                    int maxVertices = 100;


                    int xyMaxBytes = maxVertices * 2 * SIZEOF_FLOAT;
                    gl.glBindBuffer( GL_ARRAY_BUFFER, xyVbo );
                    gl.glBufferData( GL_ARRAY_BUFFER, xyMaxBytes, null, GL_STREAM_DRAW );
                    FloatBuffer xyBuffer = gl.glMapBufferRange( GL_ARRAY_BUFFER, 0, xyMaxBytes, GL_MAP_WRITE_BIT | GL_MAP_UNSYNCHRONIZED_BIT ).asFloatBuffer( );

                    int cumulativeDistanceMaxBytes = maxVertices * 1 * SIZEOF_FLOAT;
                    gl.glBindBuffer( GL_ARRAY_BUFFER, cumulativeDistanceVbo );
                    gl.glBufferData( GL_ARRAY_BUFFER, cumulativeDistanceMaxBytes, null, GL_STREAM_DRAW );
                    FloatBuffer cumulativeDistanceBuffer = gl.glMapBufferRange( GL_ARRAY_BUFFER, 0, cumulativeDistanceMaxBytes, GL_MAP_WRITE_BIT | GL_MAP_UNSYNCHRONIZED_BIT ).asFloatBuffer( );

                    xyBuffer.put( xLeft_PX  ).put( yBottom_PX );
                    xyBuffer.put( xRight_PX ).put( yBottom_PX );
                    cumulativeDistanceBuffer.put( 0 );
                    cumulativeDistanceBuffer.put( xRight_PX - xLeft_PX );

                    xyBuffer.put( xRight_PX ).put( yBottom_PX );
                    xyBuffer.put( xRight_PX ).put( yTop_PX    );
                    cumulativeDistanceBuffer.put( 0 );
                    cumulativeDistanceBuffer.put( yTop_PX - yBottom_PX );

                    xyBuffer.put( xLeft_PX ).put( yBottom_PX );
                    xyBuffer.put( xLeft_PX ).put( yTop_PX    );
                    cumulativeDistanceBuffer.put( 0 );
                    cumulativeDistanceBuffer.put( yTop_PX - yBottom_PX );

                    xyBuffer.put( xLeft_PX  ).put( yTop_PX );
                    xyBuffer.put( xRight_PX ).put( yTop_PX );
                    cumulativeDistanceBuffer.put( 0 );
                    cumulativeDistanceBuffer.put( xRight_PX - xLeft_PX );

                    int numVertices = xyBuffer.duplicate( ).flip( ).remaining( ) / 2;

                    gl.glBindBuffer( GL_ARRAY_BUFFER, xyVbo );
                    gl.glUnmapBuffer( GL_ARRAY_BUFFER );

                    gl.glBindBuffer( GL_ARRAY_BUFFER, cumulativeDistanceVbo );
                    gl.glUnmapBuffer( GL_ARRAY_BUFFER );


                    prog.draw( gl, xyVbo, cumulativeDistanceVbo, 0, numVertices );
                }
                finally
                {
                    prog.end( gl );
                }
            }
        } );



        SwingUtilities.invokeLater( new Runnable( )
        {
            public void run( )
            {
                NewtSwingEDTGlimpseCanvas canvas = new NewtSwingEDTGlimpseCanvas( );
                canvas.addLayout( plot );
                canvas.setLookAndFeel( new SwingLookAndFeel( ) );

                GLAnimatorControl animator = new SwingEDTAnimator( 30 );
                animator.add( canvas.getGLDrawable( ) );
                animator.start( );

                JFrame frame = newFrame( "Line Example", canvas, DISPOSE_ON_CLOSE );
                stopOnWindowClosing( frame, animator );
                disposeOnWindowClosing( frame, canvas );
                showFrameCentered( frame );
            }
        } );
    }

}
