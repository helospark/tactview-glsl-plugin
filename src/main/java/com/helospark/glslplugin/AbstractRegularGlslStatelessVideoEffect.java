package com.helospark.glslplugin;

import static org.lwjgl.opengl.EXTFramebufferObject.GL_FRAMEBUFFER_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.GL_RENDERBUFFER_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glBindFramebufferEXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glBindRenderbufferEXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glFramebufferRenderbufferEXT;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
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
import com.helospark.glslplugin.util.GlslUtil;
import com.helospark.glslplugin.util.RenderBufferData;
import com.helospark.glslplugin.util.RenderBufferProvider;
import com.helospark.glslplugin.util.VertexBufferProvider;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public abstract class AbstractRegularGlslStatelessVideoEffect extends StatelessVideoEffect {
    private int tex;

    protected String vertexShader;
    protected String fragmentShader;

    protected RenderBufferProvider renderBufferProvider;
    protected VertexBufferProvider vertexBufferProvider;
    protected GlslUtil glslUtil;

    public AbstractRegularGlslStatelessVideoEffect(TimelineInterval interval,
            RenderBufferProvider renderBufferProvider,
            VertexBufferProvider vertexBufferProvider,
            GlslUtil glslUtil) {
        super(interval);
        this.renderBufferProvider = renderBufferProvider;
        this.vertexBufferProvider = vertexBufferProvider;
        this.glslUtil = glslUtil;

        this.tex = GlslPlatform.runOnGlThread(() -> createTexture());
    }

    public AbstractRegularGlslStatelessVideoEffect(JsonNode node, LoadMetadata loadMetadata) {
        super(node, loadMetadata);
    }

    public AbstractRegularGlslStatelessVideoEffect(StatelessVideoEffect effect, CloneRequestMetadata cloneRequestMetadata) {
        super(effect, cloneRequestMetadata);
    }

    private int createTexture() {
        int tex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, tex);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

        glBindTexture(GL_TEXTURE_2D, 0);
        return tex;
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {

        return GlslPlatform.runOnGlThread(() -> {
            glDisable(GL_DEPTH_TEST);

            initRender(request);

            if (vertexShader == null || fragmentShader == null) {
                return ClipImage.sameSizeAs(request.getCurrentFrame());
            }

            int programId = glslUtil.createProgram(vertexShader, fragmentShader);
            glUseProgram(programId);

            int height = getHeight(request);
            int width = getWidth(request);

            RenderBufferData renderbuffer = renderBufferProvider.getRenderbuffer(width, height);

            int fbo = renderbuffer.fbo;
            int rbo = renderbuffer.rbo;

            GL11.glViewport(0, 0, width, height);

            glBindFramebufferEXT(GL31.GL_FRAMEBUFFER, fbo);
            glBindRenderbufferEXT(GL_RENDERBUFFER_EXT, rbo);
            int colorAttachement = GL31.GL_COLOR_ATTACHMENT1;
            glFramebufferRenderbufferEXT(GL_FRAMEBUFFER_EXT, colorAttachement, GL_RENDERBUFFER_EXT, rbo);

            ByteBuffer inputBuffer = request.getCurrentFrame().getBuffer();

            bindInputTexture(height, width, inputBuffer, programId);
            bindUniforms(programId, request);

            render(programId);

            ClipImage result = readColorAttachement(colorAttachement, width, height);

            vertexBufferProvider.unbind();
            glBindTexture(GL_TEXTURE_2D, 0);
            glUseProgram(0);
            renderBufferProvider.returnRenderBufferData(width, height, renderbuffer);

            return result;
        });

    }

    protected void initRender(StatelessEffectRequest request) {

    }

    protected ClipImage readColorAttachement(int colorAttachement, int width, int height) {
        ClipImage result = ClipImage.fromSize(width, height);
        result.getBuffer().position(0);

        GL31.glReadBuffer(colorAttachement);
        GL31.glReadPixels(0, 0, width, height, GL_RGBA, GL31.GL_UNSIGNED_BYTE, result.getBuffer());
        return result;
    }

    protected void bindUniforms(int programId, StatelessEffectRequest request) {

    }

    protected void bindInputTexture(int height, int width, ByteBuffer resultBuffer, int programId) {
        GL31.glActiveTexture(GL31.GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, tex);
        resultBuffer.position(0);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, resultBuffer);
        int inputTextureLocation = glGetUniformLocation(programId, getInputTextureName());
        glUniform1i(inputTextureLocation, 0);
    }

    protected String getInputTextureName() {
        return "inputImage";
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

    protected int getWidth(StatelessEffectRequest request) {
        return request.getCurrentFrame().getWidth();
    }

    protected int getHeight(StatelessEffectRequest request) {
        return request.getCurrentFrame().getHeight();
    }

}
