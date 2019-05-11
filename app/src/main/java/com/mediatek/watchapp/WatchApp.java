package com.mediatek.watchapp;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.CallLog.Calls;
import android.provider.Settings.System;
import android.util.Log;
import com.mediatek.watchapp.online.ClockSkinDBHelper;
import java.io.File;
import java.util.ArrayList;

public class WatchApp extends Application {
    private static int DayRainProbability = 0;
    private static int WeatherIcon = 0;
    private static int WeatherTemp = 0;
    private static boolean can_slide_in_viewpager = false;
    public static boolean isFristLaunch = true;
    private static boolean mBatteryCharging = false;
    private static float mBatteryLever = 0.0f;
    public static boolean mBatterySaver = false;
    public static ArrayList<String> mClocksSkinPath = new ArrayList();
    private static SQLiteDatabase mDb;
    private static ClockSkinDBHelper mDbHelper;
    private static int mHeartRate = 0;
    private static boolean mIsClockView = false;
    private static boolean mIsMainMenu = false;
    private static boolean mIsTopActivity = false;
    private static int mSteps = 0;
    private static int mStepsTarget = 8000;
    private static int mUnreadPhone = 0;
    private static int mUnreadSMS = 0;
    private static int mclockIndex = 0;
    public static ArrayList<installedClock> minstalledClocks = new ArrayList();
    private static WatchApp sWatchApp;

    public static class installedClock {
        String filePath;
        Drawable img_preview;
        String pkg;
        String previewPath;
        String title_name;
    }

    public void onCreate() {
        sWatchApp = this;
        super.onCreate();
        Log.d("WatchApp", "onCreate, WatchApp init ");
    }

    public static void setIsCanSlide(boolean b) {
        can_slide_in_viewpager = b;
    }

    public static boolean getIsCanSlide() {
        return can_slide_in_viewpager;
    }

    public static void setMainMenuStatus(boolean b) {
        mIsMainMenu = b;
    }

    public static boolean getMainMenuStatus() {
        return mIsMainMenu;
    }

    public static void setClockViewStatus(boolean status) {
        mIsClockView = status;
    }

    public static void setTopActivityStatus(boolean status) {
        mIsTopActivity = status;
    }

