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
package com.metsci.glimpse.docking;

import java.awt.Component;

public class DockingGroupAdapter implements DockingGroupListener
{

    @Override
    public void addedView( Tile tile, View view )
    {
    }

    @Override
    public void removedView( Tile tile, View view )
    {
    }

    @Override
    public void selectedView( Tile tile, View view )
    {
    }

    @Override
    public void addedLeaf( MultiSplitPane docker, Component leaf )
    {
    }

    @Override
    public void removedLeaf( MultiSplitPane docker, Component leaf )
    {
    }

    @Override
    public void movedDivider( MultiSplitPane docker, SplitPane splitPane )
    {
    }

    @Override
    public void maximizedLeaf( MultiSplitPane docker, Component leaf )
    {
    }

    @Override
    public void unmaximizedLeaf( MultiSplitPane docker, Component leaf )
    {
    }

    @Override
    public void restoredTree( MultiSplitPane docker )
    {
    }

    @Override
    public void addedFrame( DockingGroup group, DockingFrame frame )
    {
    }

    @Override
    public void disposingAllFrames( DockingGroup group )
    {
    }

    @Override
    public void disposingFrame( DockingGroup group, DockingFrame frame )
    {
    }

    @Override
    public void disposedFrame( DockingGroup group, DockingFrame frame )
    {
    }

    @Override
    public void closingView( DockingGroup group, View view )
    {
    }

    @Override
    public void closedView( DockingGroup group, View view )
    {
    }

}
