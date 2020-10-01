package com.helospark.glslplugin;

import static org.lwjgl.glfw.GLFW.glfwInit;

import com.helospark.tactview.core.init.ApplicationInitializedMainThreadCallback;

/**
 * This is an SPI service implementation that will be called from the main thread before initializing the DI framework.
 * This hack is needed, because if glfwInit is not called from the Java main thread FileChooser (save & load) crashes in nativeCode on Linux.
 * This seems like a bug, but hard to localize and the only suggestion given is to use the main thread for this methodcall.
 * However this way glfwInit is called in a different thread than general GL rendering, but it seems that does not cause
 * any issues at the moment
 * @author helospark
 */
public class TactviewGlfwInitializedCallback implements ApplicationInitializedMainThreadCallback {

    @Override
    public void call(String[] args) {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
    }

}
