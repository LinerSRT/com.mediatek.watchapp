package com.mediatek.watchapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import com.mediatek.watchapp.AppListCustomUtil.AppInfo;
import com.mediatek.watchapp.view.RoundImageView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AppListRoundFragment extends Fragment {
    private static AppListRoundFragment INSTANCE;
    List<Map<String, Object>> items = new ArrayList();
    private ArrayList<AppInfo> mApps;
    private Context mContext;
    private DirectionalViewPager mDirectionalViewPager;
    private LayoutInflater mLayoutInflater;
    private LayoutParams mlp = new LayoutParams(-2, -2);

    public class RoundAppAdapter extends PagerAdapter {
        int[] angle = new int[]{270, 225, 315, 180, 0, 135, 45, 90};
        private Context mContext;
        private List<AppInfo> mList;
        private PackageManager pm;
        ArrayList<View> views;

        public RoundAppAdapter(Context context, List<AppInfo> list, ArrayList<View> passedviews) {
            int i;
            this.mContext = context;
            this.pm = context.getPackageManager();
            this.views = new ArrayList();
            for (i = 0; i < passedviews.size(); i++) {
                this.views.add((View) passedviews.get(i));
            }
            this.mList = new ArrayList();
            for (i = 0; i < list.size(); i++) {
                this.mList.add((AppInfo) list.get(i));
            }
        }

        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        public int getCount() {
            return this.views.size();
        }

        public void destroyItem(View container, int position, Object object) {
            ((ViewPager) container).removeView((View) this.views.get(position));
        }

        public Object instantiateItem(View container, int position) {
            int index;
            View iView = (View) this.views.get(position);
            RoundImageView[] mImageViews = new RoundImageView[]{(RoundImageView) iView.findViewById(R.id.apps_icon_1), (RoundImageView) iView.findViewById(R.id.apps_icon_2), (RoundImageView) iView.findViewById(R.id.apps_icon_3), (RoundImageView) iView.findViewById(R.id.apps_icon_4), (RoundImageView) iView.findViewById(R.id.apps_icon_5), (RoundImageView) iView.findViewById(R.id.apps_icon_6), (RoundImageView) iView.findViewById(R.id.apps_icon_7), (RoundImageView) iView.findViewById(R.id.apps_icon_8)};
            CustomAnalogClock6 centerimg = (CustomAnalogClock6) iView.findViewById(R.id.watch);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(new MarginLayoutParams(200, 200));
            lp.setMargins(100, 100, 0, 0);
            centerimg.setLayoutParams(lp);
            for (index = 0; index < 8; index++) {
                lp = new RelativeLayout.LayoutParams(new MarginLayoutParams(88, 88));
                lp.setMargins((int) ((((double) 150) * (Math.cos((((double) this.angle[index]) * 3.141592653589793d) / 180.0d) + 1.0d)) + ((double) 6)), (int) ((((double) 150) * (Math.sin((((double) this.angle[index]) * 3.141592653589793d) / 180.0d) + 1.0d)) + ((double) 6)), 0, 0);
                mImageViews[index].setLayoutParams(lp);
            }
            for (index = 0; index < 8; index++) {
                int appindex = (position * 8) + index;
                if (appindex < this.mList.size()) {
                    setAppIconOnClick(mImageViews[index], (AppInfo) this.mList.get(appindex));
                } else {
                    setAppIconOnClick(mImageViews[index], null);
                }
            }
            ((ViewPager) container).addView(iView);
            return iView;
        }

        private void setAppIconOnClick(ImageView app, final AppInfo appInfo) {
            if (appInfo != null) {
                app.setImageDrawable(appInfo.icon);
                app.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        ComponentName componentName = new ComponentName(appInfo.pkg, appInfo.cls);
                        Intent intent = new Intent("android.intent.action.MAIN");
                        intent.addCategory("android.intent.category.LAUNCHER");
                        intent.setComponent(componentName);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //270532608
                        RoundAppAdapter.this.mContext.startActivity(intent);
                    }
                });
                return;
            }
            app.setVisibility(View.GONE);
        }
    }

    public AppListRoundFragment() {
        INSTANCE = this;
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        WatchApp.setMainMenuStatus(isVisibleToUser);
        WatchApp.setIsCanSlide(false);
        SystemProperties.set("persist.sys.clock.idle", String.valueOf(false));
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.applistround, container, false);
        Log.i("guoxiaolong11", "onCreateView:rootView=" + rootView);
        this.mContext = rootView.getContext();
        this.mLayoutInflater = inflater;
        this.mDirectionalViewPager = (DirectionalViewPager) rootView.findViewById(R.id.pager);
        Log.i("guoxiaolong11", "onCreateView:mDirectionalViewPager=" + this.mDirectionalViewPager);
        initViews();
        return rootView;
    }

    public void initViews() {
        this.mApps = AppListCustomUtil.getAppList(getActivity());
        Log.i("guoxiaolong", "mContext=" + this.mApps);
        Log.d("AppListRoundFragment", " initViews apps counts is: " + this.mApps.size());
        int PageCount = (int) Math.ceil((double) (((float) this.mApps.size()) / 8.0f));
        ArrayList<View> views = new ArrayList();
        for (int pageindex = 0; pageindex < PageCount; pageindex++) {
            views.add(this.mLayoutInflater.inflate(R.layout.custom_view, null));
        }
        this.mDirectionalViewPager.setAdapter(new RoundAppAdapter(this.mContext, this.mApps, views));
        Log.d("guoxiaolong11", "initViews():mDirectionalViewPager=" + this.mDirectionalViewPager);
        this.mDirectionalViewPager.setOrientation(1);
    }

    public void onResume() {
        super.onResume();
    }

    public void refreshApplist() {
        Log.d("guoxiaolong11", "refreshApplist():mDirectionalViewPager=" + this.mDirectionalViewPager);
        if (this.mDirectionalViewPager != null) {
            initViews();
            this.mDirectionalViewPager.invalidate();
        }
    }

    public void onDestroy() {
        INSTANCE = null;
        super.onDestroy();
    }
}
