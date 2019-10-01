package com.github.animalize.ting.ListView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.github.animalize.ting.Data.Item;
import com.github.animalize.ting.Data.MyColors;
import com.github.animalize.ting.R;

import java.util.ArrayList;
import java.util.List;

public abstract class RVAdapter
        extends RecyclerView.Adapter<MyHolder> {
    private List<Item> mList;
    private String currenAid;

    public abstract void onPalyItemClick(String aid);

    public abstract void onDeleteItemClick(String aid);

    public void setArrayList(List<Item> list) {
        mList = list;
        notifyDataSetChanged();
    }

    public List<String> getAidList() {
        ArrayList<String> list = new ArrayList<>();

        if (mList != null) {
            for (Item item : mList) {
                list.add(item.getAid());
            }
        }

        return list;
    }

    public void refreshItemByAid(String aid) {
        if (mList == null) {
            return;
        }

        for (int i = 0; i < mList.size(); i++) {
            Item item = mList.get(i);
            if (item.getAid().equals(aid)) {
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void playingColorByAid(String aid) {
        String temp = currenAid;
        currenAid = aid;

        if (temp != null) {
            refreshItemByAid(temp);
        }

        refreshItemByAid(currenAid);
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.main_list_item, parent, false);
        final MyHolder holder = new MyHolder(v);

        holder.play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String aid = holder.getAid();
                onPalyItemClick(aid);
            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String aid = holder.getAid();
                onDeleteItemClick(aid);
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        Item item = mList.get(position);

        if (!item.getAid().equals(currenAid)) {
            holder.root.setCardBackgroundColor(MyColors.cardStop);
        } else {
            holder.root.setCardBackgroundColor(MyColors.cardPlaying);
        }

        holder.setAid(item.getAid());

        holder.title.setText(item.getTitle());
        holder.cate.setText(item.getCate());

        if (item.isCached()) {
            holder.chars.setText("" + item.getCJKChars() + "汉字");
        } else {
            holder.chars.setText("未缓存");
        }
    }

    @Override
    public int getItemCount() {
        if (mList == null) {
            return 0;
        }
        return mList.size();
    }
}