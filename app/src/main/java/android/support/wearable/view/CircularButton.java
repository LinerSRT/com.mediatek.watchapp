package android.support.wearable.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.wearable.R$styleable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewOutlineProvider;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;

@SuppressLint({"ClickableViewAccessibility"})
@TargetApi(21)
@Deprecated
public class CircularButton extends View {
    private static final double SQRT_2 = Math.sqrt(2.0d);
    private ColorStateList mColors;
    private int mDiameter;
    private Drawable mImage;
    private final Interpolator mInterpolator;
    private int mRippleColor;
    private RippleDrawable mRippleDrawable;
    private int mScaleMode;
    private final ShapeDrawable mShapeDrawable;

    private class CircleOutlineProvider extends ViewOutlineProvider {
        private CircleOutlineProvider() {
        }

        public void getOutline(View view, Outline outline) {
            outline.setOval(0, 0, CircularButton.this.mDiameter, CircularButton.this.mDiameter);
        }
    }

    private static int inscribedSize(int r) {
        return (int) Math.floor(((double) r) * SQRT_2);
    }

    private static int encircledRadius(int l) {
        return (int) Math.floor(((double) l) / SQRT_2);
    }

    public CircularButton(Context context) {
        this(context, null);
    }

    public CircularButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircularButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CircularButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mRippleColor = -1;
        this.mShapeDrawable = new ShapeDrawable(new OvalShape());
        this.mShapeDrawable.getPaint().setColor(-3355444);
        super.setBackgroundDrawable(this.mShapeDrawable);
        setOutlineProvider(new CircleOutlineProvider());
        this.mInterpolator = new AccelerateInterpolator(2.0f);
        this.mScaleMode = 0;
        boolean clickable = true;
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.CircularButton, defStyleAttr, defStyleRes);
        for (int i = 0; i < a.getIndexCount(); i++) {
            int attr = a.getIndex(i);
            if (attr == R$styleable.CircularButton_android_color) {
                this.mColors = a.getColorStateList(attr);
                this.mShapeDrawable.getPaint().setColor(this.mColors.getDefaultColor());
            } else if (attr == R$styleable.CircularButton_android_src) {
                this.mImage = a.getDrawable(attr);
            } else if (attr == R$styleable.CircularButton_buttonRippleColor) {
                setRippleColor(a.getColor(attr, -1));
            } else if (attr == R$styleable.CircularButton_pressedButtonTranslationZ) {
                setPressedTranslationZ(a.getDimension(attr, 0.0f));
            } else if (attr == R$styleable.CircularButton_imageScaleMode) {
                this.mScaleMode = a.getInt(attr, this.mScaleMode);
            } else if (attr == R$styleable.CircularButton_android_clickable) {
                clickable = a.getBoolean(R$styleable.CircularButton_android_clickable, clickable);
            }
        }
        a.recycle();
        setClickable(clickable);
    }

    public void setColor(ColorStateList colorStateList) {
        this.mColors = colorStateList;
        this.mShapeDrawable.getPaint().setColor(this.mColors.getDefaultColor());
    }

    public void setRippleColor(int rippleColor) {
        this.mRippleColor = rippleColor;
        if (this.mRippleDrawable != null) {
            this.mRippleDrawable.setColor(ColorStateList.valueOf(rippleColor));
        } else if (this.mRippleColor == -1 || isInEditMode()) {
            this.mRippleDrawable = null;
            super.setBackgroundDrawable(this.mShapeDrawable);
        } else {
            this.mRippleDrawable = new RippleDrawable(ColorStateList.valueOf(rippleColor), this.mShapeDrawable, this.mShapeDrawable);
            super.setBackgroundDrawable(this.mRippleDrawable);
        }
    }

    public Drawable getImageDrawable() {
        return this.mImage;
    }

    public void setImageDrawable(Drawable drawable) {
        if (this.mImage != null) {
            this.mImage.setCallback(null);
        }
        if (this.mImage != drawable) {
            this.mImage = drawable;
            requestLayout();
            invalidate();
        }
        if (this.mImage != null) {
            this.mImage.setCallback(this);
        }
    }

    public int getImageScaleMode() {
        return this.mScaleMode;
    }

    public void setImageScaleMode(int scaleMode) {
        this.mScaleMode = scaleMode;
        if (this.mImage != null) {
            invalidate();
            requestLayout();
        }
    }

    protected boolean verifyDrawable(Drawable who) {
        return this.mImage == who || super.verifyDrawable(who);
    }

    public void setBackgroundDrawable(Drawable background) {
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (this.mColors != null && this.mColors.isStateful()) {
            this.mShapeDrawable.getPaint().setColor(this.mColors.getColorForState(getDrawableState(), this.mColors.getDefaultColor()));
            this.mShapeDrawable.invalidateSelf();
        }
    }

    private int dpToPx(float dp) {
        return (int) Math.ceil((double) TypedValue.applyDimension(1, dp, getResources().getDisplayMetrics()));
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode == 1073741824 && heightMode == 1073741824) {
            this.mDiameter = Math.min(widthSize, heightSize);
        } else if (widthMode == 1073741824) {
            this.mDiameter = widthSize;
        } else if (heightMode != 1073741824) {
            int imageSize;
            if (hasIntrinsicSize(this.mImage)) {
                imageSize = Math.max(this.mImage.getIntrinsicHeight(), this.mImage.getIntrinsicWidth());
            } else {
                imageSize = dpToPx(48.0f);
            }
            if (widthMode == Integer.MIN_VALUE || heightMode == Integer.MIN_VALUE) {
                int atMost;
                if (widthMode != Integer.MIN_VALUE) {
                    atMost = heightSize;
                } else if (heightMode == Integer.MIN_VALUE) {
                    atMost = Math.min(widthSize, heightSize);
                } else {
                    atMost = widthSize;
                }
                this.mDiameter = Math.min(atMost, encircledRadius(imageSize) * 2);
            } else {
                this.mDiameter = imageSize;
            }
        } else {
            this.mDiameter = heightSize;
        }
        setMeasuredDimension(this.mDiameter, this.mDiameter);
    }

    private static boolean hasIntrinsicSize(Drawable d) {
        return d != null && d.getIntrinsicHeight() > 0 && d.getIntrinsicWidth() > 0;
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int w = r - l;
        int h = b - t;
        if (this.mImage != null) {
            int iw = this.mImage.getIntrinsicWidth();
            int ih = this.mImage.getIntrinsicHeight();
            int hpad;
            int vpad;
            if (this.mScaleMode != 0 && hasIntrinsicSize(this.mImage)) {
                hpad = (int) (((float) (w - iw)) / 2.0f);
                vpad = (int) (((float) (h - ih)) / 2.0f);
                this.mImage.setBounds(hpad, vpad, hpad + iw, vpad + ih);
                return;
            }
            int inscr = inscribedSize(this.mDiameter / 2);
            vpad = (this.mDiameter - inscr) / 2;
            hpad = vpad;
            if (hasIntrinsicSize(this.mImage)) {
                if (iw != ih) {
                    float aspect = ((float) iw) / ((float) ih);
                    if (iw <= ih) {
                        ih = inscr;
                        iw = (int) (((float) inscr) * aspect);
                        hpad = (int) (((float) (inscr - iw)) / 2.0f);
                    } else {
                        iw = inscr;
                        ih = (int) (((float) inscr) / aspect);
                        vpad = (int) (((float) (inscr - ih)) / 2.0f);
                    }
                } else {
                    ih = inscr;
                    iw = inscr;
                }
                this.mImage.setBounds(hpad, vpad, hpad + iw, vpad + ih);
                return;
            }
            this.mImage.setBounds(hpad, vpad, hpad + inscr, vpad + inscr);
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mImage != null) {
            this.mImage.draw(canvas);
        }
    }

    public void setPressedTranslationZ(float translationZ) {
        StateListAnimator stateListAnimator = new StateListAnimator();
        stateListAnimator.addState(PRESSED_ENABLED_STATE_SET, setupAnimator(ObjectAnimator.ofFloat(this, "translationZ", new float[]{translationZ})));
        stateListAnimator.addState(ENABLED_FOCUSED_STATE_SET, setupAnimator(ObjectAnimator.ofFloat(this, "translationZ", new float[]{translationZ})));
        stateListAnimator.addState(EMPTY_STATE_SET, setupAnimator(ObjectAnimator.ofFloat(this, "translationZ", new float[]{getElevation()})));
        setStateListAnimator(stateListAnimator);
    }

    private Animator setupAnimator(Animator animator) {
        animator.setInterpolator(this.mInterpolator);
        return animator;
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = super.onTouchEvent(event);
        if (handled) {
            switch (event.getAction() & 255) {
                case 0:
                    getBackground().setHotspot(event.getX(), event.getY());
                    break;
                default:
                    break;
            }
        }
        return handled;
    }
}
