package com.helospark.glslplugin.init;

import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGetString;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.util.Optional;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helospark.glslplugin.GlslPlatform;

public class GlslInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlslInitializer.class);

    private static Object lock = new Object();
    private static volatile boolean initialized = false;
    private static long window;
    private static Callback debugProc;

    private static String glslVersionCache = null;

    public static void initializeGlsl() {
        synchronized (lock) {
            if (!initialized) {
                GlslPlatform.runOnGlThread(() -> {
                    init();
                });
            }
        }
    }

    private static void init() {
        try {
            glfwDefaultWindowHints();
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0);
            glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
            glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

            window = glfwCreateWindow(1, 1, "Dummy window for GLFW (GLSL shader plugin)", NULL, NULL);
            if (window == NULL) {
                throw new AssertionError("Failed to create the GLFW window, most likely due to not supported OpenGL 2.0");
            }

            glfwMakeContextCurrent(window);
            GLCapabilities caps = GL.createCapabilities();
            if (!caps.GL_EXT_framebuffer_object) {
                throw new AssertionError("Framebuffer required");
            }

            debugProc = GLUtil.setupDebugMessageCallback();

            glClearColor(1.0f, 1.0f, 1.0f, 0.0f); // using alpha = 0.0 is important here for the outline to work!
            glEnable(GL_DEPTH_TEST);
            glEnable(GL_CULL_FACE);

            initialized = true;
        } catch (Exception e) {
            LOGGER.error("Unable to initialize GLSL plugin", e);
        }
    }

    public static Optional<String> getGlslVersion() {
        String result = glslVersionCache;
        if (result == null) {
            try {
                initializeGlsl();
                result = GlslPlatform.runOnGlThread(() -> {
                    return glGetString(GL20.GL_SHADING_LANGUAGE_VERSION);
                });

            } catch (Exception e) {
                LOGGER.error("Unable to query GLSL version", e);
            }

            if (result != null) {
                LOGGER.info("GLSL version " + result);
            } else {
                LOGGER.info("No GLSL present on system, some effects will be disabled");
            }
            glslVersionCache = result;
        }
        return Optional.ofNullable(result);
    }

    public static void destroy() {
        try {
            synchronized (lock) {
                if (initialized) {
                    GlslPlatform.runOnGlThread(() -> {
                        debugProc.free();
                        glfwDestroyWindow(window);
                    });
                }
            }
        } catch (Exception e) {
            LOGGER.error("Unable to initialize GLSL plugin", e);
        }
    }

}
