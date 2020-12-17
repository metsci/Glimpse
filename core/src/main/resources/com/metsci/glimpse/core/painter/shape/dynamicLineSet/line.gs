//
// Copyright (c) 2020, Metron, Inc.
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//     * Redistributions of source code must retain the above copyright
//       notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above copyright
//       notice, this list of conditions and the following disclaimer in the
//       documentation and/or other materials provided with the distribution.
//     * Neither the name of Metron, Inc. nor the
//       names of its contributors may be used to endorse or promote products
//       derived from this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
// DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//

#version 150

layout( lines ) in;
// A single line segment takes 4 vertices, but with duplication for wrapping,
// it can take arbitrarily many ... in practice we almost never need more than
// four copies of a segment, so use max_vertices = 4*4 = 16
layout( triangle_strip, max_vertices = 16 ) out;

vec2 pxToNdc( vec2 xy_PX, vec2 viewportSize_PX )
{
    vec2 xy_FRAC = xy_PX / viewportSize_PX;
    return ( -1.0 + 2.0*xy_FRAC );
}

vec2 rectMin( vec4 rect )
{
    // Swizzle (xMin, yMin) out of (xMin, xMax, yMin, yMax)
    return rect.xz;
}

vec2 rectMax( vec4 rect )
{
    // Swizzle (xMax, yMax) out of (xMin, xMax, yMin, yMax)
    return rect.yw;
}

vec2 rectSize( vec4 rect )
{
    return ( rectMax( rect ) - rectMin( rect ) );
}

vec2 axisXyToPx( vec2 xy_AXIS, vec4 axisRect, vec2 viewportSize_PX )
{
    vec2 xy_FRAC = ( xy_AXIS - rectMin( axisRect ) ) / rectSize( axisRect );
    return ( xy_FRAC * viewportSize_PX );
}

float wrapValue( float value, float wrapMin, float wrapSpan )
{
    float wrapCount = floor( ( value - wrapMin ) / wrapSpan );
    return ( value - ( wrapCount * wrapSpan ) );
}


uniform vec2 VIEWPORT_SIZE_PX;
uniform vec4 AXIS_RECT;
uniform vec4 WRAP_RECT;
uniform float LINE_THICKNESS_PX;
uniform float FEATHER_THICKNESS_PX;

in vec4 vRgba[];

out vec2 gPosInQuad_PX;
out float gMileage_PX;
out float gQuadLength_PX;
out vec4 gRgba;

