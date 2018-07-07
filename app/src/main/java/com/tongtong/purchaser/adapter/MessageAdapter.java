package com.tongtong.purchaser.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.tongtong.purchaser.model.MessageModel;

/**
 * Created by zxy on 2018/3/10.
 */

public class MessageAdapter extends RecyclerArrayAdapter<MessageModel>{
    private Context ctx;
    private boolean is_check;
    private int msg_type;
    public interface OnCheckListener{
        public void checkDo(int id, boolean check, int position);
    }
    private OnCheckListener listener;
    public void setOnCheckListener(OnCheckListener listener){
        this.listener=listener;
    }
    public MessageAdapter(Context context,int msg_type) {

        super(context);
        this.ctx=context;
        this.msg_type=msg_type;
    }
    public void showCheck(boolean check){
        this.is_check=check;
        notifyDataSetChanged();
    }
    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new MessageViewHolder(parent,ctx);
    }

    @Override
    public void OnBindViewHolder(BaseViewHolder holder, int position) {
        ((MessageViewHolder)holder).setData(getItem(position),is_check,this,position,msg_type,listener);
    }
}
