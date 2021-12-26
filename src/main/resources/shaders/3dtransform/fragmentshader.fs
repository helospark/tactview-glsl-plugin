#version 110

uniform sampler2D inputImage;

varying vec2 vCoordinate;

void main(void)
{
    vec3 c = texture2D(inputImage, vCoordinate).xyz;
    gl_FragData[0] = vec4(c, 1.0);
}