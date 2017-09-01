package com.github.animalize.ting.TTS;

import android.content.Context;
import android.util.Log;

import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizeBag;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;

import java.util.ArrayList;
import java.util.List;


public class ArticleTTS implements SpeechSynthesizerListener {
    private static final int WINDOW = 2;

    private SpeechSynthesizer ss;

    private String title, text;

    private List<Ju> jus;
    private int nowJuIndex = 0;

    public ArticleTTS(Context context) {
        TTSHelper.initialEnv(context);

        ss = TTSHelper.initTTS(context, this);
    }

    public void setArticle(String title, String text) {
        this.title = title;
        this.text = text;

        jus = TTSUtils.fenJu(text);
    }

    public void play() {
        List<SpeechSynthesizeBag> bags = new ArrayList<>();

        int end = nowJuIndex + WINDOW < jus.size()
                ? nowJuIndex + WINDOW
                : jus.size();

        for (int i = nowJuIndex; i < end; i++) {
            final Ju ju = jus.get(i);
            final String s = text.substring(ju.begin, ju.end);

            SpeechSynthesizeBag bag = new SpeechSynthesizeBag();
            bag.setText(s);
            bag.setUtteranceId(String.valueOf(i));

            bags.add(bag);
        }

        nowJuIndex = end;
        ss.batchSpeak(bags);
    }

    @Override
    public void onSpeechStart(String s) {

    }

    @Override
    public void onSpeechProgressChanged(String s, int i) {

    }

    @Override
    public void onSpeechFinish(String s) {
        if (nowJuIndex < jus.size()) {
            final Ju ju = jus.get(nowJuIndex);
            final String temp = text.substring(ju.begin, ju.end);

            Log.i("onSpeechFinish: ", temp);

            SpeechSynthesizeBag bag = new SpeechSynthesizeBag();
            bag.setText(temp);
            bag.setUtteranceId(String.valueOf(nowJuIndex));

            ss.speak(bag);

            nowJuIndex += 1;
        }
    }

    @Override
    public void onError(String s, SpeechError speechError) {

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
}
