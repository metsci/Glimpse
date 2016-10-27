package com.metsci.glimpse.gl.shader;

import java.util.Collection;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;
import javax.media.opengl.GLArrayData;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLUniformData;

import com.google.common.collect.Lists;
import com.jogamp.opengl.util.GLArrayDataClient;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.jogamp.opengl.util.glsl.ShaderState;
import com.metsci.glimpse.gl.util.GLUtils;

/**
 * Simple Glimpse-specific convenience wrapper around {@link ShaderProgram}.
 *
 * @author ulman
 */
public class GlimpseShaderProgram
{
    protected Collection<ShaderCode> codes;
    protected Collection<GLUniformData> uniforms;
    protected Collection<GLArrayDataClient> arrays;

    protected ShaderProgram program;
    protected ShaderState state;

    protected boolean loaded;

    public GlimpseShaderProgram( )
    {
        this.codes = Lists.newArrayList( );
        this.uniforms = Lists.newArrayList( );
        this.arrays = Lists.newArrayList( );
    }

    public ShaderProgram getShaderProgram( )
    {
        return this.program;
    }

    public void loadProgram( GL gl )
    {
        load( gl.getGL3( ), this.codes );
    }

    public void useProgram( GL gl, boolean on )
    {
        if ( on )
        {
            gl.getGL3( ).glBindVertexArray( GLUtils.defaultVertexAttributeArray( gl ) );
        }

        if ( !load( gl.getGL3( ), this.codes ) ) return;

        this.state.useProgram( gl.getGL3( ), on );

        if ( on ) this.updateUniformData( gl );

        for ( GLArrayDataClient array : arrays )
        {
            if ( array.sealed( ) )
            {
                array.enableBuffer( gl, on );
            }
        }

        if ( !on )
        {
            gl.getGL3( ).glBindVertexArray( 0 );
        }
    }

    public ShaderCode addFragmentShader( String path )
    {
        return addShader( GL3.GL_FRAGMENT_SHADER, path );
    }

    public ShaderCode addVertexShader( String path )
    {
        return addShader( GL3.GL_VERTEX_SHADER, path );
    }

    public ShaderCode addGeometryShader( String path )
    {
        return addShader( GL3.GL_GEOMETRY_SHADER, path );
    }

    public ShaderCode addShader( int type, String path )
    {
        return this.addShader( ShaderCode.create( null, type, 1, getClass( ), new String[] { path }, true ) );
    }

    public ShaderCode addShader( ShaderCode code )
    {
        codes.add( code );
        return code;
    }

    public GLArrayDataClient addArrayData( GLArrayDataClient array )
    {
        this.arrays.add( array );
        return array;
    }

    public GLUniformData addUniformData( GLUniformData uniform )
    {
        this.uniforms.add( uniform );
        return uniform;
    }

    public void dispose( GLContext context )
    {
        if ( this.loaded )
        {
            this.state.destroy( context.getGL( ).getGL3( ) );
            this.loaded = false;
        }
    }

    protected void updateUniformData( GL gl )
    {
        GL3 gl3 = gl.getGL3( );

        for ( GLUniformData uniform : uniforms )
        {
            this.state.uniform( gl3, uniform );
        }
    }

    protected boolean load( GL gl, Collection<ShaderCode> codes )
    {
        if ( this.loaded ) return true;

        GL3 gl3 = gl.getGL3( );

        this.state = new ShaderState( );
        this.state.setVerbose( false );
        this.program = new ShaderProgram( );

        for ( ShaderCode code : codes )
        {
            boolean success = this.program.add( gl3, code, System.err );

            if ( !success )
            {
                return false;
            }
        }

        this.state.attachShaderProgram( gl3, this.program, true );

        for ( GLArrayData array : arrays )
        {
            this.state.ownAttribute( array, true );
        }

        for ( GLUniformData uniform : uniforms )
        {
            this.state.ownUniform( uniform );
        }

        this.loaded = true;

        return true;
    }
}
