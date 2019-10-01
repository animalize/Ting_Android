package com.github.animalize.ting.PlayerUI;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.github.animalize.ting.R;
import com.github.animalize.ting.TTS.TTSService;


public class PlayerTextWidget
        extends FrameLayout
        implements ViewTreeObserver.OnGlobalLayoutListener, View.OnClickListener {
    private ScrollView mScroller;
    private TextView mTextView;
    private CheckBox mKeepScrollCheckBox;
    private boolean mKeepScroll = true;

    private String text;
    private Spannable spannable;
    private TTSService.Ju ju;
    private boolean doSetSelect = false;

    private TTSService.ArticleTtsBinder mBinder;
    private LocalBroadcastManager mLBM = LocalBroadcastManager.getInstance(getContext());
    private SpeechStartReciver mSpeechStartReciver = new SpeechStartReciver();
    private PageChangeReciver mPageChangeReciver = new PageChangeReciver();

    public PlayerTextWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_player_text, this);

        mScroller = findViewById(R.id.scroller);

        mKeepScrollCheckBox = findViewById(R.id.keep_scroll);
        mKeepScrollCheckBox.setOnClickListener(this);

        mTextView = findViewById(R.id.text_view);

        ViewTreeObserver vto = mTextView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(this);
    }

    public void setPlayerText(String newText) {
        doSetSelect = true;
        text = newText;

        spannable = new SpannableString(newText);
        mTextView.setText(spannable);
    }

    private void setSelect() {
        doSetSelect = false;

        // 移除
        Object[] spansToRemove = spannable.getSpans(
                0, spannable.length(),
                Object.class);
        for (Object span : spansToRemove) {
            spannable.removeSpan(span);
        }

        if (ju == null) {
            ju = mBinder.getNowJu();
            if (ju == null) {
                return;
            }
        }

        if (ju.end > spannable.length()) {
            return;
        }

        // 新
        spannable.setSpan(
                new ForegroundColorSpan(Color.BLUE),
                ju.begin, ju.end,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        mTextView.setText(spannable);

        // 滚动
        scrollText();
    }

    private void scrollText() {
        Layout layout = mTextView.getLayout();

        if (mKeepScroll && layout != null && ju != null) {
            final int line = layout.getLineForOffset(ju.begin);
            int y = (line + 2) * mTextView.getLineHeight()
                    - mScroller.getHeight() / 2;

            mScroller.scrollTo(0, y >= 0 ? y : 0);
        }
    }

    @Override
    public void onGlobalLayout() {
        if (doSetSelect && spannable != null) {
            setSelect();
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        doSetSelect = true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.keep_scroll:
                mKeepScroll = mKeepScrollCheckBox.isChecked();
                scrollText();
                break;
        }
    }

    public void onStart() {
        mLBM.registerReceiver(
                mPageChangeReciver,
                TTSService.getPageChangeIntentFilter());
        mPageChangeReciver.onReceive(null, null);

        mLBM.registerReceiver(
                mSpeechStartReciver,
                TTSService.getSpeechStartIntentFilter());
        mSpeechStartReciver.onReceive(null, null);
    }

    public void onStop() {
        mLBM.unregisterReceiver(mSpeechStartReciver);
        mLBM.unregisterReceiver(mPageChangeReciver);
    }

    public void setTTSBinder(TTSService.ArticleTtsBinder binder) {
        mBinder = binder;

        if (spannable == null) {
            String s = mBinder.getPageText();
            if (s != null) {
                setPlayerText(s);
            }
        }
    }

    private class SpeechStartReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mBinder == null) {
                return;
            }

            ju = mBinder.getNowJu();

            doSetSelect = true;
            setSelect();
        }
    }

    private class PageChangeReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mBinder == null) {
                return;
            }

            String s = mBinder.getPageText();
            if (s != null && !s.equals(text)) {
                setPlayerText(s);
            }
        }
    }
}
