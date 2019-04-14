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
package com.metsci.glimpse.util.primitives;

import java.nio.ByteBuffer;

/**
 * Strings are encoded and decoded using the UTF-8 charset (multi-byte
 * charsets just aren't worth the increased complication in indexing).
 *
 * @author hogye
 */
public interface BytesModifiable extends Bytes
{

    void set( int i, byte v );

    void set( int i, byte[] vs );

    void set( int i, byte[] vs, int from, int to );

    void insert( int i, byte v );

    void insert( int i, byte[] vs );

    void insert( int i, Bytes vs );

    void insert( int i, String s );

    void insert( int i, ByteBuffer vs );

    void insert( int i, byte[] vs, int from, int to );

    void insert( int i, Bytes vs, int from, int to );

    void insert( int i, String s, int from, int to );

    void insert( int i, ByteBuffer vs, int c );

    void append( byte v );

    void append( byte[] vs );

    void append( Bytes vs );

    void append( String s );

    void append( ByteBuffer vs );

    void append( byte[] vs, int from, int to );

    void append( Bytes vs, int from, int to );

    void append( String s, int from, int to );

    void append( ByteBuffer vs, int c );

    void prepend( byte v );

    void prepend( byte[] vs );

    void prepend( Bytes vs );

    void prepend( String s );

    void prepend( ByteBuffer vs );

    void prepend( byte[] vs, int from, int to );

    void prepend( Bytes vs, int from, int to );

    void prepend( String s, int from, int to );

    void prepend( ByteBuffer vs, int c );

    /**
     * Removes a single copy of the specified value. If multiple copies
     * are present, there is no guarantee which one will be removed.
     */
    void remove( byte v );

    /**
     * Removes values starting at index from (inclusive) to index to (exclusive).
     * All other values with indices greater than or equal to index to have
     * their index in the array decreased by to-from and the size of the array is
     * decreased by to-from.
     */
    void removeRange( int from, int to );

    /**
     * Remove value at index. All other values with indices greater than index have
     * their index in the array decreased by one and the size of the array is
     * decreased by one.
     */
    void removeIndex( int index );

    void clear( );

    void ensureCapacity( int minCapacity );

    void compact( );

}
