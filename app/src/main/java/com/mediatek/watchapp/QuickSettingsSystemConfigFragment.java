package com.mediatek.watchapp;

import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.support.v4.app.Fragment;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

public class QuickSettingsSystemConfigFragment extends Fragment {
    private boolean SimState = false;
    private final String TAG = "QuickSettingsSystemConfigFragment";
    OnClickListener click = new C01535();
    private FrameLayout frame;
    private Handler handle = new Handler();
    private ImageView high;
    private boolean isFindMeWorking = true;
    private ImageView low;
    private AnimationDrawable mAnimBTDrawable;
    private AnimationDrawable mAnimationDrawable;
    private ImageView mBTState;
    private BluetoothAdapter mBTadapter = null;
    private Context mContext;
    private Handler mFindMeHandler = new C01546();
    private IntentFilter mIntentFilter;
    private BroadcastReceiver mIntentReceiver = new C01492();
    private boolean mIsWifiConnected = false;
    private NotificationManager mNoMan;
    View.OnClickListener mOnClickListener = new C01514();
    OnLongClickListener mOnLongClickListener = new C01503();
    private PowerManager mPowerManager;
    private TelephonyManager mTelephonyManager;
    private WifiManager mWifiManager;
    private ImageView mWifiState;
    private ImageView m_mobile_data_settings;
    private ImageView m_notifyStyle;
    private ImageView m_system_airplane_mode;
    private ImageView m_system_find_my_device;
    private ImageView m_system_screenon_guesture;
    private int maxBrightnessLevel = 0;
    private ImageView midd;
    private int minBrightnessLevel = 0;
    Handler myHandler = new C01481();
    private Runnable runnable;
    boolean setNetworkTypeDone = true;
    private BroadcastReceiver wifiIntentReceiver = new C01567();

