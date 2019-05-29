/*
 * Copyright (c) 2019, Metron, Inc.
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
package com.metsci.glimpse.examples.projection;

import static com.metsci.glimpse.support.QuickUtils.*;
import static com.metsci.glimpse.util.concurrent.ConcurrencyUtils.*;
import static javax.media.opengl.GLProfile.*;

import javax.swing.SwingUtilities;

import com.metsci.glimpse.examples.heatmap.HeatMapExample;
import com.metsci.glimpse.gl.texture.ColorTexture1D;
import com.metsci.glimpse.painter.info.CursorTextZPainter;
import com.metsci.glimpse.painter.info.FpsPainter;
import com.metsci.glimpse.painter.texture.HeatMapPainter;
import com.metsci.glimpse.plot.ColorAxisPlot2D;
import com.metsci.glimpse.support.colormap.ColorGradients;
import com.metsci.glimpse.support.projection.PolarProjection;
import com.metsci.glimpse.support.projection.Projection;
import com.metsci.glimpse.support.texture.FloatTextureProjected2D;

/**
 * Demonstrates dynamically updating the projection applied to a texture.
 *
 * @author ulman
 */
public class AnimatedPolarProjectionExample
{
    public static void main( String args[] ) throws Exception
    {
        SwingUtilities.invokeLater( ( ) ->
        {
            // create a premade heat map window
            ColorAxisPlot2D plot = new ColorAxisPlot2D( );

            // hide the plot title
            plot.setTitleHeight( 0 );

            // set text labels for the axes
            plot.setAxisLabelX( "x axis" );
            plot.setAxisLabelY( "y axis" );
            plot.setAxisLabelZ( "z axis" );

            // set minimum and maximum values for the axes
            plot.setMinX( -1000 );
            plot.setMaxX( 1000 );
            plot.setMinY( -1000 );
            plot.setMaxY( 1000 );
            plot.setMinZ( 0 );
            plot.setMaxZ( 1000 );

            // lock the aspect ratio of the x and y axes
            plot.lockAspectRatioXY( 1 );

            // add a painter that will use our new shader for color-mapping
            final HeatMapPainter painter = new HeatMapPainter( plot.getAxisZ( ) );
            plot.addPainter( painter );

            // setup the color-map for the painter
            ColorTexture1D colors = new ColorTexture1D( 1024 );
            colors.setColorGradient( ColorGradients.jet );

            // create a projection which will display the data as an annulus with
            // inner radius 300 and outer radius 1000
            Projection projection = new PolarProjection( 300, 1000, 0, 360 );

            // allocate a 1000 by 1000 pixel texture
            final FloatTextureProjected2D texture = new FloatTextureProjected2D( 4000, 1000 );

            // generate dummy data for the texture
            double[][] data = HeatMapExample.generateData( 4000, 1000 );

            // set the projection and data for the texture
            texture.setProjection( projection );
            texture.setData( data );

            // add the texture and color scale to the painter
            painter.setColorScale( colors );
            painter.setData( texture );

            // create a painter which displays the cursor position and data value under the cursor
            CursorTextZPainter cursorPainter = new CursorTextZPainter( );
            plot.addPainter( cursorPainter );

            // tell the cursor painter what texture to report data values from
            cursorPainter.setTexture( texture );

            // also add the color scale to the plot, which will
            // use it to display the color scale on the Z axis
            plot.setColorScale( colors );

            // paints an estimate of how many times per second the display is being updated
            plot.addPainter( new FpsPainter( ) );

            // start a thread which will periodically update the projection
            // being applied to the texture, causing it to appear to rotate and pulsate
            startThread( "Projection Updater", true, ( ) ->
            {
                double startT = 0;
                int endR = 1000;
                int dirR = 1;

                while ( true )
                {
                    startT += .1;

                    if ( startT > 360 ) startT = 0;

                    if ( endR == 1000 ) dirR = -1;
                    if ( endR == 500 ) dirR = 1;
                    endR += dirR;

                    Projection newProjection = new PolarProjection( 300, endR, startT, startT + 360 );

                    // setProjection is thread-safe
                    texture.setProjection( newProjection );

                    try
                    {
                        Thread.sleep( 5 );
                    }
                    catch ( InterruptedException e )
                    {
                    }
                }
            } );

            // create a window and show the plot
            quickGlimpseApp( "Animated Polar Projection Example", GL3bc, 800, 800, plot );
        } );
    }
}
