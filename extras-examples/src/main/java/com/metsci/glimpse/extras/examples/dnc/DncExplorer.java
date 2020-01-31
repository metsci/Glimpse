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
package com.metsci.glimpse.extras.examples.dnc;

import static com.metsci.glimpse.dnc.DncDataPaths.glimpseDncFlatDir;
import static com.metsci.glimpse.dnc.DncProjections.dncTangentPlane;
import static com.metsci.glimpse.dnc.facc.FaccIo.readFaccAttrs;
import static com.metsci.glimpse.dnc.facc.FaccIo.readFaccFeatures;
import static com.metsci.glimpse.dnc.geosym.DncGeosymThemes.DNC_THEME_NIGHT;
import static com.metsci.glimpse.dnc.geosym.DncGeosymThemes.DNC_THEME_STANDARD;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.sorted;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.startThread;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.takeNewValue;
import static com.metsci.glimpse.docking.DockingFrameCloseOperation.DISPOSE_ALL_FRAMES;
import static com.metsci.glimpse.docking.DockingUtils.requireIcon;
import static com.metsci.glimpse.docking.DockingUtils.resourceUrl;
import static com.metsci.glimpse.docking.DockingUtils.setArrangementAndSaveOnDispose;
import static com.metsci.glimpse.docking.DockingWindowTitlers.createDefaultWindowTitler;
import static com.metsci.glimpse.docking.ViewCloseOption.VIEW_NOT_CLOSEABLE;
import static com.metsci.glimpse.extras.examples.dnc.DncExampleUtils.newLabel;
import static com.metsci.glimpse.support.QuickUtils.initStandardGlimpseApp;
import static com.metsci.glimpse.support.QuickUtils.swingInvokeLater;
import static com.metsci.glimpse.tinylaf.TinyLafUtils.initTinyLaf;
import static com.metsci.glimpse.util.GeneralUtils.floats;
import static com.metsci.glimpse.util.GlimpseDataPaths.requireExistingDir;
import static com.metsci.glimpse.util.logging.LoggerUtils.initLogging;
import static java.awt.Font.BOLD;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import org.jdesktop.swingx.JXTreeTable;

import com.jogamp.opengl.GLAnimatorControl;
import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.dnc.DncChunks.DncChunkKey;
import com.metsci.glimpse.dnc.DncCoverage;
import com.metsci.glimpse.dnc.DncFeature;
import com.metsci.glimpse.dnc.DncLineFeature;
import com.metsci.glimpse.dnc.DncPainter;
import com.metsci.glimpse.dnc.DncPainterSettings;
import com.metsci.glimpse.dnc.DncPainterSettingsImpl;
import com.metsci.glimpse.dnc.DncPointFeature;
import com.metsci.glimpse.dnc.DncQuery;
import com.metsci.glimpse.dnc.convert.Flat2Query.QueryCache;
import com.metsci.glimpse.dnc.convert.Flat2Query.QueryCacheConfig;
import com.metsci.glimpse.dnc.convert.Flat2Render.RenderCache;
import com.metsci.glimpse.dnc.convert.Flat2Render.RenderCacheConfig;
import com.metsci.glimpse.dnc.facc.FaccAttr;
import com.metsci.glimpse.dnc.facc.FaccFeature;
import com.metsci.glimpse.dnc.util.DncMiscUtils.ThrowingRunnable;
import com.metsci.glimpse.dnc.util.SingletonEvictingBlockingQueue;
import com.metsci.glimpse.docking.DockingGroup;
import com.metsci.glimpse.docking.DockingGroupAdapter;
import com.metsci.glimpse.docking.View;
import com.metsci.glimpse.docking.group.frame.DockingGroupMultiframe;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.painter.decoration.BorderPainter;
import com.metsci.glimpse.painter.decoration.CrosshairPainter;
import com.metsci.glimpse.painter.info.FpsPainter;
import com.metsci.glimpse.plot.Plot2D;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.settings.SwingLookAndFeel;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;
import com.metsci.glimpse.support.swing.SwingEDTAnimator;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.miginfocom.swing.MigLayout;

public class DncExplorer
{

