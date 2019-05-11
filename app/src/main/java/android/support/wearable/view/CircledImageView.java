package android.support.wearable.view;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.Callback;
import android.os.Build.VERSION;
import android.support.annotation.Px;
import android.support.annotation.VisibleForTesting;
import android.support.wearable.R$styleable;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;

@TargetApi(23)
public class CircledImageView extends View {
    private static final ArgbEvaluator ARGB_EVALUATOR = new ArgbEvaluator();
    private final AnimatorUpdateListener mAnimationListener;
    private Cap mCircleBorderCap;
    private int mCircleBorderColor;
    private float mCircleBorderWidth;
    private ColorStateList mCircleColor;
    private boolean mCircleHidden;
    private float mCircleRadius;
    private float mCircleRadiusPercent;
    private float mCircleRadiusPressed;
    private float mCircleRadiusPressedPercent;
    private ValueAnimator mColorAnimator;
    private long mColorChangeAnimationDurationMs;
    private int mCurrentColor;
    private Drawable mDrawable;
    private final Callback mDrawableCallback;
    private float mImageCirclePercentage;
    private float mImageHorizontalOffcenterPercentage;
    private Integer mImageTint;
    @VisibleForTesting
    final Rect mIndeterminateBounds;
    private final ProgressDrawable mIndeterminateDrawable;
    private final float mInitialCircleRadius;
    @VisibleForTesting
    final RectF mOval;
    private final Paint mPaint;
    private boolean mPressed;
    private float mProgress;
    private boolean mProgressIndeterminate;
    private float mRadiusInset;
    private final OvalShadowPainter mShadowPainter;
    private Integer mSquareDimen;
    private boolean mVisible;
    private boolean mWindowVisible;

    /* renamed from: android.support.wearable.view.CircledImageView$1 */
    class C00831 implements Callback {
        C00831() {
        }

        public void invalidateDrawable(Drawable drawable) {
            CircledImageView.this.invalidate();
        }

        public void scheduleDrawable(Drawable drawable, Runnable runnable, long l) {
        }

        public void unscheduleDrawable(Drawable drawable, Runnable runnable) {
        }
    }

    /* renamed from: android.support.wearable.view.CircledImageView$2 */
    class C00842 implements AnimatorUpdateListener {
        C00842() {
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            int color = ((Integer) animation.getAnimatedValue()).intValue();
            if (color != CircledImageView.this.mCurrentColor) {
                CircledImageView.this.mCurrentColor = color;
                CircledImageView.this.invalidate();
            }
        }
    }

    private static class OvalShadowPainter {
        private final RectF mBounds = new RectF();
        private float mInnerCircleBorderWidth;
        private float mInnerCircleRadius;
        private final int[] mShaderColors = new int[]{-16777216, 0};
        private final float[] mShaderStops = new float[]{0.6f, 1.0f};
        private final Paint mShadowPaint = new Paint();
        private float mShadowRadius;
        private float mShadowVisibility;
        private final float mShadowWidth;

        OvalShadowPainter(float shadowWidth, float shadowVisibility, float innerCircleRadius, float innerCircleBorderWidth) {
            this.mShadowWidth = shadowWidth;
            this.mShadowVisibility = shadowVisibility;
            this.mInnerCircleRadius = innerCircleRadius;
            this.mInnerCircleBorderWidth = innerCircleBorderWidth;
            this.mShadowRadius = (this.mInnerCircleRadius + this.mInnerCircleBorderWidth) + (this.mShadowWidth * this.mShadowVisibility);
            this.mShadowPaint.setColor(-16777216);
            this.mShadowPaint.setStyle(Style.FILL);
            this.mShadowPaint.setAntiAlias(true);
            updateRadialGradient();
        }

        void draw(Canvas canvas, float alpha) {
            if (this.mShadowWidth > 0.0f && this.mShadowVisibility > 0.0f) {
                this.mShadowPaint.setAlpha(Math.round(((float) this.mShadowPaint.getAlpha()) * alpha));
                canvas.drawCircle(this.mBounds.centerX(), this.mBounds.centerY(), this.mShadowRadius, this.mShadowPaint);
            }
        }

        void setBounds(@Px int left, @Px int top, @Px int right, @Px int bottom) {
            this.mBounds.set((float) left, (float) top, (float) right, (float) bottom);
            updateRadialGradient();
        }

        void setInnerCircleRadius(float newInnerCircleRadius) {
            this.mInnerCircleRadius = newInnerCircleRadius;
            updateRadialGradient();
        }

