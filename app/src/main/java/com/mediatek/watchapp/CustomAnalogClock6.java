package com.mediatek.watchapp;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;

public class CustomAnalogClock6 extends View {
    private boolean ShowDate;
    private Drawable mBatteryLe;
    private int mBatteryLevel;
    private Time mCalendar;
    private boolean mChanged;
    private String mDay;
    private Drawable mDial;
    private int mDialHeight;
    private int mDialWidth;
    private final Handler mHandler;
    private float mHour;
    private Drawable mHourHand;
    private Drawable mMinuteHand;
    private float mMinutes;
    private Paint mPaintToday;
    private final Receiver mReceiver;
    private float mSecond;
    private Drawable mSecondHand;
    private Runnable mTicker;
    private boolean mTickerStopped;
    private String mToDay;

    /* renamed from: com.mediatek.watchapp.CustomAnalogClock6$1 */
    class C01061 implements Runnable {
        C01061() {
        }

        public void run() {
            if (!CustomAnalogClock6.this.mTickerStopped) {
                CustomAnalogClock6.this.onTimeChanged();
                CustomAnalogClock6.this.invalidate();
                long now = SystemClock.uptimeMillis();
                CustomAnalogClock6.this.mHandler.postAtTime(CustomAnalogClock6.this.mTicker, now + (100 - (now % 100)));
            }
        }
    }

    private final class Receiver extends BroadcastReceiver {
        private Receiver() {
        }

        public void onReceive(Context context, Intent intent) {
            Log.i("TAG", "onReceive");
            if (intent.getAction().equals("android.intent.action.BATTERY_CHANGED")) {
                CustomAnalogClock6.this.mBatteryLevel = intent.getIntExtra("level", -1);
            }
        }
    }

    public CustomAnalogClock6(Context context) {
        this(context, null);
    }

    public CustomAnalogClock6(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressLint({"ResourceAsColor"})
    public CustomAnalogClock6(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mHandler = new Handler();
        this.mBatteryLevel = -1;
        this.mReceiver = new Receiver();
        this.mTickerStopped = false;
        Resources r = getContext().getResources();
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.CustomClock, defStyle, 0);
        this.mDial = a.getDrawable(0);
        this.mHourHand = a.getDrawable(1);
        this.mMinuteHand = a.getDrawable(2);
        this.ShowDate = a.getBoolean(8, false);
        if (this.mDial == null) {
            this.mDial = r.getDrawable(R.drawable.watch_biaopan);
        }
        if (this.mHourHand == null) {
            this.mHourHand = r.getDrawable(R.drawable.watch_shizhen);
        }
        if (this.mMinuteHand == null) {
            this.mMinuteHand = r.getDrawable(R.drawable.watch_fenzhen);
        }
        if (this.mSecondHand == null) {
            this.mSecondHand = r.getDrawable(R.drawable.watch_miaozhen);
        }
        a.recycle();
        if (this.mDial != null) {
            this.mDialWidth = this.mDial.getIntrinsicWidth();
            this.mDialHeight = this.mDial.getIntrinsicHeight();
        } else {
            this.mDialWidth = 1;
            this.mDialHeight = 1;
        }
        if (this.mCalendar == null) {
            this.mCalendar = new Time();
        }
        this.mPaintToday = new Paint();
        this.mPaintToday.setTypeface(Typeface.create(Typeface.SANS_SERIF, 1));
        this.mPaintToday.setTextSize(25.0f);
        this.mPaintToday.setColor(-1);
        this.mPaintToday.setAntiAlias(true);
    }

    private void onTimeChanged() {
        this.mCalendar.setToNow();
        int hour = this.mCalendar.hour;
        int minute = this.mCalendar.minute;
        int second = this.mCalendar.second;
        this.mDay = String.valueOf(this.mCalendar.year) + "-" + String.valueOf(this.mCalendar.month + 1) + "-" + String.valueOf(this.mCalendar.monthDay);
        this.mToDay = String.valueOf(this.mCalendar.monthDay);
        this.mSecond = (float) second;
        this.mMinutes = ((float) minute) + (((float) second) / 60.0f);
        this.mHour = ((float) hour) + (this.mMinutes / 60.0f);
        this.mChanged = true;
        updateContentDescription(this.mCalendar);
    }

