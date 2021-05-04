package com.github.animalize.ting.TTS;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizeBag;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
    TTSService抽象类，基础服务
    IArticle接口，文章需实现此接口
    Ju类，一句在当前页中的位置
    PagaManager私有类，分页逻辑
    ArticleTtsBinder类，服务对外绑定的接口
 */

public abstract class TTSService
        extends Service
        implements SpeechSynthesizerListener {
    public static final int EMPTY = 0;
    public static final int STOP = 1;
    public static final int FINISHED = 2;
    public static final int PLAYING = 3;
    public static final int PAUSING = 4;
    public static final int PAGE_SIZE = 10000;
    private static String SPEECH_EVENT_INTENT = "TTSEvent";
    private static Intent mEventIntent = new Intent(SPEECH_EVENT_INTENT);
    private static String SPEECH_START_INTENT = "SpeechStart";
    private static Intent mStartIntent = new Intent(SPEECH_START_INTENT);
    private static String PAGE_CHANGE_INTENT = "PageChange";
    private static Intent mPageChangeIntent = new Intent(PAGE_CHANGE_INTENT);
    private static Pattern page_regex;

    private final ArticleTtsBinder mBinder = new ArticleTtsBinder();

    private PageManager mPageManager = new PageManager();
    private SpeechSynthesizer mSpeechSynthesizer;
    private LocalBroadcastManager mLBM;

    private int mNowState = EMPTY;

    private IArticle mArticle;
    private String mTitle, mText, mPageText;

    private int mThreshold;
    private int mWindow;

    private List<Ju> mJus;
    // 队列里最后一个句子的位置+1
    private int mNowQueueIndex = 0;
    // 当前朗读的句子
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

    public static IntentFilter getPageChangeIntentFilter() {
        return new IntentFilter(PAGE_CHANGE_INTENT);
    }

    public static Pattern getFenjuRegex() {
        if (page_regex == null) {
            page_regex = Pattern.compile(
                    "^.*[\\n，。！？：；、”…,!? ]",
                    Pattern.DOTALL);
        }
        return page_regex;
    }

    /*
        返回分段信息，单位是char
        如: "9993 19985 29983 39983 49970 54153"
    */
    public static String getSegments(String text) {
        Pattern regex = getFenjuRegex();

        int p = 0;
        StringBuilder ret = new StringBuilder();

        while (true) {
            if (p + PAGE_SIZE >= text.length()) {
                ret.append(text.length());
                break;
            }

            String sub = text.substring(p, p + PAGE_SIZE);
            Matcher m = regex.matcher(sub);

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
            p += end;
            ret.append(p).append(" ");
        }
        return ret.toString();
    }

    public int getFinishSoundID() {
        // 返回-1表示不播放
        return -1;
    }

    public abstract void doStartForeground();

    public abstract Setting getSetting();

    private void setSetting() {
        Setting s = getSetting();

        mThreshold = s.getmThreshold();
        mWindow = s.getmWindow();
        Ju.setSize(s.getmFenJu());

        if (mSpeechSynthesizer != null) {
            // 保存TTS版本
            String ttsVer = mSpeechSynthesizer.libVersion();
            if (!ttsVer.equals(s.getmTTSVersion())) {
                s.setmTTSVersion(ttsVer);
                s.saveSetting(this);
            }

            // 设置TTS对象的参数
            s.setSettingToSpeechSynthesizer(mSpeechSynthesizer, this);
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

        // 初始化引擎
        mSpeechSynthesizer = setting.initTTS(this, this);
        // 设置引擎
        setSetting();
    }

    @Override
    public void onDestroy() {
        if (mSpeechSynthesizer != null) {
            mBinder.stop();

            mSpeechSynthesizer.release();
            mSpeechSynthesizer = null;
        }

        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }

        super.onDestroy();
    }

    @Override
    public void onSpeechStart(String s) {
        // 进度
        try {
            mNowSpeechIndex = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return;
        }

        mLBM.sendBroadcast(mStartIntent);

        if (mNowQueueIndex - mNowSpeechIndex <= mThreshold) {
            // 剩余低于阈值
            List<SpeechSynthesizeBag> bags = new ArrayList<>();

            for (int i = 0; i < mWindow; i++) {
                if (mNowQueueIndex >= mJus.size()) {
                    // 已读完
                    break;
                }

                SpeechSynthesizeBag bag = new SpeechSynthesizeBag();
                final Ju ju = mJus.get(mNowQueueIndex);
                bag.setText(mPageText.substring(ju.begin, ju.end));
                bag.setUtteranceId(String.valueOf(mNowQueueIndex));

                bags.add(bag);
                mNowQueueIndex += 1;
            }

            mSpeechSynthesizer.batchSpeak(bags);
        }
    }

    @Override
    public void onSpeechProgressChanged(String s, int i) {

    }

    @Override
    public void onSpeechFinish(String s) {
        if (mNowSpeechIndex >= mJus.size() - 1) {
            if (mPageManager.toNextPage()) {
                playAction(true);
            } else {
                setEvent(FINISHED);
                mArticle.setNowChar(0, true);

                if (soundPool != null) {
                    soundPool.play(soundID, 1, 1, 1, 0, 1f);
                }
            }
        }
    }

    @Override
    public void onError(String s, SpeechError speechError) {
        //Log.d("onError", s + speechError.description);
        onSpeechFinish(s);
    }

    @Override
    public void onSynthesizeStart(String s) {
    }

    @Override
    public void onSynthesizeDataArrived(String s, byte[] bytes, int i, int i1) {
    }

    @Override
    public void onSynthesizeFinish(String s) {
    }

    private void setEvent(int state) {
        mNowState = state;
        mLBM.sendBroadcast(mEventIntent);
    }

    private void playAction(boolean resetIndex) {
        if (resetIndex) {
            mNowQueueIndex = mNowSpeechIndex = 0;
        }

        List<SpeechSynthesizeBag> bags = new ArrayList<>();

        final int temp = mNowSpeechIndex + mThreshold + 1;
        int end = temp < mJus.size() ? temp : mJus.size();

        for (int i = mNowSpeechIndex; i < end; i++) {
            final Ju ju = mJus.get(i);
            final String s = mPageText.substring(ju.begin, ju.end);

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

        String getAid();

        int getNowChar();

        void setNowChar(int posi, boolean flush);

        int getFullChar();

        int[] getPageArrary();

        int getCJKChars();
    }

    public static class Ju {
        private static int SIZE = 50; //256;
        public int begin;
        public int end;

        Ju(int begin, int end) {
            this.begin = begin;
            this.end = end;
        }

        static void setSize(int size) {
            SIZE = size;
        }

        private static List<Ju> fenJu(String s) {
            Pattern regex = getFenjuRegex();

            List<Ju> ret = new ArrayList<>();

            int p = 0;

            while (true) {
                if (p + SIZE >= s.length()) {
                    Ju ju = new Ju(p, s.length());
                    ret.add(ju);

                    break;
                }

                String sub = s.substring(p, p + SIZE);
                Matcher m = regex.matcher(sub);

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

    private class PageManager {
        private int[] pageArray;
        private int currentPage, totalPage;
        private int currentBase;

        void initArticle() {
            pageArray = mArticle.getPageArrary();

            final int posi = mArticle.getNowChar();

            // currentPage
            int l = 0, m, r = pageArray.length;

            while (r - l > 1) {
                m = (l + r) / 2;

                final int temp = pageArray[m];
                if (posi < temp) {
                    r = m;
                } else {
                    l = m;
                }
            }

            if (posi < pageArray[l]) {
                currentPage = l;
            } else {
                currentPage = r;
            }

            // totalPage
            totalPage = pageArray.length;

            // current text
            currentBase = (currentPage == 0 ? 0 : pageArray[currentPage - 1]);
            mPageText = mText.substring(currentBase, pageArray[currentPage]);

            // 分句
            mJus = Ju.fenJu(mPageText);

            // 页内跳转
            int offset = posi - currentBase;

            // broadcast
            mLBM.sendBroadcast(mPageChangeIntent);

            if (mSpeechSynthesizer == null) {
                lazyInit();
            }
            setPagePosi(offset);
        }

        boolean toNextPage() {
            if (currentPage < totalPage - 1) {
                // current page
                currentPage += 1;

                currentBase = pageArray[currentPage - 1];
                // current text
                mPageText = mText.substring(currentBase, pageArray[currentPage]);

                // fen ju
                mJus = Ju.fenJu(mPageText);

                // broadcast
                mLBM.sendBroadcast(mPageChangeIntent);

                return true;
            } else {
                return false;
            }
        }

        // 当前的总位置
        int getNowFullPosi() {
            return currentBase + mJus.get(mNowSpeechIndex).begin;
        }

        int getTotalPage() {
            return totalPage;
        }

        int getCurrentPage() {
            return currentPage;
        }

        void jumpToPage(int page) {
            if (currentPage == page) {
                return;
            }

            currentPage = page;

            // current text
            currentBase = (currentPage == 0 ? 0 : pageArray[currentPage - 1]);
            mPageText = mText.substring(currentBase, pageArray[currentPage]);

            // 分句
            mJus = Ju.fenJu(mPageText);

            // broadcast
            mLBM.sendBroadcast(mPageChangeIntent);
        }

        void prevBottom() {
            if (currentPage < 1) {
                return;
            }

            currentPage -= 1;

            // current text
            currentBase = (currentPage == 0 ? 0 : pageArray[currentPage - 1]);
            mPageText = mText.substring(currentBase, pageArray[currentPage]);

            // 分句
            mJus = Ju.fenJu(mPageText);

            // broadcast
            mLBM.sendBroadcast(mPageChangeIntent);

            // 底部
            int idx = mJus.size() - 2;
            idx = (idx >= 0 ? idx : 0);
            mNowSpeechIndex = mNowQueueIndex = idx;
        }

        // 页内跳转
        void setPagePosi(int posi) {
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
                    playAction(false);

                    setEvent(PLAYING);
                    return;
                }
            }
        }
    }

    public class ArticleTtsBinder extends Binder {

        public boolean playArticle(IArticle article) {
            Log.i("ArticleTtsBinder", "playArticle");
            if (mArticle != null) {
                if (!mArticle.getAid().equals(article.getAid())) {
                    stop();
                } else {
                    play();
                    return true;
                }
            }

            mNowQueueIndex = mNowSpeechIndex = 0;
            mArticle = article;
            if (mArticle == null) {
                mJus = null;
                setEvent(EMPTY);
                return false;
            }

            mTitle = mArticle.getTitle();
            mText = mArticle.getText();
            if (mTitle == null || mText == null || mText.isEmpty()) {
                mArticle = null;
                mJus = null;

                setEvent(EMPTY);
                return false;
            }

            // 开始播放
            mPageManager.initArticle();

            return true;
        }

        public void play() {
            if (mArticle != null) {
                if (mArticle.getNowChar() == 0) {
                    jumpToPage(0);
                } else {
                    playAction(false);
                    setEvent(PLAYING);
                }
            }
        }

        public void stop() {
            if (mSpeechSynthesizer == null) {
                return;
            }

            if (mNowState == PLAYING || mNowState == PAUSING) {
                mSpeechSynthesizer.stop();
                setEvent(STOP);

                mArticle.setNowChar(mPageManager.getNowFullPosi(), true);
            }
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

        public String getFullProgressText() {
            if (mArticle == null) {
                return "";
            }

            int v = mPageManager.getNowFullPosi() * 100 / mArticle.getFullChar();
            return "" + v + "%";
        }

        public Object getArticle() {
            return mArticle;
        }

        public String getTitle() {
            return mTitle;
        }

        public String getPageText() {
            return mPageText;
        }

        @Nullable
        public Ju getNowJu() {
            if (mArticle == null || mNowState == EMPTY ||
                    mJus == null || mNowSpeechIndex >= mJus.size()) {
                return null;
            }

            return mJus.get(mNowSpeechIndex);
        }

        public int getState() {
            return mNowState;
        }

        public String getPageButtonText() {
            if (mPageManager.getTotalPage() == 0) {
                return "页";
            }

            return "" + (mPageManager.getCurrentPage() + 1) +
                    "/" + mPageManager.getTotalPage();
        }

        public int getCurrentPage() {
            return mPageManager.getCurrentPage();
        }

        public int getTotalPage() {
            return mPageManager.getTotalPage();
        }

        public int getCJKChars() {
            return mArticle.getCJKChars();
        }

        public void jumpToPage(int page) {
            if (mSpeechSynthesizer == null) {
                return;
            }

            // 停止
            if (mNowState == PLAYING || mNowState == PAUSING) {
                mSpeechSynthesizer.stop();
                setEvent(STOP);
            }

            // 跳转
            mPageManager.jumpToPage(page);

            // 播放
            playAction(true);
            setEvent(PLAYING);
        }

        public boolean prevBottom() {
            if (mSpeechSynthesizer == null ||
                    mPageManager.getCurrentPage() < 1) {
                return false;
            }

            // 停止
            if (mNowState == PLAYING || mNowState == PAUSING) {
                mSpeechSynthesizer.stop();
                setEvent(STOP);
            }

            // 跳转
            mPageManager.prevBottom();

            // 播放
            playAction(false);
            setEvent(PLAYING);

            return true;
        }

        public void backOne() {
            if (mSpeechSynthesizer == null) {
                return;
            }

            // 停止
            if (mNowState == PLAYING || mNowState == PAUSING) {
                mSpeechSynthesizer.stop();
                setEvent(STOP);
            }

            // 回退
            int idx = mNowSpeechIndex - 1;
            idx = (idx >= 0 ? idx : 0);
            mNowSpeechIndex = mNowQueueIndex = idx;

            // 播放
            playAction(false);
            setEvent(PLAYING);
        }

        public void setSetting() {
            TTSService.this.setSetting();
        }

        public void setPagePosi(int posi) {
            if (mSpeechSynthesizer == null) {
                return;
            }

            // 停止
            if (mNowState == PLAYING || mNowState == PAUSING) {
                mSpeechSynthesizer.stop();
                setEvent(STOP);
            }

            mPageManager.setPagePosi(posi);
        }
    }
}
