package com.github.animalize.ting.TTS;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;

import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizeBag;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.github.animalize.ting.Data.Item;
import com.github.animalize.ting.Database.DataManager;
import com.github.animalize.ting.MainListActivity;
import com.github.animalize.ting.R;

import java.util.ArrayList;
import java.util.List;

public class TTSService
        extends Service
        implements SpeechSynthesizerListener {
    public static final int EMPTY = 0;
    public static final int STOP = 1;
    public static final int PLAYING = 2;
    public static final int PAUSING = 3;
    private static final int WINDOW = 2;

    private static String SPEECH_EVENT_INTENT = "TTSEvent";
    private static String SPEECH_START_INTENT = "SpeechStart";

    private final IBinder mBinder = new ArticleTtsBinder();
    private LocalBroadcastManager mLBM;
    private SpeechSynthesizer mSpeechSynthesizer;

    private DataManager dataManager = DataManager.getInstance();

    private int mNowState = EMPTY;

    private String mTitle, mText;
    private int mCjkChars;

    private List<Ju> mJus;
    private int mNowQueueIndex = 0;
    private int mNowSpeechIndex = 0;

    public TTSService() {
    }

    public static IntentFilter getSpeechEventIntentFilter() {
        return new IntentFilter(SPEECH_EVENT_INTENT);
    }

    public static IntentFilter getSpeechStartIntentFilter() {
        return new IntentFilter(SPEECH_START_INTENT);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mLBM = LocalBroadcastManager.getInstance(this);

        // 前台服务
        Intent notificationIntent = new Intent(this, MainListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notifiy_icon)
                .setContentTitle("ting的前台服务")
                .setContentText("")
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
        mLBM.sendBroadcast(new Intent(SPEECH_START_INTENT));

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

    private void setEvent(int state) {
        mNowState = state;
        mLBM.sendBroadcast(new Intent(SPEECH_EVENT_INTENT));
    }

    private void playAction() {
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
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class ArticleTtsBinder extends Binder {
        public void setArticle(String title, String text) {
            mTitle = title;
            mText = text;

            mJus = TTSUtils.fenJu(text);

            stop();
            setEvent(STOP);
        }

        public boolean setArticle(String aid) {
            Item item = dataManager.getItemByAid(aid);
            if (item == null) {
                return false;
            }

            mTitle = item.getTitle();
            mText = dataManager.readArticleByAid(aid);
            mCjkChars = item.getCjk_chars();

            mJus = TTSUtils.fenJu(mText);

            stop();
            setEvent(STOP);

            return true;
        }

        public void play() {
            if (mText != null) {
                playAction();
                setEvent(PLAYING);
            }
        }

        public void stop() {
            mSpeechSynthesizer.stop();
            mNowQueueIndex = 0;
            mNowSpeechIndex = 0;

            setEvent(STOP);
        }

        public void pause() {
            mSpeechSynthesizer.pause();

            setEvent(PAUSING);
        }

        public void resume() {
            mSpeechSynthesizer.resume();

            setEvent(PLAYING);
        }

        public String getTitle() {
            return mTitle;
        }

        public String getText() {
            return mText;
        }

        public int getCjkChars() {
            return mCjkChars;
        }

        public int getTextLengh() {
            return mText.length();
        }

        public int getTextPosition() {
            return mJus.get(mNowSpeechIndex).begin;
        }

        public Ju getNowJu() {
            return mJus.get(mNowSpeechIndex);
        }

        public int getState() {
            return mNowState;
        }

        public void setPosi(int posi) {
            mSpeechSynthesizer.stop();

            int i;
            for (i = 0; i < mJus.size(); i++) {
                Ju ju = mJus.get(i);

                if (ju.begin <= posi && posi < ju.end) {
                    mNowQueueIndex = mNowSpeechIndex = i;
                    playAction();

                    setEvent(PLAYING);
                    break;
                }
            }
        }
    }
}
