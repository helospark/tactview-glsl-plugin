package com.helospark.glslplugin.shadertoy;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;

import com.helospark.glslplugin.texture.TextureData;
import com.helospark.glslplugin.texture.TextureLoader;
import com.helospark.glslplugin.util.UniformUtil;
import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.repository.ProjectRepository;
import com.helospark.tactview.core.timeline.TimelinePosition;
import com.helospark.tactview.core.timeline.effect.StatelessEffectRequest;

@Component
public class ShadertoyHelpers {
    private UniformUtil uniformUtil;
    private ProjectRepository projectRepository;
    private TextureLoader textureLoader;

    public ShadertoyHelpers(UniformUtil uniformUtil, ProjectRepository projectRepository, TextureLoader textureLoader) {
        this.uniformUtil = uniformUtil;
        this.projectRepository = projectRepository;
        this.textureLoader = textureLoader;
    }

    public void attachCommonShadertoyUniforms(StatelessEffectRequest request, int programId) {
        TimelinePosition requestPosition = request.getEffectPosition();
        uniformUtil.bindFloatToUniform(programId, request.getEffectPosition().getSeconds().floatValue(), "iTime");

        int uniformLocation = GL31.glGetUniformLocation(programId, "iResolution");
        GL31.glUniform3f(uniformLocation, request.getCurrentFrame().getWidth(), request.getCurrentFrame().getHeight(), 0.0f);

        uniformUtil.bindFloatToUniform(programId, projectRepository.getFrameTime().floatValue(), "iTimeDelta");
        uniformUtil.bindFloatToUniform(programId, requestPosition.getSeconds().divide(projectRepository.getFrameTime(), 0, RoundingMode.FLOOR).floatValue(), "iFrame");

        uniformLocation = GL31.glGetUniformLocation(programId, "iDate");
        GL31.glUniform4f(uniformLocation, 0.0f, 0.0f, 0.0f, requestPosition.getSeconds().floatValue());

        uniformLocation = GL31.glGetUniformLocation(programId, "iMouse");
        GL31.glUniform4f(uniformLocation, 0.0f, 0.0f, 0.0f, 0.0f);
    }

    public void attachTextures(int programId, String... texturePaths) {
        List<TextureData> textures = new ArrayList<>();
        for (int i = 0; i < texturePaths.length; ++i) {
            TextureData texture = attachTexture(programId, texturePaths[i], i + 1, "iChannel" + (i + 1));
            textures.add(texture);
        }

        float[] sizes = new float[textures.size() * 2];
        for (int i = 0; i < textures.size(); ++i) {
            sizes[i * 2 + 0] = textures.get(i).getWidth();
            sizes[i * 2 + 1] = textures.get(i).getHeight();
        }
        int uniformLocation = GL31.glGetUniformLocation(programId, "iChannelResolution");
        GL31.glUniform3fv(uniformLocation, sizes);
    }

    public TextureData attachTexture(int programId, String path, int i, String samplerName) {
        GL30.glActiveTexture(GL30.GL_TEXTURE0 + i);
        TextureData texture = textureLoader.loadTexture(path);
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, texture.getId());
        uniformUtil.bindIntegerToUniform(programId, i, samplerName);
        return texture;
    }

}
