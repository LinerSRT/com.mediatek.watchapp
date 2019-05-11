package com.mediatek.watchapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

public class SportsFragment extends Fragment {
    private final String TAG = "cenontest_Log SportFragment";
    private List<Fragment> fragmentList;
    private Context mContext;
    private PageCircleIndicator mPageCircleIndicator;
    private SportsCalorieFragment mSportsCalorieFragment;
    public SportsDayFragment mSportsDayFragment;
    private SportsDistanceFragment mSportsDistanceFragment;
    private SportsStepFragment mSportsStepFragment;
    private ViewPager mViewPager;

    class SportFragmentPageAdapter extends FragmentStatePagerAdapter {
        public SportFragmentPageAdapter(FragmentManager fm) {
            super(fm);
        }

        public Fragment getItem(int position) {
            return (Fragment) SportsFragment.this.fragmentList.get(position);
        }

        public int getCount() {
            return 4;
        }

        public int getItemPosition(Object object) {
            return -2;
        }
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mContext = getActivity();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sports_fragment_layout, container, false);
        this.mViewPager = (ViewPager) view.findViewById(R.id.viewpage);
        this.fragmentList = new ArrayList();
        this.mSportsCalorieFragment = new SportsCalorieFragment();
        this.mSportsDistanceFragment = new SportsDistanceFragment();
        this.mSportsStepFragment = new SportsStepFragment();
        this.mSportsDayFragment = new SportsDayFragment();
        this.fragmentList.add(this.mSportsDayFragment);
        this.fragmentList.add(this.mSportsStepFragment);
        this.fragmentList.add(this.mSportsDistanceFragment);
        this.fragmentList.add(this.mSportsCalorieFragment);
        this.mViewPager.setAdapter(new SportFragmentPageAdapter(getChildFragmentManager()));
        this.mViewPager.setCurrentItem(0);
        this.mViewPager.setOffscreenPageLimit(4);
        this.mPageCircleIndicator = (PageCircleIndicator) view.findViewById(R.id.indicator);
        this.mPageCircleIndicator.setViewPager(this.mViewPager);
        return view;
    }
}
