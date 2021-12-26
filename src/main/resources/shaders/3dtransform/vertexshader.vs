#version 110

attribute vec2 position;
varying vec2 vCoordinate;

uniform mat4 modelViewProjectionMatrix;

void main(){
  vCoordinate = (position * 0.5 + vec2(0.5, 0.5));
  gl_Position = modelViewProjectionMatrix * vec4(position, 0.0, 1);
}