package com.helospark.glslplugin.util;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import com.helospark.lightdi.annotation.Component;

@Component
public class VertexBufferProvider {
    int quad = -1;

    public int getQuad() {
        if (quad == -1) {
            FloatBuffer pb = BufferUtils.createFloatBuffer(2 * 6);
            pb.put(-1).put(-1);
            pb.put(1).put(-1);
            pb.put(1).put(1);
            pb.put(1).put(1);
            pb.put(-1).put(1);
            pb.put(-1).put(-1);
            pb.flip();
            this.quad = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, this.quad);
            glBufferData(GL_ARRAY_BUFFER, pb, GL_STATIC_DRAW);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
        }
        return quad;
    }

    public void bindQuad() {
        glBindBuffer(GL_ARRAY_BUFFER, getQuad());
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0L);
        glDrawArrays(GL_TRIANGLES, 0, 6);
    }

    public void unbind() {
        glDisableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

}
