package com.github.animalize.ting.PlayerUI;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.animalize.ting.Data.Item;
import com.github.animalize.ting.R;
import com.github.animalize.ting.TTS.TTSService;


public class PlayerPanelWidget extends LinearLayout implements View.OnClickListener {
    private TTSService.ArticleTtsBinder mBinder;

    private SeekBar mProgress;
    private Button mPageButton;
    private TextView mTitleText, mProgressText;
    private int fullLengh;

    private Button mPlay, mStop;

    private LocalBroadcastManager mLBM = LocalBroadcastManager.getInstance(getContext());
    private SpeechEventReciver mSpeechEventReciver = new SpeechEventReciver();
    private SpeechStartReciver mSpeechStartReciver = new SpeechStartReciver();
    private PageChangeReciver mPageChangeReciver = new PageChangeReciver();

    public PlayerPanelWidget(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_player_panel, this);

        mProgressText = findViewById(R.id.progress_text);

        mPageButton = findViewById(R.id.page_button);
        mPageButton.setOnClickListener(this);

        mTitleText = findViewById(R.id.title);

        mProgress = findViewById(R.id.progress);
        mProgress.setOnSeekBarChangeListener(new SeekBarListener());

        mPlay = findViewById(R.id.play_pause);
        mPlay.setOnClickListener(this);
        mStop = findViewById(R.id.stop);
        mStop.setOnClickListener(this);

    }

    public void setTTSBinder(TTSService.ArticleTtsBinder binder) {
        mBinder = binder;

        freshUIofItem();
        mPageChangeReciver.onReceive(null, null);
        mSpeechEventReciver.onReceive(null, null);
        mSpeechStartReciver.onReceive(null, null);
    }

    @Override
    public void onClick(View v) {
        if (mBinder == null) {
            return;
        }

        switch (v.getId()) {
            case R.id.play_pause:
                int state = mBinder.getState();

                if (state == TTSService.PLAYING) {
                    mBinder.pause();
                } else if (state == TTSService.PAUSING) {
                    mBinder.resume();
                } else if (state == TTSService.STOP || state == TTSService.FINISHED) {
                    mBinder.play();
                }
                break;

            case R.id.stop:
                mBinder.stop();
                break;

            case R.id.page_button:
                break;
        }
    }

    public void onStart() {
        mLBM.registerReceiver(
                mPageChangeReciver,
                TTSService.getPageChangeIntentFilter());
        mLBM.registerReceiver(
                mSpeechEventReciver,
                TTSService.getSpeechEventIntentFilter());
        mLBM.registerReceiver(
                mSpeechStartReciver,
                TTSService.getSpeechStartIntentFilter());

        mPageChangeReciver.onReceive(null, null);
        mSpeechEventReciver.onReceive(null, null);
        mSpeechStartReciver.onReceive(null, null);
    }

    public void onStop() {
        mLBM.unregisterReceiver(mSpeechEventReciver);
        mLBM.unregisterReceiver(mSpeechStartReciver);
        mLBM.unregisterReceiver(mPageChangeReciver);
    }

    private void freshUIofItem() {
        Item item = (Item) mBinder.getArticle();
        if (item == null) {
            return;
        }

        mTitleText.setText(mBinder.getTitle());

        fullLengh = mBinder.getPageText().length();
        mProgress.setMax(fullLengh > 0 ? fullLengh - 1 : fullLengh);
    }

    private class SpeechStartReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mBinder == null) {
                return;
            }

            if (mBinder.getState() == TTSService.FINISHED) {
                mProgress.setProgress(mProgress.getMax());
                mProgressText.setText("100% ");
                return;
            }

            TTSService.Ju ju = mBinder.getNowJu();
            if (ju != null) {
                mProgress.setProgress(ju.begin);

                int v = ju.begin * 100 / fullLengh;
                mProgressText.setText("" + v + "% ");
            }
        }
    }

    private class SpeechEventReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mBinder == null) {
                return;
            }

            int state = mBinder.getState();

            String play;
            boolean enablePlay, enableStop;

            switch (state) {
                case TTSService.PLAYING:
                    play = "暂停";
                    enablePlay = true;
                    enableStop = true;

                    freshUIofItem();
                    break;

                case TTSService.PAUSING:
                    play = "恢复";
                    enablePlay = true;
                    enableStop = true;
                    break;

                case TTSService.STOP:
                    play = "播放";
                    enablePlay = true;
                    enableStop = false;

                    mProgress.setProgress(0);
                    mProgressText.setText("0% ");
                    break;

                case TTSService.FINISHED:
                    play = "播放";
                    enablePlay = true;
                    enableStop = false;

                    mProgress.setProgress(mProgress.getMax());
                    mProgressText.setText("100% ");
                    break;

                case TTSService.EMPTY:
                    play = "播放";
                    enablePlay = false;
                    enableStop = false;
                    break;

                default:
                    play = "播放";
                    enablePlay = true;
                    enableStop = true;
            }

            mPlay.setEnabled(enablePlay);
            mPlay.setText(play);

            mStop.setEnabled(enableStop);
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
            if (mBinder != null && mBinder.getArticle() != null) {
                mBinder.setPagePosi(posi);
            }
        }
    }

    private class PageChangeReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mBinder == null) {
                return;
            }

            String s = mBinder.getPageButtonText();
            mPageButton.setText(s);
        }
    }
}
