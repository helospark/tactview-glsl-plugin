package com.helospark.glslplugin.transition;

import static com.helospark.glslplugin.conditional.GlslVersion.GLSL_1_30;

import java.util.List;

import com.helospark.glslplugin.conditional.ConditionalOnGlsl;
import com.helospark.glslplugin.util.GlslUtil;
import com.helospark.glslplugin.util.RenderBufferProvider;
import com.helospark.glslplugin.util.UniformUtil;
import com.helospark.glslplugin.util.VertexBufferProvider;
import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.tactview.core.timeline.TimelineClipType;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.effect.StandardEffectFactory;
import com.helospark.tactview.core.timeline.effect.TimelineEffectType;

@Configuration
@ConditionalOnGlsl(version = GLSL_1_30)
public class GlslTransitionsFactory {

    @Bean
    public StandardEffectFactory testGlslTransitionEffect(RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider, GlslUtil glslUtil,
            UniformUtil uniformUtil) {
        return StandardEffectFactory.builder()
                .withFactory(request -> new TestGlslTransition(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(1000)), renderBufferProvider, vertexBufferProvider, glslUtil,
                        uniformUtil))
                .withRestoreFactory((node, loadMetadata) -> new TestGlslTransition(node, loadMetadata, renderBufferProvider, vertexBufferProvider, glslUtil,
                        uniformUtil))
                .withName("GLSL transition")
                .withSupportedEffectId("glsltransition")
                .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                .withEffectType(TimelineEffectType.VIDEO_TRANSITION)
                .build();
    }

}
