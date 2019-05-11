package android.support.v4.view;

import android.annotation.TargetApi;
import android.support.annotation.RequiresApi;
import android.view.WindowInsets;

@TargetApi(21)
@RequiresApi(21)
class WindowInsetsCompatApi21 {
    WindowInsetsCompatApi21() {
    }

    public static boolean isConsumed(Object insets) {
        return ((WindowInsets) insets).isConsumed();
    }
}
