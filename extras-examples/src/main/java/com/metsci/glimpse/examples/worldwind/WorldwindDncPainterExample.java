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
package com.metsci.glimpse.examples.worldwind;

import static com.metsci.glimpse.dnc.DncDataPaths.*;
import static com.metsci.glimpse.util.GlimpseDataPaths.*;
import static com.metsci.glimpse.util.io.StreamOpener.*;
import static com.metsci.glimpse.util.logging.LoggerUtils.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.dnc.DncPainter;
import com.metsci.glimpse.dnc.DncPainterSettings;
import com.metsci.glimpse.dnc.DncPainterSettingsImpl;
import com.metsci.glimpse.dnc.DncProjections;
import com.metsci.glimpse.dnc.convert.Flat2Render.RenderCache;
import com.metsci.glimpse.dnc.convert.Flat2Render.RenderCacheConfig;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.worldwind.projection.PlateCarreeProjection;
import com.metsci.glimpse.worldwind.tile.GlimpseResizingSurfaceTile;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.awt.WorldWindowGLJPanel;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.ViewControlsLayer;
import gov.nasa.worldwind.layers.ViewControlsSelectListener;
import gov.nasa.worldwindx.examples.ApplicationTemplate;

public class WorldwindDncPainterExample
{
    public static void main( String[] args ) throws IOException
    {
        initializeLogging( "config/dnc-example-logging.properties", resourceOpener );

        // setup RenderCacheConfig which points to DNC data files
        RenderCacheConfig dncCacheConfig = new RenderCacheConfig( );
        dncCacheConfig.flatParentDir = requireExistingDir( glimpseDncFlatDir );

        // setup projection for transforming DNC_FLAT lat/lon coordinates into flat map coordinates
        // here we choose the identity transform (plate carree) because that is what WorldWind
        // requires when tiling an image onto its globe
        dncCacheConfig.proj = DncProjections.dncPlateCarree;
        RenderCache dncCache = new RenderCache( dncCacheConfig, 4 );

        // setup a Glimpse axis and layout to draw the DNC charts onto
        Axis2D axis = new Axis2D( );
        GlimpseAxisLayout2D plot = new GlimpseAxisLayout2D( axis );

        // configure DNC rendering
        DncPainterSettings dncPainterSettings = new DncPainterSettingsImpl( dncCacheConfig.proj );
        DncPainter dncPainter = new DncPainter( dncCache, dncPainterSettings );
        // removing "ecr" coverage removes dnc land polygons, making WorldWind satellite imagery visible
        dncPainter.activateCoverages( "lim", "nav", "cul", "iwy", "obs", "hyd", "por", "lcr", "env", "rel", "coa" );
        dncPainter.addAxis( plot.getAxis( ) );
        plot.addPainter( dncPainter );

        // setup WorldWind
        JFrame worldwindFrame = new JFrame( "Worldwind" );

        JPanel panel = new JPanel( );
        panel.setLayout( new BorderLayout( ) );
        worldwindFrame.add( panel );

        final WorldWindowGLJPanel wwc = new WorldWindowGLJPanel( );
        wwc.setModel( new BasicModel( ) );

        panel.add( wwc, BorderLayout.CENTER );

        // If Angle.POS180 is used, LatLonGeo will normalize that value to -180, causing axis bounds to be set to [-180, -180]
        Sector fullSector = new Sector( Angle.NEG90, Angle.POS90, Angle.NEG180, Angle.fromDegrees( Math.nextDown( 180 ) ) );
        PlateCarreeProjection projection = new PlateCarreeProjection( );

        // The charts look bad when we attempt to render over the whole globe.
        // So wait to render until the user is zoomed in on a particular area.
        final GlimpseResizingSurfaceTile glimpseLayer = new GlimpseResizingSurfaceTile( plot, axis, projection, 8192, 8192, 1024, 1024, fullSector.asList( ))
        {
            @Override
            protected void updateGeometry( List<LatLon> screenCorners )
            {
                super.updateGeometry( screenCorners );
                layout.setVisible( true );
            }

            @Override
            protected void updateGeometryDefault( )
            {
                layout.setVisible( false );
            }
        };

        glimpseLayer.setOpacity( 0.4f );

        ApplicationTemplate.insertBeforePlacenames( wwc, glimpseLayer );

        // When the WorlWindow changes size, adjust the preferred size of the offscreen GlimpseCanvas to match
        // if they are too different, the view will either look very blurry or lettering will be very small.
        // Also set the size of the Axis2D. The size is used to compute the pixel-per-value for the axis, which
        // in turn determines what level of detail is drawn on the DNC chart.
        panel.addComponentListener( new ComponentAdapter( )
        {
            @Override
            public void componentResized( ComponentEvent e )
            {
                Dimension dim = e.getComponent( ).getSize( );
                glimpseLayer.setPreferredDimensions( ( int ) dim.getWidth( ) * 3, ( int ) dim.getHeight( ) * 3 );
                axis.setSizePixels( new GlimpseBounds( dim ) );
            }
        } );

        // create and install the view controls layer and register a controller for it with the World Window.
        ViewControlsLayer viewControlsLayer = new ViewControlsLayer( );
        ApplicationTemplate.insertBeforeCompass( wwc, viewControlsLayer );
        wwc.addSelectListener( new ViewControlsSelectListener( wwc, viewControlsLayer ) );

        // show the WorldWind frame
        worldwindFrame.setSize( 800, 800 );
        worldwindFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        worldwindFrame.setVisible( true );
    }
}