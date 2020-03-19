float rand(float seed){
    return fract(sin(dot(vec2(seed) ,vec2(12.9898,78.233))) * 43758.5453);
}

vec2 displace(vec2 co, float seed, float seed2) {
    vec2 shift = vec2(0);
    if (rand(seed) > 0.5) {
        shift += 0.1 * vec2(2. * (0.5 - rand(seed2)));
    }
    if (rand(seed2) > 0.6) {
        if (co.y > 0.5) {
            shift.x *= rand(seed2 * seed);
        }
    }
    return shift;
}

vec4 interlace(vec2 co, vec4 col) {
    if (int(co.y) % 3 == 0) {
        return col * ((sin(iTime * 4.) * 0.1) + 0.75) + (rand(iTime) * 0.05);
    }
    return col;
}

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    // Normalized pixel coordinates (from 0 to 1)
    vec2 uv = fragCoord/iResolution.xy;

    
    vec2 rDisplace = vec2(0);
    vec2 gDisplace = vec2(0);
    vec2 bDisplace = vec2(0);
    
    if (rand(iTime) > 0.95) {
        rDisplace = displace(uv, iTime * 2., 2. + iTime);
        gDisplace = displace(uv, iTime * 3., 3. + iTime);
        bDisplace = displace(uv, iTime * 5., 5. + iTime);
    }
    
    rDisplace.x += 0.005 * (0.5 - rand(iTime * 37. * uv.y));
    gDisplace.x += 0.007 * (0.5 - rand(iTime * 41. * uv.y));
    bDisplace.x += 0.0011 * (0.5 - rand(iTime * 53. * uv.y));

    rDisplace.y += 0.001 * (0.5 - rand(iTime * 37. * uv.x));
    gDisplace.y += 0.001 * (0.5 - rand(iTime * 41. * uv.x));
    bDisplace.y += 0.001 * (0.5 - rand(iTime * 53. * uv.x));
    
    // Output to screen
    float rcolor = texture(iChannel0, uv.xy + rDisplace).r;
    float gcolor = texture(iChannel0, uv.xy + gDisplace).g;
    float bcolor = texture(iChannel0, uv.xy + bDisplace).b;

    fragColor = interlace(fragCoord, vec4(rcolor, gcolor, bcolor, 1));
}