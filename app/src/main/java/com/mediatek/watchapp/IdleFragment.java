package com.mediatek.watchapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.service.wallpaper.IWallpaperConnection.Stub;
import android.service.wallpaper.IWallpaperEngine;
import android.service.wallpaper.IWallpaperService;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.mediatek.watchapp.ClockUtil.ClockSet;
import com.mediatek.watchapp.ClockUtil.ClockSetWallpaper;
import com.mediatek.watchapp.WatchApp.installedClock;
import java.util.ArrayList;

@SuppressLint("ValidFragment")
public class IdleFragment extends Fragment {
    private static final String TAG = IdleFragment.class.getSimpleName();
    private static FrameLayout mClockHost = null;
    private static int mIndex = 0;
    private static int mStyle = 0;
    public final ClockFragment mClockFragment = ClockFragment.getInstance();
    private Fragment mCurAppListFragment;
    private MyAnalogClock mMyclock = null;
    private final NotificationFragment mNotificationFragment = NotificationFragment.getInstance();
    private HorizontalViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private View top_black;

    /* renamed from: com.mediatek.watchapp.IdleFragment$1 */
    class C01131 extends SimpleOnPageChangeListener {
        C01131() {
        }

        public void onPageSelected(int position) {
        }
    }

    /* renamed from: com.mediatek.watchapp.IdleFragment$2 */
    class C01142 implements OnPageChangeListener {
        C01142() {
        }

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            Log.d("qs", "position" + position);
            Log.d("qs", "positionOffset" + positionOffset);
            Log.d("qs", "positionOffsetPixels" + positionOffsetPixels);
            if (position == 1) {
                IdleFragment.this.top_black.setAlpha(positionOffset);
            } else if (position == 0) {
                IdleFragment.this.top_black.setAlpha(1.0f - positionOffset);
            }
        }

        public void onPageSelected(int position) {
            Log.d("qs", "position2---" + position);
        }

