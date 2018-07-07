package com.tongtong.purchaser.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.tongtong.purchaser.model.IntegeralModel;

/**
 * Created by Administrator on 2018-05-27.
 */

public class IntegeralAdapter extends RecyclerArrayAdapter<IntegeralModel> {
    private  Context context;
    public IntegeralAdapter(Context context) {

        super(context);

        this.context=context;
    }
    @Override
    public IntegeralViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new IntegeralViewHolder(parent,context);
    }
}
