package com.helospark.glslplugin;

import java.util.List;

import org.lwjgl.opengl.GL31;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;

public class GlslDarkenImageEffect extends AbstractRegularGlslStatelessVideoEffect {
    private DoubleProvider multiplierProvider;

    public GlslDarkenImageEffect(TimelineInterval interval, GlslUtil glslUtil, RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider) {
        super(interval, renderBufferProvider, vertexBufferProvider, glslUtil);

        this.vertexShader = "sobel-edge-vs.glsl";
        this.fragmentShader = "sobel-edge-fs.glsl";
    }

    public GlslDarkenImageEffect(JsonNode node, LoadMetadata loadMetadata) {
        super(node, loadMetadata);
    }

    public GlslDarkenImageEffect(StatelessVideoEffect effect, CloneRequestMetadata cloneRequestMetadata) {
        super(effect, cloneRequestMetadata);
    }

    @Override
    protected void initializeValueProviderInternal() {
        multiplierProvider = new DoubleProvider(0.0, 2.0, new MultiKeyframeBasedDoubleInterpolator(0.5));
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        ValueProviderDescriptor multiplierDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(multiplierProvider)
                .withName("Multiplier")
                .build();
        return List.of(multiplierDescriptor);
    }

    @Override
    protected void bindUniforms(int programId, StatelessEffectRequest request) {
        Double value = multiplierProvider.getValueAt(request.getEffectPosition());

        int uniformLocation = GL31.glGetUniformLocation(programId, "multiplier");
        GL31.glUniform1f(uniformLocation, value.floatValue());
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return null;
    }

}
