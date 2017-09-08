package com.github.animalize.ting.PlayerUI;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.animalize.ting.R;
import com.github.animalize.ting.TTS.TTSService;


public class PlayerTextWidget extends FrameLayout implements ViewTreeObserver.OnGlobalLayoutListener, View.OnClickListener {
    private TextView mTextView;
    private CheckBox mKeepScrollCheckBox;
    private boolean mKeepScroll = true;
    private Layout layout;

    private Spannable spannable;
    private TTSService.Ju ju;

    private TTSService.ArticleTtsBinder mBinder;
    private LocalBroadcastManager mLBM = LocalBroadcastManager.getInstance(getContext());
    private SpeechStartReciver mSpeechStartReciver = new SpeechStartReciver();

    public PlayerTextWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_player_text, this);

        mKeepScrollCheckBox = (CheckBox) findViewById(R.id.keep_scroll);
        mKeepScrollCheckBox.setOnClickListener(this);

        mTextView = (TextView) findViewById(R.id.text_view);
        mTextView.setMovementMethod(new ScrollingMovementMethod());
    }

    public void setPlayerText(String text) {
        spannable = new SpannableString(text);
        mTextView.setText(spannable);

        ViewTreeObserver vto = mTextView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(this);
    }

    private void setSelect(int begin, int end) {
        // 移除
        Object spansToRemove[] = spannable.getSpans(
                0, spannable.length(),
                Object.class);
        for (Object span : spansToRemove) {
            spannable.removeSpan(span);
        }

        // 新
        spannable.setSpan(
                new ForegroundColorSpan(Color.BLUE),
                begin, end,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        mTextView.setText(spannable);

        // 滚动
        if (mKeepScroll && layout != null) {
            final int line = layout.getLineForOffset(begin);
            int y = (line + 2) * mTextView.getLineHeight()
                    - mTextView.getHeight() / 2;

            mTextView.scrollTo(0, y >= 0 ? y : 0);
        }
    }

    public void onStart() {
        mLBM.registerReceiver(
                mSpeechStartReciver,
                TTSService.getSpeechStartIntentFilter());

        mSpeechStartReciver.onReceive(null, null);
    }

    public void onStop() {
        mLBM.unregisterReceiver(mSpeechStartReciver);
    }

    public void setTTSBinder(TTSService.ArticleTtsBinder binder) {
        mBinder = binder;

        if (spannable == null) {
            String s = mBinder.getText();
            if (s != null) {
                setPlayerText(s);
            }
        }

        mSpeechStartReciver.onReceive(null, null);
    }

    @Override
    public void onGlobalLayout() {
        layout = mTextView.getLayout();

        if (ju != null) {
            setSelect(ju.begin, ju.end);
            ju = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.keep_scroll:
                mKeepScroll = mKeepScrollCheckBox.isChecked();
                break;
        }
    }

    private class SpeechStartReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mBinder == null) {
                return;
            }

            ju = mBinder.getNowJu();
            if (ju != null && layout != null) {
                setSelect(ju.begin, ju.end);
                ju = null;
            }
        }
    }

}
