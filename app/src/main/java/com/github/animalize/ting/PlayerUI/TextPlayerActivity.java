package com.github.animalize.ting.PlayerUI;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

import com.github.animalize.ting.Data.Item;
import com.github.animalize.ting.Database.DataManager;
import com.github.animalize.ting.R;
import com.github.animalize.ting.TTS.TTSService;
import com.github.animalize.ting.TingTTS.TingTTSService;

public class TextPlayerActivity extends AppCompatActivity implements ServiceConnection {

    private DataManager dataManager = DataManager.getInstance();

    private PlayerTextWidget playerText;
    private PlayerPanelWidget playerPanel;

    public static void actionStart(Context context) {
        Intent i = new Intent(context, TextPlayerActivity.class);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_player);

        // 控件
        playerText = findViewById(R.id.player_text_view);
        playerPanel = findViewById(R.id.player_panel_view);

        Intent intent = new Intent(this, TingTTSService.class);
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
        playerText.onStop();
        playerPanel.onStop();

        super.onStop();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        TTSService.ArticleTtsBinder mBinder = (TTSService.ArticleTtsBinder) service;

        Item i = (Item) mBinder.getArticle();
        if (i == null) {
            return;
        }

        String mAid = i.getAid();
        if (mAid == null) {
            return;
        }

        playerPanel.setTTSBinder(mBinder);

        playerText.setTTSBinder(mBinder);
        playerText.setPlayerText(mBinder.getPageText());
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}
