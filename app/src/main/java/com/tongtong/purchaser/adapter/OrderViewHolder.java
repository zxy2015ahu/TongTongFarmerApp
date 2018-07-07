package com.tongtong.purchaser.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.mobileim.conversation.YWConversation;
import com.bumptech.glide.Glide;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.activity.ChattingActivity;
import com.tongtong.purchaser.application.MyApplication;
import com.tongtong.purchaser.helper.ChattingOperationCustom;
import com.tongtong.purchaser.model.OrderModel;
import com.tongtong.purchaser.utils.Constant;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;

/**
 * Created by Administrator on 2018-02-03.
 */

public class OrderViewHolder extends BaseViewHolder<OrderModel> {
    TextView price,amount,dingjin,purchaser,status,add_time,produce_name,order_no,total;
    ImageView img;
    Button contact;
    Context context;
    public OrderViewHolder(ViewGroup parent, Context context){
        super(parent, R.layout.item_order);
        price=$(R.id.price);
        amount=$(R.id.amount);
        dingjin=$(R.id.dingjin);
        add_time=$(R.id.add_time);
        purchaser=$(R.id.purchaser);
        status=$(R.id.status);
        img=$(R.id.img);
        produce_name=$(R.id.produce_name);
        contact=$(R.id.contact);
        order_no=$(R.id.order_no);
        total=$(R.id.total);
        this.context=context;
    }
    @Override
    public void setData(final OrderModel data) {
        Glide.with(context).load(UrlUtil.IMG_SERVER_URL+data.getIcon_url())
                .placeholder(R.drawable.no_icon).centerCrop().into(img);
        price.setText(data.getPrice()+"元/"+data.getUnit());
        dingjin.setText("订金"+data.getDingjin()+"元");
        amount.setText("×"+data.getAmount()+data.getUnit());
        add_time.setText(data.getAdd_time());
        purchaser.setText(data.getPurchaser_name());
        status.setText(data.getStatus_name());
        add_time.setText(data.getAdd_time());
        order_no.setText("订单号："+data.getOrder_no());
        purchaser.setText("卖方："+data.getFarmer_name());
        produce_name.setText(data.getProduce_name());
        if(data.getStatus()==3||data.getStatus()==4){
            total.setVisibility(View.VISIBLE);
            total.setText(getString(data.getJinzhong(),data.getTotal(),data.getUnit(),context.getResources().getColor(R.color.colorPrimary)));
        }else{
            total.setVisibility(View.GONE);
        }
        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initCustomView(data);
                final String target = data.getFarmer_phone(); //消息接收者ID
                final String appkey = Constant.TARGET_APP_KEY; //消息接收者appKey
                Intent intent = new Intent();
                intent.setClass(context, ChattingActivity.class);
                intent.putExtra(ChattingActivity.TARGET_ID,target);
                intent.putExtra(ChattingActivity.TARGET_APP_KEY,appkey);
                context.startActivity(intent);
            }
        });
    }
    private void initCustomView(final OrderModel data){
        View custom_view= LayoutInflater.from(context).inflate(R.layout.my_cus_chat_view,null);
        ImageView img=(ImageView) custom_view.findViewById(R.id.img);
        TextView name=(TextView) custom_view.findViewById(R.id.name);
        //TextView chanliang=(TextView) custom_view.findViewById(R.id.chanliang);
        TextView address=(TextView) custom_view.findViewById(R.id.address);
        Glide.with(context).load(UrlUtil.IMG_SERVER_URL+data.getIcon_url()).placeholder(R.drawable.no_icon).into(img);
        name.setText(data.getProduce_name());
        //chanliang.setText("数量："+data.getAmount()+data.getUnit());
        address.setText("订单状态："+data.getStatus_name());
        Button book=(Button) custom_view.findViewById(R.id.book);
        book.setText("发送\n订单");
        book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserUtil.getIMKitInstance(context).hideCustomView();
                String userid=data.getFarmer_phone();
                YWConversation conversation= MyApplication.getConversation(userid);
                if(conversation!=null){
                    conversation.getMessageSender().sendMessage(ChattingOperationCustom.createCustomSendOrderMessage(data),120,null);
                    ChattingOperationCustom.sendTransMsg("对方发来一条订单信息",userid);
                    ChattingOperationCustom.sendSysMsg("订单信息已发送",userid);
                }
            }
        });
        UserUtil.getIMKitInstance(context).showCustomView(custom_view);
    }
    private SpannableStringBuilder getString(int jinzhong,double total,String unit,int color){
        SpannableStringBuilder builder=new SpannableStringBuilder();
        builder.append("净重");
        String jz=jinzhong+"";
        builder.append(jz);
        RelativeSizeSpan sizeSpan=new RelativeSizeSpan(1.2f);
        ForegroundColorSpan colorSpan=new ForegroundColorSpan(color);
        builder.setSpan(sizeSpan,builder.length()-jz.length(),builder.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.setSpan(colorSpan,builder.length()-jz.length(),builder.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.append(unit);
        builder.append(" ");
        builder.append("合计：");
        String to=total+"";
        builder.append(to);
        RelativeSizeSpan sizeSpan2=new RelativeSizeSpan(1.2f);
        ForegroundColorSpan colorSpan2=new ForegroundColorSpan(color);
        builder.setSpan(sizeSpan2,builder.length()-to.length(),builder.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.setSpan(colorSpan2,builder.length()-to.length(),builder.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.append("元");
        return builder;
    }
}
