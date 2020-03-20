package com.helospark.glslplugin.transition;

import com.helospark.lightdi.annotation.Component;

@Component
public class GlTransitionsPreprocessor {

    public String preprocess(String content) {
        String uniforms = ""
                + "#version 130\n"
                + "\n"
                + "uniform sampler2D fromImage;\n" +
                "uniform sampler2D toImage;\n" +
                "uniform float progress;\n"
                + "\n"
                + "varying vec2 vCoordinate;" +
                "\n\n";

        String header = ""
                + "vec4 getFromColor(vec2 coordinate) {\n"
                + "  return texture2D(fromImage, coordinate);\n"
                + "}\n"
                + "\n"
                + "vec4 getToColor(vec2 coordinate) {\n"
                + "  return texture2D(toImage, coordinate);\n"
                + "}\n"
                + "\n";

        String mainMethod = "\n\nvoid main(void)\n" +
                "{\n" +
                "    gl_FragData[0] = transition( vCoordinate );\n" +
                "}\n";

        return uniforms + header + content + mainMethod;

    }
}
