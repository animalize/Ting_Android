package com.github.animalize.ting.PlayerUI;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.animalize.ting.Data.Item;
import com.github.animalize.ting.Data.MyColors;
import com.github.animalize.ting.R;
import com.github.animalize.ting.TTS.TTSService;


public class PlayerPanelWidget extends LinearLayout implements View.OnClickListener {
    private TTSService.ArticleTtsBinder mBinder;

    private SeekBar mProgress;
    private Button mPageButton;
    private TextView mTitleText, mProgressText, mFullprogressText;
    private int fullLengh;

    private Button mPlay, mStop, mBackTwo;

    private LocalBroadcastManager mLBM = LocalBroadcastManager.getInstance(getContext());
    private SpeechEventReciver mSpeechEventReciver = new SpeechEventReciver();
    private SpeechStartReciver mSpeechStartReciver = new SpeechStartReciver();
    private PageChangeReciver mPageChangeReciver = new PageChangeReciver();

    public PlayerPanelWidget(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_player_panel, this);

        mFullprogressText = findViewById(R.id.fullprogress_text);
        mProgressText = findViewById(R.id.progress_text);

        mPageButton = findViewById(R.id.page_button);
        mPageButton.setOnClickListener(this);

        mTitleText = findViewById(R.id.title);

        mProgress = findViewById(R.id.progress);
        mProgress.setOnSeekBarChangeListener(new SeekBarListener());

        mPlay = findViewById(R.id.play_pause);
        mPlay.setOnClickListener(this);
        mStop = findViewById(R.id.stop);
        mStop.setOnClickListener(this);

