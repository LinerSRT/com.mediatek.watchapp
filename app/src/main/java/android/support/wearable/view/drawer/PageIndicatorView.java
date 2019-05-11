package android.support.wearable.view.drawer;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RadialGradient;
import android.graphics.Shader.TileMode;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.wearable.R$style;
import android.support.wearable.R$styleable;
import android.support.wearable.view.SimpleAnimatorListener;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;

@TargetApi(23)
public class PageIndicatorView extends View implements OnPageChangeListener {
    private int mCurrentViewPagerState;
    private int mDotColor;
    private int mDotColorSelected;
    private int mDotFadeInDuration;
    private int mDotFadeOutDelay;
    private int mDotFadeOutDuration;
    private boolean mDotFadeWhenIdle;
    private final Paint mDotPaint;
    private final Paint mDotPaintSelected;
    private final Paint mDotPaintShadow;
    private final Paint mDotPaintShadowSelected;
    private float mDotRadius;
    private float mDotRadiusSelected;
    private int mDotShadowColor;
    private float mDotShadowDx;
    private float mDotShadowDy;
    private float mDotShadowRadius;
    private int mDotSpacing;
    private int mNumberOfPositions;
    private int mSelectedPosition;
    private boolean mVisible;

    /* renamed from: android.support.wearable.view.drawer.PageIndicatorView$1 */
    class C00911 extends SimpleAnimatorListener {
        C00911() {
        }

        public void onAnimationComplete(Animator animator) {
            PageIndicatorView.this.mVisible = false;
            PageIndicatorView.this.animate().alpha(0.0f).setListener(null).setStartDelay((long) PageIndicatorView.this.mDotFadeOutDelay).setDuration((long) PageIndicatorView.this.mDotFadeOutDuration).start();
        }
    }

    public PageIndicatorView(Context context) {
        this(context, null);
    }

