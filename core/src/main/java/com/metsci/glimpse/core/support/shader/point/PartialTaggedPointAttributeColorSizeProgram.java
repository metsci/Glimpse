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

import static com.metsci.glimpse.core.axis.tagged.Tag.TEX_COORD_ATTR;

import java.io.IOException;
import java.util.List;

import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLUniformData;
import com.metsci.glimpse.core.axis.Axis1D;
import com.metsci.glimpse.core.axis.listener.AxisListener1D;
import com.metsci.glimpse.core.axis.tagged.Tag;
import com.metsci.glimpse.core.axis.tagged.TaggedAxis1D;

/**
 * A point shader attached to a TaggedAxis1D which allows adjusting of the color
 * scale at multiple tag points (as opposed to TaggedPointShader which only
 * reads the lowest and highest tags and spreads the color scale between those).
 *
 * @author ulman
 *
 */
public class PartialTaggedPointAttributeColorSizeProgram extends TaggedPointAttributeColorSizeProgram implements AxisListener1D
{
    private GLUniformData lengthArg;

    public PartialTaggedPointAttributeColorSizeProgram( int colorTextureUnit, int sizeTextureUnit, int vertexTexUnit, int textureTexUnit, int colorAttributeIndex, int sizeAttributeIndex, TaggedAxis1D colorAxis, TaggedAxis1D sizeAxis ) throws IOException
    {
        super( colorTextureUnit, sizeTextureUnit, colorAxis, sizeAxis );

        this.addUniformData( new GLUniformData( "vcoordtex", vertexTexUnit ) );
        this.addUniformData( new GLUniformData( "tcoordtex", textureTexUnit ) );
        this.lengthArg = this.addUniformData( new GLUniformData( "length", getLengthArgValue( ) ) );

        this.taggedSizeAxis.addAxisListener( this );
    }

    @Override
    protected void addDefaultVertexShader( )
    {
        this.addVertexShader( PartialTaggedPointAttributeColorSizeProgram.class.getResource( "tagged_point_attribute_color_size/point.vs" ) );
        this.addFragmentShader( PartialTaggedPointAttributeColorSizeProgram.class.getResource( "tagged_point_attribute_color_size/point.fs" ) );
    }

    @Override
    public void axisUpdated( Axis1D axis )
    {
        this.lengthArg.setData( getLengthArgValue( ) );
    }

    protected int getLengthArgValue( )
    {
        List<Tag> tags = taggedColorAxis.getSortedTags( );
        int size = tags.size( );

        int count = 0;
        for ( int i = size - 1; i >= 0; i-- )
        {
            Tag tag = tags.get( i );

            if ( tag.hasAttribute( TEX_COORD_ATTR ) ) count++;
        }

        return count;
    }

    @Override
    public void dispose( GLContext context )
    {
        super.dispose( context );
        this.taggedSizeAxis.removeAxisListener( this );
    }
}
