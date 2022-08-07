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
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.util.ReflectionUtil;

// https://www.shadertoy.com/view/Xsl3zf
public class GlslNightVisionEffect extends AbstractRegularGlslStatelessVideoEffect {
    private ShadertoyHelpers shadertoyHelpers;

    private DoubleProvider horizontalVignetteProvider;
    private DoubleProvider verticalVignetteProvider;
    private DoubleProvider noiseStrengthProvider;
    private DoubleProvider lightMultiplierProvider;

    private UniformUtil uniformUtil;

    public GlslNightVisionEffect(TimelineInterval interval, GlslUtil glslUtil, RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider,
            ShadertoyHelpers shadertoyHelpers, UniformUtil uniformUtil) {
        super(interval, renderBufferProvider, vertexBufferProvider, glslUtil);

        this.shadertoyHelpers = shadertoyHelpers;
        this.uniformUtil = uniformUtil;

        this.vertexShader = "shaders/common/shadertoy-common.vs";
        this.fragmentShader = "shadertoy:shaders/fun/nightvision.fs";
    }

    public GlslNightVisionEffect(JsonNode node, LoadMetadata loadMetadata, GlslUtil glslUtil, RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider,
            ShadertoyHelpers shadertoyHelpers, UniformUtil uniformUtil) {
        super(node, loadMetadata, glslUtil, renderBufferProvider, vertexBufferProvider);
        this.shadertoyHelpers = shadertoyHelpers;
        this.uniformUtil = uniformUtil;
    }

    public GlslNightVisionEffect(GlslNightVisionEffect effect, CloneRequestMetadata cloneRequestMetadata) {
        super(effect, cloneRequestMetadata);

        ReflectionUtil.copyOrCloneFieldFromTo(effect, this, cloneRequestMetadata);
    }

    @Override
    protected void initializeValueProviderInternal() {
        this.horizontalVignetteProvider = new DoubleProvider(0, 0.15, new MultiKeyframeBasedDoubleInterpolator(0.045));
        this.verticalVignetteProvider = new DoubleProvider(0, 0.4, new MultiKeyframeBasedDoubleInterpolator(0.15));
        this.noiseStrengthProvider = new DoubleProvider(0, 1.0, new MultiKeyframeBasedDoubleInterpolator(0.5));
        this.lightMultiplierProvider = new DoubleProvider(1, 2.5, new MultiKeyframeBasedDoubleInterpolator(1.8));
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        ValueProviderDescriptor horizontalVignetteProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(horizontalVignetteProvider)
                .withName("Horizontal vignette")
                .build();
        ValueProviderDescriptor verticalVignetteProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(verticalVignetteProvider)
                .withName("Vertical vignette")
                .build();
        ValueProviderDescriptor noiseStrengthProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(noiseStrengthProvider)
                .withName("Noise")
                .build();
        ValueProviderDescriptor lightMultiplierProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(lightMultiplierProvider)
                .withName("Light")
                .build();
        return List.of(horizontalVignetteProviderDescriptor, verticalVignetteProviderDescriptor, noiseStrengthProviderDescriptor, lightMultiplierProviderDescriptor);
    }

    @Override
    protected void bindUniforms(int programId, StatelessEffectRequest request) {
        shadertoyHelpers.attachCommonShadertoyUniforms(request, programId);

        uniformUtil.bindDoubleProviderToUniform(programId, horizontalVignetteProvider, request.getEffectPosition(), "horizontalVignette", request.getEvaluationContext());
        uniformUtil.bindDoubleProviderToUniform(programId, verticalVignetteProvider, request.getEffectPosition(), "verticalVignette", request.getEvaluationContext());
        uniformUtil.bindDoubleProviderToUniform(programId, noiseStrengthProvider, request.getEffectPosition(), "noiseStrength", request.getEvaluationContext());
        uniformUtil.bindDoubleProviderToUniform(programId, lightMultiplierProvider, request.getEffectPosition(), "lightMultiplier", request.getEvaluationContext());
    }

    @Override
    protected String getInputTextureName() {
        return "iChannel0";
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new GlslNightVisionEffect(this, cloneRequestMetadata);
    }

}
