package com.helospark.glslplugin.conditional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ConditionalOnGlslConditionTest {

    @ParameterizedTest
    @MethodSource("factory")
    public void test(Integer actualMajor, Integer actualMinor, GlslVersion requiredVersion, boolean expected) {
        ConditionalOnGlslCondition condition = new ConditionalOnGlslCondition(Optional.ofNullable(actualMajor), Optional.ofNullable(actualMinor), Optional.ofNullable(requiredVersion));

        boolean result = condition.evaluate(null);

        assertThat(result, is(expected));
    }

    static Stream<Arguments> factory() {
        return Stream.of(
                Arguments.of(1, 10, GlslVersion.GLSL_1_10, true),
                Arguments.of(1, 30, GlslVersion.GLSL_1_30, true),
                Arguments.of(3, 30, GlslVersion.GLSL_4_10, false),
                Arguments.of(3, 30, GlslVersion.NULL, true),
                Arguments.of(null, null, GlslVersion.NULL, false),
                Arguments.of(null, null, GlslVersion.GLSL_1_20, false));
    }
}
