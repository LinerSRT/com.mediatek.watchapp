package com.mediatek.watchapp.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;
import com.mediatek.watchapp.R;

public class RoundImageView extends ImageView {
    private Paint mBitmapPaint = new Paint();
    private BitmapShader mBitmapShader;
    private int mBorderRadius;
    private Context mContex;
    private Matrix mMatrix = new Matrix();
    private int mRadius;
    private RectF mRoundRect;
    private int mWidth;
    private int type;

    @SuppressLint("ResourceType")
    public RoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContex = context;
        this.mBitmapPaint.setAntiAlias(true);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundImageView);
        this.mBorderRadius = a.getDimensionPixelSize(0, (int) TypedValue.applyDimension(1, 10.0f, getResources().getDisplayMetrics()));
        this.type = a.getInt(1, 0);
        a.recycle();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.type == 0) {
            this.mWidth = Math.min(getMeasuredWidth(), getMeasuredHeight());
            this.mWidth = (int) this.mContex.getResources().getDimension(R.dimen.roundlist_app_icon_size);
            this.mRadius = this.mWidth / 2;
            setMeasuredDimension(this.mWidth, this.mWidth);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void setUpShader() {
        Drawable drawable = getDrawable();
        if (drawable != null) {
            Bitmap bmp = drawableToBitamp(drawable);
            this.mBitmapShader = new BitmapShader(bmp, TileMode.CLAMP, TileMode.CLAMP);
            float scale = 1.0f;
            if (this.type == 0) {
                scale = (((float) this.mWidth) * 1.0f) / ((float) Math.min(bmp.getWidth(), bmp.getHeight()));
            } else if (this.type == 1) {
                scale = Math.max((((float) getWidth()) * 1.0f) / ((float) bmp.getWidth()), (((float) getHeight()) * 1.0f) / ((float) bmp.getHeight()));
            }
            this.mMatrix.setScale(scale, scale);
            this.mBitmapShader.setLocalMatrix(this.mMatrix);
            this.mBitmapPaint.setShader(this.mBitmapShader);
        }
    }

    private Bitmap drawableToBitamp(Drawable drawable) {
        Bitmap bitmap;
        int w;
        int h;
        Canvas canvas;
        if (drawable instanceof BitmapDrawable) {
            w = drawable.getIntrinsicWidth();
            h = drawable.getIntrinsicHeight();
            bitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
            canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, w, h);
            drawable.draw(canvas);
        } else {
            w = drawable.getIntrinsicWidth();
            h = drawable.getIntrinsicHeight();
            bitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
            canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, w, h);
            drawable.draw(canvas);
        }
        return bitmap;
    }

    protected void onDraw(Canvas canvas) {
        if (getDrawable() != null) {
            setUpShader();
            if (this.type == 1) {
                canvas.drawRoundRect(this.mRoundRect, (float) this.mBorderRadius, (float) this.mBorderRadius, this.mBitmapPaint);
            } else {
                canvas.drawCircle((float) this.mRadius, (float) this.mRadius, (float) this.mRadius, this.mBitmapPaint);
            }
        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (this.type == 1) {
            this.mRoundRect = new RectF(0.0f, 0.0f, (float) getWidth(), (float) getHeight());
        }
    }

    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("state_instance", super.onSaveInstanceState());
        bundle.putInt("state_type", this.type);
        bundle.putInt("state_border_radius", this.mBorderRadius);
        return bundle;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            super.onRestoreInstanceState(((Bundle) state).getParcelable("state_instance"));
            this.type = bundle.getInt("state_type");
            this.mBorderRadius = bundle.getInt("state_border_radius");
            return;
        }
        super.onRestoreInstanceState(state);
    }
}
