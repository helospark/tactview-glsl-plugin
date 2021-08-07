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

// https://www.shadertoy.com/view/XtSBz1
public class GlslDrunkEffect extends AbstractRegularGlslStatelessVideoEffect {
    private ShadertoyHelpers shadertoyHelpers;

    private DoubleProvider speedProvider;
    private DoubleProvider vignetteStrengthProvider;
    private DoubleProvider wobbleMultiplierProvider;
    private DoubleProvider zoomAmountProvider;

    private UniformUtil uniformUtil;

    public GlslDrunkEffect(TimelineInterval interval, GlslUtil glslUtil, RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider,
            ShadertoyHelpers shadertoyHelpers, UniformUtil uniformUtil) {
        super(interval, renderBufferProvider, vertexBufferProvider, glslUtil);

        this.shadertoyHelpers = shadertoyHelpers;
        this.uniformUtil = uniformUtil;

        this.vertexShader = "shaders/common/shadertoy-common.vs";
        this.fragmentShader = "shadertoy:shaders/fun/drunk.fs";
    }

    public GlslDrunkEffect(JsonNode node, LoadMetadata loadMetadata) {
        super(node, loadMetadata);
    }

    public GlslDrunkEffect(StatelessVideoEffect effect, CloneRequestMetadata cloneRequestMetadata) {
        super(effect, cloneRequestMetadata);

        ReflectionUtil.copyOrCloneFieldFromTo(effect, this, cloneRequestMetadata);
    }

    @Override
    protected void initializeValueProviderInternal() {
        speedProvider = new DoubleProvider(0.5, 1.5, new MultiKeyframeBasedDoubleInterpolator(1.0));
        vignetteStrengthProvider = new DoubleProvider(0.5, 1.5, new MultiKeyframeBasedDoubleInterpolator(1.0));
        wobbleMultiplierProvider = new DoubleProvider(0.1, 0.5, new MultiKeyframeBasedDoubleInterpolator(0.25));
        zoomAmountProvider = new DoubleProvider(0.0, 0.3, new MultiKeyframeBasedDoubleInterpolator(0.05));
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        ValueProviderDescriptor speedProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(speedProvider)
                .withName("Speed")
                .build();
        ValueProviderDescriptor vignetteStrengthProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(vignetteStrengthProvider)
                .withName("Vignette strength")
                .build();
        ValueProviderDescriptor wobbleMultiplierProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(wobbleMultiplierProvider)
                .withName("Wobble multiplier")
                .build();
        ValueProviderDescriptor zoomAmountProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(zoomAmountProvider)
                .withName("Zoom amount")
                .build();

        return List.of(speedProviderDescriptor, vignetteStrengthProviderDescriptor, wobbleMultiplierProviderDescriptor, zoomAmountProviderDescriptor);
    }

    @Override
    protected void bindUniforms(int programId, StatelessEffectRequest request) {
        shadertoyHelpers.attachCommonShadertoyUniforms(request, programId);

        uniformUtil.bindDoubleProviderToUniform(programId, speedProvider, request.getEffectPosition(), "speed");
        uniformUtil.bindDoubleProviderToUniform(programId, vignetteStrengthProvider, request.getEffectPosition(), "vignetteStrength");
        uniformUtil.bindDoubleProviderToUniform(programId, wobbleMultiplierProvider, request.getEffectPosition(), "wobbleMultiplier");
        uniformUtil.bindDoubleProviderToUniform(programId, zoomAmountProvider, request.getEffectPosition(), "zoomAmount");
    }

    @Override
    protected String getInputTextureName() {
        return "iChannel0";
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new GlslDrunkEffect(this, cloneRequestMetadata);
    }

}
