package com.github.animalize.ting.Database;

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

    public static List<Item> getList() {
        init();

        String sql = "SELECT * FROM list ORDER BY aid DESC";
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

    public static void setList(List<String> delAidList, List<Item> addList) {
        init();

        String[] delAidArray = delAidList.toArray(new String[delAidList.size()]);

        mDb.execSQL("BEGIN");

        // 删没有的
        if (delAidArray.length > 0) {
            String sql = "DELETE FROM list " +
                    "WHERE aid IN (" +
                    makePlaceholders(delAidArray.length) + ") ";
            mDb.execSQL(sql, delAidArray);
        }

        // 添新的
        for (Item item : addList) {
            mDb.insert("list", null, item.getContentValues());
        }

        mDb.execSQL("COMMIT");
    }

    public static void delItems(List<String> delAidList) {
        init();

        String[] delAidArray = delAidList.toArray(new String[delAidList.size()]);

        mDb.execSQL("BEGIN");

        // 删没有的
        if (delAidArray.length > 0) {
            String sql = "DELETE FROM list " +
                    "WHERE aid IN (" +
                    makePlaceholders(delAidArray.length) + ") ";
            mDb.execSQL(sql, delAidArray);
        }

        mDb.execSQL("COMMIT");
    }

    public static void setCached(String aid, boolean v) {
        init();

        String sql = "UPDATE list SET cached=? WHERE aid=?";
        int cached = v ? 1 : 0;

        mDb.execSQL(sql, new String[]{String.valueOf(cached), aid});
    }

    private static String makePlaceholders(int len) {
        // 须确保 len > 0
        StringBuilder sb = new StringBuilder(len * 2 - 1);
        sb.append("?");
        for (int i = 1; i < len; i++) {
            sb.append(",?");
        }
        return sb.toString();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 建表
        String sql = "CREATE TABLE list (" +
                "cate TEXT," +
                "aid TEXT," +
                "title TEXT," +

                "time INTEGER," +
                "cjk_chars INTEGER," +
                "file_size INTEGER," +
                "crc32 INTEGER," +

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
