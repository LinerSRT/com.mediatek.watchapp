package android.support.wearable.view;

import android.annotation.TargetApi;
import android.database.DataSetObservable;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

@TargetApi(20)
@Deprecated
public abstract class GridPagerAdapter {
    public static final Drawable BACKGROUND_NONE = new NoOpDrawable();
    public static final Point POSITION_NONE = new Point(-1, -1);
    public static final Point POSITION_UNCHANGED = new Point(-2, -2);
    private final DataSetObservable mObservable = new DataSetObservable();

    private static final class NoOpDrawable extends Drawable {
        private NoOpDrawable() {
        }

        public void draw(Canvas canvas) {
        }

        public void setAlpha(int alpha) {
        }

        public void setColorFilter(ColorFilter cf) {
        }

        public int getOpacity() {
            return 0;
        }
    }

    public abstract void destroyItem(ViewGroup viewGroup, int i, int i2, Object obj);

    public abstract int getColumnCount(int i);

    public abstract int getRowCount();

    public abstract Object instantiateItem(ViewGroup viewGroup, int i, int i2);

    public abstract boolean isViewFromObject(View view, Object obj);

    public int getCurrentColumnForRow(int row, int currentColumn) {
        return 0;
    }

    public void setCurrentColumnForRow(int row, int currentColumn) {
    }

    public void startUpdate(ViewGroup container) {
    }

    public void finishUpdate(ViewGroup container) {
    }

    public Drawable getBackgroundForRow(int row) {
        return BACKGROUND_NONE;
    }

    public Drawable getBackgroundForPage(int row, int column) {
        return BACKGROUND_NONE;
    }
}
