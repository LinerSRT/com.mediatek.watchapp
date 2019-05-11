package android.support.wearable.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.v4.util.SimpleArrayMap;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.View.MeasureSpec;
import android.view.View.OnApplyWindowInsetsListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewParent;
import android.view.WindowInsets;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ScrollView;
import android.widget.Scroller;

@TargetApi(20)
@Deprecated
public class GridViewPager extends ViewGroup {
    private static final int[] LAYOUT_ATTRS = new int[]{16842931};
    private static final Interpolator OVERSCROLL_INTERPOLATOR = new DragFrictionInterpolator();
    private static final Interpolator SLIDE_INTERPOLATOR = new DecelerateInterpolator(2.5f);
    private int mActivePointerId;
    private GridPagerAdapter mAdapter;
    private OnAdapterChangeListener mAdapterChangeListener;
    private boolean mAdapterChangeNotificationPending;
    private final BackgroundController mBackgroundController;
    private boolean mCalledSuper;
    private final int mCloseEnough;
    private int mColMargin;
    private boolean mConsumeInsets;
    private final Point mCurItem;
    private boolean mDatasetChangePending;
    private boolean mDelayPopulate;
    private final Runnable mEndScrollRunnable;
    private int mExpectedCurrentColumnCount;
    private int mExpectedRowCount;
    private boolean mFirstLayout;
    private int mGestureInitialScrollY;
    private float mGestureInitialX;
    private float mGestureInitialY;
    private boolean mInLayout;
    private boolean mIsAbleToDrag;
    private boolean mIsBeingDragged;
    private final SimpleArrayMap<Point, ItemInfo> mItems;
    private final int mMinFlingDistance;
    private final int mMinFlingVelocity;
    private final int mMinUsableVelocity;
    private int mOffscreenPageCount;
    private GridPagerAdapter mOldAdapter;
    private OnApplyWindowInsetsListener mOnApplyWindowInsetsListener;
    private OnPageChangeListener mOnPageChangeListener;
    private float mPointerLastX;
    private float mPointerLastY;
    private final Rect mPopulatedPageBounds;
    private final Rect mPopulatedPages;
    private final SimpleArrayMap<Point, ItemInfo> mRecycledItems;
    private Point mRestoredCurItem;
    private int mRowMargin;
    private final SparseIntArray mRowScrollX;
    private int mScrollAxis;
    private int mScrollState;
    private final Scroller mScroller;
    private View mScrollingContent;
    private int mSlideAnimationDurationMs;
    private final Point mTempPoint1;
    private final int mTouchSlop;
    private final int mTouchSlopSquared;
    private VelocityTracker mVelocityTracker;
    private WindowInsets mWindowInsets;

    public interface OnPageChangeListener {
        void onPageScrollStateChanged(int i);

        void onPageScrolled(int i, int i2, float f, float f2, int i3, int i4);

        void onPageSelected(int i, int i2);
    }

    public interface OnAdapterChangeListener {
        void onAdapterChanged(GridPagerAdapter gridPagerAdapter, GridPagerAdapter gridPagerAdapter2);

        void onDataSetChanged();
    }

    /* renamed from: android.support.wearable.view.GridViewPager$1 */
    class C00871 implements Runnable {
        final /* synthetic */ GridViewPager this$0;

        public void run() {
            this.this$0.setScrollState(0);
            this.this$0.populate();
        }
    }

    private static final class DragFrictionInterpolator implements Interpolator {
        private final float falloffRate;

        public DragFrictionInterpolator() {
            this(4.0f);
        }

        public DragFrictionInterpolator(float falloffRate) {
            this.falloffRate = falloffRate;
        }

        public float getInterpolation(float input) {
            double e = Math.exp((double) ((2.0f * input) * this.falloffRate));
            return ((float) ((e - 1.0d) / (1.0d + e))) * (1.0f / this.falloffRate);
        }
    }

    static class ItemInfo {
        Object object;
        int positionX;
        int positionY;

        ItemInfo() {
        }

        public String toString() {
            int i = this.positionX;
            int i2 = this.positionY;
            String valueOf = String.valueOf(this.object);
            return new StringBuilder(String.valueOf(valueOf).length() + 27).append(i).append(",").append(i2).append(" => ").append(valueOf).toString();
        }
    }

    public static class LayoutParams extends MarginLayoutParams {
        public int gravity;
        public boolean needsMeasure;

