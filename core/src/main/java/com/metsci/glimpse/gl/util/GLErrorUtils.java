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
package com.metsci.glimpse.gl.util;

import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;
import static jogamp.opengl.glu.error.Error.gluErrorString;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

public class GLErrorUtils
{
    private GLErrorUtils( )
    {
    };

    public static boolean logGLError( Logger logger, Level level, GL gl, String prefix )
    {
        int error = gl.glGetError( );
        if ( error != GL2.GL_NO_ERROR )
        {
            StackTraceElement[] traceArray = Thread.currentThread( ).getStackTrace( );
            StringBuilder traceString = new StringBuilder( );

            if ( traceArray != null && traceArray.length > 0 )
            {
                for ( int i = 0; i < traceArray.length - 1; i++ )
                {
                    traceString.append( traceArray[i].toString( ) ).append( "\n" );
                }
                traceString.append( traceArray[traceArray.length - 1].toString( ) );
            }

            String errorString = gluErrorString( error );
            logWarning( logger, "%s (%d): %s%n%s", prefix, error, errorString, traceString.toString( ) );
            return true;
        }

        return false;
    }
}
