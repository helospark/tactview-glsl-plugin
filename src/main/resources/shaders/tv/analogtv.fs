float rand(vec2 co) { 
    return fract(sin(dot(co.xy , vec2(12.9898, 78.233))) * 43758.5453);
} 

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{          
    vec2 uv = (fragCoord.xy - iResolution.xy*0.5)/iResolution.y;      
        
    // Make better TV size
    uv *= .32;
    uv.x *= .56;
    
    // Fish eye
    float fovTheta = 7.55;    
    float z = sqrt(0.2 - uv.x * uv.x - uv.y * uv.y);
    float a = 1. / (z * tan(fovTheta * 0.5));   
    uv = uv * a;
    
    // Take video pixel
    vec3 col = texture(iChannel0, (uv + 0.5) ).rgb;
        
    // Glitch color
    vec2 ruv = uv;
    ruv.x += 0.02;
    col.r += texture(iChannel0, (ruv + 0.5) ).r * 0.4;
    
    // Color noise    
    col += rand(fract(floor((ruv + iTime)*iResolution.y)*0.7))*0.2;    
    
    // Make small lines               
    col *= clamp(fract(uv.y * 100.+iTime*8.), 0.8, 1.);       
    
    // Make big lines
    float bf = fract(uv.y * 3.+iTime*26.);
    float ff = min(bf, 1. - bf) + 0.35;
    col *= clamp(ff, 0.5, 0.75) + 0.75;       
    
    // Make low Hz
    col *= (sin(iTime*120.)*0.5 + 0.5)*0.1+0.9;
    
    // Make borders
    col *= smoothstep(-0.51, -0.50,  uv.x) * smoothstep(0.51, 0.50, uv.x);
    col *= smoothstep(-0.51, -0.50,  uv.y) * smoothstep(0.51, 0.50, uv.y);       
    
    fragColor = vec4(col, 1.);
}