        public LayoutParams() {
            super(-1, -1);
        }

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray a = context.obtainStyledAttributes(attrs, GridViewPager.LAYOUT_ATTRS);
            this.gravity = a.getInteger(0, 48);
            a.recycle();
        }
    }

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new C00881();
        int currentX;
        int currentY;

        /* renamed from: android.support.wearable.view.GridViewPager$SavedState$1 */
        class C00881 implements Creator<SavedState> {
            C00881() {
            }

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.currentX);
            out.writeInt(this.currentY);
        }

        private SavedState(Parcel in) {
            super(in);
            this.currentX = in.readInt();
            this.currentY = in.readInt();
        }
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mFirstLayout = true;
        getParent().requestFitSystemWindows();
    }

    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            getChildAt(i).dispatchApplyWindowInsets(insets);
        }
        this.mWindowInsets = insets;
        return insets;
    }

    public void setOnApplyWindowInsetsListener(OnApplyWindowInsetsListener listener) {
        this.mOnApplyWindowInsetsListener = listener;
    }

    public WindowInsets dispatchApplyWindowInsets(WindowInsets insets) {
        insets = onApplyWindowInsets(insets);
        if (this.mOnApplyWindowInsetsListener != null) {
            this.mOnApplyWindowInsetsListener.onApplyWindowInsets(this, insets);
        }
        return !this.mConsumeInsets ? insets : insets.consumeSystemWindowInsets();
    }

    public void requestFitSystemWindows() {
    }

    protected void onDetachedFromWindow() {
        removeCallbacks(this.mEndScrollRunnable);
        super.onDetachedFromWindow();
    }

    private void adapterChanged(GridPagerAdapter oldAdapter, GridPagerAdapter newAdapter) {
        if (this.mAdapterChangeListener != null) {
            this.mAdapterChangeListener.onAdapterChanged(oldAdapter, newAdapter);
        }
        if (this.mBackgroundController != null) {
            this.mBackgroundController.onAdapterChanged(oldAdapter, newAdapter);
        }
    }

    public void scrollTo(int x, int y) {
        if (this.mScrollState == 2 && this.mScrollAxis == 1) {
            x = getRowScrollX(this.mCurItem.y);
        }
        super.scrollTo(0, y);
        scrollCurrentRowTo(x);
    }

    private void setScrollState(int newState) {
        if (this.mScrollState != newState) {
            this.mScrollState = newState;
            if (this.mOnPageChangeListener != null) {
                this.mOnPageChangeListener.onPageScrollStateChanged(newState);
            }
            if (this.mBackgroundController != null) {
                this.mBackgroundController.onPageScrollStateChanged(newState);
            }
        }
    }

    private int getRowScrollX(int row) {
        return this.mRowScrollX.get(row, 0);
    }

    private void setRowScrollX(int row, int scrollX) {
        this.mRowScrollX.put(row, scrollX);
    }

    private void scrollRowTo(int row, int x) {
        if (getRowScrollX(row) != x) {
            int size = getChildCount();
            int scrollAmount = x - getRowScrollX(row);
            for (int i = 0; i < size; i++) {
                View child = getChildAt(i);
                ItemInfo ii = infoForChild(child);
                if (ii != null && ii.positionY == row) {
                    child.offsetLeftAndRight(-scrollAmount);
                    postInvalidateOnAnimation();
                }
            }
            setRowScrollX(row, x);
        }
    }

    private void scrollCurrentRowTo(int x) {
        scrollRowTo(this.mCurItem.y, x);
    }

    private int getContentWidth() {
        return getMeasuredWidth() - (getPaddingLeft() + getPaddingRight());
    }

    private int getContentHeight() {
        return getMeasuredHeight() - (getPaddingTop() + getPaddingBottom());
    }

    public void setCurrentItem(int row, int column, boolean smoothScroll) {
        this.mDelayPopulate = false;
        setCurrentItemInternal(row, column, smoothScroll, false);
    }

    void setCurrentItemInternal(int row, int column, boolean smoothScroll, boolean always) {
        setCurrentItemInternal(row, column, smoothScroll, always, 0);
    }

    void setCurrentItemInternal(int row, int column, boolean smoothScroll, boolean always, int velocity) {
        if (this.mAdapter == null || this.mAdapter.getRowCount() <= 0) {
            return;
        }
        if (always || !this.mCurItem.equals(column, row) || this.mItems.size() == 0) {
            boolean dispatchSelected;
            row = limit(row, 0, this.mAdapter.getRowCount() - 1);
            column = limit(column, 0, this.mAdapter.getColumnCount(row) - 1);
            if (column != this.mCurItem.x) {
                this.mScrollAxis = 0;
                dispatchSelected = true;
            } else if (row == this.mCurItem.y) {
                dispatchSelected = false;
            } else {
                this.mScrollAxis = 1;
                dispatchSelected = true;
            }
            if (this.mFirstLayout) {
                this.mCurItem.set(0, 0);
                this.mAdapter.setCurrentColumnForRow(row, column);
                if (dispatchSelected) {
                    if (this.mOnPageChangeListener != null) {
                        this.mOnPageChangeListener.onPageSelected(row, column);
                    }
                    if (this.mBackgroundController != null) {
                        this.mBackgroundController.onPageSelected(row, column);
                    }
                }
                requestLayout();
            } else {
                populate(column, row);
                scrollToItem(column, row, smoothScroll, velocity, dispatchSelected);
            }
        }
    }

    private void scrollToItem(int x, int y, boolean smoothScroll, int velocity, boolean dispatchSelected) {
        ItemInfo curInfo = infoForPosition(x, y);
        int destX = 0;
        int destY = 0;
        if (curInfo != null) {
            destX = computePageLeft(curInfo.positionX) - getPaddingLeft();
            destY = computePageTop(curInfo.positionY) - getPaddingTop();
        }
        this.mAdapter.setCurrentColumnForRow(y, x);
        if (dispatchSelected) {
            if (this.mOnPageChangeListener != null) {
                this.mOnPageChangeListener.onPageSelected(y, x);
            }
            if (this.mBackgroundController != null) {
                this.mBackgroundController.onPageSelected(y, x);
            }
        }
        if (smoothScroll) {
            smoothScrollTo(destX, destY, velocity);
            return;
        }
        completeScroll(false);
        scrollTo(destX, destY);
        pageScrolled(destX, destY);
    }

    void smoothScrollTo(int x, int y, int velocity) {
        if (getChildCount() != 0) {
            int sx = getRowScrollX(this.mCurItem.y);
            int sy = getScrollY();
            int dx = x - sx;
            int dy = y - sy;
            if (dx == 0 && dy == 0) {
                completeScroll(false);
                populate();
                setScrollState(0);
                return;
            }
            setScrollState(2);
            this.mScroller.startScroll(sx, sy, dx, dy, this.mSlideAnimationDurationMs);
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    void flingContent(int limitX, int limitY, int velocityX, int velocityY) {
        if (this.mScrollingContent == null) {
            return;
        }
        if (velocityX == 0 && velocityY == 0) {
            completeScroll(false);
            setScrollState(0);
            return;
        }
        int minX;
        int maxX;
        int minY;
        int maxY;
        int sx = this.mScrollingContent.getScrollX();
        int sy = this.mScrollingContent.getScrollY();
        setScrollState(3);
        if (velocityX <= 0) {
            minX = sx + limitX;
            maxX = sx;
        } else {
            minX = sx;
            maxX = sx + limitX;
        }
        if (velocityY <= 0) {
            minY = sy + limitY;
            maxY = sy;
        } else {
            minY = sy;
            maxY = sy + limitY;
        }
        this.mScroller.fling(sx, sy, velocityX, velocityY, minX, maxX, minY, maxY);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    private ItemInfo addNewItem(int positionX, int positionY) {
        Point key = new Point(positionX, positionY);
        ItemInfo ii = (ItemInfo) this.mRecycledItems.remove(key);
        if (ii == null) {
            ii = new ItemInfo();
            ii.object = this.mAdapter.instantiateItem(this, positionY, positionX);
            ii.positionX = positionX;
            ii.positionY = positionY;
        }
        key.set(positionX, positionY);
        ii.positionX = positionX;
        ii.positionY = positionY;
        this.mItems.put(key, ii);
        return ii;
    }

    private void dispatchOnDataSetChanged() {
        if (this.mAdapterChangeListener != null) {
            this.mAdapterChangeListener.onDataSetChanged();
        }
        if (this.mBackgroundController != null) {
            this.mBackgroundController.onDataSetChanged();
        }
    }

    private void populate() {
        if (this.mAdapter != null && this.mAdapter.getRowCount() > 0) {
            populate(this.mCurItem.x, this.mCurItem.y);
        }
    }

    private void populate(int newX, int newY) {
        Point oldCurItem = new Point();
        if (this.mCurItem.x != newX || this.mCurItem.y != newY) {
            oldCurItem.set(this.mCurItem.x, this.mCurItem.y);
            this.mCurItem.set(newX, newY);
        }
        if (!this.mDelayPopulate && getWindowToken() != null) {
            this.mAdapter.startUpdate(this);
            this.mPopulatedPageBounds.setEmpty();
            int rowCount = this.mAdapter.getRowCount();
            if (this.mExpectedRowCount == rowCount) {
                int colCount = this.mAdapter.getColumnCount(newY);
                if (colCount >= 1) {
                    int i;
                    ItemInfo ii;
                    Point point;
                    this.mExpectedRowCount = rowCount;
                    this.mExpectedCurrentColumnCount = colCount;
                    int offscreenPages = Math.max(1, this.mOffscreenPageCount);
                    int startPosY = Math.max(0, newY - offscreenPages);
                    int endPosY = Math.min(rowCount - 1, newY + offscreenPages);
                    int startPosX = Math.max(0, newX - offscreenPages);
                    int endPosX = Math.min(colCount - 1, newX + offscreenPages);
                    for (i = this.mItems.size() - 1; i >= 0; i--) {
                        ii = (ItemInfo) this.mItems.valueAt(i);
                        if (ii.positionY != newY) {
                            if (ii.positionX == this.mAdapter.getCurrentColumnForRow(ii.positionY, this.mCurItem.x) && ii.positionY >= startPosY) {
                                if (ii.positionY <= endPosY) {
                                }
                            }
                        } else if (ii.positionX >= startPosX) {
                            if (ii.positionX <= endPosX) {
                            }
                        }
                        Point key = (Point) this.mItems.keyAt(i);
                        this.mItems.removeAt(i);
                        key.set(ii.positionX, ii.positionY);
                        this.mRecycledItems.put(key, ii);
                    }
                    this.mTempPoint1.y = newY;
                    this.mTempPoint1.x = startPosX;
                    while (this.mTempPoint1.x <= endPosX) {
                        if (!this.mItems.containsKey(this.mTempPoint1)) {
                            addNewItem(this.mTempPoint1.x, this.mTempPoint1.y);
                        }
                        point = this.mTempPoint1;
                        point.x++;
                    }
                    this.mTempPoint1.y = startPosY;
                    while (this.mTempPoint1.y <= endPosY) {
                        this.mTempPoint1.x = this.mAdapter.getCurrentColumnForRow(this.mTempPoint1.y, newX);
                        if (!this.mItems.containsKey(this.mTempPoint1)) {
                            addNewItem(this.mTempPoint1.x, this.mTempPoint1.y);
                        }
                        if (this.mTempPoint1.y != this.mCurItem.y) {
                            setRowScrollX(this.mTempPoint1.y, computePageLeft(this.mTempPoint1.x) - getPaddingLeft());
                        }
                        point = this.mTempPoint1;
                        point.y++;
                    }
                    for (i = this.mRecycledItems.size() - 1; i >= 0; i--) {
                        ii = (ItemInfo) this.mRecycledItems.removeAt(i);
                        this.mAdapter.destroyItem(this, ii.positionY, ii.positionX, ii.object);
                    }
                    this.mRecycledItems.clear();
                    this.mAdapter.finishUpdate(this);
                    this.mPopulatedPages.set(startPosX, startPosY, endPosX, endPosY);
                    this.mPopulatedPageBounds.set(computePageLeft(startPosX) - getPaddingLeft(), computePageTop(startPosY) - getPaddingTop(), computePageLeft(endPosX + 1) - getPaddingRight(), computePageTop(endPosY + 1) + getPaddingBottom());
                    if (this.mAdapterChangeNotificationPending) {
                        this.mAdapterChangeNotificationPending = false;
                        adapterChanged(this.mOldAdapter, this.mAdapter);
                        this.mOldAdapter = null;
                    }
                    if (this.mDatasetChangePending) {
                        this.mDatasetChangePending = false;
                        dispatchOnDataSetChanged();
                    }
                    return;
                }
                throw new IllegalStateException("All rows must have at least 1 column");
            }
            throw new IllegalStateException("Adapter row count changed without a call to notifyDataSetChanged()");
        }
    }

    public Parcelable onSaveInstanceState() {
        SavedState state = new SavedState(super.onSaveInstanceState());
        state.currentX = this.mCurItem.x;
        state.currentY = this.mCurItem.y;
        return state;
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState ss = (SavedState) state;
            super.onRestoreInstanceState(ss.getSuperState());
            if (pointInRange(ss.currentX, ss.currentY)) {
                this.mRestoredCurItem = new Point(ss.currentX, ss.currentY);
            } else {
                this.mCurItem.set(0, 0);
                scrollTo(0, 0);
            }
            return;
        }
        super.onRestoreInstanceState(state);
    }

    public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
        ItemInfo ii = infoForChild(child);
        if (!checkLayoutParams(params)) {
            params = generateLayoutParams(params);
        }
        LayoutParams lp = (LayoutParams) params;
        if (this.mInLayout) {
            lp.needsMeasure = true;
            addViewInLayout(child, index, params);
        } else {
            super.addView(child, index, params);
        }
        if (this.mWindowInsets != null) {
            child.onApplyWindowInsets(this.mWindowInsets);
        }
    }

    public void removeView(View view) {
        ItemInfo ii = infoForChild(view);
        if (this.mInLayout) {
            removeViewInLayout(view);
        } else {
            super.removeView(view);
        }
    }

    private ItemInfo infoForChild(View child) {
        for (int i = 0; i < this.mItems.size(); i++) {
            ItemInfo ii = (ItemInfo) this.mItems.valueAt(i);
            if (ii != null && this.mAdapter.isViewFromObject(child, ii.object)) {
                return ii;
            }
        }
        return null;
    }

    private ItemInfo infoForPosition(Point p) {
        return (ItemInfo) this.mItems.get(p);
    }

    private ItemInfo infoForPosition(int x, int y) {
        this.mTempPoint1.set(x, y);
        return (ItemInfo) this.mItems.get(this.mTempPoint1);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));
        this.mInLayout = true;
        populate();
        this.mInLayout = false;
        int size = getChildCount();
        for (int i = 0; i < size; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp != null) {
                    measureChild(child, lp);
                }
            }
        }
    }

    public void measureChild(View child, LayoutParams lp) {
        int widthMode;
        int heightMode;
        int childDefaultWidth = getContentWidth();
        int childDefaultHeight = getContentHeight();
        if (lp.width != -2) {
            widthMode = 1073741824;
        } else {
            widthMode = 0;
        }
        if (lp.height != -2) {
            heightMode = 1073741824;
        } else {
            heightMode = 0;
        }
        child.measure(getChildMeasureSpec(MeasureSpec.makeMeasureSpec(childDefaultWidth, widthMode), lp.leftMargin + lp.rightMargin, lp.width), getChildMeasureSpec(MeasureSpec.makeMeasureSpec(childDefaultHeight, heightMode), lp.topMargin + lp.bottomMargin, lp.height));
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (!this.mItems.isEmpty()) {
            recomputeScrollPosition(w, oldw, h, oldh, this.mColMargin, this.mColMargin, this.mRowMargin, this.mRowMargin);
        }
    }

    private int computePageLeft(int column) {
        return ((getContentWidth() + this.mColMargin) * column) + getPaddingLeft();
    }

    private int computePageTop(int row) {
        return ((getContentHeight() + this.mRowMargin) * row) + getPaddingTop();
    }

    private void recomputeScrollPosition(int width, int oldWidth, int height, int oldHeight, int colMargin, int oldColMargin, int rowMargin, int oldRowMargin) {
        if (oldWidth > 0 && oldHeight > 0) {
            float pageOffset = ((float) getRowScrollX(this.mCurItem.y)) / ((float) (((oldWidth - getPaddingLeft()) - getPaddingRight()) + oldColMargin));
            int newOffsetXPixels = (int) (((float) (((width - getPaddingLeft()) - getPaddingRight()) + colMargin)) * pageOffset);
            float pageOffsetY = ((float) getScrollY()) / ((float) (((oldHeight - getPaddingTop()) - getPaddingBottom()) + oldRowMargin));
            int newOffsetYPixels = (int) (((float) (((height - getPaddingTop()) - getPaddingBottom()) + rowMargin)) * pageOffsetY);
            scrollTo(newOffsetXPixels, newOffsetYPixels);
            if (!this.mScroller.isFinished()) {
                ItemInfo targetInfo = infoForPosition(this.mCurItem);
                this.mScroller.startScroll(newOffsetXPixels, newOffsetYPixels, computePageLeft(targetInfo.positionX) - getPaddingLeft(), computePageTop(targetInfo.positionY) - getPaddingTop(), this.mScroller.getDuration() - this.mScroller.timePassed());
                return;
            }
            return;
        }
        ItemInfo ii = infoForPosition(this.mCurItem);
        if (ii != null) {
            int targetX = computePageLeft(ii.positionX) - getPaddingLeft();
            int targetY = computePageTop(ii.positionY) - getPaddingTop();
            if (targetX != getRowScrollX(ii.positionY) || targetY != getScrollY()) {
                completeScroll(false);
                scrollTo(targetX, targetY);
            }
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int children = getChildCount();
        for (int i = 0; i < children; i++) {
            View view = getChildAt(i);
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            String valueOf;
            if (lp != null) {
                ItemInfo ii = infoForChild(view);
                if (ii != null) {
                    if (lp.needsMeasure) {
                        lp.needsMeasure = false;
                        measureChild(view, lp);
                    }
                    int left = computePageLeft(ii.positionX);
                    left = (left - getRowScrollX(ii.positionY)) + lp.leftMargin;
                    int top = computePageTop(ii.positionY) + lp.topMargin;
                    view.layout(left, top, view.getMeasuredWidth() + left, view.getMeasuredHeight() + top);
                } else {
                    valueOf = String.valueOf(view);
                    Log.w("GridViewPager", new StringBuilder(String.valueOf(valueOf).length() + 44).append("Unknown child view, not claimed by adapter: ").append(valueOf).toString());
                }
            } else {
                valueOf = String.valueOf(view);
                Log.w("GridViewPager", new StringBuilder(String.valueOf(valueOf).length() + 34).append("Got null layout params for child: ").append(valueOf).toString());
            }
        }
        if (this.mFirstLayout && !this.mItems.isEmpty()) {
            scrollToItem(this.mCurItem.x, this.mCurItem.y, false, 0, false);
        }
        this.mFirstLayout = false;
    }

    public void computeScroll() {
        if (!this.mScroller.isFinished() && this.mScroller.computeScrollOffset()) {
            if (this.mScrollState != 3) {
                int oldX = getRowScrollX(this.mCurItem.y);
                int oldY = getScrollY();
                int x = this.mScroller.getCurrX();
                int y = this.mScroller.getCurrY();
                if (oldX != x || oldY != y) {
                    scrollTo(x, y);
                    if (!pageScrolled(x, y)) {
                        this.mScroller.abortAnimation();
                        scrollTo(0, 0);
                    }
                }
            } else if (this.mScrollingContent != null) {
                this.mScrollingContent.scrollTo(this.mScroller.getCurrX(), this.mScroller.getCurrY());
            } else {
                this.mScroller.abortAnimation();
            }
            ViewCompat.postInvalidateOnAnimation(this);
            return;
        }
        completeScroll(true);
    }

    private boolean pageScrolled(int xpos, int ypos) {
        if (this.mItems.size() != 0) {
            ItemInfo ii = infoForCurrentScrollPosition();
            int pageLeft = computePageLeft(ii.positionX);
            int offsetLeftPx = (getPaddingLeft() + xpos) - pageLeft;
            int offsetTopPx = (getPaddingTop() + ypos) - computePageTop(ii.positionY);
            float offsetLeft = getXIndex((float) offsetLeftPx);
            float offsetTop = getYIndex((float) offsetTopPx);
            this.mCalledSuper = false;
            onPageScrolled(ii.positionX, ii.positionY, offsetLeft, offsetTop, offsetLeftPx, offsetTopPx);
            if (this.mCalledSuper) {
                return true;
            }
            throw new IllegalStateException("onPageScrolled did not call superclass implementation");
        }
        this.mCalledSuper = false;
        onPageScrolled(0, 0, 0.0f, 0.0f, 0, 0);
        if (this.mCalledSuper) {
            return false;
        }
        throw new IllegalStateException("onPageScrolled did not call superclass implementation");
    }

    public void onPageScrolled(int positionX, int positionY, float offsetX, float offsetY, int offsetLeftPx, int offsetTopPx) {
        this.mCalledSuper = true;
        if (this.mOnPageChangeListener != null) {
            this.mOnPageChangeListener.onPageScrolled(positionY, positionX, offsetY, offsetX, offsetTopPx, offsetLeftPx);
        }
        if (this.mBackgroundController != null) {
            this.mBackgroundController.onPageScrolled(positionY, positionX, offsetY, offsetX, offsetTopPx, offsetLeftPx);
        }
    }

    private void completeScroll(boolean postEvents) {
        boolean needPopulate;
        if (this.mScrollState != 2) {
            needPopulate = false;
        } else {
            needPopulate = true;
        }
        if (needPopulate) {
            this.mScroller.abortAnimation();
            int oldX = getRowScrollX(this.mCurItem.y);
            int oldY = getScrollY();
            int x = this.mScroller.getCurrX();
            int y = this.mScroller.getCurrY();
            if (oldX != x || oldY != y) {
                scrollTo(x, y);
            }
        }
        this.mScrollingContent = null;
        this.mDelayPopulate = false;
        if (!needPopulate) {
            return;
        }
        if (postEvents) {
            ViewCompat.postOnAnimation(this, this.mEndScrollRunnable);
        } else {
            this.mEndScrollRunnable.run();
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction() & 255;
        if (action == 3 || action == 1) {
            this.mIsBeingDragged = false;
            this.mIsAbleToDrag = false;
            this.mActivePointerId = -1;
            if (this.mVelocityTracker != null) {
                this.mVelocityTracker.recycle();
                this.mVelocityTracker = null;
            }
            return false;
        }
        if (action != 0) {
            if (this.mIsBeingDragged) {
                return true;
            }
            if (!this.mIsAbleToDrag) {
                return false;
            }
        }
        switch (action) {
            case 0:
                handlePointerDown(ev);
                break;
            case 2:
                handlePointerMove(ev);
                break;
            case 6:
                onSecondaryPointerUp(ev);
                break;
        }
        return this.mIsBeingDragged;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (this.mAdapter == null) {
            return false;
        }
        int action = ev.getAction();
        switch (action & 255) {
            case 0:
                handlePointerDown(ev);
                break;
            case 1:
            case 3:
                handlePointerUp(ev);
                break;
            case 2:
                handlePointerMove(ev);
                break;
            case 6:
                onSecondaryPointerUp(ev);
                break;
            default:
                Log.e("GridViewPager", "Unknown action type: " + action);
                break;
        }
        return true;
    }

    private void requestParentDisallowInterceptTouchEvent(boolean disallowIntercept) {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    private static float limit(float input, int limit) {
        if (limit <= 0) {
            return Math.min(0.0f, Math.max(input, (float) limit));
        }
        return Math.max(0.0f, Math.min(input, (float) limit));
    }

    private boolean performDrag(float x, float y) {
        float deltaX = this.mPointerLastX - x;
        float deltaY = this.mPointerLastY - y;
        this.mPointerLastX = x;
        this.mPointerLastY = y;
        Rect pages = this.mPopulatedPages;
        int leftBound = computePageLeft(pages.left) - getPaddingLeft();
        int rightBound = computePageLeft(pages.right) - getPaddingLeft();
        int topBound = computePageTop(pages.top) - getPaddingTop();
        int bottomBound = computePageTop(pages.bottom) - getPaddingTop();
        float scrollX = (float) getRowScrollX(this.mCurItem.y);
        float scrollY = (float) getScrollY();
        if (this.mScrollAxis == 1) {
            float distanceToFocusPoint;
            int pageSpacingY = getContentHeight() + this.mRowMargin;
            if (deltaY < 0.0f) {
                distanceToFocusPoint = -(scrollY % ((float) pageSpacingY));
            } else {
                distanceToFocusPoint = (((float) pageSpacingY) - (scrollY % ((float) pageSpacingY))) % ((float) pageSpacingY);
            }
            boolean focalPointCrossed = false;
            if (Math.abs(distanceToFocusPoint) <= Math.abs(deltaY)) {
                deltaY -= distanceToFocusPoint;
                scrollY += distanceToFocusPoint;
                focalPointCrossed = true;
            }
            if (focalPointCrossed) {
                View child = getChildForInfo(infoForScrollPosition((int) scrollX, (int) scrollY));
                if (child != null) {
                    float consumed = limit(deltaY, getScrollableDistance(child, (int) Math.signum(deltaY)));
                    child.scrollBy(0, (int) consumed);
                    deltaY -= consumed;
                    this.mPointerLastY += consumed - ((float) ((int) consumed));
                }
            }
        }
        int targetX = (int) (((float) ((int) deltaX)) + scrollX);
        int targetY = (int) (((float) ((int) deltaY)) + scrollY);
        boolean wouldOverscroll = targetX < leftBound || targetX > rightBound || targetY < topBound || targetY > bottomBound;
        if (wouldOverscroll) {
            boolean couldScroll;
            float overscrollX;
            float overscrollY;
            int mode = getOverScrollMode();
            if (this.mScrollAxis != 0 || leftBound >= rightBound) {
                if (this.mScrollAxis == 1) {
                    if (topBound >= bottomBound) {
                    }
                }
                couldScroll = false;
                if (mode != 0) {
                    if (couldScroll) {
                        if (mode != 1) {
                        }
                    }
                    deltaX = limit(deltaX, ((float) leftBound) - scrollX, ((float) rightBound) - scrollX);
                    deltaY = limit(deltaY, ((float) topBound) - scrollY, ((float) bottomBound) - scrollY);
                }
                overscrollX = scrollX <= ((float) rightBound) ? scrollX - ((float) rightBound) : scrollX >= ((float) leftBound) ? scrollX - ((float) leftBound) : 0.0f;
                overscrollY = scrollY <= ((float) bottomBound) ? scrollY - ((float) bottomBound) : scrollY >= ((float) topBound) ? scrollY - ((float) topBound) : 0.0f;
                if (Math.abs(overscrollX) > 0.0f && Math.signum(overscrollX) == Math.signum(deltaX)) {
                    deltaX *= OVERSCROLL_INTERPOLATOR.getInterpolation(1.0f - (Math.abs(overscrollX) / ((float) getContentWidth())));
                }
                if (Math.abs(overscrollY) > 0.0f && Math.signum(overscrollY) == Math.signum(deltaY)) {
                    deltaY *= OVERSCROLL_INTERPOLATOR.getInterpolation(1.0f - (Math.abs(overscrollY) / ((float) getContentHeight())));
                }
            }
            couldScroll = true;
            if (mode != 0) {
                if (couldScroll) {
                    if (mode != 1) {
                    }
                }
                deltaX = limit(deltaX, ((float) leftBound) - scrollX, ((float) rightBound) - scrollX);
                deltaY = limit(deltaY, ((float) topBound) - scrollY, ((float) bottomBound) - scrollY);
            }
            if (scrollX <= ((float) rightBound)) {
                if (scrollX >= ((float) leftBound)) {
                }
            }
            if (scrollY <= ((float) bottomBound)) {
                if (scrollY >= ((float) topBound)) {
                }
            }
            deltaX *= OVERSCROLL_INTERPOLATOR.getInterpolation(1.0f - (Math.abs(overscrollX) / ((float) getContentWidth())));
            deltaY *= OVERSCROLL_INTERPOLATOR.getInterpolation(1.0f - (Math.abs(overscrollY) / ((float) getContentHeight())));
        }
        scrollX += deltaX;
        scrollY += deltaY;
        this.mPointerLastX += scrollX - ((float) ((int) scrollX));
        this.mPointerLastY += scrollY - ((float) ((int) scrollY));
        scrollTo((int) scrollX, (int) scrollY);
        pageScrolled((int) scrollX, (int) scrollY);
        return true;
    }

    private int getScrollableDistance(View child, int dir) {
        if (child instanceof CardScrollView) {
            return ((CardScrollView) child).getAvailableScrollDelta(dir);
        }
        if (child instanceof ScrollView) {
            return getScrollableDistance((ScrollView) child, dir);
        }
        return 0;
    }

    private int getScrollableDistance(ScrollView view, int direction) {
        if (view.getChildCount() <= 0) {
            return 0;
        }
        View content = view.getChildAt(0);
        int height = view.getHeight();
        int contentHeight = content.getHeight();
        int extra = contentHeight - height;
        if (contentHeight <= height) {
            return 0;
        }
        if (direction > 0) {
            return Math.min(extra - view.getScrollY(), 0);
        }
        if (direction < 0) {
            return -view.getScrollY();
        }
        return 0;
    }

    private View getChildForInfo(ItemInfo ii) {
        if (ii.object != null) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                if (this.mAdapter.isViewFromObject(child, ii.object)) {
                    return child;
                }
            }
        }
        return null;
    }

    private ItemInfo infoForCurrentScrollPosition() {
        return infoForScrollPosition(getRowScrollX((int) getYIndex((float) getScrollY())), getScrollY());
    }

    private ItemInfo infoForScrollPosition(int scrollX, int scrollY) {
        int y = (int) getYIndex((float) scrollY);
        int x = (int) getXIndex((float) scrollX);
        ItemInfo ii = infoForPosition(x, y);
        if (ii != null) {
            return ii;
        }
        ii = new ItemInfo();
        ii.positionX = x;
        ii.positionY = y;
        return ii;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        int newPointerIndex = 0;
        int pointerIndex = MotionEventCompat.getActionIndex(ev);
        if (MotionEventCompat.getPointerId(ev, pointerIndex) == this.mActivePointerId) {
            if (pointerIndex == 0) {
                newPointerIndex = 1;
            }
            this.mPointerLastX = MotionEventCompat.getX(ev, newPointerIndex);
            this.mPointerLastY = MotionEventCompat.getY(ev, newPointerIndex);
            this.mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
            if (this.mVelocityTracker != null) {
                this.mVelocityTracker.clear();
            }
        }
    }

    private void endDrag() {
        this.mIsBeingDragged = false;
        this.mIsAbleToDrag = false;
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    public boolean canScrollHorizontally(int direction) {
        boolean z = false;
        if (getVisibility() != 0 || this.mAdapter == null || this.mItems.isEmpty()) {
            return false;
        }
        int scrollX = getRowScrollX(this.mCurItem.y);
        int lastColumnIndex = this.mExpectedCurrentColumnCount - 1;
        if (direction <= 0) {
            if (scrollX > 0) {
                z = true;
            }
            return z;
        }
        if (getPaddingLeft() + scrollX < computePageLeft(lastColumnIndex)) {
            z = true;
        }
        return z;
    }

    public boolean canScrollVertically(int direction) {
        boolean z = false;
        if (getVisibility() != 0 || this.mAdapter == null || this.mItems.isEmpty()) {
            return false;
        }
        int scrollY = getScrollY();
        int lastRowIndex = this.mExpectedRowCount - 1;
        if (direction <= 0) {
            if (scrollY > 0) {
                z = true;
            }
            return z;
        }
        if (getPaddingTop() + scrollY < computePageTop(lastRowIndex)) {
            z = true;
        }
        return z;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event) || executeKeyEvent(event);
    }

    private boolean executeKeyEvent(KeyEvent event) {
        boolean handled = false;
        switch (event.getKeyCode()) {
            case 19:
                handled = pageUp();
                break;
            case 20:
                handled = pageDown();
                break;
            case 21:
                handled = pageLeft();
                break;
            case 22:
                handled = pageRight();
                break;
            case 62:
                debug();
                return true;
        }
        return handled;
    }

    private boolean pageLeft() {
        if (this.mCurItem.x <= 0) {
            return false;
        }
        setCurrentItem(this.mCurItem.x - 1, this.mCurItem.y, true);
        return true;
    }

    private boolean pageRight() {
        if (this.mAdapter == null || this.mCurItem.x >= this.mAdapter.getColumnCount(this.mCurItem.y) - 1) {
            return false;
        }
        setCurrentItem(this.mCurItem.x + 1, this.mCurItem.y, true);
        return true;
    }

    private boolean pageUp() {
        if (this.mCurItem.y <= 0) {
            return false;
        }
        setCurrentItem(this.mCurItem.x, this.mCurItem.y - 1, true);
        return true;
    }

    private boolean pageDown() {
        if (this.mAdapter == null || this.mCurItem.y >= this.mAdapter.getRowCount() - 1) {
            return false;
        }
        setCurrentItem(this.mCurItem.x, this.mCurItem.y + 1, true);
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean handlePointerDown(android.view.MotionEvent r5) {
        /*
        r4 = this;
        r3 = 1;
        r2 = 0;
        r0 = r4.mIsBeingDragged;
        if (r0 != 0) goto L_0x004b;
    L_0x0006:
        r0 = android.support.v4.view.MotionEventCompat.getPointerId(r5, r2);
        r4.mActivePointerId = r0;
        r0 = r5.getX();
        r4.mGestureInitialX = r0;
        r0 = r5.getY();
        r4.mGestureInitialY = r0;
        r0 = r4.getScrollY();
        r4.mGestureInitialScrollY = r0;
        r0 = r4.mGestureInitialX;
        r4.mPointerLastX = r0;
        r0 = r4.mGestureInitialY;
        r4.mPointerLastY = r0;
        r4.mIsAbleToDrag = r3;
        r0 = android.view.VelocityTracker.obtain();
        r4.mVelocityTracker = r0;
        r0 = r4.mVelocityTracker;
        r0.addMovement(r5);
        r0 = r4.mScroller;
        r0.computeScrollOffset();
        r0 = r4.mScrollState;
        r1 = 2;
        if (r0 != r1) goto L_0x004c;
    L_0x003d:
        r0 = r4.mScrollAxis;
        if (r0 == 0) goto L_0x0052;
    L_0x0041:
        r0 = r4.mScrollAxis;
        if (r0 == r3) goto L_0x007a;
    L_0x0045:
        r4.completeScroll(r2);
        r4.mIsBeingDragged = r2;
    L_0x004a:
        return r2;
    L_0x004b:
        return r2;
    L_0x004c:
        r0 = r4.mScrollState;
        r1 = 3;
        if (r0 == r1) goto L_0x003d;
    L_0x0051:
        goto L_0x0041;
    L_0x0052:
        r0 = r4.mScroller;
        r0 = r0.getFinalX();
        r1 = r4.mScroller;
        r1 = r1.getCurrX();
        r0 = r0 - r1;
        r0 = java.lang.Math.abs(r0);
        r1 = r4.mCloseEnough;
        if (r0 <= r1) goto L_0x0041;
    L_0x0067:
        r0 = r4.mScroller;
        r0.abortAnimation();
        r4.mDelayPopulate = r2;
        r4.populate();
        r4.mIsBeingDragged = r3;
        r4.requestParentDisallowInterceptTouchEvent(r3);
        r4.setScrollState(r3);
        goto L_0x004a;
    L_0x007a:
        r0 = r4.mScroller;
        r0 = r0.getFinalY();
        r1 = r4.mScroller;
        r1 = r1.getCurrY();
        r0 = r0 - r1;
        r0 = java.lang.Math.abs(r0);
        r1 = r4.mCloseEnough;
        if (r0 > r1) goto L_0x0067;
    L_0x008f:
        goto L_0x0045;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.wearable.view.GridViewPager.handlePointerDown(android.view.MotionEvent):boolean");
    }

    private boolean handlePointerMove(MotionEvent ev) {
        int activePointerId = this.mActivePointerId;
        if (activePointerId == -1) {
            return false;
        }
        int pointerIndex = ev.findPointerIndex(activePointerId);
        if (pointerIndex == -1) {
            return this.mIsBeingDragged;
        }
        float x = MotionEventCompat.getX(ev, pointerIndex);
        float y = MotionEventCompat.getY(ev, pointerIndex);
        float dx = x - this.mPointerLastX;
        float xDiff = Math.abs(dx);
        float dy = y - this.mPointerLastY;
        float yDiff = Math.abs(dy);
        if (this.mIsBeingDragged) {
        }
        if (!this.mIsBeingDragged && (xDiff * xDiff) + (yDiff * yDiff) > ((float) this.mTouchSlopSquared)) {
            float sy;
            float sx;
            this.mIsBeingDragged = true;
            requestParentDisallowInterceptTouchEvent(true);
            setScrollState(1);
            if (yDiff >= xDiff) {
                this.mScrollAxis = 1;
            } else {
                this.mScrollAxis = 0;
            }
            if (yDiff > 0.0f && xDiff > 0.0f) {
                double t = Math.acos(((double) xDiff) / Math.hypot((double) xDiff, (double) yDiff));
                sy = (float) (Math.sin(t) * ((double) this.mTouchSlop));
                sx = (float) (Math.cos(t) * ((double) this.mTouchSlop));
            } else if (yDiff == 0.0f) {
                sx = (float) this.mTouchSlop;
                sy = 0.0f;
            } else {
                sx = 0.0f;
                sy = (float) this.mTouchSlop;
            }
            this.mPointerLastX = dx > 0.0f ? this.mPointerLastX + sx : this.mPointerLastX - sx;
            this.mPointerLastY = dy > 0.0f ? this.mPointerLastY + sy : this.mPointerLastY - sy;
        }
        if (this.mIsBeingDragged) {
            float dragX;
            float dragY;
            if (this.mScrollAxis != 0) {
                dragX = this.mPointerLastX;
            } else {
                dragX = x;
            }
            if (this.mScrollAxis != 1) {
                dragY = this.mPointerLastY;
            } else {
                dragY = y;
            }
            if (performDrag(dragX, dragY)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }
        this.mVelocityTracker.addMovement(ev);
        return this.mIsBeingDragged;
    }

    private boolean handlePointerUp(MotionEvent ev) {
        if (this.mIsBeingDragged && this.mExpectedRowCount != 0) {
            VelocityTracker velocityTracker = this.mVelocityTracker;
            velocityTracker.addMovement(ev);
            velocityTracker.computeCurrentVelocity(1000);
            int activePointerIndex = ev.findPointerIndex(this.mActivePointerId);
            int targetPageX = this.mCurItem.x;
            int targetPageY = this.mCurItem.y;
            int velocity = 0;
            ItemInfo ii = infoForCurrentScrollPosition();
            switch (this.mScrollAxis) {
                case 0:
                    int totalDeltaX = (int) (ev.getRawX() - this.mGestureInitialX);
                    velocity = (int) velocityTracker.getXVelocity(this.mActivePointerId);
                    int currentPageX = ii.positionX;
                    float pageOffsetX = getXIndex((float) (getRowScrollX(ii.positionY) - computePageLeft(ii.positionX)));
                    targetPageX = determineTargetPage(this.mCurItem.x, currentPageX, pageOffsetX, this.mPopulatedPages.left, this.mPopulatedPages.right, velocity, totalDeltaX);
                    break;
                case 1:
                    float y = ev.getX(activePointerIndex);
                    int totalDeltaY = this.mGestureInitialScrollY - getScrollY();
                    velocity = (int) velocityTracker.getYVelocity(this.mActivePointerId);
                    int currentPageY = ii.positionY;
                    float pageOffsetY = getYIndex((float) (getScrollY() - computePageTop(ii.positionY)));
                    if (pageOffsetY != 0.0f) {
                        targetPageY = determineTargetPage(this.mCurItem.y, currentPageY, pageOffsetY, this.mPopulatedPages.top, this.mPopulatedPages.bottom, velocity, totalDeltaY);
                        break;
                    }
                    View child = getChildForInfo(infoForCurrentScrollPosition());
                    int scrollable = getScrollableDistance(child, -velocity);
                    if (scrollable != 0) {
                        this.mScrollingContent = child;
                        if (Math.abs(velocity) >= Math.abs(this.mMinFlingVelocity)) {
                            flingContent(0, scrollable, 0, -velocity);
                            endDrag();
                            break;
                        }
                    }
                    break;
            }
            if (this.mScrollState != 3) {
                this.mDelayPopulate = true;
                if (targetPageY != this.mCurItem.y) {
                    targetPageX = this.mAdapter.getCurrentColumnForRow(targetPageY, this.mCurItem.x);
                }
                setCurrentItemInternal(targetPageY, targetPageX, true, true, velocity);
            }
            this.mActivePointerId = -1;
            endDrag();
            return false;
        }
        this.mActivePointerId = -1;
        endDrag();
        return false;
    }

    private float getXIndex(float distanceX) {
        int width = getContentWidth() + this.mColMargin;
        if (width != 0) {
            return distanceX / ((float) width);
        }
        Log.e("GridViewPager", "getXIndex() called with zero width.");
        return 0.0f;
    }

    private float getYIndex(float distanceY) {
        int height = getContentHeight() + this.mRowMargin;
        if (height != 0) {
            return distanceY / ((float) height);
        }
        Log.e("GridViewPager", "getYIndex() called with zero height.");
        return 0.0f;
    }

    private int determineTargetPage(int previousPage, int currentPage, float pageOffset, int firstPage, int lastPage, int velocity, int totalDragDistance) {
        int targetPage;
        if (Math.abs(velocity) < this.mMinUsableVelocity) {
            velocity = (int) Math.copySign((float) velocity, (float) totalDragDistance);
        }
        float flingBoost = (0.5f / Math.max(Math.abs(0.5f - pageOffset), 0.001f)) * 100.0f;
        if (Math.abs(totalDragDistance) > this.mMinFlingDistance && ((float) Math.abs(velocity)) + flingBoost > ((float) this.mMinFlingVelocity)) {
            targetPage = velocity <= 0 ? currentPage + 1 : currentPage;
        } else {
            targetPage = Math.round(((float) currentPage) + pageOffset);
        }
        return limit(targetPage, firstPage, lastPage);
    }

    private static int limit(int val, int min, int max) {
        if (val < min) {
            return min;
        }
        if (val <= max) {
            return val;
        }
        return max;
    }

    private static float limit(float val, float min, float max) {
        if (val < min) {
            return min;
        }
        if (val > max) {
            return max;
        }
        return val;
    }

    protected android.view.ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams();
    }

    protected android.view.ViewGroup.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return generateDefaultLayoutParams();
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return (p instanceof LayoutParams) && super.checkLayoutParams(p);
    }

    public android.view.ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    public void debug() {
        debug(0);
    }

    protected void debug(int depth) {
        super.debug(depth);
        String valueOf = String.valueOf(debugIndent(depth));
        String valueOf2 = String.valueOf(this.mCurItem);
        Log.d("View", new StringBuilder((String.valueOf(valueOf).length() + 11) + String.valueOf(valueOf2).length()).append(valueOf).append("mCurItem={").append(valueOf2).append("}").toString());
        valueOf = String.valueOf(debugIndent(depth));
        valueOf2 = String.valueOf(this.mAdapter);
        Log.d("View", new StringBuilder((String.valueOf(valueOf).length() + 11) + String.valueOf(valueOf2).length()).append(valueOf).append("mAdapter={").append(valueOf2).append("}").toString());
        valueOf = String.valueOf(debugIndent(depth));
        Log.d("View", new StringBuilder(String.valueOf(valueOf).length() + 21).append(valueOf).append("mRowCount=").append(this.mExpectedRowCount).toString());
        valueOf = String.valueOf(debugIndent(depth));
        Log.d("View", new StringBuilder(String.valueOf(valueOf).length() + 31).append(valueOf).append("mCurrentColumnCount=").append(this.mExpectedCurrentColumnCount).toString());
        int count = this.mItems.size();
        if (count != 0) {
            Log.d("View", String.valueOf(debugIndent(depth)).concat("mItems={"));
        }
        for (int i = 0; i < count; i++) {
            valueOf = String.valueOf(debugIndent(depth + 1));
            valueOf2 = String.valueOf(this.mItems.keyAt(i));
            String valueOf3 = String.valueOf(this.mItems.valueAt(i));
            Log.d("View", new StringBuilder(((String.valueOf(valueOf).length() + 4) + String.valueOf(valueOf2).length()) + String.valueOf(valueOf3).length()).append(valueOf).append(valueOf2).append(" => ").append(valueOf3).toString());
        }
        if (count != 0) {
            Log.d("View", String.valueOf(debugIndent(depth)).concat("}"));
        }
    }

    private static String debugIndent(int depth) {
        StringBuilder spaces = new StringBuilder(((depth * 2) + 3) * 2);
        for (int i = 0; i < (depth * 2) + 3; i++) {
            spaces.append(' ').append(' ');
        }
        return spaces.toString();
    }

    private static boolean inRange(int value, int min, int max) {
        return value >= min && value <= max;
    }

    private boolean pointInRange(int x, int y) {
        return inRange(y, 0, this.mExpectedRowCount + -1) && inRange(x, 0, this.mAdapter.getColumnCount(y) - 1);
    }
}
