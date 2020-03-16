package com.helospark.glslplugin;

import static org.lwjgl.opengl.EXTFramebufferObject.GL_FRAMEBUFFER_COMPLETE_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.GL_FRAMEBUFFER_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.GL_RENDERBUFFER_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glBindFramebufferEXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glBindRenderbufferEXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glCheckFramebufferStatusEXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glFramebufferRenderbufferEXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glGenFramebuffersEXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glGenRenderbuffersEXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glRenderbufferStorageEXT;
import static org.lwjgl.opengl.GL11.GL_RGBA8;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.lwjgl.opengl.GL31;

import com.helospark.lightdi.annotation.Component;

@Component
public class RenderBufferProvider {
    private Map<String, RenderBufferData> frameBufferCache = new HashMap<>();

    private Queue<Integer> textureFrameBuffers = new ConcurrentLinkedQueue<>();

    public RenderBufferData getRenderbuffer(int width, int height) {
        RenderBufferData data = frameBufferCache.remove(createCacheKey(width, height));

        if (data != null) {
            return data;
        } else {
            int fbo = glGenFramebuffersEXT();
            glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, fbo);

            int rbo = glGenRenderbuffersEXT();
            glBindRenderbufferEXT(GL_RENDERBUFFER_EXT, rbo);
            glRenderbufferStorageEXT(GL_RENDERBUFFER_EXT, GL_RGBA8, width, height);
            glFramebufferRenderbufferEXT(GL31.GL_FRAMEBUFFER, GL31.GL_COLOR_ATTACHMENT1, GL31.GL_RENDERBUFFER, rbo);

            int status = glCheckFramebufferStatusEXT(GL_FRAMEBUFFER_EXT);
            if (status != GL_FRAMEBUFFER_COMPLETE_EXT) {
                throw new AssertionError("Incomplete framebuffer: " + status);
            }
            glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
            glBindRenderbufferEXT(GL_RENDERBUFFER_EXT, 0);

            RenderBufferData renderBufferData = new RenderBufferData();
            renderBufferData.fbo = fbo;
            renderBufferData.rbo = rbo;

            return renderBufferData;
        }
    }

    public Integer getFrameBufferAttachedTexture(int texture, int colorAttachement) {
        Integer buffer = textureFrameBuffers.poll();

        if (buffer != null) {
            glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, buffer);
            GL31.glFramebufferTexture2D(GL31.GL_FRAMEBUFFER, colorAttachement, GL31.GL_TEXTURE_2D, texture, 0);

            return buffer;
        } else {
            int fbo = glGenFramebuffersEXT();
            glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, fbo);

            GL31.glFramebufferTexture2D(GL31.GL_FRAMEBUFFER, colorAttachement, GL31.GL_TEXTURE_2D, texture, 0);

            int status = glCheckFramebufferStatusEXT(GL_FRAMEBUFFER_EXT);
            if (status != GL_FRAMEBUFFER_COMPLETE_EXT) {
                throw new AssertionError("Incomplete framebuffer: " + status);
            }
            glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);

            return fbo;
        }
    }

    public void returnFrameBuffer(int buffer) {
        textureFrameBuffers.offer(buffer);
    }

    public void returnRenderBufferData(int width, int height, RenderBufferData data) {
        frameBufferCache.put(createCacheKey(width, height), data);
    }

    private String createCacheKey(int width, int height) {
        return "" + width + "x" + height;
    }

    public static class RenderBufferData {
        int fbo, rbo;
    }

}
