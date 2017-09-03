package com.github.animalize.ting.PlayerUI;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.animalize.ting.Database.DataManager;
import com.github.animalize.ting.R;
import com.github.animalize.ting.TTS.ArticleTtsService;
import com.github.animalize.ting.TTS.Ju;

public class TextPlayerActivity extends AppCompatActivity implements ServiceConnection {

    private LocalBroadcastManager localBroadcastManager;
    private OnSpeechStartReciver onSpeechStartReciver;

    private DataManager dataManager = DataManager.getInstance();
    private String mTitle, mText;

    private TextView mTitleTextView;
    private PlayerTextWidget playerText;
    private PlayerPanelWidget playerPanel;

    private ArticleTtsService.ArticleTtsBinder mBinder;

    public static void actionStart(Context context, String aid) {
        Intent i = new Intent(context, TextPlayerActivity.class);
        i.putExtra("aid", aid);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_player);

        // intent
        Intent intent = getIntent();
        String aid = intent.getStringExtra("aid");

        mTitleTextView = (TextView) findViewById(R.id.title);
        playerText = (PlayerTextWidget) findViewById(R.id.player_text_view);
        playerPanel = (PlayerPanelWidget) findViewById(R.id.player_panel_view);

        // 读取文章
        mTitle = dataManager.getItemByAid(aid).getTitle();
        mTitleTextView.setText(mTitle);

        mText = dataManager.readArticleByAid(aid);
        playerText.setPlayerText(mText);

        // 广播接收
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        onSpeechStartReciver = new OnSpeechStartReciver();
        localBroadcastManager.registerReceiver(
                onSpeechStartReciver,
                onSpeechStartReciver.getIntentFilter());

        intent = new Intent(this, ArticleTtsService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        localBroadcastManager.unregisterReceiver(onSpeechStartReciver);

        unbindService(this);

        super.onDestroy();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mBinder = (ArticleTtsService.ArticleTtsBinder) service;
        mBinder.setArticle(mTitle, mText);

        playerPanel.setTTSBinder(mBinder);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    private class OnSpeechStartReciver extends BroadcastReceiver {

        public IntentFilter getIntentFilter() {
            return new IntentFilter("SpeechStart");
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Ju ju = mBinder.getNowJu();
            playerText.setSelect(ju.begin, ju.end);
        }
    }

    private class OnEventReciver extends BroadcastReceiver {

        public IntentFilter getIntentFilter() {
            return new IntentFilter("TTSEvent");
        }

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }
}
