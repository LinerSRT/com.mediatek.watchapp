package android.support.wearable.view;

import android.annotation.TargetApi;

@TargetApi(20)
class Func {
    Func() {
    }

    static float clamp(float value, int min, int max) {
        if (value < ((float) min)) {
            return (float) min;
        }
        if (value > ((float) max)) {
            return (float) max;
        }
        return value;
    }

    static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value <= max) {
            return value;
        }
        return max;
    }
}