        mBackTwo = findViewById(R.id.back_two);
        mBackTwo.setOnClickListener(this);
    }

    public void setTTSBinder(TTSService.ArticleTtsBinder binder) {
        mBinder = binder;

        freshUIofItem();
        mPageChangeReciver.onReceive(null, null);
        mSpeechEventReciver.onReceive(null, null);
        mSpeechStartReciver.onReceive(null, null);
    }

    @Override
    public void onClick(View v) {
        if (mBinder == null) {
            return;
        }

        switch (v.getId()) {
            case R.id.play_pause:
                int state = mBinder.getState();

                if (state == TTSService.PLAYING) {
                    mBinder.pause();
                } else if (state == TTSService.PAUSING) {
                    mBinder.resume();
                } else if (state == TTSService.STOP || state == TTSService.FINISHED) {
                    mBinder.play();
                }
                break;

            case R.id.stop:
                mBinder.stop();
                break;

            case R.id.page_button:
                if (mBinder == null) {
                    return;
                }
                PageJumpDialog d = new PageJumpDialog(getContext());
                d.show();
                break;

            case R.id.back_two:
                mBinder.backOne();
                break;
        }
    }

    public void onStart() {
        mLBM.registerReceiver(
                mPageChangeReciver,
                TTSService.getPageChangeIntentFilter());
        mLBM.registerReceiver(
                mSpeechEventReciver,
                TTSService.getSpeechEventIntentFilter());
        mLBM.registerReceiver(
                mSpeechStartReciver,
                TTSService.getSpeechStartIntentFilter());

        mPageChangeReciver.onReceive(null, null);
        mSpeechEventReciver.onReceive(null, null);
        mSpeechStartReciver.onReceive(null, null);
    }

    public void onStop() {
        mLBM.unregisterReceiver(mSpeechEventReciver);
        mLBM.unregisterReceiver(mSpeechStartReciver);
        mLBM.unregisterReceiver(mPageChangeReciver);
    }

    private void freshUIofItem() {
        Item item = (Item) mBinder.getArticle();
        if (item == null) {
            return;
        }

        mTitleText.setText(mBinder.getTitle());

        fullLengh = mBinder.getPageText().length();
        mProgress.setMax(fullLengh > 0 ? fullLengh - 1 : fullLengh);
    }

    private class SpeechStartReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mBinder == null) {
                return;
            }

            if (mBinder.getState() == TTSService.FINISHED) {
                mProgress.setProgress(mProgress.getMax());
                mProgressText.setText("100% ");
                return;
            }

            if (mFullprogressText != null) {
                mFullprogressText.setText(mBinder.getFullProgressText());
            }

            TTSService.Ju ju = mBinder.getNowJu();
            if (ju != null) {
                mProgress.setProgress(ju.begin);

                int v = ju.begin * 100 / fullLengh;
                mProgressText.setText("" + v + "% ");
            }
        }
    }

    private class SpeechEventReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mBinder == null) {
                return;
            }

            int state = mBinder.getState();

            String play;
            boolean enablePlay, enableStop;

            switch (state) {
                case TTSService.PLAYING:
                    play = "暂停";
                    enablePlay = true;
                    enableStop = true;

                    freshUIofItem();
                    break;

                case TTSService.PAUSING:
                    play = "恢复";
                    enablePlay = true;
                    enableStop = true;
                    break;

                case TTSService.STOP:
                    play = "播放";
                    enablePlay = true;
                    enableStop = false;

                    mProgress.setProgress(0);
                    mProgressText.setText("0% ");
                    break;

                case TTSService.FINISHED:
                    play = "播放";
                    enablePlay = true;
                    enableStop = false;

                    mProgress.setProgress(mProgress.getMax());
                    mProgressText.setText("100% ");
                    break;

                case TTSService.EMPTY:
                    play = "播放";
                    enablePlay = false;
                    enableStop = false;
                    break;

                default:
                    play = "播放";
                    enablePlay = true;
                    enableStop = true;
            }

            mPlay.setEnabled(enablePlay);
            mPlay.setText(play);

            mStop.setEnabled(enableStop);
        }
    }

    private class SeekBarListener implements SeekBar.OnSeekBarChangeListener {
        private int posi = 0;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                posi = progress;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (mBinder != null && mBinder.getArticle() != null) {
                mBinder.setPagePosi(posi);
            }
        }
    }

    private class PageChangeReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mBinder == null) {
                return;
            }

            String s = mBinder.getPageButtonText();
            mPageButton.setText(s);
        }
    }

    public class PageJumpDialog extends Dialog implements OnClickListener {
        private PageChangeReciver1 mPageChangeReciver = new PageChangeReciver1();
        private RecyclerView mPageList;
        private PageAdapter mPageAdapter;

        public PageJumpDialog(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.dialog_pagejump);

            Button bt = findViewById(R.id.cancel_button);
            bt.setOnClickListener(this);

            bt = findViewById(R.id.first_page_button);
            bt.setOnClickListener(this);

            bt = findViewById(R.id.prev_bottom_button);
            bt.setOnClickListener(this);

            TextView tv = findViewById(R.id.page_info_text);
            tv.setText("  (" + mBinder.getCJKChars() +
                    "汉字，" + mBinder.getTotalPage() + "页)");

            mPageList = findViewById(R.id.pages_list);

            // 布局管理
            GridLayoutManager lm = new GridLayoutManager(getContext(), 3);
            mPageList.setLayoutManager(lm);
            // adapter
            mPageAdapter = new PageAdapter();
            mPageList.setAdapter(mPageAdapter);

            // 跳转
            mPageList.scrollToPosition(mBinder.getCurrentPage());
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.cancel_button:
                    dismiss();
                    break;

                case R.id.first_page_button:
                    if (mBinder.getCurrentPage() > 0) {
                        mBinder.jumpToPage(0);
                        dismiss();
                    }
                    break;

                case R.id.prev_bottom_button:
                    if (mBinder.prevBottom()) {
                        dismiss();
                    }
                    break;
            }
        }

        @Override
        protected void onStart() {
            super.onStart();
            mLBM.registerReceiver(
                    mPageChangeReciver,
                    TTSService.getPageChangeIntentFilter());
        }

        @Override
        protected void onStop() {
            mLBM.unregisterReceiver(mPageChangeReciver);
            super.onStop();
        }

        private class PageChangeReciver1 extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                mPageAdapter.notifyDataSetChanged();
                mPageList.scrollToPosition(mBinder.getCurrentPage());
            }
        }
    }

    class PageHolder extends RecyclerView.ViewHolder {
        public TextView item;
        private int pageNum;

        public PageHolder(View itemView) {
            super(itemView);

            item = itemView.findViewById(R.id.page_text);
        }

        public int getPageNum() {
            return pageNum;
        }

        public void setPageNum(int pageNum) {
            this.pageNum = pageNum;
        }
    }

    public class PageAdapter
            extends RecyclerView.Adapter<PageHolder> {

        @Override
        public PageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.page_item, parent, false);
            final PageHolder holder = new PageHolder(v);

            holder.item.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int page = holder.getPageNum();
                    if (page != mBinder.getCurrentPage()) {
                        mBinder.jumpToPage(page);
                    }
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(PageHolder holder, int position) {
            int color = (position != mBinder.getCurrentPage()) ?
                    Color.BLACK :
                    MyColors.current;

            holder.item.setText("" + (position + 1));
            holder.item.setTextColor(color);

            holder.setPageNum(position);
        }

        @Override
        public int getItemCount() {
            return mBinder.getTotalPage();
        }
    }
}
