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
package com.metsci.glimpse.examples.charts.bathy;

import static com.metsci.glimpse.axis.tagged.Tag.TEX_COORD_ATTR;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import com.metsci.glimpse.com.jogamp.opengl.util.awt.TextRenderer;
import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener;
import com.metsci.glimpse.axis.painter.ColorYAxisPainter;
import com.metsci.glimpse.axis.painter.NumericAxisPainter;
import com.metsci.glimpse.axis.painter.label.AxisLabelHandler;
import com.metsci.glimpse.axis.tagged.NamedConstraint;
import com.metsci.glimpse.axis.tagged.OrderedConstraint;
import com.metsci.glimpse.axis.tagged.Tag;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.axis.tagged.TaggedAxisMouseListener1D;
import com.metsci.glimpse.axis.tagged.painter.TaggedPartialColorYAxisPainter;
import com.metsci.glimpse.charts.bathy.TopographyData;
import com.metsci.glimpse.charts.bathy.ContourData;
import com.metsci.glimpse.charts.bathy.ContourPainter;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.gl.texture.ColorTexture1D;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.geo.LatLonTrackPainter;
import com.metsci.glimpse.painter.geo.ScalePainter;
import com.metsci.glimpse.painter.info.AnnotationPainter;
import com.metsci.glimpse.painter.info.CursorTextZPainter;
import com.metsci.glimpse.painter.info.SimpleTextPainter.HorizontalPosition;
import com.metsci.glimpse.painter.info.SimpleTextPainter.VerticalPosition;
import com.metsci.glimpse.painter.texture.TaggedHeatMapPainter;
import com.metsci.glimpse.plot.MapPlot2D;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.colormap.ColorGradients;
import com.metsci.glimpse.support.font.FontUtils;
import com.metsci.glimpse.support.texture.FloatTextureProjected2D;
import com.metsci.glimpse.support.texture.mutator.ColorGradientConcatenator;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.geo.projection.TangentPlane;
import com.metsci.glimpse.util.io.StreamOpener;
import com.metsci.glimpse.util.vector.Vector2d;

/**
 * Data displayed was downloaded from the NOAA/NGDC Bathymetry
 * tool at http://www.ngdc.noaa.gov/mgg/gdas/gd_designagrid.html</p>
 *
 * @author ulman
 */
