package com.metsci.glimpse.canvas;

import static com.metsci.glimpse.util.logging.LoggerUtils.logInfo;
import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.JPanel;

import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.context.GlimpseContextImpl;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.mouse.swing.MouseWrapperSwing;
import com.metsci.glimpse.gl.GLRunnable;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.support.settings.LookAndFeel;

/**
 * A Swing GlimpseCanvas implementation which draws to a {@link javax.media.opengl.awt.GLCanvas}. This
 * Canvas implementation is not as well supported as the newer JOGL {@link com.jogamp.newt.awt.NewtCanvasAWT}.
 * For this reason, {@link NewtSwingGlimpseCanvas} should generally be prefered over this class.
 * 
 * @author ulman
 * @deprecated see {@link NewtSwingGlimpseCanvas}
 */
@Deprecated
public class SwingLightweightGlimpseCanvas extends JPanel implements GlimpseCanvas
{
    private static final Logger logger = Logger.getLogger( SwingLightweightGlimpseCanvas.class.getName( ) );

    private static final long serialVersionUID = 1L;

    protected GLProfile glProfile;
    protected GLCapabilities glCapabilities;
    protected GLJPanel glCanvas;
    protected GLAutoDrawable glDrawable;

    protected LayoutManager layoutManager;
    protected MouseWrapperSwing mouseHelper;

    protected List<GLRunnable> disposeListeners;

    protected boolean isEventConsumer = true;
    protected boolean isEventGenerator = true;
    protected boolean isDisposed = false;

    public SwingLightweightGlimpseCanvas( )
    {
        this( null );
    }
    
    public SwingLightweightGlimpseCanvas( GLContext context )
    {
        this( GLProfile.GL2GL3, context );
    }
    
    public SwingLightweightGlimpseCanvas( String profile, GLContext context )
    {
        this( true, profile, context );
    }

    public SwingLightweightGlimpseCanvas( boolean setNoEraseBackgroundProperty, String profile, GLContext context )
    {
        if ( setNoEraseBackgroundProperty )
        {
            System.setProperty( "sun.awt.noerasebackground", "true" );
        }

        this.glProfile = GLProfile.get( profile );
        this.glCapabilities = new GLCapabilities( glProfile );

        this.glCanvas = new GLJPanel( glCapabilities, null );

        if ( context != null ) {
            this.glCanvas.setSharedContext( context );
        }
        
        this.mouseHelper = new MouseWrapperSwing( this );
        this.addMouseListener( this.mouseHelper );
        this.addMouseMotionListener( this.mouseHelper );
        this.addMouseWheelListener( this.mouseHelper );

        this.setLayout( new BorderLayout( ) );
        this.add( this.glCanvas, BorderLayout.CENTER );

        this.layoutManager = new LayoutManager( );

        // workaround to enable the panel to shrink
        this.setMinimumSize( new Dimension( 0, 0 ) );

        this.isDisposed = false;

        this.glCanvas.addGLEventListener( createGLEventListener( ) );
        
        this.disposeListeners = new CopyOnWriteArrayList<GLRunnable>( );
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
                // ignore initial reshapes while canvas is not showing
                // (the canvas can report incorrect/transient sizes during this time)
                if ( !glCanvas.isShowing( ) ) return;

                for ( GlimpseLayout layout : layoutManager.getLayoutList( ) )
                {
                    layout.paintTo( getGlimpseContext( ) );
                }
            }