    public PageIndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R$styleable.PageIndicatorView, defStyleAttr, R$style.PageIndicatorViewStyle);
        this.mDotSpacing = a.getDimensionPixelOffset(R$styleable.PageIndicatorView_pageIndicatorDotSpacing, 0);
        this.mDotRadius = a.getDimension(R$styleable.PageIndicatorView_pageIndicatorDotRadius, 0.0f);
        this.mDotRadiusSelected = a.getDimension(R$styleable.PageIndicatorView_pageIndicatorDotRadiusSelected, 0.0f);
        this.mDotColor = a.getColor(R$styleable.PageIndicatorView_pageIndicatorDotColor, 0);
        this.mDotColorSelected = a.getColor(R$styleable.PageIndicatorView_pageIndicatorDotColorSelected, 0);
        this.mDotFadeOutDelay = a.getInt(R$styleable.PageIndicatorView_pageIndicatorDotFadeOutDelay, 0);
        this.mDotFadeOutDuration = a.getInt(R$styleable.PageIndicatorView_pageIndicatorDotFadeOutDuration, 0);
        this.mDotFadeInDuration = a.getInt(R$styleable.PageIndicatorView_pageIndicatorDotFadeInDuration, 0);
        this.mDotFadeWhenIdle = a.getBoolean(R$styleable.PageIndicatorView_pageIndicatorDotFadeWhenIdle, false);
        this.mDotShadowDx = a.getDimension(R$styleable.PageIndicatorView_pageIndicatorDotShadowDx, 0.0f);
        this.mDotShadowDy = a.getDimension(R$styleable.PageIndicatorView_pageIndicatorDotShadowDy, 0.0f);
        this.mDotShadowRadius = a.getDimension(R$styleable.PageIndicatorView_pageIndicatorDotShadowRadius, 0.0f);
        this.mDotShadowColor = a.getColor(R$styleable.PageIndicatorView_pageIndicatorDotShadowColor, 0);
        a.recycle();
        this.mDotPaint = new Paint(1);
        this.mDotPaint.setColor(this.mDotColor);
        this.mDotPaint.setStyle(Style.FILL);
        this.mDotPaintSelected = new Paint(1);
        this.mDotPaintSelected.setColor(this.mDotColorSelected);
        this.mDotPaintSelected.setStyle(Style.FILL);
        this.mDotPaintShadow = new Paint(1);
        this.mDotPaintShadowSelected = new Paint(1);
        this.mCurrentViewPagerState = 0;
        if (isInEditMode()) {
            this.mNumberOfPositions = 5;
            this.mSelectedPosition = 2;
            this.mDotFadeWhenIdle = false;
        }
        if (this.mDotFadeWhenIdle) {
            this.mVisible = false;
            animate().alpha(0.0f).setStartDelay(2000).setDuration((long) this.mDotFadeOutDuration).start();
        } else {
            animate().cancel();
            setAlpha(1.0f);
        }
        updateShadows();
    }

    private void updateShadows() {
        updateDotPaint(this.mDotPaint, this.mDotPaintShadow, this.mDotRadius, this.mDotShadowRadius, this.mDotColor, this.mDotShadowColor);
        updateDotPaint(this.mDotPaintSelected, this.mDotPaintShadowSelected, this.mDotRadiusSelected, this.mDotShadowRadius, this.mDotColorSelected, this.mDotShadowColor);
    }

    private void updateDotPaint(Paint dotPaint, Paint shadowPaint, float baseRadius, float shadowRadius, int color, int shadowColor) {
        float radius = baseRadius + shadowRadius;
        float shadowStart = baseRadius / radius;
        float f = 0.0f;
        shadowPaint.setShader(new RadialGradient(0.0f, f, radius, new int[]{shadowColor, shadowColor, 0}, new float[]{0.0f, shadowStart, 1.0f}, TileMode.CLAMP));
        dotPaint.setColor(color);
        dotPaint.setStyle(Style.FILL);
    }

    private void positionChanged(int position) {
        this.mSelectedPosition = position;
        invalidate();
    }

    private void fadeIn() {
        this.mVisible = true;
        animate().cancel();
        animate().alpha(1.0f).setStartDelay(0).setDuration((long) this.mDotFadeInDuration).start();
    }

    private void fadeOut(long delayMillis) {
        this.mVisible = false;
        animate().cancel();
        animate().alpha(0.0f).setStartDelay(delayMillis).setDuration((long) this.mDotFadeOutDuration).start();
    }

    private void fadeInOut() {
        this.mVisible = true;
        animate().cancel();
        animate().alpha(1.0f).setStartDelay(0).setDuration((long) this.mDotFadeInDuration).setListener(new C00911()).start();
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (!this.mDotFadeWhenIdle || this.mCurrentViewPagerState != 1) {
            return;
        }
        if (positionOffset != 0.0f) {
            if (!this.mVisible) {
                fadeIn();
            }
        } else if (this.mVisible) {
            fadeOut(0);
        }
    }

    public void onPageSelected(int position) {
        if (position != this.mSelectedPosition) {
            positionChanged(position);
        }
    }

    public void onPageScrollStateChanged(int state) {
        if (this.mCurrentViewPagerState != state) {
            this.mCurrentViewPagerState = state;
            if (!this.mDotFadeWhenIdle || state != 0) {
                return;
            }
            if (this.mVisible) {
                fadeOut((long) this.mDotFadeOutDelay);
            } else {
                fadeInOut();
            }
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int totalWidth;
        int totalHeight;
        if (MeasureSpec.getMode(widthMeasureSpec) != 1073741824) {
            totalWidth = (getPaddingLeft() + (this.mNumberOfPositions * this.mDotSpacing)) + getPaddingRight();
        } else {
            totalWidth = MeasureSpec.getSize(widthMeasureSpec);
        }
        if (MeasureSpec.getMode(heightMeasureSpec) != 1073741824) {
            totalHeight = (getPaddingTop() + ((int) (((float) ((int) Math.ceil((double) (2.0f * Math.max(this.mDotRadius + this.mDotShadowRadius, this.mDotRadiusSelected + this.mDotShadowRadius))))) + this.mDotShadowDy))) + getPaddingBottom();
        } else {
            totalHeight = MeasureSpec.getSize(heightMeasureSpec);
        }
        setMeasuredDimension(resolveSizeAndState(totalWidth, widthMeasureSpec, 0), resolveSizeAndState(totalHeight, heightMeasureSpec, 0));
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mNumberOfPositions > 1) {
            float dotCenterLeft = ((float) getPaddingLeft()) + (((float) this.mDotSpacing) / 2.0f);
            float dotCenterTop = ((float) getHeight()) / 2.0f;
            canvas.save();
            canvas.translate(dotCenterLeft, dotCenterTop);
            for (int i = 0; i < this.mNumberOfPositions; i++) {
                if (i != this.mSelectedPosition) {
                    canvas.drawCircle(this.mDotShadowDx, this.mDotShadowDy, this.mDotRadius + this.mDotShadowRadius, this.mDotPaintShadow);
                    canvas.drawCircle(0.0f, 0.0f, this.mDotRadius, this.mDotPaint);
                } else {
                    canvas.drawCircle(this.mDotShadowDx, this.mDotShadowDy, this.mDotRadiusSelected + this.mDotShadowRadius, this.mDotPaintShadowSelected);
                    canvas.drawCircle(0.0f, 0.0f, this.mDotRadiusSelected, this.mDotPaintSelected);
                }
                canvas.translate((float) this.mDotSpacing, 0.0f);
            }
            canvas.restore();
        }
    }
}