        public void onPageScrollStateChanged(int state) {
            Log.d("qs", "state" + state);
        }
    }

    public static class ClockFragment extends Fragment implements OnTouchListener {
        private static ClockFragment INSTANCE;
        private String TAG = "cenontest_Log ClockFragment";
        View clockview = null;
        private FrameLayout mClockHost = null;
        private FrameLayout mClockHost2 = null;
        private View mClockView = null;
        private Dialog mDialog;
        private boolean mPauseState = false;
        private WallpaperConnection mWallpaperConnection;

        /* renamed from: com.mediatek.watchapp.IdleFragment$ClockFragment$1 */
        class C01151 implements OnLongClickListener {
            C01151() {
            }

            public boolean onLongClick(View v) {
                if (ClockFragment.this.mDialog != null) {
                    ClockFragment.this.mDialog.dismiss();
                }
                if (ClockFragment.this.mWallpaperConnection != null && ClockFragment.this.mWallpaperConnection.mConnected) {
                    ClockFragment.this.mWallpaperConnection.disconnect();
                }
                ClockFragment.this.mWallpaperConnection = null;
                ClockFragment.this.getChooseWatchView();
                return true;
            }
        }

        /* renamed from: com.mediatek.watchapp.IdleFragment$ClockFragment$2 */
        class C01162 implements Runnable {
            C01162() {
            }

            public void run() {
                if (ClockFragment.this.mWallpaperConnection != null && !ClockFragment.this.mWallpaperConnection.connect()) {
                    ClockFragment.this.mWallpaperConnection = null;
                }
            }
        }

        class WallpaperConnection extends Stub implements ServiceConnection {
            boolean mConnected;
            IWallpaperEngine mEngine;
            final Intent mIntent;
            IWallpaperService mService;

            WallpaperConnection(Intent intent) {
                this.mIntent = intent;
            }

            public boolean connect() {
                synchronized (this) {
                    if (ClockFragment.this.getActivity().bindService(this.mIntent, this, Context.BIND_AUTO_CREATE)) {
                        this.mConnected = true;
                        Log.d("xiaocai_clockFragment", "WallpaperConnection connect");
                        return true;
                    }
                    return false;
                }
            }

            public void disconnect() {
                synchronized (this) {
                    this.mConnected = false;
                    if (this.mEngine != null) {
                        try {
                            this.mEngine.destroy();
                        } catch (RemoteException e) {
                        }
                        this.mEngine = null;
                    }
                    ClockFragment.this.getActivity().unbindService(this);
                    this.mService = null;
                }
            }

            public void onServiceConnected(ComponentName name, IBinder service) {
                if (ClockFragment.this.mWallpaperConnection == this) {
                    this.mService = IWallpaperService.Stub.asInterface(service);
                    try {
                        View view = ClockFragment.this.mClockHost;
                        View root = view.getRootView();
                        this.mService.attach(this, view.getWindowToken(), 1004, true, root.getWidth(), root.getHeight(), new Rect(0, 0, 0, 0));
                    } catch (RemoteException e) {
                        Log.d("xiaocai_clockFragment", "Failed attaching wallpaper; clearing");
                    }
                }
            }

            public void onServiceDisconnected(ComponentName name) {
                this.mService = null;
                this.mEngine = null;
                if (ClockFragment.this.mWallpaperConnection == this) {
                    Log.d("xiaocai_clockFragment", "Wallpaper service gone: " + name);
                }
            }

            public void attachEngine(IWallpaperEngine engine) {
                synchronized (this) {
                    if (this.mConnected) {
                        this.mEngine = engine;
                        try {
                            engine.setVisibility(true);
                        } catch (RemoteException e) {
                        }
                    } else {
                        try {
                            engine.destroy();
                        } catch (RemoteException e2) {
                        }
                    }
                }
            }

            public ParcelFileDescriptor setWallpaper(String name) {
                return null;
            }

            public void engineShown(IWallpaperEngine engine) throws RemoteException {
            }
        }

        public static ClockFragment getInstance() {
            if (INSTANCE == null) {
                INSTANCE = new ClockFragment();
            }
            return INSTANCE;
        }

        public ClockFragment() {
            INSTANCE = this;
        }

        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            WatchApp.setIsCanSlide(true);
            WatchApp.setClockViewStatus(isVisibleToUser);
            SystemProperties.set("persist.sys.clock.idle", String.valueOf(true));
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.idle_fragment_layout_new, container, false);
            this.mClockHost = (FrameLayout) view.findViewById(R.id.idle_clock);
            this.mClockHost2 = (FrameLayout) getActivity().findViewById(R.id.bottom_clock);
            this.mClockHost.setOnLongClickListener(new C01151());
            this.mClockHost.setOnTouchListener(this);
            IdleFragment.mIndex = WatchApp.getClockIndex(getActivity());
            setClockViewVisibile(IdleFragment.mIndex);
            return view;
        }

        public void setWallpaperPreview(int index) {
            ClockSet clockSetWallpaper = ClockUtil.mClockList[index];
            if (clockSetWallpaper instanceof ClockSetWallpaper) {
                Log.d("xiaocai_clockFragment", "setWallpaperPreview");
                ClockSetWallpaper wallpaperClock = (ClockSetWallpaper) clockSetWallpaper;
                showLoadingPic(wallpaperClock.mLoadingPic);
                Intent mWallpaperIntent = new Intent("android.service.wallpaper.WallpaperService");
                mWallpaperIntent.setComponent(new ComponentName(wallpaperClock.PackageName, wallpaperClock.ServiceName));
                Log.d("qs_setWallpaperPreview", wallpaperClock.PackageName);
                Log.d("qs_setWallpaperPreview", wallpaperClock.ServiceName);
                this.mWallpaperConnection = new WallpaperConnection(mWallpaperIntent);
                if (this.mClockHost2 != null) {
                    this.mClockHost2.post(new C01162());
                    return;
                }
                return;
            }
            Log.d("qs_else", index + "");
            if (this.mWallpaperConnection != null && this.mWallpaperConnection.mConnected) {
                this.mWallpaperConnection.disconnect();
            }
            this.mWallpaperConnection = null;
        }

        public void getChooseWatchView() {
            ((MainActivity) getActivity()).getChooseWatchView(WatchApp.getClockIndex(getActivity()));
        }

        public void onCreate(Bundle savedInstanceState) {
            Log.d("xiaocai_clockFragment", "ClockFragment_onCreate");
            super.onCreate(savedInstanceState);
        }

        public void onActivityCreated(Bundle savedInstanceState) {
            Log.d("xiaocai_clockFragment", "ClockFragment_onActivityCreated");
            super.onActivityCreated(savedInstanceState);
        }

        public void onStart() {
            super.onStart();
            Log.d("xiaocai_clockFragment", "ClockFragment_onStart");
        }

        public void onDetach() {
            Log.d("xiaocai_clockFragment", "ClockFragment_onDetach");
            super.onDetach();
            if (this.mWallpaperConnection != null && this.mWallpaperConnection.mConnected) {
                this.mWallpaperConnection.disconnect();
            }
            this.mWallpaperConnection = null;
        }

        public void onResume() {
            Log.d("xiaocai_clockFragment", "ClockFragment_onResume");
            super.onResume();
            this.mPauseState = false;
            if (IdleFragment.mIndex < ClockUtil.mClockList.length && (ClockUtil.mClockList[IdleFragment.mIndex] instanceof ClockSetWallpaper)) {
                if (this.mWallpaperConnection == null) {
                    setWallpaperPreview(IdleFragment.mIndex);
                } else if (this.mWallpaperConnection != null && this.mWallpaperConnection.mEngine != null) {
                    try {
                        this.mWallpaperConnection.mEngine.setVisibility(true);
                    } catch (RemoteException e) {
                    }
                }
            }
        }

        public void onPause() {
            Log.d("xiaocai_clockFragment", "ClockFragment_onPause");
            super.onPause();
            this.mPauseState = true;
            if (this.mWallpaperConnection != null && this.mWallpaperConnection.mEngine != null) {
                try {
                    this.mWallpaperConnection.mEngine.setVisibility(false);
                } catch (RemoteException e) {
                }
            }
        }

        public void onStop() {
            Log.d("xiaocai_clockFragment", "ClockFragment_onStop");
            super.onStop();
        }

        public void onDestroyView() {
            Log.d("xiaocai_clockFragment", "ClockFragment_onDestroyView");
            super.onDestroyView();
        }

        public void onDestroy() {
            Log.d("xiaocai_clockFragment", "ClockFragment_onDestroy");
            super.onDestroy();
        }

        @SuppressLint("ResourceType")
        private void showLoadingPic(int idLoadingPic) {
            TextView content = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.live_wallpaper_loading, null);
            content.setBackgroundResource(idLoadingPic);
            this.mDialog = new Dialog(getActivity(), 16973832);
            this.mDialog.requestWindowFeature(1);
            Window window = this.mDialog.getWindow();
            LayoutParams lp = window.getAttributes();
            lp.width = -1;
            lp.height = -1;
            window.setType(1001);
            this.mDialog.setContentView(content, new ViewGroup.LayoutParams(-1, -1));
            this.mDialog.show();
        }

        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == 1 && this.mClockView != null) {
                float x = event.getX();
                float y = event.getY();
                if (this.mClockView instanceof RelativeLayout) {
                    View cv = this.mClockView.findViewById(R.id.watchtable);
                    if (cv != null && (cv instanceof WiiteWatchFace)) {
                        ((WiiteWatchFace) cv).onTouch(x, y);
                    }
                } else if (this.mClockView instanceof WiiteWatchFace) {
                    ((WiiteWatchFace) this.mClockView).onTouch(x, y);
                }
            }
            return false;
        }

        public void setClockViewVisibile(int index) {
            try {
                ArrayList<installedClock> installedClocks = WatchApp.getInstalledClocks();
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-1, -1);
                if (this.mClockHost2 != null) {
                    if (index < ClockUtil.mClockList.length) {
                        this.mClockView = inflater.inflate(ClockUtil.mClockList[index].mViewId, null);
                    } else {
                        String clockSkinFullPath = ((installedClock) installedClocks.get(index - ClockUtil.mClockList.length)).filePath;
                        if (clockSkinFullPath == null) {
                            this.mClockView = new MyAnalogClock3(getActivity(), null);
                        } else {
                            Log.d("xiaocai", "clockSkinFullPath:" + clockSkinFullPath + " path:" + clockSkinFullPath.substring(0, clockSkinFullPath.lastIndexOf("/")));
                            this.mClockView = new MyAnalogClock3(getActivity(), clockSkinFullPath);
                        }
                    }
                    this.mClockHost2.removeAllViews();
                    this.mClockHost2.addView(this.mClockView, params);
                }
            } catch (Exception e) {
                WatchApp.setClockIndex(getActivity(), 0);
                throw new NullPointerException();
            }
        }

        public void ChangeClockApply(int index) {
            IdleFragment.mIndex = index;
            WatchApp.setClockIndex(getActivity(), index);
            setClockViewVisibile(IdleFragment.mIndex);
        }

        public void RelaodClockFace() {
            if (this.mClockHost != null) {
                setClockViewVisibile(IdleFragment.mIndex);
            }
        }
    }

    private class IdlePagerAdapter extends FragmentStatePagerAdapter {
        private int mChildCount = 3;

        public IdlePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public Fragment getItem(int position) {
            if (position == 1) {
                return IdleFragment.this.mClockFragment;
            }
            if (position == 0) {
                return IdleFragment.this.mNotificationFragment;
            }
            if (position == 2) {
                return IdleFragment.this.mCurAppListFragment;
            }
            if (position == 3) {
                // return IdleFragment.this.mFitnessFragment;
            }
            return IdleFragment.this.mClockFragment;
        }

        public int getCount() {
            return 3;
        }

        public void notifyDataSetChanged() {
            this.mChildCount = getCount();
            super.notifyDataSetChanged();
        }

        public int getItemPosition(Object object) {
            if (this.mChildCount <= 0) {
                return super.getItemPosition(object);
            }
            this.mChildCount--;
            return -2;
        }
    }

    @SuppressLint("ValidFragment")
    public IdleFragment(int style) {
        setAppListStyle(style);
    }

    public void setAppListStyle(int style) {
        mStyle = style;
        if (style == 0) {
            this.mCurAppListFragment = new AppArcListFragment();
        } else if (1 == style) {
            this.mCurAppListFragment = new AppListRoundFragment();
        } else {
            this.mCurAppListFragment = new AppListRoundFragment();
        }
    }

    public void refreshApplist() {
        if (this.mCurAppListFragment != null) {
            if (this.mCurAppListFragment instanceof AppListFragment) {
                ((AppListFragment) this.mCurAppListFragment).refreshApplist();
            } else if (this.mCurAppListFragment instanceof AppMatrixFragment) {
                ((AppMatrixFragment) this.mCurAppListFragment).refreshApplist();
            } else if (this.mCurAppListFragment instanceof AppListRoundFragment) {
                ((AppListRoundFragment) this.mCurAppListFragment).refreshApplist();
            } else if (this.mCurAppListFragment instanceof AppArcListFragment) {
                ((AppArcListFragment) this.mCurAppListFragment).refreshApplist();
            }
        }
        //if (this.mFitnessFragment != null) {
        //    this.mFitnessFragment.refreshApplist();
        //}
    }

    public int getAppListStyle() {
        return mStyle;
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        WatchApp.setClockViewStatus(isVisibleToUser);
        SystemProperties.set("persist.sys.clock.idle", String.valueOf(true));
    }

    public void backToClock() {
        if (this.mPager != null && this.mPager.getCurrentItem() != 1) {
            this.mPager.setCurrentItem(1, false);
        }
    }

    public void backToNotification() {
        if (this.mPager != null && this.mPager.getCurrentItem() != 0) {
            this.mPager.setCurrentItem(0, false);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.idle_fragment_layout, container, false);
        this.top_black = getActivity().findViewById(R.id.top_black);
        this.mPager = (HorizontalViewPager) view.findViewById(R.id.pager);
        this.mPagerAdapter = new IdlePagerAdapter(getChildFragmentManager());
        this.mPager.setAdapter(this.mPagerAdapter);
        this.mPager.setOffscreenPageLimit(3);
        this.mPager.setOnPageChangeListener(new C01131());
        this.mPager.setOnPageChangeListener(new C01142());
        backToClock();
        return view;
    }

    public void update() {
        this.mPagerAdapter.notifyDataSetChanged();
    }
}
