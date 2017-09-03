package com.github.animalize.ting.TTS;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;

import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizeBag;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.github.animalize.ting.MainListActivity;
import com.github.animalize.ting.R;

import java.util.ArrayList;
import java.util.List;

public class ArticleTtsService
        extends Service
        implements SpeechSynthesizerListener {
    public static final int EMPTY = 0;
    public static final int STOP = 1;
    public static final int PLAYING = 2;
    public static final int PAUSING = 3;

    private static final int WINDOW = 2;
    private final IBinder mBinder = new ArticleTtsBinder();

    private LocalBroadcastManager localBroadcastManager;
    private SpeechSynthesizer mSpeechSynthesizer;

    private int mNowState = EMPTY;
    private String mTitle, mText;
    private List<Ju> mJus;
    private int mNowQueueIndex = 0;
    private int mNowSpeechIndex = 0;

    public ArticleTtsService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        // 前台服务
        Intent notificationIntent = new Intent(this, MainListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notifiy_icon)
                .setContentTitle("ting正在运行")
                .setContentText("前台服务保证ting不被销毁")
                .setContentIntent(pendingIntent)
                .build();
        startForeground(828, notification);

        // tts引擎
        TTSHelper.initialEnv(this);
        mSpeechSynthesizer = TTSHelper.initTTS(this, this);
    }

    @Override
    public void onDestroy() {
        mSpeechSynthesizer.release();
        super.onDestroy();
    }

    @Override
    public void onSpeechStart(String s) {
        // 进度
        mNowSpeechIndex = Integer.parseInt(s);
        localBroadcastManager.sendBroadcast(new Intent("SpeechStart"));

        // 队列
        if (mNowQueueIndex < mJus.size()) {
            final Ju ju = mJus.get(mNowQueueIndex);
            final String temp = mText.substring(ju.begin, ju.end);

            mSpeechSynthesizer.speak(temp, String.valueOf(mNowQueueIndex));

            mNowQueueIndex += 1;
        }
    }

    @Override
    public void onSpeechProgressChanged(String s, int i) {

    }

    @Override
    public void onSpeechFinish(String s) {
        if (mNowSpeechIndex >= mJus.size() - 1) {
            mNowState = STOP;
        }
    }

    @Override
    public void onError(String s, SpeechError speechError) {
        if (mNowSpeechIndex >= mJus.size() - 1) {
            mNowState = STOP;
        }
    }

    @Override
    public void onSynthesizeStart(String s) {
    }

    @Override
    public void onSynthesizeDataArrived(String s, byte[] bytes, int i) {
    }

    @Override
    public void onSynthesizeFinish(String s) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class ArticleTtsBinder extends Binder {
        @SuppressWarnings("unused")
        public void setArticle(String title, String text) {
            mTitle = title;
            mText = text;

            mJus = TTSUtils.fenJu(text);

            stop();
            mNowState = STOP;
        }

        @SuppressWarnings("unused")
        public void play() {
            List<SpeechSynthesizeBag> bags = new ArrayList<>();

            int end = mNowQueueIndex + WINDOW < mJus.size()
                    ? mNowQueueIndex + WINDOW
                    : mJus.size();

            for (int i = mNowQueueIndex; i < end; i++) {
                final Ju ju = mJus.get(i);
                final String s = mText.substring(ju.begin, ju.end);

                SpeechSynthesizeBag bag = new SpeechSynthesizeBag();
                bag.setText(s);
                bag.setUtteranceId(String.valueOf(i));

                bags.add(bag);
            }

            mNowQueueIndex = end;
            mSpeechSynthesizer.batchSpeak(bags);

            mNowState = PLAYING;
        }

        @SuppressWarnings("unused")
        public void stop() {
            mSpeechSynthesizer.stop();
            mNowQueueIndex = 0;
            mNowSpeechIndex = 0;

            mNowState = STOP;
        }

        @SuppressWarnings("unused")
        public void pause() {
            mSpeechSynthesizer.pause();
            mNowState = PAUSING;
        }

        @SuppressWarnings("unused")
        public void resume() {
            mSpeechSynthesizer.resume();
            mNowState = PLAYING;
        }

        @SuppressWarnings("unused")
        public Ju getNowJu() {
            return mJus.get(mNowSpeechIndex);
        }

        @SuppressWarnings("unused")
        public int getState() {
            return mNowState;
        }
    }
}
