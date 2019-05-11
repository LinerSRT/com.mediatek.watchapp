package android.support.wearable.view;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;
import android.support.wearable.view.GridViewPager.OnAdapterChangeListener;
import android.support.wearable.view.GridViewPager.OnPageChangeListener;

@TargetApi(20)
@Deprecated
class BackgroundController implements OnPageChangeListener, OnAdapterChangeListener {
    private GridPagerAdapter mAdapter;
    private final CrossfadeDrawable mBackground = new CrossfadeDrawable();
    private final ViewportDrawable mBaseLayer = new ViewportDrawable();
    private final Point mBaseSourcePage = new Point();
    private float mBaseXPos;
    private int mBaseXSteps;
    private float mBaseYPos;
    private int mBaseYSteps;
    private final ViewportDrawable mCrossfadeLayer = new ViewportDrawable();
    private float mCrossfadeXPos;
    private float mCrossfadeYPos;
    private final Point mCurrentPage = new Point();
    private Direction mDirection = Direction.NONE;
    private final Point mFadeSourcePage = new Point();
    private int mFadeXSteps;
    private int mFadeYSteps;
    private final Point mLastPageScrolled = new Point();
    private final Point mLastSelectedPage = new Point();
    private final LruCache<Integer, Drawable> mPageBackgrounds = new LruCache<Integer, Drawable>(5) {
        protected Drawable create(Integer key) {
            int col = BackgroundController.unpackX(key.intValue());
            return BackgroundController.this.mAdapter.getBackgroundForPage(BackgroundController.unpackY(key.intValue()), col).mutate();
        }
    };
    private final LruCache<Integer, Drawable> mRowBackgrounds = new LruCache<Integer, Drawable>(3) {
        protected Drawable create(Integer key) {
            return BackgroundController.this.mAdapter.getBackgroundForRow(key.intValue()).mutate();
        }
    };
    private float mScrollRelativeX;
    private float mScrollRelativeY;
    private boolean mUsingCrossfadeLayer;

    private enum Direction {
        LEFT(-1, 0),
        UP(0, -1),
        RIGHT(1, 0),
        DOWN(0, 1),
        NONE(0, 0);
        
        /* renamed from: x */
        private final int f0x;
        /* renamed from: y */
        private final int f1y;

        private Direction(int x, int y) {
            this.f0x = x;
            this.f1y = y;
        }

        boolean isVertical() {
            return this.f1y != 0;
        }

        boolean isHorizontal() {
            return this.f0x != 0;
        }

        static Direction fromOffset(float x, float y) {
            if (y != 0.0f) {
                return y > 0.0f ? DOWN : UP;
            } else if (x == 0.0f) {
                return NONE;
            } else {
                return x > 0.0f ? RIGHT : LEFT;
            }
        }
    }

    private static int pack(int x, int y) {
        return (y << 16) | (65535 & x);
    }

    private static int pack(Point p) {
        return pack(p.x, p.y);
    }

    private static int unpackX(int key) {
        return 65535 & key;
    }

    private static int unpackY(int key) {
        return key >>> 16;
    }

    public BackgroundController() {
        this.mBackground.setFilterBitmap(true);
        this.mCrossfadeLayer.setFilterBitmap(true);
        this.mBaseLayer.setFilterBitmap(true);
    }

    public void onPageScrollStateChanged(int state) {
        if (state == 0) {
            this.mDirection = Direction.NONE;
        }
    }

    public void onPageScrolled(int row, int column, float rowOffset, float colOffset, int rowOffsetPx, int colOffsetPx) {
        float relX;
        float relY;
        if (this.mDirection == Direction.NONE || !this.mCurrentPage.equals(this.mLastSelectedPage) || !this.mLastPageScrolled.equals(column, row)) {
            this.mLastPageScrolled.set(column, row);
            this.mCurrentPage.set(this.mLastSelectedPage.x, this.mLastSelectedPage.y);
            relX = 0.0f;
            relY = ((float) Func.clamp(row - this.mCurrentPage.y, -1, 0)) + rowOffset;
            if (relY == 0.0f) {
                relX = ((float) Func.clamp(column - this.mCurrentPage.x, -1, 0)) + colOffset;
            }
            this.mDirection = Direction.fromOffset(relX, relY);
            updateBackgrounds(this.mCurrentPage, this.mLastPageScrolled, this.mDirection, relX, relY);
        } else if (this.mDirection.isVertical()) {
            relX = 0.0f;
            relY = ((float) Func.clamp(row - this.mCurrentPage.y, -1, 0)) + rowOffset;
        } else {
            relX = ((float) Func.clamp(column - this.mCurrentPage.x, -1, 0)) + colOffset;
            relY = 0.0f;
        }
        this.mScrollRelativeX = relX;
        this.mScrollRelativeY = relY;
        this.mBaseLayer.setPosition(this.mBaseXPos + relX, this.mBaseYPos + relY);
        if (this.mUsingCrossfadeLayer) {
            this.mBackground.setProgress(!this.mDirection.isVertical() ? Math.abs(relX) : Math.abs(relY));
            this.mCrossfadeLayer.setPosition(this.mCrossfadeXPos + relX, this.mCrossfadeYPos + relY);
        }
    }

