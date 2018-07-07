package com.tongtong.purchaser.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.model.AccountModel;


/**
 * Created by zxy on 2018/3/10.
 */

public class MoneyViewHolder extends BaseViewHolder<AccountModel> {
    private TextView sign,add_time,title;
    private Context context;
    public MoneyViewHolder(ViewGroup parent, Context context){
        super(parent, R.layout.mingxi_list_item);
        sign=$(R.id.sign);
        add_time=$(R.id.add_time);
        title=$(R.id.title);
        this.context=context;
    }

    @Override
    public void setData(AccountModel data) {
        title.setText(data.getDescription());
        add_time.setText(data.getAdd_time());
        if(data.getSign()==1){
            sign.setTextColor(ContextCompat.getColor(context,R.color.rp_msg_red));
            sign.setText("+"+data.getAmount());
        }else{
            sign.setTextColor(ContextCompat.getColor(context,R.color.dark_green));
            sign.setText("-"+data.getAmount());
        }
    }
}
