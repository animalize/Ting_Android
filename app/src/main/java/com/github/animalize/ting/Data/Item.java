package com.github.animalize.ting.Data;

import android.content.ContentValues;
import android.database.Cursor;

import com.github.animalize.ting.Database.DataManager;
import com.github.animalize.ting.Database.MyDatabaseHelper;
import com.github.animalize.ting.TTS.TTSService;

import org.json.JSONObject;

/**
 * # json:
 * 键	       值	     备注
 * cate	     字符串	    分类
 * aid	     字符串	    文章ID
 * title     字符串	    标题
 * time	     整数	    unix时间戳
 * cjk_chars 整数	    汉字数
 * file_size 整数	    文件字节数
 * crc32     无符号整数	crc32
 * <p>
 * segments  字符串      分段信息
 */

public class Item implements TTSService.IArticle {
    private String cate;
    private String aid;
    private String title;

    private int time;
    private int cjk_chars;
    private int file_size;
    private long crc32;

    private String segments;

    private boolean cached = false;
    private int posi = 0;

    private int segments_array[];

    public Item(JSONObject object) {
        try {
            this.cate = object.getString("cate");
        } catch (Exception e) {
        }

        try {
            this.aid = object.getString("aid");
        } catch (Exception e) {
        }

        try {
            this.title = object.getString("title");
        } catch (Exception e) {
        }


        try {
            this.time = object.getInt("time");
        } catch (Exception e) {
        }

        try {
            this.cjk_chars = object.getInt("cjk_chars");
        } catch (Exception e) {
        }

        try {
            this.file_size = object.getInt("file_size");
        } catch (Exception e) {
        }

        try {
            this.crc32 = object.getLong("crc32");
        } catch (Exception e) {
        }

        try {
            segments = object.getString("segments");
        } catch (Exception e) {
        }
    }

    public Item(Cursor c) {
        cate = c.getString(c.getColumnIndex("cate"));
        aid = c.getString(c.getColumnIndex("aid"));
        title = c.getString(c.getColumnIndex("title"));

        time = c.getInt(c.getColumnIndex("time"));
        cjk_chars = c.getInt(c.getColumnIndex("cjk_chars"));
        file_size = c.getInt(c.getColumnIndex("file_size"));
        crc32 = c.getInt(c.getColumnIndex("crc32"));

        cached = c.getInt(c.getColumnIndex("cached")) != 0;
        posi = c.getInt(c.getColumnIndex("posi"));

        segments = c.getString(c.getColumnIndex("segments"));
    }

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();

        cv.put("cate", cate);
        cv.put("aid", aid);
        cv.put("title", title);

        cv.put("time", time);
        cv.put("cjk_chars", cjk_chars);
        cv.put("file_size", file_size);
        cv.put("crc32", crc32);

        cv.put("segments", segments);

        cv.put("cached", cached ? 1 : 0);
        cv.put("posi", posi);

        return cv;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getText() {
        DataManager dataManager = DataManager.getInstance();
        return dataManager.readArticleByAid(aid);
    }

    @Override
    public int[] getPageArrary() {
        if (segments_array == null) {
            String[] temp = segments.split(" ");
            segments_array = new int[temp.length];

            for (int i = 0; i < temp.length; i++) {
                segments_array[i] = Integer.parseInt(temp[i]);
            }
        }

        return segments_array;
    }

    @Override
    public int getPosi() {
        return posi;
    }

    @Override
    public void setPosi(int posi, boolean flush) {
        this.posi = posi;

        if (flush) {
            MyDatabaseHelper.flushPosi(aid, posi);
        }
    }

    public void setDBSegmentsCached() {
        MyDatabaseHelper.setSegmentsCached(aid, segments, cached);
    }

    public String getCate() {
        return cate;
    }

    public void setCate(String cate) {
        this.cate = cate;
    }

    public String getAid() {
        return aid;
    }

    public int getTime() {
        return time;
    }

    public int getCjk_chars() {
        return cjk_chars;
    }

    public int getFile_size() {
        return file_size;
    }

    public long getCrc32() {
        return crc32;
    }

    public boolean isCached() {
        return cached;
    }

    public void setCached(boolean cached) {
        this.cached = cached;
    }

    public String getSegments() {
        return segments;
    }

    public void setSegments(String segments) {
        this.segments = segments;
    }
}
