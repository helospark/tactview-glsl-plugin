package com.helospark.glslplugin;

import java.util.List;

import com.helospark.glslplugin.shadertoy.ShadertoyHelpers;
import com.helospark.glslplugin.texture.TextureLoader;
import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.tactview.core.timeline.TimelineClipType;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.effect.StandardEffectFactory;
import com.helospark.tactview.core.timeline.effect.TimelineEffectType;

@Configuration
public class GlslEffectFactory {

    @Bean
    public StandardEffectFactory glslFilmEffect(GlslUtil glslUtil, RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider, UniformUtil uniformUtil) {
        return StandardEffectFactory.builder()
                .withFactory(
                        request -> new GlslFilmEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), glslUtil, renderBufferProvider, vertexBufferProvider, uniformUtil))
                .withRestoreFactory((node, loadMetadata) -> new GlslDarkenImageEffect(node, loadMetadata))
                .withName("Old film")
                .withSupportedEffectId("glslfilm")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory glslLensFlareEffect(GlslUtil glslUtil, RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider, UniformUtil uniformUtil,
            TextureLoader textureLoader) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new GlslLensFlareEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), glslUtil, renderBufferProvider, vertexBufferProvider,
                        uniformUtil, textureLoader))
                .withRestoreFactory((node, loadMetadata) -> new GlslLensFlareEffect(node, loadMetadata))
                .withName("Lens flare")
                .withSupportedEffectId("glsllensflare")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory glslGlitchEffect(GlslUtil glslUtil, RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider, ShadertoyHelpers shadertoyHelpers) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new GlslGlitchImageEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), glslUtil, renderBufferProvider, vertexBufferProvider,
                        shadertoyHelpers))
                .withRestoreFactory((node, loadMetadata) -> new GlslGlitchImageEffect(node, loadMetadata))
                .withName("Glitch")
                .withSupportedEffectId("glslglitch")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory glslTvEffect(GlslUtil glslUtil, RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider, ShadertoyHelpers shadertoyHelpers) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new GlslTvEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), glslUtil, renderBufferProvider, vertexBufferProvider,
                        shadertoyHelpers))
                .withRestoreFactory((node, loadMetadata) -> new GlslTvEffect(node, loadMetadata))
                .withName("TV effects")
                .withSupportedEffectId("glsltv")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory glslFadedImageEffect(GlslUtil glslUtil, RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider, ShadertoyHelpers shadertoyHelpers) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new FadedImageEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), glslUtil, renderBufferProvider, vertexBufferProvider,
                        shadertoyHelpers))
                .withRestoreFactory((node, loadMetadata) -> new FadedImageEffect(node, loadMetadata))
                .withName("Faded image")
                .withSupportedEffectId("glslfaded")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory halftoneImageEffect(GlslUtil glslUtil, RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider, ShadertoyHelpers shadertoyHelpers) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new HalfToneImageEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), glslUtil, renderBufferProvider, vertexBufferProvider,
                        shadertoyHelpers))
                .withRestoreFactory((node, loadMetadata) -> new HalfToneImageEffect(node, loadMetadata))
                .withName("Half tone")
                .withSupportedEffectId("glslhalftone")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory glslRainEffect(GlslUtil glslUtil, RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider, ShadertoyHelpers shadertoyHelpers) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new GlslRainEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), glslUtil, renderBufferProvider, vertexBufferProvider,
                        shadertoyHelpers))
                .withRestoreFactory((node, loadMetadata) -> new GlslRainEffect(node, loadMetadata))
                .withName("Rain")
                .withSupportedEffectId("glslrain")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }
}
