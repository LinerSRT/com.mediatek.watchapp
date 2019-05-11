package com.mediatek.watchapp;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Audio.Media;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MusicMediaFragment extends Fragment implements OnClickListener {
    static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
    static final String sExternalMediaUri = Media.EXTERNAL_CONTENT_URI.toString();
    long mAudioId = -1;
    AudioManager mAudioManager;
    ImageButton mBtnBig;
    ImageButton mBtnNext;
    ImageButton mBtnPause;
    ImageButton mBtnPrev;
    ImageButton mBtnSmall;
    Context mContext;
    BroadcastReceiver mIntentReceiver = new C01291();
    RelativeLayout mLayBackground;
    TextView mTvTrack;
    TextView mTvVolume;

    /* renamed from: com.mediatek.watchapp.MusicMediaFragment$1 */
    class C01291 extends BroadcastReceiver {
        C01291() {
        }

        public void onReceive(Context context, Intent intent) {
            MusicMediaFragment.this.handleAction(intent);
        }
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mContext = getActivity();
        registerReceiver();
    }

    @SuppressLint("WrongConstant")
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.music_media_fragment, container, false);
        this.mLayBackground = (RelativeLayout) view.findViewById(R.id.lay_background);
        this.mLayBackground.setOnClickListener(this);
        this.mBtnBig = (ImageButton) view.findViewById(R.id.btn_big);
        this.mBtnBig.setOnClickListener(this);
        this.mBtnSmall = (ImageButton) view.findViewById(R.id.btn_small);
        this.mBtnSmall.setOnClickListener(this);
        this.mBtnPrev = (ImageButton) view.findViewById(R.id.btn_prev);
        this.mBtnPrev.setOnClickListener(this);
        this.mBtnPause = (ImageButton) view.findViewById(R.id.btn_pause);
        this.mBtnPause.setOnClickListener(this);
        this.mBtnNext = (ImageButton) view.findViewById(R.id.btn_next);
        this.mBtnNext.setOnClickListener(this);
        this.mTvVolume = (TextView) view.findViewById(R.id.tv_volume);
        this.mTvTrack = (TextView) view.findViewById(R.id.tv_trackname);
        if (this.mAudioManager == null) {
            this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        }
        this.mTvVolume.setText(this.mAudioManager.getStreamVolume(3) + "");
        return view;
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            onResume();
        } else {
            onPause();
        }
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    public void onStop() {
        super.onStop();
    }

    public void onDestroy() {
        super.onDestroy();
        this.mContext.unregisterReceiver(this.mIntentReceiver);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_prev:
                sendCommand("com.android.music.musicservicecommand.previous");
                return;
            case R.id.btn_pause:
                sendCommand("com.android.music.musicservicecommand.togglepause");
                return;
            case R.id.btn_next:
                sendCommand("com.android.music.musicservicecommand.next");
                return;
            case R.id.btn_small:
                setVolumeDown();
                return;
            case R.id.btn_big:
                setVolumeUp();
                return;
            default:
                return;
        }
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.android.music.playbackcomplete");
        filter.addAction("com.android.music.quitplayback");
        filter.addAction("com.android.music.playstatechanged");
        filter.addAction("com.android.music.metachanged");
        filter.addAction("com.android.music.queuechanged");
        filter.addAction("android.intent.action.LOCALE_CHANGED");
        this.mContext.registerReceiver(this.mIntentReceiver, filter);
    }

    private void handleAction(Intent intent) {
        long id = intent.getLongExtra("id", -1);
        String artist = intent.getStringExtra("artist");
        String album = intent.getStringExtra("album");
        String track = intent.getStringExtra("track");
        boolean playing = intent.getBooleanExtra("playing", false);
        if (this.mTvTrack != null) {
            if (track == null) {
                track = getString(R.string.no_music);
            }
            this.mTvTrack.setText(track);
            this.mBtnPause.setImageResource(playing ? R.drawable.music_pause_key : R.drawable.music_play_key);
            if (id != this.mAudioId) {
                this.mAudioId = id;
            }
        }
    }

    private void sendCommand(String command) {
        ComponentName serviceName = new ComponentName("com.android.music", "com.android.music.MediaPlaybackService");
        Intent intent = new Intent(command);
        intent.setComponent(serviceName);
        this.mContext.startService(intent);
    }

    private void setVolumeDown() {
        synchronized (this.mAudioManager) {
            this.mAudioManager.adjustStreamVolume(3, -1, 8);
        }
        this.mTvVolume.setText(this.mAudioManager.getStreamVolume(3) + "");
    }

    private void setVolumeUp() {
        synchronized (this.mAudioManager) {
            this.mAudioManager.adjustStreamVolume(3, 1, 8);
        }
        this.mTvVolume.setText(this.mAudioManager.getStreamVolume(3) + "");
    }
}
