package com.github.animalize.ting.ListView;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.animalize.ting.R;

class MyHolder extends RecyclerView.ViewHolder {
    public LinearLayout root;

    public TextView title;

    public TextView cate;
    public TextView chars;
    public TextView cached;

    public MyHolder(View itemView) {
        super(itemView);

        root = (LinearLayout) itemView.findViewById(R.id.main_list_item);

        title = (TextView) itemView.findViewById(R.id.title);
        cate = (TextView) itemView.findViewById(R.id.cate);
        chars = (TextView) itemView.findViewById(R.id.chars);

        cached = (TextView) itemView.findViewById(R.id.cached);
    }
}
