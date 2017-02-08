/*
 * Copyright (c) 2016, Metron, Inc.
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
package com.metsci.glimpse.plot.timeline.painter;

import java.awt.geom.Rectangle2D;
import java.util.List;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLContext;

import com.metsci.glimpse.axis.tagged.Tag;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.plot.stacked.StackedPlot2D.Orientation;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;
import com.metsci.glimpse.support.color.GlimpseColor;

public class SelectedTimeRegionPainter extends SimpleSelectedTimeRegionPainter
{
    protected StackedTimePlot2D plot;

    protected boolean showLockedStatus = true;

    public SelectedTimeRegionPainter( StackedTimePlot2D plot )
    {
        super( plot.getOrientation( ) );
        this.plot = plot;
    }

    public void setShowLockedStatus( boolean show )
    {
        this.showLockedStatus = show;
    }

    protected void paint( GL2 gl, TaggedAxis1D taggedAxis, List<Tag> tags, float min, float max, float current, int width, int height )
    {
        super.paint( gl, taggedAxis, tags, min, max, current, width, height );

        if ( plot.isLocked( ) && showLockedStatus )
        {
            String text = "LOCKED";

            int value = taggedAxis.valueToScreenPixel( current );
            Rectangle2D textbounds = textRenderer.getBounds( text );

            GlimpseColor.setColor( textRenderer, selectionBorderColor );
            textRenderer.beginRendering( width, height );
            try
            {
                if ( orientation == Orientation.VERTICAL )
                {
                    textRenderer.draw( text, ( int ) ( value - textbounds.getWidth( ) - 3 ), 3 );
                }
                else
                {
                    textRenderer.draw( text, 3, ( int ) ( value - textbounds.getHeight( ) - 3 ) );
                }
            }
            finally
            {
                textRenderer.endRendering( );
            }
        }
    }

    @Override
    public void dispose( GLContext context )
    {
        // nothing to dispose
    }
}
