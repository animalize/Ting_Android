package com.github.animalize.ting.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.github.animalize.ting.Data.Item;
import com.github.animalize.ting.MyApplication;

import java.util.ArrayList;
import java.util.List;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "data.db";
    private static final int DATABASE_VERSION = 1;

    private static MyDatabaseHelper mHelper;
    private static SQLiteDatabase mDb;

    public MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static void init() {
        if (mHelper == null) {
            Context context = MyApplication.getContext();
            mHelper = new MyDatabaseHelper(context);

            mDb = mHelper.getWritableDatabase();
        }
    }

    public static void setList(List<Item> list) {
        init();

        mDb.execSQL("BEGIN");

        // 删所有
        String sql = "DELETE FROM list";
        mDb.execSQL(sql);

        // 添新的
        for (Item item : list) {
            ContentValues cv = new ContentValues();

            cv.put("cate", item.getCate());
            cv.put("aid", item.getAid());
            cv.put("time", item.getTime());
            cv.put("chars", item.getChars());
            cv.put("title", item.getTitle());

            cv.put("cached", item.isChached());
            cv.put("posi", item.getPosi());

            mDb.insert("list", null, cv);
        }

        mDb.execSQL("COMMIT");
    }

    public static List<Item> getList() {
        init();

        String sql = "SELECT * FROM list ORDER BY aid ASC";
        Cursor c = mDb.rawQuery(sql, null);

        List<Item> l = new ArrayList<>();
        if (c.moveToFirst()) {
            do {
                Item item = new Item(c);
                l.add(item);
            } while (c.moveToNext());
        }
        c.close();

        return l;
    }

    public static void setCached(String aid, boolean v) {
        init();

        String sql = "UPDATE list SET cached=? WHERE aid=?";
        int cached = v ? 1 : 0;

        mDb.execSQL(sql, new String[]{String.valueOf(cached), aid});
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 建表
        String sql = "CREATE TABLE list (" +
                "cate TEXT," +
                "aid TEXT," +
                "time INTEGER," +
                "chars INTEGER," +
                "title TEXT," +

                "cached INTEGER," +
                "posi INTEGER" +
                ");";
        db.execSQL(sql);

        sql = "CREATE INDEX aid_idx ON list(aid);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
