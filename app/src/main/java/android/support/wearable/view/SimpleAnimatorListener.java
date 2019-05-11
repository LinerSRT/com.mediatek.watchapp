package android.support.wearable.view;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.annotation.TargetApi;

@TargetApi(20)
public class SimpleAnimatorListener implements AnimatorListener {
    private boolean mWasCanceled;

    public void onAnimationCancel(Animator animator) {
        this.mWasCanceled = true;
    }

    public void onAnimationEnd(Animator animator) {
        if (!this.mWasCanceled) {
            onAnimationComplete(animator);
        }
    }

    public void onAnimationRepeat(Animator animator) {
    }

    public void onAnimationStart(Animator animator) {
        this.mWasCanceled = false;
    }

    public void onAnimationComplete(Animator animator) {
    }
}
