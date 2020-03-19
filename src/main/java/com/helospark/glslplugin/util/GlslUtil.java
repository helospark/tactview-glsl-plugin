package com.helospark.glslplugin.util;

import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glBindAttribLocation;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glLinkProgram;

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
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.slf4j.Logger;

import com.google.common.base.Charsets;
import com.helospark.glslplugin.GlslPlatform;
import com.helospark.glslplugin.shadertoy.ShadertoyShaderProporessor;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.message.WatchedFileChangedMessage;
import com.helospark.tactview.core.service.FileChangedWatchService;
import com.helospark.tactview.core.timeline.TimelineInterval;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.util.logger.Slf4j;
import com.helospark.tactview.core.util.messaging.IntervalDirtyMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;

@Component
public class GlslUtil {
    private Map<String, Integer> programCache = new HashMap<>();
    private Map<String, Integer> shaderCache = new HashMap<>();

    private MessagingService messagingService;
    private FileChangedWatchService fileWatchService;
    private ShadertoyShaderProporessor shadertoyPreprocessor;

    @Slf4j
    private Logger logger;

    public GlslUtil(MessagingService messagingService, FileChangedWatchService fileWatchService, ShadertoyShaderProporessor shadertoyPreprocessor) {
        this.messagingService = messagingService;
        this.fileWatchService = fileWatchService;
        this.shadertoyPreprocessor = shadertoyPreprocessor;
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

    public int createProgram(String vertexShader, String fragmentShader) {
        Integer cachedProgram = programCache.get(createProgramCacheKey(vertexShader, fragmentShader));
        if (cachedProgram != null) {
            return cachedProgram;
        } else {
            int createdProgram = createProgramInternal(vertexShader, fragmentShader);
            programCache.put(createProgramCacheKey(vertexShader, fragmentShader), createdProgram);
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
            } //else {
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

}
