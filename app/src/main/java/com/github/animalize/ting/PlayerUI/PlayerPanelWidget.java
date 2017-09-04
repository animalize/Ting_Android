package com.github.animalize.ting.PlayerUI;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.github.animalize.ting.R;
import com.github.animalize.ting.TTS.ArticleTtsService;


public class PlayerPanelWidget extends LinearLayout implements View.OnClickListener {
    private ArticleTtsService.ArticleTtsBinder mBinder;

    private SeekBar mProgress;
    private Button mPlay, mStop;

    private LocalBroadcastManager mLBM = LocalBroadcastManager.getInstance(getContext());
    private SpeechEventReciver mSpeechEventReciver = new SpeechEventReciver();
    private SpeechStartReciver mSpeechStartReciver = new SpeechStartReciver();

    public PlayerPanelWidget(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_player_panel, this);

        mProgress = (SeekBar) findViewById(R.id.progress);
        mProgress.setOnSeekBarChangeListener(new SeekBarListener());

        mPlay = (Button) findViewById(R.id.play_pause);
        mPlay.setOnClickListener(this);
        mStop = (Button) findViewById(R.id.stop);
        mStop.setOnClickListener(this);
    }

    public void setTTSBinder(ArticleTtsService.ArticleTtsBinder binder) {
        mBinder = binder;

        mBinder.play();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_pause:
                int state = mBinder.getState();

                if (state == ArticleTtsService.PLAYING) {
                    mBinder.pause();
                } else if (state == ArticleTtsService.PAUSING) {
                    mBinder.resume();
                } else if (state == ArticleTtsService.STOP) {
                    mBinder.play();
                }
                break;

            case R.id.stop:
                mBinder.stop();
                break;
        }
    }

    public void onStart() {
        mLBM.registerReceiver(
                mSpeechEventReciver,
                ArticleTtsService.getSpeechEventIntentFilter());
        mLBM.registerReceiver(
                mSpeechStartReciver,
                ArticleTtsService.getSpeechStartIntentFilter());
    }

    public void onStop() {
        mLBM.unregisterReceiver(mSpeechEventReciver);
        mLBM.unregisterReceiver(mSpeechStartReciver);
    }

    private class SpeechStartReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("Panel onReceive: ", "开始");
            mProgress.setProgress(mBinder.getTextPosition());
        }
    }

    private class SpeechEventReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("Panel onReceive: ", "事件");
            int state = mBinder.getState();

            String play;
            boolean stop;

            switch (state) {
                case ArticleTtsService.PLAYING:
                    play = "暂停";
                    stop = true;
                    mProgress.setMax(mBinder.getTextLengh());
                    break;

                case ArticleTtsService.PAUSING:
                    play = "恢复";
                    stop = true;
                    break;

                case ArticleTtsService.STOP:
                    play = "播放";
                    stop = false;
                    break;

                case ArticleTtsService.EMPTY:
                    play = "空文";
                    stop = false;
                    break;

                default:
                    play = "播放";
                    stop = true;
            }

            mPlay.setText(play);
            mStop.setEnabled(stop);
        }
    }

    private class SeekBarListener implements SeekBar.OnSeekBarChangeListener {
        private int posi = 0;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                posi = progress;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mBinder.setPosi(posi);
        }
    }
}
