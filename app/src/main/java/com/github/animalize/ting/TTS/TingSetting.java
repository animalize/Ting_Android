package com.github.animalize.ting.TTS;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.animalize.ting.MyApplication;


public class TingSetting extends Setting {
    private static final String FILE_NAME = "tts_config";

    private static final String TAG_VOLUME = "volume";
    private static final String TAG_SPEED = "speed";
    private static final String TAG_PITCH = "pitch";
    private static final String TAG_SPEAKER = "speaker";
    private static final String TAG_MIXMODE = "mix_mode";

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
    }

    @Override
    public void saveSetting() {
        Context c = MyApplication.getContext();
        SharedPreferences.Editor editor = c.getSharedPreferences(
                FILE_NAME,
                Context.MODE_PRIVATE).edit();

        editor.putInt(TAG_VOLUME, getmVolume());
        editor.putInt(TAG_SPEED, getmSpeed());
        editor.putInt(TAG_PITCH, getmPitch());
        editor.putInt(TAG_SPEAKER, getmSpeaker());
        editor.putInt(TAG_MIXMODE, getmMixMode());

        editor.apply();
    }

    @Override
    public String getTextModelFile() {
        return TTSInitializer.getTextModelName();
    }

    @Override
    public String getSpeechModelFile() {
        int temp = getmSpeaker();
        if (temp == 0 || temp == 4) {
            return TTSInitializer.getSpeechFemaleModelName();
        } else {
            return TTSInitializer.getSpeechMaleModelName();
        }
    }
}
