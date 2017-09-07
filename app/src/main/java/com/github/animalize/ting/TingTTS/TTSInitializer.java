package com.github.animalize.ting.TingTTS;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;


public class TTSInitializer {
    private static final int FILE_VER = 2;

    private static final String SAMPLE_DIR_NAME = "baiduTTS";

    private static final String SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female.dat";
    private static final String SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male.dat";
    private static final String TEXT_MODEL_NAME = "bd_etts_text.dat";

    private static Context appContext;
    private static String ttsDataDir;

    public static int initialEnv(Context context, int currentVer) {
        appContext = context.getApplicationContext();
        File fileDir = appContext.getFilesDir();

        File dataDir = new File(fileDir, SAMPLE_DIR_NAME);
        if (!dataDir.isDirectory()) {
            dataDir.mkdir();
        }
        ttsDataDir = dataDir.getAbsolutePath();

        if (currentVer < FILE_VER) {
            copyFromAssetsToSdcard(
                    SPEECH_FEMALE_MODEL_NAME,
                    getSpeechFemaleModelName());
            copyFromAssetsToSdcard(
                    SPEECH_MALE_MODEL_NAME,
                    getSpeechMaleModelName());
            copyFromAssetsToSdcard(
                    TEXT_MODEL_NAME,
                    getTextModelName());
        }

        return FILE_VER;
    }

    public static String getTextModelName() {
        if (ttsDataDir == null) {
            return null;
        }
        return ttsDataDir + "/" + TEXT_MODEL_NAME;
    }

    public static String getSpeechMaleModelName() {
        if (ttsDataDir == null) {
            return null;
        }
        return ttsDataDir + "/" + SPEECH_MALE_MODEL_NAME;
    }

    public static String getSpeechFemaleModelName() {
        if (ttsDataDir == null) {
            return null;
        }
        return ttsDataDir + "/" + SPEECH_FEMALE_MODEL_NAME;
    }

    /**
     * 将sample工程需要的资源文件拷贝到SD卡中使用（授权文件为临时授权文件，请注册正式授权）
     *
     * @param source
     * @param dest
     */
    private static void copyFromAssetsToSdcard(String source,
                                               String dest) {
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = appContext.getResources().getAssets().open(source);
            String path = dest;
            fos = new FileOutputStream(path);
            byte[] buffer = new byte[1024];
            int size;
            while ((size = is.read(buffer, 0, 1024)) >= 0) {
                fos.write(buffer, 0, size);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
