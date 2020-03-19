package com.helospark.glslplugin.effects;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL20.glUseProgram;

import java.nio.ByteBuffer;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL31;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.glslplugin.GlslPlatform;
import com.helospark.glslplugin.texture.TextureLoader;
import com.helospark.glslplugin.util.GlslUtil;
import com.helospark.glslplugin.util.RenderBufferData;
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
import com.helospark.tactview.core.timeline.effect.interpolation.provider.DoubleProvider;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.IntegerProvider;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.ReflectionUtil;

// https://john-chapman-graphics.blogspot.com/2013/02/pseudo-lens-flare.html
// https://github.com/jeromeetienne/threex.sslensflare
public class GlslLensFlareEffect extends StatelessVideoEffect {
    protected RenderBufferProvider renderBufferProvider;
    protected VertexBufferProvider vertexBufferProvider;
    protected GlslUtil glslUtil;
    private UniformUtil uniformUtil;
    private TextureLoader textureLoader;

    int tex;
    int firstPhaseOutputTexture;
    int secondPhaseOutputTexture;
    double scale = 1.0;

    private DoubleProvider scaleProvider;

    private DoubleProvider highlightMultiplierProvider;
    private DoubleProvider highlightBiasProvider;

    private DoubleProvider distanceFalloffProvider;
    private DoubleProvider haloWidthProvider;
    private DoubleProvider haloDistortionProvider;
    private DoubleProvider ghostDispersalProvider;
    private IntegerProvider maxGhostsProvider;

    private DoubleProvider artifactScaleProvider;
    private DoubleProvider effectStrengthProvider;

    public GlslLensFlareEffect(TimelineInterval interval, GlslUtil glslUtil, RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider,
            UniformUtil uniformUtil, TextureLoader textureLoader) {
        super(interval);
        tex = GlslPlatform.runOnGlThread(() -> createTexture());
        firstPhaseOutputTexture = GlslPlatform.runOnGlThread(() -> createTexture());
        secondPhaseOutputTexture = GlslPlatform.runOnGlThread(() -> createTexture());

        this.glslUtil = glslUtil;
        this.vertexBufferProvider = vertexBufferProvider;
        this.renderBufferProvider = renderBufferProvider;
        this.uniformUtil = uniformUtil;
        this.textureLoader = textureLoader;
    }

    public GlslLensFlareEffect(JsonNode node, LoadMetadata loadMetadata) {
        super(node, loadMetadata);
    }

