#version 130

attribute vec2 position;

varying vec2 vUv;
varying vec2 vCanvasUv;

void main(void) {
  vUv = position * 0.5 + vec2(0.5, 0.5);
  vCanvasUv = position * 0.5 + vec2(0.5, 0.5);
  gl_Position = vec4(position, 0.0, 1.0);
}
