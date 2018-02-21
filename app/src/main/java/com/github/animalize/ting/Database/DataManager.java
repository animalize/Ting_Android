package com.github.animalize.ting.Database;


import android.support.annotation.Nullable;

import com.github.animalize.ting.Data.Item;
import com.github.animalize.ting.Data.TingConfig;
import com.github.animalize.ting.Message.Methods;
import com.github.animalize.ting.MyApplication;
import com.github.animalize.ting.TTS.TTSService;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataManager {
    public static final String GB18030 = "GB18030";
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
        List<Item> ret = Methods.downloadList();
        if (ret == null) {
            return false;
        }

        Set<String> filter = TingConfig.getInstance().getmFilters();
        List<Item> newFullList = new ArrayList<>();
        List<Item> addList = new ArrayList<>();

        Set<String> newListAidSet = new HashSet<>();
        List<String> delAidList = new ArrayList<>();

        for (Item item : ret) {
            // 过滤分类
            String cate = item.getCate();
            if (cate.contains("_")) {
                String parts[] = cate.split("_", 2);
                if (!filter.contains(parts[0])) {
                    continue;
                }
                item.setCate(parts[1]);
            }

            // 设置cached, posi
            Item old = aidMap.get(item.getAid());
            if (old != null) {
                item.setCached(old.isCached());
                item.setNowChar(old.getNowChar(), false);
            }

            if (!aidMap.containsKey(item.getAid())) {
                addList.add(item);
            }
            newFullList.add(item);
            newListAidSet.add(item.getAid());
        }

        // 要删的
        for (String aid : aidMap.keySet()) {
            if (!newListAidSet.contains(aid)) {
                delAidList.add(aid);
            }
        }

        // 加载
        Collections.reverse(newFullList);
        loadDataFromList(newFullList);

        // 写数据库
        MyDatabaseHelper.setList(delAidList, addList);

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

    public synchronized void deleteAidList(List<String> aidList) {
        // 数据库
        List<Item> list = new ArrayList<>();
        for (Item item : fullList) {
            if (!aidList.contains(item.getAid())) {
                list.add(item);
            }
        }

        loadDataFromList(list);

        // 删无效缓存
        delOldCache(aidList);

        // 写数据库
        MyDatabaseHelper.delItems(aidList);

        // 从服务器删除
        Methods.deleteAids(aidList);
    }

    public void delOldCache(List<String> delAidList) {
        File dir = new File(dataDirPath);

        for (String aid : delAidList) {
            try {
                new File(dir, aid).delete();
            } catch (Exception e) {
            }
        }
    }

    /*
    返回分段信息，单位是char
    如: "9993 19985 29983 39983 49970 54153"
     */
    @Nullable
    public synchronized String downloadAndSaveArticleByAid(String aid, int fileSize) {
        File path = new File(dataDirPath, aid);

        // 已存在？
        if (path.isFile()) {
            return null;
        }

        // 下载
        byte[] b = Methods.downloadArticleByAid(aid);
        if (b == null || b.length != fileSize) {
            //Log.i("downloadAndSav: ", "" + b.length + " " + fileSize);
            return null;
        }

        String text;
        try {
            text = new String(b, GB18030);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        // txt文件存盘
        OutputStream out = null;
        try {
            out = new FileOutputStream(path);
            out.write(b);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return TTSService.getSegments(text);
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

            String s = new String(fileData, GB18030);

            return s;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
