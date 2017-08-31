package com.github.animalize.ting.Message;

import android.support.annotation.Nullable;

import com.github.animalize.ting.Data.Item;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by anima on 17-8-30.
 */

public class Methods {
    private static final String HOST = "http://192.168.21.60:17828";
    private static final String ENCODING = "GB18030";

    // 得到文章列表
    @Nullable
    public static List<Item> downloadList() {
        String url = HOST + "/get_list";

        byte[] b = downloadUrl(url);
        if (b == null) {
            return null;
        }

        ArrayList<Item> ret = new ArrayList<>();
        try {
            String s = new String(b, ENCODING);

            JSONArray jsonArray = new JSONArray(s);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Item item = new Item(jsonObject);
                ret.add(item);
            }
        } catch (Exception e) {
            return null;
        }

        return ret;
    }

    /**
     * @param aid 文章ID
     * @return byte[]或null
     */
    @Nullable
    public static byte[] downloadArticleByAid(String aid) {
        String url = HOST + "/get_article" + "?aid=" + aid;

        byte[] b = downloadUrl(url);
        return b;
    }


    @Nullable
    private static byte[] downloadUrl(String url) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            Response response = client.newCall(request).execute();
            byte[] b = response.body().bytes();
            return b;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
