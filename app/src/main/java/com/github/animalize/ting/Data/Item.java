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
 */

public class Item implements TTSService.IArticle {
    private String cate;
    private String aid;
    private String title;

    private int time;
    private int cjk_chars;
    private int file_size;
    private long crc32;

    private boolean cached = false;
    private int posi = 0;

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
    }

    public Item(Cursor c) {
        cate = c.getString(c.getColumnIndex("cate"));
        aid = c.getString(c.getColumnIndex("aid"));
        title = c.getString(c.getColumnIndex("title"));

        time = c.getInt(c.getColumnIndex("time"));
        cjk_chars = c.getInt(c.getColumnIndex("cjk_chars"));
        file_size = c.getInt(c.getColumnIndex("file_size"));
        crc32 = c.getInt(c.getColumnIndex("crc32"));

        cached = c.getInt(c.getColumnIndex("cached")) == 1;
        posi = c.getInt(c.getColumnIndex("posi"));
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

        cv.put("cached", cached);
        cv.put("posi", posi);

        return cv;
    }

    public String getCate() {
        return cate;
    }

    public String getAid() {
        return aid;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String getText() {
        DataManager dataManager = DataManager.getInstance();
        return dataManager.readArticleByAid(aid);
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

        MyDatabaseHelper.setCached(aid, cached);
    }

    public int getPosi() {
        return posi;
    }

    public void setPosi(int posi) {
        this.posi = posi;
    }
}
