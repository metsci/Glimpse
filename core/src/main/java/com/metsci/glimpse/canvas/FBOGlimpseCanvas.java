package com.metsci.glimpse.canvas;

import static com.metsci.glimpse.util.logging.LoggerUtils.*;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLFBODrawable;
import javax.media.opengl.GLOffscreenAutoDrawable;
import javax.media.opengl.GLProfile;

import com.jogamp.opengl.FBObject.TextureAttachment;
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.gl.GLRunnable;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.support.texture.ExternalTextureProjected2D;
import com.metsci.glimpse.support.texture.TextureProjected2D;

public class FBOGlimpseCanvas extends AbstractGlimpseCanvas
{
    private static final Logger logger = Logger.getLogger( FBOGlimpseCanvas.class.getName( ) );

    protected GLProfile glProfile;
    protected GLOffscreenAutoDrawable.FBO drawable;
    protected boolean isDisposed;

    public FBOGlimpseCanvas( String glProfileName, GLContext glContext, int width, int height )
    {
        this( GLProfile.get( glProfileName ), glContext, width, height );
    }
    
    public FBOGlimpseCanvas( GLProfile glProfile, GLContext glContext, int width, int height )
    {
        this.glProfile = glProfile;
        this.drawable = ( GLOffscreenAutoDrawable.FBO ) GLUtils.newOffscreenDrawable( this.glProfile, glContext );
        this.drawable.addGLEventListener( createGLEventListener( ) );
        this.drawable.setSize( width, height );
        this.drawable.setRealized( true );
    }
    
    public void resize( int width, int height )
    {
        this.drawable.setSize( width, height );
    }
    
    public BufferedImage toBufferedImage( )
    {
        GLContext glContext = this.drawable.getContext( );
        glContext.makeCurrent( );
        try
        {
            this.paint( );
            AWTGLReadBufferUtil util = new AWTGLReadBufferUtil( this.glProfile, true );
            return util.readPixelsToBufferedImage( glContext.getGL( ), true );   
        }
        finally
        {
            glContext.release( );
        }
    }

    // see: http://forum.jogamp.org/querying-textures-bound-to-default-draw-read-framebuffers-td4026564.html
    public int getTextureUnit( )
    {
        GLFBODrawable delegate = ( GLFBODrawable ) drawable.getDelegatedDrawable( );
        TextureAttachment texAttach = delegate.getTextureBuffer( GL.GL_FRONT );
        return texAttach.getName( );
    }

    public TextureProjected2D getProjectedTexture( )
    {
        return new ExternalTextureProjected2D( getTextureUnit( ), drawable.getWidth( ), drawable.getHeight( ), false );
    }

    public Texture getTexture( )
    {
        return TextureIO.newTexture( drawable.getTextureUnit( ) );
    }

    private GLEventListener createGLEventListener( )
    {
        return new GLEventListener( )
        {
            @Override
            public void init( GLAutoDrawable drawable )
            {
                try
                {
                    GL gl = drawable.getGL( );
                    gl.setSwapInterval( 0 );
                }
                catch ( Exception e )
                {
                    // without this, repaint rate is tied to screen refresh rate on some systems
                    // this doesn't work on some machines (Mac OSX in particular)
                    // but it's not a big deal if it fails
                    logWarning( logger, "Trouble in init.", e );
                }
            }

            @Override
            public void display( GLAutoDrawable drawable )
            {
                for ( GlimpseLayout layout : layoutManager.getLayoutList( ) )
                {
                    layout.paintTo( getGlimpseContext( ) );
                }
            }

            @Override
            public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height )
            {
                for ( GlimpseLayout layout : layoutManager.getLayoutList( ) )
                {
                    layout.layoutTo( getGlimpseContext( ) );
                }
            }

            @Override
            public void dispose( GLAutoDrawable drawable )
            {
                for ( GlimpseLayout layout : layoutManager.getLayoutList( ) )
                {
                    layout.dispose( getGlimpseContext( ) );
                }

                for ( GLRunnable runnable : disposeListeners )
                {
                    runnable.run( drawable.getContext( ) );
                }
            }
        };
    }

    @Override
    public GLProfile getGLProfile( )
    {
        return this.glProfile;
    }
    
    @Override
    public GLOffscreenAutoDrawable.FBO getGLDrawable( )
    {
        return drawable;
    }

    @Override
    public GLContext getGLContext( )
    {
        return drawable.getContext( );
    }

    @Override
    public void paint( )
    {
        drawable.display( );
    }

    @Override
    public GlimpseBounds getTargetBounds( )
    {
        return new GlimpseBounds( new Dimension( drawable.getWidth( ), drawable.getHeight( ) ) );
    }

    @Override
    public GlimpseBounds getTargetBounds( GlimpseTargetStack stack )
    {
        return getTargetBounds( );
    }

    @Override
    public void dispose( )
    {
        if ( !isDisposed )
        {
            this.drawable.destroy( );
            this.isDisposed = true;
        }
    }

    @Override
    public boolean isDisposed( )
    {
        return this.isDisposed;
    }
}