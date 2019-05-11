package com.nostra13.universalimageloader.utils;

import android.opengl.GLES10;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;

public final class ImageSizeUtils {
    private static ImageSize maxBitmapSize;

    /* renamed from: com.nostra13.universalimageloader.utils.ImageSizeUtils$1 */
    static /* synthetic */ class C01961 {
        /* renamed from: $SwitchMap$com$nostra13$universalimageloader$core$assist$ViewScaleType */
        static final /* synthetic */ int[] f7x841fdc36 = new int[ViewScaleType.values().length];

        static {
            try {
                f7x841fdc36[ViewScaleType.FIT_INSIDE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                f7x841fdc36[ViewScaleType.CROP.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    static {
        int[] maxTextureSize = new int[1];
        GLES10.glGetIntegerv(3379, maxTextureSize, 0);
        int maxBitmapDimension = Math.max(maxTextureSize[0], 2048);
        maxBitmapSize = new ImageSize(maxBitmapDimension, maxBitmapDimension);
    }

    private ImageSizeUtils() {
    }

    public static ImageSize defineTargetSizeForView(ImageAware imageAware, ImageSize maxImageSize) {
        int width = imageAware.getWidth();
        if (width <= 0) {
            width = maxImageSize.getWidth();
        }
        int height = imageAware.getHeight();
        if (height <= 0) {
            height = maxImageSize.getHeight();
        }
        return new ImageSize(width, height);
    }

    public static int computeImageSampleSize(ImageSize srcSize, ImageSize targetSize, ViewScaleType viewScaleType, boolean powerOf2Scale) {
        int srcWidth = srcSize.getWidth();
        int srcHeight = srcSize.getHeight();
        int targetWidth = targetSize.getWidth();
        int targetHeight = targetSize.getHeight();
        int scale = 1;
        int halfWidth;
        int halfHeight;
        switch (C01961.f7x841fdc36[viewScaleType.ordinal()]) {
            case 1:
                if (!powerOf2Scale) {
                    scale = Math.max(srcWidth / targetWidth, srcHeight / targetHeight);
                    break;
                }
                halfWidth = srcWidth / 2;
                halfHeight = srcHeight / 2;
                while (true) {
                    if (halfWidth / scale <= targetWidth && halfHeight / scale <= targetHeight) {
                        break;
                    }
                    scale *= 2;
                }
                break;
            case 2:
                if (!powerOf2Scale) {
                    scale = Math.min(srcWidth / targetWidth, srcHeight / targetHeight);
                    break;
                }
                halfWidth = srcWidth / 2;
                halfHeight = srcHeight / 2;
                while (halfWidth / scale > targetWidth && halfHeight / scale > targetHeight) {
                    scale *= 2;
                }
                break;
        }
        if (scale < 1) {
            scale = 1;
        }
        return considerMaxTextureSize(srcWidth, srcHeight, scale, powerOf2Scale);
    }

    private static int considerMaxTextureSize(int srcWidth, int srcHeight, int scale, boolean powerOf2) {
        int maxWidth = maxBitmapSize.getWidth();
        int maxHeight = maxBitmapSize.getHeight();
        while (true) {
            if (srcWidth / scale <= maxWidth && srcHeight / scale <= maxHeight) {
                return scale;
            }
            if (powerOf2) {
                scale *= 2;
            } else {
                scale++;
            }
        }
    }

    public static int computeMinImageSampleSize(ImageSize srcSize) {
        int srcWidth = srcSize.getWidth();
        int srcHeight = srcSize.getHeight();
        return Math.max((int) Math.ceil((double) (((float) srcWidth) / ((float) maxBitmapSize.getWidth()))), (int) Math.ceil((double) (((float) srcHeight) / ((float) maxBitmapSize.getHeight()))));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static float computeImageScale(com.nostra13.universalimageloader.core.assist.ImageSize r12, com.nostra13.universalimageloader.core.assist.ImageSize r13, com.nostra13.universalimageloader.core.assist.ViewScaleType r14, boolean r15) {
        /*
        r9 = 0;
        r5 = r12.getWidth();
        r4 = r12.getHeight();
        r7 = r13.getWidth();
        r6 = r13.getHeight();
        r10 = (float) r5;
        r11 = (float) r7;
        r8 = r10 / r11;
        r10 = (float) r4;
        r11 = (float) r6;
        r2 = r10 / r11;
        r10 = com.nostra13.universalimageloader.core.assist.ViewScaleType.FIT_INSIDE;
        if (r14 == r10) goto L_0x002c;
    L_0x001d:
        r9 = com.nostra13.universalimageloader.core.assist.ViewScaleType.CROP;
        if (r14 == r9) goto L_0x0038;
    L_0x0021:
        r9 = (float) r5;
        r9 = r9 / r2;
        r1 = (int) r9;
        r0 = r6;
    L_0x0025:
        r3 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        if (r15 == 0) goto L_0x003d;
    L_0x0029:
        if (r15 != 0) goto L_0x0046;
    L_0x002b:
        return r3;
    L_0x002c:
        r10 = (r8 > r2 ? 1 : (r8 == r2 ? 0 : -1));
        if (r10 < 0) goto L_0x0031;
    L_0x0030:
        r9 = 1;
    L_0x0031:
        if (r9 == 0) goto L_0x001d;
    L_0x0033:
        r1 = r7;
        r9 = (float) r4;
        r9 = r9 / r8;
        r0 = (int) r9;
        goto L_0x0025;
    L_0x0038:
        r9 = (r8 > r2 ? 1 : (r8 == r2 ? 0 : -1));
        if (r9 >= 0) goto L_0x0021;
    L_0x003c:
        goto L_0x0033;
    L_0x003d:
        if (r1 >= r5) goto L_0x0029;
    L_0x003f:
        if (r0 >= r4) goto L_0x0029;
    L_0x0041:
        r9 = (float) r1;
        r10 = (float) r5;
        r3 = r9 / r10;
        goto L_0x002b;
    L_0x0046:
        if (r1 == r5) goto L_0x002b;
    L_0x0048:
        if (r0 != r4) goto L_0x0041;
    L_0x004a:
        goto L_0x002b;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.nostra13.universalimageloader.utils.ImageSizeUtils.computeImageScale(com.nostra13.universalimageloader.core.assist.ImageSize, com.nostra13.universalimageloader.core.assist.ImageSize, com.nostra13.universalimageloader.core.assist.ViewScaleType, boolean):float");
    }
}
