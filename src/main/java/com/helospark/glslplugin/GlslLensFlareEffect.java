package com.helospark.glslplugin;

import static org.lwjgl.opengl.EXTFramebufferObject.glBindFramebufferEXT;
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
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUseProgram;

import java.nio.ByteBuffer;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL31;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.glslplugin.RenderBufferProvider.RenderBufferData;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

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
            glBindFramebufferEXT(GL31.GL_FRAMEBUFFER, renderbuffer);

            GL11.glViewport(0, 0, width, height);

            ByteBuffer inputBuffer = request.getCurrentFrame().getBuffer();

            bindInputTexture(height, width, inputBuffer, programId);

            uniformUtil.bindFloatToUniform(programId, (float) scale, "scale");
            uniformUtil.bindFloatToUniform(programId, 0.7f, "uBias");
            uniformUtil.bindFloatToUniform(programId, 4.0f, "uScale");

            render(programId);

            // second phase

            programId = glslUtil.createProgram("shaders/lensflare/scale-pass.vs", "shaders/lensflare/flare-second-pass.fs");
            GL31.glUseProgram(programId);

            GL31.glActiveTexture(GL31.GL_TEXTURE3);
            glBindTexture(GL31.GL_TEXTURE_2D, secondPhaseOutputTexture);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
            Integer renderbufferData2 = renderBufferProvider.getFrameBufferAttachedTexture(secondPhaseOutputTexture, GL31.GL_COLOR_ATTACHMENT1);
            glBindFramebufferEXT(GL31.GL_FRAMEBUFFER, renderbufferData2);

            GL11.glViewport(0, 0, width, height);

            GL31.glActiveTexture(GL31.GL_TEXTURE0);
            GL31.glBindTexture(GL31.GL_TEXTURE_2D, firstPhaseOutputTexture);
            uniformUtil.bindIntegerToUniform(programId, 0, "tDiffuse");

            printUniforms(programId);

            GL31.glActiveTexture(GL31.GL_TEXTURE1);
            int lensColorTexture = textureLoader.loadTexture("shaders/lensflare/data/lenscolor.png");
            glBindTexture(GL31.GL_TEXTURE_2D, lensColorTexture);
            uniformUtil.bindIntegerToUniform(programId, 1, "tLensColor");
            uniformUtil.bindFloatToUniform(programId, 0.35f, "uGhostDispersal");
            uniformUtil.bindFloatToUniform(programId, 0.25f, "uHaloWidth");
            uniformUtil.bindFloatToUniform(programId, 4.5f, "uDistortion");
            uniformUtil.bindVec2ToUniform(programId, downSampledWidth, downSampledHeight, "textureSize");
            uniformUtil.bindFloatToUniform(programId, (float) (1.0), "scale");

            render(programId);

            // end of phase 2
            // phase 3
            programId = glslUtil.createProgram("shaders/lensflare/pass.vs", "shaders/lensflare/flare-third-pass.fs");
            GL31.glUseProgram(programId);

            RenderBufferData renderbufferData = renderBufferProvider.getRenderbuffer(width, height);
            glBindFramebufferEXT(GL31.GL_FRAMEBUFFER, renderbufferData.fbo);

            GL11.glViewport(0, 0, width, height);

            GL31.glActiveTexture(GL31.GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, tex);
            uniformUtil.bindIntegerToUniform(programId, 0, "tDiffuse");

            printUniforms(programId);

            GL31.glActiveTexture(GL31.GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, secondPhaseOutputTexture);
            uniformUtil.bindIntegerToUniform(programId, 1, "tLensColor");

            GL31.glActiveTexture(GL31.GL_TEXTURE2);
            glBindTexture(GL_TEXTURE_2D, textureLoader.loadTexture("shaders/lensflare/data/lensdirt.png"));
            uniformUtil.bindIntegerToUniform(programId, 2, "tLensDirt");

            GL31.glActiveTexture(GL31.GL_TEXTURE3);
            glBindTexture(GL_TEXTURE_2D, textureLoader.loadTexture("shaders/lensflare/data/lensstar.png"));
            uniformUtil.bindIntegerToUniform(programId, 3, "tLensStar");

            uniformUtil.bindFloatToUniform(programId, 10.0f, "artefactScale");
            uniformUtil.bindFloatToUniform(programId, 0.5f, "blendRatio");
            uniformUtil.bindFloatToUniform(programId, 0.5f, "opacity");
            uniformUtil.bindFloatToUniform(programId, 0.5f, "blendRatio");

            render(programId);

            /**
             * uniform vec2   textureSize;
            uniform float  uGhostDispersal;
            
            uniform float  uHaloWidth;
            uniform float  uDistortion;
             * 
             */

            // -----------
            ClipImage result = readColorAttachement(GL31.GL_COLOR_ATTACHMENT1, width, height);

            vertexBufferProvider.unbind();
            glBindTexture(GL_TEXTURE_2D, 0);
            glUseProgram(0);
            renderBufferProvider.returnRenderBufferData(width, height, renderbufferData);
            renderBufferProvider.returnFrameBuffer(renderbuffer);

            return result;
        });

    }

    protected void printUniforms(int programId) {
        int count = GL31.glGetProgrami(programId, GL31.GL_ACTIVE_UNIFORMS);
        System.out.println("Active Uniforms: %d " + count);

        for (int i = 0; i < count; i++) {
            String name = GL31.glGetActiveUniformName(programId, i);

            System.out.println("Uniform #%d Type: %u Name: " + i + " " + name);
        }
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
    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        return List.of();
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        return null;
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
        int inputTextureLocation = glGetUniformLocation(programId, "tDiffuse");
        glUniform1i(inputTextureLocation, 0);
    }

}
