package com.github.animalize.ting.Data;

import android.database.Cursor;

import org.json.JSONObject;

/**
 * # json
 * one = {'cate': cate,
 * 'aid': aid,
 * 'chars': chn_count,
 * 'time': unixtime,
 * 'title': title
 * }
 */

public class Item {
    private String cate;
    private String aid;
    private int chars;
    private int time;
    private String title;

    private boolean chached = false;
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
            this.chars = object.getInt("chars");
        } catch (Exception e) {
        }

        try {
            this.time = object.getInt("time");
        } catch (Exception e) {
        }

        try {
            this.title = object.getString("title");
        } catch (Exception e) {
        }
    }

    public Item(Cursor c) {
        cate = c.getString(c.getColumnIndex("cate"));
        aid = c.getString(c.getColumnIndex("aid"));
        chars = c.getInt(c.getColumnIndex("chars"));
        time = c.getInt(c.getColumnIndex("time"));
        title = c.getString(c.getColumnIndex("title"));

        chached = c.getInt(c.getColumnIndex("cached")) == 1;
        posi = c.getInt(c.getColumnIndex("posi"));
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

    public void setAid(String aid) {
        this.aid = aid;
    }

    public int getChars() {
        return chars;
    }

    public void setChars(int chars) {
        this.chars = chars;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isChached() {
        return chached;
    }

    public void setChached(boolean chached) {
        this.chached = chached;
    }

    public int getPosi() {
        return posi;
    }

    public void setPosi(int posi) {
        this.posi = posi;
    }
}
