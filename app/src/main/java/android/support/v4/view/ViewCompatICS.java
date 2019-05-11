package android.support.v4.view;

import android.annotation.TargetApi;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.View.AccessibilityDelegate;

@TargetApi(14)
@RequiresApi(14)
class ViewCompatICS {
    ViewCompatICS() {
    }

    public static boolean canScrollHorizontally(View v, int direction) {
        return v.canScrollHorizontally(direction);
    }

    public static boolean canScrollVertically(View v, int direction) {
        return v.canScrollVertically(direction);
    }

    public static void setAccessibilityDelegate(View v, @Nullable Object delegate) {
        v.setAccessibilityDelegate((AccessibilityDelegate) delegate);
    }
}
