package com.github.animalize.ting.Database;


import android.support.annotation.Nullable;

import com.github.animalize.ting.Data.Item;
import com.github.animalize.ting.Message.Methods;
import com.github.animalize.ting.MyApplication;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataManager {
    private static final String DATA_DIR_NAME = "data";

    private static DataManager singleton;

    private String dataDirPath;

    private List<Item> fullList;
    private List<String> cateNameList;
    private Map<String, ArrayList<Item>> cateMap;
    private Map<String, Item> aidMap;

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

    public static DataManager getInstance() {
        if (singleton == null) {
            singleton = new DataManager();
        }
        return singleton;
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
            return true;
        }

        // 下载
        byte[] b = Methods.downloadArticleByAid(aid);
        if (b == null) {
            return false;
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

    @Nullable
    public synchronized String readArticleByAid(String aid) {
        File file = new File(dataDirPath, aid);

        byte[] fileData = new byte[(int) file.length()];
        DataInputStream dis = null;
        try {
            dis = new DataInputStream(new FileInputStream(file));
            dis.readFully(fileData);
            dis.close();

            String s = new String(fileData, "GB18030");

            return s;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
