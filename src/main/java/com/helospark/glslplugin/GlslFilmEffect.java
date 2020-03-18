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
        super(interval, renderBufferProvider, vertexBufferProvider, glslUtil);

        this.uniformUtil = uniformUtil;

        this.vertexShader = "shaders/common/common.vs";
        this.fragmentShader = "shaders/glitch/software_glitch.fs";
    }

    public GlslFilmEffect(JsonNode node, LoadMetadata loadMetadata) {
        super(node, loadMetadata);
    }

    public GlslFilmEffect(StatelessVideoEffect effect, CloneRequestMetadata cloneRequestMetadata) {
        super(effect, cloneRequestMetadata);
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
