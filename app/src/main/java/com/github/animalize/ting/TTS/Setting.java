package com.github.animalize.ting.TTS;


import android.content.Context;

import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;

public abstract class Setting {
    public static final int MIX_MODE_DEFAULT = 0;
    public static final int MIX_MODE_HIGH_SPEED_NETWORK = 1;
    public static final int MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI = 2;
    public static final int MIX_MODE_HIGH_SPEED_SYNTHESIZE = 3;

    public static final int THRESHOLD_DEFAULT = 2;
    public static final int THRESHOLD_MIN = 1;
    public static final int THRESHOLD_MAX = 10;

    public static final int WINDOW_DEFAULT = 6;
    public static final int WINDOW_MIN = 1;
    public static final int WINDOW_MAX = 10;

    public static final int FENJU_DEFAULT = 80;
    public static final int FENJU_MIN = 32;
    public static final int FENJU_MAX = 500;

    private static final String[] SPEAKER_NAMES = {
            "普通女声", "普通男声", "特别男声", "情感男声", "情感儿童声"};
    private static String[] MIXMODE_NAMES = {
            "A：wifi在线，非wifi离线",
            "B：wifi,3G,4G在线，其它离线",
            "同A，在网速慢时自动离线",
            "同B，在网速慢时自动离线"
    };
    // 音量，范围[0-9]
    private int mVolume = 5;
    // 语速，范围[0-9]
    private int mSpeed = 5;
    // 语调，范围[0-9]
    private int mPitch = 5;
    /*
    * 0 (普通女声)
    * 1 (普通男声)
    * 2 (特别男声)
    * 3 (情感男声<度逍遥>)
    * 4 (情感儿童声<度丫丫>)
    * */
    private int mSpeaker = 0;
    /*
    * MIX_MODE_DEFAULT
    * (mix模式下，wifi使用在线合成，非wifi使用离线合成)
    * MIX_MODE_HIGH_SPEED_NETWORK
    * (mix模式下，wifi,4G,3G使用在线合成，其他使用离线合成)
    * MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI
    * (mix模式下，仅wifi使用在线合成,返回速度如果慢（超时，一般为1.2秒）直接切换离线，适用于仅WIFI网络环境较差的情况)
    * MIX_MODE_HIGH_SPEED_SYNTHESIZE
    * (mix模式下，在线返回速度如果慢（超时，一般为1.2秒）直接切换离线，适用于网络环境较差的情况)
 * */
    private int mMixMode = MIX_MODE_DEFAULT;

    private int mThreshold = THRESHOLD_DEFAULT;
    private int mWindow = WINDOW_DEFAULT;
    private int mFenJu = FENJU_DEFAULT;

    public Setting() {
        loadSetting();
    }

    public static String[] getSpeakerNameList() {
        return SPEAKER_NAMES;
    }

    public static String[] getMixModeNameList() {
        return MIXMODE_NAMES;
    }

    public int getmFenJu() {
        return mFenJu;
    }

    public void setmFenJu(int mFenJu) {
        if (FENJU_MIN <= mFenJu && mFenJu <= FENJU_MAX) {
            this.mFenJu = mFenJu;
        } else {
            this.mFenJu = FENJU_DEFAULT;
        }
    }

    public int getmThreshold() {
        return mThreshold;
    }

    public void setmThreshold(int mThreshold) {
        if (THRESHOLD_MIN <= mThreshold && mThreshold <= THRESHOLD_MAX) {
            this.mThreshold = mThreshold;
        } else {
            this.mThreshold = THRESHOLD_DEFAULT;
        }
    }

    public int getmWindow() {
        return mWindow;
    }

    public void setmWindow(int mWindow) {
        if (WINDOW_MIN <= mWindow && mWindow <= WINDOW_MAX) {
            this.mWindow = mWindow;
        } else {
            this.mWindow = WINDOW_DEFAULT;
        }
    }

    public abstract void loadSetting();

    public abstract void saveSetting();

    public abstract String getTextModelFile();

    public abstract String getSpeechMaleModelFile();

    public abstract String getSpeechFemaleModelFile();

