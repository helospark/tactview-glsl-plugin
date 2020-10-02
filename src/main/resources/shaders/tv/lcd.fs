// https://www.shadertoy.com/view/ldBfDd
void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    // Get pos relative to 0-1 screen space
    vec2 uv = fragCoord.xy / iResolution.xy;;
    
    // Map texture to 0-1 space
    vec4 texColor = texture(iChannel0,uv);
    
    // Default lcd colour (affects brightness)
    float pb = 0.4;
    vec4 lcdColor = vec4(pb,pb,pb,1.0);
    
    // Change every 1st, 2nd, and 3rd vertical strip to RGB respectively
    int px = int(mod(fragCoord.x,3.0));
    if (px == 1) lcdColor.r = 1.0;
    else if (px == 2) lcdColor.g = 1.0;
    else lcdColor.b = 1.0;
    
    // Darken every 3rd horizontal strip for scanline
    float sclV = 0.25;
    if (int(mod(fragCoord.y,3.0)) == 0) lcdColor.rgb = vec3(sclV,sclV,sclV);
    
    
    fragColor = texColor*lcdColor;
}