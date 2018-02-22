package com.github.animalize.ting.Data;


import android.content.Context;
import android.content.SharedPreferences;

import com.github.animalize.ting.MyApplication;

import java.util.HashSet;
import java.util.Set;

public class TingConfig {
    private static final String FILE_NAME = "config";

    private static final String TAG_FILTERS = "filters";
    private static final String TAG_HOST = "host";

    private static final String defaultHost = "http://192.168.21.60:17828";

    private static TingConfig singleTong;

    private Set<String> mFilters;
    private String mHost;

    private TingConfig() {
        loadConfig();
    }

    public static TingConfig getInstance() {
        if (singleTong == null) {
            singleTong = new TingConfig();
        }
        return singleTong;
    }

    public void loadConfig() {
        Context c = MyApplication.getContext();
        SharedPreferences sp = c.getSharedPreferences(
                FILE_NAME,
                Context.MODE_PRIVATE);

        mFilters = sp.getStringSet(TAG_FILTERS, new HashSet<String>());
        mHost = sp.getString(TAG_HOST, defaultHost);
    }

    private void saveOne(String name, Set<String> value) {
        Context c = MyApplication.getContext();
        SharedPreferences.Editor editor = c.getSharedPreferences(
                FILE_NAME,
                Context.MODE_PRIVATE).edit();
        editor.putStringSet(name, value);
        editor.apply();
    }

    private void saveOne(String name, String value) {
        Context c = MyApplication.getContext();
        SharedPreferences.Editor editor = c.getSharedPreferences(
                FILE_NAME,
                Context.MODE_PRIVATE).edit();
        editor.putString(name, value);
        editor.apply();
    }

    public Set<String> getmFilters() {
        return mFilters;
    }

    public void setmFilters(Set<String> mFilters) {
        if (!this.mFilters.equals(mFilters)) {
            this.mFilters = mFilters;
            saveOne(TAG_FILTERS, mFilters);
        }
    }

    public String getHost() {
        return mHost;
    }

    public void setHost(String host) {
        if (!this.mHost.equals(host)) {
            this.mHost = host;
            saveOne(TAG_HOST, host);
        }
    }
}
