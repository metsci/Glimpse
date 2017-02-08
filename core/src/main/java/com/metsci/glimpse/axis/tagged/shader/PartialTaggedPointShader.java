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
package com.metsci.glimpse.axis.tagged.shader;

import static com.metsci.glimpse.axis.tagged.Tag.TEX_COORD_ATTR;

import java.io.IOException;
import java.util.List;

import com.jogamp.opengl.GLContext;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.listener.AxisListener1D;
import com.metsci.glimpse.axis.tagged.Tag;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.gl.shader.ShaderArg;
import com.metsci.glimpse.gl.shader.ShaderSource;
import com.metsci.glimpse.util.io.StreamOpener;

public class PartialTaggedPointShader extends TaggedPointShader implements AxisListener1D
{
    private ShaderArg vertexCoordTexUnit;
    private ShaderArg textureCoordTexUnit;

    private ShaderArg sizeArg;

    public PartialTaggedPointShader( int colorTextureUnit, int sizeTextureUnit, int vertexTexUnit, int textureTexUnit, int colorAttributeIndex, int sizeAttributeIndex, TaggedAxis1D colorAxis, TaggedAxis1D sizeAxis, ShaderSource... source ) throws IOException
    {
        super( colorTextureUnit, sizeTextureUnit, colorAttributeIndex, sizeAttributeIndex, colorAxis, sizeAxis, source );

        this.vertexCoordTexUnit.setValue( vertexTexUnit );
        this.textureCoordTexUnit.setValue( textureTexUnit );
        this.setSizeArgValue( );

        sizeAxis.addAxisListener( this );
    }

    public PartialTaggedPointShader( int colorTextureUnit, int sizeTextureUnit, int vertexTexUnit, int textureTexUnit, int colorAttributeIndex, int sizeAttributeIndex, TaggedAxis1D colorAxis, TaggedAxis1D sizeAxis ) throws IOException
    {
        this( colorTextureUnit, sizeTextureUnit, vertexTexUnit, textureTexUnit, colorAttributeIndex, sizeAttributeIndex, colorAxis, sizeAxis, readSource( ) );
    }

    @Override
    protected void initializeShaderArgs( )
    {
        super.initializeShaderArgs( );

        this.vertexCoordTexUnit = getArg( "vcoordtex" );
        this.textureCoordTexUnit = getArg( "tcoordtex" );
        this.sizeArg = getArg( "size" );
    }

    private final static ShaderSource readSource( ) throws IOException
    {
        return new ShaderSource( "shaders/point/tagged_point_shader.vs", StreamOpener.fileThenResource );
    }

    @Override
    public void axisUpdated( Axis1D axis )
    {
        setSizeArgValue( );
    }

    protected void setSizeArgValue( )
    {
        List<Tag> tags = taggedColorAxis.getSortedTags( );
        int size = tags.size( );

        int count = 0;
        for ( int i = size - 1; i >= 0; i-- )
        {
            Tag tag = tags.get( i );

            if ( tag.hasAttribute( TEX_COORD_ATTR ) ) count++;
        }

        this.sizeArg.setValue( count );
    }

    @Override
    public void dispose( GLContext context )
    {
        super.dispose( context );
        this.taggedSizeAxis.removeAxisListener( this );
    }
}
