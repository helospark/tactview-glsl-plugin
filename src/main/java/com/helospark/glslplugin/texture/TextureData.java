package com.helospark.glslplugin.texture;

import javax.annotation.Generated;

public class TextureData {
    private int id;
    private int width, height;

    @Generated("SparkTools")
    private TextureData(Builder builder) {
        this.id = builder.id;
        this.width = builder.width;
        this.height = builder.height;
    }

    public int getId() {
        return id;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    @Generated("SparkTools")
    public static final class Builder {
        private int id;
        private int width;
        private int height;

        private Builder() {
        }

        public Builder withId(int id) {
            this.id = id;
            return this;
        }

        public Builder withWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder withHeight(int height) {
            this.height = height;
            return this;
        }

        public TextureData build() {
            return new TextureData(this);
        }
    }

}
