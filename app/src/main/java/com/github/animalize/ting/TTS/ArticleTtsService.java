package com.github.animalize.ting.TTS;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;

import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizeBag;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.github.animalize.ting.MainListActivity;

import java.util.ArrayList;
import java.util.List;

public class ArticleTtsService
        extends Service
        implements SpeechSynthesizerListener {
    private static final int WINDOW = 2;

    private final IBinder mBinder = new ArticleTtsBinder();

    private SpeechSynthesizer mSpeechSynthesizer;

    private String mTitle, mText;
    private List<Ju> mJus;
    private int mNowJuIndex = 0;

    public ArticleTtsService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // 前台服务
        Intent notificationIntent = new Intent(this, MainListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
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
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onSpeechStart(String s) {
        if (mNowJuIndex < mJus.size()) {
            final Ju ju = mJus.get(mNowJuIndex);
            final String temp = mText.substring(ju.begin, ju.end);

            mSpeechSynthesizer.speak(temp, String.valueOf(mNowJuIndex));

            mNowJuIndex += 1;
        }
    }

    @Override
    public void onSpeechProgressChanged(String s, int i) {

    }

    @Override
    public void onSpeechFinish(String s) {

    }

    @Override
    public void onError(String s, SpeechError speechError) {

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

    public class ArticleTtsBinder extends Binder {
        @SuppressWarnings("unused")
        public void setArticle(String title, String text) {
            mTitle = title;
            mText = text;

            mJus = TTSUtils.fenJu(text);

            stop();
        }

        @SuppressWarnings("unused")
        public void play() {
            List<SpeechSynthesizeBag> bags = new ArrayList<>();

            int end = mNowJuIndex + WINDOW < mJus.size()
                    ? mNowJuIndex + WINDOW
                    : mJus.size();

            for (int i = mNowJuIndex; i < end; i++) {
                final Ju ju = mJus.get(i);
                final String s = mText.substring(ju.begin, ju.end);

                SpeechSynthesizeBag bag = new SpeechSynthesizeBag();
                bag.setText(s);
                bag.setUtteranceId(String.valueOf(i));

                bags.add(bag);
            }

            mNowJuIndex = end;
            mSpeechSynthesizer.batchSpeak(bags);
        }

        @SuppressWarnings("unused")
        public void stop() {
            mSpeechSynthesizer.stop();
            mNowJuIndex = 0;
        }
    }
}
