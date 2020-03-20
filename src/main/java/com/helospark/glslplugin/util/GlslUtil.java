package com.helospark.glslplugin.util;

import static org.lwjgl.opengl.EXTFramebufferObject.GL_FRAMEBUFFER_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.GL_RENDERBUFFER_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glBindFramebufferEXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glBindRenderbufferEXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glFramebufferRenderbufferEXT;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glBindAttribLocation;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glUseProgram;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.slf4j.Logger;

import com.google.common.base.Charsets;
import com.helospark.glslplugin.GlslPlatform;
import com.helospark.glslplugin.shadertoy.ShadertoyShaderPreprocessor;
import com.helospark.glslplugin.transition.GlTransitionsPreprocessor;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.message.WatchedFileChangedMessage;
import com.helospark.tactview.core.service.FileChangedWatchService;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.core.util.messaging.IntervalDirtyMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class GlslUtil {
    private Map<String, Integer> programCache = new HashMap<>();
    private Map<String, Integer> shaderCache = new HashMap<>();

    private MessagingService messagingService;
    private FileChangedWatchService fileWatchService;
    private ShadertoyShaderPreprocessor shadertoyPreprocessor;
    private GlTransitionsPreprocessor glTransitionsPreprocessor;
    private VertexBufferProvider vertexBufferProvider;
    private RenderBufferProvider renderBufferProvider;
    private UniformUtil uniformUtil;

    @Slf4j
    private Logger logger;

    public GlslUtil(MessagingService messagingService, FileChangedWatchService fileWatchService, ShadertoyShaderPreprocessor shadertoyPreprocessor,
            VertexBufferProvider vertexBufferProvider, RenderBufferProvider renderBufferProvider, UniformUtil uniformUtil, GlTransitionsPreprocessor glTransitionsPreprocessor) {
        this.messagingService = messagingService;
        this.fileWatchService = fileWatchService;
        this.shadertoyPreprocessor = shadertoyPreprocessor;
        this.vertexBufferProvider = vertexBufferProvider;
        this.renderBufferProvider = renderBufferProvider;
        this.uniformUtil = uniformUtil;
        this.glTransitionsPreprocessor = glTransitionsPreprocessor;
    }

    @PostConstruct
    public void init() {
        messagingService.register(WatchedFileChangedMessage.class, message -> {
            GlslPlatform.runOnGlThread(() -> {
                // TODO: clear only changed elements
                programCache.clear();
                shaderCache.clear();

                // TODO: glDelete* to avoid leaking resources

                // Hack to clear all cache, but we don't know what actually should be cleared
                messagingService.sendAsyncMessage(new IntervalDirtyMessage(List.of(new TimelineInterval(TimelinePosition.ofSeconds(0.0), TimelinePosition.ofSeconds(1000.0)))));
            });
        });
    }

    public int useProgram(String vertexShader, String fragmentShader) {
        Integer cachedProgram = programCache.get(createProgramCacheKey(vertexShader, fragmentShader));
        if (cachedProgram != null) {
            glUseProgram(cachedProgram);
            return cachedProgram;
        } else {
            int createdProgram = createProgramInternal(vertexShader, fragmentShader);
            programCache.put(createProgramCacheKey(vertexShader, fragmentShader), createdProgram);
            glUseProgram(createdProgram);
            return createdProgram;
        }
    }

    private String createProgramCacheKey(String vertexShader, String fragmentShader) {
        return fragmentShader + "_" + vertexShader;
    }

    private int createProgramInternal(String vertexShader, String fragmentShader) throws AssertionError {
        int program = glCreateProgram();
        int vshader = createShader(vertexShader, GL_VERTEX_SHADER);
        int fshader = createShader(fragmentShader, GL_FRAGMENT_SHADER);
        glAttachShader(program, vshader);
        glAttachShader(program, fshader);
        glBindAttribLocation(program, 0, "position");
        glLinkProgram(program);
        int linked = glGetProgrami(program, GL_LINK_STATUS);
        String programLog = glGetProgramInfoLog(program);
        if (programLog.trim().length() > 0) {
            System.err.println(programLog);
        }
        if (linked == 0) {
            throw new AssertionError("Could not link program");
        }
        return program;
    }

    public int createShader(String resource, int type) {
        Integer cachedShader = shaderCache.get(getCacheKey(resource, type));
        if (cachedShader != null) {
            return cachedShader;
        }
        int createdShader = createShaderInternal(resource, type);
        shaderCache.put(getCacheKey(resource, type), createdShader);
        return createdShader;
    }

    private String getCacheKey(String resource, int type) {
        return resource + "_" + type;
    }

    private int createShaderInternal(String resource, int type) throws AssertionError {
        try {
            int shader = GL30.glCreateShader(type);

            String[] parts = resource.split(":");
            String protocol = "";
            String fileName = "";
            if (parts.length == 2) {
                protocol = parts[0];
                fileName = parts[1];
            } else {
                fileName = resource;
            }

            String fileToLoad = this.getClass().getResource("/" + fileName).getFile();
            fileWatchService.requestFileWatch(new File(fileToLoad));
            byte[] dataBytes = new FileInputStream(fileToLoad).readAllBytes();
            String content = new String(dataBytes, Charsets.UTF_8);

            if (protocol.equals("shadertoy")) {
                content = shadertoyPreprocessor.preprocess(content);
            } else if (protocol.equals("gltransitions")) {
                content = glTransitionsPreprocessor.preprocess(content);
            }

            //else {
            // TODO: introduce chain when needed
            //}

            logger.debug("Compiling shader:\n" + content);

            ByteBuffer sourceO = ByteBuffer.wrap(content.getBytes());

            sourceO.position(0);

            ByteBuffer source = ByteBuffer.allocateDirect(sourceO.capacity());
            source.put(sourceO);
            source.position(0);

            PointerBuffer strings = BufferUtils.createPointerBuffer(1);
            IntBuffer lengths = BufferUtils.createIntBuffer(1);

            strings.put(0, source);
            lengths.put(0, source.remaining());

            GL30.glShaderSource(shader, strings, lengths);

            GL31.glCompileShader(shader);
            int compiled = GL31.glGetShaderi(shader, GL31.GL_COMPILE_STATUS);
            String shaderLog = GL31.glGetShaderInfoLog(shader);
            if (shaderLog.trim().length() > 0) {
                System.err.println(shaderLog);
            }
            if (compiled == 0) {
                throw new AssertionError("Could not compile shader");
            }
            return shader;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void renderFullScreenQuad() {
        int[] attachments = {GL31.GL_COLOR_ATTACHMENT1};
        GL11.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);;
        glClear(GL_COLOR_BUFFER_BIT);

        GL31.glDrawBuffers(attachments);
        vertexBufferProvider.bindQuad();
    }

    public int bindClipImageToTexture(int programId, String name, ReadOnlyClipImage image, int channelId) {
        ByteBuffer inputBuffer = image.getBuffer();
        int width = image.getWidth();
        int height = image.getHeight();

        GL31.glActiveTexture(GL31.GL_TEXTURE0 + channelId);
        int texture = createTexture();
        glBindTexture(GL_TEXTURE_2D, texture);
        inputBuffer.position(0);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, inputBuffer);

        uniformUtil.bindIntegerToUniform(programId, channelId, name);

        return texture;
    }

    public int createTexture() {
        int tex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, tex);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

        glBindTexture(GL_TEXTURE_2D, 0);
        return tex;
    }

    public RenderBufferData attachOutputBuffer(int height, int width) {
        RenderBufferData renderbuffer = renderBufferProvider.getRenderbuffer(width, height);

        int fbo = renderbuffer.fbo;
        int rbo = renderbuffer.rbo;

        glBindFramebufferEXT(GL31.GL_FRAMEBUFFER, fbo);
        glBindRenderbufferEXT(GL_RENDERBUFFER_EXT, rbo);
        glFramebufferRenderbufferEXT(GL_FRAMEBUFFER_EXT, GL31.GL_COLOR_ATTACHMENT1, GL_RENDERBUFFER_EXT, rbo);
        return renderbuffer;
    }

    public ClipImage readColorAttachement(int width, int height) {
        ClipImage result = ClipImage.fromSize(width, height);
        result.getBuffer().position(0);

        GL31.glReadBuffer(GL31.GL_COLOR_ATTACHMENT1);
        GL31.glReadPixels(0, 0, width, height, GL_RGBA, GL31.GL_UNSIGNED_BYTE, result.getBuffer());
        return result;
    }
}
