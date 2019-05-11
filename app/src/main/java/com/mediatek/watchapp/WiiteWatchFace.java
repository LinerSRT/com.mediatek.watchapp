package com.mediatek.watchapp;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

abstract class WiiteWatchFace extends View {
    public WiiteWatchFace(Context context) {
        super(context);
    }

    public WiiteWatchFace(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public WiiteWatchFace(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public WiiteWatchFace(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    abstract void onTouch(float f, float f2);
}
