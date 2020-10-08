package com.helospark.glslplugin.conditional;

public enum GlslVersion {
    NULL(0, 0),
    GLSL_1_10(1, 10),
    GLSL_1_20(1, 20),
    GLSL_1_30(1, 30),
    GLSL_1_40(1, 40),
    GLSL_1_50(1, 50),
    GLSL_3_30(3, 30),
    GLSL_4_00(4, 00),
    GLSL_4_10(4, 10),
    GLSL_4_20(4, 20),
    GLSL_4_30(4, 30),
    GLSL_4_40(4, 40),
    GLSL_4_50(4, 50),
    GLSL_4_60(4, 60);

    int majorVersion;
    int minorVersion;

    GlslVersion(int majorVersion, int minorVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
    }
}
