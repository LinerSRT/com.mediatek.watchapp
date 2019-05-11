package com.nostra13.universalimageloader.core.assist;

public class ImageSize {
    private final int height;
    private final int width;

    public ImageSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public ImageSize(int width, int height, int rotation) {
        if (rotation % 180 != 0) {
            this.width = height;
            this.height = width;
            return;
        }
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public ImageSize scaleDown(int sampleSize) {
        return new ImageSize(this.width / sampleSize, this.height / sampleSize);
    }

    public ImageSize scale(float scale) {
        return new ImageSize((int) (((float) this.width) * scale), (int) (((float) this.height) * scale));
    }

    public String toString() {
        return this.width + "x" + this.height;
    }
}
