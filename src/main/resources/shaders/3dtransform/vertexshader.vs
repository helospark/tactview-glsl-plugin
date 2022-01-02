#version 110

attribute vec2 position;
varying vec2 vCoordinate;
varying vec3 normal;
varying vec3 vertexPosition;

uniform mat4 modelViewProjectionMatrix;
uniform mat4 modelMatrix;
uniform mat3 normalMatrix;

void main(){
  vCoordinate = (position * 0.5 + vec2(0.5, 0.5));
  normal = normalMatrix * vec3(0.0, 0.0, -1.0);
  vertexPosition = vec3(modelMatrix * vec4(position, 0.0, 1.0));
  gl_Position = modelViewProjectionMatrix * vec4(position, 0.0, 1);
}