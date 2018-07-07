package com.tongtong.purchaser.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.activity.BaseActivity;
import com.tongtong.purchaser.activity.HongBaoDetailsActivity;
import com.tongtong.purchaser.activity.OrderDetailsActivity;
import com.tongtong.purchaser.activity.PlayWebViewActivity;
import com.tongtong.purchaser.activity.RobRpActivity;
import com.tongtong.purchaser.model.MessageModel;
import com.tongtong.purchaser.utils.Constant;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.widget.CheckableRelativeLayout;
import com.tongtong.purchaser.widget.StyleableToast;


/**
 * Created by zxy on 2018/3/10.
 */

public class MessageViewHolder extends BaseViewHolder<MessageModel> {

    private Context context;
    private TextView title,time,content,details;
    private ImageView red_dot;
    private CheckBox check;
    private CheckableRelativeLayout root;
    public MessageViewHolder(ViewGroup parent, Context context){
        super(parent, R.layout.msg_item);
        this.context=context;
        title=$(R.id.title);
        time=$(R.id.time);
        content=$(R.id.content);
        red_dot=$(R.id.red_dot);
        check=$(R.id.check);
        root=$(R.id.root);
        details=$(R.id.details);
    }
    public void setData(final MessageModel data, boolean is_check, final MessageAdapter adapter, final int position,final int msg_type, final MessageAdapter.OnCheckListener listener) {
        title.setText(data.getTitle());
        content.setText(data.getContent());
        time.setText(data.getAdd_time());
        if(data.getIs_new()==1){
            red_dot.setVisibility(View.VISIBLE);
            title.setTextColor(ContextCompat.getColor(context,R.color.aliwx_common_text_color));
        }else{
            title.setTextColor(ContextCompat.getColor(context,R.color.aliwx_common_text_color2));
            red_dot.setVisibility(View.GONE);
        }
        if(is_check){
            check.setVisibility(View.VISIBLE);
            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    root.toggle();
                    adapter.getItem(position).setIs_check(root.isChecked());
                    if(listener!=null){
                        listener.checkDo(data.getId(),root.isChecked(),position);
                    }
                    adapter.notifyDataSetChanged();
                }
            });
            root.setChecked(data.is_check());
            check.setChecked(data.is_check());
            details.setOnClickListener(null);
        }else{
            root.setOnClickListener(null);
            check.setChecked(false);
            root.setChecked(false);
            check.setVisibility(View.GONE);
            details.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(msg_type== Constant.MSG_TYPE_SYS){
                        Intent intent=new Intent();
                        intent.setClass(context, PlayWebViewActivity.class);
                        intent.putExtra("url",data.getLink());
                        context.startActivity(intent);
                    }else if(msg_type==Constant.MSG_TYPE_RP){
                        opendetails(data.getRel_id());
                    }else if(msg_type==Constant.MSG_TYPE_ORDER){
                        Intent intent=new Intent();
                        intent.setClass(context, OrderDetailsActivity.class);
                        intent.putExtra("order_id",data.getRel_id());
                        context.startActivity(intent);
                    }
                }
            });
        }
    }
    private void opendetails(int id){
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
                intent.setClass(context, HongBaoDetailsActivity.class);
                intent.putExtra("result",str);
                context.startActivity(intent);
            }
            @Override
            public void taskFailed(int code) {
                ((BaseActivity)context).dismissLoading();
                StyleableToast.error(RobRpActivity.getInstance(),"打开详情失败");
            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("token", UserUtil.getUserModel(context).getToken());
        object.addProperty("purchaser_id",UserUtil.getUserModel(context).getId());
        object.addProperty("id",id);
        task.execute(UrlUtil.ROB_RP,object.toString());
    }
}
