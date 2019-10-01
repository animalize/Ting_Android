package com.github.animalize.ting;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.animalize.ting.Data.Item;
import com.github.animalize.ting.Database.DataManager;
import com.github.animalize.ting.ListView.RVAdapter;
import com.github.animalize.ting.PlayerUI.PlayerPanelWidget;
import com.github.animalize.ting.PlayerUI.TextPlayerActivity;
import com.github.animalize.ting.TTS.TTSService;
import com.github.animalize.ting.TingTTS.TingTTSService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainListActivity
        extends AppCompatActivity
        implements View.OnClickListener, ThreadHost {

    private final static int OPTION_REQ_CODE = 3333;
    private String currentAid;

    private boolean isAlive = true;
    private boolean netOperating = false;

    private DataManager dataManager = DataManager.getInstance();

    private PlayerPanelWidget playerPanel;
    private RVAdapter listAdapter;

    private Button refreshButton, delAllButton;

    private TTSService.ArticleTtsBinder mBinder;
    private ServiceConnection mServerConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (TTSService.ArticleTtsBinder) service;
            playerPanel.setTTSBinder(mBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 防止从安装器打开出现问题
        if (!isTaskRoot()) {
            finish();
            return;
        }

        setContentView(R.layout.activity_main_list);

        playerPanel = findViewById(R.id.player_panel_view);

        RecyclerView mainList = findViewById(R.id.main_list);
        mainList.setHasFixedSize(true);

        // 布局管理
        LinearLayoutManager lm = new LinearLayoutManager(this);
        mainList.setLayoutManager(lm);
        // adapter
        listAdapter = new RVAdapter() {
            @Override
            public void onPalyItemClick(String aid) {
                if (mBinder == null) {
                    return;
                }

                Item item = dataManager.getItemByAid(aid);
                if (item == null) {
                    return;
                }

                if (mBinder.playArticle(item)) {
                    if (!aid.equals(currentAid)) {
                        currentAid = aid;
                        listAdapter.playingColorByAid(aid);
                    }
                }
            }

            @Override
            public void onDeleteItemClick(final String aid) {
                if (netOperating) {
                    Toast.makeText(
                            MainListActivity.this,
                            "正在进行网络操作，无法删除。",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(MainListActivity.this);
                builder.setTitle("确认删除此文章？");
                builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new DeleteAidListAsyncTask().execute(Arrays.asList(aid));
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.show();
            }
        };
        mainList.setAdapter(listAdapter);

        refreshButton = findViewById(R.id.refresh);
        refreshButton.setOnClickListener(this);
        delAllButton = findViewById(R.id.del_all);
        delAllButton.setOnClickListener(this);
        Button bt = findViewById(R.id.open_text);
        bt.setOnClickListener(this);
        bt = findViewById(R.id.config);
        bt.setOnClickListener(this);

        // 读数据库list
        List<Item> list = dataManager.getFullList();
        listAdapter.setArrayList(list);

        // 绑定服务
        Intent intent = new Intent(this, TingTTSService.class);
        bindService(intent, mServerConn, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        setNotAlive();
        unbindService(mServerConn);

        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (mBinder == null) {
                        finish();
                        return true;
                    }

                    int state = mBinder.getState();
                    if (state == TTSService.PLAYING || state == TTSService.PAUSING) {
                        AlertDialog.Builder d = new AlertDialog.Builder(this);
                        d.setTitle("确认退出");
                        d.setMessage("此时退出将中止播放。\n（可以按Home键切换到后台运行）");
                        d.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 保存进度
                                mBinder.stop();
                                finish();
                            }
                        });
                        d.setNegativeButton("取消", null);
                        d.show();
                    } else {
                        finish();
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStart() {
        super.onStart();
        playerPanel.onStart();
    }

    @Override
    protected void onStop() {
        playerPanel.onStop();
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.refresh:
                if (netOperating) {
                    return;
                }

                new GetListAsyncTask().execute();
                break;

            case R.id.del_all:
                if (netOperating) {
                    return;
                }

                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(this);
                builder.setTitle("确认删除所有文章？");
                builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<String> temp = new ArrayList<>();
                        List<Item> full = dataManager.getFullList();

                        for (Item item : full) {
                            temp.add(item.getAid());
                        }

                        new DeleteAidListAsyncTask().execute(temp);
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.show();

                break;

            case R.id.open_text:
                TextPlayerActivity.actionStart(this);
                break;

            case R.id.config:
                OptionActivity.actionStart(this, OPTION_REQ_CODE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == OPTION_REQ_CODE && resultCode == RESULT_OK) {
            mBinder.setSetting();
        }
    }

    @Override
    public void setNotAlive() {
        isAlive = false;
    }

    @Override
    public boolean isAlive() {
        return isAlive;
    }

    // 刷新列表
    private class GetListAsyncTask extends AsyncTask<Void, String, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            netOperating = true;
            refreshButton.setEnabled(false);
            delAllButton.setEnabled(false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // 下载列表
            boolean success = dataManager.loadListFromServer();
            if (!success) {
                return false;
            }

            publishProgress("");

            // 缓存文章
            List<Item> list = dataManager.getFullList();
            for (Item item : list) {
                if (item.isCached()) {
                    continue;
                }

                String aid = item.getAid();
                String segments = dataManager.downloadAndSaveArticleByAid(
                        aid,
                        item.getFile_size());

                if (segments != null) {
                    item.setSegments(segments);
                    item.setCached(true);
                    item.saveDBSegmentsCached();

                    publishProgress(aid);
                }
            }

            return true;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if (!isAlive()) {
                return;
            }

            String v = values[0];

            if ("".equals(v)) {
                listAdapter.setArrayList(dataManager.getFullList());
            } else {
                listAdapter.refreshItemByAid(v);
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (!isAlive()) {
                return;
            }

            if (!success) {
                Toast.makeText(MainListActivity.this,
                        "刷新列表失败。\n请确保设置中的服务器地址正确、服务器正常运行。",
                        Toast.LENGTH_LONG).show();
            }

            refreshButton.setEnabled(true);
            delAllButton.setEnabled(true);

            netOperating = false;
        }
    }

    private class DeleteAidListAsyncTask extends AsyncTask<List<String>, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            netOperating = true;
            refreshButton.setEnabled(false);
            delAllButton.setEnabled(false);
        }

        @Override
        protected Boolean doInBackground(List<String>... params) {
            return dataManager.deleteAidList(params[0]);
        }

        @Override
        protected void onPostExecute(Boolean remoteDeleted) {
            super.onPostExecute(remoteDeleted);
            if (!isAlive()) {
                return;
            }

            if (!remoteDeleted) {
                Toast.makeText(MainListActivity.this,
                        "从服务器删除列表失败，仅删除本地复本，成功刷新后会重新出现。\n请确保设置中的服务器地址正确、服务器正常运行。",
                        Toast.LENGTH_LONG).show();
            }

            listAdapter.setArrayList(dataManager.getFullList());

            refreshButton.setEnabled(true);
            delAllButton.setEnabled(true);

            netOperating = false;
        }
    }
}
