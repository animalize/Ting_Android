package com.github.animalize.ting.ListView;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.animalize.ting.Data.Item;
import com.github.animalize.ting.Data.MyColors;
import com.github.animalize.ting.R;

import java.util.ArrayList;
import java.util.List;

public abstract class RVAdapter
        extends RecyclerView.Adapter<MyHolder> {
    private List<Item> mList;

    public abstract void onItemClick(String aid);

    public void setArrayList(List<Item> list) {
        mList = list;
        notifyDataSetChanged();
    }

    public List<String> getAidList() {
        ArrayList<String> list = new ArrayList<>();
        for (Item item : mList) {
            list.add(item.getAid());
        }
        return list;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.main_list_item, parent, false);
        final MyHolder holder = new MyHolder(v);

        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int posi = holder.getAdapterPosition();
                Item item = mList.get(posi);

                onItemClick(item.getAid());
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        Item item = mList.get(position);

        if (position % 2 == 0) {
            holder.root.setBackgroundColor(MyColors.c1);
        } else {
            holder.root.setBackgroundColor(MyColors.c2);
        }

        holder.title.setText(item.getTitle());
        holder.cate.setText(item.getCate());
        holder.chars.setText("" + item.getChars());

        holder.cached.setText(item.isChached() ? "已缓存" : "未缓存");
    }

    @Override
    public int getItemCount() {
        if (mList == null) {
            return 0;
        }
        return mList.size();
    }
}