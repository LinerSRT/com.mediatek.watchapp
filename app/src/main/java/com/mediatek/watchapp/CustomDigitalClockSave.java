package com.mediatek.watchapp;

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
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import java.util.Calendar;
import java.util.Date;

public class CustomDigitalClockSave extends View {
    private String TAG;
    private boolean is24Hour;
    private Calendar mCal;
    private Time mCalendar;
    private Callback mCallback;
    private boolean mChanged;
    private Context mContext;
    private String mDay;
    private Drawable mDial;
    private int mDialHeight;
    private int mDialWidth;
    private String mHHMM;
    private final Handler mHandler;
    private float mHour;
    private final BroadcastReceiver mIntentReceiver;
    private int mLevel;
    private float mMinutes;
    private Paint mPaint;
    private Paint mPaintCircle;
    private Paint mPaintLevel;
    private Paint mPaintToday;
    private int mScreenH;
    private int mScreenW;
    private float mSecond;
    private Runnable mTicker;
    private boolean mTickerStopped;
    private String mToDay;
    private String mWeek;

    /* renamed from: com.mediatek.watchapp.CustomDigitalClockSave$1 */
    class C01071 extends BroadcastReceiver {
        C01071() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.BATTERY_CHANGED")) {
                CustomDigitalClockSave.this.mLevel = (int) ((((float) intent.getIntExtra("level", 0)) * 100.0f) / ((float) intent.getIntExtra("scale", 100)));
            }
        }
    }

    /* renamed from: com.mediatek.watchapp.CustomDigitalClockSave$2 */
    class C01082 implements Runnable {
        C01082() {
        }

        public void run() {
            if (!CustomDigitalClockSave.this.mTickerStopped) {
                CustomDigitalClockSave.this.onTimeChanged();
                CustomDigitalClockSave.this.invalidate();
                long now = SystemClock.uptimeMillis();
                CustomDigitalClockSave.this.mHandler.postAtTime(CustomDigitalClockSave.this.mTicker, now + (1000 - (now % 1000)));
            }
        }
    }

    public interface Callback {
        void onClick();
    }

    public CustomDigitalClockSave(Context context) {
        this(context, null);
        this.mContext = context;
    }

    public CustomDigitalClockSave(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        this.mContext = context;
    }

    public CustomDigitalClockSave(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.TAG = CustomDigitalClockSave.class.getSimpleName();
        this.mLevel = 0;
        this.mHandler = new Handler();
        this.mTickerStopped = false;
        this.is24Hour = true;
        this.mIntentReceiver = new C01071();
        this.mContext = context;
        Resources r = getContext().getResources();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomClock, defStyle, 0);
        this.mDial = a.getDrawable(0);
        if (this.mDial == null) {
            this.mDial = r.getDrawable(R.drawable.watch_clock_save);
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
        if (this.mCal == null) {
            this.mCal = Calendar.getInstance();
        }
        this.mPaintToday = new Paint();
        this.mPaintToday.setTypeface(Typeface.SANS_SERIF);
        this.mPaintToday.setTextSize(40.0f);
        this.mPaintToday.setColor(-65536);
        this.mPaintToday.setAntiAlias(true);
        this.mPaint = new Paint();
        this.mPaint.setTypeface(Typeface.SANS_SERIF);
        this.mPaint.setTextSize(100.0f);
        this.mPaint.setColor(-1);
        this.mPaint.setAntiAlias(true);
        this.mPaintCircle = new Paint();
        this.mPaintCircle.setTypeface(Typeface.SANS_SERIF);
        this.mPaintCircle.setTextSize(30.0f);
        this.mPaintCircle.setColor(-7829368);
        this.mPaintCircle.setAntiAlias(true);
        this.mPaintLevel = new Paint();
        this.mPaintLevel.setTypeface(Typeface.SANS_SERIF);
        this.mPaintLevel.setTextSize(40.0f);
        this.mPaintLevel.setColor(-1);
        this.mPaintLevel.setAntiAlias(true);
    }

    private void onTimeChanged() {
        this.mCalendar.setToNow();
        this.mCalendar.format("MMMM dd yyyy HH:mm:ss");
        this.mCal.setTime(new Date());
        int hour = this.mCalendar.hour;
        int minute = this.mCalendar.minute;
        int second = this.mCalendar.second;
        this.mDay = String.valueOf(this.mCalendar.year) + "-" + String.valueOf(this.mCalendar.month + 1) + "-" + String.valueOf(this.mCalendar.monthDay);
        this.mToDay = String.valueOf(this.mCalendar.monthDay);
        this.mWeek = getWeek(this.mCalendar.weekDay);
        this.mHHMM = DateFormat.format("HH:mm", this.mCal).toString();
        this.mHour = (((float) hour) + (this.mMinutes / 60.0f)) + (this.mSecond / 3600.0f);
        this.mMinutes = ((float) minute) + (((float) second) / 60.0f);
        this.mSecond = (float) second;
        this.mChanged = true;
        updateContentDescription(this.mCalendar);
    }

    protected void onAttachedToWindow() {
        this.mTickerStopped = false;
        super.onAttachedToWindow();
        DisplayMetrics dm = this.mContext.getResources().getDisplayMetrics();
        this.mScreenW = dm.widthPixels;
        this.mScreenH = dm.heightPixels;
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mTickerStopped = true;
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            this.mTickerStopped = false;
            this.mTicker = new C01082();
            this.mTicker.run();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.BATTERY_CHANGED");
            Intent sticky = getContext().registerReceiver(this.mIntentReceiver, filter);
            if (sticky != null) {
                this.mIntentReceiver.onReceive(getContext(), sticky);
                return;
            }
            return;
        }
        this.mTickerStopped = true;
        getContext().unregisterReceiver(this.mIntentReceiver);
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
        float scale = Math.min(hScale, vScale);
        Log.v("caojinliang", "onMeasure::widthSize=" + widthSize);
        Log.v("caojinliang", "onMeasure::heightSize=" + heightSize);
        Log.v("caojinliang", "onMeasure::scale=" + scale);
        setMeasuredDimension(resolveSizeAndState((int) (((float) this.mDialWidth) * scale), widthMeasureSpec, 0), resolveSizeAndState((int) (((float) this.mDialHeight) * scale), heightMeasureSpec, 0));
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mChanged = true;
    }

    protected void onDraw(Canvas canvas) {
        String[] c;
        String sDot;
        super.onDraw(canvas);
        this.is24Hour = DateFormat.is24HourFormat(getContext());
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
            int w = dial.getIntrinsicWidth();
            int h = dial.getIntrinsicHeight();
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
        canvas.save();
        String textToDraw = this.mLevel + "%";
        canvas.drawText(textToDraw, (((float) this.mScreenW) - this.mPaintLevel.measureText(textToDraw)) / 2.0f, ((((float) (this.mScreenH / 2)) - (this.mPaintLevel.getFontMetrics().descent - this.mPaintLevel.getFontMetrics().ascent)) / 2.0f) + 30.0f, this.mPaintLevel);
        String sHour = String.valueOf(this.mCalendar.hour);
        String[] b = new String[]{"00", "01", "02", "03", "04", "05", "06", "07", "08", "09"};
        if (!this.is24Hour) {
            int i = this.mCalendar.hour;
            if (i < 0 || i > 12) {
                int j = i % 12;
                c = new String[]{"01", "02", "03", "04", "05", "06", "07", "08", "09"};
                if (j <= 9) {
                    sHour = c[j - 1];
                } else {
                    sHour = String.valueOf(j);
                }
            } else if (this.mCalendar.hour < 10) {
                sHour = b[this.mCalendar.hour];
            } else {
                sHour = String.valueOf(this.mCalendar.hour);
            }
        } else if (this.mCalendar.hour < 10) {
            sHour = b[this.mCalendar.hour];
        } else {
            sHour = String.valueOf(this.mCalendar.hour);
        }
        if (this.mCalendar.second % 2 == 0) {
            sDot = ":";
        } else {
            sDot = " ";
        }
        String sMinute = String.valueOf(this.mCalendar.minute);
        c = new String[]{"00", "01", "02", "03", "04", "05", "06", "07", "08", "09"};
        if (this.mCalendar.minute < 10) {
            sMinute = c[this.mCalendar.minute];
        } else {
            sMinute = String.valueOf(this.mCalendar.minute);
        }
        textToDraw = sHour + sDot + sMinute;
        float textToDraw_w = this.mPaint.measureText(textToDraw);
        float textToDraw_h = this.mPaint.getFontMetrics().descent - this.mPaintLevel.getFontMetrics().ascent;
        Log.v("caojinliang", "(mScreenH - textToDraw_h)/2=" + ((((float) this.mScreenH) - textToDraw_h) / 2.0f));
        Log.v("caojinliang", "textToDraw_h=" + textToDraw_h);
        canvas.drawText(textToDraw, (((float) this.mScreenW) - textToDraw_w) / 2.0f, ((((float) this.mScreenH) - textToDraw_h) / 2.0f) + 70.0f, this.mPaint);
        String sMonth = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"}[this.mCalendar.month];
        String week = new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"}[this.mCalendar.weekDay];
    }

    private void updateContentDescription(Time time) {
        setContentDescription(DateUtils.formatDateTime(getContext(), time.toMillis(false), 129));
    }

    private String getWeek(int week) {
        switch (week) {
            case 0:
                return getContext().getString(R.string.sunday_abbre);
            case 1:
                return getContext().getString(R.string.monday_abbre);
            case 2:
                return getContext().getString(R.string.tuesday_abbre);
            case 3:
                return getContext().getString(R.string.wednesday_abbre);
            case 4:
                return getContext().getString(R.string.thursday_abbre);
            case 5:
                return getContext().getString(R.string.friday_abbre);
            case 6:
                return getContext().getString(R.string.saturday_abbre);
            default:
                return "";
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (event.getAction() == 0 && distance(x, y, 200.0f, 319.0f) <= 35.0f && this.mCallback != null) {
            this.mCallback.onClick();
        }
        return true;
    }

    public void setClickCallback(Callback callback) {
        this.mCallback = callback;
    }

    private static float distance(float x1, float y1, float x2, float y2) {
        float x = x2 - x1;
        float y = y2 - y1;
        return (float) Math.sqrt((double) ((x * x) + (y * y)));
    }
}
