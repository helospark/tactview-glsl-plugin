#version 130

attribute vec2 position;

varying vec2 vUv;

uniform float scale;

void main() {
    vUv = (position * 0.5 + vec2(0.5, 0.5)) / scale;

    gl_Position = vec4( position, 0.0, 1.0 );
}