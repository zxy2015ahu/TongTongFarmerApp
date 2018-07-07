package com.tongtong.purchaser.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.model.AccountModel;

import java.util.List;

/**
 * Created by zxy on 2018/3/10.
 */

public class MoneyAdapter extends RecyclerArrayAdapter<AccountModel> implements StickyRecyclerHeadersAdapter<HeaderViewHolder> {
    private Context ctx;
    public MoneyAdapter(Context context) {

        super(context);
        this.ctx=context;

    }
    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new MoneyViewHolder(parent,ctx);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public long getHeaderId(int position) {
        if(position>=getCount()){
            return -1;
        }
        return getItem(position).getHeader_id();
    }

    @Override
    public HeaderViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        final View view = View.inflate(ctx, R.layout.header_item,null);
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(HeaderViewHolder holder, int position) {
        AccountModel item=getItem(position);
        holder.title.setText(item.getYear()+"年"+item.getMonth()+"月");
        holder.income.setText("支出￥"+item.getOutcome()+"\t"+"收入￥"+item.getIncome());
    }
}
