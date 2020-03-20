package com.helospark.glslplugin.transition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
        List<String> filePath;
        try {
            filePath = getResourceFiles(ROOT_DIRECTORY);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (String fragmentShader : filePath) {
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

    private List<String> getResourceFiles(String path) throws IOException {
        List<String> filenames = new ArrayList<>();

        try (
                InputStream in = getResourceAsStream(path);
                BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String resource;

            while ((resource = br.readLine()) != null) {
                filenames.add(resource);
            }
        }

        return filenames;
    }

    private InputStream getResourceAsStream(String resource) {
        final InputStream in = getContextClassLoader().getResourceAsStream(resource);

        return in == null ? getClass().getResourceAsStream(resource) : in;
    }

    private ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}
