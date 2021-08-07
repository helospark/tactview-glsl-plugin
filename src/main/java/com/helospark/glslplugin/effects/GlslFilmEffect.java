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
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.MultiKeyframeBasedDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.bezier.BezierDoubleInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.BooleanProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.util.ReflectionUtil;

// https://github.com/mattdesl/filmic-gl
public class GlslFilmEffect extends AbstractRegularGlslStatelessVideoEffect {
    private UniformUtil uniformUtil;

    private DoubleProvider grainamount;
    private BooleanProvider colored;
    private DoubleProvider coloramount;
    private DoubleProvider grainsize;
    private DoubleProvider lumamount;
    private DoubleProvider k;
    private DoubleProvider kcube;
    private DoubleProvider scale;
    private DoubleProvider dispersion;
    private DoubleProvider blurAmount;
    private BooleanProvider blurEnabled;
    private DoubleProvider scratches;
    private DoubleProvider burn;
    private DoubleProvider vignette_size;
    private DoubleProvider tolerance;

    public GlslFilmEffect(TimelineInterval interval, GlslUtil glslUtil, RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider,
            UniformUtil uniformUtil) {
        super(interval, renderBufferProvider, vertexBufferProvider, glslUtil);

        this.vertexShader = "shaders/film/film.vs";
        this.fragmentShader = "shaders/film/film.fs";

        this.uniformUtil = uniformUtil;
    }

    public GlslFilmEffect(JsonNode node, LoadMetadata loadMetadata) {
        super(node, loadMetadata);
    }

    public GlslFilmEffect(StatelessVideoEffect effect, CloneRequestMetadata cloneRequestMetadata) {
        super(effect, cloneRequestMetadata);

        ReflectionUtil.copyOrCloneFieldFromTo(effect, this, cloneRequestMetadata);
    }

    @Override
    protected void initializeValueProviderInternal() {
        this.grainamount = new DoubleProvider(0, 1.0, new BezierDoubleInterpolator(0.10));
        this.colored = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(0.0));
        this.coloramount = new DoubleProvider(0, 2.0, new BezierDoubleInterpolator(0.4));
        this.grainsize = new DoubleProvider(0.01, 10.0, new BezierDoubleInterpolator(0.4));
        this.lumamount = new DoubleProvider(0, 1.0, new BezierDoubleInterpolator(0.1));
        this.k = new DoubleProvider(0, 1.0, new BezierDoubleInterpolator(0.0));
        this.kcube = new DoubleProvider(0, 1.0, new BezierDoubleInterpolator(0.0));
        this.scale = new DoubleProvider(0, 1.0, new BezierDoubleInterpolator(1.0));
        this.dispersion = new DoubleProvider(0, 0.1, new BezierDoubleInterpolator(0.0));
        this.blurAmount = new DoubleProvider(0, 10.0, new BezierDoubleInterpolator(1.0));
        this.blurEnabled = new BooleanProvider(new MultiKeyframeBasedDoubleInterpolator(1.0));
        this.scratches = new DoubleProvider(0, 20.0, new BezierDoubleInterpolator(1.0));
        this.burn = new DoubleProvider(0, 10.0, new BezierDoubleInterpolator(1.0));
        this.vignette_size = new DoubleProvider(0, 2.0, new BezierDoubleInterpolator(1.1));
        this.tolerance = new DoubleProvider(0, 2.0, new BezierDoubleInterpolator(0.7));
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        ValueProviderDescriptor grainamountDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(grainamount)
                .withName("grainamount")
                .build();
        ValueProviderDescriptor coloredDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(colored)
                .withName("colored")
                .build();
        ValueProviderDescriptor coloramountDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(coloramount)
                .withName("coloramount")
                .withEnabledIf(p -> colored.getValueAt(p))
                .build();
        ValueProviderDescriptor grainsizeDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(grainsize)
                .withName("grainsize")
                .build();
        ValueProviderDescriptor lumamountDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(lumamount)
                .withName("lumamount")
                .build();
        ValueProviderDescriptor kDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(k)
                .withName("k")
                .build();
        ValueProviderDescriptor kcubeDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(kcube)
                .withName("kcube")
                .build();
        ValueProviderDescriptor scaleDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(scale)
                .withName("scale")
                .build();
        ValueProviderDescriptor blurAmountDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(blurAmount)
                .withName("blurAmount")
                .withEnabledIf(p -> blurEnabled.getValueAt(p))
                .build();
        ValueProviderDescriptor dispersionDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(dispersion)
                .withName("dispersion")
                .build();
        ValueProviderDescriptor blurEnabledDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(blurEnabled)
                .withName("blurEnabled")
                .build();
        ValueProviderDescriptor scratchesDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(scratches)
                .withName("scratches")
                .build();
        ValueProviderDescriptor burnDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(burn)
                .withName("burn")
                .build();
        ValueProviderDescriptor vignettesizeDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(vignette_size)
                .withName("vignette_size")
                .build();
        ValueProviderDescriptor toleranceDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(tolerance)
                .withName("tolerance")
                .build();
        return List.of(grainamountDescriptor, coloredDescriptor, coloramountDescriptor, grainsizeDescriptor, lumamountDescriptor, kDescriptor, kcubeDescriptor,
                scaleDescriptor, dispersionDescriptor, blurEnabledDescriptor, blurAmountDescriptor, scratchesDescriptor, burnDescriptor, vignettesizeDescriptor, toleranceDescriptor);
    }

    @Override
    protected void bindUniforms(int programId, StatelessEffectRequest request) {
        super.bindUniforms(programId, request);
        TimelinePosition requestPosition = request.getEffectPosition();
        uniformUtil.bindFloatToUniform(programId, request.getEffectPosition().getSeconds().floatValue(), "timer");
        uniformUtil.bindVec2ToUniform(programId, request.getCurrentFrame().getWidth() / request.getScale(), request.getCurrentFrame().getHeight() / request.getScale(), "resolution");

        uniformUtil.bindDoubleProviderToUniform(programId, grainamount, requestPosition, "grainamount");
        uniformUtil.bindBooleanProviderToUniform(programId, colored, requestPosition, "colored");
        uniformUtil.bindDoubleProviderToUniform(programId, coloramount, requestPosition, "coloramount");
        uniformUtil.bindDoubleProviderToUniform(programId, grainsize, requestPosition, "grainsize");
        uniformUtil.bindDoubleProviderToUniform(programId, lumamount, requestPosition, "lumamount");
        uniformUtil.bindDoubleProviderToUniform(programId, k, requestPosition, "k");
        uniformUtil.bindDoubleProviderToUniform(programId, kcube, requestPosition, "kcube");
        uniformUtil.bindDoubleProviderToUniform(programId, scale, requestPosition, "scale");
        uniformUtil.bindDoubleProviderToUniform(programId, dispersion, requestPosition, "dispersion");
        uniformUtil.bindDoubleProviderToUniform(programId, blurAmount, requestPosition, "blurAmount");
        uniformUtil.bindBooleanProviderToUniform(programId, blurEnabled, requestPosition, "blurEnabled");
        uniformUtil.bindDoubleProviderToUniform(programId, scratches, requestPosition, "scratches");
        uniformUtil.bindDoubleProviderToUniform(programId, burn, requestPosition, "burn");
        uniformUtil.bindDoubleProviderToUniform(programId, vignette_size, requestPosition, "vignette_size");
        uniformUtil.bindDoubleProviderToUniform(programId, tolerance, requestPosition, "tolerance");
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new GlslFilmEffect(this, cloneRequestMetadata);
    }

}
