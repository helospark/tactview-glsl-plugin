// https://www.shadertoy.com/view/MlScz3
uniform sampler2D tex;
const mat3 yuv2rgb = mat3(1.0, 0.0, 1.28033, 1.0, -0.21482, -0.38059, 1.0, 2.12798, 0.0);
const mat3 rgb2yuv = mat3(0.2126, 0.7152, 0.0722, -0.09991, -0.33609, 0.43600, 0.615, -0.5586, -0.05639);

vec3 coord2xyz(vec2 coord, int size) {
    vec2 uv = vec2(ivec2(vec2(coord.x, iResolution.y - coord.y) / (vec2(size, size))));
    int block = int(uv.y * iResolution.x / float(size) + uv.x);
    vec2 fragCoordY = mod(coord,vec2(size, size));
    return vec3(fragCoordY, block);
}

vec2 xyz2coord(vec3 xyz, int size, bool flip) {
    int block = int(xyz.z);
    int width = int(iResolution.x) / size;
    vec2 fullC = vec2(size, size)*vec2(block % width, block / width);
    if (flip)
        return vec2(fullC.x + xyz.x, iResolution.y - fullC.y - xyz.y);
    else
        return vec2(fullC.x + xyz.x, fullC.y + xyz.y);
}

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    float terminateAt = 10000.0 * fract(sin(iTime*42.54));
    bool flip = (fract(sin(iTime * 29.977)) < 0.21);
    if (terminateAt < 10000.0-40.0*iTime) {
        fragColor = vec4(texture(tex, fragCoord.xy / iResolution.xy));
    } else {
        float firstBlockY = (4000.0 * fract(sin(iTime*77.54))), 
            lastBlockY = (4000.0 * fract(sin(iTime*26.54)));
        float firstBlockU = (1000.0 * fract(sin(iTime*44.54))), 
            lastBlockU = (1000.0 * fract(sin(iTime*41.54)));
        float firstBlockV = (1000.0 * fract(sin(iTime*47.54))), 
            lastBlockV = (1000.0 * fract(sin(iTime*14.54)));

        vec3 uv_y = coord2xyz(fragCoord, 8);
        vec3 uv_u = coord2xyz(fragCoord, 16);
        vec3 uv_v = coord2xyz(fragCoord, 16);
        for (int n = 0; n < int(iTime/10.0); n++) {
            float deletionS = 4000.0 * fract(sin(iTime*(22.54+float(n))));
            float deletionL = 30.0 * fract(sin(iTime*(28.56+float(n))));
            if (uv_y.z > deletionS) {
                uv_y.z += deletionL;
            }
        }
        for (int n = 0; n < int(iTime/10.0); n++) {
            float deletionS = 4000.0 * fract(sin(iTime*(12.54+float(n))));
            float deletionL = 30.0 * fract(sin(iTime*(24.56+float(n))));
            if (uv_u.z > deletionS) {
                uv_u.z += deletionL;
            }
        }
        for (int n = 0; n < int(iTime/10.0); n++) {
            float deletionS = 4000.0 * fract(sin(iTime*(23.54+float(n))));
            float deletionL = 30.0 * fract(sin(iTime*(20.56+float(n))));
            if (uv_v.z > deletionS) {
                uv_v.z += deletionL;
            }
        }
        vec2 uvY = xyz2coord(uv_y, 8, flip) / iResolution.xy;
        vec2 uvU = xyz2coord(uv_u, 16, flip) / iResolution.xy;
        vec2 uvV = xyz2coord(uv_v, 16, flip) / iResolution.xy;
        vec3 col = rgb2yuv * vec3(texture(tex, uvY).x, texture(tex, uvU).y, texture(tex, uvV).z);
        if (uv_y.z >= firstBlockY && uv_y.z <= lastBlockY) col.r = 0.0;
        if (uv_u.z >= firstBlockU && uv_u.z <= lastBlockU) col.g = 0.0;
        if (uv_v.z >= firstBlockV && uv_v.z <= lastBlockV) col.b = 0.0;
        if (uv_y.z > terminateAt) col = vec3(0,0,0);
        fragColor = vec4(yuv2rgb * col, 1);
    }
}