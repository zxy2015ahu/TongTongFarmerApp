package com.tongtong.purchaser.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.tongtong.purchaser.model.HongBaoModel;

/**
 * Created by zxy on 2018/3/10.
 */

public class HongBaoRecordAdapter extends RecyclerArrayAdapter<HongBaoModel> {
    private Context ctx;
    public HongBaoRecordAdapter(Context context) {

        super(context);
        this.ctx=context;

    }
    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new HongbaoRecordViewHolder(parent,ctx);
    }
}