    public static void main( String[] args ) throws IOException
    {
        initLogging( DncExplorer.class.getResource( "logging.properties" ) );
        initTinyLaf( );
        initStandardGlimpseApp( );


        // Render config
        //

        RenderCacheConfig renderConfig = new RenderCacheConfig( );
        renderConfig.flatParentDir = requireExistingDir( glimpseDncFlatDir );
        renderConfig.proj = dncTangentPlane( 40.6892, -74.0444 ); // New York

        RenderCache renderCache = new RenderCache( renderConfig, 4 );


        // Query config
        //

        QueryCacheConfig queryConfig = new QueryCacheConfig( );
        queryConfig.flatParentDir = renderConfig.flatParentDir;
        queryConfig.proj = renderConfig.proj;

        QueryCache queryCache = new QueryCache( queryConfig, 4 );


        // Create plot
        //

        Plot2D plot = new Plot2D( "" );
        plot.lockAspectRatioXY( 1 );
        plot.setShowMinorTicksX( true );
        plot.setShowMinorTicksY( true );
        plot.setAxisSizeZ( 0 );
        plot.setTitleHeight( 0 );

        plot.getAxis( ).set( -200, 200, -200, 200 );
        plot.setSelectionSize( 30 );

        BackgroundPainter backgroundPainter = new BackgroundPainter( );

        DncPainterSettings dncPainterSettings = new DncPainterSettingsImpl( renderConfig.proj );
        DncPainter dncPainter = new DncPainter( renderCache, dncPainterSettings );
        dncPainter.addAxis( plot.getAxis( ) );

        CrosshairPainter crosshairPainter = new CrosshairPainter( );

        plot.getLayoutCenter( ).addPainter( backgroundPainter );
        plot.getLayoutCenter( ).addPainter( dncPainter );
        plot.getLayoutCenter( ).addPainter( crosshairPainter );
        plot.getLayoutCenter( ).addPainter( new FpsPainter( ) );
        plot.getLayoutCenter( ).addPainter( new BorderPainter( ) );


        // Create attrs table
        //

        Map<String, FaccFeature> faccFeatures = readFaccFeatures( );
        Map<String, FaccAttr> faccAttrs = readFaccAttrs( );
        DncExplorerTreeTableModel attrsTableModel = new DncExplorerTreeTableModel( faccFeatures, faccAttrs );
        JXTreeTable attrsTable = new JXTreeTable( attrsTableModel );
        attrsTable.getTableHeader( ).setReorderingAllowed( false );
        JScrollPane attrsScroller = new JScrollPane( attrsTable );


        // Create prefs panel
        //

        JPanel prefsPanel = new JPanel( new MigLayout( "fillx, wrap 1", "[fill,grow]" ) );

        prefsPanel.add( newLabel( "Color Theme", BOLD ), "gapy 12" );

        JRadioButton standardThemeRadio = new JRadioButton( "Standard" );
        standardThemeRadio.addItemListener( ( ev ) ->
        {
            if ( standardThemeRadio.isSelected( ) )
            {
                dncPainter.setTheme( DNC_THEME_STANDARD );
                backgroundPainter.setColor( floats( 0.5f, 0.5f, 0.5f, 1 ) );
                crosshairPainter.setCursorColor( GlimpseColor.getBlack( ) );
            }
        } );
        prefsPanel.add( standardThemeRadio, "gapleft 8" );

        JRadioButton nightThemeRadio = new JRadioButton( "Night" );
        nightThemeRadio.addItemListener( ( ev ) ->
        {
            if ( nightThemeRadio.isSelected( ) )
            {
                dncPainter.setTheme( DNC_THEME_NIGHT );
                backgroundPainter.setColor( floats( 0.1f, 0.1f, 0.1f, 1 ) );
                crosshairPainter.setCursorColor( GlimpseColor.getWhite( ) );
            }
        } );
        prefsPanel.add( nightThemeRadio, "gapleft 8" );

        ButtonGroup themeRadioGroup = new ButtonGroup( );
        themeRadioGroup.add( standardThemeRadio );
        themeRadioGroup.add( nightThemeRadio );

        standardThemeRadio.setSelected( true );

        prefsPanel.add( new JSeparator( ), "gapy 12, growx" );
        prefsPanel.add( newLabel( "Coverages", BOLD ) );

        List<DncCoverage> coverages = sorted( renderCache.coverages, ( a, b ) -> ( a.coverageName ).compareToIgnoreCase( b.coverageName ) );
        for ( DncCoverage coverage : coverages )
        {
            JCheckBox coverageCheckbox = new JCheckBox( coverage.coverageName );
            coverageCheckbox.addItemListener( ( ev ) ->
            {
                dncPainter.setCoverageActive( coverage, coverageCheckbox.isSelected( ) );
            } );
            coverageCheckbox.setSelected( true );
            prefsPanel.add( coverageCheckbox, "gapleft 8" );
        }


        // Query
        //

        Predicate<DncFeature> allowHighlight = ( feature ) ->
        {
            if ( feature instanceof DncPointFeature )
            {
                return true;
            }
            else if ( feature instanceof DncLineFeature )
            {
                String coverage = feature.chunkKey.coverage.coverageName;
                return ( coverage.equalsIgnoreCase( "hyd" ) || coverage.equalsIgnoreCase( "ecr" ) || coverage.equalsIgnoreCase( "iwy" ) );
            }
            else
            {
                return false;
            }
        };

        BlockingQueue<DncQuery> queries = new SingletonEvictingBlockingQueue<>( );

        Runnable submitQuery = ( ) ->
        {
            Collection<DncChunkKey> chunkKeys = dncPainter.activeChunkKeys( );

            Axis1D xAxis = plot.getAxisX( );
            float xMin = ( float ) ( xAxis.getSelectionCenter( ) - 0.5 * xAxis.getSelectionSize( ) );
            float xMax = ( float ) ( xAxis.getSelectionCenter( ) + 0.5 * xAxis.getSelectionSize( ) );

            Axis1D yAxis = plot.getAxisY( );
            float yMin = ( float ) ( yAxis.getSelectionCenter( ) - 0.5 * yAxis.getSelectionSize( ) );
            float yMax = ( float ) ( yAxis.getSelectionCenter( ) + 0.5 * yAxis.getSelectionSize( ) );

            queries.add( new DncQuery( chunkKeys, xMin, xMax, yMin, yMax ) );
        };
        dncPainter.addActiveChunksListener( submitQuery );
        plot.addAxisListener( ( axis ) -> submitQuery.run( ) );

        startThread( "DncQuery", true, new ThrowingRunnable( )
        {
            DncQuery oldQuery = null;

            @Override
            public void runThrows( ) throws Exception
            {
                while ( true )
                {
                    DncQuery query = takeNewValue( queries, oldQuery );

                    attrsTableModel.retainChunks( new HashSet<>( query.chunkKeys ) );

                    queryCache.runQuery( query, dncPainter.chunkPriorityFunc, ( chunkKey, features ) ->
                    {
                        List<DncFeature> highlighted = features.stream( ).filter( allowHighlight ).collect( Collectors.toList( ) );

                        attrsTableModel.setChunkFeatures( chunkKey, highlighted );

                        IntCollection featureNums = new IntOpenHashSet( );
                        highlighted.stream( ).mapToInt( f -> f.featureNum ).forEach( featureNums::add );
                        dncPainter.highlightFeatures( chunkKey, featureNums );
                    } );

                    oldQuery = query;
                }
            }
        } );


        // Show
        //

        swingInvokeLater( ( ) ->
        {
            NewtSwingEDTGlimpseCanvas geoCanvas = new NewtSwingEDTGlimpseCanvas( );
            geoCanvas.addLayout( plot );
            geoCanvas.setLookAndFeel( new SwingLookAndFeel( ) );

            GLAnimatorControl animator = new SwingEDTAnimator( 30 );
            animator.add( geoCanvas.getGLDrawable( ) );
            animator.start( );

            View[] views =
            {
                new View( "geoView",   geoCanvas,     "Geo",      VIEW_NOT_CLOSEABLE, null, requireIcon( "icons/fugue/map.png"        ) ),
                new View( "attrsView", attrsScroller, "Features", VIEW_NOT_CLOSEABLE, null, requireIcon( "icons/eclipse/class_hi.gif" ) ),
                new View( "prefsView", prefsPanel,    "Prefs",    VIEW_NOT_CLOSEABLE, null, requireIcon( "icons/fugue/equalizer.png"  ) ),
            };

            DockingGroup dockingGroup = new DockingGroupMultiframe( DISPOSE_ALL_FRAMES );
            dockingGroup.addListener( createDefaultWindowTitler( "DNC Explorer" ) );
            setArrangementAndSaveOnDispose( dockingGroup, "dnc-explorer", resourceUrl( DncExplorer.class, "dnc-examples/docking-defaults.xml" ) );

            dockingGroup.addListener( new DockingGroupAdapter( )
            {
                @Override
                public void disposingAllWindows( DockingGroup group )
                {
                    attrsTableModel.dispose( );
                    geoCanvas.destroy( );
                }
            } );

            dockingGroup.addViews( views );
            dockingGroup.setVisible( true );
        } );
    }

}
