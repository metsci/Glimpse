/*
 * Copyright (c) 2012, Metron, Inc.
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
package com.metsci.glimpse.docking;

import static com.metsci.glimpse.docking.DockingGroup.DockingFrameCloseOperation.DISPOSE_ALL_FRAMES;
import static com.metsci.glimpse.docking.DockingThemes.tinyLafDockingTheme;
import static com.metsci.glimpse.docking.DockingUtils.newButtonPopup;
import static com.metsci.glimpse.docking.DockingUtils.newToolbar;
import static com.metsci.glimpse.docking.DockingUtils.requireIcon;
import static java.util.Arrays.asList;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import net.sf.tinylaf.Theme;
import net.sf.tinylaf.TinyLookAndFeel;

import com.metsci.glimpse.docking.DockingGroup.DockerArrangementNode;
import com.metsci.glimpse.docking.DockingGroup.DockerArrangementSplit;
import com.metsci.glimpse.docking.DockingGroup.DockerArrangementTile;
import com.metsci.glimpse.docking.DockingGroup.FrameArrangement;
import com.metsci.glimpse.docking.DockingGroup.GroupArrangement;
import com.metsci.glimpse.docking.DockingThemes.DockingTheme;
import com.metsci.glimpse.docking.TileFactories.TileFactory;
import com.metsci.glimpse.docking.TileFactories.TileFactoryStandard;

public class DockingExperiment
{

    public static void main( String[] args ) throws Exception
    {
        Theme.loadTheme( DockingExperiment.class.getClassLoader( ).getResource( "tinylaf/radiance.theme" ) );
        UIManager.setLookAndFeel( new TinyLookAndFeel( ) );
        DockingTheme dockingTheme = tinyLafDockingTheme( );


        DockingGroup dockingGroup = new DockingGroup( "Docking Example", dockingTheme, DISPOSE_ALL_FRAMES );
        TileFactory tileFactory = new TileFactoryStandard( dockingGroup );


        JPanel aPanel = new JPanel( ) {{ setBackground( Color.red ); }};
        JPanel bPanel = new JPanel( ) {{ setBackground( Color.green ); }};
        JPanel cPanel = new JPanel( ) {{ setBackground( Color.blue ); }};
        JPanel dPanel = new JPanel( ) {{ setBackground( Color.cyan ); }};
        JPanel ePanel = new JPanel( ) {{ setBackground( Color.magenta ); }};
        JPanel fPanel = new JPanel( ) {{ setBackground( Color.yellow ); }};
        JPanel gPanel = new JPanel( ) {{ setBackground( Color.gray ); }};
        JPanel hPanel = new JPanel( ) {{ setBackground( Color.white ); }};


        JToolBar aToolbar = newToolbar( true );
        aToolbar.add( new JButton( "A1" ) );
        aToolbar.add( new JButton( "A2" ) );
        aToolbar.add( new JButton( "A3" ) );

        JToggleButton aOptionsButton = new JToggleButton( dockingTheme.optionsIcon );
        JPopupMenu aOptionsPopup = newButtonPopup( aOptionsButton );
        aOptionsPopup.add( new JMenuItem( "Option 1" ) );
        aToolbar.add( aOptionsButton );

        JToolBar bToolbar = newToolbar( true );
        bToolbar.add( new JButton( "B1" ) );

        JToolBar cToolbar = null;

        JToolBar dToolbar = newToolbar( true );
        dToolbar.add( new JButton( "D1" ) );
        dToolbar.add( new JButton( "D2" ) );
        dToolbar.add( new JButton( "D3" ) );
        dToolbar.add( new JButton( "D4" ) );
        dToolbar.add( new JButton( "D5" ) );

        JToolBar eToolbar = newToolbar( true );
        eToolbar.add( new JButton( "E1" ) );
        eToolbar.add( new JButton( "E2" ) );

        JToolBar fToolbar = newToolbar( true );
        fToolbar.add( new JButton( "F1" ) );
        fToolbar.add( new JButton( "F2" ) );
        fToolbar.add( new JButton( "F3" ) );

        JToolBar gToolbar = newToolbar( true );

        JToolBar hToolbar = newToolbar( true );
        hToolbar.add( new JButton( "H1" ) );


        View aView = new View( "aView", aPanel, "View A", null, requireIcon( "icons/ViewA.png" ), aToolbar );
        View bView = new View( "bView", bPanel, "View B", null, requireIcon( "icons/ViewB.png" ), bToolbar );
        View cView = new View( "cView", cPanel, "View C", null, requireIcon( "icons/ViewC.png" ), cToolbar );
        View dView = new View( "dView", dPanel, "View D", null, requireIcon( "icons/ViewD.png" ), dToolbar );
        View eView = new View( "eView", ePanel, "View E", null, requireIcon( "icons/ViewE.png" ), eToolbar );
        View fView = new View( "fView", fPanel, "View F", null, requireIcon( "icons/ViewF.png" ), fToolbar );
        View gView = new View( "gView", gPanel, "View G", null, requireIcon( "icons/ViewG.png" ), gToolbar );
        View hView = new View( "hView", hPanel, "View H", null, requireIcon( "icons/ViewH.png" ), hToolbar );








        DockerArrangementTile aArrTile= new DockerArrangementTile( asList( aView.viewId, bView.viewId ), null, false );
        DockerArrangementTile bArrTile = new DockerArrangementTile( asList( cView.viewId, dView.viewId ), null, false );
        DockerArrangementNode aDockerArr = new DockerArrangementSplit( false, 0.3, aArrTile, bArrTile );

        DockerArrangementTile cArrTile = new DockerArrangementTile( asList( eView.viewId, fView.viewId ), null, false );
        DockerArrangementTile dArrTile = new DockerArrangementTile( asList( gView.viewId, hView.viewId ), null, false );
        DockerArrangementNode bDockerArr = new DockerArrangementSplit( true, 0.75, cArrTile, dArrTile );

        List<FrameArrangement> frameArrs = new ArrayList<>( );
        frameArrs.add( new FrameArrangement( aDockerArr, 50, 50, 1024, 768 ) );
        frameArrs.add( new FrameArrangement( bDockerArr, 1100, 150, 800, 600 ) );

        GroupArrangement groupArr = new GroupArrangement( frameArrs );

        dockingGroup.restoreArrangement( groupArr, tileFactory, aView, bView, cView, dView, eView, fView, gView, hView );


//        Tile aTile = tileFactory.newTile( );
//        aTile.addView( aView, 0 );
//        aTile.addView( bView, 1 );
//        aTile.addView( cView, 2 );
//
//        Tile bTile = tileFactory.newTile( );
//        bTile.addView( dView, 0 );
//        bTile.addView( eView, 1 );
//
//        Tile cTile = tileFactory.newTile( );
//        cTile.addView( fView, 0 );
//        cTile.addView( gView, 1 );
//        cTile.addView( hView, 2 );
//
//
//        DockingFrame frame = dockingGroup.addNewFrame( );
//        DockingPane docker = frame.docker;
//
//        docker.addInitialTile( aTile );
//        docker.addNeighborTile( bTile, aTile, LEFT, 0.3 );
//        docker.addEdgeTile( cTile, BOTTOM, 0.3 );
//
//        frame.setPreferredSize( new Dimension( 1024, 768 ) );
//        frame.pack( );
//        frame.setLocationByPlatform( true );
//        frame.setVisible( true );
    }

}
