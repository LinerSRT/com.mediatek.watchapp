package com.mediatek.watchapp;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import com.mediatek.watchapp.WatchApp.installedClock;
import com.mediatek.watchapp.view.ClockInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class MyAnalogClock3 extends View {
    private int SCREEN_WIDE = 400;
    private int centerX1;
    private int centerY1;
    List<ClockInfo> clockInfos = null;
    private int hour;
    private Time mCalendar;
    private boolean mChanged;
    private ClockSkinConfigure mClockSkinConfigure = new ClockSkinConfigure();
    private String mClockskinPath = null;
    private Context mContext;
    private int mDate;
    private int mDialHeight;
    private int mDialWidth;
    private Drawable mDrawBattery;
    private Drawable mDrawBatteryGray;
    private final Handler mHandler = new Handler();
    private float mHour;
    private float mMinutes;
    private int mMonth;
    Rect mRect = new Rect();
    RectF mRectF = new RectF();
    private float mSecond;
    private int mSecondHandDuring = 50;
    private int mStep = 0;
    private Runnable mTicker;
    private boolean mTickerStopped = false;
    private int mWeek;
    private int maxWidth = 0;
    private int minute;
    private Calendar moonPhaseCalendar;
    private int moonPhaseType = -1;
    private List<ClockInfo> parseClock;
    private String pkg;
    /* renamed from: r */
    private Resources f4r;
    private int second;

    /* renamed from: com.mediatek.watchapp.MyAnalogClock3$1 */
    class C01311 implements Runnable {
        C01311() {
        }

        public void run() {
            if (!MyAnalogClock3.this.mTickerStopped) {
                MyAnalogClock3.this.onTimeChanged();
                MyAnalogClock3.this.invalidate();
                long now = SystemClock.uptimeMillis();
                MyAnalogClock3.this.mHandler.postAtTime(MyAnalogClock3.this.mTicker, now + (((long) MyAnalogClock3.this.mSecondHandDuring) - (now % ((long) MyAnalogClock3.this.mSecondHandDuring))));
            }
        }
    }

    public class ClockSkinConfigure {
        private boolean SmoothRun = true;

        public void setSmoothRun(boolean smoothRun) {
            this.SmoothRun = smoothRun;
        }
    }

    public MyAnalogClock3(Context context, String clockskinPath) {
        super(context);
        this.mContext = context;
        this.mClockskinPath = clockskinPath;
        init();
    }

    public void init() {
        this.pkg = get_cur_theme_package(this.mContext);
        this.f4r = getResource(this.mContext, this.pkg);
        this.parseClock = parseClockSinXML(this.f4r, this.pkg, "clock_skin");
        Log.d("qs_ClockInfo", "parseClock-" + this.parseClock.size());
        this.mDialWidth = this.SCREEN_WIDE;
        this.mDialHeight = this.SCREEN_WIDE;
        if (this.mCalendar == null) {
            this.mCalendar = new Time();
        }
        this.mDrawBattery = getContext().getResources().getDrawable(R.drawable.clock_battery_panel);
        this.mDrawBatteryGray = getContext().getResources().getDrawable(R.drawable.clock_battery_panel_gray);
    }

    private XmlResourceParser getXmlRes(Resources r, String pkg, String id_name) {
        int ID = r.getIdentifier(id_name, "xml", pkg);
        if (ID != 0) {
            return r.getXml(ID);
        }
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void parseXML(android.content.res.Resources r12, java.lang.String r13, java.lang.String r14, com.mediatek.watchapp.view.ClockInfo r15) {
        /*
        r11 = this;
        r0 = 0;
        r6 = new java.util.ArrayList;
        r6.<init>();
        r8 = r11.mClockskinPath;	 Catch:{ XmlPullParserException -> 0x008c, IOException -> 0x00c4 }
        if (r8 != 0) goto L_0x0020;
    L_0x000a:
        r7 = r11.getXmlRes(r12, r13, r14);	 Catch:{ XmlPullParserException -> 0x008c, IOException -> 0x00c4 }
    L_0x000e:
        r5 = r7.getEventType();	 Catch:{ XmlPullParserException -> 0x008c, IOException -> 0x00c4 }
        r1 = r0;
    L_0x0013:
        r8 = 1;
        if (r5 == r8) goto L_0x00c2;
    L_0x0016:
        switch(r5) {
            case 0: goto L_0x0030;
            case 1: goto L_0x00b6;
            case 2: goto L_0x003b;
            case 3: goto L_0x0097;
            default: goto L_0x0019;
        };	 Catch:{ XmlPullParserException -> 0x008c, IOException -> 0x00c4 }
    L_0x0019:
        r0 = r1;
    L_0x001a:
        r5 = r7.next();	 Catch:{ XmlPullParserException -> 0x008c, IOException -> 0x00c4 }
        r1 = r0;
        goto L_0x0013;
    L_0x0020:
        r7 = r11.getXmlParser(r14);	 Catch:{ XmlPullParserException -> 0x008c, IOException -> 0x00c4 }
        if (r7 != 0) goto L_0x000e;
    L_0x0026:
        r8 = "xiaocai";
        r9 = "parser == null";
        android.util.Log.e(r8, r9);	 Catch:{ XmlPullParserException -> 0x008c, IOException -> 0x00c4 }
        return;
    L_0x0030:
        r8 = "qs_parseXML";
        r9 = "eventType-0";
        android.util.Log.d(r8, r9);	 Catch:{ XmlPullParserException -> 0x00cf, IOException -> 0x00d2 }
        r0 = r1;
        goto L_0x001a;
    L_0x003b:
        r0 = new com.mediatek.watchapp.view.ClockInfo$Num;	 Catch:{ XmlPullParserException -> 0x00cf, IOException -> 0x00d2 }
        r8 = new com.mediatek.watchapp.view.ClockInfo;	 Catch:{ XmlPullParserException -> 0x00cf, IOException -> 0x00d2 }
        r8.<init>();	 Catch:{ XmlPullParserException -> 0x00cf, IOException -> 0x00d2 }
        r8.getClass();	 Catch:{ XmlPullParserException -> 0x00cf, IOException -> 0x00d2 }
        r0.<init>();	 Catch:{ XmlPullParserException -> 0x00cf, IOException -> 0x00d2 }
        r8 = "qs_parseXML";
        r9 = "eventType-2";
        android.util.Log.d(r8, r9);	 Catch:{ XmlPullParserException -> 0x008c, IOException -> 0x00c4 }
        r8 = r7.getName();	 Catch:{ XmlPullParserException -> 0x008c, IOException -> 0x00c4 }
        r9 = "image";
        r8 = r8.equals(r9);	 Catch:{ XmlPullParserException -> 0x008c, IOException -> 0x00c4 }
        if (r8 == 0) goto L_0x001a;
    L_0x005e:
        r5 = r7.next();	 Catch:{ XmlPullParserException -> 0x008c, IOException -> 0x00c4 }
        r8 = r7.getText();	 Catch:{ XmlPullParserException -> 0x008c, IOException -> 0x00c4 }
        r2 = r11.getDrawableRes(r12, r13, r8);	 Catch:{ XmlPullParserException -> 0x008c, IOException -> 0x00c4 }
        r0.setNumDrawable(r2);	 Catch:{ XmlPullParserException -> 0x008c, IOException -> 0x00c4 }
        r8 = "qs_parseXML";
        r9 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x008c, IOException -> 0x00c4 }
        r9.<init>();	 Catch:{ XmlPullParserException -> 0x008c, IOException -> 0x00c4 }
        r10 = "NumDrawable-";
        r9 = r9.append(r10);	 Catch:{ XmlPullParserException -> 0x008c, IOException -> 0x00c4 }
        r10 = r7.getText();	 Catch:{ XmlPullParserException -> 0x008c, IOException -> 0x00c4 }
        r9 = r9.append(r10);	 Catch:{ XmlPullParserException -> 0x008c, IOException -> 0x00c4 }
        r9 = r9.toString();	 Catch:{ XmlPullParserException -> 0x008c, IOException -> 0x00c4 }
        android.util.Log.d(r8, r9);	 Catch:{ XmlPullParserException -> 0x008c, IOException -> 0x00c4 }
        goto L_0x001a;
    L_0x008c:
        r4 = move-exception;
    L_0x008d:
        r8 = "MyAnalogClock3";
        r9 = "Got XmlPullParserException while parsing toppackage.";
        android.util.Log.w(r8, r9, r4);
    L_0x0096:
        return;
    L_0x0097:
        r8 = "qs_parseXML";
        r9 = "eventType-3";
        android.util.Log.d(r8, r9);	 Catch:{ XmlPullParserException -> 0x00cf, IOException -> 0x00d2 }
        r8 = r7.getName();	 Catch:{ XmlPullParserException -> 0x00cf, IOException -> 0x00d2 }
        r9 = "image";
        r8 = r8.equals(r9);	 Catch:{ XmlPullParserException -> 0x00cf, IOException -> 0x00d2 }
        if (r8 == 0) goto L_0x00b3;
    L_0x00ad:
        r6.add(r1);	 Catch:{ XmlPullParserException -> 0x00cf, IOException -> 0x00d2 }
        r15.setNums(r6);	 Catch:{ XmlPullParserException -> 0x00cf, IOException -> 0x00d2 }
    L_0x00b3:
        r0 = r1;
        goto L_0x001a;
    L_0x00b6:
        r8 = "qs_parseXML";
        r9 = "eventType-1";
        android.util.Log.d(r8, r9);	 Catch:{ XmlPullParserException -> 0x00cf, IOException -> 0x00d2 }
        r0 = r1;
        goto L_0x001a;
    L_0x00c2:
        r0 = r1;
        goto L_0x0096;
    L_0x00c4:
        r3 = move-exception;
    L_0x00c5:
        r8 = "MyAnalogClock3";
        r9 = "Got IOException while parsing toppackage.";
        android.util.Log.w(r8, r9, r3);
        goto L_0x0096;
    L_0x00cf:
        r4 = move-exception;
        r0 = r1;
        goto L_0x008d;
    L_0x00d2:
        r3 = move-exception;
        r0 = r1;
        goto L_0x00c5;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.watchapp.MyAnalogClock3.parseXML(android.content.res.Resources, java.lang.String, java.lang.String, com.mediatek.watchapp.view.ClockInfo):void");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.util.List<com.mediatek.watchapp.view.ClockInfo> parseClockSinXML(android.content.res.Resources r18, java.lang.String r19, java.lang.String r20) {
        /*
        r17 = this;
        r4 = 0;
        r0 = r17;
        r14 = r0.mClockskinPath;	 Catch:{ XmlPullParserException -> 0x04c6, IOException -> 0x04bb }
        if (r14 != 0) goto L_0x001d;
    L_0x0007:
        r12 = r17.getXmlRes(r18, r19, r20);	 Catch:{ XmlPullParserException -> 0x04c6, IOException -> 0x04bb }
    L_0x000b:
        r9 = r12.getEventType();	 Catch:{ XmlPullParserException -> 0x04c6, IOException -> 0x04bb }
        r5 = r4;
    L_0x0010:
        r14 = 1;
        if (r9 == r14) goto L_0x04b5;
    L_0x0013:
        switch(r9) {
            case 0: goto L_0x0026;
            case 1: goto L_0x04a9;
            case 2: goto L_0x003a;
            case 3: goto L_0x0489;
            default: goto L_0x0016;
        };	 Catch:{ XmlPullParserException -> 0x04c6, IOException -> 0x04bb }
    L_0x0016:
        r4 = r5;
    L_0x0017:
        r9 = r12.next();	 Catch:{ XmlPullParserException -> 0x04c6, IOException -> 0x04bb }
        r5 = r4;
        goto L_0x0010;
    L_0x001d:
        r0 = r17;
        r1 = r20;
        r12 = r0.getXmlParser(r1);	 Catch:{ XmlPullParserException -> 0x04c6, IOException -> 0x04bb }
        goto L_0x000b;
    L_0x0026:
        r14 = "qs_parseClockSinXML";
        r15 = "eventType-0";
        android.util.Log.d(r14, r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = new java.util.ArrayList;	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14.<init>();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r0 = r17;
        r0.clockInfos = r14;	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r4 = r5;
        goto L_0x0017;
    L_0x003a:
        r14 = "qs_parseClockSinXML";
        r15 = "eventType-2";
        android.util.Log.d(r14, r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = r12.getName();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = "drawable";
        r14 = r14.equals(r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        if (r14 == 0) goto L_0x0056;
    L_0x0050:
        r4 = new com.mediatek.watchapp.view.ClockInfo;	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r4.<init>();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        goto L_0x0017;
    L_0x0056:
        r14 = r12.getName();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = "configure";
        r14 = r14.equals(r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        if (r14 != 0) goto L_0x0016;
    L_0x0063:
        r14 = r12.getName();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = "smoothRun";
        r14 = r14.equals(r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        if (r14 == 0) goto L_0x00b7;
    L_0x0070:
        r9 = r12.next();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = r12.getText();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = "false";
        r14 = r14.equals(r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        if (r14 == 0) goto L_0x00b2;
    L_0x0081:
        r13 = 0;
    L_0x0082:
        r0 = r17;
        r14 = r0.mClockSkinConfigure;	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14.setSmoothRun(r13);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        if (r13 == 0) goto L_0x00b4;
    L_0x008b:
        r14 = 50;
    L_0x008d:
        r0 = r17;
        r0.mSecondHandDuring = r14;	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = "qs_parseClockSinXML";
        r15 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15.<init>();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = "smooth-";
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = r12.getText();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.toString();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        android.util.Log.d(r14, r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r4 = r5;
        goto L_0x0017;
    L_0x00b2:
        r13 = 1;
        goto L_0x0082;
    L_0x00b4:
        r14 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
        goto L_0x008d;
    L_0x00b7:
        r14 = r12.getName();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = "name";
        r14 = r14.equals(r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        if (r14 == 0) goto L_0x0132;
    L_0x00c4:
        r9 = r12.next();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r10 = r12.getText();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = "qs_parseClockSinXML";
        r15 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15.<init>();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = "name-";
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.append(r10);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.toString();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        android.util.Log.d(r14, r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r5.setName(r10);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = ".xml";
        r14 = r10.endsWith(r14);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        if (r14 == 0) goto L_0x0122;
    L_0x00f2:
        r14 = "\\.";
        r3 = r10.split(r14);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = 0;
        r11 = r3[r14];	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = "qs_parseClockSinXML";
        r15 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15.<init>();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = "namexml-";
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.append(r11);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.toString();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        android.util.Log.d(r14, r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r0 = r17;
        r1 = r18;
        r2 = r19;
        r0.parseXML(r1, r2, r11, r5);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r4 = r5;
        goto L_0x0017;
    L_0x0122:
        r0 = r17;
        r1 = r18;
        r2 = r19;
        r6 = r0.getDrawableRes(r1, r2, r10);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r5.setNamepng(r6);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r4 = r5;
        goto L_0x0017;
    L_0x0132:
        r14 = r12.getName();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = "centerX";
        r14 = r14.equals(r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        if (r14 == 0) goto L_0x016b;
    L_0x013f:
        r9 = r12.next();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = r12.getText();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r5.setCenterX(r14);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = "qs_parseClockSinXML";
        r15 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15.<init>();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = "CenterX-";
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = r12.getText();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.toString();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        android.util.Log.d(r14, r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r4 = r5;
        goto L_0x0017;
    L_0x016b:
        r14 = r12.getName();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = "centerY";
        r14 = r14.equals(r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        if (r14 == 0) goto L_0x01a4;
    L_0x0178:
        r9 = r12.next();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = r12.getText();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r5.setCenterY(r14);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = "qs_parseClockSinXML";
        r15 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15.<init>();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = "CenterY-";
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = r12.getText();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.toString();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        android.util.Log.d(r14, r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r4 = r5;
        goto L_0x0017;
    L_0x01a4:
        r14 = r12.getName();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = "rotate";
        r14 = r14.equals(r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        if (r14 == 0) goto L_0x01dd;
    L_0x01b1:
        r9 = r12.next();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = r12.getText();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r5.setRotate(r14);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = "qs_parseClockSinXML";
        r15 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15.<init>();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = "rotate-";
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = r12.getText();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.toString();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        android.util.Log.d(r14, r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r4 = r5;
        goto L_0x0017;
    L_0x01dd:
        r14 = r12.getName();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = "angle";
        r14 = r14.equals(r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        if (r14 == 0) goto L_0x0216;
    L_0x01ea:
        r9 = r12.next();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = r12.getText();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r5.setAngle(r14);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = "qs_parseClockSinXML";
        r15 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15.<init>();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = "angle-";
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = r12.getText();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.toString();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        android.util.Log.d(r14, r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r4 = r5;
        goto L_0x0017;
    L_0x0216:
        r14 = r12.getName();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = "arraytype";
        r14 = r14.equals(r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        if (r14 == 0) goto L_0x024f;
    L_0x0223:
        r9 = r12.next();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = r12.getText();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r5.setArraytype(r14);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = "qs_parseClockSinXML";
        r15 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15.<init>();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = "Arraytype-";
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = r12.getText();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.toString();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        android.util.Log.d(r14, r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r4 = r5;
        goto L_0x0017;
    L_0x024f:
        r14 = r12.getName();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = "mulrotate";
        r14 = r14.equals(r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        if (r14 == 0) goto L_0x0288;
    L_0x025c:
        r9 = r12.next();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = r12.getText();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r5.setMulrotate(r14);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = "qs_parseClockSinXML";
        r15 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15.<init>();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = "mulrotate-";
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = r12.getText();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.toString();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        android.util.Log.d(r14, r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r4 = r5;
        goto L_0x0017;
    L_0x0288:
        r14 = r12.getName();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = "startAngle";
        r14 = r14.equals(r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        if (r14 == 0) goto L_0x02c1;
    L_0x0295:
        r9 = r12.next();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = r12.getText();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r5.setStartAngle(r14);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = "qs_parseClockSinXML";
        r15 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15.<init>();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = "startAngle-";
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = r12.getText();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.toString();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        android.util.Log.d(r14, r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r4 = r5;
        goto L_0x0017;
    L_0x02c1:
        r14 = r12.getName();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = "direction";
        r14 = r14.equals(r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        if (r14 == 0) goto L_0x02fa;
    L_0x02ce:
        r9 = r12.next();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = r12.getText();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r5.setDirection(r14);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = "qs_parseClockSinXML";
        r15 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15.<init>();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = "direction-";
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = r12.getText();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.toString();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        android.util.Log.d(r14, r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r4 = r5;
        goto L_0x0017;
    L_0x02fa:
        r14 = r12.getName();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = "textsize";
        r14 = r14.equals(r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        if (r14 == 0) goto L_0x0333;
    L_0x0307:
        r9 = r12.next();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = r12.getText();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r5.setTextsize(r14);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = "qs_parseClockSinXML";
        r15 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15.<init>();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = "textsize-";
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = r12.getText();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.toString();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        android.util.Log.d(r14, r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r4 = r5;
        goto L_0x0017;
    L_0x0333:
        r14 = r12.getName();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = "textcolor";
        r14 = r14.equals(r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        if (r14 == 0) goto L_0x036c;
    L_0x0340:
        r9 = r12.next();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = r12.getText();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r5.setTextcolor(r14);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = "qs_parseClockSinXML";
        r15 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15.<init>();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = "textcolor-";
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = r12.getText();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.toString();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        android.util.Log.d(r14, r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r4 = r5;
        goto L_0x0017;
    L_0x036c:
        r14 = r12.getName();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = "colorarray";
        r14 = r14.equals(r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        if (r14 == 0) goto L_0x03a5;
    L_0x0379:
        r9 = r12.next();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = r12.getText();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r5.setColorArray(r14);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = "qs_parseClockSinXML";
        r15 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15.<init>();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = "colorarray-";
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = r12.getText();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.toString();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        android.util.Log.d(r14, r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r4 = r5;
        goto L_0x0017;
    L_0x03a5:
        r14 = r12.getName();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = "color";
        r14 = r14.equals(r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        if (r14 == 0) goto L_0x03de;
    L_0x03b2:
        r9 = r12.next();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = r12.getText();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r5.setColor(r14);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = "qs_parseClockSinXML";
        r15 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15.<init>();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = "color-";
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = r12.getText();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.toString();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        android.util.Log.d(r14, r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r4 = r5;
        goto L_0x0017;
    L_0x03de:
        r14 = r12.getName();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = "width";
        r14 = r14.equals(r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        if (r14 == 0) goto L_0x0417;
    L_0x03eb:
        r9 = r12.next();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = r12.getText();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r5.setWidth(r14);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = "qs_parseClockSinXML";
        r15 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15.<init>();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = "width-";
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = r12.getText();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.toString();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        android.util.Log.d(r14, r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r4 = r5;
        goto L_0x0017;
    L_0x0417:
        r14 = r12.getName();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = "radius";
        r14 = r14.equals(r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        if (r14 == 0) goto L_0x0450;
    L_0x0424:
        r9 = r12.next();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = r12.getText();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r5.setRadius(r14);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = "qs_parseClockSinXML";
        r15 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15.<init>();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = "radius-";
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = r12.getText();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.toString();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        android.util.Log.d(r14, r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r4 = r5;
        goto L_0x0017;
    L_0x0450:
        r14 = r12.getName();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = "rotatemode";
        r14 = r14.equals(r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        if (r14 == 0) goto L_0x0016;
    L_0x045d:
        r9 = r12.next();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = r12.getText();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r5.setRotatemode(r14);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = "qs_parseClockSinXML";
        r15 = new java.lang.StringBuilder;	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15.<init>();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = "radius-";
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r16 = r12.getText();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.append(r16);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = r15.toString();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        android.util.Log.d(r14, r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r4 = r5;
        goto L_0x0017;
    L_0x0489:
        r14 = "qs_parseClockSinXML";
        r15 = "eventType-3";
        android.util.Log.d(r14, r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14 = r12.getName();	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r15 = "drawable";
        r14 = r14.equals(r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        if (r14 == 0) goto L_0x0016;
    L_0x049f:
        r0 = r17;
        r14 = r0.clockInfos;	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r14.add(r5);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r4 = 0;
        goto L_0x0017;
    L_0x04a9:
        r14 = "qs_parseClockSinXML";
        r15 = "eventType-1";
        android.util.Log.d(r14, r15);	 Catch:{ XmlPullParserException -> 0x04d1, IOException -> 0x04d4 }
        r4 = r5;
        goto L_0x0017;
    L_0x04b5:
        r4 = r5;
    L_0x04b6:
        r0 = r17;
        r14 = r0.clockInfos;
        return r14;
    L_0x04bb:
        r7 = move-exception;
    L_0x04bc:
        r14 = "MyAnalogClock3";
        r15 = "Got IOException while parsing toppackage.";
        android.util.Log.w(r14, r15, r7);
        goto L_0x04b6;
    L_0x04c6:
        r8 = move-exception;
    L_0x04c7:
        r14 = "MyAnalogClock3";
        r15 = "Got XmlPullParserException while parsing toppackage.";
        android.util.Log.w(r14, r15, r8);
        goto L_0x04b6;
    L_0x04d1:
        r8 = move-exception;
        r4 = r5;
        goto L_0x04c7;
    L_0x04d4:
        r7 = move-exception;
        r4 = r5;
        goto L_0x04bc;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.watchapp.MyAnalogClock3.parseClockSinXML(android.content.res.Resources, java.lang.String, java.lang.String):java.util.List<com.mediatek.watchapp.view.ClockInfo>");
    }

    private void onTimeChanged() {
        this.mCalendar.setToNow();
        this.hour = this.mCalendar.hour;
        this.minute = this.mCalendar.minute;
        this.second = this.mCalendar.second;
        this.mMonth = this.mCalendar.month;
        this.mWeek = this.mCalendar.weekDay;
        this.mDate = this.mCalendar.monthDay;
        this.mSecond = (float) this.second;
        this.mMinutes = ((float) this.minute) + (((float) this.second) / 60.0f);
        this.mHour = (((float) this.hour) + (this.mMinutes / 60.0f)) + (this.mSecond / 3600.0f);
        updateContentDescription(this.mCalendar);
        this.mChanged = true;
    }

    protected void onAttachedToWindow() {
        this.mTickerStopped = false;
        super.onAttachedToWindow();
        this.mTicker = new C01311();
        this.mTicker.run();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mTickerStopped = true;
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
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
        setMeasuredDimension(resolveSizeAndState((int) (((float) this.mDialWidth) * scale), widthMeasureSpec, 0), resolveSizeAndState((int) (((float) this.mDialHeight) * scale), heightMeasureSpec, 0));
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mChanged = true;
        resetScreen(getRight() - getLeft());
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void onDraw(android.graphics.Canvas r134) {
        /*
        r133 = this;
        super.onDraw(r134);
        r0 = r133;
        r4 = r0.mContext;	 Catch:{ Exception -> 0x024d }
        r4 = com.mediatek.watchapp.WatchApp.getBatteryLevel(r4);	 Catch:{ Exception -> 0x024d }
        r0 = (int) r4;	 Catch:{ Exception -> 0x024d }
        r23 = r0;
        r4 = r133.getRight();	 Catch:{ Exception -> 0x024d }
        r5 = r133.getLeft();	 Catch:{ Exception -> 0x024d }
        r94 = r4 - r5;
        r4 = r133.getBottom();	 Catch:{ Exception -> 0x024d }
        r5 = r133.getTop();	 Catch:{ Exception -> 0x024d }
        r93 = r4 - r5;
        r4 = r94 / 2;
        r0 = r133;
        r0.centerX1 = r4;	 Catch:{ Exception -> 0x024d }
        r4 = r93 / 2;
        r0 = r133;
        r0.centerY1 = r4;	 Catch:{ Exception -> 0x024d }
        r118 = java.lang.System.currentTimeMillis();	 Catch:{ Exception -> 0x024d }
        r4 = 60000; // 0xea60 float:8.4078E-41 double:2.9644E-319;
        r112 = r118 % r4;
        r0 = r133;
        r0 = r0.maxWidth;	 Catch:{ Exception -> 0x024d }
        r131 = r0;
        r0 = r133;
        r0 = r0.maxWidth;	 Catch:{ Exception -> 0x024d }
        r104 = r0;
        r126 = 0;
        r0 = r94;
        r1 = r131;
        if (r0 < r1) goto L_0x0051;
    L_0x004b:
        r0 = r93;
        r1 = r104;
        if (r0 >= r1) goto L_0x0215;
    L_0x0051:
        r126 = 1;
        r0 = r94;
        r4 = (float) r0;	 Catch:{ Exception -> 0x024d }
        r0 = r131;
        r5 = (float) r0;	 Catch:{ Exception -> 0x024d }
        r4 = r4 / r5;
        r0 = r93;
        r5 = (float) r0;	 Catch:{ Exception -> 0x024d }
        r0 = r104;
        r7 = (float) r0;	 Catch:{ Exception -> 0x024d }
        r5 = r5 / r7;
        r125 = java.lang.Math.min(r4, r5);	 Catch:{ Exception -> 0x024d }
        r134.save();	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r4 = r0.centerX1;	 Catch:{ Exception -> 0x024d }
        r4 = (float) r4;	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r5 = r0.centerY1;	 Catch:{ Exception -> 0x024d }
        r5 = (float) r5;	 Catch:{ Exception -> 0x024d }
        r0 = r134;
        r1 = r125;
        r2 = r125;
        r0.scale(r1, r2, r4, r5);	 Catch:{ Exception -> 0x024d }
    L_0x007b:
        r108 = 0;
    L_0x007d:
        r0 = r133;
        r4 = r0.parseClock;	 Catch:{ Exception -> 0x024d }
        r4 = r4.size();	 Catch:{ Exception -> 0x024d }
        r0 = r108;
        if (r0 >= r4) goto L_0x129e;
    L_0x0089:
        r0 = r133;
        r4 = r0.parseClock;	 Catch:{ Exception -> 0x024d }
        r0 = r108;
        r4 = r4.get(r0);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo) r4;	 Catch:{ Exception -> 0x024d }
        r117 = r4.getName();	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r4 = r0.parseClock;	 Catch:{ Exception -> 0x024d }
        r0 = r108;
        r4 = r4.get(r0);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo) r4;	 Catch:{ Exception -> 0x024d }
        r97 = r4.getCenterX();	 Catch:{ Exception -> 0x024d }
        if (r97 != 0) goto L_0x00ae;
    L_0x00ab:
        r97 = "0";
    L_0x00ae:
        r4 = java.lang.Integer.valueOf(r97);	 Catch:{ Exception -> 0x024d }
        r12 = r4.intValue();	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r4 = r0.parseClock;	 Catch:{ Exception -> 0x024d }
        r0 = r108;
        r4 = r4.get(r0);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo) r4;	 Catch:{ Exception -> 0x024d }
        r98 = r4.getCenterY();	 Catch:{ Exception -> 0x024d }
        if (r98 != 0) goto L_0x00cb;
    L_0x00c8:
        r98 = "0";
    L_0x00cb:
        r4 = java.lang.Integer.valueOf(r98);	 Catch:{ Exception -> 0x024d }
        r13 = r4.intValue();	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r4 = r0.parseClock;	 Catch:{ Exception -> 0x024d }
        r0 = r108;
        r4 = r4.get(r0);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo) r4;	 Catch:{ Exception -> 0x024d }
        r124 = r4.getRotate();	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r4 = r0.parseClock;	 Catch:{ Exception -> 0x024d }
        r0 = r108;
        r4 = r4.get(r0);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo) r4;	 Catch:{ Exception -> 0x024d }
        r6 = r4.getAngle();	 Catch:{ Exception -> 0x024d }
        if (r6 != 0) goto L_0x00f8;
    L_0x00f5:
        r6 = "0";
    L_0x00f8:
        r0 = r133;
        r4 = r0.parseClock;	 Catch:{ Exception -> 0x024d }
        r0 = r108;
        r4 = r4.get(r0);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo) r4;	 Catch:{ Exception -> 0x024d }
        r92 = r4.getArraytype();	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r4 = r0.parseClock;	 Catch:{ Exception -> 0x024d }
        r0 = r108;
        r4 = r4.get(r0);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo) r4;	 Catch:{ Exception -> 0x024d }
        r114 = r4.getMulrotate();	 Catch:{ Exception -> 0x024d }
        if (r114 == 0) goto L_0x0125;
    L_0x011a:
        r4 = "0";
        r0 = r114;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x0128;
    L_0x0125:
        r114 = "1";
    L_0x0128:
        r0 = r133;
        r4 = r0.parseClock;	 Catch:{ Exception -> 0x024d }
        r0 = r108;
        r4 = r4.get(r0);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo) r4;	 Catch:{ Exception -> 0x024d }
        r99 = r4.getColor();	 Catch:{ Exception -> 0x024d }
        if (r99 != 0) goto L_0x013d;
    L_0x013a:
        r99 = "0";
    L_0x013d:
        r0 = r133;
        r4 = r0.parseClock;	 Catch:{ Exception -> 0x024d }
        r0 = r108;
        r4 = r4.get(r0);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo) r4;	 Catch:{ Exception -> 0x024d }
        r77 = r4.getWidth();	 Catch:{ Exception -> 0x024d }
        if (r77 != 0) goto L_0x0152;
    L_0x014f:
        r77 = "5";
    L_0x0152:
        r0 = r133;
        r4 = r0.parseClock;	 Catch:{ Exception -> 0x024d }
        r0 = r108;
        r4 = r4.get(r0);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo) r4;	 Catch:{ Exception -> 0x024d }
        r78 = r4.getRadius();	 Catch:{ Exception -> 0x024d }
        if (r78 != 0) goto L_0x0167;
    L_0x0164:
        r78 = "50";
    L_0x0167:
        r0 = r133;
        r4 = r0.parseClock;	 Catch:{ Exception -> 0x024d }
        r0 = r108;
        r4 = r4.get(r0);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo) r4;	 Catch:{ Exception -> 0x024d }
        r16 = r4.getStartAngle();	 Catch:{ Exception -> 0x024d }
        if (r16 != 0) goto L_0x017c;
    L_0x0179:
        r16 = "0";
    L_0x017c:
        r0 = r133;
        r4 = r0.parseClock;	 Catch:{ Exception -> 0x024d }
        r0 = r108;
        r4 = r4.get(r0);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo) r4;	 Catch:{ Exception -> 0x024d }
        r101 = r4.getDirection();	 Catch:{ Exception -> 0x024d }
        if (r101 != 0) goto L_0x0191;
    L_0x018e:
        r101 = "1";
    L_0x0191:
        r0 = r133;
        r4 = r0.parseClock;	 Catch:{ Exception -> 0x024d }
        r0 = r108;
        r4 = r4.get(r0);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo) r4;	 Catch:{ Exception -> 0x024d }
        r130 = r4.getTextsize();	 Catch:{ Exception -> 0x024d }
        if (r130 != 0) goto L_0x01a6;
    L_0x01a3:
        r130 = "18";
    L_0x01a6:
        r0 = r133;
        r4 = r0.parseClock;	 Catch:{ Exception -> 0x024d }
        r0 = r108;
        r4 = r4.get(r0);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo) r4;	 Catch:{ Exception -> 0x024d }
        r56 = r4.getColorArray();	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r4 = r0.parseClock;	 Catch:{ Exception -> 0x024d }
        r0 = r108;
        r4 = r4.get(r0);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo) r4;	 Catch:{ Exception -> 0x024d }
        r26 = r4.getRotatemode();	 Catch:{ Exception -> 0x024d }
        if (r26 != 0) goto L_0x01cb;
    L_0x01c8:
        r26 = "3";
    L_0x01cb:
        r0 = r133;
        r4 = r0.parseClock;	 Catch:{ Exception -> 0x024d }
        r0 = r108;
        r4 = r4.get(r0);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo) r4;	 Catch:{ Exception -> 0x024d }
        r120 = r4.getNums();	 Catch:{ Exception -> 0x024d }
        if (r92 != 0) goto L_0x045d;
    L_0x01dd:
        r0 = r133;
        r4 = r0.parseClock;	 Catch:{ Exception -> 0x024d }
        r0 = r108;
        r4 = r4.get(r0);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo) r4;	 Catch:{ Exception -> 0x024d }
        r10 = r4.getNamepng();	 Catch:{ Exception -> 0x024d }
        if (r124 == 0) goto L_0x0454;
    L_0x01ef:
        r4 = "1";
        r0 = r124;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x024f;
    L_0x01fa:
        r0 = r133;
        r7 = r0.mHour;	 Catch:{ Exception -> 0x024d }
        r8 = java.lang.Integer.parseInt(r114);	 Catch:{ Exception -> 0x024d }
        r9 = java.lang.Integer.parseInt(r101);	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r11 = r0.mChanged;	 Catch:{ Exception -> 0x024d }
        r4 = r133;
        r5 = r134;
        r4.drawHourhand(r5, r6, r7, r8, r9, r10, r11, r12, r13);	 Catch:{ Exception -> 0x024d }
    L_0x0211:
        r108 = r108 + 1;
        goto L_0x007d;
    L_0x0215:
        r0 = r94;
        r1 = r131;
        if (r0 <= r1) goto L_0x007b;
    L_0x021b:
        r0 = r93;
        r1 = r104;
        if (r0 <= r1) goto L_0x007b;
    L_0x0221:
        r126 = 1;
        r0 = r94;
        r4 = (float) r0;	 Catch:{ Exception -> 0x024d }
        r0 = r131;
        r5 = (float) r0;	 Catch:{ Exception -> 0x024d }
        r4 = r4 / r5;
        r0 = r93;
        r5 = (float) r0;	 Catch:{ Exception -> 0x024d }
        r0 = r104;
        r7 = (float) r0;	 Catch:{ Exception -> 0x024d }
        r5 = r5 / r7;
        r125 = java.lang.Math.min(r4, r5);	 Catch:{ Exception -> 0x024d }
        r134.save();	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r4 = r0.centerX1;	 Catch:{ Exception -> 0x024d }
        r4 = (float) r4;	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r5 = r0.centerY1;	 Catch:{ Exception -> 0x024d }
        r5 = (float) r5;	 Catch:{ Exception -> 0x024d }
        r0 = r134;
        r1 = r125;
        r2 = r125;
        r0.scale(r1, r2, r4, r5);	 Catch:{ Exception -> 0x024d }
        goto L_0x007b;
    L_0x024d:
        r103 = move-exception;
    L_0x024e:
        return;
    L_0x024f:
        r4 = "2";
        r0 = r124;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x0272;
    L_0x025a:
        r0 = r133;
        r7 = r0.mMinutes;	 Catch:{ Exception -> 0x024d }
        r8 = java.lang.Integer.parseInt(r114);	 Catch:{ Exception -> 0x024d }
        r9 = java.lang.Integer.parseInt(r101);	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r11 = r0.mChanged;	 Catch:{ Exception -> 0x024d }
        r4 = r133;
        r5 = r134;
        r4.drawMinuteHand(r5, r6, r7, r8, r9, r10, r11, r12, r13);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x0272:
        r4 = "3";
        r0 = r124;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x029c;
    L_0x027d:
        r17 = java.lang.Integer.parseInt(r114);	 Catch:{ Exception -> 0x024d }
        r18 = java.lang.Integer.parseInt(r101);	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r0 = r0.mChanged;	 Catch:{ Exception -> 0x024d }
        r20 = r0;
        r14 = r133;
        r15 = r134;
        r16 = r6;
        r19 = r10;
        r21 = r12;
        r22 = r13;
        r14.drawSecondHand(r15, r16, r17, r18, r19, r20, r21, r22);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x029c:
        r4 = "4";
        r0 = r124;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x02c2;
    L_0x02a7:
        r0 = r133;
        r0 = r0.mMonth;	 Catch:{ Exception -> 0x024d }
        r17 = r0;
        r0 = r133;
        r0 = r0.mChanged;	 Catch:{ Exception -> 0x024d }
        r19 = r0;
        r14 = r133;
        r15 = r134;
        r18 = r10;
        r20 = r12;
        r21 = r13;
        r14.drawMonthhand(r15, r16, r17, r18, r19, r20, r21);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x02c2:
        r4 = "5";
        r0 = r124;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x02e8;
    L_0x02cd:
        r0 = r133;
        r0 = r0.mWeek;	 Catch:{ Exception -> 0x024d }
        r17 = r0;
        r0 = r133;
        r0 = r0.mChanged;	 Catch:{ Exception -> 0x024d }
        r19 = r0;
        r14 = r133;
        r15 = r134;
        r18 = r10;
        r20 = r12;
        r21 = r13;
        r14.drawWeekhand(r15, r16, r17, r18, r19, r20, r21);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x02e8:
        r4 = "6";
        r0 = r124;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x0314;
    L_0x02f3:
        r21 = java.lang.Integer.parseInt(r101);	 Catch:{ Exception -> 0x024d }
        r22 = java.lang.Integer.parseInt(r114);	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r0 = r0.mChanged;	 Catch:{ Exception -> 0x024d }
        r25 = r0;
        r17 = r133;
        r18 = r134;
        r19 = r6;
        r20 = r16;
        r24 = r10;
        r26 = r12;
        r27 = r13;
        r17.drawBatteryhand(r18, r19, r20, r21, r22, r23, r24, r25, r26, r27);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x0314:
        r4 = "7";
        r0 = r124;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x033c;
    L_0x031f:
        r0 = r133;
        r0 = r0.mHour;	 Catch:{ Exception -> 0x024d }
        r27 = r0;
        r0 = r133;
        r0 = r0.mChanged;	 Catch:{ Exception -> 0x024d }
        r29 = r0;
        r24 = r133;
        r25 = r134;
        r26 = r6;
        r28 = r10;
        r30 = r12;
        r31 = r13;
        r24.draw24Hourhand(r25, r26, r27, r28, r29, r30, r31);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x033c:
        r4 = "8";
        r0 = r124;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x0364;
    L_0x0347:
        r0 = r133;
        r0 = r0.mHour;	 Catch:{ Exception -> 0x024d }
        r27 = r0;
        r0 = r133;
        r0 = r0.mChanged;	 Catch:{ Exception -> 0x024d }
        r29 = r0;
        r24 = r133;
        r25 = r134;
        r26 = r6;
        r28 = r10;
        r30 = r12;
        r31 = r13;
        r24.drawHourhandShadow(r25, r26, r27, r28, r29, r30, r31);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x0364:
        r4 = "9";
        r0 = r124;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x038c;
    L_0x036f:
        r0 = r133;
        r0 = r0.mMinutes;	 Catch:{ Exception -> 0x024d }
        r27 = r0;
        r0 = r133;
        r0 = r0.mChanged;	 Catch:{ Exception -> 0x024d }
        r29 = r0;
        r24 = r133;
        r25 = r134;
        r26 = r6;
        r28 = r10;
        r30 = r12;
        r31 = r13;
        r24.drawMinuteHandShadow(r25, r26, r27, r28, r29, r30, r31);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x038c:
        r4 = "10";
        r0 = r124;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x03b6;
    L_0x0397:
        r27 = java.lang.Integer.parseInt(r114);	 Catch:{ Exception -> 0x024d }
        r28 = java.lang.Integer.parseInt(r101);	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r0 = r0.mChanged;	 Catch:{ Exception -> 0x024d }
        r30 = r0;
        r24 = r133;
        r25 = r134;
        r26 = r6;
        r29 = r10;
        r31 = r12;
        r32 = r13;
        r24.drawSecondHandShadow(r25, r26, r27, r28, r29, r30, r31, r32);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x03b6:
        r4 = "11";
        r0 = r124;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x03e0;
    L_0x03c1:
        r17 = java.lang.Integer.parseInt(r101);	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r0 = r0.mDate;	 Catch:{ Exception -> 0x024d }
        r18 = r0;
        r0 = r133;
        r0 = r0.mChanged;	 Catch:{ Exception -> 0x024d }
        r20 = r0;
        r14 = r133;
        r15 = r134;
        r19 = r10;
        r21 = r12;
        r22 = r13;
        r14.drawMonthDayhand(r15, r16, r17, r18, r19, r20, r21, r22);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x03e0:
        r4 = "12";
        r0 = r124;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x040c;
    L_0x03eb:
        r21 = java.lang.Integer.parseInt(r101);	 Catch:{ Exception -> 0x024d }
        r22 = java.lang.Integer.parseInt(r114);	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r0 = r0.mChanged;	 Catch:{ Exception -> 0x024d }
        r25 = r0;
        r17 = r133;
        r18 = r134;
        r19 = r6;
        r20 = r16;
        r24 = r10;
        r26 = r12;
        r27 = r13;
        r17.drawBatteryhand2(r18, r19, r20, r21, r22, r23, r24, r25, r26, r27);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x040c:
        r4 = "13";
        r0 = r124;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x0438;
    L_0x0417:
        r29 = java.lang.Integer.parseInt(r101);	 Catch:{ Exception -> 0x024d }
        r30 = java.lang.Integer.parseInt(r114);	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r0 = r0.mChanged;	 Catch:{ Exception -> 0x024d }
        r32 = r0;
        r24 = r133;
        r25 = r134;
        r27 = r6;
        r28 = r16;
        r31 = r10;
        r33 = r12;
        r34 = r13;
        r24.drawRotateModeHand(r25, r26, r27, r28, r29, r30, r31, r32, r33, r34);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x0438:
        r4 = "14";
        r0 = r124;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x0211;
    L_0x0443:
        r9 = java.lang.Integer.parseInt(r114);	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r11 = r0.mChanged;	 Catch:{ Exception -> 0x024d }
        r7 = r133;
        r8 = r134;
        r7.drawBalanceHand(r8, r9, r10, r11, r12, r13);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x0454:
        r0 = r133;
        r1 = r134;
        r0.drawDial(r1, r12, r13, r10);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x045d:
        r4 = "1";
        r0 = r92;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x058b;
    L_0x0468:
        r0 = r133;
        r4 = r0.mCalendar;	 Catch:{ Exception -> 0x024d }
        r0 = r4.year;	 Catch:{ Exception -> 0x024d }
        r132 = r0;
        r0 = r132;
        r0 = r0 / 1000;
        r91 = r0;
        r0 = r132;
        r4 = r0 % 1000;
        r95 = r4 / 100;
        r0 = r132;
        r4 = r0 % 1000;
        r4 = r4 % 100;
        r96 = r4 / 10;
        r0 = r132;
        r4 = r0 % 1000;
        r4 = r4 % 100;
        r4 = r4 % 10;
        r100 = r4 % 10;
        r0 = r120;
        r1 = r91;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r31 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r120;
        r1 = r95;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r32 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r120;
        r1 = r96;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r33 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r120;
        r1 = r100;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r34 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r4 = 10;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r35 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r4 = r0.mMonth;	 Catch:{ Exception -> 0x024d }
        r4 = r4 + 1;
        r5 = 10;
        if (r4 >= r5) goto L_0x053d;
    L_0x04de:
        r4 = 0;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r36 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r4 = r0.mMonth;	 Catch:{ Exception -> 0x024d }
        r4 = r4 + 1;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r37 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
    L_0x04fd:
        r4 = 10;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r38 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r4 = r0.mDate;	 Catch:{ Exception -> 0x024d }
        r5 = 10;
        if (r4 >= r5) goto L_0x0566;
    L_0x0513:
        r4 = 0;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r39 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r4 = r0.mDate;	 Catch:{ Exception -> 0x024d }
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r40 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
    L_0x0530:
        r27 = r133;
        r28 = r134;
        r29 = r12;
        r30 = r13;
        r27.drawDrawable10(r28, r29, r30, r31, r32, r33, r34, r35, r36, r37, r38, r39, r40);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x053d:
        r0 = r133;
        r4 = r0.mMonth;	 Catch:{ Exception -> 0x024d }
        r4 = r4 + 1;
        r4 = r4 / 10;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r36 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r4 = r0.mMonth;	 Catch:{ Exception -> 0x024d }
        r4 = r4 + 1;
        r4 = r4 % 10;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r37 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        goto L_0x04fd;
    L_0x0566:
        r0 = r133;
        r4 = r0.mDate;	 Catch:{ Exception -> 0x024d }
        r4 = r4 / 10;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r39 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r4 = r0.mDate;	 Catch:{ Exception -> 0x024d }
        r4 = r4 % 10;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r40 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        goto L_0x0530;
    L_0x058b:
        r4 = "2";
        r0 = r92;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x0659;
    L_0x0596:
        r0 = r133;
        r4 = r0.mMonth;	 Catch:{ Exception -> 0x024d }
        r4 = r4 + 1;
        r5 = 10;
        if (r4 >= r5) goto L_0x060b;
    L_0x05a0:
        r4 = 0;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r31 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r4 = r0.mMonth;	 Catch:{ Exception -> 0x024d }
        r4 = r4 + 1;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r32 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
    L_0x05bf:
        r4 = 10;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r33 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r4 = r0.mDate;	 Catch:{ Exception -> 0x024d }
        r5 = 10;
        if (r4 >= r5) goto L_0x0634;
    L_0x05d5:
        r4 = 0;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r34 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r4 = r0.mDate;	 Catch:{ Exception -> 0x024d }
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r35 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
    L_0x05f2:
        r43 = 1;
        r41 = r133;
        r42 = r134;
        r44 = r12;
        r45 = r13;
        r46 = r31;
        r47 = r32;
        r48 = r33;
        r49 = r34;
        r50 = r35;
        r41.drawDrawable5(r42, r43, r44, r45, r46, r47, r48, r49, r50);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x060b:
        r0 = r133;
        r4 = r0.mMonth;	 Catch:{ Exception -> 0x024d }
        r4 = r4 + 1;
        r4 = r4 / 10;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r31 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r4 = r0.mMonth;	 Catch:{ Exception -> 0x024d }
        r4 = r4 + 1;
        r4 = r4 % 10;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r32 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        goto L_0x05bf;
    L_0x0634:
        r0 = r133;
        r4 = r0.mDate;	 Catch:{ Exception -> 0x024d }
        r4 = r4 / 10;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r34 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r4 = r0.mDate;	 Catch:{ Exception -> 0x024d }
        r4 = r4 % 10;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r35 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        goto L_0x05f2;
    L_0x0659:
        r4 = "3";
        r0 = r92;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x067d;
    L_0x0664:
        r0 = r133;
        r4 = r0.mMonth;	 Catch:{ Exception -> 0x024d }
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r10 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r1 = r134;
        r0.drawDial(r1, r12, r13, r10);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x067d:
        r4 = "4";
        r0 = r92;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x06df;
    L_0x0688:
        r0 = r133;
        r4 = r0.mDate;	 Catch:{ Exception -> 0x024d }
        r5 = 10;
        if (r4 >= r5) goto L_0x06ba;
    L_0x0690:
        r4 = 0;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r21 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r4 = r0.mDate;	 Catch:{ Exception -> 0x024d }
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r22 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
    L_0x06ad:
        r17 = r133;
        r18 = r134;
        r19 = r12;
        r20 = r13;
        r17.drawDrawable2(r18, r19, r20, r21, r22);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x06ba:
        r0 = r133;
        r4 = r0.mDate;	 Catch:{ Exception -> 0x024d }
        r4 = r4 / 10;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r21 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r4 = r0.mDate;	 Catch:{ Exception -> 0x024d }
        r4 = r4 % 10;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r22 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        goto L_0x06ad;
    L_0x06df:
        r4 = "5";
        r0 = r92;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x0703;
    L_0x06ea:
        r0 = r133;
        r4 = r0.mWeek;	 Catch:{ Exception -> 0x024d }
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r10 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r1 = r134;
        r0.drawDial(r1, r12, r13, r10);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x0703:
        r4 = "6";
        r0 = r92;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x082f;
    L_0x070e:
        r36 = 0;
        r105 = 0;
        r0 = r133;
        r4 = r0.mContext;	 Catch:{ Exception -> 0x024d }
        r4 = android.text.format.DateFormat.is24HourFormat(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x07a4;
    L_0x071c:
        r127 = new java.text.SimpleDateFormat;	 Catch:{ Exception -> 0x024d }
        r4 = "HH";
        r0 = r127;
        r0.<init>(r4);	 Catch:{ Exception -> 0x024d }
        r4 = new java.util.Date;	 Catch:{ Exception -> 0x024d }
        r4.<init>();	 Catch:{ Exception -> 0x024d }
        r0 = r127;
        r107 = r0.format(r4);	 Catch:{ Exception -> 0x024d }
        r105 = java.lang.Integer.parseInt(r107);	 Catch:{ Exception -> 0x024d }
    L_0x0735:
        r4 = 10;
        r0 = r105;
        if (r0 >= r4) goto L_0x07eb;
    L_0x073b:
        r4 = 0;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r31 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r120;
        r1 = r105;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r32 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
    L_0x0756:
        r4 = 10;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r33 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r4 = r0.minute;	 Catch:{ Exception -> 0x024d }
        r5 = 10;
        if (r4 >= r5) goto L_0x0809;
    L_0x076c:
        r4 = 0;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r34 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r4 = r0.minute;	 Catch:{ Exception -> 0x024d }
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r35 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
    L_0x0789:
        r43 = 0;
        r41 = r133;
        r42 = r134;
        r44 = r12;
        r45 = r13;
        r46 = r31;
        r47 = r32;
        r48 = r33;
        r49 = r34;
        r50 = r35;
        r51 = r36;
        r41.drawDrawable6(r42, r43, r44, r45, r46, r47, r48, r49, r50, r51);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x07a4:
        r128 = new java.text.SimpleDateFormat;	 Catch:{ Exception -> 0x024d }
        r4 = "HH";
        r0 = r128;
        r0.<init>(r4);	 Catch:{ Exception -> 0x024d }
        r4 = new java.util.Date;	 Catch:{ Exception -> 0x024d }
        r4.<init>();	 Catch:{ Exception -> 0x024d }
        r0 = r128;
        r107 = r0.format(r4);	 Catch:{ Exception -> 0x024d }
        r106 = java.lang.Integer.parseInt(r107);	 Catch:{ Exception -> 0x024d }
        r4 = r120.size();	 Catch:{ Exception -> 0x024d }
        r5 = 11;
        if (r4 <= r5) goto L_0x0735;
    L_0x07c5:
        r4 = 12;
        r0 = r106;
        if (r0 >= r4) goto L_0x07db;
    L_0x07cb:
        r4 = 11;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r36 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        goto L_0x0735;
    L_0x07db:
        r4 = 12;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r36 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        goto L_0x0735;
    L_0x07eb:
        r4 = r105 / 10;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r31 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r4 = r105 % 10;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r32 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        goto L_0x0756;
    L_0x0809:
        r0 = r133;
        r4 = r0.minute;	 Catch:{ Exception -> 0x024d }
        r4 = r4 / 10;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r34 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r4 = r0.minute;	 Catch:{ Exception -> 0x024d }
        r4 = r4 % 10;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r35 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        goto L_0x0789;
    L_0x082f:
        r4 = "7";
        r0 = r92;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x08c4;
    L_0x083a:
        r105 = 0;
        r0 = r133;
        r4 = r0.mContext;	 Catch:{ Exception -> 0x024d }
        r4 = android.text.format.DateFormat.is24HourFormat(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x088d;
    L_0x0846:
        r127 = new java.text.SimpleDateFormat;	 Catch:{ Exception -> 0x024d }
        r4 = "HH";
        r0 = r127;
        r0.<init>(r4);	 Catch:{ Exception -> 0x024d }
        r4 = new java.util.Date;	 Catch:{ Exception -> 0x024d }
        r4.<init>();	 Catch:{ Exception -> 0x024d }
        r0 = r127;
        r107 = r0.format(r4);	 Catch:{ Exception -> 0x024d }
        r105 = java.lang.Integer.parseInt(r107);	 Catch:{ Exception -> 0x024d }
    L_0x085f:
        r4 = 10;
        r0 = r105;
        if (r0 >= r4) goto L_0x08a7;
    L_0x0865:
        r4 = 0;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r21 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r120;
        r1 = r105;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r22 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
    L_0x0880:
        r17 = r133;
        r18 = r134;
        r19 = r12;
        r20 = r13;
        r17.drawDrawable2(r18, r19, r20, r21, r22);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x088d:
        r127 = new java.text.SimpleDateFormat;	 Catch:{ Exception -> 0x024d }
        r4 = "hh";
        r0 = r127;
        r0.<init>(r4);	 Catch:{ Exception -> 0x024d }
        r4 = new java.util.Date;	 Catch:{ Exception -> 0x024d }
        r4.<init>();	 Catch:{ Exception -> 0x024d }
        r0 = r127;
        r107 = r0.format(r4);	 Catch:{ Exception -> 0x024d }
        r105 = java.lang.Integer.parseInt(r107);	 Catch:{ Exception -> 0x024d }
        goto L_0x085f;
    L_0x08a7:
        r4 = r105 / 10;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r21 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r4 = r105 % 10;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r22 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        goto L_0x0880;
    L_0x08c4:
        r4 = "8";
        r0 = r92;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x0926;
    L_0x08cf:
        r0 = r133;
        r4 = r0.minute;	 Catch:{ Exception -> 0x024d }
        r5 = 10;
        if (r4 >= r5) goto L_0x0901;
    L_0x08d7:
        r4 = 0;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r21 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r4 = r0.minute;	 Catch:{ Exception -> 0x024d }
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r22 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
    L_0x08f4:
        r17 = r133;
        r18 = r134;
        r19 = r12;
        r20 = r13;
        r17.drawDrawable2(r18, r19, r20, r21, r22);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x0901:
        r0 = r133;
        r4 = r0.minute;	 Catch:{ Exception -> 0x024d }
        r4 = r4 / 10;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r21 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r4 = r0.minute;	 Catch:{ Exception -> 0x024d }
        r4 = r4 % 10;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r22 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        goto L_0x08f4;
    L_0x0926:
        r4 = "9";
        r0 = r92;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x0988;
    L_0x0931:
        r0 = r133;
        r4 = r0.second;	 Catch:{ Exception -> 0x024d }
        r5 = 10;
        if (r4 >= r5) goto L_0x0963;
    L_0x0939:
        r4 = 0;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r21 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r4 = r0.second;	 Catch:{ Exception -> 0x024d }
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r22 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
    L_0x0956:
        r17 = r133;
        r18 = r134;
        r19 = r12;
        r20 = r13;
        r17.drawDrawable2(r18, r19, r20, r21, r22);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x0963:
        r0 = r133;
        r4 = r0.second;	 Catch:{ Exception -> 0x024d }
        r4 = r4 / 10;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r21 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r4 = r0.second;	 Catch:{ Exception -> 0x024d }
        r4 = r4 % 10;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r22 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        goto L_0x0956;
    L_0x0988:
        r4 = "10";
        r0 = r92;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x0bd4;
    L_0x0993:
        r4 = r133.getContext();	 Catch:{ Exception -> 0x024d }
        r90 = com.mediatek.watchapp.WatchApp.getWeatherIcon(r4);	 Catch:{ Exception -> 0x024d }
        r4 = 0;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r31 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r4 = 9;
        r0 = r90;
        if (r0 == r4) goto L_0x09b4;
    L_0x09ae:
        r4 = 10;
        r0 = r90;
        if (r0 != r4) goto L_0x09cc;
    L_0x09b4:
        r4 = 0;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r31 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
    L_0x09c1:
        r0 = r133;
        r1 = r134;
        r2 = r31;
        r0.drawDial(r1, r12, r13, r2);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x09cc:
        r4 = 27;
        r0 = r90;
        if (r0 == r4) goto L_0x09b4;
    L_0x09d2:
        r4 = 28;
        r0 = r90;
        if (r0 == r4) goto L_0x09b4;
    L_0x09d8:
        r4 = 1;
        r0 = r90;
        if (r0 == r4) goto L_0x09b4;
    L_0x09dd:
        r4 = 2;
        r0 = r90;
        if (r0 == r4) goto L_0x09e7;
    L_0x09e2:
        r4 = 3;
        r0 = r90;
        if (r0 != r4) goto L_0x09f5;
    L_0x09e7:
        r4 = 0;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r31 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        goto L_0x09c1;
    L_0x09f5:
        r4 = 4;
        r0 = r90;
        if (r0 == r4) goto L_0x09e7;
    L_0x09fa:
        r4 = 5;
        r0 = r90;
        if (r0 == r4) goto L_0x09e7;
    L_0x09ff:
        r4 = 6;
        r0 = r90;
        if (r0 == r4) goto L_0x0a09;
    L_0x0a04:
        r4 = 7;
        r0 = r90;
        if (r0 != r4) goto L_0x0a17;
    L_0x0a09:
        r4 = 1;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r31 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        goto L_0x09c1;
    L_0x0a17:
        r4 = 8;
        r0 = r90;
        if (r0 == r4) goto L_0x0a09;
    L_0x0a1d:
        r4 = 38;
        r0 = r90;
        if (r0 == r4) goto L_0x0a09;
    L_0x0a23:
        r4 = 11;
        r0 = r90;
        if (r0 != r4) goto L_0x0a37;
    L_0x0a29:
        r4 = 3;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r31 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        goto L_0x09c1;
    L_0x0a37:
        r4 = 12;
        r0 = r90;
        if (r0 == r4) goto L_0x0a43;
    L_0x0a3d:
        r4 = 39;
        r0 = r90;
        if (r0 != r4) goto L_0x0a52;
    L_0x0a43:
        r4 = 5;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r31 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        goto L_0x09c1;
    L_0x0a52:
        r4 = 40;
        r0 = r90;
        if (r0 == r4) goto L_0x0a43;
    L_0x0a58:
        r4 = 13;
        r0 = r90;
        if (r0 == r4) goto L_0x0a64;
    L_0x0a5e:
        r4 = 14;
        r0 = r90;
        if (r0 != r4) goto L_0x0a73;
    L_0x0a64:
        r4 = 5;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r31 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        goto L_0x09c1;
    L_0x0a73:
        r4 = 15;
        r0 = r90;
        if (r0 == r4) goto L_0x0a7f;
    L_0x0a79:
        r4 = 16;
        r0 = r90;
        if (r0 != r4) goto L_0x0a8e;
    L_0x0a7f:
        r4 = 7;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r31 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        goto L_0x09c1;
    L_0x0a8e:
        r4 = 41;
        r0 = r90;
        if (r0 == r4) goto L_0x0a7f;
    L_0x0a94:
        r4 = 42;
        r0 = r90;
        if (r0 == r4) goto L_0x0a7f;
    L_0x0a9a:
        r4 = 17;
        r0 = r90;
        if (r0 != r4) goto L_0x0aaf;
    L_0x0aa0:
        r4 = 7;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r31 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        goto L_0x09c1;
    L_0x0aaf:
        r4 = 18;
        r0 = r90;
        if (r0 != r4) goto L_0x0ac4;
    L_0x0ab5:
        r4 = 4;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r31 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        goto L_0x09c1;
    L_0x0ac4:
        r4 = 19;
        r0 = r90;
        if (r0 == r4) goto L_0x0ad0;
    L_0x0aca:
        r4 = 43;
        r0 = r90;
        if (r0 != r4) goto L_0x0ae0;
    L_0x0ad0:
        r4 = 8;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r31 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        goto L_0x09c1;
    L_0x0ae0:
        r4 = 20;
        r0 = r90;
        if (r0 == r4) goto L_0x0aec;
    L_0x0ae6:
        r4 = 21;
        r0 = r90;
        if (r0 != r4) goto L_0x0afc;
    L_0x0aec:
        r4 = 8;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r31 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        goto L_0x09c1;
    L_0x0afc:
        r4 = 22;
        r0 = r90;
        if (r0 == r4) goto L_0x0b08;
    L_0x0b02:
        r4 = 23;
        r0 = r90;
        if (r0 != r4) goto L_0x0b18;
    L_0x0b08:
        r4 = 8;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r31 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        goto L_0x09c1;
    L_0x0b18:
        r4 = 24;
        r0 = r90;
        if (r0 == r4) goto L_0x0b24;
    L_0x0b1e:
        r4 = 25;
        r0 = r90;
        if (r0 != r4) goto L_0x0b34;
    L_0x0b24:
        r4 = 8;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r31 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        goto L_0x09c1;
    L_0x0b34:
        r4 = 44;
        r0 = r90;
        if (r0 == r4) goto L_0x0b24;
    L_0x0b3a:
        r4 = 26;
        r0 = r90;
        if (r0 == r4) goto L_0x0b46;
    L_0x0b40:
        r4 = 29;
        r0 = r90;
        if (r0 != r4) goto L_0x0b55;
    L_0x0b46:
        r4 = 6;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r31 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        goto L_0x09c1;
    L_0x0b55:
        r4 = 30;
        r0 = r90;
        if (r0 != r4) goto L_0x0b6b;
    L_0x0b5b:
        r4 = 10;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r31 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        goto L_0x09c1;
    L_0x0b6b:
        r4 = 31;
        r0 = r90;
        if (r0 != r4) goto L_0x0b81;
    L_0x0b71:
        r4 = 11;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r31 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        goto L_0x09c1;
    L_0x0b81:
        r4 = 32;
        r0 = r90;
        if (r0 != r4) goto L_0x0b97;
    L_0x0b87:
        r4 = 12;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r31 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        goto L_0x09c1;
    L_0x0b97:
        r4 = 33;
        r0 = r90;
        if (r0 != r4) goto L_0x0bac;
    L_0x0b9d:
        r4 = 0;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r31 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        goto L_0x09c1;
    L_0x0bac:
        r4 = 34;
        r0 = r90;
        if (r0 == r4) goto L_0x0bb8;
    L_0x0bb2:
        r4 = 35;
        r0 = r90;
        if (r0 != r4) goto L_0x0bc7;
    L_0x0bb8:
        r4 = 0;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r31 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        goto L_0x09c1;
    L_0x0bc7:
        r4 = 36;
        r0 = r90;
        if (r0 == r4) goto L_0x0bb8;
    L_0x0bcd:
        r4 = 37;
        r0 = r90;
        if (r0 != r4) goto L_0x09c1;
    L_0x0bd3:
        goto L_0x0bb8;
    L_0x0bd4:
        r4 = "11";
        r0 = r92;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x0c5a;
    L_0x0bdf:
        r0 = r133;
        r4 = r0.mContext;	 Catch:{ Exception -> 0x024d }
        r129 = com.mediatek.watchapp.WatchApp.getWeatherTemp(r4);	 Catch:{ Exception -> 0x024d }
        r43 = 0;
        if (r129 >= 0) goto L_0x0bed;
    L_0x0beb:
        r43 = 1;
    L_0x0bed:
        r4 = 10;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r21 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r4 = java.lang.Math.abs(r129);	 Catch:{ Exception -> 0x024d }
        r5 = 10;
        if (r4 >= r5) goto L_0x0c3d;
    L_0x0c03:
        r4 = 0;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r22 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r120;
        r1 = r129;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r48 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
    L_0x0c1e:
        r4 = 11;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r49 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r41 = r133;
        r42 = r134;
        r44 = r12;
        r45 = r13;
        r46 = r21;
        r47 = r22;
        r41.drawDrawable4(r42, r43, r44, r45, r46, r47, r48, r49);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x0c3d:
        r4 = r129 / 10;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r22 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r4 = r129 % 10;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r48 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        goto L_0x0c1e;
    L_0x0c5a:
        r4 = "12";
        r0 = r92;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x0cf6;
    L_0x0c65:
        r0 = r133;
        r4 = r0.mContext;	 Catch:{ Exception -> 0x024d }
        r67 = com.mediatek.watchapp.WatchApp.getSteps(r4);	 Catch:{ Exception -> 0x024d }
        r0 = r67;
        r0 = r0 / 10000;
        r91 = r0;
        r0 = r67;
        r4 = r0 % 10000;
        r0 = r4 / 1000;
        r95 = r0;
        r0 = r67;
        r4 = r0 % 10000;
        r4 = r4 % 1000;
        r96 = r4 / 100;
        r0 = r67;
        r4 = r0 % 10000;
        r4 = r4 % 1000;
        r4 = r4 % 100;
        r100 = r4 / 10;
        r0 = r67;
        r4 = r0 % 10000;
        r4 = r4 % 1000;
        r4 = r4 % 100;
        r4 = r4 % 10;
        r102 = r4 % 10;
        r0 = r120;
        r1 = r91;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r21 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r120;
        r1 = r95;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r22 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r120;
        r1 = r96;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r48 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r120;
        r1 = r100;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r49 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r120;
        r1 = r102;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r59 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r52 = 1;
        r50 = r133;
        r51 = r134;
        r53 = r12;
        r54 = r13;
        r55 = r21;
        r56 = r22;
        r57 = r48;
        r58 = r49;
        r50.drawDrawable5(r51, r52, r53, r54, r55, r56, r57, r58, r59);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x0cf6:
        r4 = "13";
        r0 = r92;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x0d54;
    L_0x0d01:
        r0 = r133;
        r4 = r0.mContext;	 Catch:{ Exception -> 0x024d }
        r121 = com.mediatek.watchapp.WatchApp.getRate(r4);	 Catch:{ Exception -> 0x024d }
        r91 = r121 / 100;
        r4 = r121 % 100;
        r95 = r4 / 10;
        r4 = r121 % 100;
        r4 = r4 % 10;
        r96 = r4 % 10;
        if (r120 == 0) goto L_0x0211;
    L_0x0d17:
        r0 = r120;
        r1 = r91;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r21 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r120;
        r1 = r95;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r22 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r120;
        r1 = r96;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r48 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r50 = r133;
        r51 = r134;
        r52 = r12;
        r53 = r13;
        r54 = r21;
        r55 = r22;
        r56 = r48;
        r50.drawDrawable3(r51, r52, r53, r54, r55, r56);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x0d54:
        r4 = "14";
        r0 = r92;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x0dd7;
    L_0x0d5f:
        r91 = r23 / 100;
        r4 = r23 % 100;
        r95 = r4 / 10;
        r4 = r23 % 100;
        r4 = r4 % 10;
        r96 = r4 % 10;
        r31 = 0;
        if (r91 != 0) goto L_0x0dc8;
    L_0x0d6f:
        r4 = 10;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r31 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
    L_0x0d7d:
        r0 = r120;
        r1 = r95;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r32 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r120;
        r1 = r96;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r33 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r34 = 0;
        r4 = r120.size();	 Catch:{ Exception -> 0x024d }
        r5 = 12;
        if (r4 != r5) goto L_0x0db1;
    L_0x0da3:
        r4 = 11;
        r0 = r120;
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r34 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
    L_0x0db1:
        r52 = 1;
        r50 = r133;
        r51 = r134;
        r53 = r12;
        r54 = r13;
        r55 = r31;
        r56 = r32;
        r57 = r33;
        r58 = r34;
        r50.drawDrawable4(r51, r52, r53, r54, r55, r56, r57, r58);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x0dc8:
        r0 = r120;
        r1 = r91;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r31 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        goto L_0x0d7d;
    L_0x0dd7:
        r4 = "15";
        r0 = r92;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x0df7;
    L_0x0de2:
        if (r56 == 0) goto L_0x0211;
    L_0x0de4:
        r0 = r133;
        r4 = r0.minute;	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r5 = r0.second;	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r1 = r134;
        r2 = r56;
        r0.drawSpecialSecond(r1, r2, r4, r5);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x0df7:
        r4 = "16";
        r0 = r92;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x0e77;
    L_0x0e02:
        r0 = r133;
        r4 = r0.mCalendar;	 Catch:{ Exception -> 0x024d }
        r0 = r4.year;	 Catch:{ Exception -> 0x024d }
        r132 = r0;
        r0 = r132;
        r0 = r0 / 1000;
        r91 = r0;
        r0 = r132;
        r4 = r0 % 1000;
        r95 = r4 / 100;
        r0 = r132;
        r4 = r0 % 1000;
        r4 = r4 % 100;
        r96 = r4 / 10;
        r0 = r132;
        r4 = r0 % 1000;
        r4 = r4 % 100;
        r4 = r4 % 10;
        r100 = r4 % 10;
        r0 = r120;
        r1 = r91;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r31 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r120;
        r1 = r95;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r32 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r120;
        r1 = r96;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r33 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r120;
        r1 = r100;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r34 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r52 = 1;
        r50 = r133;
        r51 = r134;
        r53 = r12;
        r54 = r13;
        r55 = r31;
        r56 = r32;
        r57 = r33;
        r58 = r34;
        r50.drawDrawable4(r51, r52, r53, r54, r55, r56, r57, r58);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x0e77:
        r4 = "17";
        r0 = r92;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x0ebc;
    L_0x0e82:
        r52 = new java.util.ArrayList;	 Catch:{ Exception -> 0x024d }
        r52.<init>();	 Catch:{ Exception -> 0x024d }
        r116 = r120.iterator();	 Catch:{ Exception -> 0x024d }
    L_0x0e8b:
        r4 = r116.hasNext();	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x0ea1;
    L_0x0e91:
        r115 = r116.next();	 Catch:{ Exception -> 0x024d }
        r115 = (com.mediatek.watchapp.view.ClockInfo.Num) r115;	 Catch:{ Exception -> 0x024d }
        r4 = r115.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r52;
        r0.add(r4);	 Catch:{ Exception -> 0x024d }
        goto L_0x0e8b;
    L_0x0ea1:
        r0 = r133;
        r4 = r0.centerX1;	 Catch:{ Exception -> 0x024d }
        r53 = r4 + r12;
        r0 = r133;
        r4 = r0.centerY1;	 Catch:{ Exception -> 0x024d }
        r54 = r4 + r13;
        r57 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;
        r58 = 0;
        r50 = r133;
        r51 = r134;
        r55 = r23;
        r50.drawBatteryPictureWithCircle(r51, r52, r53, r54, r55, r56, r57, r58);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x0ebc:
        r4 = "18";
        r0 = r92;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x0f25;
    L_0x0ec7:
        r0 = r133;
        r4 = r0.mContext;	 Catch:{ Exception -> 0x024d }
        r67 = com.mediatek.watchapp.WatchApp.getSteps(r4);	 Catch:{ Exception -> 0x024d }
        r52 = new java.util.ArrayList;	 Catch:{ Exception -> 0x024d }
        r52.<init>();	 Catch:{ Exception -> 0x024d }
        r116 = r120.iterator();	 Catch:{ Exception -> 0x024d }
    L_0x0ed8:
        r4 = r116.hasNext();	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x0eee;
    L_0x0ede:
        r115 = r116.next();	 Catch:{ Exception -> 0x024d }
        r115 = (com.mediatek.watchapp.view.ClockInfo.Num) r115;	 Catch:{ Exception -> 0x024d }
        r4 = r115.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r52;
        r0.add(r4);	 Catch:{ Exception -> 0x024d }
        goto L_0x0ed8;
    L_0x0eee:
        r4 = java.lang.Float.parseFloat(r78);	 Catch:{ Exception -> 0x024d }
        r63 = java.lang.Float.valueOf(r4);	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r0 = r0.mContext;	 Catch:{ Exception -> 0x024d }
        r61 = r0;
        r0 = r133;
        r4 = r0.centerX1;	 Catch:{ Exception -> 0x024d }
        r65 = r4 + r12;
        r0 = r133;
        r4 = r0.centerY1;	 Catch:{ Exception -> 0x024d }
        r66 = r4 + r13;
        r0 = r133;
        r4 = r0.parseClock;	 Catch:{ Exception -> 0x024d }
        r0 = r108;
        r4 = r4.get(r0);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo) r4;	 Catch:{ Exception -> 0x024d }
        r68 = r4.getColorArray();	 Catch:{ Exception -> 0x024d }
        r69 = 0;
        r60 = r133;
        r62 = r134;
        r64 = r52;
        r60.drawStepsPictureWithCircle(r61, r62, r63, r64, r65, r66, r67, r68, r69);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x0f25:
        r4 = "19";
        r0 = r92;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x0f63;
    L_0x0f30:
        r111 = r133.getMoonPhaseType();	 Catch:{ Exception -> 0x024d }
        if (r111 < 0) goto L_0x0211;
    L_0x0f36:
        r4 = r120.size();	 Catch:{ Exception -> 0x024d }
        r0 = r111;
        if (r0 >= r4) goto L_0x0211;
    L_0x0f3e:
        r0 = r120;
        r1 = r111;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r70 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r4 = r0.centerX1;	 Catch:{ Exception -> 0x024d }
        r71 = r4 + r12;
        r0 = r133;
        r4 = r0.centerY1;	 Catch:{ Exception -> 0x024d }
        r72 = r4 + r13;
        r73 = 1;
        r68 = r133;
        r69 = r134;
        r68.drawClockQuietPircture(r69, r70, r71, r72, r73);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x0f63:
        r4 = "31";
        r0 = r92;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x0fe3;
    L_0x0f6e:
        r0 = r133;
        r4 = r0.mCalendar;	 Catch:{ Exception -> 0x024d }
        r0 = r4.year;	 Catch:{ Exception -> 0x024d }
        r132 = r0;
        r0 = r132;
        r0 = r0 / 1000;
        r91 = r0;
        r0 = r132;
        r4 = r0 % 1000;
        r95 = r4 / 100;
        r0 = r132;
        r4 = r0 % 1000;
        r4 = r4 % 100;
        r96 = r4 / 10;
        r0 = r132;
        r4 = r0 % 1000;
        r4 = r4 % 100;
        r4 = r4 % 10;
        r100 = r4 % 10;
        r0 = r120;
        r1 = r91;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r31 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r120;
        r1 = r95;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r32 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r120;
        r1 = r96;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r33 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r120;
        r1 = r100;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r34 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r73 = 1;
        r71 = r133;
        r72 = r134;
        r74 = r12;
        r75 = r13;
        r76 = r31;
        r77 = r32;
        r78 = r33;
        r79 = r34;
        r71.drawDrawable4(r72, r73, r74, r75, r76, r77, r78, r79);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x0fe3:
        r4 = "32";
        r0 = r92;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 != 0) goto L_0x0211;
    L_0x0fee:
        r4 = "50";
        r0 = r92;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x1038;
    L_0x0ff9:
        r0 = r133;
        r4 = r0.mContext;	 Catch:{ Exception -> 0x024d }
        r121 = com.mediatek.watchapp.WatchApp.getUnreadPhone(r4);	 Catch:{ Exception -> 0x024d }
        r4 = 99;
        r0 = r121;
        if (r0 <= r4) goto L_0x1009;
    L_0x1007:
        r121 = 99;
    L_0x1009:
        r91 = r121 / 10;
        r95 = r121 % 10;
        r0 = r120;
        r1 = r91;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r21 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r120;
        r1 = r95;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r22 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        if (r121 <= 0) goto L_0x0211;
    L_0x102b:
        r17 = r133;
        r18 = r134;
        r19 = r12;
        r20 = r13;
        r17.drawDrawable2(r18, r19, r20, r21, r22);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x1038:
        r4 = "51";
        r0 = r92;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x1082;
    L_0x1043:
        r0 = r133;
        r4 = r0.mContext;	 Catch:{ Exception -> 0x024d }
        r121 = com.mediatek.watchapp.WatchApp.getUnreadSMS(r4);	 Catch:{ Exception -> 0x024d }
        r4 = 99;
        r0 = r121;
        if (r0 <= r4) goto L_0x1053;
    L_0x1051:
        r121 = 99;
    L_0x1053:
        r91 = r121 / 10;
        r95 = r121 % 10;
        r0 = r120;
        r1 = r91;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r21 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r120;
        r1 = r95;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r22 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        if (r121 <= 0) goto L_0x0211;
    L_0x1075:
        r17 = r133;
        r18 = r134;
        r19 = r12;
        r20 = r13;
        r17.drawDrawable2(r18, r19, r20, r21, r22);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x1082:
        r4 = "52";
        r0 = r92;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x10b7;
    L_0x108d:
        r0 = r133;
        r4 = r0.mContext;	 Catch:{ Exception -> 0x024d }
        r4 = com.mediatek.watchapp.WatchApp.getBatteryLevel(r4);	 Catch:{ Exception -> 0x024d }
        r0 = (int) r4;	 Catch:{ Exception -> 0x024d }
        r76 = r0;
        r0 = r133;
        r0 = r0.mContext;	 Catch:{ Exception -> 0x024d }
        r72 = r0;
        r0 = r133;
        r4 = r0.centerX1;	 Catch:{ Exception -> 0x024d }
        r74 = r4 + r12;
        r0 = r133;
        r4 = r0.centerY1;	 Catch:{ Exception -> 0x024d }
        r75 = r4 + r13;
        r80 = 0;
        r71 = r133;
        r73 = r134;
        r79 = r56;
        r71.drawBatteryCircle(r72, r73, r74, r75, r76, r77, r78, r79, r80);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x10b7:
        r4 = "53";
        r0 = r92;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x111e;
    L_0x10c2:
        r0 = r133;
        r4 = r0.mContext;	 Catch:{ Exception -> 0x024d }
        r67 = com.mediatek.watchapp.WatchApp.getSteps(r4);	 Catch:{ Exception -> 0x024d }
        r52 = new java.util.ArrayList;	 Catch:{ Exception -> 0x024d }
        r52.<init>();	 Catch:{ Exception -> 0x024d }
        r116 = r120.iterator();	 Catch:{ Exception -> 0x024d }
    L_0x10d3:
        r4 = r116.hasNext();	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x10e9;
    L_0x10d9:
        r115 = r116.next();	 Catch:{ Exception -> 0x024d }
        r115 = (com.mediatek.watchapp.view.ClockInfo.Num) r115;	 Catch:{ Exception -> 0x024d }
        r4 = r115.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r52;
        r0.add(r4);	 Catch:{ Exception -> 0x024d }
        goto L_0x10d3;
    L_0x10e9:
        r0 = r133;
        r0 = r0.mContext;	 Catch:{ Exception -> 0x024d }
        r80 = r0;
        r0 = r133;
        r4 = r0.centerX1;	 Catch:{ Exception -> 0x024d }
        r83 = r4 + r12;
        r0 = r133;
        r4 = r0.centerY1;	 Catch:{ Exception -> 0x024d }
        r84 = r4 + r13;
        r0 = r133;
        r4 = r0.parseClock;	 Catch:{ Exception -> 0x024d }
        r0 = r108;
        r4 = r4.get(r0);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo) r4;	 Catch:{ Exception -> 0x024d }
        r88 = r4.getColorArray();	 Catch:{ Exception -> 0x024d }
        r89 = 0;
        r79 = r133;
        r81 = r134;
        r82 = r52;
        r85 = r67;
        r86 = r77;
        r87 = r78;
        r79.drawStepsPictureWithCircle2(r80, r81, r82, r83, r84, r85, r86, r87, r88, r89);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x111e:
        r4 = "54";
        r0 = r92;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x11a5;
    L_0x1129:
        r0 = r133;
        r4 = r0.mContext;	 Catch:{ Exception -> 0x024d }
        r4 = com.mediatek.watchapp.WatchApp.getCalories(r4);	 Catch:{ Exception -> 0x024d }
        r4 = java.lang.Float.parseFloat(r4);	 Catch:{ Exception -> 0x024d }
        r0 = (int) r4;	 Catch:{ Exception -> 0x024d }
        r110 = r0;
        r0 = r110;
        r0 = r0 / 1000;
        r91 = r0;
        r0 = r110;
        r4 = r0 % 1000;
        r95 = r4 / 100;
        r0 = r110;
        r4 = r0 % 1000;
        r4 = r4 % 100;
        r96 = r4 / 10;
        r0 = r110;
        r4 = r0 % 1000;
        r4 = r4 % 100;
        r4 = r4 % 10;
        r100 = r4 % 10;
        r0 = r120;
        r1 = r91;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r21 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r120;
        r1 = r95;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r22 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r120;
        r1 = r96;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r48 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r120;
        r1 = r100;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r49 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r81 = 1;
        r79 = r133;
        r80 = r134;
        r82 = r12;
        r83 = r13;
        r84 = r21;
        r85 = r22;
        r86 = r48;
        r87 = r49;
        r79.drawDrawable4(r80, r81, r82, r83, r84, r85, r86, r87);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x11a5:
        r4 = "55";
        r0 = r92;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x11f1;
    L_0x11b0:
        r0 = r133;
        r4 = r0.mContext;	 Catch:{ Exception -> 0x024d }
        r122 = com.mediatek.watchapp.WatchApp.getUnreadPhone(r4);	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r4 = r0.mContext;	 Catch:{ Exception -> 0x024d }
        r123 = com.mediatek.watchapp.WatchApp.getUnreadSMS(r4);	 Catch:{ Exception -> 0x024d }
        r121 = r122 + r123;
        r91 = r121 / 10;
        r95 = r121 % 10;
        r0 = r120;
        r1 = r91;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r21 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        r0 = r120;
        r1 = r95;
        r4 = r0.get(r1);	 Catch:{ Exception -> 0x024d }
        r4 = (com.mediatek.watchapp.view.ClockInfo.Num) r4;	 Catch:{ Exception -> 0x024d }
        r22 = r4.getNumDrawable();	 Catch:{ Exception -> 0x024d }
        if (r121 <= 0) goto L_0x0211;
    L_0x11e4:
        r17 = r133;
        r18 = r134;
        r19 = r12;
        r20 = r13;
        r17.drawDrawable2(r18, r19, r20, r21, r22);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x11f1:
        r4 = "97";
        r0 = r92;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x121f;
    L_0x11fc:
        r0 = r133;
        r0 = r0.mContext;	 Catch:{ Exception -> 0x024d }
        r80 = r0;
        r82 = java.lang.Integer.parseInt(r16);	 Catch:{ Exception -> 0x024d }
        r83 = java.lang.Integer.parseInt(r101);	 Catch:{ Exception -> 0x024d }
        r84 = java.lang.Integer.parseInt(r130);	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r4 = r0.mContext;	 Catch:{ Exception -> 0x024d }
        r85 = com.mediatek.watchapp.WatchApp.getSteps(r4);	 Catch:{ Exception -> 0x024d }
        r79 = r133;
        r81 = r134;
        r79.drawPedometer(r80, r81, r82, r83, r84, r85);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x121f:
        r4 = "98";
        r0 = r92;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x124d;
    L_0x122a:
        r0 = r133;
        r0 = r0.mContext;	 Catch:{ Exception -> 0x024d }
        r80 = r0;
        r82 = java.lang.Integer.parseInt(r16);	 Catch:{ Exception -> 0x024d }
        r83 = java.lang.Integer.parseInt(r101);	 Catch:{ Exception -> 0x024d }
        r84 = java.lang.Integer.parseInt(r130);	 Catch:{ Exception -> 0x024d }
        r0 = r133;
        r4 = r0.mContext;	 Catch:{ Exception -> 0x024d }
        r85 = com.mediatek.watchapp.WatchApp.getRate(r4);	 Catch:{ Exception -> 0x024d }
        r79 = r133;
        r81 = r134;
        r79.drawHeartRate(r80, r81, r82, r83, r84, r85);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x124d:
        r4 = "99";
        r0 = r92;
        r4 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x0211;
    L_0x1258:
        r0 = r133;
        r4 = r0.mContext;	 Catch:{ Exception -> 0x024d }
        r4 = com.mediatek.watchapp.WatchApp.getIsBatteryCharging(r4);	 Catch:{ Exception -> 0x024d }
        if (r4 == 0) goto L_0x0211;
    L_0x1262:
        r4 = "0";
        r0 = r99;
        r109 = r0.equals(r4);	 Catch:{ Exception -> 0x024d }
        if (r109 == 0) goto L_0x1294;
    L_0x126d:
        r0 = r133;
        r0 = r0.mDrawBattery;	 Catch:{ Exception -> 0x024d }
        r82 = r0;
    L_0x1273:
        r0 = r133;
        r0 = r0.mContext;	 Catch:{ Exception -> 0x024d }
        r80 = r0;
        r0 = r133;
        r4 = r0.centerX1;	 Catch:{ Exception -> 0x024d }
        r83 = r4 + r12;
        r0 = r133;
        r4 = r0.centerY1;	 Catch:{ Exception -> 0x024d }
        r84 = r4 + r13;
        if (r109 == 0) goto L_0x129b;
    L_0x1287:
        r85 = -1;
    L_0x1289:
        r86 = 1;
        r79 = r133;
        r81 = r134;
        r79.drawChargingInfo(r80, r81, r82, r83, r84, r85, r86);	 Catch:{ Exception -> 0x024d }
        goto L_0x0211;
    L_0x1294:
        r0 = r133;
        r0 = r0.mDrawBatteryGray;	 Catch:{ Exception -> 0x024d }
        r82 = r0;
        goto L_0x1273;
    L_0x129b:
        r85 = -16777216; // 0xffffffffff000000 float:-1.7014118E38 double:NaN;
        goto L_0x1289;
    L_0x129e:
        r4 = " ";
        r5 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x024d }
        r5.<init>();	 Catch:{ Exception -> 0x024d }
        r7 = " ";
        r5 = r5.append(r7);	 Catch:{ Exception -> 0x024d }
        r8 = java.lang.System.currentTimeMillis();	 Catch:{ Exception -> 0x024d }
        r8 = r8 - r118;
        if (r126 == 0) goto L_0x12b8;
    L_0x12b5:
        r134.restore();	 Catch:{ Exception -> 0x024d }
    L_0x12b8:
        r4 = 0;
        r0 = r133;
        r0.mChanged = r4;	 Catch:{ Exception -> 0x024d }
        goto L_0x024e;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.watchapp.MyAnalogClock3.onDraw(android.graphics.Canvas):void");
    }

    private void resetScreen(int availableWidth) {
        this.SCREEN_WIDE = availableWidth;
    }

    public void drawHourhand(Canvas canvas, String angle, float mHour, int mulrotate, int direction, Drawable hourHand, boolean changed, int iCenterX, int iCenterY) {
        Float startAngle = Float.valueOf(angle);
        if (hourHand != null) {
            canvas.save();
            if (direction == 1) {
                canvas.rotate(startAngle.floatValue() + (((mHour / 12.0f) * 360.0f) * ((float) mulrotate)), (float) (this.centerX1 + iCenterX), (float) (this.centerY1 + iCenterY));
            } else if (direction == 2) {
                canvas.rotate(startAngle.floatValue() - (((mHour / 12.0f) * 360.0f) * ((float) mulrotate)), (float) (this.centerX1 + iCenterX), (float) (this.centerY1 + iCenterY));
            }
            if (changed) {
                int w = hourHand.getIntrinsicWidth();
                int h = hourHand.getIntrinsicHeight();
                hourHand.setBounds((this.centerX1 + iCenterX) - (w / 2), (this.centerY1 + iCenterY) - (h / 2), (this.centerX1 + iCenterX) + (w / 2), (this.centerY1 + iCenterY) + (h / 2));
            }
            hourHand.draw(canvas);
            canvas.restore();
        }
    }

    public void draw24Hourhand(Canvas canvas, String angle, float mHour, Drawable hourHand, boolean changed, int iCenterX, int iCenterY) {
        Float startAngle = Float.valueOf(angle);
        if (hourHand != null) {
            canvas.save();
            canvas.rotate(startAngle.floatValue() + ((mHour / 24.0f) * 360.0f), (float) (this.centerX1 + iCenterX), (float) (this.centerY1 + iCenterY));
            if (changed) {
                int w = hourHand.getIntrinsicWidth();
                int h = hourHand.getIntrinsicHeight();
                hourHand.setBounds((this.centerX1 + iCenterX) - (w / 2), (this.centerY1 + iCenterY) - (h / 2), (this.centerX1 + iCenterX) + (w / 2), (this.centerY1 + iCenterY) + (h / 2));
            }
            hourHand.draw(canvas);
            canvas.restore();
        }
    }

    public void drawMinuteHand(Canvas canvas, String angle, float mMinutes, int mulrotate, int direction, Drawable minuteHand, boolean changed, int iCenterX, int iCenterY) {
        Float startAngle = Float.valueOf(angle);
        if (minuteHand != null) {
            canvas.save();
            if (direction == 1) {
                canvas.rotate(startAngle.floatValue() + (((mMinutes / 60.0f) * 360.0f) * ((float) mulrotate)), (float) (this.centerX1 + iCenterX), (float) (this.centerY1 + iCenterY));
            } else if (direction == 2) {
                canvas.rotate(startAngle.floatValue() - (((mMinutes / 60.0f) * 360.0f) * ((float) mulrotate)), (float) (this.centerX1 + iCenterX), (float) (this.centerY1 + iCenterY));
            }
            if (changed) {
                int w = minuteHand.getIntrinsicWidth();
                int h = minuteHand.getIntrinsicHeight();
                minuteHand.setBounds((this.centerX1 + iCenterX) - (w / 2), (this.centerY1 + iCenterY) - (h / 2), (this.centerX1 + iCenterX) + (w / 2), (this.centerY1 + iCenterY) + (h / 2));
            }
            minuteHand.draw(canvas);
            canvas.restore();
        }
    }

    public void drawSecondHand(Canvas canvas, String startAngle, int mulrotate, int direction, Drawable mSecondHand, boolean changed, int iCenterX, int iCenterY) {
        Float startAngleF = Float.valueOf(startAngle);
        long millis = System.currentTimeMillis() % 60000;
        if (mSecondHand != null) {
            canvas.save();
            if (direction == 1) {
                canvas.rotate(startAngleF.floatValue() + ((((float) (((long) mulrotate) * millis)) * 6.0f) / 1000.0f), (float) (this.centerX1 + iCenterX), (float) (this.centerY1 + iCenterY));
            } else if (direction == 2) {
                canvas.rotate(startAngleF.floatValue() - (((((float) millis) * 6.0f) * ((float) mulrotate)) / 1000.0f), (float) (this.centerX1 + iCenterX), (float) (this.centerY1 + iCenterY));
            }
            if (changed) {
                int w = mSecondHand.getIntrinsicWidth();
                int h = mSecondHand.getIntrinsicHeight();
                mSecondHand.setBounds((this.centerX1 + iCenterX) - (w / 2), (this.centerY1 + iCenterY) - (h / 2), (this.centerX1 + iCenterX) + (w / 2), (this.centerY1 + iCenterY) + (h / 2));
            }
            mSecondHand.draw(canvas);
            canvas.restore();
        }
    }

    public void drawBalanceHand(Canvas canvas, int mulrotate, Drawable mSecondHand, boolean changed, int iCenterX, int iCenterY) {
        long now = System.currentTimeMillis();
        long millis = now % 60000;
        long millis2 = now % 2000;
        if (mSecondHand != null) {
            canvas.save();
            if (millis2 < 1000) {
                canvas.rotate(((((float) millis2) * 6.0f) * ((float) mulrotate)) / 1000.0f, (float) (this.centerX1 + iCenterX), (float) (this.centerY1 + iCenterY));
            } else {
                canvas.rotate(((((float) mulrotate) * 6000.0f) / 1000.0f) - (((((float) (millis2 - 1000)) * 6.0f) * ((float) mulrotate)) / 1000.0f), (float) (this.centerX1 + iCenterX), (float) (this.centerY1 + iCenterY));
            }
            if (changed) {
                int w = mSecondHand.getIntrinsicWidth();
                int h = mSecondHand.getIntrinsicHeight();
                mSecondHand.setBounds((this.centerX1 + iCenterX) - (w / 2), (this.centerY1 + iCenterY) - (h / 2), (this.centerX1 + iCenterX) + (w / 2), (this.centerY1 + iCenterY) + (h / 2));
            }
            mSecondHand.draw(canvas);
            canvas.restore();
        }
    }

    public void drawMonthhand(Canvas canvas, String angle, int month, Drawable hourHand, boolean changed, int iCenterX, int iCenterY) {
        Float startAngle = Float.valueOf(angle);
        if (hourHand != null) {
            canvas.save();
            canvas.rotate(startAngle.floatValue() + ((((float) (month + 1)) / 12.0f) * 360.0f), (float) (this.centerX1 + iCenterX), (float) (this.centerY1 + iCenterY));
            if (changed) {
                int w = hourHand.getIntrinsicWidth();
                int h = hourHand.getIntrinsicHeight();
                hourHand.setBounds((this.centerX1 + iCenterX) - (w / 2), (this.centerY1 + iCenterY) - (h / 2), (this.centerX1 + iCenterX) + (w / 2), (this.centerY1 + iCenterY) + (h / 2));
            }
            hourHand.draw(canvas);
            canvas.restore();
        }
    }

    public void drawMonthDayhand(Canvas canvas, String angle, int direction, int monthday, Drawable hourHand, boolean changed, int iCenterX, int iCenterY) {
        int monthday2 = monthday - 1;
        Float startAngle = Float.valueOf(angle);
        if (direction == 2) {
            monthday2 = -monthday2;
        }
        if (hourHand != null) {
            canvas.save();
            canvas.rotate(startAngle.floatValue() + ((((float) monthday2) / 31.0f) * 360.0f), (float) (this.centerX1 + iCenterX), (float) (this.centerY1 + iCenterY));
            if (changed) {
                int w = hourHand.getIntrinsicWidth();
                int h = hourHand.getIntrinsicHeight();
                hourHand.setBounds((this.centerX1 + iCenterX) - (w / 2), (this.centerY1 + iCenterY) - (h / 2), (this.centerX1 + iCenterX) + (w / 2), (this.centerY1 + iCenterY) + (h / 2));
            }
            hourHand.draw(canvas);
            canvas.restore();
        }
    }

    public void drawWeekhand(Canvas canvas, String angle, int week, Drawable hourHand, boolean changed, int iCenterX, int iCenterY) {
        Float startAngle = Float.valueOf(angle);
        if (hourHand != null) {
            canvas.save();
            canvas.rotate(startAngle.floatValue() + ((((float) (week - 1)) / 7.0f) * 360.0f), (float) (this.centerX1 + iCenterX), (float) (this.centerY1 + iCenterY));
            if (changed) {
                int w = hourHand.getIntrinsicWidth();
                int h = hourHand.getIntrinsicHeight();
                hourHand.setBounds((this.centerX1 + iCenterX) - (w / 2), (this.centerY1 + iCenterY) - (h / 2), (this.centerX1 + iCenterX) + (w / 2), (this.centerY1 + iCenterY) + (h / 2));
            }
            hourHand.draw(canvas);
            canvas.restore();
        }
    }

    public void drawBatteryhand(Canvas canvas, String angle, String startangle, int direction, int mulrotate, int batteryLevel, Drawable hourHand, boolean changed, int iCenterX, int iCenterY) {
        Float startAngle = Float.valueOf(angle);
        if (hourHand != null) {
            float degrees;
            canvas.save();
            if (direction == 2) {
                batteryLevel = -batteryLevel;
            }
            if (mulrotate >= 0) {
                degrees = (((((float) batteryLevel) / 100.0f) * 180.0f) * ((float) mulrotate)) + startAngle.floatValue();
            } else {
                degrees = (((((float) batteryLevel) / 100.0f) * 180.0f) / ((float) Math.abs(mulrotate))) + startAngle.floatValue();
            }
            canvas.rotate(degrees, (float) (this.centerX1 + iCenterX), (float) (this.centerY1 + iCenterY));
            if (changed) {
                int w = hourHand.getIntrinsicWidth();
                int h = hourHand.getIntrinsicHeight();
                hourHand.setBounds((this.centerX1 + iCenterX) - (w / 2), (this.centerY1 + iCenterY) - (h / 2), (this.centerX1 + iCenterX) + (w / 2), (this.centerY1 + iCenterY) + (h / 2));
            }
            hourHand.draw(canvas);
            canvas.restore();
        }
    }

    public void drawBatteryhand2(Canvas canvas, String angle, String startangle, int direction, int mulrotate, int batteryLevel, Drawable hourHand, boolean changed, int iCenterX, int iCenterY) {
        Float startAnglef = Float.valueOf(startangle);
        Float anglef = Float.valueOf(angle);
        if (hourHand != null) {
            float degrees;
            canvas.save();
            if (direction == 2) {
                batteryLevel = -batteryLevel;
            }
            if (mulrotate >= 0) {
                degrees = (((((float) batteryLevel) / 100.0f) * anglef.floatValue()) * ((float) mulrotate)) + startAnglef.floatValue();
            } else {
                degrees = (((((float) batteryLevel) / 100.0f) * anglef.floatValue()) / ((float) Math.abs(mulrotate))) + startAnglef.floatValue();
            }
            canvas.rotate(degrees, (float) (this.centerX1 + iCenterX), (float) (this.centerY1 + iCenterY));
            if (changed) {
                int w = hourHand.getIntrinsicWidth();
                int h = hourHand.getIntrinsicHeight();
                hourHand.setBounds((this.centerX1 + iCenterX) - (w / 2), (this.centerY1 + iCenterY) - (h / 2), (this.centerX1 + iCenterX) + (w / 2), (this.centerY1 + iCenterY) + (h / 2));
            }
            hourHand.draw(canvas);
            canvas.restore();
        }
    }

    public void drawRotateModeHand(Canvas canvas, String rotateMode, String angle, String startangle, int direction, int mulrotate, Drawable hand, boolean changed, int iCenterX, int iCenterY) {
        float rate = 0.0f;
        Float startAnglef = Float.valueOf(startangle);
        Float anglef = Float.valueOf(angle);
        int suggestSteps = WatchApp.getTargetSteps(this.mContext);
        int step = WatchApp.getSteps(this.mContext);
        if (rotateMode.equals("1")) {
            rate = (this.mHour / 12.0f) * anglef.floatValue();
        } else if (rotateMode.equals("2")) {
            rate = (this.mMinutes / 60.0f) * anglef.floatValue();
        } else if (rotateMode.equals("3")) {
            rate = (((float) (System.currentTimeMillis() % 60000)) * anglef.floatValue()) / 60000.0f;
        } else if (rotateMode.equals("4")) {
            rate = (((float) (this.mMonth + 1)) / 12.0f) * anglef.floatValue();
        } else if (rotateMode.equals("5")) {
            rate = (((float) (this.mWeek - 1)) / 7.0f) * anglef.floatValue();
        } else if (rotateMode.equals("6")) {
            rate = (((float) ((int) WatchApp.getBatteryLevel(this.mContext))) / 100.0f) * anglef.floatValue();
        } else if (rotateMode.equals("7")) {
            rate = (this.mHour / 24.0f) * anglef.floatValue();
        } else if (rotateMode.equals("8")) {
            rate = (((float) step) / ((float) suggestSteps)) * anglef.floatValue();
        } else if (rotateMode.equals("11")) {
            rate = (((float) this.mDate) / 31.0f) * anglef.floatValue();
        }
        if (hand != null) {
            float degrees;
            canvas.save();
            if (direction == 2) {
                rate = -rate;
            }
            if (mulrotate >= 0) {
                degrees = (((float) mulrotate) * rate) + startAnglef.floatValue();
            } else {
                degrees = (rate / ((float) Math.abs(mulrotate))) + startAnglef.floatValue();
            }
            canvas.rotate(degrees, (float) (this.centerX1 + iCenterX), (float) (this.centerY1 + iCenterY));
            if (changed) {
                int w = hand.getIntrinsicWidth();
                int h = hand.getIntrinsicHeight();
                hand.setBounds((this.centerX1 + iCenterX) - (w / 2), (this.centerY1 + iCenterY) - (h / 2), (this.centerX1 + iCenterX) + (w / 2), (this.centerY1 + iCenterY) + (h / 2));
            }
            hand.draw(canvas);
            canvas.restore();
        }
    }

    private void drawDial(Canvas canvas, int iCenterX, int iCenterY, Drawable dial) {
        if (dial != null) {
            int w = dial.getIntrinsicWidth();
            int h = dial.getIntrinsicHeight();
            if (this.mChanged) {
                dial.setBounds((this.centerX1 + iCenterX) - (w / 2), (this.centerY1 + iCenterY) - (h / 2), (this.centerX1 + iCenterX) + (w / 2), (this.centerY1 + iCenterY) + (h / 2));
            }
            dial.draw(canvas);
        }
    }

    private void drawDrawable2(Canvas canvas, int iCenterX, int iCenterY, Drawable drawable1, Drawable drawable2) {
        if (drawable2 != null) {
            int w = drawable2.getIntrinsicWidth();
            int h = drawable2.getIntrinsicHeight();
            if (this.mChanged) {
                drawable1.setBounds((this.centerX1 + iCenterX) - w, (this.centerY1 + iCenterY) - (h / 2), this.centerX1 + iCenterX, (this.centerY1 + iCenterY) + (h / 2));
                drawable1.draw(canvas);
                drawable2.setBounds(this.centerX1 + iCenterX, (this.centerY1 + iCenterY) - (h / 2), (this.centerX1 + iCenterX) + w, (this.centerY1 + iCenterY) + (h / 2));
                drawable2.draw(canvas);
            }
        }
    }

    private void drawDrawable3(Canvas canvas, int iCenterX, int iCenterY, Drawable drawable1, Drawable drawable2, Drawable drawable3) {
        int w = drawable1.getIntrinsicWidth();
        int h = drawable1.getIntrinsicHeight();
        int startX = (this.centerX1 + iCenterX) - ((w * 3) / 2);
        int startY = (this.centerY1 + iCenterY) - (h / 2);
        if (this.mChanged) {
            drawable1.setBounds(startX, startY, startX + w, startY + h);
            drawable1.draw(canvas);
            drawable2.setBounds(startX + w, startY, (w * 2) + startX, startY + h);
            drawable2.draw(canvas);
            drawable3.setBounds((w * 2) + startX, startY, (w * 3) + startX, startY + h);
            drawable3.draw(canvas);
        }
    }

    private void drawDrawable4(Canvas canvas, boolean minus, int iCenterX, int iCenterY, Drawable drawable1, Drawable drawable2, Drawable drawable3, Drawable drawable4) {
        if (drawable3 != null) {
            int w = drawable3.getIntrinsicWidth();
            int h = drawable3.getIntrinsicHeight();
            if (this.mChanged) {
                Drawable drawable;
                if (drawable1 != null) {
                    drawable = drawable1;
                    drawable.setBounds(((this.centerX1 + iCenterX) - w) - drawable1.getIntrinsicWidth(), (this.centerY1 + iCenterY) - (h / 2), (this.centerX1 + iCenterX) - w, (this.centerY1 + iCenterY) + (h / 2));
                    if (minus) {
                        drawable1.draw(canvas);
                    }
                }
                if (drawable2 != null) {
                    drawable2.setBounds((this.centerX1 + iCenterX) - w, (this.centerY1 + iCenterY) - (h / 2), this.centerX1 + iCenterX, (this.centerY1 + iCenterY) + (h / 2));
                    drawable2.draw(canvas);
                }
                if (drawable3 != null) {
                    drawable3.setBounds(this.centerX1 + iCenterX, (this.centerY1 + iCenterY) - (h / 2), (this.centerX1 + iCenterX) + w, (this.centerY1 + iCenterY) + (h / 2));
                    drawable3.draw(canvas);
                }
                if (drawable4 != null) {
                    int w4 = drawable4.getIntrinsicWidth();
                    drawable = drawable4;
                    drawable.setBounds((this.centerX1 + iCenterX) + w, ((this.centerY1 + iCenterY) + (h / 2)) - drawable4.getIntrinsicHeight(), ((this.centerX1 + iCenterX) + w) + w4, (this.centerY1 + iCenterY) + (h / 2));
                    drawable4.draw(canvas);
                }
            }
        }
    }

    private void drawDrawable5(Canvas canvas, boolean ispoint, int iCenterX, int iCenterY, Drawable drawable1, Drawable drawable2, Drawable drawable3, Drawable drawable4, Drawable drawable5) {
        if (drawable1 != null) {
            int w = drawable1.getIntrinsicWidth();
            int h = drawable1.getIntrinsicHeight();
            int w2 = drawable3.getIntrinsicWidth();
            int h2 = drawable3.getIntrinsicHeight();
            if (this.mChanged) {
                drawable1.setBounds(((this.centerX1 + iCenterX) - (w2 / 2)) - (w * 2), (this.centerY1 + iCenterY) - (h / 2), ((this.centerX1 + iCenterX) - (w2 / 2)) - w, (this.centerY1 + iCenterY) + (h / 2));
                drawable1.draw(canvas);
                drawable2.setBounds(((this.centerX1 + iCenterX) - (w2 / 2)) - w, (this.centerY1 + iCenterY) - (h / 2), (this.centerX1 + iCenterX) - (w2 / 2), (this.centerY1 + iCenterY) + (h / 2));
                drawable2.draw(canvas);
                drawable3.setBounds((this.centerX1 + iCenterX) - (w2 / 2), (this.centerY1 + iCenterY) - (h / 2), (this.centerX1 + iCenterX) + (w2 / 2), (this.centerY1 + iCenterY) + (h / 2));
                if (ispoint) {
                    drawable3.draw(canvas);
                } else if (this.mCalendar.second % 2 == 0) {
                    drawable3.draw(canvas);
                }
                drawable4.setBounds((this.centerX1 + iCenterX) + (w2 / 2), (this.centerY1 + iCenterY) - (h / 2), ((this.centerX1 + iCenterX) + (w2 / 2)) + w, (this.centerY1 + iCenterY) + (h / 2));
                drawable4.draw(canvas);
                drawable5.setBounds(((this.centerX1 + iCenterX) + (w2 / 2)) + w, (this.centerY1 + iCenterY) - (h / 2), ((this.centerX1 + iCenterX) + (w2 / 2)) + (w * 2), (this.centerY1 + iCenterY) + (h / 2));
                drawable5.draw(canvas);
            }
        }
    }

    private void drawDrawable6(Canvas canvas, boolean ispoint, int iCenterX, int iCenterY, Drawable drawable1, Drawable drawable2, Drawable drawable3, Drawable drawable4, Drawable drawable5, Drawable drawable6) {
        if (drawable1 != null) {
            int w = drawable1.getIntrinsicWidth();
            int h = drawable1.getIntrinsicHeight();
            int w2 = drawable3.getIntrinsicWidth();
            int h2 = drawable3.getIntrinsicHeight();
            int w6 = 0;
            if (drawable6 != null) {
                w6 = drawable6.getIntrinsicWidth();
            }
            int startX = (this.centerX1 + iCenterX) - ((((w * 4) + w2) + w6) / 2);
            int startY = (this.centerY1 + iCenterY) - (h / 2);
            if (this.mChanged) {
                drawable1.setBounds(startX, startY, startX + w, startY + h);
                drawable1.draw(canvas);
                drawable2.setBounds(startX + w, startY, (w * 2) + startX, startY + h);
                drawable2.draw(canvas);
                drawable3.setBounds((w * 2) + startX, startY, ((w * 2) + startX) + w2, startY + h);
                if (ispoint) {
                    drawable3.draw(canvas);
                } else if (this.mCalendar.second % 2 == 0) {
                    drawable3.draw(canvas);
                }
                drawable4.setBounds(((w * 2) + startX) + w2, startY, ((w * 3) + startX) + w2, startY + h);
                drawable4.draw(canvas);
                drawable5.setBounds(((w * 3) + startX) + w2, startY, ((w * 4) + startX) + w2, startY + h);
                drawable5.draw(canvas);
                if (drawable6 != null) {
                    drawable6.setBounds(((w * 4) + startX) + w2, startY, (((w * 4) + startX) + w2) + w6, startY + h);
                    drawable6.draw(canvas);
                }
            }
        }
    }

    private void drawDrawable10(Canvas canvas, int iCenterX, int iCenterY, Drawable drawable1, Drawable drawable2, Drawable drawable3, Drawable drawable4, Drawable drawable5, Drawable drawable6, Drawable drawable7, Drawable drawable8, Drawable drawable9, Drawable drawable10) {
        if (drawable1 != null) {
            int w = drawable1.getIntrinsicWidth();
            int h = drawable1.getIntrinsicHeight();
            if (this.mChanged) {
                drawable1.setBounds((this.centerX1 + iCenterX) - (w * 5), (this.centerY1 + iCenterY) - (h / 2), (this.centerX1 + iCenterX) - (w * 4), (this.centerY1 + iCenterY) + (h / 2));
                drawable1.draw(canvas);
                drawable2.setBounds((this.centerX1 + iCenterX) - (w * 4), (this.centerY1 + iCenterY) - (h / 2), (this.centerX1 + iCenterX) - (w * 3), (this.centerY1 + iCenterY) + (h / 2));
                drawable2.draw(canvas);
                drawable3.setBounds((this.centerX1 + iCenterX) - (w * 3), (this.centerY1 + iCenterY) - (h / 2), (this.centerX1 + iCenterX) - (w * 2), (this.centerY1 + iCenterY) + (h / 2));
                drawable3.draw(canvas);
                drawable4.setBounds((this.centerX1 + iCenterX) - (w * 2), (this.centerY1 + iCenterY) - (h / 2), (this.centerX1 + iCenterX) - w, (this.centerY1 + iCenterY) + (h / 2));
                drawable4.draw(canvas);
                drawable5.setBounds((this.centerX1 + iCenterX) - w, (this.centerY1 + iCenterY) - (h / 2), this.centerX1 + iCenterX, (this.centerY1 + iCenterY) + (h / 2));
                drawable5.draw(canvas);
                drawable6.setBounds(this.centerX1 + iCenterX, (this.centerY1 + iCenterY) - (h / 2), (this.centerX1 + iCenterX) + w, (this.centerY1 + iCenterY) + (h / 2));
                drawable6.draw(canvas);
                drawable7.setBounds((this.centerX1 + iCenterX) + w, (this.centerY1 + iCenterY) - (h / 2), (this.centerX1 + iCenterX) + (w * 2), (this.centerY1 + iCenterY) + (h / 2));
                drawable7.draw(canvas);
                drawable8.setBounds((this.centerX1 + iCenterX) + (w * 2), (this.centerY1 + iCenterY) - (h / 2), (this.centerX1 + iCenterX) + (w * 3), (this.centerY1 + iCenterY) + (h / 2));
                drawable8.draw(canvas);
                drawable9.setBounds((this.centerX1 + iCenterX) + (w * 3), (this.centerY1 + iCenterY) - (h / 2), (this.centerX1 + iCenterX) + (w * 4), (this.centerY1 + iCenterY) + (h / 2));
                drawable9.draw(canvas);
                drawable10.setBounds((this.centerX1 + iCenterX) + (w * 4), (this.centerY1 + iCenterY) - (h / 2), (this.centerX1 + iCenterX) + (w * 5), (this.centerY1 + iCenterY) + (h / 2));
                drawable10.draw(canvas);
            }
        }
    }

    private void updateContentDescription(Time time) {
        setContentDescription(DateUtils.formatDateTime(getContext(), time.toMillis(false), 129));
    }

    private Resources getResource(Context context, String themePackage) {
        if (themePackage == null) {
            return null;
        }
        Resources themeResources = null;
        try {
            themeResources = context.getPackageManager().getResourcesForApplication(themePackage.toString());
        } catch (NameNotFoundException e) {
        }
        return themeResources;
    }

    private static String get_cur_theme_package(Context context) {
        return ((installedClock) WatchApp.getInstalledClocks().get(WatchApp.getClockIndex(context) - ClockUtil.mClockList.length)).pkg;
    }

    private Drawable getDrawableRes(Resources r, String pkg, String id_name) {
        if (this.mClockskinPath == null) {
            Drawable ret = null;
            int ID = r.getIdentifier(id_name.split("\\.")[0], "drawable", pkg);
            if (ID != 0) {
                ret = r.getDrawable(ID);
            }
            return ret;
        }
        try {
            return getImageDrawable(id_name);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Drawable getImageDrawable(String path) throws IOException {
        String filepath = this.mClockskinPath + File.separator + path;
        Log.d("xiaocai", "imageFilepath:" + filepath);
        if (!new File(filepath).exists()) {
            return null;
        }
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) this.mContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        BitmapDrawable bd = new BitmapDrawable(new Resources(this.mContext.getAssets(), metrics, null), BitmapFactory.decodeFile(filepath));
        this.maxWidth = Math.max(this.maxWidth, bd.getIntrinsicWidth());
        if (this.maxWidth > 400) {
            this.maxWidth = 400;
        }
        return bd;
    }

    public XmlPullParser getXmlParser(String xmlName) throws XmlPullParserException, IOException {
        String filepath = this.mClockskinPath + File.separator + xmlName + ".xml";
        if (!new File(filepath).exists()) {
            return null;
        }
        InputStream slideInputStream = new FileInputStream(filepath);
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(slideInputStream, "UTF-8");
        return parser;
    }

    void drawBatteryPictureWithCircle(Canvas canvas, List<Drawable> drawables, int centerX, int centerY, int batteryLevel, String colorArray, int radius, boolean isAdjustResolution) {
        int res1 = batteryLevel / 100;
        int res2 = (batteryLevel / 10) % 10;
        int res3 = batteryLevel % 10;
        if (batteryLevel < 10) {
            res2 = 10;
            res1 = 10;
        } else if (batteryLevel < 100) {
            res1 = 10;
        }
        int d_1_width = ((Drawable) drawables.get(res1)).getIntrinsicWidth();
        int d_2_width = ((Drawable) drawables.get(res2)).getIntrinsicWidth();
        int d_3_width = ((Drawable) drawables.get(res3)).getIntrinsicWidth();
        int degree_width = ((Drawable) drawables.get(11)).getIntrinsicWidth();
        int height = ((Drawable) drawables.get(res3)).getIntrinsicHeight();
        int left = centerX - ((((d_1_width + d_2_width) + d_3_width) + degree_width) / 2);
        int top = centerY - (height / 2);
        int bottom = centerY + (height / 2);
        int b_1_right = left + d_1_width;
        int b_2_right = b_1_right + d_2_width;
        int b_3_right = b_2_right + d_3_width;
        ((Drawable) drawables.get(res1)).setBounds(left, top, b_1_right, bottom);
        ((Drawable) drawables.get(res1)).draw(canvas);
        ((Drawable) drawables.get(res2)).setBounds(b_1_right, top, b_2_right, bottom);
        ((Drawable) drawables.get(res2)).draw(canvas);
        ((Drawable) drawables.get(res3)).setBounds(b_2_right, top, b_3_right, bottom);
        ((Drawable) drawables.get(res3)).draw(canvas);
        ((Drawable) drawables.get(11)).setBounds(b_3_right, top, b_3_right + degree_width, bottom);
        ((Drawable) drawables.get(11)).draw(canvas);
        if (colorArray.contains(",")) {
            int dark_color = -16777216 | Integer.valueOf(colorArray.split(",")[0], 16).intValue();
            int bright_color = -16777216 | Integer.valueOf(colorArray.split(",")[1], 16).intValue();
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStrokeWidth(15.0f);
            paint.setStyle(Style.STROKE);
            canvas.save();
            canvas.translate((float) centerX, (float) centerY);
            canvas.scale(0.25f, 0.25f);
            canvas.rotate(180.0f);
            for (int i = 0; i < 20; i++) {
                Canvas canvas2;
                Canvas canvas3;
                if (i < batteryLevel / 5) {
                    paint.setColor(bright_color);
                    canvas2 = canvas;
                    canvas3 = canvas;
                    canvas3.drawLine(7.0f, (float) radius, 7.0f, (float) (radius + 25), paint);
                    canvas.rotate(18.0f, 0.0f, 0.0f);
                } else {
                    paint.setColor(dark_color);
                    canvas2 = canvas;
                    canvas3 = canvas;
                    canvas3.drawLine(7.0f, (float) radius, 7.0f, (float) (radius + 25), paint);
                    canvas.rotate(18.0f, 0.0f, 0.0f);
                }
            }
            canvas.restore();
        }
    }

    void drawStepsPictureWithCircle(Context context, Canvas canvas, Float radius, List<Drawable> drawables, int centerX, int centerY, int stepCount, String colorArray, boolean isAdjustResolution) {
        if (stepCount < 0) {
            stepCount = 0;
        }
        if (stepCount > 99999) {
            stepCount = 99999;
        }
        int res1 = stepCount / 10000;
        int res2 = (stepCount / 1000) % 10;
        int res3 = (stepCount / 100) % 10;
        int res4 = (stepCount / 10) % 10;
        int res5 = stepCount % 10;
        if (stepCount < 10) {
            res4 = 10;
            res3 = 10;
            res2 = 10;
            res1 = 10;
        } else if (stepCount < 100) {
            res3 = 10;
            res2 = 10;
            res1 = 10;
        } else if (stepCount < 1000) {
            res2 = 10;
            res1 = 10;
        } else if (stepCount < 10000) {
            res1 = 10;
        }
        int d_1_width = ((Drawable) drawables.get(res1)).getIntrinsicWidth();
        int d_2_width = ((Drawable) drawables.get(res2)).getIntrinsicWidth();
        int d_3_width = ((Drawable) drawables.get(res3)).getIntrinsicWidth();
        int d_4_width = ((Drawable) drawables.get(res4)).getIntrinsicWidth();
        int d_5_width = ((Drawable) drawables.get(res5)).getIntrinsicWidth();
        int height = ((Drawable) drawables.get(res5)).getIntrinsicHeight();
        int top = centerY - (height / 2);
        int bottom = centerY + (height / 2);
        int d_1_left = centerX - (((((d_1_width + d_2_width) + d_3_width) + d_4_width) + d_5_width) / 2);
        int d_1_right = d_1_left + d_1_width;
        int d_2_right = d_1_right + d_2_width;
        int d_3_right = d_2_right + d_3_width;
        int d_4_right = d_3_right + d_4_width;
        int d_5_right = d_4_right + d_5_width;
        ((Drawable) drawables.get(res1)).setBounds(d_1_left, top, d_1_right, bottom);
        ((Drawable) drawables.get(res1)).draw(canvas);
        ((Drawable) drawables.get(res2)).setBounds(d_1_right, top, d_2_right, bottom);
        ((Drawable) drawables.get(res2)).draw(canvas);
        ((Drawable) drawables.get(res3)).setBounds(d_2_right, top, d_3_right, bottom);
        ((Drawable) drawables.get(res3)).draw(canvas);
        ((Drawable) drawables.get(res4)).setBounds(d_3_right, top, d_4_right, bottom);
        ((Drawable) drawables.get(res4)).draw(canvas);
        ((Drawable) drawables.get(res5)).setBounds(d_4_right, top, d_5_right, bottom);
        ((Drawable) drawables.get(res5)).draw(canvas);
        if (colorArray.contains(",")) {
            int dark_color = -16777216 | Integer.valueOf(colorArray.split(",")[0], 16).intValue();
            int bright_color = -16777216 | Integer.valueOf(colorArray.split(",")[1], 16).intValue();
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStrokeWidth(10.0f);
            paint.setStyle(Style.STROKE);
            paint.setAlpha(255);
            canvas.save();
            RectF oval = new RectF();
            oval.set(((float) centerX) - radius.floatValue(), ((float) centerY) - radius.floatValue(), radius.floatValue() + ((float) centerX), radius.floatValue() + ((float) centerY));
            int suggestSteps = WatchApp.getTargetSteps(context);
            paint.setColor(dark_color);
            canvas.drawCircle((float) centerX, (float) centerY, radius.floatValue(), paint);
            paint.setColor(bright_color);
            if (stepCount > suggestSteps) {
                canvas.drawCircle((float) centerX, (float) centerY, radius.floatValue(), paint);
            } else {
                canvas.drawArc(oval, 270.0f, (((float) stepCount) / ((float) suggestSteps)) * 360.0f, false, paint);
            }
            canvas.restore();
        }
    }

    void drawStepsPictureWithCircle2(Context context, Canvas canvas, List<Drawable> drawables, int centerX, int centerY, int stepCount, String width, String radius, String colorArray, boolean isAdjustResolution) {
        int widthInt = Integer.parseInt(width);
        int radiusInt = Integer.parseInt(radius);
        if (stepCount < 0) {
            stepCount = 0;
        }
        if (stepCount > 99999) {
            stepCount = 99999;
        }
        int res1 = stepCount / 10000;
        int res2 = (stepCount / 1000) % 10;
        int res3 = (stepCount / 100) % 10;
        int res4 = (stepCount / 10) % 10;
        int res5 = stepCount % 10;
        if (stepCount < 10) {
            res4 = 10;
            res3 = 10;
            res2 = 10;
            res1 = 10;
        } else if (stepCount < 100) {
            res3 = 10;
            res2 = 10;
            res1 = 10;
        } else if (stepCount < 1000) {
            res2 = 10;
            res1 = 10;
        } else if (stepCount < 10000) {
            res1 = 10;
        }
        int d_1_width = ((Drawable) drawables.get(res1)).getIntrinsicWidth();
        int d_2_width = ((Drawable) drawables.get(res2)).getIntrinsicWidth();
        int d_3_width = ((Drawable) drawables.get(res3)).getIntrinsicWidth();
        int d_4_width = ((Drawable) drawables.get(res4)).getIntrinsicWidth();
        int d_5_width = ((Drawable) drawables.get(res5)).getIntrinsicWidth();
        int height = ((Drawable) drawables.get(res5)).getIntrinsicHeight();
        int top = centerY - (height / 2);
        int bottom = centerY + (height / 2);
        int d_1_left = centerX - (((((d_1_width + d_2_width) + d_3_width) + d_4_width) + d_5_width) / 2);
        int d_1_right = d_1_left + d_1_width;
        int d_2_right = d_1_right + d_2_width;
        int d_3_right = d_2_right + d_3_width;
        int d_4_right = d_3_right + d_4_width;
        int d_5_right = d_4_right + d_5_width;
        ((Drawable) drawables.get(res1)).setBounds(d_1_left, top, d_1_right, bottom);
        ((Drawable) drawables.get(res1)).draw(canvas);
        ((Drawable) drawables.get(res2)).setBounds(d_1_right, top, d_2_right, bottom);
        ((Drawable) drawables.get(res2)).draw(canvas);
        ((Drawable) drawables.get(res3)).setBounds(d_2_right, top, d_3_right, bottom);
        ((Drawable) drawables.get(res3)).draw(canvas);
        ((Drawable) drawables.get(res4)).setBounds(d_3_right, top, d_4_right, bottom);
        ((Drawable) drawables.get(res4)).draw(canvas);
        ((Drawable) drawables.get(res5)).setBounds(d_4_right, top, d_5_right, bottom);
        ((Drawable) drawables.get(res5)).draw(canvas);
        if (colorArray.contains(",")) {
            int dark_color = -16777216 | Integer.valueOf(colorArray.split(",")[0], 16).intValue();
            int bright_color = -16777216 | Integer.valueOf(colorArray.split(",")[1], 16).intValue();
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStrokeWidth((float) widthInt);
            paint.setStyle(Style.STROKE);
            paint.setAlpha(255);
            canvas.save();
            RectF oval = new RectF();
            oval.set((float) (centerX - radiusInt), (float) (centerY - radiusInt), (float) (radiusInt + centerX), (float) (radiusInt + centerY));
            int suggestSteps = WatchApp.getTargetSteps(context);
            paint.setColor(dark_color);
            canvas.drawCircle((float) centerX, (float) centerY, (float) radiusInt, paint);
            paint.setColor(bright_color);
            if (stepCount > suggestSteps) {
                canvas.drawCircle((float) centerX, (float) centerY, (float) radiusInt, paint);
            } else {
                canvas.drawArc(oval, 270.0f, (((float) stepCount) / ((float) suggestSteps)) * 360.0f, false, paint);
            }
            canvas.restore();
        }
    }

    void drawBatteryCircle(Context context, Canvas canvas, int centerX, int centerY, int level, String width, String radius, String colorArray, boolean isAdjustResolution) {
        if (level < 0) {
            level = 0;
        }
        int widthInt = Integer.parseInt(width);
        int radiusInt = Integer.parseInt(radius);
        if (colorArray.contains(",")) {
            int dark_color = -16777216 | Integer.valueOf(colorArray.split(",")[0], 16).intValue();
            int bright_color = -16777216 | Integer.valueOf(colorArray.split(",")[1], 16).intValue();
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStrokeWidth((float) widthInt);
            paint.setStyle(Style.STROKE);
            paint.setAlpha(255);
            canvas.save();
            RectF oval = new RectF();
            oval.set((float) (centerX - radiusInt), (float) (centerY - radiusInt), (float) (radiusInt + centerX), (float) (radiusInt + centerY));
            paint.setColor(dark_color);
            canvas.drawCircle((float) centerX, (float) centerY, (float) radiusInt, paint);
            paint.setColor(bright_color);
            if (level > 100) {
                canvas.drawCircle((float) centerX, (float) centerY, (float) radiusInt, paint);
            } else {
                canvas.drawArc(oval, -90.0f, (((float) level) / 100.0f) * 360.0f, false, paint);
            }
            canvas.restore();
        }
    }

    void drawPedometer(Context context, Canvas canvas, int startAngle, int direction, int textSize, int stepCount) {
        canvas.save();
        canvas.translate((float) (this.centerX1 - (this.maxWidth / 2)), (float) (this.centerY1 - (this.maxWidth / 2)));
        Path path = new Path();
        RectF rect = new RectF(15.0f, 15.0f, ((float) this.maxWidth) - 15.0f, ((float) this.maxWidth) - 15.0f);
        Paint paint = new Paint();
        paint.setTextSize((float) textSize);
        paint.setAntiAlias(true);
        paint.setColor(-3355444);
        if (direction == 2) {
            path.addArc(rect, (float) startAngle, 33.0f);
        } else {
            path.addArc(rect, (float) startAngle, 33.0f);
        }
        canvas.drawTextOnPath("Pedometer", path, 0.0f, 0.0f, paint);
        paint.setColor(-13972358);
        path.reset();
        if (direction == 2) {
            path.addArc(rect, (float) (startAngle - 35), 50.0f);
        } else {
            path.addArc(rect, (float) (startAngle + 35), 50.0f);
        }
        if (stepCount < 0) {
            stepCount = 0;
        }
        canvas.drawTextOnPath(stepCount + " step", path, 0.0f, 0.0f, paint);
        canvas.restore();
    }

    void drawHeartRate(Context context, Canvas canvas, int startAngle, int direction, int textSize, int lastHeartRate) {
        canvas.translate((float) (this.centerX1 - (this.maxWidth / 2)), (float) (this.centerY1 - (this.maxWidth / 2)));
        Path path = new Path();
        RectF rect = new RectF(15.0f, 15.0f, ((float) this.maxWidth) - 15.0f, ((float) this.maxWidth) - 15.0f);
        if (direction == 2) {
            path.addArc(rect, (float) startAngle, 30.0f);
        } else {
            path.addArc(rect, (float) startAngle, 30.0f);
        }
        Paint paint = new Paint();
        paint.setTextSize((float) textSize);
        paint.setAntiAlias(true);
        paint.setColor(-3355444);
        canvas.drawTextOnPath("Heart rate", path, 0.0f, 0.0f, paint);
        paint.setColor(-2414295);
        path.reset();
        if (direction == 2) {
            path.addArc(rect, (float) (startAngle - 33), 25.0f);
        } else {
            path.addArc(rect, (float) (startAngle + 33), 25.0f);
        }
        canvas.drawTextOnPath(lastHeartRate + "bpm", path, 0.0f, 0.0f, paint);
    }

    void drawChargingInfo(Context context, Canvas canvas, Drawable mDrawBattery, int centerX, int centerY, int color, boolean isAdjustResolution) {
        int batteryLevel = (int) WatchApp.getBatteryLevel(context);
        int width = mDrawBattery.getIntrinsicWidth();
        int height = mDrawBattery.getIntrinsicHeight();
        mDrawBattery.setBounds(centerX - (width / 2), centerY - (height / 2), (width / 2) + centerX, (height / 2) + centerY);
        mDrawBattery.draw(canvas);
        String batteryVol = batteryLevel + "%";
        Paint paint = new Paint();
        paint.setTextSize(20.0f);
        paint.setAntiAlias(true);
        paint.setColor(color);
        canvas.drawText(batteryVol, (float) (mDrawBattery.getBounds().right + 5), (float) (mDrawBattery.getBounds().bottom - 8), paint);
    }

    void drawSpecialSecond(Canvas canvas, String colorsArray, int t_minute, int t_second) {
        int RADIUS = this.maxWidth / 2;
        canvas.save();
        canvas.translate((float) this.centerX1, (float) this.centerY1);
        if (colorsArray.contains(",")) {
            int bright_color = -16777216 | Integer.valueOf(colorsArray.split(",")[0], 16).intValue();
            int dark_color = -16777216 | Integer.valueOf(colorsArray.split(",")[1], 16).intValue();
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStrokeWidth(10.0f);
            paint.setStyle(Style.STROKE);
            paint.setAlpha(255);
            float y = (float) ((-RADIUS) + 5);
            for (int i = 0; i < 60; i++) {
                if (t_minute % 2 == 0) {
                    if (i < t_second) {
                        paint.setColor(bright_color);
                        canvas.drawLine(5.0f, y, 5.0f, y + 15.0f, paint);
                        canvas.rotate(6.0f, 0.0f, 0.0f);
                    } else {
                        paint.setColor(dark_color);
                        canvas.drawLine(5.0f, y, 5.0f, y + 15.0f, paint);
                        canvas.rotate(6.0f, 0.0f, 0.0f);
                    }
                } else if (i >= t_second) {
                    paint.setColor(bright_color);
                    canvas.drawLine(5.0f, y, 5.0f, y + 15.0f, paint);
                    canvas.rotate(6.0f, 0.0f, 0.0f);
                } else {
                    paint.setColor(dark_color);
                    canvas.drawLine(5.0f, y, 5.0f, y + 15.0f, paint);
                    canvas.rotate(6.0f, 0.0f, 0.0f);
                }
            }
        }
        canvas.restore();
    }

    void drawClockQuietPircture(Canvas canvas, Drawable mDrawCircle, int centerX, int centerY, boolean isAdjustResolution) {
        int width = mDrawCircle.getIntrinsicWidth();
        int heigh = mDrawCircle.getIntrinsicHeight();
        mDrawCircle.setBounds(centerX - (width / 2), centerY - (heigh / 2), (width / 2) + centerX, (heigh / 2) + centerY);
        mDrawCircle.draw(canvas);
    }

    public void drawSecondHandShadow(Canvas canvas, String startAngle, int mulrotate, int direction, Drawable secondHand, boolean changed, int iCenterX, int iCenterY) {
        Float startAngleF = Float.valueOf(startAngle);
        long millis = System.currentTimeMillis() % 60000;
        if (secondHand != null) {
            float shadowAngle;
            canvas.save();
            if (direction == 2) {
                millis = -millis;
            }
            float angle = startAngleF.floatValue() + (((((float) millis) * 6.0f) * ((float) mulrotate)) / 1000.0f);
            if (angle >= 0.0f && angle < 90.0f) {
                shadowAngle = 3.5f * (angle / 90.0f);
            } else if (angle < 90.0f || angle >= 270.0f) {
                shadowAngle = 3.5f * ((angle - 360.0f) / 90.0f);
            } else {
                shadowAngle = 3.5f * ((180.0f - angle) / 90.0f);
            }
            canvas.rotate(angle + shadowAngle, (float) (this.centerX1 + iCenterX), (float) (this.centerY1 + iCenterY));
            if (changed) {
                int w = secondHand.getIntrinsicWidth();
                int h = secondHand.getIntrinsicHeight();
                secondHand.setBounds((this.centerX1 + iCenterX) - (w / 2), (this.centerY1 + iCenterY) - (h / 2), (this.centerX1 + iCenterX) + (w / 2), (this.centerY1 + iCenterY) + (h / 2));
            }
            secondHand.draw(canvas);
            canvas.restore();
        }
    }

    public void drawHourhandShadow(Canvas canvas, String angle, float hour, Drawable hourHand, boolean changed, int iCenterX, int iCenterY) {
        Float startAngle = Float.valueOf(angle);
        if (hourHand != null) {
            float shadowAngle;
            canvas.save();
            float angleF = startAngle.floatValue() + (((hour % 12.0f) / 12.0f) * 360.0f);
            if (angleF >= 0.0f && angleF < 90.0f) {
                shadowAngle = 3.5f * (angleF / 90.0f);
            } else if (angleF < 90.0f || angleF >= 270.0f) {
                shadowAngle = 3.5f * ((angleF - 360.0f) / 90.0f);
            } else {
                shadowAngle = 3.5f * ((180.0f - angleF) / 90.0f);
            }
            canvas.rotate(angleF + shadowAngle, (float) (this.centerX1 + iCenterX), (float) (this.centerY1 + iCenterY));
            if (changed) {
                int w = hourHand.getIntrinsicWidth();
                int h = hourHand.getIntrinsicHeight();
                hourHand.setBounds((this.centerX1 + iCenterX) - (w / 2), (this.centerY1 + iCenterY) - (h / 2), (this.centerX1 + iCenterX) + (w / 2), (this.centerY1 + iCenterY) + (h / 2));
            }
            hourHand.draw(canvas);
            canvas.restore();
        }
    }

    public void drawMinuteHandShadow(Canvas canvas, String angle, float minutes, Drawable minuteHand, boolean changed, int iCenterX, int iCenterY) {
        Float startAngle = Float.valueOf(angle);
        if (minuteHand != null) {
            float shadowAngle;
            canvas.save();
            float angleF = startAngle.floatValue() + ((minutes / 60.0f) * 360.0f);
            if (angleF >= 0.0f && angleF < 90.0f) {
                shadowAngle = 3.5f * (angleF / 90.0f);
            } else if (angleF < 90.0f || angleF >= 270.0f) {
                shadowAngle = 3.5f * ((angleF - 360.0f) / 90.0f);
            } else {
                shadowAngle = 3.5f * ((180.0f - angleF) / 90.0f);
            }
            canvas.rotate(angleF + shadowAngle, (float) (this.centerX1 + iCenterX), (float) (this.centerY1 + iCenterY));
            if (changed) {
                int w = minuteHand.getIntrinsicWidth();
                int h = minuteHand.getIntrinsicHeight();
                minuteHand.setBounds((this.centerX1 + iCenterX) - (w / 2), (this.centerY1 + iCenterY) - (h / 2), (this.centerX1 + iCenterX) + (w / 2), (this.centerY1 + iCenterY) + (h / 2));
            }
            minuteHand.draw(canvas);
            canvas.restore();
        }
    }

    private int getMoonPhaseType() {
        boolean isDayChange = false;
        if (this.moonPhaseCalendar == null) {
            this.moonPhaseCalendar = Calendar.getInstance(Locale.getDefault());
            this.moonPhaseCalendar.setTimeInMillis(System.currentTimeMillis());
            this.moonPhaseCalendar.setTimeZone(TimeZone.getDefault());
            isDayChange = true;
        }
        Calendar when = Calendar.getInstance(Locale.getDefault());
        when.setTimeInMillis(System.currentTimeMillis());
        when.setTimeZone(TimeZone.getDefault());
        if (!(when.get(Calendar.MONTH) == this.moonPhaseCalendar.get(Calendar.MONTH) && when.get(Calendar.DAY_OF_MONTH) == this.moonPhaseCalendar.get(Calendar.DAY_OF_MONTH))) {
            isDayChange = true;
            this.moonPhaseCalendar = when;
        }
        if (this.moonPhaseType == -1 || isDayChange) {
            this.moonPhaseType = new MoonPhase(this.mContext).searchMoonPhase();
        }
        return this.moonPhaseType;
    }
}
