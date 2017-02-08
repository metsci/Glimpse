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
package com.metsci.glimpse.axis.painter;

import static com.metsci.glimpse.util.units.time.TimeStamp.currentTime;

import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.TimeZone;

import com.jogamp.opengl.GL2;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.painter.label.TimeAxisLabelHandler;
import com.metsci.glimpse.axis.painter.label.TimeAxisLabelHandler.TimeStruct;
import com.metsci.glimpse.axis.painter.label.TimeAxisLabelHandler.TimeStructFactory;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.util.units.time.Time;
import com.metsci.glimpse.util.units.time.TimeStamp;
import com.metsci.glimpse.util.units.time.format.TimeStampFormat;

/**
 * A horizontal (x) time axis painter.
 *
 * @author ulman
 * @see TimeAxisPainter
 */
public class TimeXAxisPainter extends TimeAxisPainter
{
    public TimeXAxisPainter( TimeZone timeZone, Epoch epoch )
    {
        super( new TimeAxisLabelHandler( timeZone, epoch ) );
    }

    public TimeXAxisPainter( Epoch epoch )
    {
        super( new TimeAxisLabelHandler( epoch ) );
    }

    public TimeXAxisPainter( TimeAxisLabelHandler handler )
    {
        super( handler );
    }

    @Override
    public void paintTo( GlimpseContext context, GlimpseBounds bounds, Axis1D axis )
    {
        super.paintTo( context, bounds, axis );

        if ( textRenderer == null ) return;

        GL2 gl = context.getGL( ).getGL2( );

        int width = bounds.getWidth( );
        int height = bounds.getHeight( );

        if ( width == 0 || height == 0 ) return;

        gl.glMatrixMode( GL2.GL_PROJECTION );
        gl.glLoadIdentity( );
        gl.glOrtho( axis.getMin( ), axis.getMax( ), -0.5, height - 1 + 0.5f, -1, 1 );

        gl.glMatrixMode( GL2.GL_MODELVIEW );
        gl.glLoadIdentity( );

        gl.glColor4fv( tickColor, 0 );

        List<TimeStamp> tickTimes = handler.tickTimes( axis, width );
        double tickInterval = handler.tickInterval( tickTimes );

        // Tick marks
        gl.glBegin( GL2.GL_LINES );
        for ( TimeStamp t : tickTimes )
        {
            double x = fromTimeStamp( t );
            gl.glVertex2d( x, height );
            gl.glVertex2d( x, height - tickLineLength );
        }
        gl.glEnd( );

        if ( showCurrentTimeLabel ) drawCurrentTimeTick( gl, axis, width, height );

        GlimpseColor.setColor( textRenderer, textColor );
        textRenderer.beginRendering( width, height );
        try
        {
            if ( tickInterval <= Time.fromMinutes( 1 ) )
            {
                // Time labels
                double jTimeText = printTickLabels( tickTimes, axis, handler.getSecondMinuteFormat( ), width, height );

                // Date labels
                if ( isShowDateLabels( ) ) printHoverLabels( tickTimes, axis, handler.getHourDayMonthFormat( ), handler.getHourStructFactory( ), jTimeText, width, height );
            }
            else if ( tickInterval <= Time.fromHours( 12 ) )
            {
                // Time labels
                double jTimeText = printTickLabels( tickTimes, axis, handler.getHourMinuteFormat( ), width, height );

                // Date labels
                if ( isShowDateLabels( ) ) printHoverLabels( tickTimes, axis, handler.getDayMonthYearFormat( ), handler.getDayStructFactory( ), jTimeText, width, height );
            }
            else if ( tickInterval <= Time.fromDays( 10 ) )
            {
                // Date labels
                double jTimeText = printTickLabels( tickTimes, axis, handler.getDayFormat( ), width, height );

                // Year labels
                if ( isShowDateLabels( ) ) printHoverLabels( tickTimes, axis, handler.getMonthYearFormat( ), handler.getMonthStructFactory( ), jTimeText, width, height );
            }
            else if ( tickInterval <= Time.fromDays( 60 ) )
            {
                // Date labels
                double jTimeText = printTickLabels( tickTimes, axis, handler.getMonthFormat( ), width, height );

                // Year labels
                if ( isShowDateLabels( ) ) printHoverLabels( tickTimes, axis, handler.getYearFormat( ), handler.getYearStructFactory( ), jTimeText, width, height );
            }
            else
            {
                // Date labels
                printTickLabels( tickTimes, axis, handler.getYearFormat( ), width, height );
            }
        }
        finally
        {
            textRenderer.endRendering( );
        }
    }