        private void updateRadialGradient() {
            this.mShadowRadius = (this.mInnerCircleRadius + this.mInnerCircleBorderWidth) + (this.mShadowWidth * this.mShadowVisibility);
            if (this.mShadowRadius > 0.0f) {
                this.mShadowPaint.setShader(new RadialGradient(this.mBounds.centerX(), this.mBounds.centerY(), this.mShadowRadius, this.mShaderColors, this.mShaderStops, TileMode.MIRROR));
            }
        }
    }

    public CircledImageView(Context context) {
        this(context, null);
    }

    public CircledImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircledImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mIndeterminateBounds = new Rect();
        this.mCircleHidden = false;
        this.mProgress = 1.0f;
        this.mPressed = false;
        this.mColorChangeAnimationDurationMs = 0;
        this.mImageCirclePercentage = 1.0f;
        this.mImageHorizontalOffcenterPercentage = 0.0f;
        this.mDrawableCallback = new C00831();
        this.mAnimationListener = new C00842();
        TypedArray a = getContext().obtainStyledAttributes(attrs, R$styleable.CircledImageView);
        this.mDrawable = a.getDrawable(R$styleable.CircledImageView_android_src);
        if (!(this.mDrawable == null || this.mDrawable.getConstantState() == null)) {
            if (VERSION.SDK_INT < 21) {
                this.mDrawable = this.mDrawable.getConstantState().newDrawable(context.getResources());
            } else {
                this.mDrawable = this.mDrawable.getConstantState().newDrawable(context.getResources(), context.getTheme());
            }
            this.mDrawable = this.mDrawable.mutate();
        }
        this.mCircleColor = a.getColorStateList(R$styleable.CircledImageView_circle_color);
        if (this.mCircleColor == null) {
            this.mCircleColor = ColorStateList.valueOf(17170432);
        }
        this.mCircleRadius = a.getDimension(R$styleable.CircledImageView_circle_radius, 0.0f);
        this.mInitialCircleRadius = this.mCircleRadius;
        this.mCircleRadiusPressed = a.getDimension(R$styleable.CircledImageView_circle_radius_pressed, this.mCircleRadius);
        this.mCircleBorderColor = a.getColor(R$styleable.CircledImageView_circle_border_color, -16777216);
        this.mCircleBorderCap = Cap.values()[a.getInt(R$styleable.CircledImageView_circle_border_cap, 0)];
        this.mCircleBorderWidth = a.getDimension(R$styleable.CircledImageView_circle_border_width, 0.0f);
        if (this.mCircleBorderWidth > 0.0f) {
            this.mRadiusInset += this.mCircleBorderWidth / 2.0f;
        }
        float circlePadding = a.getDimension(R$styleable.CircledImageView_circle_padding, 0.0f);
        if (circlePadding > 0.0f) {
            this.mRadiusInset += circlePadding;
        }
        this.mImageCirclePercentage = a.getFloat(R$styleable.CircledImageView_image_circle_percentage, 0.0f);
        this.mImageHorizontalOffcenterPercentage = a.getFloat(R$styleable.CircledImageView_image_horizontal_offcenter_percentage, 0.0f);
        if (a.hasValue(R$styleable.CircledImageView_image_tint)) {
            this.mImageTint = Integer.valueOf(a.getColor(R$styleable.CircledImageView_image_tint, 0));
        }
        if (a.hasValue(R$styleable.CircledImageView_square_dimen)) {
            this.mSquareDimen = Integer.valueOf(a.getInt(R$styleable.CircledImageView_square_dimen, 0));
        }
        this.mCircleRadiusPercent = a.getFraction(R$styleable.CircledImageView_circle_radius_percent, 1, 1, 0.0f);
        this.mCircleRadiusPressedPercent = a.getFraction(R$styleable.CircledImageView_circle_radius_pressed_percent, 1, 1, this.mCircleRadiusPercent);
        float shadowWidth = a.getDimension(R$styleable.CircledImageView_shadow_width, 0.0f);
        a.recycle();
        this.mOval = new RectF();
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mShadowPainter = new OvalShadowPainter(shadowWidth, 0.0f, getCircleRadius(), this.mCircleBorderWidth);
        this.mIndeterminateDrawable = new ProgressDrawable();
        this.mIndeterminateDrawable.setCallback(this.mDrawableCallback);
        setWillNotDraw(false);
        setColorForCurrentState();
    }

    protected boolean onSetAlpha(int alpha) {
        return true;
    }

    protected void onDraw(Canvas canvas) {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        float circleRadius = !this.mPressed ? getCircleRadius() : getCircleRadiusPressed();
        this.mShadowPainter.draw(canvas, getAlpha());
        this.mOval.set((float) paddingLeft, (float) paddingTop, (float) (getWidth() - getPaddingRight()), (float) (getHeight() - getPaddingBottom()));
        this.mOval.set(this.mOval.centerX() - circleRadius, this.mOval.centerY() - circleRadius, this.mOval.centerX() + circleRadius, this.mOval.centerY() + circleRadius);
        if (this.mCircleBorderWidth > 0.0f) {
            this.mPaint.setColor(this.mCircleBorderColor);
            this.mPaint.setAlpha(Math.round(((float) this.mPaint.getAlpha()) * getAlpha()));
            this.mPaint.setStyle(Style.STROKE);
            this.mPaint.setStrokeWidth(this.mCircleBorderWidth);
            this.mPaint.setStrokeCap(this.mCircleBorderCap);
            if (this.mProgressIndeterminate) {
                this.mOval.roundOut(this.mIndeterminateBounds);
                this.mIndeterminateBounds.inset((int) ((-this.mCircleBorderWidth) / 2.0f), (int) ((-this.mCircleBorderWidth) / 2.0f));
                this.mIndeterminateDrawable.setBounds(this.mIndeterminateBounds);
                this.mIndeterminateDrawable.setRingColor(this.mCircleBorderColor);
                this.mIndeterminateDrawable.setRingWidth(this.mCircleBorderWidth);
                this.mIndeterminateDrawable.draw(canvas);
            } else {
                canvas.drawArc(this.mOval, -90.0f, 360.0f * this.mProgress, false, this.mPaint);
            }
        }
        if (!this.mCircleHidden) {
            this.mPaint.setColor(this.mCurrentColor);
            this.mPaint.setAlpha(Math.round(((float) this.mPaint.getAlpha()) * getAlpha()));
            this.mPaint.setStyle(Style.FILL);
            canvas.drawCircle(this.mOval.centerX(), this.mOval.centerY(), circleRadius, this.mPaint);
        }
        if (this.mDrawable != null) {
            this.mDrawable.setAlpha(Math.round(getAlpha() * 255.0f));
            if (this.mImageTint != null) {
                this.mDrawable.setTint(this.mImageTint.intValue());
            }
            this.mDrawable.draw(canvas);
        }
        super.onDraw(canvas);
    }

    private void setColorForCurrentState() {
        int newColor = this.mCircleColor.getColorForState(getDrawableState(), this.mCircleColor.getDefaultColor());
        if ((this.mColorChangeAnimationDurationMs <= 0 ? 1 : 0) == 0) {
            if (this.mColorAnimator == null) {
                this.mColorAnimator = new ValueAnimator();
            } else {
                this.mColorAnimator.cancel();
            }
            this.mColorAnimator.setIntValues(new int[]{this.mCurrentColor, newColor});
            this.mColorAnimator.setEvaluator(ARGB_EVALUATOR);
            this.mColorAnimator.setDuration(this.mColorChangeAnimationDurationMs);
            this.mColorAnimator.addUpdateListener(this.mAnimationListener);
            this.mColorAnimator.start();
        } else if (newColor != this.mCurrentColor) {
            this.mCurrentColor = newColor;
            invalidate();
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width;
        int height;
        float radius = (getCircleRadius() + this.mCircleBorderWidth) + (this.mShadowPainter.mShadowWidth * this.mShadowPainter.mShadowVisibility);
        float desiredWidth = radius * 2.0f;
        float desiredHeight = radius * 2.0f;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode == 1073741824) {
            width = widthSize;
        } else if (widthMode != Integer.MIN_VALUE) {
            width = (int) desiredWidth;
        } else {
            width = (int) Math.min(desiredWidth, (float) widthSize);
        }
        if (heightMode == 1073741824) {
            height = heightSize;
        } else if (heightMode != Integer.MIN_VALUE) {
            height = (int) desiredHeight;
        } else {
            height = (int) Math.min(desiredHeight, (float) heightSize);
        }
        if (this.mSquareDimen != null) {
            switch (this.mSquareDimen.intValue()) {
                case 1:
                    width = height;
                    break;
                case 2:
                    height = width;
                    break;
                default:
                    break;
            }
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, 1073741824), MeasureSpec.makeMeasureSpec(height, 1073741824));
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (this.mDrawable != null) {
            float imageCirclePercentage;
            float f;
            float f2;
            int nativeDrawableWidth = this.mDrawable.getIntrinsicWidth();
            int nativeDrawableHeight = this.mDrawable.getIntrinsicHeight();
            int viewWidth = getMeasuredWidth();
            int viewHeight = getMeasuredHeight();
            if (this.mImageCirclePercentage > 0.0f) {
                imageCirclePercentage = this.mImageCirclePercentage;
            } else {
                imageCirclePercentage = 1.0f;
            }
            if (((float) nativeDrawableWidth) != 0.0f) {
                f = (((float) viewWidth) * imageCirclePercentage) / ((float) nativeDrawableWidth);
            } else {
                f = 1.0f;
            }
            if (((float) nativeDrawableHeight) != 0.0f) {
                f2 = (((float) viewHeight) * imageCirclePercentage) / ((float) nativeDrawableHeight);
            } else {
                f2 = 1.0f;
            }
            float scaleFactor = Math.min(1.0f, Math.min(f, f2));
            int drawableWidth = Math.round(((float) nativeDrawableWidth) * scaleFactor);
            int drawableHeight = Math.round(((float) nativeDrawableHeight) * scaleFactor);
            int drawableLeft = ((viewWidth - drawableWidth) / 2) + Math.round(this.mImageHorizontalOffcenterPercentage * ((float) drawableWidth));
            int drawableTop = (viewHeight - drawableHeight) / 2;
            this.mDrawable.setBounds(drawableLeft, drawableTop, drawableLeft + drawableWidth, drawableTop + drawableHeight);
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    public void setImageDrawable(Drawable drawable) {
        boolean skipLayout = false;
        if (drawable != this.mDrawable) {
            Drawable existingDrawable = this.mDrawable;
            this.mDrawable = drawable;
            if (!(this.mDrawable == null || this.mDrawable.getConstantState() == null)) {
                this.mDrawable = this.mDrawable.getConstantState().newDrawable(getResources(), getContext().getTheme()).mutate();
            }
            if (drawable != null && existingDrawable != null && existingDrawable.getIntrinsicHeight() == drawable.getIntrinsicHeight() && existingDrawable.getIntrinsicWidth() == drawable.getIntrinsicWidth()) {
                skipLayout = true;
            }
            if (skipLayout) {
                this.mDrawable.setBounds(existingDrawable.getBounds());
            } else {
                requestLayout();
            }
            invalidate();
        }
    }

    public float getCircleRadius() {
        float radius = this.mCircleRadius;
        if (this.mCircleRadius <= 0.0f && this.mCircleRadiusPercent > 0.0f) {
            radius = ((float) Math.max(getMeasuredHeight(), getMeasuredWidth())) * this.mCircleRadiusPercent;
        }
        return radius - this.mRadiusInset;
    }

    public float getCircleRadiusPressed() {
        float radius = this.mCircleRadiusPressed;
        if (this.mCircleRadiusPressed <= 0.0f && this.mCircleRadiusPressedPercent > 0.0f) {
            radius = ((float) Math.max(getMeasuredHeight(), getMeasuredWidth())) * this.mCircleRadiusPressedPercent;
        }
        return radius - this.mRadiusInset;
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        setColorForCurrentState();
    }

    public void showIndeterminateProgress(boolean show) {
        this.mProgressIndeterminate = show;
        if (this.mIndeterminateDrawable != null) {
            if (show && this.mVisible && this.mWindowVisible) {
                this.mIndeterminateDrawable.startAnimation();
            } else {
                this.mIndeterminateDrawable.stopAnimation();
            }
        }
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        boolean z = false;
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == 0) {
            z = true;
        }
        this.mVisible = z;
        showIndeterminateProgress(this.mProgressIndeterminate);
    }

    protected void onWindowVisibilityChanged(int visibility) {
        boolean z = false;
        super.onWindowVisibilityChanged(visibility);
        if (visibility == 0) {
            z = true;
        }
        this.mWindowVisible = z;
        showIndeterminateProgress(this.mProgressIndeterminate);
    }

    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        if (pressed != this.mPressed) {
            this.mPressed = pressed;
            this.mShadowPainter.setInnerCircleRadius(!this.mPressed ? getCircleRadius() : getCircleRadiusPressed());
            invalidate();
        }
    }

    public void setPadding(@Px int left, @Px int top, @Px int right, @Px int bottom) {
        if (left != getPaddingLeft() || top != getPaddingTop() || right != getPaddingRight() || bottom != getPaddingBottom()) {
            this.mShadowPainter.setBounds(left, top, getWidth() - right, getHeight() - bottom);
        }
        super.setPadding(left, top, right, bottom);
    }

    public void onSizeChanged(int newWidth, int newHeight, int oldWidth, int oldHeight) {
        if (newWidth != oldWidth || newHeight != oldHeight) {
            this.mShadowPainter.setBounds(getPaddingLeft(), getPaddingTop(), newWidth - getPaddingRight(), newHeight - getPaddingBottom());
        }
    }
}
