package android.support.wearable.view;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.annotation.TargetApi;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.Property;
import android.view.animation.LinearInterpolator;

@TargetApi(20)
class ProgressDrawable extends Drawable {
    private static final Property<ProgressDrawable, Integer> LEVEL = new C00891(Integer.class, "level");
    private static final TimeInterpolator mInterpolator = Gusterpolator.INSTANCE;
    private final ObjectAnimator mAnimator;
    private int mCircleBorderColor;
    private float mCircleBorderWidth;
    private final RectF mInnerCircleBounds = new RectF();
    private final Paint mPaint = new Paint();

    /* renamed from: android.support.wearable.view.ProgressDrawable$1 */
    class C00891 extends Property<ProgressDrawable, Integer> {
        C00891(Class x0, String x1) {
            super(x0, x1);
        }

        public Integer get(ProgressDrawable drawable) {
            return Integer.valueOf(drawable.getLevel());
        }

        public void set(ProgressDrawable drawable, Integer value) {
            drawable.setLevel(value.intValue());
            drawable.invalidateSelf();
        }
    }

    public ProgressDrawable() {
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStyle(Style.STROKE);
        this.mAnimator = ObjectAnimator.ofInt(this, LEVEL, new int[]{0, 10000});
        this.mAnimator.setRepeatCount(-1);
        this.mAnimator.setRepeatMode(1);
        this.mAnimator.setDuration(6000);
        this.mAnimator.setInterpolator(new LinearInterpolator());
    }

    public void setRingColor(int color) {
        this.mCircleBorderColor = color;
    }

    public void setRingWidth(float width) {
        this.mCircleBorderWidth = width;
    }

    public void startAnimation() {
        if (!this.mAnimator.isStarted()) {
            this.mAnimator.start();
        }
    }

    public void stopAnimation() {
        this.mAnimator.cancel();
    }

    public void draw(Canvas canvas) {
        boolean growing;
        float sweepAngle;
        canvas.save();
        this.mInnerCircleBounds.set(getBounds());
        this.mInnerCircleBounds.inset(this.mCircleBorderWidth / 2.0f, this.mCircleBorderWidth / 2.0f);
        this.mPaint.setStrokeWidth(this.mCircleBorderWidth);
        this.mPaint.setColor(this.mCircleBorderColor);
        int level = getLevel();
        float progress = ((float) (level - ((level / 2000) * 2000))) / 2000.0f;
        if (progress < 0.5f) {
            growing = true;
        } else {
            growing = false;
        }
        float correctionAngle = 54.0f * progress;
        if (growing) {
            sweepAngle = 306.0f * mInterpolator.getInterpolation(lerpInv(0.0f, 0.5f, progress));
        } else {
            sweepAngle = 306.0f * (1.0f - mInterpolator.getInterpolation(lerpInv(0.5f, 1.0f, progress)));
        }
        sweepAngle = Math.max(1.0f, sweepAngle);
        canvas.rotate(((((((float) level) * 1.0E-4f) * 2.0f) * 360.0f) - 0.049804688f) + correctionAngle, this.mInnerCircleBounds.centerX(), this.mInnerCircleBounds.centerY());
        canvas.drawArc(this.mInnerCircleBounds, !growing ? 306.0f - sweepAngle : 0.0f, sweepAngle, false, this.mPaint);
        canvas.restore();
    }

    public void setAlpha(int i) {
    }

    public void setColorFilter(ColorFilter colorFilter) {
    }

    public int getOpacity() {
        return -1;
    }

    protected boolean onLevelChange(int level) {
        return true;
    }

    private static float lerpInv(float a, float b, float value) {
        return a != b ? (value - a) / (b - a) : 0.0f;
    }
}
