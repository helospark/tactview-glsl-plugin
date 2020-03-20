package com.helospark.glslplugin.effects;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL20.glUseProgram;

import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL31;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.glslplugin.GlslPlatform;
import com.helospark.glslplugin.util.GlslUtil;
import com.helospark.glslplugin.util.RenderBufferData;
import com.helospark.glslplugin.util.RenderBufferProvider;
import com.helospark.glslplugin.util.VertexBufferProvider;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.ValueProviderDescriptor;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public abstract class AbstractRegularGlslStatelessVideoEffect extends StatelessVideoEffect {
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
    }

    public AbstractRegularGlslStatelessVideoEffect(JsonNode node, LoadMetadata loadMetadata) {
        super(node, loadMetadata);
    }

    public AbstractRegularGlslStatelessVideoEffect(StatelessVideoEffect effect, CloneRequestMetadata cloneRequestMetadata) {
        super(effect, cloneRequestMetadata);
    }

    @Override
    public ReadOnlyClipImage createFrame(StatelessEffectRequest request) {
        return GlslPlatform.runOnGlThread(() -> {
            glDisable(GL_DEPTH_TEST);

            initRender(request);

            if (vertexShader == null || fragmentShader == null) {
                return ClipImage.sameSizeAs(request.getCurrentFrame());
            }

            int programId = glslUtil.useProgram(vertexShader, fragmentShader);

            int height = getHeight(request);
            int width = getWidth(request);
            GL11.glViewport(0, 0, width, height);

            RenderBufferData renderbuffer = glslUtil.attachOutputBuffer(height, width);

            int texture = glslUtil.bindClipImageToTexture(programId, getInputTextureName(), request.getCurrentFrame(), 0);

            bindUniforms(programId, request);

            glslUtil.renderFullScreenQuad();

            ClipImage result = glslUtil.readColorAttachement(width, height);

            clearRender();

            vertexBufferProvider.unbind();
            glBindTexture(GL_TEXTURE_2D, 0);
            glUseProgram(0);
            GL31.glDeleteTextures(texture);
            renderBufferProvider.returnRenderBufferData(width, height, renderbuffer);

            return result;
        });

    }

    protected void clearRender() {

    }

    protected void initRender(StatelessEffectRequest request) {

    }

    protected void bindUniforms(int programId, StatelessEffectRequest request) {

    }

    protected String getInputTextureName() {
        return "inputImage";
    }

    @Override
    protected void initializeValueProviderInternal() {

    }

    @Override
    protected List<ValueProviderDescriptor> getValueProvidersInternal() {
        return List.of();
    }

    protected int getWidth(StatelessEffectRequest request) {
        return request.getCurrentFrame().getWidth();
    }

    protected int getHeight(StatelessEffectRequest request) {
        return request.getCurrentFrame().getHeight();
    }

}
