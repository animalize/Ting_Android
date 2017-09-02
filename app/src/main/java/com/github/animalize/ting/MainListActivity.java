package com.github.animalize.ting;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import com.github.animalize.ting.Data.Item;
import com.github.animalize.ting.Database.DataManager;
import com.github.animalize.ting.ListView.RVAdapter;
import com.github.animalize.ting.TTS.ArticleTtsService;

import java.util.List;

public class MainListActivity
        extends AppCompatActivity
        implements View.OnClickListener, ThreadHost, AdapterView.OnItemSelectedListener {

    private boolean isAlive = true;

    private DataManager dataManager = DataManager.getInstance();

    private RecyclerView mainList;
    private RVAdapter listAdapter;

    private Spinner nameSpinner;
    private NameListAdapter nameAdapter;

    private ArticleTtsService.ArticleTtsBinder mBinder;
    private ServiceConnection mServerConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (ArticleTtsService.ArticleTtsBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);

        mainList = (RecyclerView) findViewById(R.id.main_list);
        // 布局管理
        LinearLayoutManager lm = new LinearLayoutManager(this);
        mainList.setLayoutManager(lm);
        // adapter
        listAdapter = new RVAdapter() {
            @Override
            public void onItemClick(String aid) {
                String title = dataManager.getItemByAid(aid).getTitle();
                String text = dataManager.readArticleByAid(aid);

                if (text != null) {
                    mBinder.setArticle(title, text);
                    mBinder.play();
                }
            }
        };
        mainList.setAdapter(listAdapter);

        Button bt = (Button) findViewById(R.id.refresh);
        bt.setOnClickListener(this);
        bt = (Button) findViewById(R.id.pause);
        bt.setOnClickListener(this);
        bt = (Button) findViewById(R.id.resume);
        bt.setOnClickListener(this);
        bt = (Button) findViewById(R.id.stop);
        bt.setOnClickListener(this);

        // 读数据库list
        List<Item> list = dataManager.getFullList();
        listAdapter.setArrayList(list);


        nameSpinner = (Spinner) findViewById(R.id.cate_spinner);
        nameSpinner.setOnItemSelectedListener(this);

        nameAdapter = new NameListAdapter(this);
        nameAdapter.setList(dataManager.getCateNameList());
        nameSpinner.setAdapter(nameAdapter);


        Intent intent = new Intent(this, ArticleTtsService.class);
        bindService(intent, mServerConn, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        setNotAlive();
        unbindService(mServerConn);

        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.refresh:
                new GetListAsyncTask().execute();
                break;

            case R.id.pause:
                mBinder.pause();
                break;

            case R.id.resume:
                mBinder.resume();
                break;

            case R.id.stop:
                mBinder.stop();
                break;
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        nameAdapter.setCurrentIndex(position);

        String name = nameAdapter.getItem(position);

        List<Item> list;
        list = dataManager.getCateList(name);

        listAdapter.setArrayList(list);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private class GetListAsyncTask extends AsyncTask<Void, String, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            // 下载列表
            dataManager.loadListFromServer();
            publishProgress("");

            // 缓存文章
            List<Item> list = dataManager.getFullList();
            for (Item item : list) {
                if (item.isCached()) {
                    continue;
                }

                String aid = item.getAid();
                boolean r = dataManager.downloadAndSaveArticleByAid(aid);
                if (r) {
                    item.setCached(true);
                    publishProgress(aid);
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            String v = values[0];

            if ("".equals(v)) {
                if (isAlive()) {
                    nameAdapter.setList(dataManager.getCateNameList());
                    String cate = nameAdapter.getItem(nameAdapter.getCurrentIndex());

                    listAdapter.setArrayList(dataManager.getCateList(cate));
                }
            } else {
                listAdapter.notifyDataSetChanged();
            }
        }
    }
}
