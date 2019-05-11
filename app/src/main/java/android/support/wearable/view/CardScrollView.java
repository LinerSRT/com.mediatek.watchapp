package android.support.wearable.view;

import android.annotation.TargetApi;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

@TargetApi(20)
@Deprecated
public class CardScrollView extends FrameLayout {
    private CardFrame mCardFrame;
    private final int mCardShadowWidth;
    private boolean mRoundDisplay;

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        requestApplyInsets();
    }

    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        boolean round = insets.isRound();
        if (this.mRoundDisplay != round) {
            this.mRoundDisplay = round;
            LayoutParams lp = (LayoutParams) this.mCardFrame.getLayoutParams();
            lp.leftMargin = -this.mCardShadowWidth;
            lp.rightMargin = -this.mCardShadowWidth;
            lp.bottomMargin = -this.mCardShadowWidth;
            this.mCardFrame.setLayoutParams(lp);
        }
        if (insets.getSystemWindowInsetBottom() > 0) {
            int bottomInset = insets.getSystemWindowInsetBottom();
            ViewGroup.LayoutParams lp2 = getLayoutParams();
            if (lp2 instanceof MarginLayoutParams) {
                ((MarginLayoutParams) lp2).bottomMargin = bottomInset;
            }
        }
        if (this.mRoundDisplay && this.mCardFrame != null) {
            this.mCardFrame.onApplyWindowInsets(insets);
        }
        requestLayout();
        return insets;
    }

    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() <= 0 && (child instanceof CardFrame)) {
            super.addView(child, index, params);
            this.mCardFrame = (CardFrame) child;
            return;
        }
        throw new IllegalStateException("CardScrollView may contain only a single CardFrame.");
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() == 0 || !(getChildAt(0) instanceof CardFrame)) {
            Log.w("CardScrollView", "No CardFrame has been added!");
        }
    }

    private boolean hasCardFrame() {
        if (this.mCardFrame != null) {
            return true;
        }
        Log.w("CardScrollView", "No CardFrame has been added.");
        return false;
    }

    public boolean canScrollHorizontally(int direction) {
        return false;
    }

    public int getAvailableScrollDelta(int direction) {
        if (!hasCardFrame()) {
            return 0;
        }
        LayoutParams lp = (LayoutParams) this.mCardFrame.getLayoutParams();
        int marginHeight = lp.topMargin + lp.bottomMargin;
        int cardVerticalSpan = (this.mCardFrame.getMeasuredHeight() + (getPaddingTop() + getPaddingBottom())) + marginHeight;
        if (cardVerticalSpan <= getMeasuredHeight()) {
            return 0;
        }
        int extra = cardVerticalSpan - getMeasuredHeight();
        int avail = 0;
        int sy = getScrollY();
        if (this.mCardFrame.getExpansionDirection() != 1) {
            if (this.mCardFrame.getExpansionDirection() == -1 && sy <= 0) {
                if (direction > 0) {
                    avail = -sy;
                } else if (direction < 0) {
                    avail = -(extra + sy);
                }
            }
        } else if (sy >= 0) {
            if (direction < 0) {
                avail = -sy;
            } else if (direction > 0) {
                avail = Math.max(0, extra - sy);
            }
        }
        if (Log.isLoggable("CardScrollView", 3)) {
            Log.d("CardScrollView", "getVerticalScrollableDistance: " + Math.max(0, avail));
        }
        return avail;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mCardFrame != null) {
            MarginLayoutParams lp = (MarginLayoutParams) this.mCardFrame.getLayoutParams();
            int availableHeight = (MeasureSpec.getSize(heightMeasureSpec) - (getPaddingTop() + getPaddingBottom())) - (lp.topMargin + lp.bottomMargin);
            this.mCardFrame.measure(MeasureSpec.makeMeasureSpec((MeasureSpec.getSize(widthMeasureSpec) - (getPaddingLeft() + getPaddingRight())) - (lp.leftMargin + lp.rightMargin), 1073741824), MeasureSpec.makeMeasureSpec(availableHeight, Integer.MIN_VALUE));
        }
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), getDefaultSize(getSuggestedMinimumWidth(), heightMeasureSpec));
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (this.mCardFrame != null) {
            LayoutParams lp = (LayoutParams) this.mCardFrame.getLayoutParams();
            int cardHeight = this.mCardFrame.getMeasuredHeight();
            int cardWidth = this.mCardFrame.getMeasuredWidth();
            int parentHeight = bottom - top;
            boolean alignBottom = (getPaddingTop() + cardHeight) + lp.topMargin > parentHeight ? this.mCardFrame.getExpansionDirection() == -1 : (lp.gravity & 112) == 80;
            int l = getPaddingLeft() + lp.leftMargin;
            int t = getPaddingTop() + lp.topMargin;
            int r = l + cardWidth;
            int b = t + cardHeight;
            if (alignBottom) {
                b = parentHeight - (getPaddingBottom() + lp.bottomMargin);
                t = b - cardHeight;
            }
            this.mCardFrame.layout(l, t, r, b);
        }
    }
}
