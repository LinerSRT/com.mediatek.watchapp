package com.mediatek.watchapp;

public class ClockUtil {
    public static final ClockSet[] mClockList = new ClockSet[0];

    public static class ClockSet {
        public int mThumbImageId;
        public int mTitleId;
        public int mViewId;
    }

    public static class ClockSetWallpaper extends ClockSet {
        public String PackageName;
        public String ServiceName;
        public int mLoadingPic;
    }
}
