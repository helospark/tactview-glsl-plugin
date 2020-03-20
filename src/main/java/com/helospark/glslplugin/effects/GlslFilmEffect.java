package com.helospark.glslplugin.effects;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.glslplugin.util.GlslUtil;
import com.helospark.glslplugin.util.RenderBufferProvider;
import com.helospark.glslplugin.util.UniformUtil;
import com.helospark.glslplugin.util.VertexBufferProvider;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.util.ReflectionUtil;

// https://github.com/mattdesl/filmic-gl
public class GlslFilmEffect extends AbstractRegularGlslStatelessVideoEffect {

    public GlslFilmEffect(TimelineInterval interval, GlslUtil glslUtil, RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider,
            UniformUtil uniformUtil) {
        super(interval, renderBufferProvider, vertexBufferProvider, glslUtil);

        this.vertexShader = "shaders/film/film.vs";
        this.fragmentShader = "shaders/film/film.fs";
    }

    public GlslFilmEffect(JsonNode node, LoadMetadata loadMetadata) {
        super(node, loadMetadata);
    }

    public GlslFilmEffect(StatelessVideoEffect effect, CloneRequestMetadata cloneRequestMetadata) {
        super(effect, cloneRequestMetadata);

        ReflectionUtil.copyOrCloneFieldFromTo(effect, this);
    }

    @Override
    protected void initializeValueProviderInternal() {
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        return List.of();
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new GlslFilmEffect(this, cloneRequestMetadata);
    }

}
