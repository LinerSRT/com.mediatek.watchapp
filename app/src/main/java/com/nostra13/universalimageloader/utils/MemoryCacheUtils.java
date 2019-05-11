package com.nostra13.universalimageloader.utils;

import com.nostra13.universalimageloader.core.assist.ImageSize;
import java.util.Comparator;

public final class MemoryCacheUtils {

    /* renamed from: com.nostra13.universalimageloader.utils.MemoryCacheUtils$1 */
    static class C01981 implements Comparator<String> {
        C01981() {
        }

        public int compare(String key1, String key2) {
            return key1.substring(0, key1.lastIndexOf("_")).compareTo(key2.substring(0, key2.lastIndexOf("_")));
        }
    }

    private MemoryCacheUtils() {
    }

    public static String generateKey(String imageUri, ImageSize targetSize) {
        return "_" + targetSize.getWidth() + "x" + targetSize.getHeight();
    }

    public static Comparator<String> createFuzzyKeyComparator() {
        return new C01981();
    }
}
