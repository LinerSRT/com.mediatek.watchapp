package android.support.v4.view;

import android.annotation.TargetApi;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.View.OnApplyWindowInsetsListener;
import android.view.WindowInsets;

@TargetApi(21)
@RequiresApi(21)
class ViewCompatLollipop {

    public interface OnApplyWindowInsetsListenerBridge {
        Object onApplyWindowInsets(View view, Object obj);
    }

    ViewCompatLollipop() {
    }

    public static String getTransitionName(View view) {
        return view.getTransitionName();
    }

    public static void setOnApplyWindowInsetsListener(View view, final OnApplyWindowInsetsListenerBridge bridge) {
        if (bridge != null) {
            view.setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener() {
                public WindowInsets onApplyWindowInsets(View view, WindowInsets insets) {
                    return (WindowInsets) bridge.onApplyWindowInsets(view, insets);
                }
            });
        } else {
            view.setOnApplyWindowInsetsListener(null);
        }
    }

    public static Object onApplyWindowInsets(View v, Object insets) {
        WindowInsets unwrapped = (WindowInsets) insets;
        WindowInsets result = v.onApplyWindowInsets(unwrapped);
        if (result == unwrapped) {
            return insets;
        }
        return new WindowInsets(result);
    }

    public static Object dispatchApplyWindowInsets(View v, Object insets) {
        WindowInsets unwrapped = (WindowInsets) insets;
        WindowInsets result = v.dispatchApplyWindowInsets(unwrapped);
        if (result == unwrapped) {
            return insets;
        }
        return new WindowInsets(result);
    }

    public static void stopNestedScroll(View view) {
        view.stopNestedScroll();
    }
}
