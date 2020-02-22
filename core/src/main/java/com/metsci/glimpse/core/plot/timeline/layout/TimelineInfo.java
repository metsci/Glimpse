/*
 * Copyright (c) 2020, Metron, Inc.
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
package com.metsci.glimpse.core.plot.timeline.layout;

import static com.metsci.glimpse.core.support.font.FontUtils.*;

import java.awt.Font;
import java.util.TimeZone;

import com.metsci.glimpse.core.axis.painter.TimeAxisPainter;
import com.metsci.glimpse.core.axis.painter.TimeXAxisPainter;
import com.metsci.glimpse.core.axis.painter.TimeYAxisPainter;
import com.metsci.glimpse.core.layout.GlimpseAxisLayout1D;
import com.metsci.glimpse.core.layout.GlimpseAxisLayoutX;
import com.metsci.glimpse.core.layout.GlimpseAxisLayoutY;
import com.metsci.glimpse.core.layout.GlimpseLayout;
import com.metsci.glimpse.core.painter.decoration.BorderPainter;
import com.metsci.glimpse.core.painter.group.DelegatePainter;
import com.metsci.glimpse.core.painter.info.SimpleTextPainter;
import com.metsci.glimpse.core.painter.info.SimpleTextPainter.HorizontalPosition;
import com.metsci.glimpse.core.painter.info.SimpleTextPainter.VerticalPosition;
import com.metsci.glimpse.core.plot.stacked.PlotInfo;
import com.metsci.glimpse.core.plot.stacked.PlotInfoWrapper;
import com.metsci.glimpse.core.plot.timeline.StackedTimePlot2D;
import com.metsci.glimpse.core.plot.timeline.data.Epoch;
import com.metsci.glimpse.core.support.color.GlimpseColor;

public class TimelineInfo extends PlotInfoWrapper
{
    protected DelegatePainter timeAxisDelegate;
    protected TimeAxisPainter timeAxisPainter;
    protected SimpleTextPainter timeZonePainter;
    protected BorderPainter borderPainter;

    /**
     * General layout section, empty by default, used for adding additional information
     * to the section of the timeline that aligns with the label layouts of the timeline rows.
     */
    protected GlimpseLayout labelLayout;

    /**
     * Default layout of the timeline sized to be identical to the size of the plot sections of
     * the timeline rows.
     */
    protected GlimpseAxisLayout1D timeLayout;

    protected StackedTimePlot2D plot;

    public TimelineInfo( StackedTimePlot2D plot, PlotInfo info )
    {
        super( info );

        this.plot = plot;

        this.initialize( );
    }

    public void setEpoch( Epoch epoch )
    {
        this.timeAxisPainter.setEpoch( epoch );
    }

    public void setTimeZone( TimeZone timeZone )
    {
        this.timeAxisPainter.getLabelHandler( ).setTimeZone( timeZone );
        this.timeZonePainter.setText( timeZone.getID( ) );
    }

    public TimeZone getTimeZone( )
    {
        return this.timeAxisPainter.getLabelHandler( ).getTimeZone( );
    }

    public DelegatePainter getAxisPainterDelegate( )
    {
        return timeAxisDelegate;
    }

    public TimeAxisPainter getAxisPainter( )
    {
        return timeAxisPainter;
    }

    public SimpleTextPainter getTimeZonePainter( )
    {
        return timeZonePainter;
    }

    public BorderPainter getBorderPainter( )
    {
        return borderPainter;
    }

    /**
     * The layout on which the time axis markings are painted. Aligns with the plot section of the timeline rows.
     * <p>
     * Painters may be added to this layout to draw additional decorations onto the time axis.
     */
    public GlimpseAxisLayout1D getTimelineLayout( )
    {
        return this.timeLayout;
    }

    /**
     * The layout which aligns to the label section of the timeline rows.
     * <p>
     * Painters may be added to this layout to draw additional decorations onto the label section of the time axis.
     */
    public GlimpseLayout getLabelLayout( )
    {
        return this.labelLayout;
    }

    public void setTimeAxisPainter( TimeAxisPainter painter )
    {
        this.timeAxisDelegate.removePainter( this.timeAxisPainter );
        this.timeAxisPainter = painter;
        this.timeAxisDelegate.addPainter( this.timeAxisPainter );
    }

    public void setAxisColor( float[] rgba )
    {
        this.timeAxisPainter.setTextColor( rgba );
        this.timeAxisPainter.setTickColor( rgba );
    }

    public void setAxisFont( Font font )
    {
        this.timeAxisPainter.setFont( font );
    }

    public void setShowCurrentTime( boolean show )
    {
        this.timeAxisPainter.showCurrentTimeLabel( show );
    }

    public void setCurrentTimeColor( float[] rgba )
    {
        this.timeAxisPainter.setCurrentTimeTextColor( rgba );
        this.timeAxisPainter.setCurrentTimeTickColor( rgba );
    }

    protected void initialize( )
    {
        labelLayout = new GlimpseLayout( info.getLayout( ) );

        info.setPlotSpacing( 0 );

        if ( plot.isTimeAxisHorizontal( ) )
        {
            info.setSize( 45 );
            info.setOrder( Integer.MAX_VALUE );

            timeLayout = new GlimpseAxisLayoutX( info.getLayout( ) );
            timeLayout.setEventConsumer( false );
        }
        else
        {
            info.setSize( 60 );
            info.setOrder( Integer.MIN_VALUE );

            timeLayout = new GlimpseAxisLayoutY( info.getLayout( ) );
            timeLayout.setEventConsumer( false );
        }

        info.getLayout( ).setEventConsumer( false );

        timeAxisPainter = createTimeAxisPainter( );
        timeAxisPainter.setFont( getDefaultPlain( 12 ), false );
        timeAxisPainter.showCurrentTimeLabel( false );
        timeAxisPainter.setCurrentTimeTickColor( GlimpseColor.getGreen( ) );

        timeAxisDelegate = new DelegatePainter( );
        timeAxisDelegate.addPainter( timeAxisPainter );

        timeLayout.addPainter( timeAxisDelegate );

        timeZonePainter = new SimpleTextPainter( );
        timeZonePainter.setHorizontalPosition( HorizontalPosition.Right );
        timeZonePainter.setVerticalPosition( VerticalPosition.Bottom );
        timeZonePainter.setColor( GlimpseColor.getBlack( ) );
        timeZonePainter.setFont( getDefaultBold( 12 ) );
        timeZonePainter.setText( "GMT" );
        timeZonePainter.setBackgroundColor( GlimpseColor.getYellow( ) );
        timeZonePainter.setPaintBackground( true );

        borderPainter = new BorderPainter( );
        borderPainter.setVisible( false );

        timeLayout.addPainter( timeZonePainter );
        timeLayout.addPainter( borderPainter );
    }

    protected TimeAxisPainter createTimeAxisPainter( )
    {
        TimeAxisPainter painter;
        if ( plot.isTimeAxisHorizontal( ) )
        {
            painter = new TimeXAxisPainter( plot.getTimeAxisLabelHandler( ) );
        }
        else
        {
            painter = new TimeYAxisPainter( plot.getTimeAxisLabelHandler( ) );
        }

        painter.setFont( getDefaultPlain( 12 ), false );
        painter.showCurrentTimeLabel( false );
        painter.setCurrentTimeTickColor( GlimpseColor.getGreen( ) );

        return painter;
    }

    protected boolean doAnyOtherPlotsGrow( )
    {
        for ( PlotInfo plot : getStackedPlot( ).getAllPlots( ) )
        {
            if ( this != plot && plot.isGrow( ) ) return true;
        }

        return false;
    }

    @Override
    public void updateLayout( int index )
    {
        super.updateLayout( index );

        if ( timeLayout == null ) return;

        // push the timeline plot over so that it lines up with the plot labels
        if ( plot.isTimeAxisHorizontal( ) )
        {
            labelLayout.setLayoutData( String.format( "width %d!", plot.getOverlayLayoutOffsetX( ) ) );
            timeLayout.setLayoutData( String.format( "push x, grow x" ) );
        }
        else
        {
            labelLayout.setLayoutData( String.format( "height %d!, wrap", plot.getOverlayLayoutOffsetY2( ) ) );
            timeLayout.setLayoutData( String.format( "push y, grow y" ) );
        }
    }
}
