package android.support.wearable.view;

import android.annotation.TargetApi;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowInsets;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

@TargetApi(20)
@Deprecated
public class CardFrame extends ViewGroup {
    private int mBoxInset;
    private boolean mCanExpand;
    private int mCardBaseHeight;
    private final Rect mChildClipBounds;
    private final Rect mContentPadding;
    private final EdgeFade mEdgeFade;
    private final int mEdgeFadeDistance;
    private int mExpansionDirection;
    private boolean mExpansionEnabled;
    private float mExpansionFactor;
    private boolean mHasBottomInset;
    private final Rect mInsetPadding;
    private boolean mRoundDisplay;

    private static class EdgeFade {
        private final Matrix matrix = new Matrix();
        private final Paint paint = new Paint();
        private final Shader shader = new LinearGradient(0.0f, 0.0f, 0.0f, 1.0f, -16777216, 0, TileMode.CLAMP);

        public EdgeFade() {
            this.paint.setShader(this.shader);
            this.paint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
        }
    }

    public int getExpansionDirection() {
        return this.mExpansionDirection;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        requestApplyInsets();
    }

    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        boolean inset = false;
        boolean round = insets.isRound();
        if (round != this.mRoundDisplay) {
            this.mRoundDisplay = round;
            requestLayout();
        }
        if (insets.getSystemWindowInsetBottom() > 0) {
            inset = true;
        }
        if (inset != this.mHasBottomInset) {
            this.mHasBottomInset = inset;
            requestLayout();
        }
        return insets.consumeSystemWindowInsets();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int logicalWidth = MeasureSpec.getSize(widthMeasureSpec);
        int logicalHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (this.mRoundDisplay) {
            MarginLayoutParams lp = (MarginLayoutParams) getLayoutParams();
            this.mInsetPadding.setEmpty();
            int outsetLeft = 0;
            int outsetBottom = 0;
            int outsetRight = 0;
            if (lp.leftMargin < 0) {
                outsetLeft = -lp.leftMargin;
                logicalWidth -= outsetLeft;
            }
            if (lp.rightMargin < 0) {
                outsetRight = -lp.rightMargin;
                logicalWidth -= outsetRight;
            }
            if (lp.bottomMargin < 0) {
                outsetBottom = -lp.bottomMargin;
                logicalHeight -= outsetBottom;
            }
            this.mBoxInset = (int) (((float) Math.max(logicalWidth, logicalHeight)) * 0.146467f);
            this.mInsetPadding.left = this.mBoxInset - (getPaddingLeft() - outsetLeft);
            this.mInsetPadding.right = this.mBoxInset - (getPaddingRight() - outsetRight);
            if (!this.mHasBottomInset) {
                this.mInsetPadding.bottom = this.mBoxInset - (getPaddingBottom() - outsetBottom);
            }
        }
        int cardMeasuredWidth = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec, true);
        int cardMeasuredHeight = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec, false);
        if (getChildCount() != 0) {
            int childHeightMeasureSpecMode;
            int childHeightMeasureSpecSize;
            View content = getChildAt(0);
            int parentHeightSize = MeasureSpec.getSize(heightMeasureSpec);
            int parentHeightMode = MeasureSpec.getMode(heightMeasureSpec);
            int childWidthMeasureSpecSize = cardMeasuredWidth;
            boolean cardHeightMatchContent = false;
            this.mCanExpand = this.mExpansionEnabled;
            if (parentHeightMode == 0 || parentHeightSize == 0) {
                Log.w("CardFrame", "height measure spec passed with mode UNSPECIFIED, or zero height.");
                this.mCanExpand = false;
                this.mCardBaseHeight = 0;
                cardMeasuredHeight = 0;
                cardHeightMatchContent = true;
                childHeightMeasureSpecMode = 0;
                childHeightMeasureSpecSize = 0;
            } else if (parentHeightMode != 1073741824) {
                this.mCardBaseHeight = parentHeightSize;
                cardMeasuredHeight = this.mCardBaseHeight;
                if (this.mCanExpand) {
                    cardMeasuredHeight = (int) (((float) cardMeasuredHeight) * this.mExpansionFactor);
                }
                if (this.mExpansionDirection != -1) {
                    childHeightMeasureSpecMode = Integer.MIN_VALUE;
                    childHeightMeasureSpecSize = cardMeasuredHeight + getPaddingBottom();
                } else {
                    childHeightMeasureSpecMode = 0;
                    childHeightMeasureSpecSize = 0;
                }
            } else {
                Log.w("CardFrame", "height measure spec passed with mode EXACT");
                this.mCanExpand = false;
                this.mCardBaseHeight = parentHeightSize;
                cardMeasuredHeight = this.mCardBaseHeight;
                childHeightMeasureSpecMode = 1073741824;
                childHeightMeasureSpecSize = cardMeasuredHeight;
            }
            int paddingHeight = ((((getPaddingTop() + getPaddingBottom()) + this.mContentPadding.top) + this.mContentPadding.bottom) + this.mInsetPadding.top) + this.mInsetPadding.bottom;
            int childWidthSpec = MeasureSpec.makeMeasureSpec(cardMeasuredWidth - (((((getPaddingLeft() + getPaddingRight()) + this.mContentPadding.left) + this.mContentPadding.right) + this.mInsetPadding.left) + this.mInsetPadding.right), 1073741824);
            content.measure(getChildMeasureSpec(childWidthSpec, 0, content.getLayoutParams().width), MeasureSpec.makeMeasureSpec(childHeightMeasureSpecSize - paddingHeight, childHeightMeasureSpecMode));
            if (cardHeightMatchContent) {
                cardMeasuredHeight = content.getMeasuredHeight() + paddingHeight;
            } else {
                int i;
                cardMeasuredHeight = Math.min(cardMeasuredHeight, content.getMeasuredHeight() + paddingHeight);
                boolean z = this.mCanExpand;
                if (content.getMeasuredHeight() <= cardMeasuredHeight - paddingHeight) {
                    i = 0;
                } else {
                    i = 1;
                }
                this.mCanExpand = i & z;
            }
            setMeasuredDimension(cardMeasuredWidth, cardMeasuredHeight);
            return;
        }
        setMeasuredDimension(cardMeasuredWidth, cardMeasuredHeight);
    }

    public static int getDefaultSize(int size, int measureSpec, boolean greedy) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case Integer.MIN_VALUE:
                return !greedy ? size : specSize;
            case 0:
                return size;
            case 1073741824:
                return specSize;
            default:
                return result;
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (getChildCount() != 0) {
            int t;
            int b;
            View content = getChildAt(0);
            int parentHeight = bottom - top;
            int l = (getPaddingLeft() + this.mInsetPadding.left) + this.mContentPadding.left;
            int r = l + content.getMeasuredWidth();
            if (this.mExpansionDirection != -1) {
                t = (getPaddingTop() + this.mInsetPadding.top) + this.mContentPadding.top;
                b = t + content.getMeasuredHeight();
            } else {
                b = parentHeight;
                t = parentHeight - (((content.getMeasuredHeight() + getPaddingBottom()) + this.mInsetPadding.bottom) + this.mContentPadding.bottom);
            }
            content.layout(l, t, r, b);
        }
    }

    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        int fadeDistance = this.mEdgeFadeDistance;
        boolean bottomFade = false;
        boolean topFade = false;
        this.mChildClipBounds.set(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
        int paddingHeight = getPaddingTop() + getPaddingBottom();
        int contentHeight = child.getHeight();
        if (this.mCanExpand) {
            if (this.mExpansionDirection == -1 && contentHeight + paddingHeight > getHeight()) {
                topFade = true;
                this.mChildClipBounds.top = getPaddingTop();
            } else if (this.mExpansionDirection == 1 && contentHeight + paddingHeight > getHeight()) {
                bottomFade = true;
                this.mChildClipBounds.bottom = getHeight() - getPaddingBottom();
            }
        }
        int saveCount = canvas.getSaveCount();
        canvas.clipRect(this.mChildClipBounds);
        if (topFade) {
            canvas.saveLayer((float) this.mChildClipBounds.left, (float) this.mChildClipBounds.top, (float) this.mChildClipBounds.right, (float) (this.mChildClipBounds.top + fadeDistance), null, 4);
        }
        if (bottomFade) {
            canvas.saveLayer((float) this.mChildClipBounds.left, (float) (this.mChildClipBounds.bottom - fadeDistance), (float) this.mChildClipBounds.right, (float) this.mChildClipBounds.bottom, null, 4);
        }
        boolean more = super.drawChild(canvas, child, drawingTime);
        if (topFade) {
            this.mEdgeFade.matrix.reset();
            this.mEdgeFade.matrix.setScale(1.0f, (float) fadeDistance);
            this.mEdgeFade.matrix.postTranslate((float) this.mChildClipBounds.left, (float) this.mChildClipBounds.top);
            this.mEdgeFade.shader.setLocalMatrix(this.mEdgeFade.matrix);
            this.mEdgeFade.paint.setShader(this.mEdgeFade.shader);
            canvas.drawRect((float) this.mChildClipBounds.left, (float) this.mChildClipBounds.top, (float) this.mChildClipBounds.right, (float) (this.mChildClipBounds.top + fadeDistance), this.mEdgeFade.paint);
        }
        if (bottomFade) {
            this.mEdgeFade.matrix.reset();
            this.mEdgeFade.matrix.setScale(1.0f, (float) fadeDistance);
            this.mEdgeFade.matrix.postRotate(180.0f);
            this.mEdgeFade.matrix.postTranslate((float) this.mChildClipBounds.left, (float) this.mChildClipBounds.bottom);
            this.mEdgeFade.shader.setLocalMatrix(this.mEdgeFade.matrix);
            this.mEdgeFade.paint.setShader(this.mEdgeFade.shader);
            canvas.drawRect((float) this.mChildClipBounds.left, (float) (this.mChildClipBounds.bottom - fadeDistance), (float) this.mChildClipBounds.right, (float) this.mChildClipBounds.bottom, this.mEdgeFade.paint);
        }
        canvas.restoreToCount(saveCount);
        return more;
    }

    public void addView(View child) {
        if (getChildCount() <= 0) {
            super.addView(child);
            return;
        }
        throw new IllegalStateException("CardFrame can host only one direct child");
    }

    public void addView(View child, int index) {
        if (getChildCount() <= 0) {
            super.addView(child, index);
            return;
        }
        throw new IllegalStateException("CardFrame can host only one direct child");
    }

    public void addView(View child, LayoutParams params) {
        if (getChildCount() <= 0) {
            super.addView(child, params);
            return;
        }
        throw new IllegalStateException("CardFrame can host only one direct child");
    }

    public void addView(View child, int index, LayoutParams params) {
        if (getChildCount() <= 0) {
            super.addView(child, index, params);
            return;
        }
        throw new IllegalStateException("CardFrame can host only one direct child");
    }

    public boolean shouldDelayChildPressedState() {
        return false;
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(CardFrame.class.getName());
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(CardFrame.class.getName());
    }
}
