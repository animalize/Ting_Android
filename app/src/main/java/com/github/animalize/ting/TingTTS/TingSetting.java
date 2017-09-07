package com.github.animalize.ting.TingTTS;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.animalize.ting.MyApplication;
import com.github.animalize.ting.R;
import com.github.animalize.ting.TTS.Setting;


public class TingSetting extends Setting {
    private static final String FILE_NAME = "tts_config";

    private static final String TAG_VOLUME = "volume";
    private static final String TAG_SPEED = "speed";
    private static final String TAG_PITCH = "pitch";
    private static final String TAG_SPEAKER = "speaker";
    private static final String TAG_MIXMODE = "mix_mode";

    private static final String TAG_THRESHOLD = "threshold";
    private static final String TAG_WINDOW = "window";
    private static final String TAG_FENJU = "fenju";

    private static final String TAG_MODELFILEVER = "model_file_ver";

    private static TingSetting singleTongle;

    private TingSetting() {
        super();
    }

    public static TingSetting getInstance() {
        if (singleTongle == null) {
            singleTongle = new TingSetting();
        }
        return singleTongle;
    }

    @Override
    public void loadSetting() {
        Context c = MyApplication.getContext();
        SharedPreferences sp = c.getSharedPreferences(
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

        // 文件
        temp = sp.getInt(TAG_MODELFILEVER, getmModelFileVer());
        setmModelFileVer(temp);
    }

    @Override
    public void saveSetting() {
        Context c = MyApplication.getContext();
        SharedPreferences.Editor editor = c.getSharedPreferences(
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

        // 模型文件
        editor.putInt(TAG_MODELFILEVER, getmModelFileVer());

        editor.apply();
    }

    @Override
    public String getTextModelFile() {
        return TTSInitializer.getTextModelName();
    }

    @Override
    public String getSpeechMaleModelFile() {
        return TTSInitializer.getSpeechMaleModelName();
    }

    @Override
    public String getSpeechFemaleModelFile() {
        return TTSInitializer.getSpeechFemaleModelName();
    }

    @Override
    public String getApiID() {
        return MyApplication.getContext().getString(R.string.APP_ID);
    }

    @Override
    public String getApiKey() {
        return MyApplication.getContext().getString(R.string.API_KEY);
    }

    @Override
    public String getSecretKey() {
        return MyApplication.getContext().getString(R.string.SECRET_KEY);
    }
}
