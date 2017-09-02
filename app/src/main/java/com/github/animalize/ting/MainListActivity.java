package com.github.animalize.ting;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import com.baidu.tts.client.SpeechSynthesizeBag;
import com.github.animalize.ting.Data.Item;
import com.github.animalize.ting.Database.DataManager;
import com.github.animalize.ting.Database.MyDatabaseHelper;
import com.github.animalize.ting.ListView.RVAdapter;
import com.github.animalize.ting.TTS.ArticleTTS;

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

    private ArticleTTS articleTTS;

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

                Log.i("onItemClick: ", "text为空：" + (text == null));

                articleTTS.setArticle(title, text);
                articleTTS.play();
            }
        };
        mainList.setAdapter(listAdapter);

        Button bt = (Button) findViewById(R.id.refresh);
        bt.setOnClickListener(this);
        bt = (Button) findViewById(R.id.cache_all);
        bt.setOnClickListener(this);
        bt = (Button) findViewById(R.id.clear_cache);
        bt.setOnClickListener(this);

        // 读数据库list
        List<Item> list = dataManager.getFullList();
        listAdapter.setArrayList(list);


        nameSpinner = (Spinner) findViewById(R.id.cate_spinner);
        nameSpinner.setOnItemSelectedListener(this);

        nameAdapter = new NameListAdapter(this);
        nameAdapter.setList(dataManager.getCateNameList());
        nameSpinner.setAdapter(nameAdapter);

        articleTTS = new ArticleTTS(this);
    }

    private SpeechSynthesizeBag getSpeechSynthesizeBag(String text, String utteranceId) {
        SpeechSynthesizeBag speechSynthesizeBag = new SpeechSynthesizeBag();
        //需要合成的文本text的长度不能超过1024个GBK字节。
        speechSynthesizeBag.setText(text);
        speechSynthesizeBag.setUtteranceId(utteranceId);
        return speechSynthesizeBag;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.refresh:
                new GetListAsyncTask().execute();
                break;

            case R.id.cache_all:
                new CacheAllAsyncTask().execute(listAdapter.getAidList());
                break;

            case R.id.clear_cache:
                dataManager.clearCache();
                listAdapter.notifyDataSetChanged();
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
    protected void onDestroy() {
        setNotAlive();
        super.onDestroy();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String name = nameAdapter.getItem(position);

        List<Item> list;
        list = dataManager.getCateList(name);

        listAdapter.setArrayList(list);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private class GetListAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            dataManager.loadListFromServer();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (isAlive()) {
                nameAdapter.setList(dataManager.getCateNameList());
            }
        }
    }

    private class CacheAllAsyncTask extends AsyncTask<List<String>, String, Void> {
        @Override
        protected Void doInBackground(List<String>... params) {
            for (String aid : params[0]) {
                boolean r = dataManager.downloadAndSaveArticleByAid(aid);
                if (r) {
                    publishProgress(aid);
                    MyDatabaseHelper.setCached(aid, true);
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            String aid = values[0];
            Item item = dataManager.getItemByAid(aid);
            item.setCached(true);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            listAdapter.notifyDataSetChanged();
        }
    }
}
