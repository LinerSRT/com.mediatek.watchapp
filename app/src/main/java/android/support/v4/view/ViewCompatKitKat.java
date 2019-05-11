package android.support.v4.view;

import android.annotation.TargetApi;
import android.support.annotation.RequiresApi;
import android.view.View;

@TargetApi(19)
@RequiresApi(19)
class ViewCompatKitKat {
    ViewCompatKitKat() {
    }

    public static boolean isAttachedToWindow(View view) {
        return view.isAttachedToWindow();
    }
}
