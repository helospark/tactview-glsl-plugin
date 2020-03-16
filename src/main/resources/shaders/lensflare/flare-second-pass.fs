#define MAX_GHOSTS 4

uniform sampler2D tDiffuse;
uniform sampler2D tLensColor;

varying vec2   vUv;

uniform vec2   textureSize;
uniform float  uGhostDispersal;

uniform float  uHaloWidth;
uniform float  uDistortion;

const float distanceFalloff = 1.5;

/*----------------------------------------------------------------------------*/
vec4 textureDistorted(
   in sampler2D    texture,
   in vec2     uv,
   in vec2     direction,    // direction of distortion
   in vec3     distortion    // per-channel distortion factor
) {
   return vec4(
       texture2D(texture, uv + direction * distortion.r).r,
       texture2D(texture, uv + direction * distortion.g).g,
       texture2D(texture, uv + direction * distortion.b).b,
       1.0
   );
}

void main() {

    vec2 texcoord = -vUv + vec2(1.0);
    
    // ghost vector to image centre
    vec2 ghostVec = (vec2(0.5) - texcoord) * uGhostDispersal;

    gl_FragData[0] = vec4(0.0);

    ///////////////////////////////////////////////////
    //  sample ghosts:
    for(int i = 0; i < MAX_GHOSTS; ++i){
        // offset of the ghosts
       vec2 offset = fract(texcoord + ghostVec * float(i));

        // linear falloff at the center
       float weight    = length(vec2(0.5) - offset) / length(vec2(0.5));
       weight      = pow(1.0 - weight,  distanceFalloff);
        // sample tDiffuse with offset/weight
           gl_FragData[0]    += texture2D(tDiffuse, offset)* weight;
    }

    // honor tLensColor
    float distance2Center  = length(vec2(0.5) - vUv) / length(vec2(0.5));
    vec2 uvLensColor   = vec2(distance2Center, 1.0);
    gl_FragData[0]       *= texture2D(tLensColor, uvLensColor);
    
    ///////////////////////////////////////////////////
    // sample halo:

    vec2 texelSize = 1.0 / textureSize;
    vec2 haloVec   = normalize(ghostVec) * uHaloWidth;
    vec3 distortion= vec3(-texelSize.x * uDistortion, 0.0, texelSize.x * uDistortion);


    float weight   = length(vec2(0.5) - fract(texcoord + haloVec)) / length(vec2(0.5));
    weight     = pow(1.0 - weight, 5.0);
    
    //gl_FragData[0] = texture2D(tDiffuse, vUv);
    
    
    gl_FragData[1]   += textureDistorted(
       tDiffuse,
       fract(texcoord + haloVec),
       normalize(ghostVec),
       distortion
    ) * weight;
}