void main( )
{
    vec2 posA_PX = gl_in[ 0 ].gl_Position.xy;
    vec2 posB_PX = gl_in[ 1 ].gl_Position.xy;

    vec4 rgbaA = vRgba[ 0 ];
    vec4 rgbaB = vRgba[ 1 ];

    vec2 lineDelta_PX = posB_PX - posA_PX;
    float lineLength_PX = length( lineDelta_PX );

    if ( lineLength_PX <= 0.0 )
    {
        return;
    }

    float halfFeather_PX = 0.5 * FEATHER_THICKNESS_PX;

    vec2 parallelDir = lineDelta_PX / lineLength_PX;
    vec2 parallelOffset_PX = halfFeather_PX * parallelDir;

    vec2 normalDir = vec2( -parallelDir.y, parallelDir.x );
    float halfNormal_PX = 0.5*LINE_THICKNESS_PX + halfFeather_PX;
    vec2 normalOffset_PX = halfNormal_PX * normalDir;


    // Compute render-shift values for wrapping
    //

    vec2 wrapMin_PX = axisXyToPx( rectMin( WRAP_RECT ), AXIS_RECT, VIEWPORT_SIZE_PX );
    vec2 wrapMax_PX = axisXyToPx( rectMax( WRAP_RECT ), AXIS_RECT, VIEWPORT_SIZE_PX );
    vec2 wrapSpan_PX = wrapMax_PX - wrapMin_PX;

    vec2 a1_PX = posA_PX - parallelOffset_PX + normalOffset_PX;
    vec2 a2_PX = posA_PX - parallelOffset_PX - normalOffset_PX;
    vec2 b1_PX = posB_PX + parallelOffset_PX + normalOffset_PX;
    vec2 b2_PX = posB_PX + parallelOffset_PX - normalOffset_PX;

    float xShiftFirst_PX;
    float xShiftStep_PX;
    int xShiftCount;
    if ( isinf( wrapSpan_PX.x ) )
    {
        xShiftFirst_PX = 0.0;
        xShiftStep_PX = 0.0;
        xShiftCount = 1;
    }
    else
    {
        float xMin_PX = a1_PX.x;
        xMin_PX = min( xMin_PX, a1_PX.x );
        xMin_PX = min( xMin_PX, a2_PX.x );
        xMin_PX = min( xMin_PX, b1_PX.x );
        xMin_PX = min( xMin_PX, b2_PX.x );

        float xMax_PX = a1_PX.x;
        xMax_PX = max( xMax_PX, a1_PX.x );
        xMax_PX = max( xMax_PX, a2_PX.x );
        xMax_PX = max( xMax_PX, b1_PX.x );
        xMax_PX = max( xMax_PX, b2_PX.x );

        xShiftFirst_PX = wrapValue( xMin_PX, wrapMin_PX.x, wrapSpan_PX.x ) - xMin_PX;
        xShiftStep_PX = wrapSpan_PX.x;
        float xShiftCount0 = ceil( ( ( xMax_PX + xShiftFirst_PX ) - wrapMin_PX.x ) / xShiftStep_PX );
        xShiftCount = max( 0, int( xShiftCount0 ) );
    }

    float yShiftFirst_PX;
    float yShiftStep_PX;
    int yShiftCount;
    if ( isinf( wrapSpan_PX.y ) )
    {
        yShiftFirst_PX = 0.0;
        yShiftStep_PX = 0.0;
        yShiftCount = 1;
    }
    else
    {
        float yMin_PX = a1_PX.y;
        yMin_PX = min( yMin_PX, a1_PX.y );
        yMin_PX = min( yMin_PX, a2_PX.y );
        yMin_PX = min( yMin_PX, b1_PX.y );
        yMin_PX = min( yMin_PX, b2_PX.y );

        float yMax_PX = a1_PX.y;
        yMax_PX = max( yMax_PX, a1_PX.y );
        yMax_PX = max( yMax_PX, a2_PX.y );
        yMax_PX = max( yMax_PX, b1_PX.y );
        yMax_PX = max( yMax_PX, b2_PX.y );

        yShiftFirst_PX = wrapValue( yMin_PX, wrapMin_PX.y, wrapSpan_PX.y ) - yMin_PX;
        yShiftStep_PX = wrapSpan_PX.y;
        float yShiftCount0 = ceil( ( ( yMax_PX + yShiftFirst_PX ) - wrapMin_PX.y ) / yShiftStep_PX );
        yShiftCount = max( 0, int( yShiftCount0 ) );
    }

    vec2 shiftFirst_PX = vec2( xShiftFirst_PX, yShiftFirst_PX );
    vec2 shiftStep_PX = vec2( xShiftStep_PX, yShiftStep_PX );


    // Emit primitives for each render-shift
    //

    for ( int iShift = 0; iShift < xShiftCount; iShift++ )
    {
        for ( int jShift = 0; jShift < yShiftCount; jShift++ )
        {
            vec2 shift_PX = shiftFirst_PX - vec2( float( iShift ), float( jShift ) )*shiftStep_PX;

            // Emit triangle-strip for line interior
            //

            gl_Position.xy = pxToNdc( a1_PX + shift_PX, VIEWPORT_SIZE_PX );
            gl_Position.zw = vec2( 0.0, 1.0 );
            gPosInQuad_PX = vec2( -halfFeather_PX, halfNormal_PX );
            gMileage_PX = 0;
            gQuadLength_PX = lineLength_PX;
            gRgba = rgbaA;
            EmitVertex( );

            gl_Position.xy = pxToNdc( a2_PX + shift_PX, VIEWPORT_SIZE_PX );
            gl_Position.zw = vec2( 0.0, 1.0 );
            gPosInQuad_PX = vec2( -halfFeather_PX, -halfNormal_PX );
            gMileage_PX = 0;
            gQuadLength_PX = lineLength_PX;
            gRgba = rgbaA;
            EmitVertex( );

            gl_Position.xy = pxToNdc( b1_PX + shift_PX, VIEWPORT_SIZE_PX );
            gl_Position.zw = vec2( 0.0, 1.0 );
            gPosInQuad_PX = vec2( lineLength_PX + halfFeather_PX, halfNormal_PX );
            gMileage_PX = lineLength_PX;
            gQuadLength_PX = lineLength_PX;
            gRgba = rgbaB;
            EmitVertex( );

            gl_Position.xy = pxToNdc( b2_PX + shift_PX, VIEWPORT_SIZE_PX );
            gl_Position.zw = vec2( 0.0, 1.0 );
            gPosInQuad_PX = vec2( lineLength_PX + halfFeather_PX, -halfNormal_PX );
            gMileage_PX = lineLength_PX;
            gQuadLength_PX = lineLength_PX;
            gRgba = rgbaB;
            EmitVertex( );

            EndPrimitive( );
        }
    }

    EndPrimitive( );
}
