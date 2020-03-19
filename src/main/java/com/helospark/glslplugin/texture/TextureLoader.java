package com.helospark.glslplugin.texture;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.helospark.lightdi.annotation.Component;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;
import com.helospark.tactview.core.util.BufferedImageToClipFrameResultConverter;

@Component
public class TextureLoader {
    private Map<String, TextureData> textureCache = new HashMap<>();

    private BufferedImageToClipFrameResultConverter converter;

    public TextureLoader(BufferedImageToClipFrameResultConverter converter) {
        this.converter = converter;
    }

    public TextureData loadTexture(String path) {
        TextureData data = textureCache.get(path);
        if (data == null) {
            data = loadTextureInternal(path);
            textureCache.put(path, data);
        }
        return data;
    }

    private TextureData loadTextureInternal(String path) {
        try {
            int tex = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, tex);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

            String fileToLoad = this.getClass().getResource("/" + path).getFile();

            BufferedImage image = ImageIO.read(new FileInputStream(fileToLoad));

            ReadOnlyClipImage clipImage = converter.convert(image);

            clipImage.getBuffer().position(0);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, clipImage.getBuffer());

            return TextureData.builder()
                    .withId(tex)
                    .withWidth(image.getWidth())
                    .withHeight(image.getHeight())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
