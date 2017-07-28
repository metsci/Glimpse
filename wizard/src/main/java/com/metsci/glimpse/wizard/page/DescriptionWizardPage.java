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
package com.metsci.glimpse.wizard.page;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;

public abstract class DescriptionWizardPage<D> extends SimpleWizardPage<D>
{
    protected String descriptionFile;

    public DescriptionWizardPage( Object parentId, String title, String descriptionFile )
    {
        this( UUID.randomUUID( ), parentId, title, descriptionFile );
    }
    
    public DescriptionWizardPage( Object id, Object parentId, String title, String descriptionFile )
    {
        super( id, parentId, title );

        this.descriptionFile = descriptionFile;
        this.container.setLayout( new MigLayout( ) );

        JTextPane descriptionArea = new JTextPane( );
        descriptionArea.setEditable( false );
        descriptionArea.setOpaque( false );

        URL url = getDescriptionResource( descriptionFile );
        try
        {
            descriptionArea.setPage( url );
        }
        catch ( IOException e )
        {
            descriptionArea.setText( String.format( "Error: Unable to load page description from: %s", descriptionFile ) );
        }

        this.container.add( descriptionArea, "split, span, pushx, growx, wrap" );
        this.container.add( new JSeparator( SwingConstants.HORIZONTAL ), "split, span, gap 0 0 10 10, pushx, growx, wrap" );
    }
    
    protected URL getDescriptionResource( String descriptionFile )
    {
        return this.getClass( ).getClassLoader( ).getResource( descriptionFile );
    }
}