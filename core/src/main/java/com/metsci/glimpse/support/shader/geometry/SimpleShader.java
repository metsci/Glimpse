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
package com.metsci.glimpse.support.shader.geometry;

import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;
import static jogamp.opengl.glu.error.Error.gluErrorString;

import java.io.IOException;
import java.util.logging.Logger;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

import com.metsci.glimpse.gl.shader.Shader;
import com.metsci.glimpse.gl.shader.ShaderSource;
import com.metsci.glimpse.gl.shader.ShaderType;
import com.metsci.glimpse.util.io.StreamOpener;

public class SimpleShader extends Shader
{
    public SimpleShader( String name, ShaderType type, String shaderFile )
    {
        super( name, type, getSource( shaderFile ) );
    }

    public static Shader passVertex( )
    {
        return new SimpleShader( "passthrough", ShaderType.vertex, "shaders/geometry/passthrough.vs" );
    }

    public static Shader passFragment( )
    {
        return new SimpleShader( "passthrough", ShaderType.fragment, "shaders/geometry/passthrough.fs" );
    }

    public static ShaderSource getSource( String shaderFile )
    {
        try
        {
            return new ShaderSource( shaderFile, StreamOpener.fileThenResource );
        }
        catch ( IOException ioe )
        {
            throw new RuntimeException( ioe );
        }
    }

    private static final Logger logger = Logger.getLogger( SimpleShader.class.getName( ) );

    protected void logGlError( GL gl )
    {
        int error = gl.glGetError( );
        if ( error != GL2.GL_NO_ERROR )
        {
            String errorString = gluErrorString( error );
            logWarning( logger, "GL error (%d): %s", error, errorString );
        }
    }

    @Override
    public boolean preLink( GL gl, int glProgramHandle )
    {
        return true;
    }

    @Override
    public void preDisplay( GL gl )
    {
    }

    @Override
    public void postDisplay( GL gl )
    {
    }
}
