package com.helospark.glslplugin.effects;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.glslplugin.shadertoy.ShadertoyHelpers;
import com.helospark.glslplugin.util.GlslUtil;
import com.helospark.glslplugin.util.RenderBufferProvider;
import com.helospark.glslplugin.util.VertexBufferProvider;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListProvider;
import com.helospark.tactview.core.util.ReflectionUtil;

// https://www.shadertoy.com/view/MtfSz2
// https://www.shadertoy.com/view/Md3SRM
// https://www.shadertoy.com/view/4l2SDh
// https://www.shadertoy.com/view/XtBXDt
// https://www.shadertoy.com/view/Mt2XDV
// https://www.shadertoy.com/view/4syfRt
// https://www.shadertoy.com/view/lsfGD2
// https://www.shadertoy.com/view/4lB3Dc
// https://www.shadertoy.com/view/Md2GDw
// https://www.shadertoy.com/view/4t23Rc
// https://www.shadertoy.com/view/XtfXR8
// https://www.shadertoy.com/view/MtXBDs
// https://www.shadertoy.com/view/ldXGW4
public class GlslGlitchImageEffect extends AbstractRegularGlslStatelessVideoEffect {
    private ShadertoyHelpers shadertoyHelpers;

    private ValueListProvider<ValueListElement> glitchTypeProvider;

    public GlslGlitchImageEffect(TimelineInterval interval, GlslUtil glslUtil, RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider,
            ShadertoyHelpers shadertoyHelpers) {
        super(interval, renderBufferProvider, vertexBufferProvider, glslUtil);

        this.shadertoyHelpers = shadertoyHelpers;

        this.vertexShader = "shaders/common/shadertoy-common.vs";
        this.fragmentShader = "shadertoy:shaders/glitch/digitalglitch.fs";
    }

    public GlslGlitchImageEffect(JsonNode node, LoadMetadata loadMetadata, GlslUtil glslUtil, RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider,
            ShadertoyHelpers shadertoyHelpers) {
        super(node, loadMetadata, glslUtil, renderBufferProvider, vertexBufferProvider);
        this.shadertoyHelpers = shadertoyHelpers;
    }

    public GlslGlitchImageEffect(GlslGlitchImageEffect effect, CloneRequestMetadata cloneRequestMetadata) {
        super(effect, cloneRequestMetadata);

        ReflectionUtil.copyOrCloneFieldFromTo(effect, this, cloneRequestMetadata);
    }

    @Override
    protected void initializeValueProviderInternal() {
        List<ValueListElement> glitchShaders = List.of(
                new ValueListElement("shadertoy:shaders/glitch/glitchpixel.fs", "Heavy TV glitch"),
                new ValueListElement("shadertoy:shaders/glitch/digitalglitch.fs", "Digital glitch"),
                new ValueListElement("shadertoy:shaders/glitch/digitalglitch_2.fs", "Digital glitch 2"),
                new ValueListElement("shadertoy:shaders/glitch/glitches.fs", "Glithces"),
                new ValueListElement("shadertoy:shaders/glitch/em_interfence.fs", "EM interference"),
                new ValueListElement("shadertoy:shaders/glitch/vhspaused.fs", "VHS paused"),
                new ValueListElement("shadertoy:shaders/glitch/mpeg_artifacts.fs", "MPEG artifact"),
                new ValueListElement("shadertoy:shaders/glitch/mpeg_glitch.fs", "MPEG glitch"),
                new ValueListElement("shadertoy:shaders/glitch/rgbshiftglitch.fs", "RGB shift glitch"),
                new ValueListElement("shadertoy:shaders/glitch/rgbshiftglitch2.fs", "RGB shift glitch 2"),
                new ValueListElement("shadertoy:shaders/glitch/interlaced_glitch.fs", "Interlaced glitch"));
        glitchTypeProvider = new ValueListProvider<>(glitchShaders, new StepStringInterpolator("shadertoy:shaders/glitch/digitalglitch.fs"));
    }

    @Override
    protected void initRender(StatelessEffectRequest request) {
        ValueListElement value = glitchTypeProvider.getValueAt(request.getEffectPosition(), request.getEvaluationContext());

        this.fragmentShader = value.getId();
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        ValueProviderDescriptor glitchTypeProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(glitchTypeProvider)
                .withName("Type")
                .build();
        return List.of(glitchTypeProviderDescriptor);
    }

    @Override
    protected void bindUniforms(int programId, StatelessEffectRequest request) {
        shadertoyHelpers.attachCommonShadertoyUniforms(request, programId);

        ValueListElement value = glitchTypeProvider.getValueAt(request.getEffectPosition(), request.getEvaluationContext());
        if (value.getId().endsWith("digitalglitch.fs")) {
            shadertoyHelpers.attachTextures(programId, "shaders/glitch/texture/noise64.png");
        } else if (value.getId().endsWith("mpeg_artifacts.fs") || value.getId().endsWith("rgbshiftglitch.fs")) {
            shadertoyHelpers.attachTextures(programId, "shaders/glitch/texture/rgbnoise64.png");
        }
    }

    @Override
    protected String getInputTextureName() {
        return "iChannel0";
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new GlslGlitchImageEffect(this, cloneRequestMetadata);
    }

}
