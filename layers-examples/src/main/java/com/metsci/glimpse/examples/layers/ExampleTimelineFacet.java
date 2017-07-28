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
package com.metsci.glimpse.examples.layers;

import static com.metsci.glimpse.axis.painter.label.AxisUnitConverters.suShownAsFeet;
import static com.metsci.glimpse.examples.layers.ExampleTrait.requireExampleTrait;
import static com.metsci.glimpse.layers.geo.GeoTrait.requireGeoTrait;
import static com.metsci.glimpse.layers.misc.AxisUtils.addAxisListener2D;
import static com.metsci.glimpse.layers.misc.AxisUtils.addTaggedAxisListener1D;
import static com.metsci.glimpse.layers.time.TimeTrait.requireTimeTrait;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.layers.geo.GeoTrait;
import com.metsci.glimpse.layers.time.TimeTrait;
import com.metsci.glimpse.layers.time.TimelineView;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.layout.TimePlotInfo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.var.DisposableGroup;
import com.metsci.glimpse.util.var.Listenable;
import com.metsci.glimpse.util.var.ListenableGroup;
import com.metsci.glimpse.util.var.VarEvent;
import com.metsci.glimpse.util.vector.Vector2d;

public class ExampleTimelineFacet extends ExampleFacet
{
    // Multiple facets can share a timeline row by using the same rowId
    public static final String exampleTimelineFacetRowId = "z_suShownAsFeet";


    protected final ExampleLayer layer;

    protected final TimelineView view;
    protected final GeoTrait geoTrait;
    protected final TimeTrait timeTrait;
    protected final ExampleTrait exampleTrait;

    protected final TimePlotInfo row;
    protected final ExampleTimelinePainter painter;

    protected final DisposableGroup disposables;


    public ExampleTimelineFacet( ExampleLayer layer, TimelineView view, ExampleStyle style )
    {
        this.layer = layer;

        this.view = view;
        this.geoTrait = requireGeoTrait( this.view );
        this.timeTrait = requireTimeTrait( this.view );
        this.exampleTrait = requireExampleTrait( this.view );

        this.row = view.acquirePlotRow( exampleTimelineFacetRowId, "Example (FT)", suShownAsFeet, ( newRow ) ->
        {
            Axis1D zAxis = newRow.getOrthogonalAxis( );
            zAxis.setParent( this.exampleTrait.zAxis );
        } );

        this.painter = new ExampleTimelinePainter( style );
        this.row.addPainter( this.painter );

        this.disposables = new DisposableGroup( );

        Axis2D geoAxis = this.geoTrait.axis;
        this.disposables.add( addAxisListener2D( geoAxis, true, ( axis ) ->
        {
            Axis1D xAxis = geoAxis.getAxisX( );
            Axis1D yAxis = geoAxis.getAxisY( );

            float xMin = ( float ) ( xAxis.getSelectionCenter( ) - 0.5*xAxis.getSelectionSize( ) );
            float xMax = ( float ) ( xAxis.getSelectionCenter( ) + 0.5*xAxis.getSelectionSize( ) );
            float yMin = ( float ) ( yAxis.getSelectionCenter( ) - 0.5*yAxis.getSelectionSize( ) );
            float yMax = ( float ) ( yAxis.getSelectionCenter( ) + 0.5*yAxis.getSelectionSize( ) );

            this.painter.setXyWindow( xMin, xMax, yMin, yMax );
        } ) );

        TaggedAxis1D timeAxis = this.timeTrait.axis;
        this.disposables.add( addTaggedAxisListener1D( timeAxis, true, ( axis ) ->
        {
            Epoch epoch = this.timeTrait.epoch;

            float tMin = ( float ) epoch.fromPosixMillis( this.timeTrait.selectionMin_PMILLIS( ) );
            float tMax = ( float ) epoch.fromPosixMillis( this.timeTrait.selectionMax_PMILLIS( ) );

            this.painter.setTWindow( tMin, tMax );
        } ) );

        Listenable<VarEvent> visibilityGroup = new ListenableGroup<>( this.isVisible, this.layer.isVisible );
        this.disposables.add( visibilityGroup.addListener( true, ( ) ->
        {
            this.painter.setVisible( this.layer.isVisible.v( ) && this.isVisible.v( ) );
        } ) );
    }

    @Override
    public void addPoint( ExamplePoint point )
    {
        Epoch epoch = this.timeTrait.epoch;
        float t = ( float ) epoch.fromPosixMillis( point.time_PMILLIS );

        GeoProjection geoProj = this.geoTrait.proj;
        Vector2d xy_SU = geoProj.project( point.latlon );
        float x = ( float ) xy_SU.getX( );
        float y = ( float ) xy_SU.getY( );

        float z = ( float ) point.z_SU;

        this.painter.addPoint( t, x, y, z );
    }

    @Override
    public void dispose( boolean reinstalling )
    {
        this.disposables.dispose( );
        this.row.removePainter( this.painter );
        this.view.releaseRow( this.row.getId( ), reinstalling );

        this.view.glimpseInvoke( ( context ) ->
        {
            this.painter.dispose( context );
            return true;
        } );
    }

}
