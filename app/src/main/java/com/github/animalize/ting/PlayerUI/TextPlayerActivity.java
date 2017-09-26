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
    private String mAid;
    private boolean mAutoPlay = false;

    private PlayerTextWidget playerText;
    private PlayerPanelWidget playerPanel;

    private TTSService.ArticleTtsBinder mBinder;

    public static void actionStart(Context context, String aid, boolean autoPlay) {
        Intent i = new Intent(context, TextPlayerActivity.class);
        i.putExtra("aid", aid);
        i.putExtra("auto_play", autoPlay);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_player);

        // intent
        Intent intent = getIntent();
        mAid = intent.getStringExtra("aid");
        mAutoPlay = intent.getBooleanExtra("auto_play", false);

        // 控件
        playerText = (PlayerTextWidget) findViewById(R.id.player_text_view);
        playerPanel = (PlayerPanelWidget) findViewById(R.id.player_panel_view);

        intent = new Intent(this, TingTTSService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        unbindService(this);

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        playerPanel.onResume();
        playerText.onResume();
    }

    @Override
    protected void onPause() {
        playerPanel.onPause();
        playerText.onPause();

        super.onPause();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mBinder = (TTSService.ArticleTtsBinder) service;

        if (mAid == null) {
            Item i = (Item) mBinder.getArticle();
            if (i == null) {
                return;
            }

            mAid = i.getAid();
        }

        if (mAid == null) {
            return;
        }

        Item item = dataManager.getItemByAid(mAid);

        if (mAutoPlay) {
            mBinder.setArticle(item);
        }

        playerPanel.setTTSBinder(mBinder);

        playerText.setTTSBinder(mBinder);
        playerText.setPlayerText(mBinder.getText());

        if (mAutoPlay) {
            mBinder.play();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}
