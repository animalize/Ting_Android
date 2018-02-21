package com.github.animalize.ting.ListView;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.animalize.ting.R;

class MyHolder extends RecyclerView.ViewHolder {
    public TextView title;

    public Button play;
    public Button delete;

    public TextView cate;
    public TextView progress;
    public TextView chars;

    private String aid;

    public MyHolder(View itemView) {
        super(itemView);

        title = itemView.findViewById(R.id.title);

        play = itemView.findViewById(R.id.button_play);
        delete = itemView.findViewById(R.id.button_delete);

        cate = itemView.findViewById(R.id.cate);
        progress = itemView.findViewById(R.id.progress);
        chars = itemView.findViewById(R.id.chars);
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }
}