    public abstract String getApiID();

    public abstract String getApiKey();

    public abstract String getSecretKey();

    public int getmVolume() {
        return mVolume;
    }

    public void setmVolume(int mVolume) {
        if (mVolume < 0) {
            this.mVolume = 0;
        } else if (mVolume > 9) {
            this.mVolume = 9;
        } else {
            this.mVolume = mVolume;
        }
    }

    public int getmSpeed() {
        return mSpeed;
    }

    public void setmSpeed(int mSpeed) {
        if (mSpeed < 0) {
            this.mSpeed = 0;
        } else if (mSpeed > 9) {
            this.mSpeed = 9;
        } else {
            this.mSpeed = mSpeed;
        }
    }

    public int getmPitch() {
        return mPitch;
    }

    public void setmPitch(int mPitch) {
        if (mPitch < 0) {
            this.mPitch = 0;
        } else if (mPitch > 9) {
            this.mPitch = 9;
        } else {
            this.mPitch = mPitch;
        }
    }

    public int getmSpeaker() {
        return mSpeaker;
    }

    public void setmSpeaker(int mSpeaker) {
        if (mSpeaker < 0 || mSpeaker > 4) {
            this.mSpeaker = 0;
        } else {
            this.mSpeaker = mSpeaker;
        }
    }

    public int getmMixMode() {
        return mMixMode;
    }

    public void setmMixMode(int mMixMode) {
        if (mMixMode < 0 || mMixMode > 3) {
            this.mMixMode = 0;
        } else {
            this.mMixMode = mMixMode;
        }
    }

    public SpeechSynthesizer initTTS(Context context,
                                     SpeechSynthesizerListener speechSynthesizerListener) {

        SpeechSynthesizer speechSynthesizer = SpeechSynthesizer.getInstance();
        speechSynthesizer.setContext(context);
        speechSynthesizer.setSpeechSynthesizerListener(speechSynthesizerListener);

        speechSynthesizer.setAppId(getApiID());
        speechSynthesizer.setApiKey(getApiKey(), getSecretKey());

        //AuthInfo authInfo = speechSynthesizer.auth(TtsMode.MIX);
        speechSynthesizer.initTts(TtsMode.MIX);

        return speechSynthesizer;
    }

    public void setSettingToSpeechSynthesizer(SpeechSynthesizer ss) {
        ss.setParam(
                SpeechSynthesizer.PARAM_VOLUME,
                "" + getmVolume());

        ss.setParam(
                SpeechSynthesizer.PARAM_SPEED,
                "" + getmSpeed());

        ss.setParam(
                SpeechSynthesizer.PARAM_PITCH,
                "" + getmPitch());

        ss.setParam(
                SpeechSynthesizer.PARAM_SPEAKER,
                "" + getmSpeaker());

        // Mix的模式
        String mixMode;
        switch (getmMixMode()) {
            case MIX_MODE_DEFAULT:
                mixMode = SpeechSynthesizer.MIX_MODE_DEFAULT;
                break;

            case MIX_MODE_HIGH_SPEED_NETWORK:
                mixMode = SpeechSynthesizer.MIX_MODE_HIGH_SPEED_NETWORK;
                break;

            case MIX_MODE_HIGH_SPEED_SYNTHESIZE:
                mixMode = SpeechSynthesizer.MIX_MODE_HIGH_SPEED_SYNTHESIZE;
                break;

            case MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI:
                mixMode = SpeechSynthesizer.MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI;
                break;

            default:
                mixMode = SpeechSynthesizer.MIX_MODE_DEFAULT;
        }
        ss.setParam(SpeechSynthesizer.PARAM_MIX_MODE, mixMode);

        // 模型文件
        ss.setParam(
                SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE,
                getTextModelFile());

        int temp = getmSpeaker();
        String fn;
        if (0 < temp && temp < 4) {
            fn = getSpeechMaleModelFile();
        } else {
            fn = getSpeechFemaleModelFile();
        }
        ss.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, fn);

        // 离线模型
        ss.loadModel(fn, getTextModelFile());
    }
}