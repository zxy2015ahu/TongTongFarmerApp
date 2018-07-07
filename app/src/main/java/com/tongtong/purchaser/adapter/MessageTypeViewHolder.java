package com.tongtong.purchaser.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.model.MessageTypeModel;
import com.tongtong.purchaser.utils.UrlUtil;


/**
 * Created by zxy on 2018/3/10.
 */

public class MessageTypeViewHolder extends BaseViewHolder<MessageTypeModel>{

    private Context context;
    private TextView title,time,content;
    private TextView count;
    private ImageView head;
    public MessageTypeViewHolder(ViewGroup parent, Context context){
        super(parent, R.layout.msg_type_item);
        this.context=context;
        title=$(R.id.title);
        time=$(R.id.time);
        content=$(R.id.content);
        count=$(R.id.count);
        head=$(R.id.head);
    }
    @Override
    public void setData(MessageTypeModel data) {
        if(data.getUnread_count()>0){
            count.setVisibility(View.VISIBLE);
            count.setText(String.valueOf(data.getUnread_count()));
        }else{
            count.setVisibility(View.GONE);
        }
        Glide.with(context).load(UrlUtil.IMG_SERVER_URL+data.getIcon())
                .placeholder(R.drawable.no_icon).into(head);
        title.setText(data.getTitle());
        content.setText(TextUtils.isEmpty(data.getContent())?"暂无消息":data.getContent());
        time.setText(data.getAdd_time());
    }
}