    protected void onAttachedToWindow() {
        this.mTickerStopped = false;
        super.onAttachedToWindow();
        Log.i("TAG", "onAttachedToWindow");
        this.mTicker = new C01061();
        this.mTicker.run();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BATTERY_CHANGED");
        getContext().registerReceiver(this.mReceiver, filter);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mTickerStopped = true;
        getContext().unregisterReceiver(this.mReceiver);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        float hScale = 1.0f;
        float vScale = 1.0f;
        if (widthMode != 0 && widthSize < this.mDialWidth) {
            hScale = ((float) widthSize) / ((float) this.mDialWidth);
        }
        if (heightMode != 0 && heightSize < this.mDialHeight) {
            vScale = ((float) heightSize) / ((float) this.mDialHeight);
        }
        float scale = Math.max(hScale, vScale);
        setMeasuredDimension(resolveSizeAndState((int) (((float) this.mDialWidth) * scale), widthMeasureSpec, 0), resolveSizeAndState((int) (((float) this.mDialHeight) * scale), heightMeasureSpec, 0));
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mChanged = true;
    }

    protected void onDraw(Canvas canvas) {
        int w;
        int h;
        super.onDraw(canvas);
        boolean changed = this.mChanged;
        if (changed) {
            this.mChanged = false;
        }
        int availableWidth = getRight() - getLeft();
        int availableHeight = getBottom() - getTop();
        int x = availableWidth / 2;
        int y = availableHeight / 2;
        Drawable dial = this.mDial;
        if (dial != null) {
            w = dial.getIntrinsicWidth();
            h = dial.getIntrinsicHeight();
            int dialWidth = w;
            int dialHeight = h;
            float scale;
            if (availableWidth < w || availableHeight < h) {
                scale = Math.min(((float) availableWidth) / ((float) w), ((float) availableHeight) / ((float) h));
                canvas.save();
                canvas.scale(scale, scale, (float) x, (float) y);
            } else if (availableWidth > w && availableHeight > h) {
                scale = Math.min(((float) availableWidth) / ((float) w), ((float) availableHeight) / ((float) h));
                canvas.save();
                canvas.scale(scale, scale, (float) x, (float) y);
            }
            if (changed) {
                dial.setBounds(x - (w / 2), y - (h / 2), (w / 2) + x, (h / 2) + y);
            }
            dial.draw(canvas);
        }
        if (this.mBatteryLe != null) {
            canvas.save();
            if (changed) {
                Drawable Battery = this.mBatteryLe;
                w = Battery.getIntrinsicWidth();
                h = Battery.getIntrinsicHeight();
                Battery.setBounds(((availableWidth * 7) / 10) - (w / 2), y - (h / 2), ((availableWidth * 7) / 10) + (w / 2), (h / 2) + y);
                Battery.draw(canvas);
                String battery = String.valueOf(this.mBatteryLevel) + "%";
                if (this.mBatteryLevel == -1) {
                    battery = "unknown";
                }
                canvas.drawText(battery, (float) (((availableWidth * 7) / 10) - (((battery.length() * 10) + ((battery.length() - 1) * 2)) / 2)), (float) ((availableHeight / 2) + 6), this.mPaintToday);
            }
            canvas.restore();
        }
        canvas.save();
        canvas.rotate((this.mHour / 12.0f) * 360.0f, (float) x, (float) y);
        Drawable hourHand = this.mHourHand;
        if (changed) {
            w = hourHand.getIntrinsicWidth();
            h = hourHand.getIntrinsicHeight();
            hourHand.setBounds(x - (w / 2), y - (h / 2), (w / 2) + x, (h / 2) + y);
        }
        hourHand.draw(canvas);
        canvas.restore();
        canvas.save();
        canvas.rotate((this.mMinutes / 60.0f) * 360.0f, (float) x, (float) y);
        Drawable minuteHand = this.mMinuteHand;
        if (changed) {
            w = minuteHand.getIntrinsicWidth();
            h = minuteHand.getIntrinsicHeight();
            minuteHand.setBounds(x - (w / 2), y - (h / 2), (w / 2) + x, (h / 2) + y);
        }
        minuteHand.draw(canvas);
        canvas.restore();
        if (this.mSecondHand != null) {
            canvas.save();
            canvas.rotate((this.mSecond / 60.0f) * 360.0f, (float) x, (float) y);
            Drawable secondHand = this.mSecondHand;
            if (changed) {
                w = secondHand.getIntrinsicWidth();
                h = secondHand.getIntrinsicHeight();
                secondHand.setBounds(x - (w / 2), (y - (h / 2)) + 10, (w / 2) + x, ((h / 2) + y) - 10);
            }
            secondHand.draw(canvas);
            canvas.restore();
        }
    }

    private void updateContentDescription(Time time) {
        setContentDescription(DateUtils.formatDateTime(getContext(), time.toMillis(false), 129));
    }
}
