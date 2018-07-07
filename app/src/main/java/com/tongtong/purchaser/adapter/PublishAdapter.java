package com.tongtong.purchaser.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.tongtong.purchaser.model.ReleaseModel;

/**
 * Created by Administrator on 2018-02-03.
 */

public class PublishAdapter extends RecyclerArrayAdapter<ReleaseModel> {

    private Context ctx;
    public PublishAdapter(Context context) {

        super(context);
        this.ctx=context;

    }
    @Override
    public PublishViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new PublishViewHolder(parent,ctx);
    }
}
