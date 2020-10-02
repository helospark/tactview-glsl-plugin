package com.helospark.glslplugin.transition;

import java.util.ArrayList;
import java.util.List;

import com.helospark.glslplugin.util.GlslUtil;
import com.helospark.glslplugin.util.RenderBufferProvider;
import com.helospark.glslplugin.util.UniformUtil;
import com.helospark.glslplugin.util.VertexBufferProvider;
import com.helospark.lightdi.LightDiContext;
import com.helospark.lightdi.annotation.Bean;
import com.helospark.lightdi.annotation.Configuration;
import com.helospark.tactview.core.timeline.TimelineClipType;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelineLength;
import com.helospark.tactview.core.timeline.effect.StandardEffectFactory;
import com.helospark.tactview.core.timeline.effect.TimelineEffectType;

@Configuration
public class GlTransitionsFactory {

    private static final String ROOT_DIRECTORY = "shaders/gl-transitions";

    private LightDiContext context;

    public GlTransitionsFactory(LightDiContext context) {
        this.context = context;
    }

    @Bean
    public List<StandardEffectFactory> glTransitions(RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider, GlslUtil glslUtil,
            UniformUtil uniformUtil) {

        List<StandardEffectFactory> result = new ArrayList<>();
        List<String> fileNames;
        fileNames = ResourceList.getFileNamesInDirectory("gl-transitions/transitions");

        System.out.println("Found GL transition resources: " + fileNames);

        for (String fragmentShader : fileNames) {
            String fragmentShaderResource = "gltransitions:" + ROOT_DIRECTORY + "/" + fragmentShader;
            String name = fragmentShader.replaceAll("\\.glsl", "");
            StandardEffectFactory factory = StandardEffectFactory.builder()
                    .withFactory(
                            request -> new GlTransitionsTransition(new TimelineInterval(request.getPosition(), TimelineLength.ofMillis(1000)), renderBufferProvider, vertexBufferProvider, glslUtil,
                                    uniformUtil, fragmentShaderResource))
                    .withRestoreFactory((node, loadMetadata) -> new GlTransitionsTransition(node, loadMetadata, renderBufferProvider, vertexBufferProvider, glslUtil,
                            uniformUtil))
                    .withName(name + " - GlTransition")
                    .withSupportedEffectId(name + "gltransition")
                    .withSupportedClipTypes(List.of(TimelineClipType.VIDEO, TimelineClipType.IMAGE))
                    .withEffectType(TimelineEffectType.VIDEO_TRANSITION)
                    .build();

            factory.setContext(context); // TODO: this is needed due to a LightDi bug

            result.add(factory);
        }

        return result;
    }

}
