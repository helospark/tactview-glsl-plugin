package com.helospark.glslplugin;

import java.util.List;

import org.lwjgl.opengl.GL31;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;

// https://github.com/mattdesl/filmic-gl
public class GlslFilmEffect extends AbstractRegularGlslStatelessVideoEffect {
    private DoubleProvider grainAmountProvider;
    private BooleanProvider coloredProvider;
    private DoubleProvider colorAmountProvider;
    private DoubleProvider grainSizeProvider;
    private DoubleProvider lumAmountProvider;
    private DoubleProvider kProvider;
    private DoubleProvider kCubeProvider;
    private DoubleProvider scaleProvider;
    private DoubleProvider dispertionProvider;
    private BooleanProvider blurEnabledProvider;
    private DoubleProvider scratchesProvider;
    private DoubleProvider burnProvider;

    private UniformUtil uniformUtil;

    public GlslFilmEffect(TimelineInterval interval, GlslUtil glslUtil, RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider,
            UniformUtil uniformUtil) {
        super(interval, renderBufferProvider, vertexBufferProvider, glslUtil, "shaders/film/film.vs", "shaders/film/film.fs");

        this.uniformUtil = uniformUtil;
    }

    public GlslFilmEffect(JsonNode node, LoadMetadata loadMetadata) {
        super(node, loadMetadata);
    }

    public GlslFilmEffect(StatelessVideoEffect effect, CloneRequestMetadata cloneRequestMetadata) {
        super(effect, cloneRequestMetadata);
    }

    @Override
    protected void initializeValueProviderInternal() {
        grainAmountProvider = new DoubleProvider(0.0, 0.1, new MultiKeyframeBasedDoubleInterpolator(0.03));
        coloredProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(0.0));
        colorAmountProvider = new DoubleProvider(0.0, 2.0, new MultiKeyframeBasedDoubleInterpolator(0.6));
        grainSizeProvider = new DoubleProvider(0.0, 10.0, new MultiKeyframeBasedDoubleInterpolator(1.9));
        lumAmountProvider = new DoubleProvider(0.0, 2.0, new MultiKeyframeBasedDoubleInterpolator(1.0));

        kProvider = new DoubleProvider(0.0, 1.0, new MultiKeyframeBasedDoubleInterpolator(0.05));
        kCubeProvider = new DoubleProvider(0.0, 1.0, new MultiKeyframeBasedDoubleInterpolator(0.3));
        scaleProvider = new DoubleProvider(0.0, 1.0, new MultiKeyframeBasedDoubleInterpolator(0.85));
        dispertionProvider = new DoubleProvider(0.0, 0.1, new MultiKeyframeBasedDoubleInterpolator(0.01));
        blurEnabledProvider = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(0.0));
        scratchesProvider = new DoubleProvider(0.0, 1.0, new MultiKeyframeBasedDoubleInterpolator(0.1));
        burnProvider = new DoubleProvider(0.0, 1.0, new MultiKeyframeBasedDoubleInterpolator(0.3));
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        ValueProviderDescriptor grainAmountProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(grainAmountProvider)
                .withName("Grain amount")
                .withGroup("Film grain")
                .build();
        ValueProviderDescriptor coloredProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(coloredProvider)
                .withName("Colored")
                .withGroup("Film grain")
                .build();
        ValueProviderDescriptor colorAmountProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(colorAmountProvider)
                .withName("Colored amount amount")
                .withGroup("Film grain")
                .build();
        ValueProviderDescriptor grainSizeProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(grainSizeProvider)
                .withName("Grain size")
                .withGroup("Film grain")
                .build();
        ValueProviderDescriptor lumAmountProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(lumAmountProvider)
                .withName("Lum amount")
                .withGroup("Film grain")
                .build();

        ValueProviderDescriptor kProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(kProvider)
                .withName("K")
                .withGroup("Lens distortion")
                .build();
        ValueProviderDescriptor kCubeProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(kCubeProvider)
                .withName("K cube")
                .withGroup("Lens distortion")
                .build();
        ValueProviderDescriptor scaleProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(scaleProvider)
                .withName("Scale")
                .withGroup("Lens distortion")
                .build();
        ValueProviderDescriptor dispertionProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(dispertionProvider)
                .withName("Dispersion")
                .withGroup("Lens distortion")
                .build();
        ValueProviderDescriptor blurEnabledProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(blurEnabledProvider)
                .withName("Enable blur")
                .withGroup("Lens distortion")
                .build();
        ValueProviderDescriptor scratchesProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(scratchesProvider)
                .withName("Scratches")
                .withGroup("Film damage")
                .build();
        ValueProviderDescriptor burnProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(burnProvider)
                .withName("Burns")
                .withGroup("Film damage")
                .build();
        return List.of(grainAmountProviderDescriptor, colorAmountProviderDescriptor, coloredProviderDescriptor, grainSizeProviderDescriptor, lumAmountProviderDescriptor,
                kProviderDescriptor, kCubeProviderDescriptor, scaleProviderDescriptor, dispertionProviderDescriptor, blurEnabledProviderDescriptor, scratchesProviderDescriptor,
                burnProviderDescriptor);
    }

    @Override
    protected void bindUniforms(int programId, StatelessEffectRequest request) {
        TimelinePosition requestPosition = request.getEffectPosition();
        uniformUtil.bindDoubleProviderToUniform(programId, grainAmountProvider, requestPosition, "grainamount");
        uniformUtil.bindDoubleProviderToUniform(programId, colorAmountProvider, requestPosition, "coloramount");
        uniformUtil.bindDoubleProviderToUniform(programId, grainSizeProvider, requestPosition, "grainsize");
        uniformUtil.bindDoubleProviderToUniform(programId, lumAmountProvider, requestPosition, "lumamount");
        uniformUtil.bindBooleanProviderToUniform(programId, coloredProvider, requestPosition, "coloredProvider");
        uniformUtil.bindFloatToUniform(programId, request.getEffectPosition().getSeconds().floatValue(), "timer");

        uniformUtil.bindDoubleProviderToUniform(programId, kProvider, requestPosition, "k");
        uniformUtil.bindDoubleProviderToUniform(programId, kCubeProvider, requestPosition, "kcube");
        uniformUtil.bindDoubleProviderToUniform(programId, scaleProvider, requestPosition, "scale");
        uniformUtil.bindDoubleProviderToUniform(programId, dispertionProvider, requestPosition, "dispersion");
        uniformUtil.bindBooleanProviderToUniform(programId, blurEnabledProvider, requestPosition, "blurenabled");
        uniformUtil.bindDoubleProviderToUniform(programId, scratchesProvider, requestPosition, "scratches");
        uniformUtil.bindDoubleProviderToUniform(programId, burnProvider, requestPosition, "burn");

        int uniformLocation = GL31.glGetUniformLocation(programId, "resolution");
        GL31.glUniform2f(uniformLocation, request.getCurrentFrame().getWidth(), request.getCurrentFrame().getHeight());
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return null;
    }

}
