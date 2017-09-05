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
import com.github.animalize.ting.MainListActivity;
import com.github.animalize.ting.R;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TTSService
        extends Service
        implements SpeechSynthesizerListener {
    public static final int EMPTY = 0;
    public static final int STOP = 1;
    public static final int PLAYING = 2;
    public static final int PAUSING = 3;

    private static final int THRESHOLD = 2;
    private static final int WINDOW = 2;
    private static String SPEECH_EVENT_INTENT = "TTSEvent";
    private static String SPEECH_START_INTENT = "SpeechStart";
    private final IBinder mBinder = new ArticleTtsBinder();
    private SpeechSynthesizer mSpeechSynthesizer;
    private LocalBroadcastManager mLBM;
    private int mNowState = EMPTY;

    private IArticle mArticle;
    private String mTitle, mText;

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

        if (mNowQueueIndex - mNowSpeechIndex <= THRESHOLD) {
            // 剩余低于阈值
            for (int i = 0; i < WINDOW; i++) {
                if (mNowQueueIndex >= mJus.size()) {
                    // 已读完
                    break;
                }

                final Ju ju = mJus.get(mNowQueueIndex);
                final String str = mText.substring(ju.begin, ju.end);
                mSpeechSynthesizer.speak(str, String.valueOf(mNowQueueIndex));

                mNowQueueIndex += 1;
            }
        }
    }

    @Override
    public void onSpeechProgressChanged(String s, int i) {

    }

    @Override
    public void onSpeechFinish(String s) {
        if (mNowSpeechIndex >= mJus.size() - 1) {
            setEvent(STOP);
        }
    }

    @Override
    public void onError(String s, SpeechError speechError) {
        if (mNowSpeechIndex >= mJus.size() - 1) {
            setEvent(STOP);
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

    public interface IArticle {
        String getTitle();

        String getText();
    }

    public static class Ju {
        private static final int SIZE = 64; //256;
        private static Pattern biaodian;
        public int begin;
        public int end;

        public Ju(int begin, int end) {
            this.begin = begin;
            this.end = end;
        }

        private static List<TTSService.Ju> fenJu(String s) {
            if (biaodian == null) {
                biaodian = Pattern.compile(
                        "^.*[\n，。！？；：,.!?]",
                        Pattern.DOTALL);
            }

            List<TTSService.Ju> ret = new ArrayList<>();

            int p = 0;

            while (true) {
                if (p + SIZE >= s.length()) {
                    TTSService.Ju ju = new TTSService.Ju(p, s.length());
                    ret.add(ju);

                    break;
                }

                String sub = s.substring(p, p + SIZE);
                Matcher m = biaodian.matcher(sub);

                int end;
                if (m.find()) {
                    end = m.end();
                } else {
                    final char last = sub.charAt(sub.length() - 1);
                    if (Character.isHighSurrogate(last)) {
                        end = sub.length() - 1;
                    } else {
                        end = sub.length();
                    }
                }

                TTSService.Ju ju = new TTSService.Ju(p, p + end);
                ret.add(ju);
                p += end;
            }

            return ret;
        }
    }

    public class ArticleTtsBinder extends Binder {
        public boolean setArticle(IArticle article) {
            mArticle = article;
            if (mArticle == null) {
                return false;
            }

            mTitle = mArticle.getTitle();
            mText = mArticle.getText();
            if (mTitle == null || mText == null) {
                mArticle = null;
                return false;
            }

            mJus = Ju.fenJu(mText);

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

        public Object getArticle() {
            return mArticle;
        }

        public String getTitle() {
            return mTitle;
        }

        public String getText() {
            return mText;
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
