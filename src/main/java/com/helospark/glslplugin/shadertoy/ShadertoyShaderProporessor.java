package com.helospark.glslplugin.shadertoy;

import com.helospark.lightdi.annotation.Component;

@Component
public class ShadertoyShaderProporessor {

    public String preprocess(String content) {
        String uniforms = "#version 130\n"
                + "\n"
                + "uniform vec3 iResolution;\n" +
                "uniform float iTime;\n" +
                "uniform float iTimeDelta;\n" +
                "uniform float iFrame;\n" +
                "uniform float iChannelTime[4];\n" +
                "uniform vec4 iMouse;\n" +
                "uniform vec4 iDate;\n" +
                //                "uniform float iSampleRate;\n" +
                "uniform vec3 iChannelResolution[4];\n" +
                "uniform sampler2D iChannel0;\n" +
                "uniform sampler2D iChannel1;\n" +
                "uniform sampler2D iChannel2;\n " +
                "\n\n";

        String header = "varying vec2 vCoordinate;"
                + "\n"
                + "vec4 texture(sampler2D sampler, vec2 coordinate) {\n"
                + "  return texture2D(sampler, coordinate);\n"
                + "}\n"
                + "\n"
                + "vec4 texture(sampler2D sampler, vec2 coordinate, float bias) {\n"
                + "  return texture2D(sampler, coordinate, bias);\n"
                + "}\n"
                + "\n";

        String mainMethod = "\n\nvoid main(void)\n" +
                "{\n" +
                "    vec4 fragColor = vec4(0.0, 0.0, 0.0, 0.0);\n" +
                "    mainImage( fragColor, vCoordinate );\n" +
                "    gl_FragData[0] =  vec4(vec3(fragColor),1.0);\n" +
                "}\n";

        return uniforms + header + content + mainMethod;
    }

}
