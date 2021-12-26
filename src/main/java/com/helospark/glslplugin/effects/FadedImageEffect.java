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
import com.helospark.tactview.core.util.ReflectionUtil;

public class FadedImageEffect extends AbstractRegularGlslStatelessVideoEffect {
    private ShadertoyHelpers shadertoyHelpers;

    public FadedImageEffect(TimelineInterval interval, GlslUtil glslUtil, RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider,
            ShadertoyHelpers shadertoyHelpers) {
        super(interval, renderBufferProvider, vertexBufferProvider, glslUtil);

        this.shadertoyHelpers = shadertoyHelpers;

        this.vertexShader = "shaders/common/shadertoy-common.vs";
        this.fragmentShader = "shadertoy:shaders/faded/earlybird.fs";
    }

    public FadedImageEffect(JsonNode node, LoadMetadata loadMetadata, GlslUtil glslUtil, RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider,
            ShadertoyHelpers shadertoyHelpers) {
        super(node, loadMetadata, glslUtil, renderBufferProvider, vertexBufferProvider);
        this.shadertoyHelpers = shadertoyHelpers;
    }

    public FadedImageEffect(FadedImageEffect effect, CloneRequestMetadata cloneRequestMetadata) {
        super(effect, cloneRequestMetadata);

        ReflectionUtil.copyOrCloneFieldFromTo(effect, this, cloneRequestMetadata);
    }

    @Override
    protected void initializeValueProviderInternal() {

    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        return List.of();
    }

    @Override
    protected void bindUniforms(int programId, StatelessEffectRequest request) {
        shadertoyHelpers.attachCommonShadertoyUniforms(request, programId);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new FadedImageEffect(this, cloneRequestMetadata);
    }

}
