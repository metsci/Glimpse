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
package com.metsci.glimpse.util.math.stochastic.pdfcont;

import static com.metsci.glimpse.util.GeneralUtils.doublesEqual;

import com.metsci.glimpse.util.math.stochastic.Generator;

/**
 * @author osborn
 */
public class PdfContGaussianBoxMuller implements PdfCont
{
    private final double _mean;
    private final double _stdev;

    public PdfContGaussianBoxMuller( double mean, double stdev )
    {
        _mean = mean;
        _stdev = stdev;
    }

    @Override
    public double draw( Generator g )
    {
        double v1, v2, s;
        do
        {
            v1 = 2 * g.nextDouble( ) - 1; // between -1.0 and 1.0
            v2 = 2 * g.nextDouble( ) - 1; // between -1.0 and 1.0
            s = v1 * v1 + v2 * v2;
        }
        while ( s >= 1 );

        double norm = Math.sqrt( -2 * Math.log( s ) / s );
        return _mean + _stdev * v1 * norm;
    }

    @Override
    public int hashCode( )
    {
        final int prime = 20509;
        int result = 1;
        result = prime * result + Double.hashCode( _mean );
        result = prime * result + Double.hashCode( _stdev );
        return result;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( o == this ) return true;
        if ( o == null ) return false;
        if ( o.getClass( ) != this.getClass( ) ) return false;

        PdfContGaussianBoxMuller other = ( PdfContGaussianBoxMuller ) o;
        return ( doublesEqual( other._mean, _mean )
              && doublesEqual( other._stdev, _stdev ) );
    }
}
