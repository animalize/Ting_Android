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

    // 清空一个目录的内容
    static void deleteFiles(File f) {
        for (String child : f.list()) {
            File temp = new File(f.getAbsolutePath() + "/" + child);

            if (temp.isDirectory()) {
                deleteFiles(temp);
                temp.delete();
            } else {
                temp.delete();
            }
        }
    }

    static int initialEnv(Context context, final int currentVer) {
        appContext = context.getApplicationContext();
        File fileDir = appContext.getFilesDir();

        File dataDir = new File(fileDir, SAMPLE_DIR_NAME);
        if (!dataDir.isDirectory()) {
            dataDir.mkdir();
        }
        ttsDataDir = dataDir.getAbsolutePath();

        if (currentVer < FILE_VER) {
            // 清空目录
            deleteFiles(dataDir);

            // 复制文件
            for (String fn : MODELS) {
                copyAssetsFile(fn);
            }
            copyAssetsFile(TEXT_MODEL_NAME);

            return FILE_VER;
        } else {
            return currentVer;
        }
    }

    static String getSpeechModelName(int idx) {
        if (ttsDataDir == null) {
            return null;
        }
        String fn;

        /*
            "度小宇(标准男声)", "度小美(标准女声)",
            "度逍遥(情感男声)", "度丫丫(情感儿童声)",

            "度博文(*情感男声)", "度小童(*情感儿童声)",
            "度小萌(*情感女声)", "度米朵(*情感儿童声)",
            "度小娇(*情感女声)"

            as 度丫丫
            f7 离线女声
            m15 离线男声
            yyjw 度逍遥
        */
        switch (idx) {
            case 0:
                fn = MODELS[2];
                break;
            case 1:
                fn = MODELS[1];
                break;
            case 2:
                fn = MODELS[3];
                break;
            case 3:
                fn = MODELS[0];
                break;

            case 4:
                fn = MODELS[2];
                break;
            case 5:
            case 7:
                fn = MODELS[0];
                break;
            case 6:
            case 8:
                fn = MODELS[1];
                break;
            default:
                fn = MODELS[1];
        }
        return ttsDataDir + "/" + fn;
    }

    static String getTextFileName() {
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
     */
    private static void copyFromAssetsToSdcard(String source,
                                               String dest) {
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = appContext.getResources().getAssets().open(source);
            fos = new FileOutputStream(dest);
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
