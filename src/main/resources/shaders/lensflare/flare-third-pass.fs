#version 130

uniform sampler2D tDiffuse;
uniform sampler2D tLensDirt;
uniform sampler2D tLensColor;
uniform sampler2D tLensStar;

varying vec2   vUv;

uniform float  artefactScale;
uniform float effectStrength;

void main() {

    // compute artefactColor
    vec4 artefactColor = texture2D(tLensDirt, vUv);
    // take the tLensStar fragment using the rotation matrix in tLensStarMatrix
    vec2 lensStarUv    = (vec4(vUv.x, vUv.y,0.0,1.0)).xy;
    artefactColor      += texture2D(tLensStar, lensStarUv);
    // honor artefactScale
    artefactColor      *= vec4(vec3(artefactScale), 1.0);
    // build the final fragment
    vec4 texelLensColor    = texture2D(tLensColor, vUv) * artefactColor;
    vec4 texelDiffuse  = texture2D(tDiffuse, vUv);
    gl_FragData[0]       = texelDiffuse +  texelLensColor * effectStrength;
    //gl_FragData[0] = mix(texelDiffuse, texelLensColor, mixRatio ); 
}