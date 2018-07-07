package com.tongtong.purchaser.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.model.HongBaoModel;
import com.tongtong.purchaser.utils.NetUtil;

/**
 * Created by zxy on 2018/3/10.
 */

public class HongbaoRecordViewHolder extends BaseViewHolder<HongBaoModel>{
    private ImageView iv_item_avatar_icon,iv_random_icon;
    private TextView tv_money_to_user,tv_time,tv_item_money_amount,tv_best_icon;
    private Context context;
    public HongbaoRecordViewHolder(ViewGroup parent, Context context){
        super(parent, R.layout.record_list_item);
        iv_item_avatar_icon=$(R.id.iv_record_avatar_icon);
        tv_money_to_user=$(R.id.tv_money_sender);
        iv_random_icon=$(R.id.iv_random_icon);
        tv_time=$(R.id.tv_time);
        tv_item_money_amount=$(R.id.tv_item_money_amount);
        tv_best_icon=$(R.id.tv_best_icon);
        this.context=context;
    }

    @Override
    public void setData(HongBaoModel data) {
        Glide.with(context).load(NetUtil.getFullUrl(data.getHeadUrl()))
                .asBitmap().centerCrop().placeholder(R.drawable.rp_avatar)
                .into(new BitmapImageViewTarget(iv_item_avatar_icon){
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        iv_item_avatar_icon.setImageDrawable(circularBitmapDrawable);
                    }
                });
        iv_random_icon.setVisibility(View.VISIBLE);
        tv_money_to_user.setText(data.getName());
        tv_time.setText(data.getTime());
        tv_item_money_amount.setText(String.valueOf(data.getAmount())+"元");
        if(data.is_best()==1){
            tv_best_icon.setVisibility(View.VISIBLE);
        }else{
            tv_best_icon.setVisibility(View.GONE);
        }
    }
}
