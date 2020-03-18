#version 130

attribute vec2 position;

uniform vec3 iResolution;

varying vec2 vCoordinate;

void main(void) {
  vCoordinate = (position * 0.5 + vec2(0.5, 0.5)) * vec2(iResolution);
  gl_Position = vec4(position, 0.0, 1.0);
}