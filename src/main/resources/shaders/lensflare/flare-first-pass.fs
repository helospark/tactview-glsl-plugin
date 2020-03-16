#version 130

uniform sampler2D tDiffuse;

varying vec2 vUv;

uniform float uScale;
uniform float uBias;

void main() {
    gl_FragData[0] = max(vec4(0.0), texture2D(tDiffuse, vUv) - vec4(uBias)) * vec4(uScale);
}