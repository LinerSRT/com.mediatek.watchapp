package com.mediatek.watchapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.util.Log;

public class ScreenControl {
    private static Context mContext;
    private static ScreenControl mScreenControl = new ScreenControl();
    private String TAG = ScreenControl.class.getSimpleName();
    private PowerManager mPowerManager;

    private ScreenControl() {
    }

    public static ScreenControl getInstance(Context context) {
        mContext = context;
        return mScreenControl;
    }

    protected void ScreenOff() {
        if (getPowerManager().isScreenOn()) {
            //getPowerManager().goToSleep(SystemClock.uptimeMillis());
            @SuppressLint("InvalidWakeLockTag") WakeLock wakeLock = this.mPowerManager.newWakeLock(6, "ScreenOff");
            wakeLock.acquire();
            wakeLock.release();
            Log.d(this.TAG, "ScreenOff");
        }
    }

    @SuppressLint("WrongConstant")
    private PowerManager getPowerManager() {
        if (this.mPowerManager == null) {
            this.mPowerManager = (PowerManager) mContext.getSystemService("power");
        }
        return this.mPowerManager;
    }
}
