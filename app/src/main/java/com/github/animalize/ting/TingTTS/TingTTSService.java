package com.github.animalize.ting.TingTTS;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.github.animalize.ting.R;
import com.github.animalize.ting.TTS.Setting;
import com.github.animalize.ting.TTS.TTSService;

public class TingTTSService extends TTSService {
    @Override
    public int getFinishSoundID() {
        return R.raw.finish;
    }

    @Override
    public int initTTS(int currentVer) {
        return TTSInitializer.initialEnv(this, currentVer);
    }

    @Override
    public void doStartForeground() {
        // 前台服务
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else {
            Notification notification = new NotificationCompat.Builder(
                    this, "12828")
                    .setSmallIcon(R.drawable.notifiy_icon)
                    .setContentTitle("Ting的前台服务")
                    .setContentText("保持TTS服务持续运行")
                    .build();
            startForeground(12828, notification);
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = "com.github.animalize.ting";
        String channelName = "Ting Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setSound(null, null);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder
                .setSmallIcon(R.drawable.notifiy_icon)
                .setContentTitle("Ting的前台服务")
                .setContentText("保持TTS服务持续运行")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(12828, notification);
    }

    @Override
    public Setting getSetting() {
        return TingSetting.getInstance(this);
    }
}
