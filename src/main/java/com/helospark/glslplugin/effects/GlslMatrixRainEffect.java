package com.helospark.glslplugin.effects;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.glslplugin.shadertoy.ShadertoyHelpers;
import com.helospark.glslplugin.util.GlslUtil;
import com.helospark.glslplugin.util.RenderBufferProvider;
import com.helospark.glslplugin.util.UniformUtil;
import com.helospark.glslplugin.util.VertexBufferProvider;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.util.ReflectionUtil;

// https://www.shadertoy.com/view/lsXSDn
public class GlslMatrixRainEffect extends AbstractRegularGlslStatelessVideoEffect {
    private ShadertoyHelpers shadertoyHelpers;

    private DoubleProvider rainSpeedProvider;
    private DoubleProvider dropSizeProvider;

    private UniformUtil uniformUtil;

    public GlslMatrixRainEffect(TimelineInterval interval, GlslUtil glslUtil, RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider,
            ShadertoyHelpers shadertoyHelpers, UniformUtil uniformUtil) {
        super(interval, renderBufferProvider, vertexBufferProvider, glslUtil);

        this.shadertoyHelpers = shadertoyHelpers;
        this.uniformUtil = uniformUtil;

        this.vertexShader = "shaders/common/shadertoy-common.vs";
        this.fragmentShader = "shadertoy:shaders/fun/matrix_rain.fs";
    }

    public GlslMatrixRainEffect(JsonNode node, LoadMetadata loadMetadata) {
        super(node, loadMetadata);
    }

    public GlslMatrixRainEffect(StatelessVideoEffect effect, CloneRequestMetadata cloneRequestMetadata) {
        super(effect, cloneRequestMetadata);

        ReflectionUtil.copyOrCloneFieldFromTo(effect, this, cloneRequestMetadata);
    }

    @Override
    protected void initializeValueProviderInternal() {
        rainSpeedProvider = new DoubleProvider(0.5, 5, new MultiKeyframeBasedDoubleInterpolator(1.75));
        dropSizeProvider = new DoubleProvider(0.5, 10, new MultiKeyframeBasedDoubleInterpolator(3.0));
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        ValueProviderDescriptor rainSpeedProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(rainSpeedProvider)
                .withName("Rain speed")
                .build();
        ValueProviderDescriptor dropSizeProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(dropSizeProvider)
                .withName("Drop size")
                .build();

        return List.of(rainSpeedProviderDescriptor, dropSizeProviderDescriptor);
    }

    @Override
    protected void bindUniforms(int programId, StatelessEffectRequest request) {
        shadertoyHelpers.attachCommonShadertoyUniforms(request, programId);

        uniformUtil.bindDoubleProviderToUniform(programId, rainSpeedProvider, request.getEffectPosition(), "rainSpeed");
        uniformUtil.bindDoubleProviderToUniform(programId, dropSizeProvider, request.getEffectPosition(), "dropSize");
    }

    @Override
    protected String getInputTextureName() {
        return "iChannel0";
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new GlslMatrixRainEffect(this, cloneRequestMetadata);
    }

}
