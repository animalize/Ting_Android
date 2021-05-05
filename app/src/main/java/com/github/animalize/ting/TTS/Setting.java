package com.github.animalize.ting.TTS;


import android.content.Context;
import android.content.SharedPreferences;

import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;

public abstract class Setting {
    public static final int THRESHOLD_MIN = 1;
    public static final int THRESHOLD_MAX = 10;
    public static final int WINDOW_MIN = 1;
    public static final int WINDOW_MAX = 10;
    public static final int FENJU_MIN = 32;
    public static final int FENJU_MAX = 60;
    // 参数部分
    private static final int MIX_MODE_DEFAULT = 0;
    private static final int MIX_MODE_HIGH_SPEED_NETWORK = 1;
    private static final int MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI = 2;
    private static final int MIX_MODE_HIGH_SPEED_SYNTHESIZE = 3;
    private static final int THRESHOLD_DEFAULT = 2;
    private static final int WINDOW_DEFAULT = 6;
    private static final int FENJU_DEFAULT = 60;
    private static final String TTS_VER = "";
    // 保存部分
    private static final String FILE_NAME = "tts_config";
    private static final String TAG_VOLUME = "volume";
    private static final String TAG_SPEED = "speed";
    private static final String TAG_PITCH = "pitch";
    private static final String TAG_SPEAKER = "speaker";
    private static final String TAG_MIXMODE = "mix_mode";
    private static final String TAG_THRESHOLD = "threshold";
    private static final String TAG_WINDOW = "window";
    private static final String TAG_FENJU = "fenju";
    private static final String TAG_TTS_VER = "tts_ver";
    private static final String[] SPEAKER_NAMES = {
            "普通女声", "普通男声", "特别男声",
            "度逍遥(情感男声)", "度丫丫(情感儿童声)",
            "度博文(情感男声)", "度小童(情感儿童声)",
            "度小萌(情感女声)", "度米朵(情感儿童声)",
            "度小娇(情感女声)"};
    private static final int[] SPEAKER_VALUES = {
            0, 1, 2,
            3, 4,
            106, 110,
            111, 103,
            5};
    private static String[] MIXMODE_NAMES = {
            "A：wifi在线，非wifi或6秒超时离线",
            "B：wifi,3G,4G在线，其它或6秒超时离线",
            "同A，1.2秒超时离线",
            "同B，1.2秒超时离线"
    };
    // 音量，范围[0-15]
    private int mVolume = 5;
    // 语速，范围[0-15]
    private int mSpeed = 5;
    // 语调，范围[0-15]
    private int mPitch = 5;
    /* SPEAKER_NAMES的索引 */
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

    // 服务的参数
    private int mThreshold = THRESHOLD_DEFAULT;
    private int mWindow = WINDOW_DEFAULT;
    private int mFenJu = FENJU_DEFAULT;

    // TTS版本
    private String mTTSVersion = TTS_VER;

    public Setting(Context context) {
        loadSetting(context);
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

    public abstract String getApiID();

    public abstract String getApiKey();

    public abstract String getSecretKey();

    public int getmVolume() {
        return mVolume;
    }

    public void setmVolume(int mVolume) {
        if (mVolume < 0) {
            this.mVolume = 0;
        } else if (mVolume > 15) {
            this.mVolume = 15;
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
        } else if (mSpeed > 15) {
            this.mSpeed = 15;
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
        } else if (mPitch > 15) {
            this.mPitch = 15;
        } else {
            this.mPitch = mPitch;
        }
    }

    public int getmSpeaker() {
        return mSpeaker;
    }

    public void setmSpeaker(int mSpeaker) {
        if (mSpeaker >= 0 && mSpeaker < SPEAKER_VALUES.length) {
            this.mSpeaker = mSpeaker;
        } else {
            this.mSpeaker = 0;
        }
    }

    public int getSpeakerValue() {
        if (mSpeaker >= 0 && mSpeaker < SPEAKER_VALUES.length) {
            return SPEAKER_VALUES[mSpeaker];
        } else {
            return SPEAKER_VALUES[0];
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

    public String getmTTSVersion() {
        return mTTSVersion;
    }

    public void setmTTSVersion(String mTTSVersion) {
        this.mTTSVersion = mTTSVersion;
    }

    SpeechSynthesizer initTTS(Context context,
                              SpeechSynthesizerListener speechSynthesizerListener) {

        SpeechSynthesizer ss = SpeechSynthesizer.getInstance();
        ss.setContext(context);
        ss.setSpeechSynthesizerListener(speechSynthesizerListener);

        // 设置离线授权所需要的AppId，关系到离线合成是否可用
        ss.setAppId(getApiID());
        // 设置在线授权时需要用到的apiKey和secretKey，关系到在线合成是否可用
        ss.setApiKey(getApiKey(), getSecretKey());

        //AuthInfo authInfo = speechSynthesizer.auth(TtsMode.MIX);
        ss.initTts(TtsMode.ONLINE);

//        // 不使用压缩，彻底不使用so文件
//        ss.setParam(SpeechSynthesizer.PARAM_AUDIO_ENCODE,
//                    SpeechSynthesizer.AUDIO_ENCODE_PCM);
//        ss.setParam(SpeechSynthesizer.PARAM_AUDIO_RATE,
//                    SpeechSynthesizer.AUDIO_BITRATE_PCM);

        return ss;
    }

    void setSettingToSpeechSynthesizer(SpeechSynthesizer ss,
                                       Context context) {
        ss.setContext(context);

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
                "" + getSpeakerValue());

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

        // 离线模型
//        final int temp = getmSpeaker();
//        ss.loadModel(getModelFileName(temp), getTextFileName());
    }

    private void loadSetting(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                FILE_NAME,
                Context.MODE_PRIVATE);

        int temp;

        // 引擎
        temp = sp.getInt(TAG_VOLUME, getmVolume());
        setmVolume(temp);

        temp = sp.getInt(TAG_SPEED, getmSpeed());
        setmSpeed(temp);

        temp = sp.getInt(TAG_PITCH, getmPitch());
        setmPitch(temp);

        temp = sp.getInt(TAG_SPEAKER, getmSpeaker());
        setmSpeaker(temp);

        temp = sp.getInt(TAG_MIXMODE, getmMixMode());
        setmMixMode(temp);

        // 服务
        temp = sp.getInt(TAG_THRESHOLD, getmThreshold());
        setmThreshold(temp);

        temp = sp.getInt(TAG_WINDOW, getmWindow());
        setmWindow(temp);

        temp = sp.getInt(TAG_FENJU, getmFenJu());
        setmFenJu(temp);

        // tts版本
        String ttsVer = sp.getString(TAG_TTS_VER, getmTTSVersion());
        setmTTSVersion(ttsVer);
    }

    public void saveSetting(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(
                FILE_NAME,
                Context.MODE_PRIVATE).edit();

        // 引擎
        editor.putInt(TAG_VOLUME, getmVolume());
        editor.putInt(TAG_SPEED, getmSpeed());
        editor.putInt(TAG_PITCH, getmPitch());
        editor.putInt(TAG_SPEAKER, getmSpeaker());
        editor.putInt(TAG_MIXMODE, getmMixMode());

        // 服务
        editor.putInt(TAG_THRESHOLD, getmThreshold());
        editor.putInt(TAG_WINDOW, getmWindow());
        editor.putInt(TAG_FENJU, getmFenJu());

        // TTS版本
        editor.putString(TAG_TTS_VER, getmTTSVersion());

        editor.apply();
    }
}