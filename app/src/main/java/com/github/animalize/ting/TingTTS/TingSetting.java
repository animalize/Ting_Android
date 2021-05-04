package com.github.animalize.ting.TingTTS;


import android.content.Context;

import com.github.animalize.ting.MyApplication;
import com.github.animalize.ting.R;
import com.github.animalize.ting.TTS.Setting;


public class TingSetting extends Setting {
    private static TingSetting singleTongle;

    private TingSetting(Context context) {
        super(context);
    }

    public static TingSetting getInstance(Context context) {
        if (singleTongle == null) {
            singleTongle = new TingSetting(context);
        }
        return singleTongle;
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
