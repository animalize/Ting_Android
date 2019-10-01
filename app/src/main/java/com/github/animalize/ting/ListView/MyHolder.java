package com.github.animalize.ting.ListView;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.github.animalize.ting.R;

class MyHolder extends RecyclerView.ViewHolder {
    public CardView root;

    public TextView title;

    public Button play;
    public Button delete;

    public TextView cate;
    public TextView chars;

    private String aid;

    public MyHolder(View itemView) {
        super(itemView);

        root = itemView.findViewById(R.id.root);

        title = itemView.findViewById(R.id.title);

        play = itemView.findViewById(R.id.button_play);
        delete = itemView.findViewById(R.id.button_delete);

        cate = itemView.findViewById(R.id.cate);
        chars = itemView.findViewById(R.id.chars);
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }
}
