float rand(vec2 co){
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    vec2 uv = fragCoord.xy / iResolution.xy;
    vec2 uv_org = vec2(uv);

    float t = mod(iTime, 360.0);
    float t2 = floor(t*0.6);

    float x,y,yt,xt;

        yt= abs(cos(t)) * rand(vec2(t,t)) * 100.0;
        xt=sin(360.0*rand(vec2(t,t)))*0.25;
        if (xt < 0.0) {
            xt=0.125;
                }
        x=uv.x-xt*exp(-pow(uv.y*100.0-yt,2.0)/24.0);
        y=uv.y;
        
        
    uv.x=x;
    uv.y=y;

   yt=0.5*cos((yt/100.0)/100.0*360.0);
   float yr=0.1*cos((yt/100.0)/100.0*360.0);
    if (uv_org.y > yt && uv_org.y < yt+rand(vec2(t2,t))*0.25) {
        float md = mod(x*100.0,10.0);
        if (md*sin(t) > sin(yr*360.0) || rand(vec2(md,md))>0.4) {
            vec4 org_c = texture(iChannel0, uv);
            float colx = rand(vec2(t2,t2)) * 0.75;
            float coly = rand(vec2(uv.x+t,t));// * 0.5;
            float colz = rand(vec2(t2,t2));// * 0.5;
           fragColor = vec4(org_c.x+colx,org_c.y+colx,org_c.z+colx,0.0);

        }
    }
    else if (y<cos(t) && mod(x*40.0,2.0)>rand(vec2(y*t,t*t))*1.0 ||  mod(y*12.0,2.0)<rand(vec2(x,t))*1.0) {
        if (rand(vec2(x+t,y+t))>0.8) {
   fragColor = vec4(rand(vec2(x*t,y*t)),rand(vec2(x*t,y*t)),rand(vec2(x*t,y*t)),0.0);

        }
        else 
        {
   fragColor = texture(iChannel0, uv);
        }
    }
    else {
    
        uv.x = uv.x + rand(vec2(t,uv.y)) * 0.0087 * sin(y*2.0);
                                                        
                                                        
    
   fragColor = texture(iChannel0, uv);
    }

}