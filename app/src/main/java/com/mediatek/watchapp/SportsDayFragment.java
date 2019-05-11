package com.mediatek.watchapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings.System;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import com.mediatek.watchapp.view.CalculatorUtil;
import com.mediatek.watchapp.view.User;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

public class SportsDayFragment extends Fragment {
    private final String TAG = "SportsStepFragment";
    int data = -1;
    private boolean hasStarted = false;
    Time mCalendar = new Time();
    private Context mContext;
    public int mCurrentSteps = 0;
    private boolean mIsVisibleToUser = false;
    private final Receiver mReceiver = new Receiver();
    private MiniColumnChart mStepsMeterView = null;
    private TextView mTvAverg;
    private TextView mTvTotall;
    private TextView mTvTotallDis;
    private String queryUri = "content://com.watchHealth.Walk/query";
    private ContentResolver resolver;
    User user;

    /* renamed from: com.mediatek.watchapp.SportsDayFragment$1 */
    class C01591 implements OnClickListener {
        C01591() {
        }

        public void onClick(View v) {
            Intent i = new Intent();
            i.setComponent(new ComponentName("creator.android.SHealth", "creator.android.SHealth.SettingsActivity"));
            SportsDayFragment.this.startActivity(i);
        }
    }

    private final class Receiver extends BroadcastReceiver {
        private Receiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.sinsoft.action.health.step_count")) {
                SportsDayFragment.this.showUI(false);
            }
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mContext = getActivity();
        this.resolver = this.mContext.getContentResolver();
    }

    private boolean cursorIsNull(Cursor paramCursor) {
        return paramCursor == null || paramCursor.getCount() < 1;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sports_day_fragment_layout, container, false);
        this.mCurrentSteps = WatchApp.getSteps(this.mContext);
        this.mStepsMeterView = (MiniColumnChart) view.findViewById(R.id.stepDayview);
        this.mTvAverg = (TextView) view.findViewById(R.id.tv_averg_step);
        this.mTvTotall = (TextView) view.findViewById(R.id.tv_totall_step);
        this.mTvTotallDis = (TextView) view.findViewById(R.id.tv_totall_distance);
        ((ImageButton) view.findViewById(R.id.sport_target_settings)).setOnClickListener(new C01591());
        registerStepsCntReceiver();
        showUI(true);
        return view;
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    private void registerStepsCntReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.sinsoft.action.health.step_count");
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    private void unRegisterStepsCntReceiver() {
        this.mContext.unregisterReceiver(this.mReceiver);
    }

    public String getCurrentDateByOffset(String paramString, int paramInt1, int paramInt2) {
        try {
            SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat(paramString);
            GregorianCalendar localGregorianCalendar = new GregorianCalendar();
            localGregorianCalendar.add(paramInt1, paramInt2);
            return localSimpleDateFormat.format(localGregorianCalendar.getTime());
        } catch (Exception localException) {
            localException.printStackTrace();
            return null;
        }
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.mIsVisibleToUser = isVisibleToUser;
        if (this.mStepsMeterView != null) {
            if (isVisibleToUser) {
                this.hasStarted = true;
                registerStepsCntReceiver();
                showUI(true);
            } else if (this.hasStarted) {
                this.hasStarted = false;
                unRegisterStepsCntReceiver();
                this.mStepsMeterView.cleanAnim();
            }
        }
    }

    public int[] queryStepDate(ContentResolver resolver) {
        int weeday;
        this.mCalendar.setToNow();
        if (this.mCalendar.weekDay == 0) {
            weeday = 7;
        } else {
            weeday = this.mCalendar.weekDay;
        }
        Log.d("qs_weeday", this.mCalendar.weekDay + "");
        Log.d("qs_weeday", weeday + "");
        int[] arrayOfInt = new int[weeday];
        int i = weeday - 1;
        for (int j = 0; j <= i; j++) {
            String currentDateByOffset = getCurrentDateByOffset("yyyy-MM-dd", 5, j - i);
            ContentResolver contentResolver = resolver;
            Cursor cursor = contentResolver.query(Uri.parse(this.queryUri), new String[]{"_id", "date", "steps", "diatance", "kcal", "long_time", "targetsteps"}, "date=?", new String[]{currentDateByOffset}, null);
            if (cursorIsNull(cursor)) {
                Log.d("qs_queryDate1", arrayOfInt[j] + "");
            } else {
                cursor.moveToFirst();
                arrayOfInt[j] = cursor.getInt(cursor.getColumnIndexOrThrow("steps"));
                Log.d("qs_queryDate2", arrayOfInt[j] + "");
            }
            if (cursor != null) {
                cursor.close();
            }
            Log.d("qs_weeday", " i: " + i + " j: " + j);
        }
        arrayOfInt[i] = WatchApp.getSteps(this.mContext);
        return arrayOfInt;
    }

    public int[] queryTargetDate(ContentResolver resolver) {
        int weeday;
        this.mCalendar.setToNow();
        if (this.mCalendar.weekDay == 0) {
            weeday = 7;
        } else {
            weeday = this.mCalendar.weekDay;
        }
        int[] arrayOfInt = new int[weeday];
        int i = weeday - 1;
        for (int j = 0; j <= i; j++) {
            String currentDateByOffset = getCurrentDateByOffset("yyyy-MM-dd", 5, j - i);
            ContentResolver contentResolver = resolver;
            Cursor cursor = contentResolver.query(Uri.parse(this.queryUri), new String[]{"_id", "date", "steps", "diatance", "kcal", "long_time", "targetsteps"}, "date=?", new String[]{currentDateByOffset}, null);
            if (cursorIsNull(cursor)) {
                Log.d("qs_queryTargetDate1", "cursorIsNull");
            } else {
                cursor.moveToFirst();
                if (cursor.getInt(cursor.getColumnIndexOrThrow("targetsteps")) == 0) {
                    arrayOfInt[j] = 1000;
                } else {
                    arrayOfInt[j] = cursor.getInt(cursor.getColumnIndexOrThrow("targetsteps"));
                }
                Log.d("qs_queryTargetDate2", arrayOfInt[j] + "");
            }
            if (cursor != null) {
                cursor.close();
            }
            Log.d("qs_weeday", " i: " + i + " j: " + j);
        }
        int steps = WatchApp.getSteps(this.mContext);
        int targetSteps = WatchApp.getTargetSteps(this.mContext);
        arrayOfInt[i] = targetSteps;
        Log.d("qs_weeday", " steps: " + steps + " targetSteps: " + targetSteps + " arrayOfInt[i]" + arrayOfInt[i]);
        return arrayOfInt;
    }

    public User getUser() {
        this.user = null;
        this.user = new User(Integer.parseInt(System.getString(this.resolver, "step_user_sex")), Double.valueOf(System.getString(this.resolver, "step_user_weight")).doubleValue(), Double.valueOf(System.getString(this.resolver, "step_user_height")).doubleValue());
        return this.user;
    }

    public void showUI(boolean anim) {
        int i;
        int[] stepDate = queryStepDate(this.resolver);
        int[] stepTargetDate = queryTargetDate(this.resolver);
        int sum = 0;
        for (i = 0; i < stepDate.length; i++) {
            Log.d("qs_showUI", "stepDate" + stepDate[i] + "stepTargetDate" + stepTargetDate[i]);
            sum += stepDate[i];
        }
        String distance = CalculatorUtil.getDistance(sum, getUser());
        this.mTvTotall.setText(sum + "");
        this.mTvAverg.setText((sum / stepDate.length) + "");
        this.mTvTotallDis.setText(distance + "");
        if (stepDate != null) {
            String[] level = new String[stepDate.length];
            for (i = 0; i < stepDate.length; i++) {
                level[i] = ((int) ((Float.valueOf((float) stepDate[i]).floatValue() * 100.0f) / ((float) stepTargetDate[i]))) + "";
                Log.d("qs_showUI", level[i]);
            }
            if (this.mStepsMeterView == null) {
                return;
            }
            if (anim) {
                this.mStepsMeterView.runAnim(level);
            } else {
                this.mStepsMeterView.setData(level);
            }
        }
    }
}
