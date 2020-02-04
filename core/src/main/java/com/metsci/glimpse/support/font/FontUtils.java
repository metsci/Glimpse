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
package com.metsci.glimpse.support.font;

import static java.awt.Font.createFont;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Utilities for loading default Glimpse fonts. Most applications should use the
 * {@code getDefaultXXXX( )} methods in order to maintain a consistent look and feel.
 * For small font sizes (exactly 8 point), {@code getSilkscreenXXXX( )} is also a
 * good option.<p>
 *
 * The default Glimpse font is Verdana, a highly hinted font which is highly readable
 * even at small sizes. However, Verdana cannot be distributed with Glimpse due to
 * licensing restrictions. If Verdana cannot be found on a system, then an error message
 * is printed and Bitstream Vera Sans is used.
 *
 * @author ulman
 */
public class FontUtils
{
    private static final Logger logger = Logger.getLogger( FontUtils.class.getName( ) );

    private static final boolean foundVerdana;

    /**
     * Map key is {@code url.toString()}, not just {@code url}, because {@code url.equals()}
     * would block on hostname resolution.
     */
    private static final Map<String, Font> loadedFonts = new HashMap<>( );

    static
    {
        boolean foundVerdanaTemp = false;

        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment( ).getAvailableFontFamilyNames( );
        if ( fonts != null )
        {
            for ( String font : fonts )
            {
                if ( font != null && font.equals( "Verdana" ) )
                {
                    foundVerdanaTemp = true;
                    break;
                }
            }
        }

        if ( !foundVerdanaTemp )
        {
            logger.info( "Verdana font is not installed. Falling back to Bitstream Vera Sans. Verdana looks *much* nicer but cannot be packaged with Glimpse due to licensing restrictions. For information on installing Verdana see: https://github.com/metsci/glimpse/wiki/Verdana-Font" );
        }

        foundVerdana = foundVerdanaTemp;
    }

    public static Font getDefaultPlain( float size )
    {
        return foundVerdana ? getVerdanaPlain( size ) : getBitstreamVeraSansPlain( size );
    }

    public static Font getDefaultBold( float size )
    {
        return foundVerdana ? getVerdanaBold( size ) : getBitstreamVeraSansBold( size );
    }

    public static Font getDefaultItalic( float size )
    {
        return foundVerdana ? getVerdanaItalic( size ) : getBitstreamVeraSansItalic( size );
    }

    public static Font getDefaultBoldItalic( float size )
    {
        return foundVerdana ? getVerdanaBoldItalic( size ) : getBitstreamVeraSansBoldItalic( size );
    }

    public static Font getBitstreamVeraSansPlain( float size )
    {
        return requireBuiltinTtf( "/com/metsci/glimpse/core/fonts/bitstream/Vera.ttf", size, Font.PLAIN );
    }

    public static Font getBitstreamVeraSansBold( float size )
    {
        return requireBuiltinTtf( "/com/metsci/glimpse/core/fonts/bitstream/VeraBd.ttf", size, Font.BOLD );
    }

    public static Font getBitstreamVeraSansItalic( float size )
    {
        return requireBuiltinTtf( "/com/metsci/glimpse/core/fonts/bitstream/VeraIt.ttf", size, Font.ITALIC );
    }

    public static Font getBitstreamVeraSansBoldItalic( float size )
    {
        return requireBuiltinTtf( "/com/metsci/glimpse/core/fonts/bitstream/Veralt.ttf", size, Font.ITALIC | Font.BOLD );
    }

    /**
     * Pixel-level designed font value at 8pt.  Creates 4/5-pixel wide characters with 2 pixel spacing.
     * Don't use for any size besides 8pt.
     */
    public static Font getSilkscreen( )
    {
        return getSilkscreenPlain( );
    }

    public static Font getSilkscreenPlain( )
    {
        return requireBuiltinTtf( "/com/metsci/glimpse/core/fonts/silkscreen/slkscr.ttf", 8, Font.PLAIN );
    }

    public static Font getSilkscreenBold( )
    {
        return requireBuiltinTtf( "/com/metsci/glimpse/core/fonts/silkscreen/slkscrb.ttf", 8, Font.BOLD );
    }

    public static Font getSilkscreenItalic( )
    {
        return requireBuiltinTtf( "/com/metsci/glimpse/core/fonts/silkscreen/slkscre.ttf", 8, Font.ITALIC );
    }

    public static Font getSilkscreenBoldItalic( )
    {
        return requireBuiltinTtf( "/com/metsci/glimpse/core/fonts/silkscreen/slkscreb.ttf", 8, Font.ITALIC | Font.BOLD );
    }

    private static Font requireBuiltinTtf( String location, float size, int style )
    {
        URL url = FontUtils.class.getResource( location );
        return loadTrueTypeFont( url, size, style );
    }

    public static Font getVerdanaPlain( float size )
    {
        return Font.decode( "Verdana" ).deriveFont( size ).deriveFont( Font.PLAIN );
    }

    public static Font getVerdanaBold( float size )
    {
        return Font.decode( "Verdana" ).deriveFont( size ).deriveFont( Font.BOLD );
    }

    public static Font getVerdanaItalic( float size )
    {
        return Font.decode( "Verdana" ).deriveFont( size ).deriveFont( Font.ITALIC );
    }

    public static Font getVerdanaBoldItalic( float size )
    {
        return Font.decode( "Verdana" ).deriveFont( size ).deriveFont( Font.ITALIC | Font.BOLD );
    }

    public static Font loadTrueTypeFont( URL url, float size, int style )
    {
        synchronized ( loadedFonts )
        {
            // URLs make poor keys, because URL.equals() blocks on hostname resolution
            String key = url.toString( );
            Font font = loadedFonts.get( key );
            if ( font == null )
            {
                font = requireFont( url );
                loadedFonts.put( key, font );
            }

            return font.deriveFont( style, size );
        }
    }

    private static Font requireFont( URL url )
    {
        try ( InputStream stream = url.openStream( ) )
        {
            return createFont( Font.TRUETYPE_FONT, stream );
        }
        catch ( IOException | FontFormatException e )
        {
            throw new RuntimeException( "Could not load font.", e );
        }
    }
}