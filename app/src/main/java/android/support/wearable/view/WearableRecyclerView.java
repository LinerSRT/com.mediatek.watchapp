package android.support.wearable.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.Recycler;
import android.support.v7.widget.RecyclerView.State;
import android.support.wearable.R$styleable;
import android.support.wearable.input.RotaryEncoder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;

@TargetApi(23)
public class WearableRecyclerView extends RecyclerView {
    private static final String TAG = WearableRecyclerView.class.getSimpleName();
    private boolean mCenterEdgeItems;
    private boolean mCenterEdgeItemsWhenThereAreChildren;
    private boolean mCircularScrollingEnabled;
    private OffsettingHelper mOffsettingHelper;
    private int mOriginalPaddingBottom;
    private int mOriginalPaddingTop;
    private final OnPreDrawListener mPaddingPreDrawListener;
    private final ScrollManager mScrollManager;

    public static abstract class ChildLayoutManager extends LinearLayoutManager {
        public abstract void updateChild(View view, WearableRecyclerView wearableRecyclerView);

        public ChildLayoutManager(Context context) {
            super(context, 1, false);
        }

        public int scrollVerticallyBy(int dy, Recycler recycler, State state) {
            int scrolled = super.scrollVerticallyBy(dy, recycler, state);
            updateLayout();
            return scrolled;
        }

        public void onLayoutChildren(Recycler recycler, State state) {
            super.onLayoutChildren(recycler, state);
            if (getChildCount() != 0) {
                updateLayout();
            }
        }

        private void updateLayout() {
            for (int count = 0; count < getChildCount(); count++) {
                View child = getChildAt(count);
                updateChild(child, (WearableRecyclerView) child.getParent());
            }
        }
    }

    /* renamed from: android.support.wearable.view.WearableRecyclerView$1 */
    class C00901 implements OnPreDrawListener {
        C00901() {
        }

        public boolean onPreDraw() {
            if (WearableRecyclerView.this.mCenterEdgeItemsWhenThereAreChildren && WearableRecyclerView.this.getChildCount() > 0) {
                WearableRecyclerView.this.setupCenteredPadding();
                WearableRecyclerView.this.mCenterEdgeItemsWhenThereAreChildren = false;
            }
            return true;
        }
    }

    @Deprecated
    public static abstract class OffsettingHelper {
        public abstract void updateChild(View view, WearableRecyclerView wearableRecyclerView);
    }

    @Deprecated
    private final class OffsettingLinearLayoutManager extends LinearLayoutManager {
        public OffsettingLinearLayoutManager(Context context) {
            super(context, 1, false);
        }

        public int scrollVerticallyBy(int dy, Recycler recycler, State state) {
            int scrolled = super.scrollVerticallyBy(dy, recycler, state);
            updateLayout();
            return scrolled;
        }

        public void onLayoutChildren(Recycler recycler, State state) {
            super.onLayoutChildren(recycler, state);
            if (getChildCount() != 0) {
                updateLayout();
            }
        }

        private void updateLayout() {
            if (WearableRecyclerView.this.mOffsettingHelper != null) {
                for (int count = 0; count < getChildCount(); count++) {
                    WearableRecyclerView.this.mOffsettingHelper.updateChild(getChildAt(count), WearableRecyclerView.this);
                }
            }
        }
    }

    public WearableRecyclerView(Context context) {
        this(context, null);
    }

    public WearableRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WearableRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mScrollManager = new ScrollManager();
        this.mOriginalPaddingTop = Integer.MIN_VALUE;
        this.mOriginalPaddingBottom = Integer.MIN_VALUE;
        this.mPaddingPreDrawListener = new C00901();
        setHasFixedSize(true);
        setClipToPadding(false);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.RecyclerView, defStyle, 0);
            setCircularScrollingGestureEnabled(a.getBoolean(R$styleable.WearableRecyclerView_circular_scrolling_gesture_enabled, this.mCircularScrollingEnabled));
            setBezelWidth(a.getFloat(R$styleable.WearableRecyclerView_bezel_width, this.mScrollManager.getBezelWidth()));
            setScrollDegreesPerScreen(a.getFloat(R$styleable.WearableRecyclerView_scroll_degrees_per_screen, this.mScrollManager.getScrollDegreesPerScreen()));
            a.recycle();
        }
        setLayoutManager(new OffsettingLinearLayoutManager(getContext()));
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mCircularScrollingEnabled && this.mScrollManager.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    public boolean onGenericMotionEvent(MotionEvent ev) {
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager == null || isLayoutFrozen()) {
            return false;
        }
        if (ev.getAction() == 8 && RotaryEncoder.isFromRotaryEncoder(ev)) {
            int delta = Math.round((-RotaryEncoder.getRotaryAxisValue(ev)) * RotaryEncoder.getScaledScrollFactor(getContext()));
            if (layoutManager.canScrollVertically()) {
                scrollBy(0, delta);
                return true;
            } else if (layoutManager.canScrollHorizontally()) {
                scrollBy(delta, 0);
                return true;
            }
        }
        return super.onGenericMotionEvent(ev);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mScrollManager.setRecyclerView(this);
        getViewTreeObserver().addOnPreDrawListener(this.mPaddingPreDrawListener);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mScrollManager.clearRecyclerView();
        getViewTreeObserver().removeOnPreDrawListener(this.mPaddingPreDrawListener);
    }

    public void setScrollDegreesPerScreen(float degreesPerScreen) {
        this.mScrollManager.setScrollDegreesPerScreen(degreesPerScreen);
    }

    public void setBezelWidth(float fraction) {
        this.mScrollManager.setBezelWidth(fraction);
    }

    public void setCircularScrollingGestureEnabled(boolean circularScrollingGestureEnabled) {
        this.mCircularScrollingEnabled = circularScrollingGestureEnabled;
    }

    public void setCenterEdgeItems(boolean centerEdgeItems) {
        this.mCenterEdgeItems = centerEdgeItems;
        if (!this.mCenterEdgeItems) {
            setupOriginalPadding();
            this.mCenterEdgeItemsWhenThereAreChildren = false;
        } else if (getChildCount() <= 0) {
            this.mCenterEdgeItemsWhenThereAreChildren = true;
        } else {
            setupCenteredPadding();
        }
    }

    private void setupCenteredPadding() {
        int focusedPosition = 0;
        if (this.mCenterEdgeItems && getChildCount() >= 1) {
            int desiredPadding = (int) ((((float) getHeight()) * 0.5f) - (((float) getChildAt(0).getHeight()) * 0.5f));
            if (getPaddingTop() != desiredPadding) {
                this.mOriginalPaddingTop = getPaddingTop();
                this.mOriginalPaddingBottom = getPaddingBottom();
                setPadding(getPaddingLeft(), desiredPadding, getPaddingRight(), desiredPadding);
                View focusedChild = getFocusedChild();
                if (focusedChild != null) {
                    focusedPosition = getLayoutManager().getPosition(focusedChild);
                }
                getLayoutManager().scrollToPosition(focusedPosition);
            }
            return;
        }
        Log.w(TAG, "No children available");
    }

    private void setupOriginalPadding() {
        if (this.mOriginalPaddingTop != Integer.MIN_VALUE) {
            setPadding(getPaddingLeft(), this.mOriginalPaddingTop, getPaddingRight(), this.mOriginalPaddingBottom);
        }
    }
}
