package com.mediatek.watchapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import com.mediatek.watchapp.WatchApp.installedClock;
import com.mediatek.watchapp.online.ClockSkinOnlineActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.ImageLoader;
import java.util.ArrayList;

public class ChooseClockActivity extends Activity {
    private final String INDEX = "index";
    private final int RESULTSCODE = 50;
    String TAG = "ChooseClockActivity";
    public ImageAdapter imageAdapter;
    public Handler mHandler = new C01022();
    private int mIndex;
    private final BroadcastReceiver mUpdateClockReceiver = new C01011();
    private Gallery myGallery;

    /* renamed from: com.mediatek.watchapp.ChooseClockActivity$1 */
    class C01011 extends BroadcastReceiver {
        C01011() {
        }

        public void onReceive(Context context, Intent intent) {
            String str = intent.getStringExtra("action_str");
            if (str.equals("installclock")) {
                ChooseClockActivity.this.mHandler.sendEmptyMessage(0);
            } else if (str.equals("deleteclock")) {
                ChooseClockActivity.this.mHandler.sendEmptyMessage(1);
            } else if (str.equals("cleanrelaodclock")) {
                ChooseClockActivity.this.mHandler.sendEmptyMessage(2);
            }
        }
    }

    /* renamed from: com.mediatek.watchapp.ChooseClockActivity$2 */
    class C01022 extends Handler {
        C01022() {
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    ChooseClockActivity.this.mIndex = WatchApp.getAllClockCount() - 1;
                    ChooseClockActivity.this.initUI();
                    ChooseClockActivity.this.scrollToView();
                    return;
                case 1:
                    int index;
                    int allClockCnt = WatchApp.getAllClockCount();
                    int selectedPos = ChooseClockActivity.this.myGallery.getSelectedItemPosition();
                    if (selectedPos >= allClockCnt) {
                        index = allClockCnt - 1;
                    } else {
                        index = selectedPos;
                    }
                    ChooseClockActivity.this.mIndex = WatchApp.getClockIndex(ChooseClockActivity.this);
                    ChooseClockActivity.this.initUI();
                    ChooseClockActivity.this.scrollToView(index);
                    return;
                case 2:
                    ChooseClockActivity.this.mIndex = WatchApp.getClockIndex(ChooseClockActivity.this);
                    ChooseClockActivity.this.initUI();
                    ChooseClockActivity.this.scrollToView(ChooseClockActivity.this.mIndex);
                    return;
                default:
                    return;
            }
        }
    }

    private class ImageAdapter extends BaseAdapter {
        private int[] ids;
        private LayoutInflater inflater;
        private Context mContext;
        private String[] mFilePath;
        private int mIndex;
        private String[] mPkgNames;
        private String[] mTitleIds;
        private DisplayImageOptions options = new Builder().showImageOnLoading(R.drawable.loadding_backround).showImageForEmptyUri(R.drawable.loadding_backround).showImageOnFail(R.drawable.loadding_backround).cacheInMemory(false).cacheOnDisk(true).bitmapConfig(Config.RGB_565).build();

        /* renamed from: com.mediatek.watchapp.ChooseClockActivity$ImageAdapter$2 */
        class C01052 implements OnClickListener {
            C01052() {
            }

            public void onClick(View v) {
                Intent it = new Intent("com.update.installclock");
                it.putExtra("action_str", "cleanrelaodclock");
                ImageAdapter.this.mContext.sendBroadcast(it);
            }
        }

        private class ViewHolder {
            private ImageView mImage;
            private TextView mTitle;

            private ViewHolder() {
            }
        }

        public ImageAdapter(Context context, String[] titleids, int[] ids, String[] filepath, int index, String[] pkgname) {
            this.inflater = LayoutInflater.from(context);
            this.ids = ids;
            this.mFilePath = filepath;
            this.mIndex = index;
            this.mContext = context;
            this.mTitleIds = titleids;
            this.mPkgNames = pkgname;
        }

        public void setImageAdapter(Context context, String[] titleids, int[] ids, String[] filepath, int index, String[] pkgname) {
            this.ids = ids;
            this.mFilePath = filepath;
            this.mIndex = index;
            this.mContext = context;
            this.mTitleIds = titleids;
            this.mPkgNames = pkgname;
        }

        public int getCount() {
            return this.ids.length;
        }

        public Object getItem(int position) {
            return Integer.valueOf(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            String url;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = this.inflater.inflate(R.layout.horizontal_list_item, null);
                holder.mImage = (ImageView) convertView.findViewById(R.id.img_list_item);
                holder.mTitle = (TextView) convertView.findViewById(R.id.text_list_item);
                Button deleteButton = (Button) convertView.findViewById(R.id.delete_clock);
                int pos = position;
                if (this.mTitleIds[position].equals("downloadclock")) {
                    deleteButton.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            Intent it = new Intent("com.update.installclock");
                            it.putExtra("action_str", "deleteclock");
                            it.putExtra("PACKAG_NAME", ImageAdapter.this.mPkgNames[position]);
                            it.putExtra("INDEX", position);
                            ImageAdapter.this.mContext.sendBroadcast(it);
                        }
                    });
                    if (position == this.mFilePath.length - 1 || this.mIndex == position) {
                        deleteButton.setVisibility(View.GONE);
                    } else {
                        deleteButton.setVisibility(View.VISIBLE);
                    }
                } else if (this.mTitleIds[position].equals("add")) {
                    deleteButton.setBackgroundResource(R.drawable.refresh_list);
                    deleteButton.setOnClickListener(new C01052());
                } else {
                    deleteButton.setVisibility(View.GONE);
                }
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (this.mFilePath[position] != null) {
                url = "file://" + this.mFilePath[position];
            } else {
                url = "drawable://" + this.ids[position];
            }
            ImageLoader.getInstance().displayImage(url, holder.mImage, this.options);
            return convertView;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.addActivity(this);
        Log.d(this.TAG, "onCreate");
        setContentView(R.layout.activity_choose_clock);
        this.myGallery = (Gallery) findViewById(R.id.myGallery);
        this.mIndex = 0;
        if (getIntent() != null) {
            this.mIndex = getIntent().getIntExtra("index", 0);
        }
        initUI();
        scrollToView();
        registerReceiver(this.mUpdateClockReceiver, new IntentFilter("com.update.installclock.done"));
    }

    protected void onResume() {
        Log.d(this.TAG, "onResume");
        super.onResume();
    }

    public void onDestroy() {
        Log.d(this.TAG, "onDestroy");
        super.onDestroy();
        Util.removeActivity(this);
        unregisterReceiver(this.mUpdateClockReceiver);
    }

    protected void onNewIntent(Intent intent) {
        Log.d(this.TAG, "onNewIntent");
        super.onNewIntent(intent);
    }

    private void scrollToView() {
        this.myGallery.setSelection(this.mIndex);
    }

    private void scrollToView(int index) {
        this.myGallery.setSelection(index);
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        Log.d(this.TAG, "onWindowFocusChanged");
        super.onWindowFocusChanged(hasFocus);
    }

    public void initUI() {
        int i;
        ArrayList<installedClock> installedClocks = WatchApp.getInstalledClocks();
        int munber = (ClockUtil.mClockList.length + installedClocks.size()) + 1;
        String[] titles = new String[munber];
        String[] pakageName = new String[munber];
        String[] filePath = new String[munber];
        int[] ids = new int[munber];
        for (i = 0; i < ClockUtil.mClockList.length; i++) {
            titles[i] = getResources().getString(ClockUtil.mClockList[i].mTitleId);
            ids[i] = ClockUtil.mClockList[i].mThumbImageId;
        }
        for (i = ClockUtil.mClockList.length; i < munber - 1; i++) {
            Log.d("xiaocai", "filePath:" + ((installedClock) installedClocks.get(installedClocks.size() - ((munber - 1) - i))).filePath);
            titles[i] = ((installedClock) installedClocks.get(installedClocks.size() - ((munber - 1) - i))).title_name;
            pakageName[i] = ((installedClock) installedClocks.get(installedClocks.size() - ((munber - 1) - i))).pkg;
            filePath[i] = ((installedClock) installedClocks.get(installedClocks.size() - ((munber - 1) - i))).previewPath;
        }
        titles[munber - 1] = "add";
        ids[munber - 1] = R.drawable.add_button;
        if (this.imageAdapter != null) {
            this.imageAdapter.setImageAdapter(this, titles, ids, filePath, this.mIndex, pakageName);
            this.imageAdapter.notifyDataSetChanged();
        } else {
            this.imageAdapter = new ImageAdapter(this, titles, ids, filePath, this.mIndex, pakageName);
            this.myGallery.setAdapter(this.imageAdapter);
        }
        final int i2 = munber;
        this.myGallery.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (position == i2 - 1) {
                    ChooseClockActivity.this.startActivity(new Intent(ChooseClockActivity.this, ClockSkinOnlineActivity.class));
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra("index", position);
                ChooseClockActivity.this.setResult(50, intent);
                ChooseClockActivity.this.finish();
                ChooseClockActivity.this.overridePendingTransition(0, R.anim.exit_anim);
            }
        });
    }
}