    /* renamed from: com.mediatek.watchapp.QuickSettingsSystemConfigFragment$1 */
    class C01481 extends Handler {
        C01481() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 55:
                    QuickSettingsSystemConfigFragment.this.setMobiledataInfo();
                    return;
                default:
                    return;
            }
        }
    }

    /* renamed from: com.mediatek.watchapp.QuickSettingsSystemConfigFragment$2 */
    class C01492 extends BroadcastReceiver {
        C01492() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            QuickSettingsSystemConfigFragment.this.setMobiledataInfo();
        }
    }

    /* renamed from: com.mediatek.watchapp.QuickSettingsSystemConfigFragment$3 */
    class C01503 implements OnLongClickListener {
        C01503() {
        }

        public boolean onLongClick(View v) {
            QuickSettingsSystemConfigFragment.this.mContext.startActivity(new Intent("android.intent.action.SHOW_BRIGHTNESS_DIALOG"));
            return true;
        }
    }

    /* renamed from: com.mediatek.watchapp.QuickSettingsSystemConfigFragment$4 */
    class C01514 implements View.OnClickListener {
        C01514() {
        }

        public void onClick(View v) {
            boolean z = true;
            boolean z2 = false;
            String toast_info;
            switch (v.getId()) {
                case R.id.notify_style:
                    boolean status = QuickSettingsSystemConfigFragment.this.getNotificationComingState();
                    QuickSettingsSystemConfigFragment quickSettingsSystemConfigFragment = QuickSettingsSystemConfigFragment.this;
                    if (status) {
                        z = false;
                    }
                    quickSettingsSystemConfigFragment.setNotificationComingState(z);
                    if (!status) {
                        QuickSettingsSystemConfigFragment.this.m_notifyStyle.setImageResource(R.drawable.zenmode_off);
                        Toast.makeText(QuickSettingsSystemConfigFragment.this.mContext, QuickSettingsSystemConfigFragment.this.mContext.getResources().getString(R.string.send_info_text1).toString(), 0).show();
                        break;
                    }
                    QuickSettingsSystemConfigFragment.this.m_notifyStyle.setImageResource(R.drawable.zenmode_on);
                    Toast.makeText(QuickSettingsSystemConfigFragment.this.mContext, QuickSettingsSystemConfigFragment.this.mContext.getResources().getString(R.string.send_info_text2).toString(), 0).show();
                    break;
                case R.id.low:
                    QuickSettingsSystemConfigFragment.this.midd.setVisibility(0);
                    QuickSettingsSystemConfigFragment.this.high.setVisibility(8);
                    QuickSettingsSystemConfigFragment.this.low.setVisibility(8);
                    QuickSettingsSystemConfigFragment.this.setBrightnessLevel(((QuickSettingsSystemConfigFragment.this.maxBrightnessLevel - QuickSettingsSystemConfigFragment.this.minBrightnessLevel) / 2) + QuickSettingsSystemConfigFragment.this.minBrightnessLevel);
                    break;
                case R.id.high:
                    QuickSettingsSystemConfigFragment.this.midd.setVisibility(8);
                    QuickSettingsSystemConfigFragment.this.high.setVisibility(8);
                    QuickSettingsSystemConfigFragment.this.low.setVisibility(0);
                    QuickSettingsSystemConfigFragment.this.setBrightnessLevel(QuickSettingsSystemConfigFragment.this.minBrightnessLevel);
                    break;
                case R.id.midd:
                    QuickSettingsSystemConfigFragment.this.midd.setVisibility(8);
                    QuickSettingsSystemConfigFragment.this.high.setVisibility(0);
                    QuickSettingsSystemConfigFragment.this.low.setVisibility(8);
                    QuickSettingsSystemConfigFragment.this.setBrightnessLevel(QuickSettingsSystemConfigFragment.this.maxBrightnessLevel);
                    break;
                case R.id.system_screenon_guesture:
                    Intent intent;
                    if (QuickSettingsSystemConfigFragment.this.getSystemRaiseWakeUp()) {
                        QuickSettingsSystemConfigFragment.this.setRaiseWakeUpOnOff(false);
                        QuickSettingsSystemConfigFragment.this.m_system_screenon_guesture.setImageResource(R.drawable.smart_watch_screenon_guesture_off);
                        toast_info = QuickSettingsSystemConfigFragment.this.mContext.getResources().getString(R.string.screenon_guesture_off).toString();
                        intent = new Intent();
                        intent.setAction("action.RAISE_WAKEUP_ENABLE");
                        intent.putExtra("raise_wakeup_enable", false);
                        QuickSettingsSystemConfigFragment.this.getActivity().sendBroadcast(intent);
                        Log.d("QuickSettingsSystemConfigFragment", "stop ScreenSensorService");
                    } else {
                        QuickSettingsSystemConfigFragment.this.setRaiseWakeUpOnOff(true);
                        QuickSettingsSystemConfigFragment.this.m_system_screenon_guesture.setImageResource(R.drawable.smart_watch_screenon_guesture_on);
                        toast_info = QuickSettingsSystemConfigFragment.this.mContext.getResources().getString(R.string.screenon_guesture_on).toString();
                        intent = new Intent();
                        intent.setAction("action.RAISE_WAKEUP_ENABLE");
                        intent.putExtra("raise_wakeup_enable", true);
                        QuickSettingsSystemConfigFragment.this.getActivity().sendBroadcast(intent);
                        Log.d("QuickSettingsSystemConfigFragment", "start ScreenSensorService");
                    }
                    Toast.makeText(QuickSettingsSystemConfigFragment.this.mContext, toast_info, 0).show();
                    break;
                case R.id.system_airplane_mode:
                    if (QuickSettingsSystemConfigFragment.this.isAirPlaneModeOn()) {
                        QuickSettingsSystemConfigFragment.this.setAirPlaneModeOn(false);
                        QuickSettingsSystemConfigFragment.this.m_system_airplane_mode.setImageResource(R.drawable.smart_watch_airmode_off);
                        toast_info = QuickSettingsSystemConfigFragment.this.mContext.getResources().getString(R.string.airplane_mode_off).toString();
                    } else {
                        QuickSettingsSystemConfigFragment.this.setAirPlaneModeOn(true);
                        QuickSettingsSystemConfigFragment.this.m_system_airplane_mode.setImageResource(R.drawable.smart_watch_airmode_on);
                        toast_info = QuickSettingsSystemConfigFragment.this.mContext.getResources().getString(R.string.airplane_mode_on).toString();
                    }
                    Toast.makeText(QuickSettingsSystemConfigFragment.this.mContext, toast_info, 0).show();
                    break;
                case R.id.mobile_data_settings:
                    if (TelephonyManager.getDefault().hasIccCard()) {
                        if (!QuickSettingsSystemConfigFragment.this.isAirPlaneModeOn() && QuickSettingsSystemConfigFragment.this.mTelephonyManager.getCallState() == 0) {
                            boolean isEnable = QuickSettingsSystemConfigFragment.this.mTelephonyManager.getDataEnabled(1);
                            TelephonyManager get4 = QuickSettingsSystemConfigFragment.this.mTelephonyManager;
                            if (!isEnable) {
                                z2 = true;
                            }
                            get4.setDataEnabled(1, z2);
                            QuickSettingsSystemConfigFragment.this.myHandler.sendEmptyMessageDelayed(55, 2000);
                            break;
                        }
                        return;
                    }
                    Toast.makeText(QuickSettingsSystemConfigFragment.this.mContext, R.string.mobiledata_remind, 0).show();
                    return;
                    break;
                case R.id.system_connected_device:
                    if (!QuickSettingsSystemConfigFragment.this.isGpsModeOn()) {
                        QuickSettingsSystemConfigFragment.this.setGpsModeOn(true);
                        QuickSettingsSystemConfigFragment.this.m_system_find_my_device.setImageResource(R.drawable.gps_on);
                        break;
                    }
                    QuickSettingsSystemConfigFragment.this.setGpsModeOn(false);
                    QuickSettingsSystemConfigFragment.this.m_system_find_my_device.setImageResource(R.drawable.gps_off);
                    break;
            }
        }
    }

    /* renamed from: com.mediatek.watchapp.QuickSettingsSystemConfigFragment$5 */
    class C01535 implements OnClickListener {
        C01535() {
        }

        public void onClick(DialogInterface dialog, int which) {
            boolean z = false;
            final int subId = SubscriptionManager.getSubIdUsingPhoneId(0);
            if (which == -1) {
                QuickSettingsSystemConfigFragment.this.setNetworkTypeDone = false;
                boolean isEnable = QuickSettingsSystemConfigFragment.this.mTelephonyManager.getDataEnabled(subId);
                TelephonyManager get4 = QuickSettingsSystemConfigFragment.this.mTelephonyManager;
                if (!isEnable) {
                    z = true;
                }
                get4.setDataEnabled(subId, z);
                QuickSettingsSystemConfigFragment.this.myHandler.sendEmptyMessageDelayed(55, 2000);
                final int networkType = !isEnable ? 1 : 0;
                new Thread(new Runnable() {
                    public void run() {
                        QuickSettingsSystemConfigFragment.this.mTelephonyManager.setPreferredNetworkType(subId, networkType);
                        Global.putInt(QuickSettingsSystemConfigFragment.this.mContext.getContentResolver(), "preferred_network_mode" + subId, networkType);
                        QuickSettingsSystemConfigFragment.this.setNetworkTypeDone = true;
                    }
                }).start();
            }
            dialog.dismiss();
        }
    }

    /* renamed from: com.mediatek.watchapp.QuickSettingsSystemConfigFragment$6 */
    class C01546 extends Handler {
        C01546() {
        }

        public void handleMessage(Message msg) {
            Log.d("QuickSettingsSystemConfigFragment", "mFindMeHandler handleMessage, msg.what = " + msg.what);
            switch (msg.what) {
                case 1:
                    QuickSettingsSystemConfigFragment.this.updateUiViews();
                    return;
                case 2:
                    QuickSettingsSystemConfigFragment.this.updateImage();
                    return;
                default:
                    return;
            }
        }
    }

    /* renamed from: com.mediatek.watchapp.QuickSettingsSystemConfigFragment$7 */
    class C01567 extends BroadcastReceiver {

        /* renamed from: com.mediatek.watchapp.QuickSettingsSystemConfigFragment$7$1 */
        class C01551 implements Runnable {
            C01551() {
            }

            public void run() {
                QuickSettingsSystemConfigFragment.this.wifiConnectedShow(QuickSettingsSystemConfigFragment.this.mContext);
            }
        }

        C01567() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                switch (intent.getIntExtra("wifi_state", 0)) {
                    case 0:
                        QuickSettingsSystemConfigFragment.this.wifiAnimaStart();
                        return;
                    case 1:
                        QuickSettingsSystemConfigFragment.this.mIsWifiConnected = false;
                        QuickSettingsSystemConfigFragment.this.wifiDisConnectedShow(context);
                        return;
                    case 2:
                        QuickSettingsSystemConfigFragment.this.wifiAnimaStart();
                        return;
                    case 3:
                        new Handler().postDelayed(new C01551(), 500);
                        return;
                    default:
                        return;
                }
            } else if (action.equals("android.net.wifi.supplicant.CONNECTION_CHANGE")) {
                if (WifiInfo.getDetailedStateOf((SupplicantState) intent.getParcelableExtra("connected")) != DetailedState.CONNECTED) {
                }
            } else if (action.equals("android.bluetooth.adapter.action.STATE_CHANGED")) {
                QuickSettingsSystemConfigFragment.this.updateBTStat();
            } else if (action.equals("android.intent.action.AIRPLANE_MODE")) {
                if (QuickSettingsSystemConfigFragment.this.isAirPlaneModeOn()) {
                    QuickSettingsSystemConfigFragment.this.m_system_airplane_mode.setImageResource(R.drawable.smart_watch_airmode_on);
                } else {
                    QuickSettingsSystemConfigFragment.this.m_system_airplane_mode.setImageResource(R.drawable.smart_watch_airmode_off);
                }
            } else if (!action.equals("android.intent.action.LOCALE_CHANGED")) {
            }
        }
    }

    /* renamed from: com.mediatek.watchapp.QuickSettingsSystemConfigFragment$8 */
    class C01578 implements View.OnClickListener {
        C01578() {
        }

        public void onClick(View v) {
            if (QuickSettingsSystemConfigFragment.this.mBTadapter.getState() == 10) {
                QuickSettingsSystemConfigFragment.this.mBTadapter.enable();
            } else if (QuickSettingsSystemConfigFragment.this.mBTadapter.getState() == 12) {
                QuickSettingsSystemConfigFragment.this.mBTadapter.disable();
            }
        }
    }

    /* renamed from: com.mediatek.watchapp.QuickSettingsSystemConfigFragment$9 */
    class C01589 implements OnLongClickListener {
        C01589() {
        }

        public boolean onLongClick(View v) {
            QuickSettingsSystemConfigFragment.this.startBTSettings();
            return true;
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mContext = getActivity();
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        getMaxAndMinBrightnessLevel();
        this.mTelephonyManager = TelephonyManager.from(this.mContext);
        this.SimState = TelephonyManager.getDefault().hasIccCard();
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.mIntentFilter.addAction("android.intent.action.ANY_DATA_STATE");
        IntentFilter wifiIntentFilter = new IntentFilter();
        wifiIntentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        wifiIntentFilter.addAction("android.net.wifi.supplicant.CONNECTION_CHANGE");
        wifiIntentFilter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
        wifiIntentFilter.addAction("android.intent.action.AIRPLANE_MODE");
        wifiIntentFilter.addAction("android.intent.action.LOCALE_CHANGED");
        this.mContext.registerReceiver(this.wifiIntentReceiver, wifiIntentFilter);
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mBTadapter = BluetoothAdapter.getDefaultAdapter();
        this.mNoMan = (NotificationManager) this.mContext.getSystemService("notification");
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.quicksetting_system_config, container, false);
        this.m_notifyStyle = (ImageView) view.findViewById(R.id.notify_style);
        this.m_system_screenon_guesture = (ImageView) view.findViewById(R.id.system_screenon_guesture);
        this.m_system_airplane_mode = (ImageView) view.findViewById(R.id.system_airplane_mode);
        this.m_system_find_my_device = (ImageView) view.findViewById(R.id.system_connected_device);
        this.m_mobile_data_settings = (ImageView) view.findViewById(R.id.mobile_data_settings);
        this.frame = (FrameLayout) view.findViewById(R.id.frame);
        this.low = (ImageView) view.findViewById(R.id.low);
        this.midd = (ImageView) view.findViewById(R.id.midd);
        this.high = (ImageView) view.findViewById(R.id.high);
        this.mBTState = (ImageView) view.findViewById(R.id.bt_stat);
        updateBTStat();
        this.mBTState.setOnClickListener(new C01578());
        this.mBTState.setOnLongClickListener(new C01589());
        this.mWifiState = (ImageView) view.findViewById(R.id.wifi_status);
        if (this.mWifiManager.isWifiEnabled()) {
            wifiConnectedShow(this.mContext);
        } else {
            this.mWifiState.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.wifi_off));
        }
        this.mWifiState.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (QuickSettingsSystemConfigFragment.this.mWifiManager.isWifiEnabled()) {
                    QuickSettingsSystemConfigFragment.this.mWifiManager.setWifiEnabled(false);
                } else {
                    QuickSettingsSystemConfigFragment.this.mWifiManager.setWifiEnabled(true);
                }
            }
        });
        this.mWifiState.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                QuickSettingsSystemConfigFragment.this.startWifiSettings();
                return true;
            }
        });
        this.m_system_find_my_device.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                QuickSettingsSystemConfigFragment.this.startGPSsettings();
                return true;
            }
        });
        return view;
    }

    private void initViews() {
        setViewState();
        setViewsOnClickListener();
        setBrightnessLevelBg();
    }

    private void setViewState() {
        if (getNotificationComingState()) {
            this.m_notifyStyle.setImageResource(R.drawable.zenmode_off);
        } else {
            this.m_notifyStyle.setImageResource(R.drawable.zenmode_on);
        }
        if (getSystemRaiseWakeUp()) {
            this.m_system_screenon_guesture.setImageResource(R.drawable.smart_watch_screenon_guesture_on);
        } else {
            this.m_system_screenon_guesture.setImageResource(R.drawable.smart_watch_screenon_guesture_off);
        }
        if (isAirPlaneModeOn()) {
            this.m_system_airplane_mode.setImageResource(R.drawable.smart_watch_airmode_on);
        } else {
            this.m_system_airplane_mode.setImageResource(R.drawable.smart_watch_airmode_off);
        }
        setMobiledataInfo();
        this.mContext.registerReceiver(this.mIntentReceiver, this.mIntentFilter);
        if (isGpsModeOn()) {
            this.m_system_find_my_device.setImageResource(R.drawable.gps_on);
        } else {
            this.m_system_find_my_device.setImageResource(R.drawable.gps_off);
        }
    }

    public void onResume() {
        super.onResume();
        initViews();
        if (this.mWifiManager.getWifiState() == 1) {
            wifiDisConnectedShow(this.mContext);
        } else {
            wifiConnectedShow(this.mContext);
        }
    }

    public void onPause() {
        super.onPause();
        this.mContext.unregisterReceiver(this.mIntentReceiver);
    }

    private void setViewsOnClickListener() {
        this.m_notifyStyle.setOnClickListener(this.mOnClickListener);
        this.m_system_screenon_guesture.setOnClickListener(this.mOnClickListener);
        this.m_system_airplane_mode.setOnClickListener(this.mOnClickListener);
        this.m_system_find_my_device.setOnClickListener(this.mOnClickListener);
        this.m_mobile_data_settings.setOnClickListener(this.mOnClickListener);
        this.high.setOnClickListener(this.mOnClickListener);
        this.low.setOnClickListener(this.mOnClickListener);
        this.midd.setOnClickListener(this.mOnClickListener);
        this.high.setOnLongClickListener(this.mOnLongClickListener);
        this.low.setOnLongClickListener(this.mOnLongClickListener);
        this.midd.setOnLongClickListener(this.mOnLongClickListener);
    }

    private void setMobiledataInfo() {
        if (this.mTelephonyManager.getDataEnabled(1) && !isAirPlaneModeOn() && TelephonyManager.getDefault().hasIccCard()) {
            this.m_mobile_data_settings.setImageResource(R.drawable.smart_watch_mobile_data_on);
        } else {
            this.m_mobile_data_settings.setImageResource(R.drawable.smart_watch_mobile_data_off);
        }
    }

    private void setBrightnessLevelBg() {
        if (getCurrBrightnessLevel() == 0) {
            this.midd.setVisibility(8);
            this.high.setVisibility(8);
            this.low.setVisibility(0);
        } else if (getCurrBrightnessLevel() == 1) {
            this.midd.setVisibility(0);
            this.high.setVisibility(8);
            this.low.setVisibility(8);
        } else if (getCurrBrightnessLevel() == 2) {
            this.midd.setVisibility(8);
            this.high.setVisibility(0);
            this.low.setVisibility(8);
        }
    }

    private boolean getNotificationComingState() {
        return this.mNoMan.getZenMode() != 3;
    }

    private void setNotificationComingState(boolean b) {
        this.mNoMan.setZenMode(b ? 0 : 3, null, "F");
    }

    private boolean getSystemRaiseWakeUp() {
        return SystemProperties.getBoolean("persist.sys.raise.wakeup", false);
    }

    private void setRaiseWakeUpOnOff(boolean on) {
        SystemProperties.set("persist.sys.raise.wakeup", String.valueOf(on));
        System.putString(getActivity().getContentResolver(), "persist.sys.raise.wakeup", String.valueOf(on));
    }

    private boolean isAirPlaneModeOn() {
        if (System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0) {
            return true;
        }
        return false;
    }

    private boolean isGpsModeOn() {
        if (Secure.getInt(this.mContext.getContentResolver(), "location_mode", 0) != 0) {
            return true;
        }
        return false;
    }

    private void setGpsModeOn(boolean on) {
        Secure.putInt(this.mContext.getContentResolver(), "location_mode", on ? 1 : 0);
    }

    private void setAirPlaneModeOn(boolean on) {
        Global.putInt(this.mContext.getContentResolver(), "airplane_mode_on", on ? 1 : 0);
        Intent intent = new Intent("android.intent.action.AIRPLANE_MODE");
        intent.putExtra("state", on);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
        Log.d("QuickSettingsSystemConfigFragment", "setAirPlaneModeOff()");
    }

    private int getCurrBrightnessLevel() {
        int value = 30;
        try {
            value = System.getInt(this.mContext.getContentResolver(), "screen_brightness");
        } catch (SettingNotFoundException e) {
        }
        if (value <= 110) {
            return 0;
        }
        if (value > 110 && value < 220) {
            return 1;
        }
        if (value >= 220) {
            return 2;
        }
        return 0;
    }

    private void getMaxAndMinBrightnessLevel() {
        this.maxBrightnessLevel = this.mPowerManager.getMaximumScreenBrightnessSetting();
        this.minBrightnessLevel = this.mPowerManager.getMinimumScreenBrightnessSetting();
    }

    private void setBrightnessLevel(int level) {
        this.mPowerManager.setBacklightBrightness(level);
        System.putInt(this.mContext.getContentResolver(), "screen_brightness", level);
    }

    private void updateImage() {
    }

    private void updateUiViews() {
    }

    private void wifiConnectedShow(Context context) {
        int level = ((WifiManager) context.getSystemService("wifi")).getConnectionInfo().getRssi();
        wifiAnimaStop();
        if (level > -55) {
            this.mWifiState.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.wifi_4));
        } else if (level > -70) {
            this.mWifiState.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.wifi_3));
        } else if (level > -85) {
            this.mWifiState.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.wifi_2));
        } else if (level > -100) {
            this.mWifiState.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.wifi_1));
        } else {
            this.mWifiState.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.wifi_4));
        }
    }

    private void wifiDisConnectedShow(Context context) {
        this.mIsWifiConnected = false;
        wifiAnimaStop();
        this.mWifiState.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.wifi_off));
    }

    private void startWifiSettings() {
        this.mContext.startActivity(new Intent("android.settings.WIFI_SETTINGS"));
    }

    private void startBTSettings() {
        this.mContext.startActivity(new Intent("android.settings.BLUETOOTH_SETTINGS"));
    }

    private void startGPSsettings() {
        this.mContext.startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
    }

    private void wifiAnimaStart() {
        this.mWifiState.setImageResource(R.anim.anima_wifi);
        this.mAnimationDrawable = (AnimationDrawable) this.mWifiState.getDrawable();
        this.mAnimationDrawable.start();
    }

    private void wifiAnimaStop() {
        if (this.mAnimationDrawable != null) {
            this.mAnimationDrawable.stop();
        }
    }

    private void BTAnimaStart() {
        this.mBTState.setImageResource(R.anim.anima_bt);
        this.mAnimBTDrawable = (AnimationDrawable) this.mBTState.getDrawable();
        this.mAnimBTDrawable.start();
    }

    private void BTAnimaStop() {
        if (this.mAnimBTDrawable != null) {
            this.mAnimBTDrawable.stop();
        }
    }

    private void updateBTStat() {
        int blueState = this.mBTadapter.getState();
        if (blueState == 12) {
            BTAnimaStop();
            this.mBTState.setImageResource(R.drawable.bt_on);
        } else if (blueState == 10) {
            BTAnimaStop();
            this.mBTState.setImageResource(R.drawable.bt_off);
        } else if (blueState == 13 || blueState == 11) {
            BTAnimaStart();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        this.mContext.unregisterReceiver(this.wifiIntentReceiver);
        this.handle.removeCallbacks(this.runnable);
    }
}