    private void updateBackgrounds(Point current, Point scrolling, Direction dir, float relX, float relY) {
        boolean overScrolling = true;
        if (this.mAdapter != null && this.mAdapter.getRowCount() > 0) {
            boolean z;
            Drawable base = updateBaseLayer(current, relX, relY);
            if (((float) current.x) + relX < 0.0f) {
                z = true;
            } else {
                z = false;
            }
            if (!z) {
                if (!(((float) current.y) + relY < 0.0f)) {
                    if (((float) scrolling.x) + relX > ((float) (this.mAdapter.getColumnCount(current.y) - 1))) {
                        z = true;
                    } else {
                        z = false;
                    }
                    if (!z && ((float) scrolling.y) + relY <= ((float) (this.mAdapter.getRowCount() - 1))) {
                        overScrolling = false;
                    }
                }
            }
            if (this.mDirection == Direction.NONE || overScrolling) {
                this.mUsingCrossfadeLayer = false;
                this.mCrossfadeLayer.setDrawable(null);
                this.mBackground.setProgress(0.0f);
                return;
            }
            updateFadingLayer(current, scrolling, dir, relX, relY, base);
            return;
        }
        this.mUsingCrossfadeLayer = false;
        this.mBaseLayer.setDrawable(null);
        this.mCrossfadeLayer.setDrawable(null);
    }

    private Drawable updateBaseLayer(Point current, float relX, float relY) {
        Drawable base = (Drawable) this.mPageBackgrounds.get(Integer.valueOf(pack(current)));
        this.mBaseSourcePage.set(current.x, current.y);
        if (base != GridPagerAdapter.BACKGROUND_NONE) {
            this.mBaseXSteps = 3;
            this.mBaseXPos = 1.0f;
        } else {
            base = (Drawable) this.mRowBackgrounds.get(Integer.valueOf(current.y));
            this.mBaseXSteps = this.mAdapter.getColumnCount(current.y) + 2;
            this.mBaseXPos = (float) (current.x + 1);
        }
        this.mBaseYSteps = 3;
        this.mBaseYPos = 1.0f;
        this.mBaseLayer.setDrawable(base);
        this.mBaseLayer.setStops(this.mBaseXSteps, this.mBaseYSteps);
        this.mBaseLayer.setPosition(this.mBaseXPos + relX, this.mBaseYPos + relY);
        this.mBackground.setBase(this.mBaseLayer);
        return base;
    }

    private void updateFadingLayer(Point current, Point scrolling, Direction dir, float relX, float relY, Drawable base) {
        int i;
        int i2 = scrolling.y;
        if (dir != Direction.DOWN) {
            i = 0;
        } else {
            i = 1;
        }
        int crossfadeY = i2 + i;
        i2 = scrolling.x;
        if (dir != Direction.RIGHT) {
            i = 0;
        } else {
            i = 1;
        }
        int crossfadeX = i2 + i;
        if (crossfadeY != this.mCurrentPage.y) {
            crossfadeX = this.mAdapter.getCurrentColumnForRow(crossfadeY, current.x);
        }
        Drawable fade = (Drawable) this.mPageBackgrounds.get(Integer.valueOf(pack(crossfadeX, crossfadeY)));
        this.mFadeSourcePage.set(crossfadeX, crossfadeY);
        boolean fadeIsRowBg = false;
        if (fade == GridPagerAdapter.BACKGROUND_NONE) {
            fade = (Drawable) this.mRowBackgrounds.get(Integer.valueOf(crossfadeY));
            fadeIsRowBg = true;
        }
        if (base != fade) {
            if (fadeIsRowBg) {
                this.mFadeXSteps = this.mAdapter.getColumnCount(Func.clamp(crossfadeY, 0, this.mAdapter.getRowCount() - 1)) + 2;
                if (dir.isHorizontal()) {
                    this.mCrossfadeXPos = (float) (current.x + 1);
                } else {
                    this.mCrossfadeXPos = (float) (crossfadeX + 1);
                }
            } else {
                this.mFadeXSteps = 3;
                this.mCrossfadeXPos = (float) (1 - dir.f0x);
            }
            this.mFadeYSteps = 3;
            this.mCrossfadeYPos = (float) (1 - dir.f1y);
            this.mUsingCrossfadeLayer = true;
            this.mCrossfadeLayer.setDrawable(fade);
            this.mCrossfadeLayer.setStops(this.mFadeXSteps, this.mFadeYSteps);
            this.mCrossfadeLayer.setPosition(this.mCrossfadeXPos + relX, this.mCrossfadeYPos + relY);
            this.mBackground.setFading(this.mCrossfadeLayer);
            return;
        }
        this.mUsingCrossfadeLayer = false;
        this.mCrossfadeLayer.setDrawable(null);
        this.mBackground.setFading(null);
        this.mBackground.setProgress(0.0f);
    }

    public void onPageSelected(int row, int column) {
        this.mLastSelectedPage.set(column, row);
    }

    public void onAdapterChanged(GridPagerAdapter oldAdapter, GridPagerAdapter newAdapter) {
        reset();
        this.mLastSelectedPage.set(0, 0);
        this.mCurrentPage.set(0, 0);
        this.mAdapter = newAdapter;
    }

    public void onDataSetChanged() {
        reset();
    }

    private void reset() {
        this.mDirection = Direction.NONE;
        this.mPageBackgrounds.evictAll();
        this.mRowBackgrounds.evictAll();
        this.mCrossfadeLayer.setDrawable(null);
        this.mBaseLayer.setDrawable(null);
    }
}
