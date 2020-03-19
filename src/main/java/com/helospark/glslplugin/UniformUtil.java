package com.helospark.glslplugin;

import org.lwjgl.opengl.GL31;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;

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

    public void bindVec2ToUniform(int programId, int x, int y, String name) {
        int uniformLocation = GL31.glGetUniformLocation(programId, name);
        GL31.glUniform2f(uniformLocation, x, y);
    }

    public void bindIntegerProviderToUniform(int programId, IntegerProvider provider, TimelinePosition position, String name) {
        Integer value = provider.getValueAt(position);
        bindIntegerToUniform(programId, value, name);
    }

}
