package com.github.animalize.ting.TTS;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizeBag;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class TTSService
        extends Service
        implements SpeechSynthesizerListener {
    public static final int EMPTY = 0;
    public static final int STOP = 1;
    public static final int PLAYING = 2;
    public static final int PAUSING = 3;

    private static String SPEECH_EVENT_INTENT = "TTSEvent";
    private static Intent mEventIntent = new Intent(SPEECH_EVENT_INTENT);
    private static String SPEECH_START_INTENT = "SpeechStart";
    private static Intent mStartIntent = new Intent(SPEECH_START_INTENT);
    private final IBinder mBinder = new ArticleTtsBinder();
    private int mThreshold;
    private int mWindow;
    private SpeechSynthesizer mSpeechSynthesizer;
    private LocalBroadcastManager mLBM;

    private IArticle mArticle;
    private String mTitle, mText;

    private int mNowState = EMPTY;

    private List<Ju> mJus;
    private int mNowQueueIndex = 0;
    private int mNowSpeechIndex = 0;

    private SoundPool soundPool;
    private int soundID;

    public TTSService() {
    }

    public static IntentFilter getSpeechEventIntentFilter() {
        return new IntentFilter(SPEECH_EVENT_INTENT);
    }

    public static IntentFilter getSpeechStartIntentFilter() {
        return new IntentFilter(SPEECH_START_INTENT);
    }

    public int getFinishSoundID() {
        // 返回-1表示不播放
        return -1;
    }

    public abstract int initTTS(int currentVer);

    public abstract void doStartForeground();

    public abstract Setting getSetting();

    private void setSetting() {
        Setting s = getSetting();

        mThreshold = s.getmThreshold();
        mWindow = s.getmWindow();
        Ju.setSize(s.getmFenJu());

        if (mSpeechSynthesizer != null) {
            s.setSettingToSpeechSynthesizer(mSpeechSynthesizer);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mLBM = LocalBroadcastManager.getInstance(this);

        doStartForeground();
    }

    private void lazyInit() {
        // SoundPool
        int resSoundID = getFinishSoundID();
        if (resSoundID != -1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                soundPool = new SoundPool.Builder()
                        .setMaxStreams(1)
                        .build();
            } else {
                soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
            }
            soundID = soundPool.load(this, resSoundID, 1);
        }

        Setting setting = getSetting();

        // 复制引擎文件
        int nowFileVer = initTTS(setting.getmModelFileVer());
        if (nowFileVer != setting.getmModelFileVer()) {
            setting.setmModelFileVer(nowFileVer);
            setting.saveSetting(this);
        }

        // 初始化引擎
        mSpeechSynthesizer = setting.initTTS(this, this);
        // 设置引擎
        setSetting();
    }

    @Override
    public void onDestroy() {
        if (mSpeechSynthesizer != null) {
            mSpeechSynthesizer.release();
        }

        if (soundPool != null) {
            soundPool.release();
        }

        super.onDestroy();
    }

    @Override
    public void onSpeechStart(String s) {
        // 进度
        mNowSpeechIndex = Integer.parseInt(s);
        mLBM.sendBroadcast(mStartIntent);

        if (mNowQueueIndex - mNowSpeechIndex <= mThreshold) {
            // 剩余低于阈值
            for (int i = 0; i < mWindow; i++) {
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
            mNowQueueIndex = mNowSpeechIndex = 0;
            setEvent(STOP);

            if (soundPool != null) {
                soundPool.play(soundID, 1, 1, 1, 0, 1f);
            }
        }
    }

    @Override
    public void onError(String s, SpeechError speechError) {
        if (mNowSpeechIndex >= mJus.size() - 1) {
            mNowQueueIndex = mNowSpeechIndex = 0;
            setEvent(STOP);

            if (soundPool != null) {
                soundPool.play(soundID, 1, 1, 1, 0, 1f);
            }
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
        mLBM.sendBroadcast(mEventIntent);
    }

    private void playAction() {
        List<SpeechSynthesizeBag> bags = new ArrayList<>();

        final int temp = mNowQueueIndex + mThreshold + 1;
        int end = temp < mJus.size() ? temp : mJus.size();

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
        // 以下两个函数，任何一个返回null则会清空TTSServices
        String getTitle();

        String getText();
    }

    public static class Ju {
        private static int SIZE = 80; //256;
        private static Pattern biaodian;
        public int begin;
        public int end;

        public Ju(int begin, int end) {
            this.begin = begin;
            this.end = end;
        }

        public static void setSize(int size) {
            SIZE = size;
        }

        private static List<Ju> fenJu(String s) {
            if (biaodian == null) {
                biaodian = Pattern.compile(
                        "^.*[\n，。！？：；、”…,!?]",
                        Pattern.DOTALL);
            }

            List<Ju> ret = new ArrayList<>();

            int p = 0;

            while (true) {
                if (p + SIZE >= s.length()) {
                    Ju ju = new Ju(p, s.length());
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

                Ju ju = new Ju(p, p + end);
                ret.add(ju);
                p += end;
            }

            return ret;
        }
    }

    public class ArticleTtsBinder extends Binder {
        public boolean setArticle(IArticle article) {
            mNowQueueIndex = mNowSpeechIndex = 0;

            mArticle = article;
            if (mArticle == null) {
                mJus = null;
                setEvent(EMPTY);
                return false;
            }

            mTitle = mArticle.getTitle();
            mText = mArticle.getText();
            if (mTitle == null || mText == null) {
                mArticle = null;
                mJus = null;
                setEvent(EMPTY);
                return false;
            }

            mJus = Ju.fenJu(mText);

            stop();
            setEvent(STOP);

            return true;
        }

        public void play() {
            if (mSpeechSynthesizer == null) {
                lazyInit();
            }

            if (mArticle != null) {
                playAction();
                setEvent(PLAYING);
            }
        }

        public void stop() {
            if (mSpeechSynthesizer == null) {
                return;
            }

            mSpeechSynthesizer.stop();
            mNowQueueIndex = mNowSpeechIndex = 0;
            setEvent(STOP);
        }

        public void pause() {
            if (mSpeechSynthesizer == null) {
                return;
            }

            mSpeechSynthesizer.pause();
            setEvent(PAUSING);
        }

        public void resume() {
            if (mSpeechSynthesizer == null) {
                return;
            }

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

        @Nullable
        public Ju getNowJu() {
            if (mArticle == null || mNowState == STOP || mNowState == EMPTY ||
                    mJus == null || mNowQueueIndex >= mJus.size()) {
                return null;
            }

            return mJus.get(mNowSpeechIndex);
        }

        public int getState() {
            return mNowState;
        }

        public boolean setPosi(int posi) {
            if (mSpeechSynthesizer == null) {
                return false;
            }

            mSpeechSynthesizer.stop();

            int low = 0;
            int high = mJus.size() - 1;

            while (high >= low) {
                int mid = (low + high) / 2;
                final Ju temp = mJus.get(mid);

                if (posi < temp.begin) {
                    high = mid - 1;
                } else if (posi >= temp.end) {
                    low = mid + 1;
                } else {
                    mNowQueueIndex = mNowSpeechIndex = mid;
                    playAction();

                    setEvent(PLAYING);
                    return true;
                }
            }

            return false;
        }

        public void setSetting() {
            TTSService.this.setSetting();
        }
    }
}
