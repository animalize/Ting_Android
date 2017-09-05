package com.github.animalize.ting.ListView;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.animalize.ting.R;

class MyHolder extends RecyclerView.ViewHolder {
    public LinearLayout root;
    public TextView title;
    public Button play;
    public Button delete;
    public TextView cate;
    public TextView chars;
    private String aid;

    public MyHolder(View itemView) {
        super(itemView);

        root = (LinearLayout) itemView.findViewById(R.id.main_list_item);

        title = (TextView) itemView.findViewById(R.id.title);

        play = (Button) itemView.findViewById(R.id.button_play);
        delete = (Button) itemView.findViewById(R.id.button_delete);

        cate = (TextView) itemView.findViewById(R.id.cate);
        chars = (TextView) itemView.findViewById(R.id.chars);
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }
}