            @Override
            public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height )
            {
                // ignore initial reshapes while canvas is not showing
                // (the canvas can report incorrect/transient sizes during this time)
                if ( !glCanvas.isShowing( ) ) return;

                for ( GlimpseLayout layout : layoutManager.getLayoutList( ) )
                {
                    layout.layoutTo( getGlimpseContext( ) );
                }
            }

            @Override
            public void dispose( GLAutoDrawable drawable )
            {
                logInfo( logger, "Disposing SwingGlimpseCanvas..." );

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
    // the glCanvas covers the entire underlying JPanel, so event listeners should be attached to the glCanvas, not this
    public void addMouseListener( MouseListener listener )
    {
        this.glCanvas.addMouseListener( listener );
    }

    @Override
    // the glCanvas covers the entire underlying JPanel, so event listeners should be attached to the glCanvas, not this
    public void addMouseMotionListener( MouseMotionListener listener )
    {
        this.glCanvas.addMouseMotionListener( listener );
    }

    @Override
    // the glCanvas covers the entire underlying JPanel, so event listeners should be attached to the glCanvas, not this
    public void addMouseWheelListener( MouseWheelListener listener )
    {
        this.glCanvas.addMouseWheelListener( listener );
    }

    @Override
    // the glCanvas covers the entire underlying JPanel, so event listeners should be attached to the glCanvas, not this
    public void removeMouseListener( MouseListener listener )
    {
        this.glCanvas.removeMouseListener( listener );
    }

    @Override
    // the glCanvas covers the entire underlying JPanel, so event listeners should be attached to the glCanvas, not this
    public void removeMouseMotionListener( MouseMotionListener listener )
    {
        this.glCanvas.removeMouseMotionListener( listener );
    }

    @Override
    // the glCanvas covers the entire underlying JPanel, so event listeners should be attached to the glCanvas, not this
    public void removeMouseWheelListener( MouseWheelListener listener )
    {
        this.glCanvas.removeMouseWheelListener( listener );
    }

    @Override
    // the glCanvas covers the entire underlying JPanel, so event listeners should be attached to the glCanvas, not this
    public void addKeyListener( KeyListener listener )
    {
        this.glCanvas.addKeyListener( listener );
    }

    @Override
    // the glCanvas covers the entire underlying JPanel, so event listeners should be attached to the glCanvas, not this
    public void removeKeyListener( KeyListener listener )
    {
        this.glCanvas.removeKeyListener( listener );
    }
    
    @Override
    public GLProfile getGLProfile( )
    {
        return this.glProfile;
    }

    @Override
    public GLAutoDrawable getGLDrawable( )
    {
        return this.glCanvas;
    }

    @Override
    public GlimpseContext getGlimpseContext( )
    {
        return new GlimpseContextImpl( this );
    }

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        for ( GlimpseTarget target : this.layoutManager.getLayoutList( ) )
        {
            target.setLookAndFeel( laf );
        }
    }

    @Override
    public void addLayout( GlimpseLayout layout )
    {
        this.layoutManager.addLayout( layout );
    }

    @Override
    public void addLayout( GlimpseLayout layout, int zOrder )
    {
        this.layoutManager.addLayout( layout, zOrder );
    }

    @Override
    public void setZOrder( GlimpseLayout layout, int zOrder )
    {
        this.layoutManager.setZOrder( layout, zOrder );
    }

    @Override
    public void removeLayout( GlimpseLayout layout )
    {
        this.layoutManager.removeLayout( layout );
    }

    @Override
    public void removeAllLayouts( )
    {
        this.layoutManager.removeAllLayouts( );
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    @Override
    public List<GlimpseTarget> getTargetChildren( )
    {
        // layoutManager returns an unmodifiable list, thus this cast is typesafe
        // (there is no way for the recipient of the List<GlimpseTarget> view to
        // add GlimpseTargets which are not GlimpseLayouts to the list)
        return ( List ) this.layoutManager.getLayoutList( );
    }

    public Dimension getDimension( )
    {
        return this.glCanvas.getSize( );
    }

    @Override
    public GlimpseBounds getTargetBounds( GlimpseTargetStack stack )
    {
        return new GlimpseBounds( getDimension( ) );
    }

    @Override
    public GlimpseBounds getTargetBounds( )
    {
        return getTargetBounds( null );
    }

    @Override
    public void paint( )
    {
        System.out.println( "PAINT");
        this.repaint( );
    }

    @Override
    public GLContext getGLContext( )
    {
        return this.glCanvas.getContext( );
    }

    @Override
    public String toString( )
    {
        return NewtSwingGlimpseCanvas.class.getSimpleName( );
    }

    @Override
    public boolean isEventConsumer( )
    {
        return this.isEventConsumer;
    }

    @Override
    public void setEventConsumer( boolean consume )
    {
        this.isEventConsumer = consume;
    }

    @Override
    public boolean isEventGenerator( )
    {
        return this.isEventGenerator;
    }

    @Override
    public void setEventGenerator( boolean generate )
    {
        this.isEventGenerator = generate;
    }

    @Override
    public boolean isDisposed( )
    {
        return this.isDisposed;
    }

    @Override
    public void dispose( )
    {
        if ( !this.isDisposed )
        {
            if ( this.glCanvas != null ) this.glCanvas.destroy( );
            this.isDisposed = true;
        }
    }

    @Override
    public void addDisposeListener( GLRunnable runnable )
    {
        this.disposeListeners.add( runnable );
    }
}
