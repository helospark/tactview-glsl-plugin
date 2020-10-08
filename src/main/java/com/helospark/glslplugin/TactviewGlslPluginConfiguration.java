package com.helospark.glslplugin;

import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.helospark.glslplugin.init.GlslInitializer;
import com.helospark.lightdi.annotation.ComponentScan;
import com.helospark.lightdi.annotation.Configuration;

@Configuration
@ComponentScan
public class TactviewGlslPluginConfiguration {

    @PostConstruct
    public void initializeGlsl() throws InterruptedException, ExecutionException {
        GlslInitializer.initializeGlsl();
    }

    @PreDestroy
    public void destroy() {
        GlslInitializer.destroy();
    }
}
