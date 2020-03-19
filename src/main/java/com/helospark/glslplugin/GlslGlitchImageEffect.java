package com.helospark.glslplugin;

import java.math.RoundingMode;
import java.util.List;

import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.interpolator.StepStringInterpolator;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.ValueListProvider;

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
    private UniformUtil uniformUtil;
    private ProjectRepository projectRepository;
    private TextureLoader textureLoader;

    private ValueListProvider<ValueListElement> glitchTypeProvider;

    public GlslGlitchImageEffect(TimelineInterval interval, GlslUtil glslUtil, RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider, UniformUtil uniformUtil,
            ProjectRepository projectRepository, TextureLoader textureLoader) {
        super(interval, renderBufferProvider, vertexBufferProvider, glslUtil);

        this.uniformUtil = uniformUtil;
        this.projectRepository = projectRepository;
        this.textureLoader = textureLoader;

        this.vertexShader = "shaders/common/common.vs";
        this.fragmentShader = "shadertoy:shaders/glitch/software_glitch.fs";
    }

    public GlslGlitchImageEffect(JsonNode node, LoadMetadata loadMetadata) {
        super(node, loadMetadata);
    }

    public GlslGlitchImageEffect(StatelessVideoEffect effect, CloneRequestMetadata cloneRequestMetadata) {
        super(effect, cloneRequestMetadata);
    }

    @Override
    protected void initializeValueProviderInternal() {
        List<ValueListElement> glitchShaders = List.of(
                new ValueListElement("shadertoy:shaders/glitch/software_glitch.fs", "Software glitch"),
                new ValueListElement("shadertoy:shaders/glitch/glitchpixel.fs", "Heavy TV glitch"),
                new ValueListElement("shadertoy:shaders/glitch/digitalglitch.fs", "Digital glitch"),
                new ValueListElement("shadertoy:shaders/glitch/oldtv.fs", "Old TV"),
                new ValueListElement("shadertoy:shaders/glitch/oldtv2.fs", "Old TV 2"),
                new ValueListElement("shadertoy:shaders/glitch/oldtv3.fs", "Old TV 3"),
                new ValueListElement("shadertoy:shaders/glitch/oldtv4.fs", "Old TV 4"),
                new ValueListElement("shadertoy:shaders/glitch/vcrtape.fs", "VCR tape"),
                new ValueListElement("shadertoy:shaders/glitch/vhs.fs", "VHS"),
                new ValueListElement("shadertoy:shaders/glitch/vhspaused.fs", "VHS paused"),
                new ValueListElement("shadertoy:shaders/glitch/mpeg_artifacts.fs", "MPEG artifact"),
                new ValueListElement("shadertoy:shaders/glitch/rgbshiftglitch.fs", "RGB shift glitch"),
                new ValueListElement("shadertoy:shaders/glitch/rgbshiftglitch2.fs", "RGB shift glitch 2"));
        glitchTypeProvider = new ValueListProvider<>(glitchShaders, new StepStringInterpolator("shadertoy:shaders/glitch/digitalglitch.fs"));
    }

    @Override
    protected void initRender(StatelessEffectRequest request) {
        ValueListElement value = glitchTypeProvider.getValueAt(request.getEffectPosition());

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
        TimelinePosition requestPosition = request.getEffectPosition();
        uniformUtil.bindFloatToUniform(programId, request.getEffectPosition().getSeconds().floatValue(), "iTime");

        int uniformLocation = GL31.glGetUniformLocation(programId, "iResolution");
        GL31.glUniform3f(uniformLocation, request.getCurrentFrame().getWidth(), request.getCurrentFrame().getHeight(), 0.0f);

        uniformUtil.bindFloatToUniform(programId, projectRepository.getFrameTime().floatValue(), "iTimeDelta");
        uniformUtil.bindFloatToUniform(programId, requestPosition.getSeconds().divide(projectRepository.getFrameTime(), 0, RoundingMode.FLOOR).floatValue(), "iFrame");

        uniformLocation = GL31.glGetUniformLocation(programId, "iDate");
        GL31.glUniform4f(uniformLocation, 0.0f, 0.0f, 0.0f, requestPosition.getSeconds().floatValue());

        uniformLocation = GL31.glGetUniformLocation(programId, "iMouse");
        GL31.glUniform4f(uniformLocation, 0.0f, 0.0f, 0.0f, 0.0f);
        // channelTime

        ValueListElement value = glitchTypeProvider.getValueAt(request.getEffectPosition());

        if (value.getId().endsWith("digitalglitch.fs")) {
            GL30.glActiveTexture(GL30.GL_TEXTURE2);
            int texture = textureLoader.loadTexture("shaders/glitch/texture/noise64.png");
            GL30.glBindTexture(GL30.GL_TEXTURE_2D, texture);
            uniformUtil.bindIntegerToUniform(programId, 2, "iChannel1");

            uniformLocation = GL31.glGetUniformLocation(programId, "iChannelResolution");
            GL31.glUniform3fv(uniformLocation, new float[]{64, 64, 0});
        } else if (value.getId().endsWith("vcrtape.fs")) {
            GL30.glActiveTexture(GL30.GL_TEXTURE2);
            int texture = textureLoader.loadTexture("shaders/glitch/texture/rgbnoise.png");
            GL30.glBindTexture(GL30.GL_TEXTURE_2D, texture);
            uniformUtil.bindIntegerToUniform(programId, 2, "iChannel1");

            uniformLocation = GL31.glGetUniformLocation(programId, "iChannelResolution");
            GL31.glUniform3fv(uniformLocation, new float[]{256, 256, 0});
        } else if (value.getId().endsWith("mpeg_artifacts.fs") || value.getId().endsWith("rgbshiftglitch.fs")) {
            GL30.glActiveTexture(GL30.GL_TEXTURE2);
            int texture = textureLoader.loadTexture("shaders/glitch/texture/rgbnoise64.png");
            GL30.glBindTexture(GL30.GL_TEXTURE_2D, texture);
            uniformUtil.bindIntegerToUniform(programId, 2, "iChannel1");

            uniformLocation = GL31.glGetUniformLocation(programId, "iChannelResolution");
            GL31.glUniform3fv(uniformLocation, new float[]{64, 64, 0});
        }
    }

    @Override
    protected String getInputTextureName() {
        return "iChannel0";
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return null;
    }

}