public class BathymetryExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new BathymetryExample( ) );
    }

    TaggedHeatMapPainter bathymetryPainter;
    ContourPainter contourPainter;
    AnnotationPainter annotationPainter;

    public TaggedHeatMapPainter getBathymetryPainter( )
    {
        return this.bathymetryPainter;
    }

    public ContourPainter getContourPainter( )
    {
        return this.contourPainter;
    }

    public AnnotationPainter getAnnotationPainter( )
    {
        return this.annotationPainter;
    }

    @Override
    public MapPlot2D getLayout( )
    {
        return getLayout( new TangentPlane( LatLonGeo.fromDeg( 20.14, -79.23 ) ) );
    }

    public MapPlot2D getLayout( GeoProjection projection )
    {
        // create a premade heat map window
        MapPlot2D plot = new MapPlot2D( projection )
        {
            @Override
            protected Axis1D createAxisZ( )
            {
                return new TaggedAxis1D( );
            }

            @Override
            protected AxisMouseListener createAxisMouseListenerZ( )
            {
                return new TaggedAxisMouseListener1D( );
            }

            @Override
            protected NumericAxisPainter createAxisPainterZ( AxisLabelHandler tickHandler )
            {
                return new TaggedPartialColorYAxisPainter( tickHandler );
            }
        };

        // add tags for setting the "sea level" which controls how the data is colored
        TaggedAxis1D axisZ = ( TaggedAxis1D ) plot.getAxisZ( );
        axisZ.addTag( "Max", 10000.0 ).setAttribute( TEX_COORD_ATTR, 1.0f );
        axisZ.addTag( "Sea Level", 0.0 ).setAttribute( TEX_COORD_ATTR, 0.5f );
        axisZ.addTag( "Min", -8000.0 ).setAttribute( TEX_COORD_ATTR, 0.0f );

        // add a constraint which disallows moving the max tag above 15000
        axisZ.addConstraint( new NamedConstraint( "MaxConstraint" )
        {
            @Override
            public void applyConstraint( TaggedAxis1D currentAxis, Map<String, Tag> previousTags )
            {
                Tag t = currentAxis.getTag( "Max" );

                // if attempting to set a tag value above 15000, disallow the tag update
                if ( t.getValue( ) > 15000.0 )
                {
                    resetTags( );
                }
            }
        } );

        // set a constraint which enforces the ordering of the tags (and keeps them spaced by 200 units)
        axisZ.addConstraint( new OrderedConstraint( "OrderingConstraint", 200, Arrays.asList( "Min", "Sea Level", "Max" ) ) );

        // load a bathemetry data set from a data file obtained from
        // http://www.ngdc.noaa.gov/mgg/gdas/gd_designagrid.html
        TopographyData bathymetryData;
        try
        {
            bathymetryData = new TopographyData( StreamOpener.fileThenResource.openForRead( "data/Cayman.bathy" ) );
        }
        catch ( IOException e )
        {
            e.printStackTrace( );
            throw new RuntimeException( e );
        }

        // create an OpenGL texture wrapper object
        // the BathymetryData helper class automatically loads
        // the appropriate data and projection into the texture
        FloatTextureProjected2D texture = bathymetryData.getTexture( projection );

        // create a color map which is half bathymetry color scale and half topography color scale
        ColorTexture1D elevationColors = new ColorTexture1D( 1024 );
        elevationColors.mutate( new ColorGradientConcatenator( ColorGradients.bathymetry, ColorGradients.topography ) );

        // create a painter to display the bathymetry data
        bathymetryPainter = new TaggedHeatMapPainter( axisZ );
        bathymetryPainter.setData( texture );
        bathymetryPainter.setColorScale( elevationColors );

        // load the color-map into the plot (so the color scale is displayed on the z axis)
        ( ( ColorYAxisPainter ) plot.getAxisPainterZ( ) ).setColorScale( elevationColors );

        // define an array of contour levels to draw contour lines at
        double[] contourLevels = new double[] { -4000, -3000, -2000, -1000, -900, -800, -700, -600, -500, -400, -300, -200, -100, -50, -10 };

        // generate a set of contour lines using the bathemetry data set and the contour levels
        ContourData contourData = new ContourData( bathymetryData, projection, contourLevels );

        // create a painter to display the contour lines
        contourPainter = new ContourPainter( contourData );
        contourPainter.setLineColor( 1.0f, 1.0f, 1.0f, 0.5f );
        contourPainter.setLineWidth( 1.6f );

        // set the x and y axis bounds based on the extent of the bathemetry data
        bathymetryData.setAxisBounds( plot.getAxis( ), projection );

        // set the size of the z axis (MapFrame hides it by default)
        plot.setAxisSizeZ( 60 );

        // set the z axis bounds
        plot.setMinZ( -5000 );
        plot.setMaxZ( 2000 );

        // show minor tick marks on all the plot axes
        plot.setShowMinorTicksX( true );
        plot.setShowMinorTicksY( true );
        plot.setShowMinorTicksZ( true );

        // add the bathymetry painter to the plot
        plot.addPainter( bathymetryPainter );

        // add the contour painter to the plot
        plot.addPainter( contourPainter );

        // create a painter which displays the cursor position and data value under the cursor
        CursorTextZPainter cursorPainter = new CursorTextZPainter( );
        plot.addPainter( cursorPainter );

        // tell the cursor painter what texture to report data values from
        cursorPainter.setTexture( texture );

        // create a painter to display text annotations
        annotationPainter = new AnnotationPainter( new TextRenderer( FontUtils.getDefaultPlain( 12 ) ) );
        plot.addPainter( annotationPainter );

        // create a painter to display "buoy" positions
        LatLonTrackPainter dotPainter = new LatLonTrackPainter( projection );
        plot.addPainter( dotPainter );
        dotPainter.setShowLines( 1, false );
        dotPainter.setPointSize( 1, 5.0f );
        dotPainter.setPointColor( 1, GlimpseColor.getBlack( ) );

        Vector2d pos = projection.project( LatLonGeo.fromDeg( 19.14, -80.23 ) );
        annotationPainter.addAnnotation( "buoy 125A-3", ( float ) pos.getX( ), ( float ) pos.getY( ), 5, 2, HorizontalPosition.Left, VerticalPosition.Center, GlimpseColor.getGreen( ) );
        dotPainter.addPointGeo( 1, 1, 19.14, -80.23, 0 );

        pos = projection.project( LatLonGeo.fromDeg( 18.88, -80.83 ) );
        annotationPainter.addAnnotation( "buoy 126A-2", ( float ) pos.getX( ), ( float ) pos.getY( ), 5, 2, HorizontalPosition.Left, VerticalPosition.Center, GlimpseColor.getGreen( ) );
        dotPainter.addPointGeo( 1, 1, 18.88, -80.83, 0 );

        pos = projection.project( LatLonGeo.fromDeg( 19.64, -79.50 ) );
        annotationPainter.addAnnotation( "buoy 126A-1", ( float ) pos.getX( ), ( float ) pos.getY( ), 5, 2, HorizontalPosition.Left, VerticalPosition.Center, GlimpseColor.getRed( ) );
        dotPainter.addPointGeo( 1, 1, 19.64, -79.50, 0 );

        pos = projection.project( LatLonGeo.fromDeg( 19.80, -79.08 ) );
        annotationPainter.addAnnotation( "buoy 125B-3", ( float ) pos.getX( ), ( float ) pos.getY( ), 5, 2, HorizontalPosition.Left, VerticalPosition.Center, GlimpseColor.getGreen( ) );
        dotPainter.addPointGeo( 1, 1, 19.80, -79.08, 0 );

        ScalePainter scale = new ScalePainter( );
        scale.setPixelBufferX( 8 );
        scale.setPixelBufferY( 8 );
        plot.addPainter( scale );

        return plot;
    }
}
