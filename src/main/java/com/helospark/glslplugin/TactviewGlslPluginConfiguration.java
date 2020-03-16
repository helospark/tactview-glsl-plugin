package com.helospark.glslplugin;

import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Callback;

import com.helospark.lightdi.annotation.ComponentScan;
import com.helospark.lightdi.annotation.Configuration;

@Configuration
@ComponentScan
public class TactviewGlslPluginConfiguration {
    private long window;
    private Callback debugProc;

    @PostConstruct
    public void initializeGlsl() throws InterruptedException, ExecutionException {
        GlslPlatform.runOnGlThread(() -> {
            init();
        });
    }

    void init() {
        try {
            if (!glfwInit())
                throw new IllegalStateException("Unable to initialize GLFW");

            glfwDefaultWindowHints();
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0);
            glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
            glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

            window = glfwCreateWindow(1, 1, "Dummy window for GLFW (GLSL shader plugin)", NULL, NULL);
            if (window == NULL) {
                throw new AssertionError("Failed to create the GLFW window");
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void destroy() {
        GlslPlatform.runOnGlThread(() -> {
            debugProc.free();
            glfwDestroyWindow(window);
        });
    }
}
