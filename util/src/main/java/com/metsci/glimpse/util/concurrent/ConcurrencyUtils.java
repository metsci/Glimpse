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
package com.metsci.glimpse.util.concurrent;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class ConcurrencyUtils
{

    public static void requireLock( ReentrantLock lock )
    {
        if ( !lock.isHeldByCurrentThread( ) )
        {
            throw new RuntimeException( "Lock is not held by current thread: thread-name = " + Thread.currentThread( ).getName( ) );
        }
    }

    public static ThreadFactory newDaemonThreadFactory( final ThreadFactory baseThreadFactory )
    {
        return new ThreadFactory( )
        {
            public Thread newThread( Runnable r )
            {
                Thread thread = baseThreadFactory.newThread( r );
                thread.setDaemon( true );
                return thread;
            }
        };
    }

    public static ThreadFactory newDaemonThreadFactory( String nameFormat )
    {
        return new ThreadFactoryBuilder( ).setThreadFactory( Executors.defaultThreadFactory( ) )
                                          .setNameFormat( nameFormat )
                                          .setDaemon( true )
                                          .build( );
    }

    public static Thread startThread( String name, boolean daemon, Runnable runnable )
    {
        Thread thread = new Thread( runnable, name );
        thread.setDaemon( daemon );
        thread.start( );
        return thread;
    }

}
