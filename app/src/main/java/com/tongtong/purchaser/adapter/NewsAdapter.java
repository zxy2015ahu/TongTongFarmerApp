package com.tongtong.purchaser.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.tongtong.purchaser.model.NewsModel;

/**
 * Created by zxy on 2018/3/10.
 */

public class NewsAdapter extends RecyclerArrayAdapter<NewsModel>{
    private Context ctx;
    public NewsAdapter(Context context) {

        super(context);
        this.ctx=context;

    }
    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new NewsViewHolder(parent,ctx);
    }
}
