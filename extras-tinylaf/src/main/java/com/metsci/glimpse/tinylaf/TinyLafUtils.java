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
package com.metsci.glimpse.tinylaf;

import static java.lang.Boolean.FALSE;
import static java.util.logging.Level.WARNING;

import java.awt.Color;
import java.net.URL;
import java.util.logging.Logger;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.sf.tinylaf.Theme;
import net.sf.tinylaf.TinyLookAndFeel;

public class TinyLafUtils
{
    private static final Logger logger = Logger.getLogger( TinyLafUtils.class.getName( ) );

    public static void initTinyLaf( )
    {
        initTinyLaf( TinyLafUtils.class.getClassLoader( ).getResource( "tinylaf/radiance.theme" ) );
    }

    public static void initTinyLaf( URL themeUrl )
    {
        try
        {
            Theme.loadTheme( themeUrl );
            UIManager.setLookAndFeel( new TinyLookAndFeel( ) );

            // TinyLaf uses text-area foreground color for option-pane foreground, which doesn't look right
            Color fgColor = UIManager.getColor( "Label.foreground" );
            if ( fgColor != null )
            {
                UIManager.put( "OptionPane.messageForeground", fgColor );
            }

            // TinyLaf disables the "new folder" button in some cases ... not sure why
            UIManager.put( "FileChooser.readOnly", FALSE );

            // TinyLaf progress bars look extremely dated
            UIManager.put( "ProgressBarUI", TinyProgressBarUI2.class.getName( ) );
        }
        catch ( UnsupportedLookAndFeelException e )
        {
            logger.log( WARNING, "Failed to init Tiny L&F", e );
        }
    }

}
