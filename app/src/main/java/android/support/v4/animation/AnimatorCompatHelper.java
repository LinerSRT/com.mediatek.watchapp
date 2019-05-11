package android.support.v4.animation;

import android.os.Build.VERSION;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.view.View;

@RestrictTo({Scope.LIBRARY_GROUP})
public final class AnimatorCompatHelper {
    private static final AnimatorProvider IMPL;

    static {
        if (VERSION.SDK_INT < 12) {
            IMPL = new GingerbreadAnimatorCompatProvider();
        } else {
            IMPL = new HoneycombMr1AnimatorCompatProvider();
        }
    }

    private AnimatorCompatHelper() {
    }

    public static void clearInterpolator(View view) {
        IMPL.clearInterpolator(view);
    }
}
