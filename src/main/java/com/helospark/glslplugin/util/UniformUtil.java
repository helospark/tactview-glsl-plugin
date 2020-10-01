package com.helospark.glslplugin.util;

import org.lwjgl.opengl.GL31;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Color;
import com.helospark.tactview.core.timeline.effect.interpolation.pojo.Point;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ColorProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.PointProvider;

@Component
public class UniformUtil {

    public void bindBooleanProviderToUniform(int programId, BooleanProvider provider, TimelinePosition requestPosition, String name) {
        Boolean value = provider.getValueAt(requestPosition);
        int uniformLocation = GL31.glGetUniformLocation(programId, name);
        GL31.glUniform1i(uniformLocation, value.booleanValue() ? 1 : 0);
    }

    public void bindDoubleProviderToUniform(int programId, DoubleProvider provider, TimelinePosition requestPosition, String name) {
        Double value = provider.getValueAt(requestPosition);
        bindFloatToUniform(programId, value.floatValue(), name);
    }

    public void bindFloatToUniform(int programId, float value, String name) {
        int uniformLocation = GL31.glGetUniformLocation(programId, name);
        GL31.glUniform1f(uniformLocation, value);
    }

    public void bindIntegerToUniform(int programId, int value, String name) {
        int uniformLocation = GL31.glGetUniformLocation(programId, name);
        GL31.glUniform1i(uniformLocation, value);
    }

    public void bindVec2ToUniform(int programId, double x, double y, String name) {
        int uniformLocation = GL31.glGetUniformLocation(programId, name);
        GL31.glUniform2f(uniformLocation, (float) x, (float) y);
    }

    public void bindIntegerProviderToUniform(int programId, IntegerProvider provider, TimelinePosition position, String name) {
        Integer value = provider.getValueAt(position);
        bindIntegerToUniform(programId, value, name);
    }

    public void bindColorProviderToUniform(int programId, ColorProvider provider, TimelinePosition position, String name) {
        Color value = provider.getValueAt(position);
        int uniformLocation = GL31.glGetUniformLocation(programId, name);
        GL31.glUniform3f(uniformLocation, (float) value.red, (float) value.green, (float) value.blue);
    }

    public void bindPointProviderToUniform(int programId, PointProvider provider, TimelinePosition position, String name) {
        Point value = provider.getValueAt(position);
        int uniformLocation = GL31.glGetUniformLocation(programId, name);
        GL31.glUniform2f(uniformLocation, (float) value.x, (float) value.y);
    }

    public void bindIntegerPointProviderToUniform(int programId, PointProvider provider, TimelinePosition position, String name) {
        Point value = provider.getValueAt(position);
        int uniformLocation = GL31.glGetUniformLocation(programId, name);
        GL31.glUniform2i(uniformLocation, (int) value.x, (int) value.y);

    }

    public void bindColorProvider4ProviderToUniform(int programId, ColorProvider provider, TimelinePosition position, String name) {
        Color value = provider.getValueAt(position);
        int uniformLocation = GL31.glGetUniformLocation(programId, name);
        GL31.glUniform4f(uniformLocation, (float) value.red, (float) value.green, (float) value.blue, 1.0f);
    }

}
