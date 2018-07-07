package com.tongtong.purchaser.adapter;

import android.view.View;
import android.widget.TextView;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.model.AccountModel;


/**
 * Created by zxy on 2018/3/14.
 */

public class HeaderViewHolder extends BaseViewHolder<AccountModel> {
    public TextView title;
    public TextView income;
    public HeaderViewHolder(View itemView) {
        super(itemView);
        title = (TextView) itemView.findViewById(R.id.title);
        income = (TextView) itemView.findViewById(R.id.income);
    }
}
