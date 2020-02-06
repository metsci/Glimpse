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
package com.metsci.glimpse.core.support.shader.point;

import java.io.IOException;
import java.util.List;

import com.jogamp.opengl.GLContext;
import com.metsci.glimpse.core.axis.Axis1D;
import com.metsci.glimpse.core.axis.listener.AxisListener1D;
import com.metsci.glimpse.core.axis.tagged.Tag;
import com.metsci.glimpse.core.axis.tagged.TaggedAxis1D;

public class TaggedPointAttributeColorSizeProgram extends PointAttributeColorSizeProgram
{
    protected TaggedAxis1D taggedColorAxis;
    protected TaggedAxis1D taggedSizeAxis;

    protected AxisListener1D colorAxisListener;
    protected AxisListener1D sizeAxisListener;

    public TaggedPointAttributeColorSizeProgram( int colorTextureUnit, int sizeTextureUnit, TaggedAxis1D colorAxis, TaggedAxis1D sizeAxis ) throws IOException
    {
        super( colorTextureUnit, sizeTextureUnit, colorAxis, sizeAxis );

        this.taggedColorAxis = ( TaggedAxis1D ) colorAxis;
        this.taggedSizeAxis = ( TaggedAxis1D ) sizeAxis;

        this.colorMin.setData( ( float ) getMinTag( taggedColorAxis ) );
        this.colorMax.setData( ( float ) getMaxTag( taggedColorAxis ) );

        this.colorAxisListener = new AxisListener1D( )
        {
            @Override
            public void axisUpdated( Axis1D handler )
            {
                colorMin.setData( ( float ) getMinTag( taggedColorAxis ) );
                colorMax.setData( ( float ) getMaxTag( taggedColorAxis ) );
            }
        };

        this.taggedColorAxis.addAxisListener( colorAxisListener );

        this.sizeMin.setData( ( float ) getMinTag( taggedSizeAxis ) );
        this.sizeMax.setData( ( float ) getMaxTag( taggedSizeAxis ) );

        this.sizeAxisListener = new AxisListener1D( )
        {
            @Override
            public void axisUpdated( Axis1D handler )
            {
                sizeMin.setData( ( float ) getMinTag( taggedSizeAxis ) );
                sizeMax.setData( ( float ) getMaxTag( taggedSizeAxis ) );
            }
        };

        this.taggedSizeAxis.addAxisListener( sizeAxisListener );

        this.discardAboveColor.setData( 0 );
        this.discardBelowColor.setData( 0 );
        this.discardAboveSize.setData( 0 );
        this.discardBelowSize.setData( 0 );

        this.constantColor.setData( 1 );
        this.constantSize.setData( 1 );
    }

    protected double getMinTag( TaggedAxis1D axis )
    {
        List<Tag> tags = axis.getSortedTags( );
        return tags.get( 0 ).getValue( );
    }

    protected double getMaxTag( TaggedAxis1D axis )
    {
        List<Tag> tags = axis.getSortedTags( );
        return tags.get( tags.size( ) - 1 ).getValue( );
    }

    @Override
    public void dispose( GLContext context )
    {
        super.dispose( context );
        this.taggedColorAxis.removeAxisListener( this.colorAxisListener );
        this.taggedSizeAxis.removeAxisListener( this.sizeAxisListener );
    }
}
