package com.tongtong.purchaser.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.model.NewsModel;


/**
 * Created by zxy on 2018/3/10.
 */

public class NewsViewHolder extends BaseViewHolder<NewsModel>{

    public NewsViewHolder(ViewGroup parent, Context context){
        super(parent, R.layout.rp_details_list_item);

    }

    @Override
    public void setData(NewsModel data) {

    }
}
