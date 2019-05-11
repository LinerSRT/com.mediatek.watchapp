package com.mediatek.watchapp.online;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.mediatek.watchapp.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipException;

public class ClockSkinDownloadService extends Service {
    private static boolean isDownloading = false;
    private final int MSG_CANCEL_DOWNLOAD = 1008;
    private final int MSG_DOWNLOAD_ALREADY = 1004;
    private final int MSG_FAIL_DOWNLOAD = 1005;
    private final int MSG_FINISH_DOWNLOAD = 1003;
    private final int MSG_NET_WRONG = 1000;
    private final int MSG_SERVER_UNVALIABLE = 1006;
    private final int MSG_START_DOWNLOAD = 1001;
    private final int MSG_UNZIP_ERROR = 1007;
    private final int MSG_UPDATE_PROGRESS = 1002;
    private String clockskinDirPath = "/data/data/com.mediatek.watchapp//WiiwearClockSkin/";
    private String mAuthor;
    private byte[] mBmpFirstByByte;
    private BroadcastReceiver mCancelDownloadReceiver = null;
    private AlertDialog mDialog = null;
    private String mFileName;
    private float mFileSize;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            Intent it = new Intent("BROADCAST_DOWNLOAD_STATE_FILTER");
            switch (msg.what) {
                case 1000:
                    Toast.makeText(ClockSkinDownloadService.this.getApplicationContext(), ClockSkinDownloadService.this.getString(R.string.font_alert_info_net_exception), Toast.LENGTH_LONG).show();
                    it.putExtra("BROADCAST_DOWNLOAD_STATE", 1004);
                    it.putExtra("progress", msg.arg1);
                    ClockSkinDownloadService.this.sendBroadcast(it);
                    return;
                case 1001:
                    it.putExtra("BROADCAST_DOWNLOAD_STATE", 1004);
                    it.putExtra("progress", 0);
                    ClockSkinDownloadService.this.sendBroadcast(it);
                    return;
                case 1002:
                    it.putExtra("BROADCAST_DOWNLOAD_STATE", 1004);
                    it.putExtra("progress", msg.arg1);
                    ClockSkinDownloadService.this.sendBroadcast(it);
                    return;
                case 1003:
                    it.putExtra("BROADCAST_DOWNLOAD_STATE", 1004);
                    it.putExtra("progress", -1);
                    ClockSkinDownloadService.this.sendBroadcast(it);
                    Intent i = new Intent("com.update.installclock");
                    i.putExtra("action_str", "installclock");
                    i.putExtra("THEME_NAME", ClockSkinDownloadService.this.mThemeName);
                    i.putExtra("APK_NAME", ClockSkinDownloadService.this.mFileName);
                    i.putExtra("APK_SIZE", ClockSkinDownloadService.this.mFileSize);
                    i.putExtra("PACKAG_NAME", ClockSkinDownloadService.this.mPackageName);
                    i.putExtra("BMP_FIRST", ClockSkinDownloadService.this.mBmpFirstByByte);
                    i.putExtra("AUTHOR", ClockSkinDownloadService.this.mAuthor);
                    i.putExtra("VERSION", ClockSkinDownloadService.this.mVersion);
                    ClockSkinDownloadService.this.sendBroadcast(i);
                    ClockSkinDownloadService.this.stopSelf();
                    System.exit(0);
                    return;
                case 1004:
                    Toast.makeText(ClockSkinDownloadService.this.getApplicationContext(), ClockSkinDownloadService.this.getString(R.string.font_download_already), Toast.LENGTH_SHORT).show();
                    return;
                case 1005:
                    ClockSkinDownloadService.this.showDownloadFailAlert(R.string.font_alert_title_download_fail, R.string.font_alert_info_net_exception);
                    it.putExtra("BROADCAST_DOWNLOAD_STATE", 1004);
                    it.putExtra("progress", -1);
                    ClockSkinDownloadService.this.sendBroadcast(it);
                    return;
                case 1006:
                    ClockSkinDownloadService.this.showDownloadFailAlert(R.string.font_alert_title_download_fail, R.string.font_alert_info_server_exception);
                    return;
                case 1007:
                    ClockSkinDownloadService.this.showDownloadFailAlert(R.string.font_error, R.string.font_unzip_failed);
                    it.putExtra("BROADCAST_DOWNLOAD_STATE", 1004);
                    it.putExtra("progress", -1);
                    ClockSkinDownloadService.this.sendBroadcast(it);
                    return;
                case 1008:
                    it.putExtra("BROADCAST_DOWNLOAD_STATE", 1004);
                    it.putExtra("progress", -1);
                    ClockSkinDownloadService.this.sendBroadcast(it);
                    ClockSkinDownloadService.this.stopSelf();
                    System.exit(0);
                    return;
                default:
                    return;
            }
        }
    };
    private String mPackageName;
    private SharedPreferences mPreferences;
    private String mThemeName;
    private String mVersion;
    private boolean misDownloadError = false;

    /* renamed from: com.mediatek.watchapp.online.ClockSkinDownloadService$2 */
    class C01712 extends Thread {
        C01712() {
        }

        public void run() {
            ClockSkinDownloadService.isDownloading = true;
            ClockSkinDownloadService.this.mHandler.sendEmptyMessage(1001);
            if (NetHelper.checkIsServerOk()) {
                int fileSize = NetHelper.getOnlineFileSize(ClockSkinDownloadService.this.mFileName);
                if (fileSize != 0) {
                    ClockSkinDownloadService.this.doDownload(ClockSkinDownloadService.this.mFileName, (float) fileSize);
                    ClockSkinDownloadService.isDownloading = false;
                    return;
                }
                return;
            }
            ClockSkinDownloadService.this.mHandler.sendEmptyMessage(1006);
            ClockSkinDownloadService.isDownloading = false;
        }
    }

    /* renamed from: com.mediatek.watchapp.online.ClockSkinDownloadService$3 */
    class C01723 extends BroadcastReceiver {
        C01723() {
        }

        public void onReceive(Context arg0, Intent arg1) {
            ClockSkinDownloadService.this.showCancelDownloadDialog();
        }
    }

    /* renamed from: com.mediatek.watchapp.online.ClockSkinDownloadService$4 */
    class C01734 implements OnClickListener {
        C01734() {
        }

        public void onClick(View v) {
            ClockSkinDownloadService.this.stopSelf();
            System.exit(0);
        }
    }

    /* renamed from: com.mediatek.watchapp.online.ClockSkinDownloadService$5 */
    class C01745 implements OnClickListener {
        C01745() {
        }

        public void onClick(View v) {
            ClockSkinDownloadService.this.mDialog.dismiss();
        }
    }

    /* renamed from: com.mediatek.watchapp.online.ClockSkinDownloadService$6 */
    class C01756 implements OnClickListener {
        C01756() {
        }

        public void onClick(View v) {
            ClockSkinDownloadService.this.mHandler.sendEmptyMessage(1008);
            ClockSkinDownloadService.this.mDialog.dismiss();
        }
    }

    public void onCreate() {
        super.onCreate();
        Log.e("downloadService", "onCreate");
        initStopDownlaodReceiver();
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (NetHelper.checkNetIsOk(this)) {
            Log.d("downloadService", "got net");
            if (isDownloading) {
                Log.d("downloadService", "downloading ,try again later");
                this.mHandler.sendEmptyMessage(1004);
                return START_STICKY;
            }
            Log.d("downloadService", "start intent = " + intent);
            this.mFileName = intent.getStringExtra("APK_NAME");
            this.mFileSize = intent.getFloatExtra("APK_SIZE", 1.0f);
            this.mPackageName = intent.getStringExtra("PACKAG_NAME");
            this.mAuthor = intent.getStringExtra("AUTHOR");
            this.mVersion = intent.getStringExtra("VERSION");
            this.mBmpFirstByByte = (byte[]) intent.getSerializableExtra("BMP_FIRST");
            this.mThemeName = intent.getStringExtra("THEME_NAME");
            new C01712().start();
            return START_NOT_STICKY;
        }
        this.mHandler.sendEmptyMessage(1000);
        Log.d("downloadService", "no net");
        return START_NOT_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(this.mCancelDownloadReceiver);
    }

    private void doDownload(String fileName, float size) {
        File clockskinDir = new File(this.clockskinDirPath + this.mPackageName);
        if (clockskinDir.exists() || clockskinDir.mkdirs()) {
            File clockskinZipDir = new File("/data/data/com.mediatek.watchapp//WiiwearClockSkin/zip/");
            if (clockskinZipDir.exists() || clockskinZipDir.mkdirs()) {
                String str = fileName;
                String zipfile = clockskinZipDir + File.separator + str.substring(fileName.lastIndexOf("/") + 1);
                if (new File(zipfile).exists()) {
                    unzipToFolder(zipfile, clockskinDir + File.separator);
                    return;
                }
                this.mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                InputStream is = NetHelper.fetchOnlineFile(fileName);
                if (is == null) {
                    this.mHandler.sendEmptyMessage(1005);
                    return;
                }
                try {
                    FileOutputStream outputStream = new FileOutputStream(zipfile);
                    byte[] buffer = new byte[4096];
                    int sum = 0;
                    int total = (int) size;
                    int lastProgress = 0;
                    while (true) {
                        int count = is.read(buffer);
                        if (count != -1) {
                            outputStream.write(buffer, 0, count);
                            sum += count;
                            int progress = (sum * 100) / total;
                            if (progress % 5 == 0 && lastProgress != progress) {
                                lastProgress = progress;
                                Message msg = this.mHandler.obtainMessage();
                                msg.what = 1002;
                                msg.arg1 = progress;
                                this.mHandler.sendMessage(msg);
                            }
                        } else {
                            outputStream.flush();
                            outputStream.close();
                            Log.e("downloadService", "finish downloading.. ");
                            unzipToFolder(zipfile, clockskinDir + File.separator);
                            return;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    this.mHandler.sendEmptyMessage(1005);
                    return;
                }
            }
            this.mHandler.sendEmptyMessage(1005);
            return;
        }
        Log.e("xxxx", "mkdir fail");
        this.mHandler.sendEmptyMessage(1005);
    }

    private void unzipToFolder(String srcFile, String desDir) {
        File zipFile = new File(srcFile);
        try {
            ZipUtils.upZipFile(zipFile, desDir);
            this.mHandler.sendEmptyMessage(1003);
        } catch (ZipException ze) {
            Log.e("xiaocai_online", "ZipException" + ze);
            zipFile.delete();
            this.mHandler.sendEmptyMessage(1007);
        } catch (IOException e) {
            zipFile.delete();
            Log.e("xiaocai_online", "IOException" + e);
            this.mHandler.sendEmptyMessage(1007);
        }
    }

    private void initStopDownlaodReceiver() {
        IntentFilter intentFilter = new IntentFilter("STOP_FONT_DOWNLOAD_SERVICE");
        this.mCancelDownloadReceiver = new C01723();
        registerReceiver(this.mCancelDownloadReceiver, intentFilter);
    }

    private void showDownloadFailAlert(int titleResId, int contentResId) {
        Builder builder = new Builder(this);
        builder.setCancelable(false);
        AlertDialog dlg = builder.create();
        dlg.getWindow().setType(2003);
        dlg.show();
        Window window = dlg.getWindow();
        window.setContentView(R.layout.simple_alert_dialog);
        TextView content = (TextView) window.findViewById(R.id.content);
        ((TextView) window.findViewById(R.id.title)).setText(titleResId);
        content.setText(contentResId);
        ((Button) window.findViewById(R.id.alertok)).setOnClickListener(new C01734());
    }

    private void showCancelDownloadDialog() {
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
        Builder builder = new Builder(this);
        builder.setCancelable(false);
        this.mDialog = builder.create();
        this.mDialog.getWindow().setType(2003);
        this.mDialog.show();
        Window window = this.mDialog.getWindow();
        window.setContentView(R.layout.query_alert_dialog);
        window.setGravity(17);
        TextView content = (TextView) window.findViewById(R.id.content);
        ((TextView) window.findViewById(R.id.title)).setText(R.string.alert_title_font_download_cancel);
        content.setText(R.string.alert_info_font_download_cancel);
        Button btnok = (Button) window.findViewById(R.id.alertok);
        ((Button) window.findViewById(R.id.alertcancel)).setOnClickListener(new C01745());
        btnok.setOnClickListener(new C01756());
    }
}
