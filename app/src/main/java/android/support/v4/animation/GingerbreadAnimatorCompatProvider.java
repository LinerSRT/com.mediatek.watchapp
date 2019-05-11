package android.support.v4.animation;

import android.annotation.TargetApi;
import android.support.annotation.RequiresApi;
import android.view.View;

@TargetApi(9)
@RequiresApi(9)
class GingerbreadAnimatorCompatProvider implements AnimatorProvider {
    GingerbreadAnimatorCompatProvider() {
    }

    public void clearInterpolator(View view) {
    }
}