    public static boolean getTopActivityStatus() {
        return mIsTopActivity;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void updateInstalledClocks(android.content.Context r32) {
        /*
        r28 = minstalledClocks;
        r28 = r28.size();
        if (r28 <= 0) goto L_0x0037;
    L_0x0008:
        r28 = minstalledClocks;
        r9 = r28.iterator();
    L_0x000e:
        r28 = r9.hasNext();
        if (r28 == 0) goto L_0x0037;
    L_0x0014:
        r8 = r9.next();
        r8 = (com.mediatek.watchapp.WatchApp.installedClock) r8;
        r13 = r8.img_preview;
        if (r13 == 0) goto L_0x000e;
    L_0x001e:
        r0 = r13 instanceof android.graphics.drawable.BitmapDrawable;
        r28 = r0;
        if (r28 == 0) goto L_0x000e;
    L_0x0024:
        r7 = r13;
        r7 = (android.graphics.drawable.BitmapDrawable) r7;
        r6 = r7.getBitmap();
        if (r6 == 0) goto L_0x000e;
    L_0x002d:
        r28 = r6.isRecycled();
        if (r28 != 0) goto L_0x000e;
    L_0x0033:
        r6.recycle();
        goto L_0x000e;
    L_0x0037:
        r28 = minstalledClocks;
        r28.clear();
        r22 = new android.content.Intent;
        r28 = "ss.watch.clock";
        r0 = r22;
        r1 = r28;
        r0.<init>(r1);
        r28 = "android.intent.category.DEFAULT";
        r0 = r22;
        r1 = r28;
        r0.addCategory(r1);
        r24 = r32.getPackageManager();
        r28 = 0;
        r0 = r24;
        r1 = r22;
        r2 = r28;
        r10 = r0.queryIntentActivities(r1, r2);
        r28 = "WatchApp";
        r29 = new java.lang.StringBuilder;
        r29.<init>();
        r30 = "themes.size() = ";
        r29 = r29.append(r30);
        r30 = r10.size();
        r29 = r29.append(r30);
        r29 = r29.toString();
        android.util.Log.d(r28, r29);
        r28 = mClocksSkinPath;
        r28.clear();
        r28 = new java.lang.StringBuilder;
        r28.<init>();
        r29 = java.io.File.separator;
        r28 = r28.append(r29);
        r29 = "system";
        r28 = r28.append(r29);
        r29 = java.io.File.separator;
        r28 = r28.append(r29);
        r29 = "media";
        r28 = r28.append(r29);
        r21 = r28.toString();
        r28 = new java.lang.StringBuilder;
        r28.<init>();
        r0 = r28;
        r1 = r21;
        r28 = r0.append(r1);
        r29 = java.io.File.separator;
        r28 = r28.append(r29);
        r29 = "InsideClockSkin";
        r28 = r28.append(r29);
        r29 = java.io.File.separator;
        r28 = r28.append(r29);
        r21 = r28.toString();
        r16 = new java.io.File;
        r0 = r16;
        r1 = r21;
        r0.<init>(r1);
        r28 = r16.exists();
        if (r28 == 0) goto L_0x00de;
    L_0x00db:
        search(r16);
    L_0x00de:
        r28 = android.os.Environment.getExternalStorageDirectory();
        r25 = r28.getAbsolutePath();
        r28 = new java.lang.StringBuilder;
        r28.<init>();
        r0 = r28;
        r1 = r25;
        r28 = r0.append(r1);
        r29 = java.io.File.separator;
        r28 = r28.append(r29);
        r29 = "clockskin";
        r28 = r28.append(r29);
        r29 = java.io.File.separator;
        r28 = r28.append(r29);
        r18 = r28.toString();
        r17 = new java.io.File;
        r17.<init>(r18);
        r28 = r17.exists();
        if (r28 != 0) goto L_0x0118;
    L_0x0115:
        r17.mkdir();
    L_0x0118:
        r28 = r17.exists();
        if (r28 == 0) goto L_0x0121;
    L_0x011e:
        search(r17);
    L_0x0121:
        r19 = 0;
    L_0x0123:
        r28 = r10.size();
        r0 = r19;
        r1 = r28;
        if (r0 >= r1) goto L_0x01cd;
    L_0x012d:
        r0 = r19;
        r28 = r10.get(r0);
        r28 = (android.content.pm.ResolveInfo) r28;
        r0 = r28;
        r0 = r0.activityInfo;
        r28 = r0;
        r0 = r28;
        r0 = r0.packageName;
        r28 = r0;
        r5 = r28.toString();
        r0 = r19;
        r28 = r10.get(r0);
        r28 = (android.content.pm.ResolveInfo) r28;
        r0 = r28;
        r1 = r24;
        r28 = r0.loadLabel(r1);
        r27 = r28.toString();
        r28 = "WatchApp";
        r29 = new java.lang.StringBuilder;
        r29.<init>();
        r30 = "appPackageName = ";
        r29 = r29.append(r30);
        r0 = r29;
        r29 = r0.append(r5);
        r29 = r29.toString();
        android.util.Log.d(r28, r29);
        r28 = "WatchApp";
        r29 = new java.lang.StringBuilder;
        r29.<init>();
        r30 = "title = ";
        r29 = r29.append(r30);
        r0 = r29;
        r1 = r27;
        r29 = r0.append(r1);
        r29 = r29.toString();
        android.util.Log.d(r28, r29);
        r26 = 0;
        r28 = r5.toString();	 Catch:{ NameNotFoundException -> 0x0349 }
        r0 = r24;
        r1 = r28;
        r26 = r0.getResourcesForApplication(r1);	 Catch:{ NameNotFoundException -> 0x0349 }
    L_0x01a1:
        r28 = "img_clock_preview";
        r29 = "drawable";
        r30 = r5.toString();
        r0 = r26;
        r1 = r28;
        r2 = r29;
        r3 = r30;
        r20 = r0.getIdentifier(r1, r2, r3);
        r12 = new com.mediatek.watchapp.WatchApp$installedClock;
        r12.<init>();
        r0 = r27;
        r12.title_name = r0;
        r12.pkg = r5;
        r28 = minstalledClocks;
        r0 = r28;
        r0.add(r12);
        r19 = r19 + 1;
        goto L_0x0123;
    L_0x01cd:
        r19 = 0;
    L_0x01cf:
        r28 = mClocksSkinPath;
        r28 = r28.size();
        r0 = r19;
        r1 = r28;
        if (r0 >= r1) goto L_0x0237;
    L_0x01db:
        r12 = new com.mediatek.watchapp.WatchApp$installedClock;
        r12.<init>();
        r28 = "sdclock";
        r0 = r28;
        r12.title_name = r0;
        r28 = "sdclock";
        r0 = r28;
        r12.pkg = r0;
        r28 = mClocksSkinPath;
        r0 = r28;
        r1 = r19;
        r28 = r0.get(r1);
        r28 = (java.lang.String) r28;
        r29 = mClocksSkinPath;
        r0 = r29;
        r1 = r19;
        r29 = r0.get(r1);
        r29 = (java.lang.String) r29;
        r30 = "/";
        r29 = r29.lastIndexOf(r30);
        r30 = 0;
        r0 = r28;
        r1 = r30;
        r2 = r29;
        r28 = r0.substring(r1, r2);
        r0 = r28;
        r12.filePath = r0;
        r28 = mClocksSkinPath;
        r0 = r28;
        r1 = r19;
        r28 = r0.get(r1);
        r28 = (java.lang.String) r28;
        r0 = r28;
        r12.previewPath = r0;
        r28 = minstalledClocks;
        r0 = r28;
        r0.add(r12);
        r19 = r19 + 1;
        goto L_0x01cf;
    L_0x0237:
        r28 = new com.mediatek.watchapp.online.ClockSkinDBHelper;
        r29 = "clockskin.db";
        r30 = 0;
        r31 = 1;
        r0 = r28;
        r1 = r32;
        r2 = r29;
        r3 = r30;
        r4 = r31;
        r0.<init>(r1, r2, r3, r4);
        mDbHelper = r28;
        r11 = 0;
        r28 = mDbHelper;	 Catch:{ SQLiteException -> 0x0316 }
        r28 = r28.getReadableDatabase();	 Catch:{ SQLiteException -> 0x0316 }
        mDb = r28;	 Catch:{ SQLiteException -> 0x0316 }
        r28 = mDb;	 Catch:{ SQLiteException -> 0x0316 }
        r29 = "select * from clockskin_install_list";
        r30 = 0;
        r11 = r28.rawQuery(r29, r30);	 Catch:{ SQLiteException -> 0x0316 }
        r28 = "WatchApp";
        r29 = new java.lang.StringBuilder;	 Catch:{ SQLiteException -> 0x0316 }
        r29.<init>();	 Catch:{ SQLiteException -> 0x0316 }
        r30 = "cur ";
        r29 = r29.append(r30);	 Catch:{ SQLiteException -> 0x0316 }
        r0 = r29;
        r29 = r0.append(r11);	 Catch:{ SQLiteException -> 0x0316 }
        r29 = r29.toString();	 Catch:{ SQLiteException -> 0x0316 }
        android.util.Log.d(r28, r29);	 Catch:{ SQLiteException -> 0x0316 }
        if (r11 == 0) goto L_0x033c;
    L_0x0281:
        r11.moveToFirst();	 Catch:{ SQLiteException -> 0x0316 }
    L_0x0284:
        r28 = r11.isAfterLast();	 Catch:{ SQLiteException -> 0x0316 }
        if (r28 != 0) goto L_0x0339;
    L_0x028a:
        r23 = new com.mediatek.watchapp.online.OnlineClockSkinLocalNode;	 Catch:{ SQLiteException -> 0x0316 }
        r0 = r23;
        r1 = r32;
        r0.<init>(r1, r11);	 Catch:{ SQLiteException -> 0x0316 }
        r28 = "WatchApp";
        r29 = new java.lang.StringBuilder;	 Catch:{ SQLiteException -> 0x0316 }
        r29.<init>();	 Catch:{ SQLiteException -> 0x0316 }
        r30 = "node ";
        r29 = r29.append(r30);	 Catch:{ SQLiteException -> 0x0316 }
        r30 = r23.getName();	 Catch:{ SQLiteException -> 0x0316 }
        r29 = r29.append(r30);	 Catch:{ SQLiteException -> 0x0316 }
        r29 = r29.toString();	 Catch:{ SQLiteException -> 0x0316 }
        android.util.Log.d(r28, r29);	 Catch:{ SQLiteException -> 0x0316 }
        r12 = new com.mediatek.watchapp.WatchApp$installedClock;	 Catch:{ SQLiteException -> 0x0316 }
        r12.<init>();	 Catch:{ SQLiteException -> 0x0316 }
        r28 = "downloadclock";
        r0 = r28;
        r12.title_name = r0;	 Catch:{ SQLiteException -> 0x0316 }
        r28 = r23.getPackageName();	 Catch:{ SQLiteException -> 0x0316 }
        r0 = r28;
        r12.pkg = r0;	 Catch:{ SQLiteException -> 0x0316 }
        r28 = new java.lang.StringBuilder;	 Catch:{ SQLiteException -> 0x0316 }
        r28.<init>();	 Catch:{ SQLiteException -> 0x0316 }
        r29 = "/data/data/com.mediatek.watchapp//WiiwearClockSkin/";
        r28 = r28.append(r29);	 Catch:{ SQLiteException -> 0x0316 }
        r29 = r23.getPackageName();	 Catch:{ SQLiteException -> 0x0316 }
        r28 = r28.append(r29);	 Catch:{ SQLiteException -> 0x0316 }
        r28 = r28.toString();	 Catch:{ SQLiteException -> 0x0316 }
        r0 = r28;
        r12.filePath = r0;	 Catch:{ SQLiteException -> 0x0316 }
        r28 = new java.lang.StringBuilder;	 Catch:{ SQLiteException -> 0x0316 }
        r28.<init>();	 Catch:{ SQLiteException -> 0x0316 }
        r29 = "/data/data/com.mediatek.watchapp//WiiwearClockSkin/preview/";
        r28 = r28.append(r29);	 Catch:{ SQLiteException -> 0x0316 }
        r29 = r23.getPackageName();	 Catch:{ SQLiteException -> 0x0316 }
        r28 = r28.append(r29);	 Catch:{ SQLiteException -> 0x0316 }
        r29 = ".png";
        r28 = r28.append(r29);	 Catch:{ SQLiteException -> 0x0316 }
        r28 = r28.toString();	 Catch:{ SQLiteException -> 0x0316 }
        r0 = r28;
        r12.previewPath = r0;	 Catch:{ SQLiteException -> 0x0316 }
        r28 = 0;
        r0 = r28;
        r12.img_preview = r0;	 Catch:{ SQLiteException -> 0x0316 }
        r28 = minstalledClocks;	 Catch:{ SQLiteException -> 0x0316 }
        r0 = r28;
        r0.add(r12);	 Catch:{ SQLiteException -> 0x0316 }
        r11.moveToNext();	 Catch:{ SQLiteException -> 0x0316 }
        goto L_0x0284;
    L_0x0316:
        r15 = move-exception;
        r28 = "xiaocai_SQ";
        r29 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0342 }
        r29.<init>();	 Catch:{ all -> 0x0342 }
        r30 = "SQLiteException = ";
        r29 = r29.append(r30);	 Catch:{ all -> 0x0342 }
        r0 = r29;
        r29 = r0.append(r15);	 Catch:{ all -> 0x0342 }
        r29 = r29.toString();	 Catch:{ all -> 0x0342 }
        android.util.Log.e(r28, r29);	 Catch:{ all -> 0x0342 }
        r28 = mDb;
        r28.close();
    L_0x0338:
        return;
    L_0x0339:
        r11.close();	 Catch:{ SQLiteException -> 0x0316 }
    L_0x033c:
        r28 = mDb;
        r28.close();
        goto L_0x0338;
    L_0x0342:
        r28 = move-exception;
        r29 = mDb;
        r29.close();
        throw r28;
    L_0x0349:
        r14 = move-exception;
        goto L_0x01a1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.watchapp.WatchApp.updateInstalledClocks(android.content.Context):void");
    }

    public static ArrayList<installedClock> getInstalledClocks() {
        return minstalledClocks;
    }

    public static int getAllClockCount() {
        return ClockUtil.mClockList.length + minstalledClocks.size();
    }

    public static int getBTClockfaceIndex(String clockfacePath) {
        int index = -1;
        if (minstalledClocks.size() > 0) {
            for (installedClock clock : minstalledClocks) {
                index++;
                if (clock.title_name.equals("sdclock") && clock.filePath.substring(clock.filePath.lastIndexOf("/") + 1).equals(clockfacePath)) {
                    return ClockUtil.mClockList.length + index;
                }
            }
        }
        return -1;
    }

    public static void setClockIndex(Context context, int index) {
        mclockIndex = index;
        Editor mEditor = context.getSharedPreferences("clockview_settings", 0).edit();
        mEditor.putInt("clockview_index", index);
        mEditor.commit();
    }

    public static int getClockIndex(Context context) {
        mclockIndex = context.getSharedPreferences("clockview_settings", 0).getInt("clockview_index", 0);
        return mclockIndex;
    }

    public static void setBatteryLevel(Context context, float level) {
        mBatteryLever = level;
    }

    public static float getBatteryLevel(Context context) {
        return mBatteryLever;
    }

    public static void setBatteryCharging(Context context, boolean charging) {
        mBatteryCharging = charging;
    }

    public static boolean getIsBatteryCharging(Context context) {
        return mBatteryCharging;
    }

    public static void setUnreadPhone(Context context, int unreadPhone) {
        mUnreadPhone = unreadPhone;
    }

    public static int getUnreadPhone(Context context) {
        return mUnreadPhone;
    }

    public static void setUnreadSMS(Context context, int unreadSms) {
        mUnreadSMS = unreadSms;
    }

    public static int getUnreadSMS(Context context) {
        return mUnreadSMS;
    }

    public static int getSteps(Context context) {
        mSteps = System.getInt(context.getContentResolver(), "today_steps", 0);
        return mSteps;
    }

    public static int getTargetSteps(Context context) {
        mStepsTarget = System.getInt(context.getContentResolver(), "step_target_count", 8000);
        return mStepsTarget;
    }

    public static int getRate(Context context) {
        mHeartRate = System.getInt(context.getContentResolver(), "heart_rate", 0);
        return mHeartRate;
    }

    public static String getDistance(Context context) {
        String s = System.getString(context.getContentResolver(), "today_distance");
        return s == null ? "0.0" : s;
    }

    public static float getTargetDistance(Context context) {
        return 200.0f;
    }

    public static String getCalories(Context context) {
        String s = System.getString(context.getContentResolver(), "today_calories");
        return s == null ? "0" : s;
    }

    public static int getTargetCalories(Context context) {
        return 300;
    }

    public static int getWeatherTemp(Context context) {
        WeatherTemp = System.getInt(context.getContentResolver(), "WeatherTemp", 23);
        return WeatherTemp;
    }

    public static int getWeatherIcon(Context context) {
        WeatherIcon = System.getInt(context.getContentResolver(), "WeatherIcon", 10);
        return WeatherIcon;
    }

    public static void search(File fileold) {
        try {
            File[] files = fileold.listFiles();
            if (files.length > 0) {
                int j = 0;
                while (j < files.length) {
                    if (files[j].isDirectory()) {
                        search(files[j]);
                    } else if (files[j].getName().indexOf("img_clock_preview.png") > -1 || files[j].getName().indexOf("clock_skin_model.png") > -1) {
                        mClocksSkinPath.add(files[j].getPath());
                    }
                    j++;
                }
            }
        } catch (Exception e) {
        }
    }

    public static void reloadUnreadPhoneAndSMS(Context context) {
        setUnreadPhone(context, getPhoneCounts(context));
        setUnreadSMS(context, getSmsCounts(context));
    }

    public static int getPhoneCounts(Context context) {
        Cursor cursor = context.getContentResolver().query(Calls.CONTENT_URI, new String[]{"type"}, " type=? and new=?", new String[]{"3", "1"}, "date desc");
        if (cursor == null) {
            return 0;
        }
        int result = cursor.getCount();
        cursor.close();
        return result;
    }

    public static int getSmsCounts(Context context) {
        Cursor csr = context.getContentResolver().query(Uri.parse("content://sms"), null, "type = 1 and read = 0", null, null);
        if (csr == null) {
            return 0;
        }
        int result = csr.getCount();
        csr.close();
        return result;
    }

    public static int clearAppCache(Context context) {
        if (isServiceRunning(context)) {
            return 2;
        }
        try {
            File file_cache = context.getCacheDir();
            if (file_cache.exists()) {
                File[] files = file_cache.listFiles();
                for (File delete : files) {
                    delete.delete();
                }
            }
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @SuppressLint("WrongConstant")
    private static boolean isServiceRunning(Context context) {
        for (RunningServiceInfo service : ((ActivityManager) context.getSystemService("activity")).getRunningServices(Integer.MAX_VALUE)) {
            if (!"com.wiiteck.clockpreviewer.online.ClockSkinDownloadService".equals(service.service.getClassName())) {
                if ("com.mediatek.watchapp.online.ClockSkinDownloadService".equals(service.service.getClassName())) {
                }
            }
            return true;
        }
        return false;
    }
}
