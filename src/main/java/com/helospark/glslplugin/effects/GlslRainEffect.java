package com.helospark.glslplugin.effects;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.glslplugin.shadertoy.ShadertoyHelpers;
import com.helospark.glslplugin.util.GlslUtil;
import com.helospark.glslplugin.util.RenderBufferProvider;
import com.helospark.glslplugin.util.VertexBufferProvider;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListProvider;
import com.helospark.tactview.core.util.ReflectionUtil;

// https://www.shadertoy.com/view/ldSBWW
// https://www.shadertoy.com/view/XdBBzh
// https://www.shadertoy.com/view/ltffzl
public class GlslRainEffect extends AbstractRegularGlslStatelessVideoEffect {
    private ShadertoyHelpers shadertoyHelpers;

    private ValueListProvider<ValueListElement> glitchTypeProvider;

    public GlslRainEffect(TimelineInterval interval, GlslUtil glslUtil, RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider,
            ShadertoyHelpers shadertoyHelpers) {
        super(interval, renderBufferProvider, vertexBufferProvider, glslUtil);

        this.shadertoyHelpers = shadertoyHelpers;

        this.vertexShader = "shaders/common/shadertoy-common.vs";
        this.fragmentShader = "shadertoy:shaders/rain/raindrops_flowing.fs";
    }

    public GlslRainEffect(JsonNode node, LoadMetadata loadMetadata, GlslUtil glslUtil, RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider,
            ShadertoyHelpers shadertoyHelpers) {
        super(node, loadMetadata, glslUtil, renderBufferProvider, vertexBufferProvider);
        this.shadertoyHelpers = shadertoyHelpers;
    }

    public GlslRainEffect(GlslRainEffect effect, CloneRequestMetadata cloneRequestMetadata) {
        super(effect, cloneRequestMetadata);

        ReflectionUtil.copyOrCloneFieldFromTo(effect, this, cloneRequestMetadata);
    }

    @Override
    protected void initializeValueProviderInternal() {
        List<ValueListElement> glitchShaders = List.of(
                new ValueListElement("shadertoy:shaders/rain/foggy_window_rain.fs", "Foggy window rain"),
                new ValueListElement("shadertoy:shaders/rain/raindrops_flowing.fs", "Raindrops flowing"),
                new ValueListElement("shadertoy:shaders/rain/raining_to_water.fs", "Raining to water"),
                new ValueListElement("shadertoy:shaders/rain/raindrops_on_window.fs", "Raindrop on window"));
        glitchTypeProvider = new ValueListProvider<>(glitchShaders, new StepStringInterpolator("shadertoy:shaders/rain/raindrops_flowing.fs"));
    }

    @Override
    protected void initRender(StatelessEffectRequest request) {
        ValueListElement value = glitchTypeProvider.getValueAt(request.getEffectPosition(), request.getEvaluationContext());

        this.fragmentShader = value.getId();
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        ValueProviderDescriptor glitchTypeProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(glitchTypeProvider)
                .withName("Type")
                .build();
        return List.of(glitchTypeProviderDescriptor);
    }

    @Override
    protected void bindUniforms(int programId, StatelessEffectRequest request) {
        shadertoyHelpers.attachCommonShadertoyUniforms(request, programId);

        ValueListElement value = glitchTypeProvider.getValueAt(request.getEffectPosition(), request.getEvaluationContext());
        if (value.getId().endsWith("raindrops_on_window.fs")) {
            shadertoyHelpers.attachTextures(programId, "shaders/glitch/texture/noise64.png");
        }
    }

    @Override
    protected String getInputTextureName() {
        return "iChannel0";
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new GlslRainEffect(this, cloneRequestMetadata);
    }

}
