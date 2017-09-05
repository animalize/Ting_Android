package com.github.animalize.ting.PlayerUI;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import com.github.animalize.ting.Data.Item;
import com.github.animalize.ting.Database.DataManager;
import com.github.animalize.ting.R;
import com.github.animalize.ting.TTS.TTSService;

public class TextPlayerActivity extends AppCompatActivity implements ServiceConnection {

    private LocalBroadcastManager mLBM;

    private DataManager dataManager = DataManager.getInstance();
    private String mAid;

    private PlayerTextWidget playerText;
    private PlayerPanelWidget playerPanel;

    private TTSService.ArticleTtsBinder mBinder;

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
        mAid = intent.getStringExtra("aid");

        // 控件
        playerText = (PlayerTextWidget) findViewById(R.id.player_text_view);
        playerPanel = (PlayerPanelWidget) findViewById(R.id.player_panel_view);

        intent = new Intent(this, TTSService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        unbindService(this);

        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();

        playerPanel.onStart();
        playerText.onStart();
    }

    @Override
    protected void onStop() {
        playerPanel.onStop();
        playerText.onStop();

        super.onStop();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mBinder = (TTSService.ArticleTtsBinder) service;

        Item item = dataManager.getItemByAid(mAid);
        mBinder.setArticle(item);

        playerPanel.setTTSBinder(mBinder);

        playerText.setTTSBinder(mBinder);
        playerText.setPlayerText(mBinder.getText());
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}
