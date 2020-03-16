#version 130

varying vec2 coord;
uniform sampler2D inputImage;
uniform float multiplier;

void main(void) {
  vec4 value = texture2D(inputImage,coord) * vec4(multiplier,multiplier,multiplier, 1.0);
  gl_FragData[0] = value;
}
