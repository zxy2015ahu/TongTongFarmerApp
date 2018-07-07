package com.tongtong.purchaser.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.activity.BaseActivity;
import com.tongtong.purchaser.activity.ProduceDetailsViewerActvity;
import com.tongtong.purchaser.model.ReleaseModel;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.NetUtil;
import com.tongtong.purchaser.utils.RelativeDateFormat;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;

/**
 * Created by Administrator on 2018-02-03.
 */

public class PublishViewHolder extends BaseViewHolder<ReleaseModel> {
    ImageView icon,video_view;
    TextView title;
    TextView location;
    TextView time;
    TextView estimatedQuantity;
    Context context;
    TextView price,lastUpdate;
    View root;
    public PublishViewHolder(ViewGroup parent, Context context){
        super(parent, R.layout.farmer_info_list_item);
        icon=$(R.id.icon);
        location=$(R.id.location);
        title=$(R.id.title);
        time=$(R.id.time);
        estimatedQuantity=$(R.id.estimated_quantity);
        price=$(R.id.price);
        lastUpdate=$(R.id.lastUpdate);
        video_view=$(R.id.video_view);
        root=$(R.id.root);
        this.context=context;
    }
    @Override
    public void setData(final ReleaseModel data) {
        if(data==null){
            return;
        }
        Glide.with(context).load(NetUtil.getFullUrl((TextUtils.isEmpty(data.getThumb_img())?data.getReleaseVedioThumb():data.getThumb_img())))
                .placeholder(R.drawable.no_icon).centerCrop().into(icon);
        title.setText(data.getProduce_name());
        location.setText(data.getReleaseLocation());
        estimatedQuantity.setText(RelativeDateFormat.format(data.getReleaseTime()));
        time.setText(data.getStartTime()+"-"+data.getEndTime());
        price.setText(getPriceString(data.getPrice(),data.getPunit()));
        lastUpdate.setText("距离"+String.format("%.1f",data.getDistance())+"公里");
        if(TextUtils.isEmpty(data.getReleaseVedioThumb())){
            video_view.setVisibility(View.GONE);
        }else{
            video_view.setVisibility(View.VISIBLE);
        }
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               getReleaseInfo(data.getId());
            }
        });
    }

    private void getReleaseInfo(int id){
        HttpTask task=new HttpTask(context);
        task.setTaskHandler(new HttpTask.HttpTaskHandler() {
            @Override
            public void taskStart(int code) {
                ((BaseActivity)context).showLoading();
            }
            @Override
            public void taskSuccessful(String str, int code) {
                ((BaseActivity)context).dismissLoading();
                Intent intent=new Intent();
                intent.setClass(context, ProduceDetailsViewerActvity.class);
                intent.putExtra("data",str);
                context.startActivity(intent);
            }
            @Override
            public void taskFailed(int code) {
                ((BaseActivity)context).dismissLoading();
            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("produce_id",id);
        object.addProperty("purchaser_id", UserUtil.getUserModel(context).getId());
        task.execute(UrlUtil.GET_FARMER_RELEASE,object.toString());
    }
    private SpannableStringBuilder getString(String text,String unit){
        SpannableStringBuilder builder=new SpannableStringBuilder();
        builder.append("×");
        builder.append(text);
        builder.append(unit);
        RelativeSizeSpan sizeSpan=new RelativeSizeSpan(1.4f);
        builder.setSpan(sizeSpan,1,builder.length()-unit.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return builder;
    }
    private SpannableStringBuilder getPriceString(double price,String unit){
        SpannableStringBuilder builder=new SpannableStringBuilder();
        builder.append(String.valueOf(price));
        RelativeSizeSpan sizeSpan=new RelativeSizeSpan(1.5f);
        builder.setSpan(sizeSpan,0,builder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.append("元/");
        builder.append(unit);
        return builder;
    }
}
