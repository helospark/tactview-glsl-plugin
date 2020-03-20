#define scale_uv(uv, scale, center) ((uv - center) * scale + center)

uniform float speed = 1.0;
uniform float vignetteStrength = 1.0;
uniform float wobbleMultiplier = 0.25;
uniform float zoomAmount = 0.05;

void mainImage(out vec4 color, vec2 coord) {
    vec2 uv = coord / iResolution.xy;
    
    float t = iTime * speed;
    vec2 center = vec2(
        sin(t * 1.25 + 75.0 + uv.y * 0.5) + sin(t * 1.75 - 18.0 - uv.x * 0.25),
        sin(t * 1.75 - 125.0 + uv.x * 0.25) + sin(t * 1.25 + 4.0 - uv.y * 0.5)
    ) * wobbleMultiplier + 0.5;
    
    float z = sin((t + 234.5) * 3.0) * zoomAmount + 0.75;
    
    vec2 uv2 = scale_uv(uv, z, center);
    
    color = texture(iChannel0, uv2);
    
    float vignette = 1.0 - distance(uv, vec2(0.5)) * vignetteStrength;
    color = mix(color, color * vignette, sin((t + 80.023) * 2.0) * 0.75);
}