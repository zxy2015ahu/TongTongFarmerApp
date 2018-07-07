package com.tongtong.purchaser.adapter;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.model.HongbaoSendModel;

/**
 * Created by zxy on 2018/3/10.
 */

public class HongbaoSendViewHolder extends BaseViewHolder<HongbaoSendModel>{
    private TextView tv_money_type,tv_time,tv_item_money_amount,tv_item_status;
    private Context context;
    public HongbaoSendViewHolder(ViewGroup parent, Context context){
        super(parent, R.layout.rp_send_record_list_item);
        tv_money_type=$(R.id.tv_money_type);
        tv_time=$(R.id.tv_time);
        tv_item_money_amount=$(R.id.tv_item_money_amount);
        tv_item_status=$(R.id.tv_item_status);
        this.context=context;
    }

    @Override
    public void setData(HongbaoSendModel data) {
        tv_money_type.setText("拼手气红包");
        tv_time.setText(data.getAdd_time());
        tv_item_money_amount.setText(String.valueOf(data.getTotal_amount())+"元");
        if(data.getStatus()==1){
            tv_item_status.setText("已领取"+data.getYiling()+"/"+data.getTotal_num()+"个");
        }else{
            tv_item_status.setText("已领完"+data.getYiling()+"/"+data.getTotal_num()+"个");
        }
    }
}
