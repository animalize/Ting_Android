package com.github.animalize.ting.Database;


import android.support.annotation.Nullable;
import android.util.Log;

import com.github.animalize.ting.Data.Item;
import com.github.animalize.ting.FenJu.Ju;
import com.github.animalize.ting.FenJu.TTSUtils;
import com.github.animalize.ting.Message.Methods;
import com.github.animalize.ting.MyApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static java.lang.Integer.max;

public class DataManager {
    private static final String DATA_DIR_NAME = "data";

    private static DataManager singleton;

    private String dataDirPath;

    private List<Item> fullList;
    private List<String> cateNameList;
    private Map<String, ArrayList<Item>> cateMap;
    private Map<String, Item> aidMap;

    public static DataManager getInstance() {
        if (singleton == null) {
            singleton = new DataManager();
        }
        return singleton;
    }

    private DataManager() {
        // 创建data目录
        File fileDir = MyApplication.getContext().getFilesDir();
        File dataDir = new File(fileDir, DATA_DIR_NAME);
        if (!dataDir.isDirectory()) {
            dataDir.mkdir();
        }
        dataDirPath = dataDir.getAbsolutePath();

        loadListFromDB();
    }

    public synchronized void loadListFromDB() {
        // 读数据库
        List<Item> list = MyDatabaseHelper.getList();
        // 加载
        loadDataFromList(list);
    }

    public synchronized boolean loadListFromServer() {
        // 下载
        List<Item> list = Methods.downloadList();
        if (list == null) {
            return false;
        }

        // 设置cached, posi
        for (Item item : list) {
            Item old = aidMap.get(item.getAid());
            if (old != null) {
                item.setChached(old.isChached());
                item.setPosi(old.getPosi());
            }
        }

        // 加载
        loadDataFromList(list);

        // 写数据库
        MyDatabaseHelper.setList(list);

        // 删无效缓存
        deleteInvalidCache();

        return true;
    }

    // 加载数据
    private synchronized void loadDataFromList(List<Item> list) {
        fullList = list;
        cateMap = new HashMap<>();
        aidMap = new HashMap<>();

        for (Item item : list) {
            if (!cateMap.containsKey(item.getCate())) {
                cateMap.put(item.getCate(), new ArrayList<Item>());
            }
            cateMap.get(item.getCate()).add(item);

            aidMap.put(item.getAid(), item);
        }

        cateNameList = new ArrayList<>(cateMap.keySet());
        Collections.sort(cateNameList);
    }

    // 删无效缓存
    private void deleteInvalidCache() {
        File dir = new File(dataDirPath);

        if (dir.isDirectory()) {
            String[] children = dir.list();

            for (String child : children) {
                if (!aidMap.containsKey(child)) {
                    new File(dir, child).delete();
                }
            }
        }
    }

    public synchronized List<Item> getFullList() {
        return fullList;
    }

    public synchronized List<String> getCateNameList() {
        return cateNameList;
    }

    public synchronized List<Item> getCateList(String cate) {
        if (cateMap.containsKey(cate)) {
            return cateMap.get(cate);
        }

        return new ArrayList<>();
    }

    @Nullable
    public synchronized Item getItemByAid(String aid) {
        return aidMap.get(aid);
    }

    public void clearCache() {
        // 清空目录
        File dir = new File(dataDirPath);
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                new File(dir, child).delete();
            }
        }

        // 更新 数据库
        for (Item item : fullList) {
            item.setChached(false);
            MyDatabaseHelper.setCached(item.getAid(), false);
        }
    }

    public synchronized boolean downloadAndSaveArticleByAid(String aid) {
        File path = new File(dataDirPath, aid);

        // 已存在？
        if (path.isFile()) {
            Log.i("cleByAid: ", "忆存在" + aid);
            return true;
        }

        // 下载
        byte[] b = Methods.downloadArticleByAid(aid);
        if (b == null) {
            return false;
        }

        // test
        try {
            String s = new String(b, "GB18030");

            List<Ju> jus = TTSUtils.fenJu(s);

            Log.i("rticleByAid: ", "数量" + jus.size());
            for (Ju ju : jus) {
                Log.i("rticleByAid: ",
                        "起始" + ju.begin + " 结束" + ju.end + " 长度" + (ju.end - ju.begin));
                Log.i("rticleByAid: ", s.substring(ju.begin, ju.end));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        // 存盘
        OutputStream out = null;
        try {
            out = new FileOutputStream(path);
            out.write(b);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }
}
