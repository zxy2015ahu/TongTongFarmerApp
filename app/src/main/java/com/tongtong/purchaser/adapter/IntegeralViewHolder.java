package com.tongtong.purchaser.adapter;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.model.IntegeralModel;

/**
 * Created by zxy on 2018/3/10.
 */

public class IntegeralViewHolder extends BaseViewHolder<IntegeralModel>{
    private TextView title,add_time,sign;
    private Context context;
    public IntegeralViewHolder(ViewGroup parent,Context context){
        super(parent, R.layout.mingxi_list_item);
        title=$(R.id.title);
        add_time=$(R.id.add_time);
        sign=$(R.id.sign);
        this.context=context;
    }

    @Override
    public void setData(IntegeralModel data) {

        title.setText(data.getContent());
        add_time.setText(data.getAdd_time());
        if(data.getSign()==1){
            sign.setTextColor(ContextCompat.getColor(context,R.color.rp_msg_red));
            sign.setText("+"+data.getAmount());
        }else{
            sign.setTextColor(ContextCompat.getColor(context,R.color.dark_green));
            sign.setText("-"+data.getAmount());
        }
        if(data.getIs_new()==1){
            title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.red_oval,0,0,0);
        }else{
            title.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
        }
    }
}
