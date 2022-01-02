#version 110

uniform sampler2D inputImage;

uniform bool specularEnabled;
uniform float specularShininess;
uniform float specularLightStrength;

varying vec2 vCoordinate;
varying vec3 normal;
varying vec3 vertexPosition;

void main(void)
{
    vec3 c = texture2D(inputImage, vCoordinate).xyz;
    
    if (specularEnabled) {
      vec3 normalDirection = normalize(normal);
      vec3 viewDirection = normalize(vec3(0.0, 0.0, 0.0) - vertexPosition);
      vec3 lightDirection = normalize(vec3(vec3(0.0, 0.0, 1.0) - vertexPosition));

      vec3 specularReflection = specularLightStrength * vec3(1.0, 1.0, 1.0) * pow(max(0.0, dot(reflect(-lightDirection, normalDirection), viewDirection)), specularShininess);

      c += specularReflection;
    }
    
    gl_FragData[0] = vec4(c, 1.0);
}