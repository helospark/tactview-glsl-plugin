vec3 rgb2hsv(vec3 c){
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
        vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
        vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

        float d = q.x - min(q.w, q.y);
        float e = 1.0e-10;
        return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 hsv2rgb(vec3 c){
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

float rand(float n){return fract(sin(n) * 43758.5453123);}

float rand(vec2 n) { 
    return fract(sin(dot(n, vec2(12.9898, 4.1414))) * 43758.5453);
}

// MAIN

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    vec2 uv = fragCoord.xy / iResolution.xy;
    vec4 color = texture(iChannel0, uv);
    
    // blue
    vec3 hsvColor = rgb2hsv(color.rgb);
    hsvColor.x = .59;
    hsvColor.y = clamp(hsvColor.y, .4, .6 );
    
    float noise = rand(1. + fragCoord.y + fragCoord.x * iTime);
    hsvColor.z += noise * .2;
    
    // vertical lines
    hsvColor.z += .2 * step(mod(fragCoord.x, 3.), 1.);
    
    // horizontal lines
    float offset = rand(fragCoord.y * fragCoord.x + fragCoord.x * iTime) * 10.;
    float ratio = step(rand(50. + fragCoord.y + fragCoord.x * iTime), .2);
    float timeShift = cos(iTime) * 200.;
    hsvColor.z += .4 * step(mod(fragCoord.y + offset + iTime * 400. + timeShift, 200.), 10.) * ratio;
    
    // random rectangles
    //float offset2 = rand(ceil(fragCoord.y / 100.) * floor(iTime * 20.));
    //hsvColor.z += .15 * step(mod(fragCoord.y + iTime + offset2 * 100. + 100., 200.), 100.);
    
    // horizontal rectangles
    float offset3 = rand(ceil(fragCoord.y * 0.01));
    hsvColor.z += .15 * step(mod(fragCoord.y + iTime * 15000., 2000. + 2000. * offset3), 400. + 800. * offset3);
    
    color.rgb = hsv2rgb(hsvColor);
    
    fragColor = color;
}