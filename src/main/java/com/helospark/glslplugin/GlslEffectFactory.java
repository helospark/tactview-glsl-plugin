package com.helospark.glslplugin;

import java.util.List;

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
    public StandardEffectFactory glslDarkenEffect(GlslUtil glslUtil, RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new GlslDarkenImageEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), glslUtil, renderBufferProvider, vertexBufferProvider))
                .withRestoreFactory((node, loadMetadata) -> new GlslDarkenImageEffect(node, loadMetadata))
                .withName("GLSL darken")
                .withSupportedEffectId("glsldarken")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }

    @Bean
    public StandardEffectFactory glslFilmEffect(GlslUtil glslUtil, RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider, UniformUtil uniformUtil) {
        return StandardEffectFactory.builder()
                .withFactory(
                        request -> new GlslFilmEffect(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(5000)), glslUtil, renderBufferProvider, vertexBufferProvider, uniformUtil))
                .withRestoreFactory((node, loadMetadata) -> new GlslDarkenImageEffect(node, loadMetadata))
                .withName("GLSL film")
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
                .withName("GLSL lens flare")
                .withSupportedEffectId("glsllensflare")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_EFFECT)
                .build();
    }
}
