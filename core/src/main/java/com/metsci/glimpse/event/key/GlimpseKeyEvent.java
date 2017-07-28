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
package com.metsci.glimpse.event.key;

import static com.metsci.glimpse.context.TargetStackUtil.newTargetStack;

import java.util.EnumSet;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.mouse.ModifierKey;
import com.metsci.glimpse.layout.GlimpseAxisLayout1D;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;

/**
 * A Glimpse-specific KeyEvent implementation which allows Glimpse axis handling
 * code to be written in a widget framework (NEWT, Swing, etc...) independent manner.
 *
 * @author hogye
 */
public class GlimpseKeyEvent
{

    protected GlimpseTargetStack stack;
    protected EnumSet<ModifierKey> modifiers;
    protected GlimpseKey key;
    protected boolean handled;

    public GlimpseKeyEvent( GlimpseTargetStack stack, EnumSet<ModifierKey> modifiers, GlimpseKey key )
    {
        this( stack, modifiers, key, false );
    }

    public GlimpseKeyEvent( GlimpseKeyEvent ev )
    {
        this( newTargetStack( ev.stack ), EnumSet.copyOf( ev.modifiers ), ev.key, ev.handled );
    }

    public GlimpseKeyEvent( GlimpseTargetStack stack, EnumSet<ModifierKey> modifiers, GlimpseKey key, boolean handled )
    {
        this.stack = stack;
        this.modifiers = modifiers;
        this.key = key;
        this.handled = false;
    }

    public boolean isHandled( )
    {
        return this.handled;
    }

    public void setHandled( boolean handled )
    {
        this.handled = handled;
    }

    public EnumSet<ModifierKey> getModifiers( )
    {
        return this.modifiers;
    }

    public GlimpseKey getKey( )
    {
        return this.key;
    }

    public GlimpseTargetStack getTargetStack( )
    {
        return this.stack;
    }

    public Axis1D getAxis1D( )
    {
        GlimpseTargetStack stack = this.getTargetStack( );
        GlimpseTarget target = stack.getTarget( );

        if ( target instanceof GlimpseAxisLayout1D )
        {
            GlimpseAxisLayout1D layout = ( GlimpseAxisLayout1D ) target;
            return layout.getAxis( stack );
        }
        else
        {
            return null;
        }
    }

    public Axis2D getAxis2D( )
    {
        GlimpseTargetStack stack = this.getTargetStack( );
        GlimpseTarget target = stack.getTarget( );

        if ( target instanceof GlimpseAxisLayout2D )
        {
            GlimpseAxisLayout2D layout = ( GlimpseAxisLayout2D ) target;
            return layout.getAxis( stack );
        }
        else
        {
            return null;
        }
    }

    @Override
    public String toString( )
    {
        return String.format( "code: %s modifier: %s", this.key, this.modifiers );
    }

}
