package com.helospark.glslplugin;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GlslPlatform {
    public static final ExecutorService GL_EXECUTOR = Executors.newSingleThreadExecutor();

    public static void runOnGlThread(Runnable runnable) {
        try {
            GL_EXECUTOR.submit(runnable).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T runOnGlThread(Callable<T> runnable) {
        try {
            T result = GL_EXECUTOR.submit(runnable).get();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
