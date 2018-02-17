package com.github.animalize.ting.TingTTS;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;


public class TTSInitializer {
    private static final int FILE_VER = 4;

    private static final String SAMPLE_DIR_NAME = "baiduTTS";

    private static final String[] MODELS = {
            "bd_etts_common_speech_as_mand_eng_high_am_v3.0.0_20170516.dat",
            "bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat",
            "bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat",
            "bd_etts_common_speech_yyjw_mand_eng_high_am-mix_v3.0.0_20170512.dat"};
    private static final String TEXT_MODEL_NAME = "bd_etts_text.dat";

    private static Context appContext;
    private static String ttsDataDir;

    public static int initialEnv(Context context, final int currentVer) {
        appContext = context.getApplicationContext();
        File fileDir = appContext.getFilesDir();

        File dataDir = new File(fileDir, SAMPLE_DIR_NAME);
        if (!dataDir.isDirectory()) {
            dataDir.mkdir();
        }
        ttsDataDir = dataDir.getAbsolutePath();

        if (currentVer < FILE_VER) {
            copyAssetsFile(MODELS[0]);
            copyAssetsFile(MODELS[1]);
            copyAssetsFile(MODELS[2]);
            copyAssetsFile(MODELS[3]);

            copyFromAssetsToSdcard(
                    TEXT_MODEL_NAME,
                    getTextModelName());
            return FILE_VER;
        } else {
            return currentVer;
        }
    }

    public static String getSpeechModelName(int idx) {
        if (ttsDataDir == null) {
            return null;
        }
        String fn;

        /*
        m15 离线男声
        f7 离线女声
        yyjw 度逍遥
        as 度丫丫
        */
        switch (idx) {
            case 0: // 普通女声
                fn = MODELS[1];
                break;
            case 1: // 普通男声
            case 2: // 特别男声
                fn = MODELS[2];
                break;
            case 3: // 情感男声<度逍遥>
                fn = MODELS[3];
                break;
            case 4: // 情感儿童声<度丫丫>
                fn = MODELS[0];
                break;
            default:
                fn = MODELS[2];
        }
        return ttsDataDir + "/" + fn;
    }

    public static String getTextModelName() {
        if (ttsDataDir == null) {
            return null;
        }
        return ttsDataDir + "/" + TEXT_MODEL_NAME;
    }

    private static void copyAssetsFile(String sourceFilename) {
        if (ttsDataDir == null) {
            return;
        }
        String destFilename = ttsDataDir + "/" + sourceFilename;
        copyFromAssetsToSdcard(sourceFilename, destFilename);
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
