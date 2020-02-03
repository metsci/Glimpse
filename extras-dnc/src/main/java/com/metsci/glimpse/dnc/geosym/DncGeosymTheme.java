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
package com.metsci.glimpse.dnc.geosym;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Objects.equal;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.readText;

import java.net.URL;
import java.util.function.Function;

import com.google.common.base.Objects;
import com.metsci.glimpse.dnc.geosym.DncGeosymImageUtils.KeyedTextLoader;
import com.metsci.glimpse.dnc.geosym.DncGeosymImageUtils.TextLoader;

public class DncGeosymTheme
{

    public final TextLoader colorsLoader;
    public final TextLoader lineAreaStylesLoader;
    public final KeyedTextLoader cgmLoader;
    public final KeyedTextLoader svgLoader;


    public DncGeosymTheme( URL colorsUrl,
                           URL lineAreaStylesUrl,
                           Function<String,URL> cgmUrlForSymbolId,
                           Function<String,URL> svgUrlForSymbolId )
    {
        this( ( ) -> readText( colorsUrl, UTF_8 ),
              ( ) -> readText( lineAreaStylesUrl, UTF_8 ),
              symbolId -> readText( cgmUrlForSymbolId.apply( symbolId ), UTF_8 ),
              symbolId -> readText( svgUrlForSymbolId.apply( symbolId ), UTF_8 ) );
    }

    public DncGeosymTheme( TextLoader colorsLoader,
                           TextLoader lineAreaStylesLoader,
                           KeyedTextLoader cgmLoader,
                           KeyedTextLoader svgLoader )
    {
        this.colorsLoader = colorsLoader;
        this.lineAreaStylesLoader = lineAreaStylesLoader;
        this.cgmLoader = cgmLoader;
        this.svgLoader = svgLoader;
    }

    @Override
    public int hashCode( )
    {
        final int prime = 42643;
        int result = 1;
        result = prime * result + Objects.hashCode( this.colorsLoader );
        result = prime * result + Objects.hashCode( this.lineAreaStylesLoader );
        result = prime * result + Objects.hashCode( this.cgmLoader );
        result = prime * result + Objects.hashCode( this.svgLoader );
        return result;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == this ) return true;
        if ( o == null ) return false;
        if ( o.getClass( ) != getClass( ) ) return false;

        DncGeosymTheme other = ( DncGeosymTheme ) o;
        return ( equal( other.colorsLoader, colorsLoader )
              && equal( other.lineAreaStylesLoader, lineAreaStylesLoader )
              && equal( other.cgmLoader, cgmLoader )
              && equal( other.svgLoader, svgLoader ) );
    }

}
