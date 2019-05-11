package com.mediatek.watchapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import com.mediatek.watchapp.CustomDigitalClockSave.Callback;
import com.mediatek.watchapp.online.ClockSkinDBHelper;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends FragmentActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private final int DELAYED_TIME = 6000;
    private final int EXIT_BATTERY_SAVER_MODE = 1002;
    private final int SCRREN_OFF_DELAYED = 1001;
    private final int TO_BATTERY_SAVER_MODE = 1003;
    private boolean firstEnter = true;
    private WatchAppWidgetHost mAppWidgetHost = null;
    private AppWidgetManager mAppWidgetManager = null;
    private final BroadcastReceiver mBatteryReceiver = new C01237();
    private CustomDigitalClockSave mClockSaveView;
    private float mConfigurationFontScale;
    ExecutorService mExecutorService;
    Handler mHander = new C01171();
    private boolean mHasFocus = false;
    private IdleFragment mIdleFragment;
    private MainVerticalViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private PowerManager mPowerManager;
    private final BroadcastReceiver mPowerSaveReceiver = new C01248();
    private final BroadcastReceiver mPowerSaver = new C01193();
    private Dialog mProgressDialog = null;
    private QuickSettingsFragment mQuickSettingsFragment = new QuickSettingsFragment();
    private final BroadcastReceiver mReceiver = new C01226();
    private final BroadcastReceiver mScreenUpdateReceiver = new C01215();
    private SportsFragment mSportsFragment = new SportsFragment();
    private TimeZoneLocalChangedReciever mTZLocalReciever = new TimeZoneLocalChangedReciever();
    private final BroadcastReceiver mUnreadLoader = new C01182();
    private final BroadcastReceiver mUpdateClockReceiver = new C01204();
    private Vibrator mVibrator;
    private View top_black;

    /* renamed from: com.mediatek.watchapp.MainActivity$1 */
    class C01171 extends Handler {
        C01171() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1001:
                    ScreenControl.getInstance(MainActivity.this).ScreenOff();
                    return;
                case 1002:
                    MainActivity.this.refreshActivity(false);
                    Log.i(MainActivity.TAG, "handleMessage   setPowerSaveMode(false)");
                    return;
                case 1003:
                    MainActivity.this.refreshActivity(true);
                    return;
                default:
                    return;
            }
        }
    }

    /* renamed from: com.mediatek.watchapp.MainActivity$2 */
    class C01182 extends BroadcastReceiver {
        C01182() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("com.mediatek.action.UNREAD_CHANGED".equals(intent.getAction())) {
                ComponentName componentName = (ComponentName) intent.getComponent(); // "com.mediatek.intent.extra.UNREAD_COMPONENT"
                int unreadNum = intent.getIntExtra("com.mediatek.intent.extra.UNREAD_NUMBER", -1);
                if (componentName != null && unreadNum != -1) {
                    Log.d("xiaocai_unread", "Receive unread broadcast: componentName = " + componentName + ", unreadNum = " + unreadNum);
                    ComponentName componentMms = new ComponentName("com.android.mms", "com.android.mms.ui.BootActivity");
                    ComponentName componentPhone = new ComponentName("com.android.dialer", "com.android.dialer.DialtactsActivity");
                    if (componentMms.equals(componentName)) {
                        WatchApp.setUnreadSMS(context, unreadNum);
                        if (MainActivity.this.mIdleFragment != null) {
                            MainActivity.this.mIdleFragment.refreshApplist();
                        }
                    } else if (componentPhone.equals(componentName)) {
                        WatchApp.setUnreadPhone(context, unreadNum);
                        if (MainActivity.this.mIdleFragment != null) {
                            MainActivity.this.mIdleFragment.refreshApplist();
                        }
                    }
                }
            }
        }
    }

    /* renamed from: com.mediatek.watchapp.MainActivity$3 */
    class C01193 extends BroadcastReceiver {
        C01193() {
        }

        public void onReceive(Context context, Intent intent) {
            if (MainActivity.this.mPowerManager.isPowerSaveMode()) {
                MainActivity.this.mHander.sendEmptyMessage(1003);
                return;
            }
            MainActivity.this.mHander.sendEmptyMessage(1002);
            MainActivity.this.mHander.removeMessages(1001);
        }
    }

    /* renamed from: com.mediatek.watchapp.MainActivity$4 */
    class C01204 extends BroadcastReceiver {
        C01204() {
        }

        public void onReceive(Context context, Intent intent) {
            Context c = context;
            String str = intent.getStringExtra("action_str");
            if (str.equals("installclock")) {
                ContentValues values = new ContentValues();
                values.put("clockName", intent.getStringExtra("THEME_NAME"));
                values.put("skinid", intent.getStringExtra("PACKAG_NAME"));
                values.put("filePath", intent.getStringExtra("APK_NAME"));
                values.put("clocktype", intent.getStringExtra("VERSION"));
                values.put("state", Integer.valueOf(1));
                new installUpdateTask(context, values).executeOnExecutor(MainActivity.this.mExecutorService, new Void[0]);
            } else if (str.equals("deleteclock")) {
                MainActivity.this.showOnlineLoadingDialog(context);
                new uninstallUpdateTask(context, intent.getStringExtra("PACKAG_NAME"), intent.getIntExtra("INDEX", 0)).executeOnExecutor(MainActivity.this.mExecutorService, new Void[0]);
            } else if (str.equals("cleanrelaodclock")) {
                MainActivity.this.showOnlineLoadingDialog(context);
                new cleanRelaodClocksinTask(context).executeOnExecutor(MainActivity.this.mExecutorService, new Void[0]);
            } else if (str.equals("installbtclock")) {
                new installBTClockfaceTask(context, intent.getStringExtra("path")).executeOnExecutor(MainActivity.this.mExecutorService, new Void[0]);
            }
        }
    }

    /* renamed from: com.mediatek.watchapp.MainActivity$5 */
    class C01215 extends BroadcastReceiver {
        C01215() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(MainActivity.TAG, "onReceive: action = " + action);
            if ("com.mediatek.watchapp.NOTIFICATION_LISTENER.POSTED".equals(action)) {
                if (MainActivity.this.mPager == null) {
                }
            } else if ("com.mediatek.watchapp.NOTIFICATION_LISTENER.REMOVED".equals(action)) {
                if (NotificationFragment.getInstance() != null && NotificationFragment.getInstance().getPageCount() == 0) {
                    Log.d(MainActivity.TAG, "Notification removed, pageCount=" + NotificationFragment.getInstance().getPageCount());
                    MainActivity.this.sendBroadcast(new Intent("com.mediatek.watchapp.NOTIFICATION_LISTENER.HIDE_INDICATOR"));
                }
            } else if ("com.mediatek.watchapp.NOTIFICATION_LISTENER.REFRESH".equals(action)) {
                if (MainActivity.this.mPager != null) {
                    MainActivity.this.mPager.invalidate();
                }
            } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                if (MainActivity.this.mPager != null) {
                    MainActivity.this.mPager.setCurrentItem(1, false);
                }
                if (MainActivity.this.mIdleFragment != null) {
                    MainActivity.this.mIdleFragment.backToClock();
                }
            } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                if (!MainActivity.this.mPowerManager.isPowerSaveMode()) {
                }
            } else if (action.equals("ipc_handphone_state_change")) {
                boolean isConnected = 3 == intent.getIntExtra("state", 0);
                MainActivity.this.mQuickSettingsFragment.mQuickSettingsSubFragment.setPhoneConnectedState(isConnected);
                String toast_info = context.getResources().getString(R.string.phone_disconnected_toast).toString();
                if (!isConnected) {
                }
            }
        }
    }

    /* renamed from: com.mediatek.watchapp.MainActivity$6 */
    class C01226 extends BroadcastReceiver {
        C01226() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(MainActivity.TAG, "guoxiaolong: action = " + action);
            if ("android.intent.action.PACKAGE_ADDED".equals(action) || "android.intent.action.PACKAGE_REMOVED".equals(action) || "android.intent.action.PACKAGE_REPLACED".equals(action) || "android.intent.action.PACKAGE_CHANGED".equals(action)) {
                WatchApp.updateInstalledClocks(context);
                if (MainActivity.this.mIdleFragment != null) {
                    MainActivity.this.mIdleFragment.refreshApplist();
                }
            }
        }
    }

    /* renamed from: com.mediatek.watchapp.MainActivity$7 */
    class C01237 extends BroadcastReceiver {
        C01237() {
        }

        public void onReceive(Context context, Intent intent) {
            boolean mCharged = true;
            String action = intent.getAction();
            Log.d(MainActivity.TAG, "onReceive: action = " + action);
            if (action.equals("android.intent.action.BATTERY_CHANGED")) {
                int status = intent.getIntExtra("status", 1);
                if (status != 5) {
                    mCharged = false;
                }
                boolean isCharging = mCharged || status == 2;
                int level = (int) ((((float) intent.getIntExtra("level", 0)) * 100.0f) / ((float) intent.getIntExtra("scale", 100)));
                int triggerLevel = Global.getInt(MainActivity.this.getContentResolver(), "low_power_trigger_level", 5);
                WatchApp.setBatteryLevel(context, (float) level);
                WatchApp.setBatteryCharging(MainActivity.this, isCharging);
                Log.i("battery_change", "  isCharging = " + isCharging);
                Log.i("battery_change", "  isPowerSaveMode = " + WatchApp.mBatterySaver + "  level = " + level + "  triggerLevel = " + triggerLevel);
            }
        }
    }

    /* renamed from: com.mediatek.watchapp.MainActivity$8 */
    class C01248 extends BroadcastReceiver {
        C01248() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(MainActivity.TAG, "mPowerSaveReceiver action = " + action);
            if (action.equals("com.android.SSaction.powerSaveMode_change")) {
                WatchApp.mBatterySaver = MainActivity.this.mPowerManager.isPowerSaveMode();
                MainActivity.this.refreshActivity(WatchApp.mBatterySaver);
            }
        }
    }

    /* renamed from: com.mediatek.watchapp.MainActivity$9 */
    class C01259 implements Callback {
        C01259() {
        }

        public void onClick() {
           // MainActivity.this.mPowerManager.setPowerSaveMode(false);
            MainActivity.this.mHander.sendEmptyMessage(1002);
            MainActivity.this.mHander.removeMessages(1001);
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public Fragment getItem(int position) {
            if (position == 1) {
                return MainActivity.this.mIdleFragment;
            }
            if (position == 0) {
                Log.d("MainActivity", "getItem.QUICKSETTINGS_PAGE_INDEX");
                return MainActivity.this.mQuickSettingsFragment;
            } else if (position == 2) {
                return MainActivity.this.mSportsFragment;
            } else {
                return null;
            }
        }

        public int getCount() {
            return 3;
        }
    }

    public class TimeZoneLocalChangedReciever extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.TIMEZONE_CHANGED")) {
                if (MainActivity.this.mIdleFragment != null) {
                    MainActivity.this.mIdleFragment.mClockFragment.RelaodClockFace();
                }
            } else if (intent.getAction().equals("android.intent.action.LOCALE_CHANGED")) {
                if (MainActivity.this.mIdleFragment != null) {
                    MainActivity.this.mIdleFragment.refreshApplist();
                }
            } else if (action.equals("watch_app_list_change")) {
                int style = System.getInt(MainActivity.this.getContentResolver(), "watch_app_list_style", 0);
                if (MainActivity.this.mIdleFragment.getAppListStyle() != style) {
                    MainActivity.this.mIdleFragment.setAppListStyle(style);
                    MainActivity.this.mIdleFragment.update();
                }
            } else if (action.equals("com.wiitetech.wiiwatch.write_settings")) {
                String key = intent.getStringExtra("key");
                if (key.equals("step_target_count")) {
                    String step = intent.getStringExtra(key);
                    System.putString(MainActivity.this.getContentResolver(), key, step);
                    Log.d("write_settings", "step: " + step);
                } else if (key.equals("persist.sys.raise.wakeup")) {
                    String raise = intent.getStringExtra(key);
                    System.putString(MainActivity.this.getContentResolver(), key, raise);
                    SystemProperties.set("persist.sys.raise.wakeup", String.valueOf(raise));
                    Log.d("write_settings", "raise: " + raise);
                } else if (key.equals("ambient_clock_switch")) {
                    int ambient = intent.getIntExtra(key, 0);
                    System.putInt(MainActivity.this.getContentResolver(), key, ambient);
                    Log.d("write_settings", "ambient: " + ambient);
                }
            }
        }
    }

    private class cleanRelaodClocksinTask extends AsyncTask<Void, Integer, Integer> {
        private Context mContext;

        public cleanRelaodClocksinTask(Context context) {
            this.mContext = context;
        }

        protected Integer doInBackground(Void... params) {
            int ret = WatchApp.clearAppCache(this.mContext);
            if (ret == 1) {
                WatchApp.updateInstalledClocks(this.mContext);
                WatchApp.setClockIndex(this.mContext, 0);
            }
            return Integer.valueOf(ret);
        }

        protected void onPostExecute(Integer result) {
            switch (result.intValue()) {
                case 1:
                    Toast.makeText(this.mContext, R.string.clearCacheSuccess, Toast.LENGTH_SHORT).show();
                    MainActivity.this.mIdleFragment.mClockFragment.ChangeClockApply(0);
                    Intent it = new Intent("com.update.installclock.done");
                    it.putExtra("action_str", "cleanrelaodclock");
                    MainActivity.this.sendBroadcast(it);
                    break;
                case 2:
                    Toast.makeText(this.mContext, R.string.onDownloading, Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(this.mContext, R.string.clearCacheFailed, Toast.LENGTH_SHORT).show();
                    break;
            }
            MainActivity.this.dismissOnlineLoadingDialog();
        }
    }

    private class installBTClockfaceTask extends AsyncTask<Void, Integer, Void> {
        private String mClockfacePath;
        private Context mContext;

        public installBTClockfaceTask(Context context, String values) {
            this.mContext = context;
            this.mClockfacePath = values;
        }

        protected Void doInBackground(Void... params) {
            WatchApp.updateInstalledClocks(this.mContext);
            return null;
        }

        protected void onPostExecute(Void result) {
            int index = WatchApp.getBTClockfaceIndex(this.mClockfacePath);
            if (index != -1) {
                WatchApp.setClockIndex(this.mContext, index);
                MainActivity.this.mIdleFragment.mClockFragment.ChangeClockApply(index);
                Intent it = new Intent("com.update.installclock.done");
                it.putExtra("action_str", "installclock");
                MainActivity.this.sendBroadcast(it);
            }
            MainActivity.this.dismissOnlineLoadingDialog();
        }
    }

    private class installUpdateTask extends AsyncTask<Void, Integer, Void> {
        private Context mContext;
        private ContentValues mValues;

        public installUpdateTask(Context context, ContentValues values) {
            this.mContext = context;
            this.mValues = values;
        }

        protected Void doInBackground(Void... params) {
            SQLiteDatabase mDb = new ClockSkinDBHelper(this.mContext, "clockskin.db", null, 1).getReadableDatabase();
            try {
                String clockskinId = this.mValues.getAsString("skinid");
                mDb.delete("clockskin_install_list", "skinid=?", new String[]{clockskinId});
                mDb.insert("clockskin_install_list", null, this.mValues);
                Cursor countCur = mDb.rawQuery("select * from clockskin_online_list WHERE skinid=?", new String[]{clockskinId});
                if (countCur.getCount() > 0) {
                    countCur.moveToFirst();
                    while (true) {
                        ContentValues v = new ContentValues();
                        v.put("state", Integer.valueOf(1));
                        int n = mDb.update("clockskin_online_list", v, "skinid=?", new String[]{clockskinId});
                        Intent it = new Intent("BROADCAST_DOWNLOAD_STATE_FILTER");
                        it.putExtra("BROADCAST_DOWNLOAD_STATE", 1003);
                        MainActivity.this.sendBroadcast(it);
                        if (!countCur.moveToNext()) {
                            break;
                        }
                    }
                }
                countCur.close();
                WatchApp.updateInstalledClocks(this.mContext);
                return null;
            } finally {
                mDb.close();
            }
        }

        protected void onPostExecute(Void result) {
            Toast.makeText(this.mContext, R.string.font_download_done, Toast.LENGTH_SHORT).show();
            int index = WatchApp.getAllClockCount() - 1;
            WatchApp.setClockIndex(this.mContext, index);
            MainActivity.this.mIdleFragment.mClockFragment.ChangeClockApply(index);
            Intent it = new Intent("com.update.installclock.done");
            it.putExtra("action_str", "installclock");
            MainActivity.this.sendBroadcast(it);
            MainActivity.this.dismissOnlineLoadingDialog();
        }
    }

    private class uninstallUpdateTask extends AsyncTask<Void, Integer, Void> {
        private String mClockSkinId;
        private Context mContext;
        private int mSelectIndex;

        public uninstallUpdateTask(Context context, String skinId, int index) {
            this.mContext = context;
            this.mClockSkinId = skinId;
            this.mSelectIndex = index;
        }

        protected Void doInBackground(Void... params) {
            File clockSkinDir = new File("/data/data/com.mediatek.watchapp//WiiwearClockSkin/" + this.mClockSkinId);
            MainActivity.this.delClockSkinFromDB(this.mClockSkinId, this.mContext);
            if (clockSkinDir.isDirectory() && clockSkinDir.exists()) {
                MainActivity.this.delFolder(clockSkinDir.toString());
            }
            WatchApp.updateInstalledClocks(this.mContext);
            int index = WatchApp.getClockIndex(this.mContext);
            if (this.mSelectIndex < index) {
                index--;
            }
            WatchApp.setClockIndex(this.mContext, index);
            return null;
        }

        protected void onPostExecute(Void result) {
            Intent it = new Intent("com.update.installclock.done");
            it.putExtra("action_str", "deleteclock");
            MainActivity.this.sendBroadcast(it);
            MainActivity.this.dismissOnlineLoadingDialog();
        }
    }

    @SuppressLint("WrongConstant")
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        Util.addActivity(this);
        setContentView(R.layout.activity_screen_slide);
        this.top_black = findViewById(R.id.top_black);
        this.top_black.setAlpha(0.0f);
        this.mPowerManager = (PowerManager) getSystemService("power");
        this.mAppWidgetManager = AppWidgetManager.getInstance(this);
        this.mAppWidgetHost = new WatchAppWidgetHost(this, 1024);
        // PUT IT BACKfor (int id : ) {
        // PUT IT BACK    this.mAppWidgetHost.deleteAppWidgetId(id);
        // PUT IT BACK}
        this.mAppWidgetHost.startListening();
        this.mVibrator = (Vibrator) getSystemService("vibrator");
        this.mPager = (MainVerticalViewPager) findViewById(R.id.pager);
        this.mClockSaveView = (CustomDigitalClockSave) findViewById(R.id.clock_view);
        this.mClockSaveView.setClickCallback(new C01259());
        WatchApp.updateInstalledClocks(this);
        int style = System.getInt(getContentResolver(), "watch_app_list_style", 0);
        Log.d(TAG, "style" + style);
        this.mIdleFragment = new IdleFragment(style);
        this.mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        this.mPager.setAdapter(this.mPagerAdapter);
        this.mPager.setOffscreenPageLimit(3);
        this.mPager.setCurrentItem(1, true);
        this.mPager.setOnPageChangeListener(new SimpleOnPageChangeListener() {
            public void onPageSelected(int mPosition) {
                Log.d(MainActivity.TAG, "onPageSelected, pos=" + mPosition);
            }
        });
        this.mPager.setOnPageChangeListener(new OnPageChangeListener() {
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 1) {
                    MainActivity.this.top_black.setAlpha(positionOffset);
                } else if (position == 0) {
                    MainActivity.this.top_black.setAlpha(1.0f - positionOffset);
                }
            }

            public void onPageSelected(int position) {
            }

            public void onPageScrollStateChanged(int state) {
            }
        });
        IntentFilter mScreenUpdateFilter = new IntentFilter();
        mScreenUpdateFilter.addAction("com.mediatek.watchapp.NOTIFICATION_LISTENER.POSTED");
        mScreenUpdateFilter.addAction("com.mediatek.watchapp.NOTIFICATION_LISTENER.REMOVED");
        mScreenUpdateFilter.addAction("com.mediatek.watchapp.NOTIFICATION_LISTENER.REFRESH");
        mScreenUpdateFilter.addAction("android.intent.action.SCREEN_OFF");
        mScreenUpdateFilter.addAction("android.intent.action.SCREEN_ON");
        mScreenUpdateFilter.addAction("ipc_handphone_state_change");
        registerReceiver(this.mScreenUpdateReceiver, mScreenUpdateFilter);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addAction("android.intent.action.PACKAGE_REPLACED");
        filter.addAction("android.intent.action.PACKAGE_CHANGED");
        filter.addDataScheme("package");
        registerReceiver(this.mReceiver, filter);
        IntentFilter filter1 = new IntentFilter();
        filter1.addAction("android.intent.action.TIMEZONE_CHANGED");
        filter1.addAction("android.intent.action.LOCALE_CHANGED");
        filter1.addAction("watch_app_list_change");
        filter1.addAction("com.wiitetech.wiiwatch.write_settings");
        registerReceiver(this.mTZLocalReciever, filter1);
        IntentFilter batteryFilter = new IntentFilter();
        batteryFilter.addAction("android.intent.action.BATTERY_CHANGED");
        registerReceiver(this.mBatteryReceiver, batteryFilter);
        registerReceiver(this.mUpdateClockReceiver, new IntentFilter("com.update.installclock"));
        this.mExecutorService = Executors.newSingleThreadExecutor();
        IntentFilter unreadLoaderfilter = new IntentFilter();
        unreadLoaderfilter.addAction("com.mediatek.action.UNREAD_CHANGED");
        registerReceiver(this.mUnreadLoader, unreadLoaderfilter);
        WatchApp.reloadUnreadPhoneAndSMS(this);
        registerReceiver(this.mPowerSaver, new IntentFilter("android.os.action.POWER_SAVE_MODE_CHANGING"));
        this.mConfigurationFontScale = getResources().getConfiguration().fontScale;
    }

    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        int style = System.getInt(getContentResolver(), "watch_app_list_style", 0);
        if (this.mIdleFragment.getAppListStyle() != style) {
            this.mIdleFragment.setAppListStyle(style);
            this.mIdleFragment.update();
        }
        if (WatchApp.isFristLaunch) {
            if (this.mPowerManager.isPowerSaveMode()) {
                this.mHander.sendEmptyMessageDelayed(1003, 2000);
            }
            WatchApp.isFristLaunch = false;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        if (100 == requestCode && 50 == resultCode) {
            int index = data.getIntExtra("index", 0);
            WatchApp.setClockIndex(this, index);
            this.mIdleFragment.mClockFragment.ChangeClockApply(index);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void getChooseWatchView(int index) {
        Log.d(TAG, "getChooseWatchView");
        Intent intent = new Intent();
        intent.setClassName("com.mediatek.watchapp", "com.mediatek.watchapp.ChooseClockActivity");
        intent.putExtra("index", index);
        startActivityForResult(intent, 100);
        overridePendingTransition(R.anim.enter_anim, 0);
    }

    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        Util.removeActivity(this);
        unregisterReceiver(this.mScreenUpdateReceiver);
        unregisterReceiver(this.mReceiver);
        unregisterReceiver(this.mBatteryReceiver);
        unregisterReceiver(this.mTZLocalReciever);
        unregisterReceiver(this.mUpdateClockReceiver);
        unregisterReceiver(this.mUnreadLoader);
        unregisterReceiver(this.mPowerSaver);
        try {
            this.mAppWidgetHost.stopListening();
        } catch (NullPointerException ex) {
            Log.w(TAG, "problem while stopping AppWidgetHost during Launcher destruction", ex);
        }
        this.mAppWidgetHost = null;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown   keyCode=" + keyCode);
        if (keyCode == 4) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyUp  keyCode=" + keyCode);
        if (keyCode == 4) {
            if (!(this.mPager == null || this.mPager.getCurrentItem() == 1)) {
                this.mPager.setCurrentItem(1, false);
            }
            if (this.mIdleFragment != null) {
                this.mIdleFragment.backToClock();
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putBoolean("power_saver", WatchApp.mBatterySaver);
        outState.putBoolean("frist_launch", WatchApp.isFristLaunch);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        WatchApp.mBatterySaver = savedInstanceState.getBoolean("power_saver", false);
        WatchApp.isFristLaunch = savedInstanceState.getBoolean("frist_launch", false);
        Log.d(TAG, "onRestoreInstanceState: mBatterySaver = " + WatchApp.mBatterySaver + "\n  isFristLaunch = " + WatchApp.isFristLaunch);
    }

    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent");
        super.onNewIntent(intent);
        if ("android.intent.action.MAIN".equals(intent.getAction())) {
            Log.d(TAG, "onNewIntent - ACTION_MAIN");
            if (this.mPager != null) {
                this.mPager.setCurrentItem(1, false);
            }
        }
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        Log.d(TAG, "onWindowFocusChanged  hasFocus " + hasFocus);
        super.onWindowFocusChanged(hasFocus);
        this.mHasFocus = hasFocus;
        WatchApp.setTopActivityStatus(hasFocus);
        if (hasFocus && this.firstEnter) {
            this.firstEnter = false;
            String connect = System.getString(getContentResolver(), "wiiwatch_connect_state");
            if (connect != null && connect.equals("connected") && this.mQuickSettingsFragment != null && this.mQuickSettingsFragment.mQuickSettingsSubFragment != null) {
                this.mQuickSettingsFragment.mQuickSettingsSubFragment.setPhoneConnectedState(true);
            }
        }
    }

    private void refreshActivity(boolean batterySaver) {
        Log.d(TAG, "refreshActivity");
        if (this.mPager != null && this.mClockSaveView != null) {
            if (batterySaver) {
                this.mPager.setVisibility(View.GONE);
                this.mClockSaveView.setVisibility(View.VISIBLE);
                this.mHander.removeMessages(1001);
                this.mHander.sendEmptyMessageDelayed(1001, 6000);
            } else {
                this.mPager.setVisibility(View.VISIBLE);
                this.mClockSaveView.setVisibility(View.GONE);
                this.mHander.removeMessages(1001);
            }
            Log.d("MainActivity", "refreshActivity  " + batterySaver);
        }
    }

    public WatchAppWidgetHost getWatchWidgetHost() {
        return this.mAppWidgetHost;
    }

    public AppWidgetManager getWatchWidgetManager() {
        Log.d(TAG, "getWatchWidgetManager");
        return this.mAppWidgetManager;
    }

    public void backToNoteView(boolean needWakeup) {
        if (needWakeup) {
            @SuppressLint("WrongConstant") PowerManager powerManager = (PowerManager) getSystemService("power");
            if (!powerManager.isScreenOn()) {
                @SuppressLint("InvalidWakeLockTag") WakeLock wl = powerManager.newWakeLock(268435462, "bright");
                wl.acquire();
                wl.release();
            }
        }
        if (!(this.mPager == null || this.mPager.getCurrentItem() == 1)) {
            this.mPager.setCurrentItem(1, false);
        }
        if (this.mIdleFragment != null) {
            this.mIdleFragment.backToNotification();
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mConfigurationFontScale != newConfig.fontScale) {
            this.mConfigurationFontScale = newConfig.fontScale;
            if (this.mIdleFragment == null) {
            }
        }
    }

    public void delFolder(String folderPath) {
        try {
            delAllFile(folderPath);
            String str = folderPath;
            new File(folderPath.toString()).delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delAllFile(String path) {
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            String[] tempList = file.list();
            for (int i = 0; i < tempList.length; i++) {
                File temp;
                if (path.endsWith(File.separator)) {
                    temp = new File(path + tempList[i]);
                } else {
                    temp = new File(path + File.separator + tempList[i]);
                }
                if (temp.isFile()) {
                    temp.delete();
                }
                if (temp.isDirectory()) {
                    delAllFile(path + "/" + tempList[i]);
                    delFolder(path + "/" + tempList[i]);
                }
            }
        }
    }

    private void delClockSkinFromDB(String clockSkinId, Context c) {
        SQLiteDatabase mDb = new ClockSkinDBHelper(c, "clockskin.db", null, 1).getReadableDatabase();
        try {
            mDb.delete("clockskin_install_list", "skinid=?", new String[]{clockSkinId});
            Cursor countCur = mDb.rawQuery("select * from clockskin_online_list WHERE skinid=?", new String[]{clockSkinId});
            if (countCur.getCount() > 0) {
                countCur.moveToFirst();
                do {
                    ContentValues v = new ContentValues();
                    v.put("state", Integer.valueOf(0));
                    int n = mDb.update("clockskin_online_list", v, "skinid=?", new String[]{clockSkinId});
                    Intent it = new Intent("BROADCAST_DOWNLOAD_STATE_FILTER");
                    it.putExtra("BROADCAST_DOWNLOAD_STATE", 1003);
                    sendBroadcast(it);
                } while (countCur.moveToNext());
            }
            mDb.close();
        } catch (Throwable th) {
            mDb.close();
        }
    }

    private void showOnlineLoadingDialog(Context context) {
        if (this.mProgressDialog == null) {
            this.mProgressDialog = new Dialog(context, R.style.OnlineFontPreview);
            this.mProgressDialog.requestWindowFeature(1);
            this.mProgressDialog.getWindow().setType(2003);
            this.mProgressDialog.setContentView(R.layout.global_progressbar);
            this.mProgressDialog.setCancelable(true);
            this.mProgressDialog.setCanceledOnTouchOutside(false);
            this.mProgressDialog.show();
        }
    }

    private void dismissOnlineLoadingDialog() {
        if (this.mProgressDialog != null) {
            try {
                this.mProgressDialog.dismiss();
            } catch (Exception e) {
                Log.e(TAG, "Exception when Dialog.dismiss()...");
            } catch (Throwable th) {
                this.mProgressDialog = null;
            }
            this.mProgressDialog = null;
        }
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (WatchApp.getTopActivityStatus()) {
            return super.dispatchTouchEvent(ev);
        }
        return true;
    }
}
