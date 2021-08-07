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
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
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

// https://www.shadertoy.com/view/Ms23DR
// https://www.shadertoy.com/view/WsSSDc
public class GlslTvEffect extends AbstractRegularGlslStatelessVideoEffect {
    private ShadertoyHelpers shadertoyHelpers;

    private ValueListProvider<ValueListElement> typeProvider;

    public GlslTvEffect(TimelineInterval interval, GlslUtil glslUtil, RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider,
            ShadertoyHelpers shadertoyHelpers) {
        super(interval, renderBufferProvider, vertexBufferProvider, glslUtil);

        this.shadertoyHelpers = shadertoyHelpers;

        this.vertexShader = "shaders/common/shadertoy-common.vs";
        this.fragmentShader = "shadertoy:shaders/glitch/software_glitch.fs";
    }

    public GlslTvEffect(JsonNode node, LoadMetadata loadMetadata) {
        super(node, loadMetadata);
    }

    public GlslTvEffect(StatelessVideoEffect effect, CloneRequestMetadata cloneRequestMetadata) {
        super(effect, cloneRequestMetadata);

        ReflectionUtil.copyOrCloneFieldFromTo(effect, this, cloneRequestMetadata);
    }

    @Override
    protected void initializeValueProviderInternal() {
        List<ValueListElement> glitchShaders = List.of(
                new ValueListElement("shadertoy:shaders/tv/software_glitch.fs", "Bad analog"),
                new ValueListElement("shadertoy:shaders/tv/oldtv.fs", "Old TV"),
                new ValueListElement("shadertoy:shaders/tv/oldtv2.fs", "Old TV 2"),
                new ValueListElement("shadertoy:shaders/tv/oldtv3.fs", "Old TV 3"),
                new ValueListElement("shadertoy:shaders/tv/oldtv4.fs", "Old TV 4"),
                new ValueListElement("shadertoy:shaders/tv/vcrtape.fs", "VCR tape"),
                new ValueListElement("shadertoy:shaders/tv/vcr_2.fs", "VCR tape 2"),
                new ValueListElement("shadertoy:shaders/tv/vhs.fs", "VHS"),
                new ValueListElement("shadertoy:shaders/glitch/vhspaused.fs", "VHS paused"),
                new ValueListElement("shadertoy:shaders/tv/mattiascrt.fs", "CRT closeup"),
                new ValueListElement("shadertoy:shaders/tv/analogtv.fs", "Analog CRT"),
                new ValueListElement("shadertoy:shaders/tv/scanline.fs", "Scanline CRT"),
                new ValueListElement("shadertoy:shaders/tv/bad_tv.fs", "Bad TV"),
                new ValueListElement("shadertoy:shaders/tv/lcd.fs", "LCD"));
        typeProvider = new ValueListProvider<>(glitchShaders, new StepStringInterpolator("shadertoy:shaders/tv/analogtv.fs"));
    }

    @Override
    protected void initRender(StatelessEffectRequest request) {
        ValueListElement value = typeProvider.getValueAt(request.getEffectPosition());

        this.fragmentShader = value.getId();
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        ValueProviderDescriptor glitchTypeProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(typeProvider)
                .withName("Type")
                .build();
        return List.of(glitchTypeProviderDescriptor);
    }

    @Override
    protected void bindUniforms(int programId, StatelessEffectRequest request) {
        shadertoyHelpers.attachCommonShadertoyUniforms(request, programId);

        ValueListElement value = typeProvider.getValueAt(request.getEffectPosition());
        if (value.getId().endsWith("vcrtape.fs")) {
            shadertoyHelpers.attachTextures(programId, "shaders/glitch/texture/rgbnoise.png");
        } else if (value.getId().endsWith("vcr_2.fs")) {
            shadertoyHelpers.attachTextures(programId, "shaders/glitch/texture/noise_256.png");
        }
    }

    @Override
    protected String getInputTextureName() {
        return "iChannel0";
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new GlslTvEffect(this, cloneRequestMetadata);
    }

}
