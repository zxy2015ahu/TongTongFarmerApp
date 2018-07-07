package com.tongtong.purchaser.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.tongtong.purchaser.model.OrderModel;

/**
 * Created by Administrator on 2018-02-03.
 */

public class OrderAdapter extends RecyclerArrayAdapter<OrderModel> {
    private Context ctx;
    public OrderAdapter(Context context) {

        super(context);
        this.ctx=context;

    }
    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new OrderViewHolder(parent,ctx);
    }
}
