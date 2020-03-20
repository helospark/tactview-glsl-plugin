#version 130

uniform sampler2D fromFrame;
uniform sampler2D toFrame;
uniform float progress;

varying vec2 vCoordinate;

void main() {
    vec4 fromColor = texture2D(fromFrame, vCoordinate);
    vec4 toColor = texture2D(toFrame, vCoordinate);

    vec4 finalColor = mix(fromColor, toColor, progress);
    
    gl_FragData[0] = finalColor;
}