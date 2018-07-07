package com.tongtong.purchaser.helper;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.mobileim.YWIMKit;
import com.alibaba.mobileim.aop.Pointcut;
import com.alibaba.mobileim.aop.custom.IMChattingBizService;
import com.alibaba.mobileim.aop.custom.IMChattingPageUI;
import com.alibaba.mobileim.contact.IYWContact;
import com.alibaba.mobileim.conversation.IYWMessageListener;
import com.alibaba.mobileim.conversation.YWConversation;
import com.alibaba.mobileim.conversation.YWConversationType;
import com.alibaba.mobileim.conversation.YWMessage;
import com.alibaba.mobileim.conversation.YWP2PConversationBody;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.activity.FarmerInfoActivity;
import com.tongtong.purchaser.model.FarmerModel;
import com.tongtong.purchaser.utils.CodeUtil;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.widget.AlertDialog;

import org.json.JSONObject;


public class ChattingUICustom extends IMChattingPageUI {

    public static YWConversation conversation;
    private TextView textView;
    private String title;
    public ChattingUICustom(Pointcut pointcut) {
        super(pointcut);
    }
    /**
     * isv需要返回自定义的view. openIMSDK会回调这个方法，获取用户设置的view. Fragment 聊天界面的fragment
     */
    @Override
    public View getCustomTitleView(final Fragment fragment,
                                   final Context context, LayoutInflater inflater,
                                   final YWConversation conversation) {
        View view = inflater.inflate(R.layout.title_bar_back_text_dial, new RelativeLayout(context),false);
        textView = (TextView) view.findViewById(R.id.title_text);
        if (conversation.getConversationType() == YWConversationType.P2P) {
            YWP2PConversationBody conversationBody = (YWP2PConversationBody) conversation
                    .getConversationBody();
            if (!TextUtils.isEmpty(conversationBody.getContact().getShowName())) {
                title = conversationBody.getContact().getShowName();
            } else {

                YWIMKit imKit = UserUtil.getIMKitInstance(context);
                IYWContact contact = imKit.getContactService().getContactProfileInfo(conversationBody.getContact().getUserId(), conversationBody.getContact().getAppKey());
                //生成showName，According to id。

                if (contact != null && !TextUtils.isEmpty(contact.getShowName())) {
                    title = contact.getShowName();
                }
            }
            //如果标题为空，那么直接使用Id
            if (TextUtils.isEmpty(title)) {
                title = conversationBody.getContact().getUserId();
            }
            View right_bn =  view.findViewById(R.id.right_bn);
            right_bn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    YWP2PConversationBody conversationBody = (YWP2PConversationBody) conversation
                            .getConversationBody();
                    getFarmerInfo(context,conversationBody.getContact().getUserId());
                }
            });
        }else if(conversation.getConversationType()==YWConversationType.SHOP){
            title="与客服聊天中";
            ImageView phone=(ImageView) view.findViewById(R.id.phone);
            phone.setImageResource(R.drawable.vector_drawable_clear);
            view.findViewById(R.id.right_bn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog dialog=new AlertDialog(context).builder();
                    dialog.setTitle("提示");
                    dialog.setMsg("确定清空所有聊天消息？");
                    dialog.setNegativeButton("取消", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    });
                    dialog.setPositiveButton("确定", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            conversation.getMessageLoader().deleteAllMessage();
                        }
                    });
                    dialog.show();
                }
            });
        }
        textView.setText(title);
        View backView =  view.findViewById(R.id.back_bn);
        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                fragment.getActivity().finish();

            }
        });
        return view;
    }
    private void getFarmerInfo(final Context context,String phone){
        HttpTask task=new HttpTask(context);
        task.setTaskHandler(new HttpTask.HttpTaskHandler() {
            @Override
            public void taskStart(int code) {

            }

            @Override
            public void taskSuccessful(String str, int code) {
                JsonObject refreshResultJson = new JsonParser().parse(str)
                        .getAsJsonObject();
                int refreshResultCode = refreshResultJson.get("code").getAsInt();
                JsonObject farmer=refreshResultJson.get("farmer").getAsJsonObject();
                if(refreshResultCode== CodeUtil.SUCCESS_CODE){
                    FarmerModel fm=new FarmerModel();
                    fm.setName(farmer.get("name").getAsString());
                    fm.setPhone(farmer.get("phone").getAsString());
                    fm.setCardid(farmer.get("cardid").getAsString());
                    fm.setHeadUrl(farmer.get("headUrl").getAsString());
                    fm.setId(farmer.get("id").getAsInt());
                    fm.setAddressStr(farmer.get("addressStr").getAsString());
                    Intent intent=new Intent();
                    intent.putExtra("farmer",fm);
                    intent.setClass(context,FarmerInfoActivity.class);
                    context.startActivity(intent);
                }
            }

            @Override
            public void taskFailed(int code) {

            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("phone",phone);
        task.execute(UrlUtil.GET_FARMER_INFO,object.toString());
    }
    private IYWMessageListener iywMessageListener=new IYWMessageListener() {
        @Override
        public void onItemUpdated() {

        }
        @Override
        public void onItemComing() {

        }
        @Override
        public void onInputStatus(byte b) {
            if(b==1){
                textView.setText("对方正在输入...");
            }else{
                textView.setText(title);
            }
        }
    };
    @Override
    public int getMsgBackgroundResId(YWConversation conversation, YWMessage message, boolean self) {
        int msgType = message.getSubType();
        if (msgType == YWMessage.SUB_MSG_TYPE.IM_P2P_CUS || msgType == YWMessage.SUB_MSG_TYPE.IM_TRIBE_CUS) {
            String mType = null;
            try {
                String content = message.getMessageBody().getContent();
                JSONObject object = new JSONObject(content);
                mType = object.getString("customizeMessageType");
            } catch (Exception e) {

            }
            if(ChattingOperationCustom.CustomMessageType.GEO.equals(mType)){
                if (self) {
                    return R.drawable.wx_left_chatting_bg;
                } else {
                    return R.drawable.wx_right_chatting_bg;
                }
            }else if(ChattingOperationCustom.CustomMessageType.SEND_GOODS.equals(mType)){
                if(self){
                    return R.drawable.wx_left_chatting_bg;
                }else{
                    return R.drawable.wx_right_chatting_bg;
                }
            }else if(ChattingOperationCustom.CustomMessageType.ORDER.equals(mType)){
                return -1;//透明背景
            }
        }
        return super.getMsgBackgroundResId(conversation, message, self);
    }

    @Override
    public void onDestroy(Fragment fragment, YWConversation conversation) {
        super.onDestroy(fragment, conversation);
        conversation.getMessageLoader().removeMessageListener(iywMessageListener);
        ChattingUICustom.conversation=null;
    }

    @Override
    public void onInitFinished(final IMChattingBizService bizService){
        conversation=bizService.getConversation();
        conversation.getMessageLoader().addMessageListener(iywMessageListener);
    }
    @Override
    public void modifyRightItemParentViewAfterSetValue(YWMessage msg, RelativeLayout rightItemParentView, Fragment fragment, YWConversation conversation) {
        if(msg!=null&&rightItemParentView!=null&&(msg.getSubType()==YWMessage.SUB_MSG_TYPE.IM_P2P_CUS)){
            String msgType = null;
            try {
                String content = msg.getMessageBody().getContent();
                JSONObject object = new JSONObject(content);
                msgType = object.getString("customizeMessageType");
            } catch (Exception e) {

            }
            if(ChattingOperationCustom.CustomMessageType.ORDER.equals(msgType)){
                rightItemParentView.setPadding(rightItemParentView.getPaddingLeft(), rightItemParentView.getPaddingTop(), rightItemParentView.getPaddingRight(), 0);
            }
        }
    }
    /**
     * 返回单聊默认头像资源Id
     *
     * @return 0:使用SDK默认提供的
     */
    @Override
    public int getDefaultHeadImageResId() {
        return R.drawable.default_head;
    }
}
