package com.github.animalize.ting;


import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;

import java.util.List;

public class NameListAdapter extends ArrayAdapter<String> {
    private List<String> nameList;
    private int currentIndex = -1;

    public NameListAdapter(@NonNull Context context) {
        super(context, R.layout.support_simple_spinner_dropdown_item);
    }

    public void setList(List<String> list) {
        nameList = list;
        notifyDataSetChanged();
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int index) {
        currentIndex = index;
    }

    @Override
    public int getCount() {
        if (nameList != null) {
            return nameList.size();
        } else {
            return 0;
        }
    }

    @Override
    public String getItem(int position) {
        if (nameList != null) {
            return nameList.get(position);
        } else {
            return "";
        }
    }
}
