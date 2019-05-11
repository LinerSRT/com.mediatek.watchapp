package android.support.wearable.view;

import android.annotation.TargetApi;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.Callback;

@TargetApi(20)
@Deprecated
public class CrossfadeDrawable extends Drawable implements Callback {
    private int mAlpha;
    private Drawable mBase;
    private int mChangingConfigs;
    private ColorFilter mColorFilter;
    private int mColorFilterColor;
    private Mode mColorFilterMode;
    private boolean mDither;
    private Drawable mFading;
    private boolean mFilterBitmap;
    private float mProgress;

    public void setFading(Drawable d) {
        if (this.mFading != d) {
            if (this.mFading != null) {
                this.mFading.setCallback(null);
            }
            this.mFading = d;
            if (d != null) {
                initDrawable(d);
            }
            invalidateSelf();
        }
    }

    public void setBase(Drawable d) {
        if (this.mBase != d) {
            if (this.mBase != null) {
                this.mBase.setCallback(null);
            }
            this.mBase = d;
            initDrawable(d);
            invalidateSelf();
        }
    }

    public void setProgress(float progress) {
        float updated = Func.clamp(progress, 0, 1);
        if (updated != this.mProgress) {
            this.mProgress = updated;
            invalidateSelf();
        }
    }

    private void initDrawable(Drawable d) {
        d.setCallback(this);
        d.setState(getState());
        if (this.mColorFilter != null) {
            d.setColorFilter(this.mColorFilter);
        }
        if (this.mColorFilterMode != null) {
            d.setColorFilter(this.mColorFilterColor, this.mColorFilterMode);
        }
        d.setDither(this.mDither);
        d.setFilterBitmap(this.mFilterBitmap);
        d.setBounds(getBounds());
    }

    public void draw(Canvas canvas) {
        if (this.mBase != null) {
            if ((this.mProgress < 1.0f ? 1 : null) != null || this.mFading == null) {
                this.mBase.setAlpha(255);
                this.mBase.draw(canvas);
            }
        }
        if (this.mFading != null && this.mProgress > 0.0f) {
            this.mFading.setAlpha((int) (this.mProgress * 255.0f));
            this.mFading.draw(canvas);
        }
    }

    public int getIntrinsicWidth() {
        int fading;
        int base;
        if (this.mFading != null) {
            fading = this.mFading.getIntrinsicWidth();
        } else {
            fading = -1;
        }
        if (this.mBase != null) {
            base = this.mBase.getIntrinsicHeight();
        } else {
            base = -1;
        }
        return Math.max(fading, base);
    }

    public int getIntrinsicHeight() {
        int fading;
        int base;
        if (this.mFading != null) {
            fading = this.mFading.getIntrinsicHeight();
        } else {
            fading = -1;
        }
        if (this.mBase != null) {
            base = this.mBase.getIntrinsicHeight();
        } else {
            base = -1;
        }
        return Math.max(fading, base);
    }

    protected void onBoundsChange(Rect bounds) {
        if (this.mBase != null) {
            this.mBase.setBounds(bounds);
        }
        if (this.mFading != null) {
            this.mFading.setBounds(bounds);
        }
        invalidateSelf();
    }

    public void jumpToCurrentState() {
        if (this.mFading != null) {
            this.mFading.jumpToCurrentState();
        }
        if (this.mBase != null) {
            this.mBase.jumpToCurrentState();
        }
    }

    public void setChangingConfigurations(int configs) {
        if (this.mChangingConfigs != configs) {
            this.mChangingConfigs = configs;
            if (this.mFading != null) {
                this.mFading.setChangingConfigurations(configs);
            }
            if (this.mBase != null) {
                this.mBase.setChangingConfigurations(configs);
            }
        }
    }

    public void setFilterBitmap(boolean filter) {
        if (this.mFilterBitmap != filter) {
            this.mFilterBitmap = filter;
            if (this.mFading != null) {
                this.mFading.setFilterBitmap(filter);
            }
            if (this.mBase != null) {
                this.mBase.setFilterBitmap(filter);
            }
        }
    }

    public void setDither(boolean dither) {
        if (this.mDither != dither) {
            this.mDither = dither;
            if (this.mFading != null) {
                this.mFading.setDither(dither);
            }
            if (this.mBase != null) {
                this.mBase.setDither(dither);
            }
        }
    }

    public void setColorFilter(ColorFilter cf) {
        if (this.mColorFilter != cf) {
            this.mColorFilter = cf;
            if (this.mFading != null) {
                this.mFading.setColorFilter(cf);
            }
            if (this.mBase != null) {
                this.mBase.setColorFilter(cf);
            }
        }
    }

    public void setColorFilter(int color, Mode mode) {
        if (this.mColorFilterColor != color || this.mColorFilterMode != mode) {
            this.mColorFilterColor = color;
            this.mColorFilterMode = mode;
            if (this.mFading != null) {
                this.mFading.setColorFilter(color, mode);
            }
            if (this.mBase != null) {
                this.mBase.setColorFilter(color, mode);
            }
        }
    }

    public void clearColorFilter() {
        if (this.mColorFilterMode != null) {
            this.mColorFilterMode = null;
            if (this.mFading != null) {
                this.mFading.clearColorFilter();
            }
            if (this.mBase != null) {
                this.mBase.clearColorFilter();
            }
        }
    }

    public int getChangingConfigurations() {
        return this.mChangingConfigs;
    }

    protected boolean onStateChange(int[] state) {
        boolean changed = false;
        if (this.mFading != null) {
            changed = this.mFading.setState(state) | 0;
        }
        if (this.mBase == null) {
            return changed;
        }
        return changed | this.mBase.setState(state);
    }

    protected boolean onLevelChange(int level) {
        boolean changed = false;
        if (this.mFading != null) {
            changed = this.mFading.setLevel(level) | 0;
        }
        if (this.mBase == null) {
            return changed;
        }
        return changed | this.mBase.setLevel(level);
    }

    public boolean isStateful() {
        if (this.mFading == null || !this.mFading.isStateful()) {
            if (this.mBase == null) {
                return false;
            }
            if (!this.mBase.isStateful()) {
                return false;
            }
        }
        return true;
    }

    public int getAlpha() {
        return this.mAlpha;
    }

    public void setAlpha(int alpha) {
        if (alpha != this.mAlpha) {
            this.mAlpha = alpha;
            invalidateSelf();
        }
    }

    public int getOpacity() {
        int opacity;
        int i = 0;
        if (this.mFading != null) {
            opacity = this.mFading.getOpacity();
        } else {
            opacity = 0;
        }
        if (this.mBase != null) {
            i = this.mBase.getOpacity();
        }
        return resolveOpacity(opacity, i);
    }

    public void invalidateDrawable(Drawable who) {
        if (who != this.mFading && who != this.mBase) {
            return;
        }
        if (getCallback() != null) {
            getCallback().invalidateDrawable(this);
        }
    }

    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        if (who != this.mFading && who != this.mBase) {
            return;
        }
        if (getCallback() != null) {
            getCallback().scheduleDrawable(this, what, when);
        }
    }

    public void unscheduleDrawable(Drawable who, Runnable what) {
        if (who != this.mFading && who != this.mBase) {
            return;
        }
        if (getCallback() != null) {
            getCallback().unscheduleDrawable(this, what);
        }
    }
}
