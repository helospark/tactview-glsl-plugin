package com.helospark.glslplugin.transition;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL20.glUseProgram;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL31;

import com.fasterxml.jackson.databind.JsonNode;
import com.helospark.glslplugin.GlslPlatform;
import com.helospark.glslplugin.util.GlslUtil;
import com.helospark.glslplugin.util.RenderBufferData;
import com.helospark.glslplugin.util.RenderBufferProvider;
import com.helospark.glslplugin.util.UniformUtil;
import com.helospark.glslplugin.util.VertexBufferProvider;
import com.helospark.tactview.core.clone.CloneRequestMetadata;
import com.helospark.tactview.core.save.LoadMetadata;
import com.helospark.tactview.core.timeline.StatelessEffect;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.effect.transition.AbstractVideoTransitionEffect;
import com.helospark.tactview.core.timeline.effect.transition.InternalStatelessVideoTransitionEffectRequest;
import com.helospark.tactview.core.timeline.image.ClipImage;

public class TestGlslTransition extends AbstractVideoTransitionEffect {

    protected String vertexShader;
    protected String fragmentShader;

    protected RenderBufferProvider renderBufferProvider;
    protected VertexBufferProvider vertexBufferProvider;
    protected GlslUtil glslUtil;
    private UniformUtil uniformUtil;

    public TestGlslTransition(TimelineInterval interval, RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider, GlslUtil glslUtil,
            UniformUtil uniformUtil) {
        super(interval);

        this.renderBufferProvider = renderBufferProvider;
        this.vertexBufferProvider = vertexBufferProvider;
        this.glslUtil = glslUtil;
        this.uniformUtil = uniformUtil;

        vertexShader = "shaders/common/common.vs";
        fragmentShader = "shaders/testtransition/transition.fs";
    }

    public TestGlslTransition(JsonNode node, LoadMetadata loadMetadata, RenderBufferProvider renderBufferProvider, VertexBufferProvider vertexBufferProvider, GlslUtil glslUtil,
            UniformUtil uniformUtil) {
        super(node, loadMetadata);

        this.renderBufferProvider = renderBufferProvider;
        this.vertexBufferProvider = vertexBufferProvider;
        this.glslUtil = glslUtil;
        this.uniformUtil = uniformUtil;

        vertexShader = "shaders/common/common.vs";
        fragmentShader = "shaders/testtransition/transition.fs";
    }

    @Override
    protected ClipImage applyTransitionInternal(InternalStatelessVideoTransitionEffectRequest request) {
        return GlslPlatform.runOnGlThread(() -> {
            glDisable(GL_DEPTH_TEST);

            if (vertexShader == null || fragmentShader == null) {
                return ClipImage.sameSizeAs(request.getFirstFrame());
            }

            int programId = glslUtil.useProgram(vertexShader, fragmentShader);

            int height = request.getFirstFrame().getHeight();
            int width = request.getFirstFrame().getWidth();
            GL11.glViewport(0, 0, width, height);

            RenderBufferData renderbuffer = glslUtil.attachOutputBuffer(height, width);

            int texture2 = glslUtil.bindClipImageToTexture(programId, "toFrame", request.getSecondFrame(), 2);
            int texture1 = glslUtil.bindClipImageToTexture(programId, "fromFrame", request.getFirstFrame(), 1);

            bindUniforms(programId, request);

            glslUtil.renderFullScreenQuad();

            ClipImage result = glslUtil.readColorAttachement(width, height);

            vertexBufferProvider.unbind();
            glBindTexture(GL_TEXTURE_2D, 0);
            glUseProgram(0);
            GL31.glDeleteTextures(texture1);
            GL31.glDeleteTextures(texture2);
            renderBufferProvider.returnRenderBufferData(width, height, renderbuffer);

            return result;
        });
    }

    private void bindUniforms(int programId, InternalStatelessVideoTransitionEffectRequest request) {
        uniformUtil.bindFloatToUniform(programId, (float) request.getProgress(), "progress");
    }

    @Override
    public StatelessEffect cloneEffect(CloneRequestMetadata cloneRequestMetadata) {
        // TODO Auto-generated method stub
        return null;
    }

}