    protected TimeStamp getCurrentTime( )
    {
        return currentTime( );
    }

    protected void printHoverLabels( List<TimeStamp> tickTimes, Axis1D axis, TimeStampFormat format, TimeStructFactory factory, double jTimeText, int width, int height )
    {
        // text heights vary slightly, making the labels appear unevenly spaced in height
        // just use the height of a fixed sample character
        Rectangle2D fixedBounds = textRenderer.getBounds( "M" );
        double textHeight = fixedBounds.getHeight( );

        // Date labels
        List<TimeStruct> timeStructs = handler.timeStructs( axis, tickTimes, factory );
        for ( TimeStruct time : timeStructs )
        {
            String text = time.textCenter.toString( format );
            Rectangle2D textBounds = textRenderer.getBounds( text );
            double textWidth = textBounds.getWidth( );

            int iMin = axis.valueToScreenPixel( fromTimeStamp( time.start ) );
            int iMax = ( int ) Math.floor( axis.valueToScreenPixel( fromTimeStamp( time.end ) ) - textWidth );
            int iApprox = ( int ) Math.round( axis.valueToScreenPixel( fromTimeStamp( time.textCenter ) ) - 0.5 * textWidth );
            int i = Math.max( iMin, Math.min( iMax, iApprox ) );
            if ( i < 0 || i + textWidth > width ) continue;

            int j = ( int ) Math.floor( jTimeText - textHeight - hoverLabelOffset );

            textRenderer.draw( text, i, j );
        }
    }

    protected double printTickLabels( List<TimeStamp> tickTimes, Axis1D axis, TimeStampFormat format, int width, int height )
    {
        // text heights vary slightly, making the labels appear unevenly spaced in height
        // just use the height of a fixed sample character
        Rectangle2D fixedBounds = textRenderer.getBounds( "M" );
        double textHeight = fixedBounds.getHeight( );

        // Time labels
        int jTimeText = Integer.MAX_VALUE;
        for ( TimeStamp t : tickTimes )
        {
            String string = t.toString( format );
            Rectangle2D textBounds = textRenderer.getBounds( string );

            double textWidth = textBounds.getWidth( );
            int i = ( int ) Math.round( axis.valueToScreenPixel( fromTimeStamp( t ) ) - 0.5 * textWidth );
            if ( i < 0 || i + textWidth > width ) continue;

            int j = ( int ) Math.round( height - tickLineLength - textHeight );
            jTimeText = Math.min( jTimeText, j );

            textRenderer.draw( string, i, j );
        }

        return jTimeText;
    }

    protected void drawCurrentTimeTick( GL2 gl, Axis1D axis, int width, int height )
    {
        TimeStamp currentTime = getCurrentTime( );
        double axisTime = fromTimeStamp( currentTime );
        int pixelTime = axis.valueToScreenPixel( axisTime );

        gl.glColor4fv( currentTimeTickColor, 0 );
        gl.glLineWidth( currentTimeLineThickness );
        gl.glBegin( GL2.GL_LINES );
        gl.glVertex2d( axisTime, height );
        gl.glVertex2d( axisTime, 0 );
        gl.glEnd( );

        String text = getCurrentTimeTickText( currentTime );

        GlimpseColor.setColor( textRenderer, currentTimeTextColor );
        textRenderer.beginRendering( width, height );
        try
        {
            textRenderer.draw( text, pixelTime + 3, 0 + 3 );
        }
        finally
        {
            textRenderer.endRendering( );
        }
    }

    protected String getCurrentTimeTickText( TimeStamp currentTime )
    {
        return "NOW";
    }
}
