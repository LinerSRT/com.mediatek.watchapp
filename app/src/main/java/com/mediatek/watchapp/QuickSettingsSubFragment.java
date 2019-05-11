package com.mediatek.watchapp;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings.System;
import android.support.v4.app.Fragment;
import android.support.wearable.R$styleable;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class QuickSettingsSubFragment extends Fragment {
    private final int SIGNAL_NO_SERVICE = 99;
    private final int SIGNAL_STRENGTH_GOOD = 3;
    private final int SIGNAL_STRENGTH_GREAT = 4;
    private final int SIGNAL_STRENGTH_MODERATE = 2;
    private final int SIGNAL_STRENGTH_NONE_OR_UNKNOWN = 0;
    private final int SIGNAL_STRENGTH_POOR = 1;
    private final int STATE_IN_SERVICE = 0;
    private final int STATE_OUT_OF_SERVICE = 1;
    private final String TAG = "QuickSettingsSubFragment";
    private boolean isAirPlaneMode = false;
    private boolean isCharging;
    private int mAsu = -999;
    private ImageView mBatteryDrawable;
    private Context mContext;
    private int mDbm = -999;
    private int mNetWorkType;
    private ImageView mNetworkState;
    private TextView mOprator;
    private ImageView mPhoneConnectState;
    private ImageView mSIMState;
    private int mServiceState = 1;
    private TelephonyManager mTelephonyManager;
    private Myreciever myreciever = new Myreciever();
    private int status;
    private PhoneStateListener watchPhoneStateListener = new C01461();

    /* renamed from: com.mediatek.watchapp.QuickSettingsSubFragment$1 */
    class C01461 extends PhoneStateListener {
        C01461() {
        }

        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            QuickSettingsSubFragment.this.mAsu = signalStrength.getGsmSignalStrength();
            QuickSettingsSubFragment.this.mDbm = signalStrength.getCdmaDbm();
            QuickSettingsSubFragment.this.updateSim();
        }

        public void onDataConnectionStateChanged(int state, int networkType) {
            QuickSettingsSubFragment.this.mNetWorkType = networkType;
            switch (state) {
            }
            QuickSettingsSubFragment.this.updateSim();
        }

        public void onServiceStateChanged(ServiceState state) {
            if (state != null) {
                QuickSettingsSubFragment.this.mServiceState = 0;
            } else {
                QuickSettingsSubFragment.this.mServiceState = 1;
            }
            QuickSettingsSubFragment.this.showSimOprater();
            QuickSettingsSubFragment.this.updateSim();
            super.onServiceStateChanged(state);
        }
    }

    /* renamed from: com.mediatek.watchapp.QuickSettingsSubFragment$2 */
    class C01472 implements OnClickListener {
        C01472() {
        }

        @SuppressLint("WrongConstant")
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.wiitetech.WiiWatchPro", "com.wiitetech.WiiWatchPro.ui.ServerActivity"));
            intent.setFlags(270532608);
            try {
                QuickSettingsSubFragment.this.startActivity(intent);
            } catch (ActivityNotFoundException e) {
            }
        }
    }

    public class Myreciever extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            boolean z = true;
            String action = intent.getAction();
            if (action.equals("android.intent.action.BATTERY_CHANGED")) {
                QuickSettingsSubFragment.this.status = intent.getIntExtra("status", -1);
                QuickSettingsSubFragment quickSettingsSubFragment = QuickSettingsSubFragment.this;
                if (!(QuickSettingsSubFragment.this.status == 2 || QuickSettingsSubFragment.this.status == 5)) {
                    z = false;
                }
                quickSettingsSubFragment.isCharging = z;
                if (QuickSettingsSubFragment.this.isCharging) {
                    QuickSettingsSubFragment.this.mBatteryDrawable.setVisibility(View.VISIBLE);
                } else {
                    QuickSettingsSubFragment.this.mBatteryDrawable.setVisibility(View.INVISIBLE);
                }
            } else if (action.equals("android.intent.action.AIRPLANE_MODE")) {
                QuickSettingsSubFragment.this.isAirPlaneMode = intent.getBooleanExtra("state", false);
                QuickSettingsSubFragment.this.mServiceState = 1;
                QuickSettingsSubFragment.this.showSimOprater();
                QuickSettingsSubFragment.this.updateSim();
            }
        }
    }

    @SuppressLint("WrongConstant")
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mContext = getActivity();
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mTelephonyManager.listen(this.watchPhoneStateListener, 321);
        this.mNetWorkType = this.mTelephonyManager.getNetworkType();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BATTERY_CHANGED");
        filter.addAction("android.intent.action.AIRPLANE_MODE");
        this.mContext.registerReceiver(this.myreciever, filter);
        this.isAirPlaneMode = isAirPlaneModeOn();
    }

    public void onDestroy() {
        super.onDestroy();
        this.mContext.unregisterReceiver(this.myreciever);
    }

    public void onResume() {
        super.onResume();
        showSimOprater();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.quicksetting_subfragment_layout, container, false);
        this.mBatteryDrawable = (ImageView) view.findViewById(R.id.BatteryDrawable);
        this.mPhoneConnectState = (ImageView) view.findViewById(R.id.phone_connect);
        this.mPhoneConnectState.setOnClickListener(new C01472());
        boolean state = System.getInt(this.mContext.getContentResolver(), "WiiWatchBluetoothState", 0) == 3;
        if (1 == System.getInt(this.mContext.getContentResolver(), "watch_app_phone_connect_state", 0)) {
            state = true;
        }
        setPhoneConnectedState(state);
        this.mSIMState = (ImageView) view.findViewById(R.id.sim_status);
        this.mNetworkState = (ImageView) view.findViewById(R.id.network_stat);
        this.mOprator = (TextView) view.findViewById(R.id.sim_oprator);
        this.mSIMState.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.signal_no));
        return view;
    }

    public void setPhoneConnectedState(boolean connected) {
        if (connected) {
            this.mPhoneConnectState.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.phone_connected));
        } else {
            this.mPhoneConnectState.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.phone_disconnect));
        }
    }

    private boolean is2G() {
        if (this.mNetWorkType == 1 || this.mNetWorkType == 2) {
            return true;
        }
        return false;
    }

    private boolean isAirPlaneModeOn() {
        if (System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0) {
            return true;
        }
        return false;
    }

    public int mapSignalLevel(boolean is3G, int dBm, int asu) {
        int level = 0;
        switch (this.mNetWorkType) {
            case 1:
            case 2:
            case 4:
            case 7:
            case 11:
                if (asu > 2 && asu != 99) {
                    if (asu < 12) {
                        if (asu < 8) {
                            if (asu < 5) {
                                level = 1;
                                break;
                            }
                            level = 2;
                            break;
                        }
                        level = 3;
                        break;
                    }
                    level = 4;
                    break;
                }
                level = 0;
                break;
            case 3:
            case 5:
            case 6:
            case 8:
            case 9:
            case 10:
            case 12:
            case 14:
            case R$styleable.ActionPage_imageScaleMode /*15*/:
                if (dBm > -111 && dBm != 85) {
                    if (dBm < -91) {
                        if (dBm < -98) {
                            if (dBm < -103) {
                                level = 1;
                                break;
                            }
                            level = 2;
                            break;
                        }
                        level = 3;
                        break;
                    }
                    level = 4;
                    break;
                }
                level = 0;
                break;
            case 13:
                if (dBm > -131 && dBm != 85) {
                    if (dBm < -111) {
                        if (dBm < -118) {
                            if (dBm < -123) {
                                level = 1;
                                break;
                            }
                            level = 2;
                            break;
                        }
                        level = 3;
                        break;
                    }
                    level = 4;
                    break;
                }
                level = 0;
                break;
        }
        if (this.mServiceState != 0) {
            return 99;
        }
        return level;
    }

    private void updateSim() {
        boolean is3G = !is2G();
        int level = mapSignalLevel(is3G, this.mDbm, this.mAsu);
        Log.v("QuickSettingsSubFragment", "updateSim::is3G =" + is3G);
        Log.v("QuickSettingsSubFragment", "updateSim::mDbm =" + this.mDbm);
        Log.v("QuickSettingsSubFragment", "updateSim::mAsu =" + this.mAsu);
        Log.v("QuickSettingsSubFragment", "updateSim::level =" + level);
        boolean hasSim = true;
        if (isSimInserd()) {
            if (!this.isAirPlaneMode) {
                if (this.mServiceState != 1) {
                    switch (level) {
                        case 0:
                            this.mSIMState.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.signal_0));
                            break;
                        case 1:
                            this.mSIMState.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.signal_1));
                            break;
                        case 2:
                            this.mSIMState.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.signal_2));
                            break;
                        case 3:
                            this.mSIMState.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.signal_3));
                            break;
                        case 4:
                            this.mSIMState.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.signal_4));
                            break;
                        case 99:
                            this.mSIMState.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.signal_no));
                            hasSim = false;
                            break;
                        default:
                            this.mSIMState.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.signal_no));
                            hasSim = false;
                            break;
                    }
                }
                this.mSIMState.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.signal_search));
            } else {
                this.mSIMState.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.signal_plane));
                this.mNetworkState.setImageDrawable(null);
                return;
            }
        }
        this.mSIMState.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.signal_no));
        hasSim = false;
        if (hasSim) {
            switch (this.mNetWorkType) {
                case 1:
                case 2:
                case 4:
                case 7:
                case 11:
                    this.mNetworkState.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.network_2g));
                    break;
                case 3:
                case 5:
                case 6:
                case 8:
                case 9:
                case 10:
                case 12:
                case 14:
                case R$styleable.ActionPage_imageScaleMode /*15*/:
                    this.mNetworkState.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.network_3g));
                    break;
                case 13:
                    this.mNetworkState.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.network_4g));
                    break;
                default:
                    this.mNetworkState.setImageDrawable(null);
                    break;
            }
        }
        this.mNetworkState.setImageDrawable(null);
    }

    private void showSimOprater() {
        if (this.isAirPlaneMode) {
            this.mOprator.setText(" ");
            this.mOprator.setVisibility(View.GONE);
            return;
        }
        String oprator = this.mTelephonyManager.getNetworkOperatorName();
        if (oprator != null && !oprator.equals("")) {
            this.mOprator.setVisibility(View.VISIBLE);
            this.mOprator.setText(oprator);
        }
    }

    private boolean isSimInserd() {
        if (this.mTelephonyManager != null) {
            return this.mTelephonyManager.hasIccCard();
        }
        return false;
    }
}