    public GlslLensFlareEffect(StatelessVideoEffect effect, CloneRequestMetadata cloneRequestMetadata) {
        super(effect, cloneRequestMetadata);

        ReflectionUtil.copyOrCloneFieldFromTo(effect, this);
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        return GlslPlatform.runOnGlThread(() -> {
            glDisable(GL_DEPTH_TEST);

            int width = request.getCurrentFrame().getWidth();
            int height = request.getCurrentFrame().getHeight();

            // first phase
            int programId = glslUtil.createProgram("shaders/lensflare/scale-pass.vs", "shaders/lensflare/flare-first-pass.fs");
            GL31.glUseProgram(programId);
            int downSampledWidth = (int) (width * scale);
            int downSampledHeight = (int) (height * scale);
            glBindTexture(GL31.GL_TEXTURE_2D, firstPhaseOutputTexture); // output 
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, downSampledWidth, downSampledHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);

            Integer renderbuffer = renderBufferProvider.getFrameBufferAttachedTexture(firstPhaseOutputTexture, GL31.GL_COLOR_ATTACHMENT1);
            GL31.glBindFramebuffer(GL31.GL_FRAMEBUFFER, renderbuffer);

            GL11.glViewport(0, 0, width, height);

            ByteBuffer inputBuffer = request.getCurrentFrame().getBuffer();

            bindInputTexture(height, width, inputBuffer, programId);

            TimelinePosition position = request.getEffectPosition();
            uniformUtil.bindDoubleProviderToUniform(programId, scaleProvider, position, "scale");
            uniformUtil.bindDoubleProviderToUniform(programId, highlightBiasProvider, position, "uBias");
            uniformUtil.bindDoubleProviderToUniform(programId, highlightMultiplierProvider, position, "uScale");

            render(programId);

            // second phase

            programId = glslUtil.createProgram("shaders/lensflare/pass.vs", "shaders/lensflare/flare-second-pass.fs");
            GL31.glUseProgram(programId);

            GL31.glActiveTexture(GL31.GL_TEXTURE3);
            glBindTexture(GL31.GL_TEXTURE_2D, secondPhaseOutputTexture);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
            Integer renderbuffer2 = renderBufferProvider.getFrameBufferAttachedTexture(secondPhaseOutputTexture, GL31.GL_COLOR_ATTACHMENT1);
            GL31.glBindFramebuffer(GL31.GL_FRAMEBUFFER, renderbuffer2);

            GL11.glViewport(0, 0, width, height);

            GL31.glActiveTexture(GL31.GL_TEXTURE0);
            GL31.glBindTexture(GL31.GL_TEXTURE_2D, firstPhaseOutputTexture);
            uniformUtil.bindIntegerToUniform(programId, 0, "tDiffuse");

            GL31.glActiveTexture(GL31.GL_TEXTURE1);
            int lensColorTexture = textureLoader.loadTexture("shaders/lensflare/data/lenscolor.png").getId();
            glBindTexture(GL31.GL_TEXTURE_2D, lensColorTexture);

            uniformUtil.bindDoubleProviderToUniform(programId, ghostDispersalProvider, position, "uGhostDispersal");
            uniformUtil.bindDoubleProviderToUniform(programId, haloWidthProvider, position, "uHaloWidth");
            uniformUtil.bindDoubleProviderToUniform(programId, haloDistortionProvider, position, "uDistortion");
            uniformUtil.bindDoubleProviderToUniform(programId, distanceFalloffProvider, position, "distanceFalloff");
            uniformUtil.bindIntegerProviderToUniform(programId, maxGhostsProvider, position, "maxGhosts");

            uniformUtil.bindIntegerToUniform(programId, 1, "tLensColor");
            uniformUtil.bindVec2ToUniform(programId, downSampledWidth, downSampledHeight, "textureSize");

            render(programId);

            // 3rd phase
            programId = glslUtil.createProgram("shaders/lensflare/pass.vs", "shaders/lensflare/flare-third-pass.fs");
            GL31.glUseProgram(programId);

            RenderBufferData renderbufferData = renderBufferProvider.getRenderbuffer(width, height);
            GL31.glBindFramebuffer(GL31.GL_FRAMEBUFFER, renderbufferData.fbo);

            GL11.glViewport(0, 0, width, height);

            GL31.glActiveTexture(GL31.GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, tex);
            uniformUtil.bindIntegerToUniform(programId, 0, "tDiffuse");

            GL31.glActiveTexture(GL31.GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, secondPhaseOutputTexture);
            uniformUtil.bindIntegerToUniform(programId, 1, "tLensColor");

            GL31.glActiveTexture(GL31.GL_TEXTURE2);
            glBindTexture(GL_TEXTURE_2D, textureLoader.loadTexture("shaders/lensflare/data/lensdirt.png").getId());
            uniformUtil.bindIntegerToUniform(programId, 2, "tLensDirt");

            GL31.glActiveTexture(GL31.GL_TEXTURE3);
            glBindTexture(GL_TEXTURE_2D, textureLoader.loadTexture("shaders/lensflare/data/lensstar.png").getId());
            uniformUtil.bindIntegerToUniform(programId, 3, "tLensStar");

            uniformUtil.bindDoubleProviderToUniform(programId, artifactScaleProvider, position, "artefactScale");
            uniformUtil.bindDoubleProviderToUniform(programId, effectStrengthProvider, position, "effectStrength");

            render(programId);

            ClipImage result = readColorAttachement(GL31.GL_COLOR_ATTACHMENT1, width, height);

            vertexBufferProvider.unbind();
            glBindTexture(GL_TEXTURE_2D, 0);
            glUseProgram(0);
            renderBufferProvider.returnRenderBufferData(width, height, renderbufferData);
            renderBufferProvider.returnFrameBuffer(renderbuffer);
            renderBufferProvider.returnFrameBuffer(renderbuffer2);

            return result;
        });

    }

    protected ClipImage readColorAttachement(int colorAttachement, int width, int height) {
        ClipImage result = ClipImage.fromSize(width, height);
        result.getBuffer().position(0);

        GL31.glReadBuffer(colorAttachement);
        GL31.glReadPixels(0, 0, width, height, GL_RGBA, GL31.GL_UNSIGNED_BYTE, result.getBuffer());
        return result;
    }

    protected void render(int programId) {
        int[] attachments = {GL31.GL_COLOR_ATTACHMENT1};
        GL11.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);;
        glClear(GL_COLOR_BUFFER_BIT);

        GL31.glDrawBuffers(attachments);
        vertexBufferProvider.bindQuad();
    }

    @Override
    protected void initializeValueProviderInternal() {
        scaleProvider = new DoubleProvider(0.1, 1.0, new MultiKeyframeBasedDoubleInterpolator(1.0));

        highlightMultiplierProvider = new DoubleProvider(1.0, 20.0, new MultiKeyframeBasedDoubleInterpolator(10.0));
        highlightBiasProvider = new DoubleProvider(0.0, 1.0, new MultiKeyframeBasedDoubleInterpolator(0.9));

        distanceFalloffProvider = new DoubleProvider(1.0, 20.0, new MultiKeyframeBasedDoubleInterpolator(10.0));
        haloWidthProvider = new DoubleProvider(1.0, 10.0, new MultiKeyframeBasedDoubleInterpolator(1.5));
        haloDistortionProvider = new DoubleProvider(0.1, 1.0, new MultiKeyframeBasedDoubleInterpolator(0.25));
        ghostDispersalProvider = new DoubleProvider(0.1, 1.0, new MultiKeyframeBasedDoubleInterpolator(0.4));
        maxGhostsProvider = new IntegerProvider(1, 10, new MultiKeyframeBasedDoubleInterpolator(4.0));

        artifactScaleProvider = new DoubleProvider(1.0, 30.0, new MultiKeyframeBasedDoubleInterpolator(10.0));
        effectStrengthProvider = new DoubleProvider(0.0, 2.0, new MultiKeyframeBasedDoubleInterpolator(0.5));
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        ValueProviderDescriptor scaleProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(scaleProvider)
                .withName("Prescale")
                .build();
        ValueProviderDescriptor multiplierDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(highlightMultiplierProvider)
                .withName("Highlight multiplier")
                .build();
        ValueProviderDescriptor highlightBiasProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(highlightBiasProvider)
                .withName("Highlight threshold")
                .build();
        ValueProviderDescriptor distanceFalloffProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(distanceFalloffProvider)
                .withName("Distance falloff")
                .build();
        ValueProviderDescriptor haloWidthProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(haloWidthProvider)
                .withName("Halo width")
                .build();

        ValueProviderDescriptor haloDistortionProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(haloDistortionProvider)
                .withName("Halo distortion")
                .build();

        ValueProviderDescriptor ghostDispersalProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(ghostDispersalProvider)
                .withName("Ghost dispersal")
                .build();

        ValueProviderDescriptor maxGhostsProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(maxGhostsProvider)
                .withName("Max ghosts")
                .build();

        ValueProviderDescriptor artifactScaleProviderDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(artifactScaleProvider)
                .withName("Artifact scale")
                .build();

        ValueProviderDescriptor effectStrengthDescriptor = ValueProviderDescriptor.builder()
                .withKeyframeableEffect(effectStrengthProvider)
                .withName("Strength")
                .build();
        return List.of(scaleProviderDescriptor, multiplierDescriptor, highlightBiasProviderDescriptor, distanceFalloffProviderDescriptor, haloWidthProviderDescriptor, haloDistortionProviderDescriptor,
                ghostDispersalProviderDescriptor, maxGhostsProviderDescriptor, artifactScaleProviderDescriptor, effectStrengthDescriptor);
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return new GlslLensFlareEffect(this, cloneRequestMetadata);
    }

    private int createTexture() {
        int tex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, tex);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

        glBindTexture(GL_TEXTURE_2D, 0);
        return tex;
    }

    protected void bindInputTexture(int height, int width, ByteBuffer resultBuffer, int programId) {
        GL31.glActiveTexture(GL31.GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, tex);
        resultBuffer.position(0);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, resultBuffer);
    }

}
