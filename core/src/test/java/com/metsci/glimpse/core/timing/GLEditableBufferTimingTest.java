/*
 * Copyright (c) 2020, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.metsci.glimpse.core.timing;

import static com.jogamp.common.nio.Buffers.SIZEOF_FLOAT;
import static com.jogamp.opengl.GL.GL_DYNAMIC_DRAW;
import static com.jogamp.opengl.GL.GL_POINTS;
import static com.metsci.glimpse.core.support.FrameUtils.disposeOnWindowClosing;
import static com.metsci.glimpse.core.support.FrameUtils.newFrame;
import static com.metsci.glimpse.core.support.FrameUtils.showFrameCentered;
import static com.metsci.glimpse.core.support.FrameUtils.stopOnWindowClosing;
import static com.metsci.glimpse.core.timing.GLVersionLogger.addGLVersionLogger;
import static com.metsci.glimpse.util.logging.LoggerUtils.initLogging;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

import java.nio.FloatBuffer;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAnimatorControl;
import com.jogamp.opengl.GLProfile;
import com.metsci.glimpse.core.context.GlimpseBounds;
import com.metsci.glimpse.core.context.GlimpseContext;
import com.metsci.glimpse.core.gl.GLEditableBuffer;
import com.metsci.glimpse.core.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.core.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.core.plot.EmptyPlot2D;
import com.metsci.glimpse.core.support.settings.SwingLookAndFeel;
import com.metsci.glimpse.core.support.shader.triangle.FlatColorProgram;
import com.metsci.glimpse.core.support.swing.NewtSwingEDTGlimpseCanvas;
import com.metsci.glimpse.core.support.swing.SwingEDTAnimator;


public class GLEditableBufferTimingTest
{

    public static void main( String[] args )
    {
        initLogging( GLEditableBufferTimingTest.class.getResource( "logging.properties" ) );

        final EmptyPlot2D plot = new EmptyPlot2D( );
        plot.addPainter( new BackgroundPainter( ) );
        plot.addPainter( new TestPainter( ) );
        plot.addPainter( new FpsPrinter( ) );

        SwingUtilities.invokeLater( new Runnable( )
        {
            public void run( )
            {
                NewtSwingEDTGlimpseCanvas canvas = new NewtSwingEDTGlimpseCanvas( GLProfile.GL3 );
                addGLVersionLogger( canvas );
                canvas.addLayout( plot );
                canvas.setLookAndFeel( new SwingLookAndFeel( ) );

                GLAnimatorControl animator = new SwingEDTAnimator( 1000 );
                animator.add( canvas.getGLDrawable( ) );
                animator.start( );

                JFrame frame = newFrame( "GLEditableBufferTimingTest", canvas, DISPOSE_ON_CLOSE );
                stopOnWindowClosing( frame, animator );
                disposeOnWindowClosing( frame, canvas );
                showFrameCentered( frame );
            }
        } );
    }

    protected static class TestPainter extends GlimpsePainterBase
    {
        protected static final int numIterations = 10000;
        protected static final int verticesPerIteration = 4;
        protected static final int floatsPerIteration = 2 * verticesPerIteration;
        protected static final int bytesPerIteration = SIZEOF_FLOAT * floatsPerIteration;

        protected final FlatColorProgram prog;
        protected final GLEditableBuffer buffer;

        public TestPainter( )
        {
            this.prog = new FlatColorProgram( );
            this.buffer = new GLEditableBuffer( GL_DYNAMIC_DRAW, bytesPerIteration * numIterations );
        }

        @Override
        public void doPaintTo( GlimpseContext context )
        {
            GL3 gl = context.getGL( ).getGL3( );
            GlimpseBounds bounds = getBounds( context );

            this.prog.begin( gl );
            this.prog.setColor( gl, 0, 0, 0, 1 );
            this.prog.setPixelOrtho( gl, bounds );

            this.buffer.clear( );

            for ( int i = 0; i < numIterations; i++ )
            {
                FloatBuffer editFloats = this.buffer.editFloats( i * floatsPerIteration, floatsPerIteration );
                for ( int v = 0; v < verticesPerIteration; v++ )
                {
                    float x = 2 + v + 3*i;
                    float y = 2 + v;
                    editFloats.put( x ).put( y );
                }

                int b = this.buffer.deviceBuffer( gl );
                int n = verticesPerIteration;
                this.prog.draw( gl, GL_POINTS, b, i * verticesPerIteration, n );
            }

            this.prog.end( gl );
        }

        @Override
        protected void doDispose( GlimpseContext context )
        {
            GL gl = context.getGL( );
            this.buffer.dispose( gl );
        }
    }

}
