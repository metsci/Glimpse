#version 150

uniform float FEATHER_THICKNESS_PX;
uniform int STIPPLE_ENABLE;
uniform float STIPPLE_SCALE;
uniform int STIPPLE_PATTERN;

in float gMileage_PX;
in float gFeatherAlpha;
in vec4 gRgba;

out vec4 outRgba;

void main( )
{
    float stippleAlpha;
    if ( STIPPLE_ENABLE == 0 )
    {
        stippleAlpha = 1.0;
    }
    else
    {
        // This assumes that the feather region is thinner than a single stipple-
        // bit region. The alternative would be more complicated than useful.

        float feather_PX = 0.5*FEATHER_THICKNESS_PX;

        float bitWidth_PX = STIPPLE_SCALE;
        float bitNum = mod( gMileage_PX / bitWidth_PX, 16.0 );
        float bitAlpha = float( sign( STIPPLE_PATTERN & ( 0x1 << int( bitNum ) ) ) );

        float posInBit_PX = bitWidth_PX*( bitNum - floor( bitNum ) );
        if ( FEATHER_THICKNESS_PX > 0.0 && posInBit_PX < feather_PX )
        {
            float bitNumPrev = mod( bitNum - 1.0, 16.0 );
            float bitAlphaPrev = float( sign( STIPPLE_PATTERN & ( 0x1 << int( bitNumPrev ) ) ) );
            float mixFrac = ( feather_PX + posInBit_PX ) / ( 2.0 * feather_PX );
            stippleAlpha = mix( bitAlphaPrev, bitAlpha, mixFrac );
        }
        else if ( FEATHER_THICKNESS_PX > 0.0 && posInBit_PX > bitWidth_PX - feather_PX )
        {
            float bitNumNext = mod( bitNum + 1.0, 16.0 );
            float bitAlphaNext = float( sign( STIPPLE_PATTERN & ( 0x1 << int( bitNumNext ) ) ) );
            float mixFrac = ( posInBit_PX - ( bitWidth_PX - feather_PX ) ) / ( 2.0 * feather_PX );
            stippleAlpha = mix( bitAlpha, bitAlphaNext, mixFrac );
        }
        else
        {
            stippleAlpha = bitAlpha;
        }
    }

    outRgba.rgb = gRgba.rgb;

    float minAlpha = min( gFeatherAlpha, stippleAlpha );
    outRgba.a = gRgba.a * clamp( minAlpha, 0.0, 1.0 );
}
