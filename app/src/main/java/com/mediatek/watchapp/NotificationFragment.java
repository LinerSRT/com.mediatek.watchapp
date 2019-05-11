package com.mediatek.watchapp;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.INotificationManager;
import android.app.INotificationManager.Stub;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.provider.Settings.System;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.PageTransformer;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mediatek.watchapp.NotificationData.Entry;

public class NotificationFragment extends Fragment {
    private static final String TAG = NotificationFragment.class.getSimpleName();
    private static NotificationFragment sInstance;
    private Context mContext;
    private NotificationListenerService mListener = new C01361();
    private View mNoItems;
    private INotificationManager mNoMan;
    private int mPageCount = 0;
    private VerticalViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private PowerManager mPowerManager;
    private LayoutTransition mRealLayoutTransition;
    private final BroadcastReceiver mReceiver = new C01372();
    private Vibrator mVibrator;

    /* renamed from: com.mediatek.watchapp.NotificationFragment$1 */
    class C01361 extends NotificationListenerService {
        C01361() {
        }

        public void onNotificationPosted(final StatusBarNotification mSbn) {
            NotificationFragment.this.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    Log.v(NotificationFragment.TAG, "onNotificationPosted: " + mSbn);
                    String pkg = mSbn.getPackageName();
                    String tag = mSbn.getTag();
                    if (tag == null) {
                        tag = "";
                    }
                    if ((!pkg.equals("com.mediatek.wearable") && !pkg.equals("com.weitetech.smartconnect.wiiwearsdk") && !pkg.equals("com.weite.smartconnect.wiiwearsdk")) || !tag.contains("com.mediatek.swp")) {
                        if (NotificationHelper.filterNotification(mSbn.getNotification())) {
                            Log.i(NotificationFragment.TAG, "**********  onNotificationPosted, match filter");
                            NotificationHelper.dumpNotification(mSbn.getNotification());
                        } else {
                            Log.i(NotificationFragment.TAG, "**********  onNotificationPosted");
                            if (NotificationHelper.getTitle(NotificationCompat.getExtras(mSbn.getNotification())) != null) {
                                NotificationFragment.this.addNotification(mSbn);
                            }
                        }
                    }
                }
            });
        }

        public void onNotificationRemoved(final StatusBarNotification mSbn) {
            NotificationFragment.this.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    Log.v(NotificationFragment.TAG, "********** onNotificationRemoved: " + mSbn);
                    NotificationFragment.this.removeNotification(mSbn);
                }
            });
        }
    }

    /* renamed from: com.mediatek.watchapp.NotificationFragment$2 */
    class C01372 extends BroadcastReceiver {
        C01372() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null && "com.mediatek.watchapp.NOTIFICATION_LISTENER.CANCEL".equals(intent.getAction())) {
                Log.i(NotificationFragment.TAG, "mReceiver::ACTION_NOTIFICATION_CANCEL_BY_USER");
                NotificationFragment.this.removeNotification(intent.getExtras());
            } else if ((intent == null || !"com.mediatek.watchapp.DEMO_MODE_POST".equals(intent.getAction())) && intent.getAction().equals("android.intent.action.LOCALE_CHANGED") && NotificationFragment.this.mNoItems != null) {
                TextView textview = (TextView) NotificationFragment.this.mNoItems.findViewById(R.id.no_items_textview);
                if (textview != null) {
                    textview.setText(R.string.notification_no_items);
                }
            }
        }
    }

    /* renamed from: com.mediatek.watchapp.NotificationFragment$3 */
    class C01383 implements PageTransformer {
        C01383() {
        }

        public void transformPage(View page, float position) {
            page.setAlpha(1.0f - Math.abs(position));
        }
    }

    /* renamed from: com.mediatek.watchapp.NotificationFragment$4 */
    class C01394 extends SimpleOnPageChangeListener {
        C01394() {
        }

        public void onPageSelected(int mPosition) {
            NotificationSubFragment mFragPrev = NotificationFragment.this.getNotificationSubFragment(mPosition - 1);
            if (mFragPrev != null) {
                mFragPrev.collapseLayout();
            }
            NotificationSubFragment mFragNext = NotificationFragment.this.getNotificationSubFragment(mPosition + 1);
            if (mFragNext != null) {
                mFragNext.collapseLayout();
            }
        }
    }

    private class NotificationPagerAdapter extends FragmentStatePagerAdapter {
        public NotificationPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public Fragment getItem(int position) {
            return NotificationFragment.this.getNotificationSubFragment(position);
        }

        public int getCount() {
            NotificationFragment.logd("NotificationPagerAdapter size() = " + NotificationFragment.this.mPageCount, new Object[0]);
            return NotificationFragment.this.mPageCount;
        }

        public int getItemPosition(Object object) {
            return -2;
        }
    }

    public NotificationFragment() {
        sInstance = this;
    }

    public static NotificationFragment getInstance() {
        if (sInstance == null) {
            sInstance = new NotificationFragment();
        }
        return sInstance;
    }

    public void onCreate(Bundle icicle) {
        logd("onCreate(%s)", icicle);
        super.onCreate(icicle);
        this.mContext = getActivity();
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mVibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        this.mNoMan = Stub.asInterface(ServiceManager.getService("notification"));
        try {
            this.mListener.registerAsSystemService(this.mContext, new ComponentName(this.mContext.getPackageName(), getClass().getCanonicalName()), -1);
        } catch (RemoteException e) {
            Log.e(TAG, "Cannot register listener", e);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.mediatek.watchapp.NOTIFICATION_LISTENER.CANCEL");
        filter.addAction("android.intent.action.LOCALE_CHANGED");
        getActivity().registerReceiver(this.mReceiver, filter);
    }

    public void onDestroyView() {
        super.onDestroyView();
    }

    public void onAttach(Activity activity) {
        logd("onAttach(%s)", activity.getClass().getSimpleName());
        super.onAttach(activity);
    }

    private void notifyDataChangedPosted(StatusBarNotification mSbn) {
        this.mContext.sendBroadcast(new Intent("com.mediatek.watchapp.NOTIFICATION_LISTENER.POSTED"));
    }

    private void notifyDataChangedRemoved(StatusBarNotification mSbn) {
        this.mContext.sendBroadcast(new Intent("com.mediatek.watchapp.NOTIFICATION_LISTENER.REMOVED"));
    }

    private static void logd(String msg, Object... args) {
    }

    private void refreshPager() {
        boolean z = false;
        if (this.mPager != null) {
            this.mPageCount = NotificationHelper.getNotificationData().size();
            logd("NotificationPagerAdapter mPageCount, size() = " + this.mPageCount, new Object[0]);
            this.mPager.getAdapter().notifyDataSetChanged();
            if (this.mPageCount == 0) {
                z = true;
            }
            setNoItemsVisibility(z);
        }
    }

    private boolean getNotificationComingState() {
        int mode = 0;
        try {
            mode = this.mNoMan.getZenMode();
        } catch (RemoteException e) {
        }
        return mode != 3;
    }

    private void addNotification(StatusBarNotification mSbn) {
        String mPkgName;
        String mTag;
        int pos = NotificationHelper.findPositionByKey(mSbn.getPackageName(), mSbn.getTag(), mSbn.getId());
        if (NotificationHelper.isHighPriorityNotification(mSbn.getNotification())) {
            mPkgName = mSbn.getPackageName();
            mTag = mSbn.getTag();
        } else {
            mPkgName = mSbn.getPackageName();
            mTag = mSbn.getTag();
        }
        if (mTag == null) {
            mTag = "";
        }
        boolean isNeedNoteAction = getNotificationComingState();
        Log.v("caojinliang", "isNeedNoteAction=" + isNeedNoteAction);
        MainActivity mMain = (MainActivity) getActivity();
        if (isNeedNoteAction) {
            mMain.backToNoteView(false);
        } else {
            mMain.backToNoteView(false);
        }
        boolean isVibrate = true;
        boolean vibrateWhenSMS = System.getInt(this.mContext.getContentResolver(), "vibrate_when_get_sms", 1) == 1;
        if (mPkgName.equals("com.android.mms") && !vibrateWhenSMS) {
            isVibrate = false;
        }
        if (mPkgName.equals("com.android.systemui") && mTag.equals("low_battery")) {
            isVibrate = false;
        }
        if (!isNeedNoteAction) {
            isVibrate = false;
        }
        if (!NotificationHelper.isDefaultVibrate(mSbn.getNotification()) && NotificationHelper.getVibrate(mSbn.getNotification()) == null && isVibrate) {
            this.mVibrator.vibrate(80);
            Log.v("caojinliang", "mVibrator.vibrate");
        }
        if (pos == -1) {
            Log.d(TAG, "addNotification: added at " + NotificationHelper.add(new Entry(mSbn, NotificationSubFragment.create(mSbn))));
            refreshPager();
            this.mPager.setCurrentItem(0);
        } else {
            Log.d(TAG, "addNotification: refresh at " + pos + " for" + mSbn);
            NotificationHelper.update(NotificationHelper.getNotificationData().get(pos));
            NotificationSubFragment subFragment = getNotificationSubFragment(pos);
            if (subFragment != null) {
                subFragment.setContent(mSbn);
                subFragment.refreshContent();
            }
        }
        notifyDataChangedPosted(mSbn);
    }

    private void removeNotification(StatusBarNotification mSbn) {
        String mPkgName = mSbn.getPackageName();
        String mTag = mSbn.getTag();
        int mId = mSbn.getId();
        Log.d(TAG, "removeNotification packageName=" + mPkgName + " mTag=" + mTag + " id=" + mId);
        if (NotificationHelper.remove(mPkgName, mTag, mId) != null) {
            refreshPager();
            notifyDataChangedRemoved(mSbn);
        }
    }

    private void setNoItemsVisibility(boolean mIsNoItems) {
        if (this.mNoItems != null) {
            this.mNoItems.setVisibility(mIsNoItems ? 0 : 4);
        }
    }

    private NotificationSubFragment getNotificationSubFragment(int mPos) {
        if (mPos >= 0 && mPos < NotificationHelper.getNotificationData().size()) {
            Entry mEntry = NotificationHelper.getNotificationData().get(mPos);
            if (mEntry != null) {
                return mEntry.mNotificationSubFragment;
            }
        }
        return null;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notification_fragment_layout, container, false);
        this.mPageCount = NotificationHelper.getNotificationData().size();
        logd("NotificationPagerAdapter mPageCount, size() = " + this.mPageCount, new Object[0]);
        this.mPager = (VerticalViewPager) view.findViewById(R.id.vpager);
        this.mNoItems = view.findViewById(R.id.notification_no_items);
        setNoItemsVisibility(this.mPageCount == 0);
        this.mPagerAdapter = new NotificationPagerAdapter(getChildFragmentManager());
        this.mPager.setAdapter(this.mPagerAdapter);
        this.mPager.setClipToPadding(false);
        this.mPager.setPageMargin(10);
        this.mPager.setCurrentItem(0);
        this.mPager.setOffscreenPageLimit(2);
        this.mPager.setPageTransformer(false, new C01383());
        this.mPager.setOnPageChangeListener(new C01394());
        this.mRealLayoutTransition = new LayoutTransition();
        this.mRealLayoutTransition.setAnimateParentHierarchy(true);
        return view;
    }

    public void onDestroy() {
        logd("onDestroy()", new Object[0]);
        sInstance = null;
        getActivity().unregisterReceiver(this.mReceiver);
        super.onDestroy();
    }

    public void onResume() {
        logd("onResume()", new Object[0]);
        super.onResume();
        this.mPager.setLayoutTransition(null);
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        WatchApp.setMainMenuStatus(isVisibleToUser);
        WatchApp.setIsCanSlide(false);
        SystemProperties.set("persist.sys.clock.idle", String.valueOf(false));
    }

    public int getPageCount() {
        return this.mPageCount;
    }

    private void removeNotification(Bundle mArgs) {
        String mKey = mArgs.getString("key");
        Log.d(TAG, "removeNotification Key=" + mKey);
        try {
            this.mListener.cancelNotification(mKey);
        } catch (NullPointerException e) {
            Log.e(TAG, "removeNotification NullPointerException.");
        }
    }
}
