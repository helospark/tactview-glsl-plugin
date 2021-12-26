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
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.util.ReflectionUtil;

// https://www.shadertoy.com/view/XssGz8
public class GlslBarrelBlurEffect extends AbstractRegularGlslStatelessVideoEffect {
    private DoubleProvider amountProvider;

    private UniformUtil uniformUtil;
    private ShadertoyHelpers shadertoyHelpers;

    public GlslBarrelBlurEffect(TimelineInterval interval, GlslUtil glslUtil, RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider,
            UniformUtil uniformUtil, ShadertoyHelpers shadertoyHelpers) {
        super(interval, renderBufferProvider, vertexBufferProvider, glslUtil);

        this.uniformUtil = uniformUtil;
        this.shadertoyHelpers = shadertoyHelpers;

        this.vertexShader = "shaders/common/shadertoy-common.vs";
        this.fragmentShader = "shadertoy:shaders/chroma/barrel_blur_chroma.fs";
    }

    public GlslBarrelBlurEffect(JsonNode node, LoadMetadata loadMetadata, GlslUtil glslUtil, RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider,
            UniformUtil uniformUtil, ShadertoyHelpers shadertoyHelpers) {
        super(node, loadMetadata, glslUtil, renderBufferProvider, vertexBufferProvider);
        this.uniformUtil = uniformUtil;
        this.shadertoyHelpers = shadertoyHelpers;
    }

    public GlslBarrelBlurEffect(GlslBarrelBlurEffect effect, CloneRequestMetadata cloneRequestMetadata) {
        super(effect, cloneRequestMetadata);

        ReflectionUtil.copyOrCloneFieldFromTo(effect, this, cloneRequestMetadata);
    }

    @Override
    protected void initializeValueProviderInternal() {
        amountProvider = new DoubleProvider(0.0, 1.5, new MultiKeyframeBasedDoubleInterpolator(0.2));
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        ValueProviderDescriptor amountProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(amountProvider)
                .withName("Amount")
                .build();

        return List.of(amountProviderDescriptor);
    }

    @Override
    protected void bindUniforms(int programId, StatelessEffectRequest request) {
        TimelinePosition requestPosition = request.getEffectPosition();
        uniformUtil.bindDoubleProviderToUniform(programId, amountProvider, requestPosition, "amount");

        shadertoyHelpers.attachCommonShadertoyUniforms(request, programId);

        shadertoyHelpers.attachTextures(programId, "shaders/glitch/texture/rgbnoise64.png");
    }

    @Override
    protected String getInputTextureName() {
        return "iChannel0";
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new GlslBarrelBlurEffect(this, cloneRequestMetadata);
    }

}
