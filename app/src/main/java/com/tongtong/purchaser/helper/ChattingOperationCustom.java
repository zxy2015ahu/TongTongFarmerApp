package com.tongtong.purchaser.helper;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.ClipboardManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.mobileim.aop.Pointcut;
import com.alibaba.mobileim.aop.custom.IMChattingPageOperateion;
import com.alibaba.mobileim.aop.model.ReplyBarItem;
import com.alibaba.mobileim.aop.model.YWChattingPlugin;
import com.alibaba.mobileim.channel.util.WxLog;
import com.alibaba.mobileim.channel.util.YWLog;
import com.alibaba.mobileim.conversation.YWConversation;
import com.alibaba.mobileim.conversation.YWConversationType;
import com.alibaba.mobileim.conversation.YWCustomMessageBody;
import com.alibaba.mobileim.conversation.YWMessage;
import com.alibaba.mobileim.conversation.YWMessageBody;
import com.alibaba.mobileim.conversation.YWMessageChannel;
import com.alibaba.mobileim.fundamental.widget.WxAlertDialog;
import com.alibaba.mobileim.kit.contact.YWContactHeadLoadHelper;
import com.alibaba.mobileim.utility.IMNotificationUtils;
import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.activity.GuobangDetailsActivity;
import com.tongtong.purchaser.activity.LocationSelectActivity;
import com.tongtong.purchaser.activity.NavigateActivity;
import com.tongtong.purchaser.activity.OrderDetailsActivity;
import com.tongtong.purchaser.activity.ProduceDetailsActivity;
import com.tongtong.purchaser.application.MyApplication;
import com.tongtong.purchaser.model.OrderModel;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.tongtong.purchaser.R.id.jinzhong;
import static com.tongtong.purchaser.R.id.price;
import static com.tongtong.purchaser.R.id.status;

/**
 * 聊天界面(单聊和群聊界面)的定制点(根据需要实现相应的接口来达到自定义聊天界面)，不设置则使用openIM默认的实现
 * 1.CustomChattingTitleAdvice 自定义聊天窗口标题 2. OnUrlClickChattingAdvice 自定义聊天窗口中
 * 当消息是url是点击的回调。用于isv处理url的打开处理。不处理则用第三方浏览器打开 如果需要定制更多功能，需要实现更多开放的接口
 * 需要.继承BaseAdvice .实现相应的接口
 * <p/>
 * 另外需要在Application中绑定
 * AdviceBinder.bindAdvice(PointCutEnum.CHATTING_FRAGMENT_POINTCUT,
 * ChattingOperationCustomSample.class);
 *
 * @author jing.huai
 */
public class ChattingOperationCustom extends IMChattingPageOperateion {

    private static final String TAG = "ChattingOperationCustom";
    public class CustomMessageType {
        public static final String GREETING = "Greeting";
        public static final String CARD = "CallingCard";
        public static final String IMAGE = "PrivateImage";
        public static final String GEO="SendGeo";
        public static final String ORDER="SendOrder";
        public static final String SEND_GOODS="SendGoods";
        public static final String CANCELL_ORDER="CancellOrder";
        public static final String ORDER_OPRATION="OrderOpration";
        public static final String ORDER_OPRATION_REJECT="OrderOprationReject";
        public static final String ORDER_OPRATION_APPROVE_DINGJIN="OrderOprationApproveDingjin";
        public static final String ORDER_TIP="OrderTip";
        public static final String FINAL_ORDER="FinalOrder";
        public static final String QUEREN_SONGDA="QuerenSongda";
        public static final String SHOUGOU_OPRATION="ShougouOpration";
        public static final String SHOUGOU_SONGDA_REJECT="ShougouOprationReject";
        public static final String SEND_ORDER="SendMyOrder";
    }


    // 默认写法
    public ChattingOperationCustom(Pointcut pointcut) {
        super(pointcut);
    }

    /**
     * 单聊ui界面，点击url的事件拦截 返回true;表示自定义处理，返回false，由默认处理
     *
     * @param fragment 可以通过 fragment.getActivity拿到Context
     * @param message  点击的url所属的message
     * @param url      点击的url
     */
    @Override
    public boolean onUrlClick(Fragment fragment, YWMessage message, String url,
                              YWConversation conversation) {
        IMNotificationUtils.getInstance().showToast(fragment.getActivity(), "用户点击了url:" + url);
        if(!url.startsWith("http")) {
            url = "http://" + url;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        fragment.startActivity(intent);

        return true;
    }
    /**
     * 发送单聊地理位置消息
     */
    public static void sendSysMsg(String text,String userid){
        YWMessage localSysmsg= YWMessageChannel.createLocalSystemMessage(text);
        localSysmsg.setIsLocal(false);
        YWConversation conversation=MyApplication.getConversation(userid);
        if(conversation!=null){
            conversation.getMessageSender().sendMessage(localSysmsg,120,null);
        }
    }

    /**
     * 定制点击消息事件, 每一条消息的点击事件都会回调该方法，开发者根据消息类型，对不同类型的消息设置不同的点击事件
     * @param fragment  聊天窗口fragment对象
     * @param message   被点击的消息
     * @return true:使用用户自定义的消息点击事件，false：使用默认的消息点击事件
     */
    @Override
    public boolean onMessageClick(final Fragment fragment, final YWMessage message) {
        if (message.getSubType() == YWMessage.SUB_MSG_TYPE.IM_TEXT){
//            IMNotificationUtils.getInstance().showToast(fragment.getActivity(), "你点击了文本消息");
//            return true;
        } else if (message.getSubType() == YWMessage.SUB_MSG_TYPE.IM_GEO){
            IMNotificationUtils.getInstance().showToast(fragment.getActivity(), "你点击了地理位置消息");
            return true;
        } else if (message.getSubType() == YWMessage.SUB_MSG_TYPE.IM_P2P_CUS || message.getSubType() == YWMessage.SUB_MSG_TYPE.IM_TRIBE_CUS){
            String msgType = null;
            JSONObject object=null;
            try {
                String content = message.getMessageBody().getContent();
                object = new JSONObject(content);
                msgType = object.getString("customizeMessageType");
            } catch (Exception e) {

            }
            WxLog.d(TAG, "msgType = " + msgType);
            if (!TextUtils.isEmpty(msgType) && msgType.equals(CustomMessageType.IMAGE)){
               return false;
            } else {
                if(CustomMessageType.GEO.equals(msgType)){
                    Intent intent=new Intent();
                    intent.setClass(fragment.getActivity(), NavigateActivity.class);
                    intent.putExtra("title",object.optString("title"));
                    intent.putExtra("address",object.optString("address"));
                    intent.putExtra("lat",object.optDouble("lat"));
                    intent.putExtra("lng",object.optDouble("lng"));
                    fragment.startActivity(intent);
                }
                //IMNotificationUtils.getInstance().showToast(fragment.getActivity(), "你点击了自定义消息");
            }
            return true;
        }
        return false;
    }
    public static YWMessage createCustomOprationOrderMessage(String name,double price,int amount,int order_id,String unit,String desc,int status){
        // 发送自定义消息
        YWCustomMessageBody messageBody = new YWCustomMessageBody();
        // 请注意这里不一定要是JSON格式，这里纯粹是为了演示的需要
        JSONObject object = new JSONObject();
        try {
            object.put("customizeMessageType", CustomMessageType.ORDER_OPRATION);
            object.put("name",name);
            object.put("price",price);
            object.put("amount",amount);
            object.put("order_id",order_id);
            object.put("unit",unit);
            object.put("desc",desc);
            object.put("status",status);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        messageBody.setContent(object.toString());// 用户要发送的自定义消息，SDK不关心具体的格式，比如用户可以发送JSON格式
        messageBody.setSummary("蔬菜预订交售单");// 可以理解为消息的标题，用于显示会话列表和消息通知栏
        // 注意，这里是单聊自定义消息
        return YWMessageChannel.createCustomMessage(messageBody);
    }


    /**
     * 开发者可以根据用户操作设置该值
     */
    private static boolean mUserInCallMode = false;


    /**
     * 是否使用听筒模式播放语音消息
     *
     * @param fragment
     * @param message
     * @return true：使用听筒模式， false：使用扬声器模式
     */
    @Override
    public boolean useInCallMode(Fragment fragment, YWMessage message) {
        return mUserInCallMode;
    }

    /**
     * 当打开聊天窗口时，自动发送该字符串给对方
     * @param fragment      聊天窗口fragment
     * @param conversation  当前会话
     * @return 自动发送的内容（注意，内容empty则不自动发送）
     */
    @Override
    public String messageToSendWhenOpenChatting(Fragment fragment, YWConversation conversation) {
        //p2p、客服和店铺会话处理，否则不处理，
        int mCvsType = conversation.getConversationType().getValue();
        if (mCvsType == YWConversationType.P2P.getValue() || mCvsType == YWConversationType.SHOP.getValue()) {
           return null;
        } else {
            return null;
        }

    }

    /***************** 以下是定制自定义消息view的示例代码 ****************/

    //自定义消息view的种类数
    private final int typeCount = 17;

    /** 自定义viewType，viewType的值必须从0开始，然后依次+1递增，且viewType的个数必须等于typeCount，切记切记！！！***/
    //地理位置消息
    private final int type_0 = 0;

    //群自定义消息(Say-Hi消息)
    private final int type_1 = 1;

    //单聊自定义消息(名片消息)
    private final int type_2 = 2;

    //单聊阅后即焚消息
    private final int type_3 = 3;


    private final int type_4=4;

    private final int type_5=5;
    private final int type_6=6;
    private final int type_7=7;
    private final int type_8=8;
    private final int type_9=9;
    private final int type_10=10;
    private final int type_11=11;
    private final int type_12=12;
    private final int type_13=13;
    private final int type_14=14;
    private final int type_15=15;
    private final int type_16=16;
    /**
     * 自定义消息view的种类数
     * @return  自定义消息view的种类数
     */
    @Override
    public int getCustomViewTypeCount() {
        return typeCount;
    }

    /**
     * 自定义消息view的类型，开发者可以根据自己的需求定制多种自定义消息view，这里需要根据消息返回view的类型
     * @param message 需要自定义显示的消息
     * @return  自定义消息view的类型
     */
    @Override
    public int getCustomViewType(YWMessage message) {
        if (message.getSubType() == YWMessage.SUB_MSG_TYPE.IM_GEO){
            //return type_0;
            return super.getCustomViewType(message);
        }else if (message.getSubType() == YWMessage.SUB_MSG_TYPE.IM_P2P_CUS || message.getSubType() == YWMessage.SUB_MSG_TYPE.IM_TRIBE_CUS) {
            String msgType = null;
            try {
                String content = message.getMessageBody().getContent();
                JSONObject object = new JSONObject(content);
                msgType = object.getString("customizeMessageType");
            } catch (Exception e) {

            }
            if (!TextUtils.isEmpty(msgType)) {
                if (msgType.equals(CustomMessageType.GREETING)) {
                    return type_1;
                } else if (msgType.equals(CustomMessageType.CARD)) {
                    return type_2;
                } else if (msgType.equals(CustomMessageType.IMAGE)){
                    return type_3;
                }else if(msgType.equals(CustomMessageType.GEO)){
                    return type_4;
                }else if(msgType.equals(CustomMessageType.ORDER)){
                    return type_5;
                }else if(msgType.equals(CustomMessageType.ORDER_OPRATION)){
                    return type_6;
                }else if(msgType.equals(CustomMessageType.ORDER_TIP)){
                    return type_7;
                }else if(msgType.equals(CustomMessageType.FINAL_ORDER)){
                    return type_8;
                }else if(msgType.equals(CustomMessageType.QUEREN_SONGDA)){
                    return type_9;
                }else if(msgType.equals(CustomMessageType.SHOUGOU_OPRATION)){
                    return type_10;
                }else if(msgType.equals(CustomMessageType.CANCELL_ORDER)){
                    return type_11;
                }else if(msgType.equals(CustomMessageType.ORDER_OPRATION_APPROVE_DINGJIN)){
                    return type_12;
                }else if(msgType.equals(CustomMessageType.ORDER_OPRATION_REJECT)){
                    return type_13;
                }else if(msgType.equals(CustomMessageType.SHOUGOU_SONGDA_REJECT)){
                    return type_14;
                }else if(msgType.equals(CustomMessageType.SEND_ORDER)){
                    return type_15;
                }else if(msgType.equals(CustomMessageType.SEND_GOODS)){
                    return type_16;
                }
            }
        }
        return super.getCustomViewType(message);
    }
    //第一步：用户创建自定义消息
    public static YWMessage createCustomGeoMessage(double lat,double lng,String thumb,String title,String address) {
        // 发送自定义消息
        YWCustomMessageBody messageBody = new YWCustomMessageBody();
        // 请注意这里不一定要是JSON格式，这里纯粹是为了演示的需要
        JSONObject object = new JSONObject();
        try {
            object.put("customizeMessageType", CustomMessageType.GEO);
            object.put("lat",lat);
            object.put("lng",lng);
            object.put("thumb",thumb);
            object.put("title",title);
            object.put("address",address);
            object.put("userid",UserUtil.getUserModel(MyApplication.instance).getPhone());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        messageBody.setContent(object.toString());// 用户要发送的自定义消息，SDK不关心具体的格式，比如用户可以发送JSON格式
        messageBody.setSummary("位置信息");// 可以理解为消息的标题，用于显示会话列表和消息通知栏
        // 注意，这里是单聊自定义消息
        return YWMessageChannel.createCustomMessage(messageBody);
    }
    public static YWMessage createCustomOrderMessage(String name,double price,int amount,int order_id,String unit,int dingjin,String userid,
            double lat,double lng,String address_title,String address_content){
        // 发送自定义消息
        YWCustomMessageBody messageBody = new YWCustomMessageBody();
        // 请注意这里不一定要是JSON格式，这里纯粹是为了演示的需要
        JSONObject object = new JSONObject();
        try {
            object.put("customizeMessageType", CustomMessageType.ORDER);
            object.put("name",name);
            object.put("price",price);
            object.put("amount",amount);
            object.put("order_id",order_id);
            object.put("unit",unit);
            object.put("userid",userid);
            object.put("dingjin",dingjin);
            object.put("lat",lat);
            object.put("lng",lng);
            object.put("address_title",address_title);
            object.put("address_content",address_content);
            messageBody.setExtraData(new JSONObject().put("status",0));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        messageBody.setContent(object.toString());// 用户要发送的自定义消息，SDK不关心具体的格式，比如用户可以发送JSON格式
        messageBody.setSummary("有收购人向你发来预订单");// 可以理解为消息的标题，用于显示会话列表和消息通知栏
        // 注意，这里是单聊自定义消息
        return YWMessageChannel.createCustomMessage(messageBody);
    }
    //第一步：用户创建自定义消息
    public static YWMessage createGoodsMessage(int id,String name,int amount,String aunit,double price,String punit,String thumb) {
        // 发送自定义消息
        YWCustomMessageBody messageBody = new YWCustomMessageBody();
        // 请注意这里不一定要是JSON格式，这里纯粹是为了演示的需要
        JSONObject object = new JSONObject();
        try {
            object.put("customizeMessageType", CustomMessageType.SEND_GOODS);
            object.put("name",name);
            object.put("id",id);
            object.put("amount",amount);
            object.put("aunit",aunit);
            object.put("punit",punit);
            object.put("price",price);
            object.put("thumb",thumb);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        messageBody.setContent(object.toString());// 用户要发送的自定义消息，SDK不关心具体的格式，比如用户可以发送JSON格式
        messageBody.setSummary("货品信息");// 可以理解为消息的标题，用于显示会话列表和消息通知栏
        // 注意，这里是单聊自定义消息
        return YWMessageChannel.createCustomMessage(messageBody);
    }
    public static YWMessage createCustomFinalOrderMessage(String name,int jinzhong,double total,int order_id,String unit,String userid,double price){
        // 发送自定义消息
        YWCustomMessageBody messageBody = new YWCustomMessageBody();
        // 请注意这里不一定要是JSON格式，这里纯粹是为了演示的需要
        JSONObject object = new JSONObject();
        try {
            object.put("customizeMessageType", CustomMessageType.FINAL_ORDER);
            object.put("name",name);
            object.put("jinzhong",jinzhong);
            object.put("total",total);
            object.put("order_id",order_id);
            object.put("price",price);
            object.put("userid",userid);
            object.put("unit",unit);
            messageBody.setExtraData(new JSONObject().put("status",0));
            messageBody.setSummary("你已开具收购单");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        messageBody.setContent(object.toString());// 用户要发送的自定义消息，SDK不关心具体的格式，比如用户可以发送JSON格式
        messageBody.setSummary("收货方已开具收购单");// 可以理解为消息的标题，用于显示会话列表和消息通知栏
        // 注意，这里是单聊自定义消息
        return YWMessageChannel.createCustomMessage(messageBody);
    }
    public static YWMessage createCustomSendOrderMessage(OrderModel orderModel){
        // 发送自定义消息
        YWCustomMessageBody messageBody = new YWCustomMessageBody();
        // 请注意这里不一定要是JSON格式，这里纯粹是为了演示的需要
        JSONObject object = new JSONObject();
        try {
            object.put("customizeMessageType", CustomMessageType.SEND_ORDER);
            object.put("name",orderModel.getProduce_name());
            object.put("order_no",orderModel.getOrder_no());
            object.put("status",orderModel.getStatus());
            object.put("order_id",orderModel.getOrder_id());
            object.put("price",orderModel.getPrice());
            object.put("unit",orderModel.getUnit());
            object.put("amount",orderModel.getAmount());
            object.put("status_name",orderModel.getStatus_name());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        messageBody.setContent(object.toString());// 用户要发送的自定义消息，SDK不关心具体的格式，比如用户可以发送JSON格式
        messageBody.setSummary("订单已发送");// 可以理解为消息的标题，用于显示会话列表和消息通知栏
        // 注意，这里是单聊自定义消息
        return YWMessageChannel.createCustomMessage(messageBody);
    }
    /**
     * 是否需要隐藏头像
     * @param viewType 自定义view类型
     * @return true: 隐藏头像  false：不隐藏头像
     */
    @Override
    public boolean needHideHead(int viewType) {
        if (viewType==15||viewType==type_14||viewType==type_12||viewType==type_13||viewType==type_11||viewType == type_5||viewType==type_6||viewType==type_7||viewType==type_8||viewType==type_9||viewType==type_10) {
            return true;
        }
        return super.needHideHead(viewType);
    }

    private void sendOrderTipMsg(JSONObject object,String desc){
        try{
//            YWMessage msg=createCustomOprationOrderMessage(object.getString("name"),
//                    object.getDouble("price"),object.getInt("amount"),object.getInt("order_id"),object.getString("unit"),desc,status);
            YWCustomMessageBody messageBody = new YWCustomMessageBody();
            object.put("desc",desc);
            object.put("customizeMessageType",CustomMessageType.ORDER_TIP);
            messageBody.setContent(object.toString());
            messageBody.setExtraData(new JSONObject().put("status",0));
            messageBody.setSummary("订单发货提醒已发出");
            YWMessage m=YWMessageChannel.createCustomMessage(messageBody);
            if(ChattingUICustom.conversation!=null){
                ChattingUICustom.conversation.getMessageSender().sendMessage(m, 120, null);
            }
        }catch (Exception e){

        }
    }
    /**
     * 根据viewType获取自定义view
     * @param fragment      聊天窗口fragment
     * @param message       当前需要自定义view的消息
     * @param convertView   自定义view
     * @param viewType      自定义view的类型
     * @param headLoadHelper    头像加载管理器，用户可以调用该对象的方法加载头像
     * @return  自定义view
     */
    @Override
    public View getCustomView(final Fragment fragment, final YWMessage message, View convertView, int viewType, YWContactHeadLoadHelper headLoadHelper) {
        YWLog.i(TAG, "getCustomView, type = " + viewType);
        if(viewType==type_4){
            ViewHolder4 holder = null;
            if (convertView == null) {
                YWLog.i(TAG, "getCustomView, convertView == null");
                holder = new ViewHolder4();
                convertView = fragment.getActivity().getLayoutInflater().inflate(R.layout.demo_geo_message_layout,null);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.address=(TextView) convertView.findViewById(R.id.address);
                holder.thumb=(ImageView) convertView.findViewById(R.id.thumb);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder4)convertView.getTag();
            }
            try {
                String content = message.getMessageBody().getContent();
                JSONObject object = new JSONObject(content);
                holder.title.setText(object.getString("title"));
                holder.address.setText(object.getString("address"));
                Glide.with(fragment.getActivity()).load(object.getString("thumb"))
                        .fitCenter().into(holder.thumb);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }else if(viewType==type_5){
            ViewHolder5 holder = null;
            if (convertView == null) {
                holder = new ViewHolder5();
                convertView = fragment.getActivity().getLayoutInflater().inflate(R.layout.order_msg_layout,null);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.amount=(TextView) convertView.findViewById(R.id.amount);
                holder.price=(TextView) convertView.findViewById(price);
                holder.desc=(TextView) convertView.findViewById(R.id.desc);
                holder.dingjin=(TextView) convertView.findViewById(R.id.dingjin);
                holder.details=(TextView)convertView.findViewById(R.id.details);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder5)convertView.getTag();
            }
            try {
                String content = message.getMessageBody().getContent();
                final JSONObject object = new JSONObject(content);
                holder.name.setText(object.getString("name"));
                holder.amount.setText(object.getInt("amount")+object.getString("unit"));
                holder.price.setText(object.getDouble("price")+"元");
                holder.desc.setTextColor(fragment.getResources().getColor(R.color.colorPrimary));
                holder.details.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent=new Intent();
                        intent.setClass(fragment.getActivity(), OrderDetailsActivity.class);
                        intent.putExtra("order_id",object.optInt("order_id"));
                        fragment.startActivity(intent);
                    }
                });
                //holder.desc.setTextColor(fragment.getResources().getColor(R.color.aliwx_common_text_color2));
                if(object.has("dingjin")){
                    if(object.getInt("dingjin")==0){
                        holder.dingjin.setText("(无订金)");
                    }else{
                        holder.dingjin.setText("(订金"+object.getInt("dingjin")+"元)");
                    }
                }else{
                    holder.dingjin.setText("(无订金)");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }else if(viewType==type_9){
            ViewHolder5 holder = null;
            if (convertView == null) {
                holder = new ViewHolder5();
                convertView = fragment.getActivity().getLayoutInflater().inflate(R.layout.order_msg_layout2,null);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.amount=(TextView) convertView.findViewById(R.id.amount);
                holder.price=(TextView) convertView.findViewById(price);
                holder.desc=(TextView) convertView.findViewById(R.id.desc);
                holder.dingjin=(TextView) convertView.findViewById(R.id.dingjin);
                holder.details=(TextView)convertView.findViewById(R.id.details);
                holder.guobang=(Button)convertView.findViewById(R.id.tongyi);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder5)convertView.getTag();
            }
            try {
                String content = message.getMessageBody().getContent();
                final JSONObject object = new JSONObject(content);
                holder.name.setText(object.getString("name"));
                holder.amount.setText(object.getInt("amount")+object.getString("unit"));
                holder.price.setText(object.getDouble("price")+"元");
                holder.details.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent=new Intent();
                        intent.setClass(fragment.getActivity(), OrderDetailsActivity.class);
                        intent.putExtra("order_id",object.optInt("order_id"));
                        fragment.startActivity(intent);
                    }
                });
                if(object.has("dingjin")){
                    if(object.getInt("dingjin")==0){
                        holder.dingjin.setText("(无订金)");
                    }else{
                        holder.dingjin.setText("(订金"+object.getInt("dingjin")+"元)");
                    }
                }else{
                    holder.dingjin.setText("(无订金)");
                }
                holder.guobang.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent();
                        intent.putExtra("name",object.optString("name"));
                        intent.putExtra("order_id",object.optInt("order_id"));
                        intent.putExtra("price",object.optDouble("price"));
                        intent.putExtra("unit",object.optString("unit"));
                        intent.putExtra("farmer_phone",object.optString("farmer_phone"));
                        intent.putExtra("purchase_phone",object.optString("purchase_phone"));
                        intent.putExtra("price",object.optDouble("price"));
                        intent.setClass(fragment.getActivity(), GuobangDetailsActivity.class);
                        fragment.startActivity(intent);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }else if(viewType==type_12){
            ViewHolder6 holder = null;
            if (convertView == null) {
                holder = new ViewHolder6();
                convertView = fragment.getActivity().getLayoutInflater().inflate(R.layout.order_msg_dingjin_layout,null);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.amount=(TextView) convertView.findViewById(R.id.amount);
                holder.price=(TextView) convertView.findViewById(price);
                holder.desc=(TextView) convertView.findViewById(R.id.desc);
                holder.dingjin=(TextView)convertView.findViewById(R.id.dingjin);
                holder.details=(TextView)convertView.findViewById(R.id.details);
                holder.tongyi=(Button)convertView.findViewById(R.id.tongyi);
                holder.jujue=(Button)convertView.findViewById(R.id.jujue);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder6)convertView.getTag();
            }
            try {
                String content = message.getMessageBody().getContent();
                final JSONObject object = new JSONObject(content);
                holder.name.setText(object.getString("name"));
                holder.amount.setText(object.getInt("amount")+object.getString("unit"));
                holder.price.setText(object.getDouble("price")+"元");
                holder.details.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent=new Intent();
                        intent.setClass(fragment.getActivity(), OrderDetailsActivity.class);
                        intent.putExtra("order_id",object.optInt("order_id"));
                        fragment.startActivity(intent);
                    }
                });
                if(object.has("dingjin")){
                    if(object.getInt("dingjin")==0){
                        holder.dingjin.setText("(无订金)");
                    }else{
                        holder.dingjin.setText("(订金"+object.getInt("dingjin")+"元)");
                    }
                }else{
                    holder.dingjin.setText("(无订金)");
                }
                holder.tongyi.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                Object extra=message.getMessageBody().getExtraData();
                if(extra!=null&&!"".equals(extra)){
                    int status = new JSONObject(extra.toString()).getInt("status");
                    if(status==1){
                        holder.tongyi.setEnabled(false);
                        holder.jujue.setEnabled(false);
                    }else{
                        holder.tongyi.setEnabled(true);
                        holder.jujue.setEnabled(true);
                        holder.jujue.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancellorder(fragment.getActivity(),object,message);
                            }
                        });
                    }
                }else{
                    holder.tongyi.setEnabled(true);
                    holder.jujue.setEnabled(true);
                    holder.jujue.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            cancellorder(fragment.getActivity(),object,message);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }else if(viewType==type_6){
            ViewHolder6 holder = null;
            if (convertView == null) {
                holder = new ViewHolder6();
                convertView = fragment.getActivity().getLayoutInflater().inflate(R.layout.order_msg_layout,null);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.amount=(TextView) convertView.findViewById(R.id.amount);
                holder.price=(TextView) convertView.findViewById(price);
                holder.desc=(TextView) convertView.findViewById(R.id.desc);
                holder.dingjin=(TextView)convertView.findViewById(R.id.dingjin);
                holder.details=(TextView)convertView.findViewById(R.id.details);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder6)convertView.getTag();
            }
            try {
                String content = message.getMessageBody().getContent();
                final JSONObject object = new JSONObject(content);
                holder.name.setText(object.getString("name"));
                holder.amount.setText(object.getInt("amount")+object.getString("unit"));
                holder.price.setText(object.getDouble("price")+"元");
                holder.details.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent=new Intent();
                        intent.setClass(fragment.getActivity(), OrderDetailsActivity.class);
                        intent.putExtra("order_id",object.optInt("order_id"));
                        fragment.startActivity(intent);
                    }
                });
                if(object.has("dingjin")){
                    if(object.getInt("dingjin")==0){
                        holder.dingjin.setText("(无订金)");
                    }else{
                        holder.dingjin.setText("(订金"+object.getInt("dingjin")+"元)");
                    }
                }else{
                    holder.dingjin.setText("(无订金)");
                }
                if(status==-1){
                    holder.desc.setText(object.getString("desc"));
                    holder.desc.setTextColor(0xffDB261C);
                }else{
                    holder.desc.setMovementMethod(LinkMovementMethod.getInstance());
                    holder.desc.setTextColor(fragment.getResources().getColor(R.color.colorPrimary));
                    if(object.has("dingjin")){
                        if(object.getInt("dingjin")==0){
                            holder.desc.setText(getString(object.getString("desc")+"你可以提醒对方送货。",7,object,null));
                        }else{
                            holder.desc.setText(getString(object.getString("desc")+"你需要支付订金。",5,object,null));
                        }
                    }else{
                        holder.desc.setText(getString(object.getString("desc")+"你可以提醒对方送货。",7,object,null));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;

        }else if(viewType==type_13){
            ViewHolder5 holder = null;
            if (convertView == null) {
                holder = new ViewHolder5();
                convertView = fragment.getActivity().getLayoutInflater().inflate(R.layout.order_msg_layout,null);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.amount=(TextView) convertView.findViewById(R.id.amount);
                holder.price=(TextView) convertView.findViewById(price);
                holder.desc=(TextView) convertView.findViewById(R.id.desc);
                holder.dingjin=(TextView) convertView.findViewById(R.id.dingjin);
                holder.details=(TextView)convertView.findViewById(R.id.details);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder5)convertView.getTag();
            }
            try {
                String content = message.getMessageBody().getContent();
                final JSONObject object = new JSONObject(content);
                holder.name.setText(object.getString("name"));
                holder.amount.setText(object.getInt("amount")+object.getString("unit"));
                holder.price.setText(object.getDouble("price")+"元");
                holder.desc.setTextColor(fragment.getResources().getColor(R.color.colorPrimary));
                holder.details.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent=new Intent();
                        intent.setClass(fragment.getActivity(), OrderDetailsActivity.class);
                        intent.putExtra("order_id",object.optInt("order_id"));
                        fragment.startActivity(intent);
                    }
                });
                //holder.desc.setTextColor(fragment.getResources().getColor(R.color.aliwx_common_text_color2));
                if(object.has("dingjin")){
                    if(object.getInt("dingjin")==0){
                        holder.dingjin.setText("(无订金)");
                    }else{
                        holder.dingjin.setText("(订金"+object.getInt("dingjin")+"元)");
                    }
                }else{
                    holder.dingjin.setText("(无订金)");
                }
                holder.desc.setText(object.getString("desc"));
                holder.desc.setTextColor(0xffDB261C);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }else if(viewType==type_7){
            {
                ViewHolder7 holder = null;
                if (convertView == null) {
                    holder = new ViewHolder7();
                    convertView = fragment.getActivity().getLayoutInflater().inflate(R.layout.txt_layout,null);
                    holder.desc = (TextView) convertView.findViewById(R.id.desc);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder7)convertView.getTag();
                }
                try {
                    String content = message.getMessageBody().getContent();
                    final JSONObject object = new JSONObject(content);
                    holder.desc.setMovementMethod(LinkMovementMethod.getInstance());
                    holder.desc.setText(getString("订单发货提醒已发出。你还可以给对方发送货地址。",6,null,fragment));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return convertView;
            }
        }else if(viewType==type_8){
            {
                {
                    ViewHolder8 holder = null;
                    if (convertView == null) {
                        holder = new ViewHolder8();
                        convertView = fragment.getActivity().getLayoutInflater().inflate(R.layout.order_shougou_msg_layout,null);
                        holder.name = (TextView) convertView.findViewById(R.id.name);
                        holder.desc=(TextView) convertView.findViewById(R.id.desc);
                        holder.jinzhong=(TextView) convertView.findViewById(jinzhong);
                        holder.desc=(TextView) convertView.findViewById(R.id.desc);
                        holder.total=(TextView)convertView.findViewById(R.id.total);
                        holder.details=(TextView)convertView.findViewById(R.id.details);
                        holder.price=(TextView)convertView.findViewById(price);
                        convertView.setTag(holder);
                    } else {
                        holder = (ViewHolder8)convertView.getTag();
                    }
                    try {
                        String content = message.getMessageBody().getContent();
                        final JSONObject object = new JSONObject(content);
                        holder.name.setText(object.getString("name"));
                        holder.price.setText(object.optDouble("price")+"元/"+object.getString("unit"));
                        holder.jinzhong.setText(object.getInt("jinzhong")+object.getString("unit"));
                        holder.total.setText(object.getDouble("total")+"元");
                        holder.desc.setText("等待对方同意...");
                        holder.details.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent=new Intent();
                                intent.setClass(fragment.getActivity(), OrderDetailsActivity.class);
                                intent.putExtra("order_id",object.optInt("order_id"));
                                fragment.startActivity(intent);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return convertView;
                }
            }
        }else if(viewType==type_10){
                    ViewHolder8 holder = null;
                    if (convertView == null) {
                        holder = new ViewHolder8();
                        convertView = fragment.getActivity().getLayoutInflater().inflate(R.layout.order_zhifu_msg_layout, null);
                        holder.name = (TextView) convertView.findViewById(R.id.name);
                        holder.jinzhong = (TextView) convertView.findViewById(jinzhong);
                        holder.total = (TextView) convertView.findViewById(R.id.total);
                        holder.price=(TextView)convertView.findViewById(price);
                        holder.details = (TextView) convertView.findViewById(R.id.details);
                        holder.zhifu=(Button)convertView.findViewById(R.id.tongyi);
                        convertView.setTag(holder);
                    } else {
                        holder = (ViewHolder8) convertView.getTag();
                    }
                    try {
                        String content = message.getMessageBody().getContent();
                        final JSONObject object = new JSONObject(content);
                        holder.name.setText(object.getString("name"));
                        holder.jinzhong.setText(object.getInt("jinzhong") + object.getString("unit"));
                        holder.total.setText(object.getDouble("total") + "元");
                        holder.price.setText(object.optDouble("price")+"元/"+object.getString("unit"));
                        holder.details.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent();
                                intent.setClass(fragment.getActivity(), OrderDetailsActivity.class);
                                intent.putExtra("order_id", object.optInt("order_id"));
                                fragment.startActivity(intent);
                            }
                        });
                        holder.zhifu.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return convertView;
        }else if(viewType==type_14){
            ViewHolder8 holder = null;
            if (convertView == null) {
                holder = new ViewHolder8();
                convertView = fragment.getActivity().getLayoutInflater().inflate(R.layout.order_msg_layout4, null);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.desc = (TextView) convertView.findViewById(R.id.desc);
                holder.jinzhong = (TextView) convertView.findViewById(jinzhong);
                holder.desc = (TextView) convertView.findViewById(R.id.desc);
                holder.price=(TextView)convertView.findViewById(price);
                holder.total = (TextView) convertView.findViewById(R.id.total);
                holder.details = (TextView) convertView.findViewById(R.id.details);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder8) convertView.getTag();
            }
            try {
                String content = message.getMessageBody().getContent();
                final JSONObject object = new JSONObject(content);
                holder.name.setText(object.getString("name"));
                holder.jinzhong.setText(object.getInt("jinzhong") + object.getString("unit"));
                holder.total.setText(object.getDouble("total") + "元");
                holder.price.setText(object.optDouble("price")+"元/"+object.getString("unit"));
                holder.details.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.setClass(fragment.getActivity(), OrderDetailsActivity.class);
                        intent.putExtra("order_id", object.optInt("order_id"));
                        fragment.startActivity(intent);
                    }
                });
                holder.desc.setText(object.getString("desc"));
                holder.desc.setTextColor(0xffDB261C);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }else if(viewType==type_11){
            ViewHolder6 holder = null;
            if (convertView == null) {
                holder = new ViewHolder6();
                convertView = fragment.getActivity().getLayoutInflater().inflate(R.layout.order_msg_layout,null);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.amount=(TextView) convertView.findViewById(R.id.amount);
                holder.price=(TextView) convertView.findViewById(price);
                holder.desc=(TextView) convertView.findViewById(R.id.desc);
                holder.dingjin=(TextView)convertView.findViewById(R.id.dingjin);
                holder.details=(TextView)convertView.findViewById(R.id.details);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder6)convertView.getTag();
            }
            try {
                String content = message.getMessageBody().getContent();
                final JSONObject object = new JSONObject(content);
                holder.name.setText(object.getString("name"));
                holder.amount.setText(object.getInt("amount")+object.getString("unit"));
                holder.price.setText(object.getDouble("price")+"元");
                holder.details.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent=new Intent();
                        intent.setClass(fragment.getActivity(), OrderDetailsActivity.class);
                        intent.putExtra("order_id",object.optInt("order_id"));
                        fragment.startActivity(intent);
                    }
                });
                if(object.has("dingjin")){
                    if(object.getInt("dingjin")==0){
                        holder.dingjin.setText("(无订金)");
                    }else{
                        holder.dingjin.setText("(订金"+object.getInt("dingjin")+"元)");
                    }
                }else{
                    holder.dingjin.setText("(无订金)");
                }
                    holder.desc.setText("订单已取消");
                    holder.desc.setTextColor(0xffDB261C);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }else if(viewType==type_15){
                ViewHolder9 holder = null;
                if (convertView == null) {
                    holder = new ViewHolder9();
                    convertView = fragment.getActivity().getLayoutInflater().inflate(R.layout.order_send_layout,null);
                    holder.name = (TextView) convertView.findViewById(R.id.name);
                    holder.amount=(TextView) convertView.findViewById(R.id.amount);
                    holder.price=(TextView) convertView.findViewById(price);
                    holder.status=(TextView) convertView.findViewById(R.id.status);
                    holder.order_no=(TextView) convertView.findViewById(R.id.order_no);
                    holder.details=(TextView)convertView.findViewById(R.id.details);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder9)convertView.getTag();
                }
                try {
                    String content = message.getMessageBody().getContent();
                    final JSONObject object = new JSONObject(content);
                    holder.name.setText(object.getString("name"));
                    holder.amount.setText(object.getInt("amount")+object.getString("unit"));
                    holder.price.setText(object.getDouble("price")+"元");
                    if(object.getInt("status")==-1||object.getInt("status")==-2){
                        holder.status.setTextColor(0xffDB261C);
                    }else{
                        holder.status.setTextColor(fragment.getResources().getColor(R.color.colorPrimary));
                    }
                    holder.status.setText(object.getString("status_name"));
                    holder.order_no.setText("(订单号:"+object.getString("order_no")+")");
                    holder.details.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent=new Intent();
                            intent.setClass(fragment.getActivity(), OrderDetailsActivity.class);
                            intent.putExtra("order_id",object.optInt("order_id"));
                            fragment.startActivity(intent);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return convertView;
        }else if(viewType==type_16){
            ViewHolder10 holder = null;
            if (convertView == null) {
                holder = new ViewHolder10();
                convertView = fragment.getActivity().getLayoutInflater().inflate(R.layout.goods_item,null);
                holder.thumb = (ImageView) convertView.findViewById(R.id.img);
                holder.amount=(TextView) convertView.findViewById(R.id.amount);
                holder.price=(TextView) convertView.findViewById(price);
                holder.title=(TextView) convertView.findViewById(R.id.name);
                holder.itemView=convertView.findViewById(R.id.root);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder10)convertView.getTag();
            }
            try {
                String content = message.getMessageBody().getContent();
                final JSONObject object = new JSONObject(content);
                holder.title.setText(object.getString("name"));
                holder.amount.setText(getAmountString(object.getInt("amount"),object.getString("aunit")));
                holder.price.setText(getPriceString(object.getDouble("price"),object.getString("punit")));
                Glide.with(fragment.getActivity()).load(object.getString("thumb")).centerCrop().placeholder(R.drawable.no_icon)
                        .into(holder.thumb);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent=new Intent();
                        intent.setClass(fragment.getActivity(), ProduceDetailsActivity.class);
                        intent.putExtra("id",object.optInt("id"));
                        fragment.startActivity(intent);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }
        return super.getCustomView(fragment, message, convertView, viewType, headLoadHelper);
    }
    private SpannableStringBuilder getPriceString(double price,String punit){
        SpannableStringBuilder builder=new SpannableStringBuilder();
        builder.append(String.valueOf(price));
        RelativeSizeSpan sizeSpan=new RelativeSizeSpan(2f);
        builder.setSpan(sizeSpan,0,builder.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        StyleSpan typefaceSpan=new StyleSpan(Typeface.BOLD);
        builder.setSpan(typefaceSpan,0,builder.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.append("元/"+punit);
        return builder;
    }
    private SpannableStringBuilder getAmountString(int estimatedQuantity,String aunit){
        SpannableStringBuilder builder=new SpannableStringBuilder();
        builder.append("预计产量");
        builder.append(String.valueOf(estimatedQuantity));
        ForegroundColorSpan sizeSpan=new ForegroundColorSpan(0xffEE6614);
        builder.setSpan(sizeSpan,4,builder.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.append(aunit);
        return builder;
    }
    public static void cancellorder(Context ctx, final JSONObject object, final YWMessage message){
        try{
            JsonObject dataJson = new JsonObject();
            dataJson.addProperty("token", UserUtil.getUserModel(ctx).getToken());
            dataJson.addProperty("order_id", object.getInt("order_id"));
            HttpTask task=new HttpTask(ctx);
            task.setTaskHandler(new HttpTask.HttpTaskHandler() {
                @Override
                public void taskStart(int code) {

                }
                @Override
                public void taskSuccessful(String str, int code) {
                    updateOrderMessageStatus(message);
                    JsonObject selectResultJson = new JsonParser().parse(str)
                            .getAsJsonObject();
                        sendSysMsg("你已取消该订单",selectResultJson.get("userid").getAsString());
                        sendTransMsg("收购方已取消该订单",selectResultJson.get("userid").getAsString());
                }
                @Override
                public void taskFailed(int code) {

                }
            });
            task.execute(UrlUtil.CANCELL_ORDER,dataJson.toString());
        }catch (Exception e){

            e.printStackTrace();
        }
    }
    private static void updateOrderMessageStatus(YWMessage message){
        try{
            if(message==null){
                return;
            }
            if(ChattingUICustom.conversation!=null){
                YWMessageBody body=message.getMessageBody();
                body.setExtraData(new JSONObject().put("status",1));
                ChattingUICustom.conversation.updateCustomMessageExtraData(ChattingUICustom.conversation,
                        message);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private SpannableStringBuilder getString(String text, final int start,final JSONObject object,final Fragment fragment){
        SpannableStringBuilder builder=new SpannableStringBuilder();
        builder.append(text);
        ForegroundColorSpan colorSpan=new ForegroundColorSpan(0xffDB261C);
        UnderlineSpan span=new UnderlineSpan();
        builder.setSpan(colorSpan,text.length()-start,text.length()-1,Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.setSpan(span,text.length()-start,text.length()-1,Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        ClickableSpan clickableSpan=new ClickableSpan() {
            @Override
            public void onClick(View view) {
                if(start==7){
                    sendOrderTipMsg(object,"订单发货提醒。");
                    sendTransMsg("对方发来送货提醒，请您按时交货。");
                }else if(start==6){
                    Intent intent=new Intent();
                    intent.putExtra("flags","");
                    intent.setClass(fragment.getActivity(), LocationSelectActivity.class);
                    fragment.startActivityForResult(intent,REQUEST_LOCATION);
                }else if(start==5){

                }
            }
        };
        builder.setSpan(clickableSpan,text.length()-start,text.length()-1,Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        return builder;
    }
    public class ViewHolder0 {
        TextView address;
        ImageView thumb;
    }

    public class ViewHolder1 {
        TextView greeting;
    }

    public class ViewHolder2 {
        ImageView head;
        TextView name;
    }

    public class ViewHolder3{
        TextView left;
        TextView right;
    }
    public class ViewHolder4{
        TextView title;
        TextView address;
        ImageView thumb;
    }
    public class ViewHolder5{
        TextView name;
        TextView price;
        TextView amount;
        TextView dingjin;
        TextView desc;
        TextView details;
        Button guobang;
    }
    public class ViewHolder6{
        TextView name;
        TextView price;
        TextView amount;
        TextView desc;
        TextView dingjin;
        TextView details;
        Button tongyi,jujue;
    }
    public class ViewHolder9{
        TextView name;
        TextView price;
        TextView amount;
        TextView status;
        TextView order_no;
        TextView details;
    }
    public class ViewHolder10{
        ImageView thumb;
        TextView title;
        TextView price;
        TextView amount;
        View itemView;
    }
    public class ViewHolder7{
        TextView desc;
    }
    public class ViewHolder8{
        TextView name;
        TextView jinzhong;
        TextView total;
        TextView details;
        TextView price;
        TextView desc;
        Button zhifu;
    }
    /**************** 以上是定制自定义消息view的示例代码 ****************/

    /**
     * 双击放大文字消息的开关
     * @param fragment
     * @return true:开启双击放大文字 false: 关闭双击放大文字
     */
    @Override
    public boolean enableDoubleClickEnlargeMessageText(Fragment fragment) {
        return true;
    }

    /**
     * 数字字符串点击事件,开发者可以根据自己的需求定制
     * @param activity
     * @param clickString 被点击的数字string
     * @param widget 被点击的TextView
     * @return false:不处理
     *         true:需要开发者在return前添加自己实现的响应逻辑代码
     */
    @Override
    public boolean onNumberClick(final Activity activity, final String clickString, final View widget) {
        ArrayList<String> menuList = new ArrayList<String>();
        menuList.add("呼叫");
        menuList.add("添加到手机通讯录");
        menuList.add("复制到剪贴板");
        final String[] items = new String[menuList.size()];
        menuList.toArray(items);
        Dialog alertDialog = new WxAlertDialog.Builder(activity)
                .setItems(items, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        if (TextUtils.equals(items[which], "呼叫")) {
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:" + clickString));
                            activity.startActivity(intent);
                        } else if (TextUtils.equals(items[which], "添加到手机通讯录")) {
                            Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                            intent.setType("vnd.android.cursor.item/person");
                            intent.setType("vnd.android.cursor.item/contact");
                            intent.setType("vnd.android.cursor.item/raw_contact");
                            intent.putExtra(android.provider.ContactsContract.Intents.Insert.PHONE, clickString);
                            activity.startActivity(intent);

                        } else if (TextUtils.equals(items[which], "复制到剪贴板")) {
                            ClipboardManager clipboardManager = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                            clipboardManager.setText(clickString);
                        }
                    }
                }).create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.setCancelable(true);
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                widget.invalidate();
            }
        });
        if (!alertDialog.isShowing()) {
            alertDialog.show();
        }
        return true;
    }

    //TODO 不要使用60000之前的值，防止和SDK中使用的产生冲突
    private static final int CAMERA_WITH_DATA = 60001;
    private static final int PHOTO_PICKED_WITH_DATA = 60002;
    public static final int IMAGE_CAMERA_WITH_DATA = 60003;
    private static final int IMAGE_PHOTO_PICKED_WITH_DATA = 60004;


    /**
     * 请注意不要和内部的ID重合
     * {@link YWChattingPlugin.ReplyBarItem#ID_CAMERA}
     * {@link YWChattingPlugin.ReplyBarItem#ID_ALBUM}
     * {@link YWChattingPlugin.ReplyBarItem#ID_SHORT_VIDEO}
     */
    private static int ITEM_ID_1 = 0x1;
    private static int ITEM_ID_2 = 0x2;
    private static int ITEM_ID_3 = 0X3;
    private static final int REQUEST_LOCATION=0xaa;


    /**
     *
     * 用于增加聊天窗口 下方回复栏的操作区的item
     *
     * ReplyBarItem
     * itemId:唯一标识 建议从1开始
     * ItemImageRes：显示的图片
     * ItemLabel：文字
     * needHide:是否隐藏 默认: false ,  显示：false ， 隐藏：true
     * OnClickListener: 自定义点击事件, null则使用默认的点击事件
     * 参照示例返回List<ReplyBarItem>用于操作区显示item列表，可以自定义顺序和添加item
     *
     * @param pointcut         聊天窗口fragment
     * @param conversation 当前会话，通过conversation.getConversationType() 区分个人单聊，与群聊天
     * @param replyBarItemList 默认的replyBarItemList，如拍照、选择照片、短视频等
     * @return
     */
    @Override
    public List<ReplyBarItem> getCustomReplyBarItemList(final Fragment pointcut,
                                                        final YWConversation conversation, List<ReplyBarItem> replyBarItemList) {
        List<ReplyBarItem> replyBarItems = new ArrayList<ReplyBarItem>();

        for (ReplyBarItem replyBarItem : replyBarItemList) {
            if(replyBarItem.getItemId()== YWChattingPlugin.ReplyBarItem.ID_CAMERA){
                //是否隐藏ReplyBarItem中的拍照选项
                replyBarItem.setNeedHide(false);
                //不自定义ReplyBarItem中的拍照的点击事件,设置OnClicklistener(null);
                replyBarItem.setOnClicklistener(null);
                //自定义ReplyBarItem中的拍照的点击事件,设置OnClicklistener
//                开发者在自己实现拍照逻辑时，可以在{@link #onActivityResult(int, int, Intent, List<YWMessage>)}中处理拍照完成后的操作
//                replyBarItem.setOnClicklistener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//                    }
//                });
            }else if(replyBarItem.getItemId()== YWChattingPlugin.ReplyBarItem.ID_ALBUM){
                //是否隐藏ReplyBarItem中的选择照片选项
                replyBarItem.setNeedHide(false);
                //不自定义ReplyBarItem中的相册的点击事件,设置OnClicklistener（null）
                replyBarItem.setOnClicklistener(null);
                //自定义ReplyBarItem中的相册的点击事件,设置OnClicklistener
//                replyBarItem.setOnClicklistener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        IMNotificationUtils.getInstance().showToastLong(pointcut.getActivity(), "用户点击了选择照片");
//                    }
//                });
            }else if(replyBarItem.getItemId()== YWChattingPlugin.ReplyBarItem.ID_SHORT_VIDEO){

                //检查是否集成了短视频SDK，短视频SDK集成文档请访问网页http://open.taobao.com/doc2/detail?&docType=1&articleId=104689
                if(!haveShortVideoLibrary()){
                    //是否隐藏ReplyBarItem中的短视频选项
                    replyBarItem.setNeedHide(true);
                }else{
                    //默认配置是群聊时隐藏短视频按钮。这里是为了设置显示群聊短视频item
                    if (conversation.getConversationType() == YWConversationType.Tribe){
                        replyBarItem.setNeedHide(false);
                    }
                }
            }
            replyBarItems.add(replyBarItem);
        }
        if (conversation.getConversationType() == YWConversationType.P2P) {
            ReplyBarItem replyBarItem = new ReplyBarItem();
            replyBarItem.setItemId(ITEM_ID_1);
            replyBarItem.setItemImageRes(R.drawable.demo_reply_bar_location);
            replyBarItem.setItemLabel("我的位置");
            replyBarItem.setOnClicklistener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent();
                    intent.setClass(pointcut.getActivity(), LocationSelectActivity.class);
                    pointcut.startActivityForResult(intent,REQUEST_LOCATION);
                }
            });
            replyBarItems.add(0,replyBarItem);
        }

        return replyBarItems;

    }

    public static void sendTransMsg(String text) {
        YWCustomMessageBody messageBody = new YWCustomMessageBody();
//设置透传标记，1表示透传消息，0表示非透传消息，默认为0
        messageBody.setTransparentFlag(1);
        messageBody.setContent(text);
//创建单聊透传消息
        YWMessage message = YWMessageChannel.createCustomMessage(messageBody);
        if(ChattingUICustom.conversation!=null){
            ChattingUICustom.conversation.getMessageSender().sendMessage(message, 120, null);
        }
    }
    public static void sendTransMsg(String text,String userid) {
        YWCustomMessageBody messageBody = new YWCustomMessageBody();
//设置透传标记，1表示透传消息，0表示非透传消息，默认为0
        messageBody.setTransparentFlag(1);
        messageBody.setContent(text);
//创建单聊透传消息
        YWMessage message = YWMessageChannel.createCustomMessage(messageBody);
        YWConversation conversation= MyApplication.getConversation(userid);
        if(conversation!=null){
            conversation.getMessageSender().sendMessage(message, 120, null);
        }
    }
    private static boolean compiledShortVideoLibrary =false;
    private static boolean haveCheckedShortVideoLibrary=false;

    /**
     * 检查是否集成了集成了短视频的SDK
     * @return
     */
    private boolean haveShortVideoLibrary(){
        if(!haveCheckedShortVideoLibrary){
            try {
                Class.forName("com.im.IMRecordVideoActivity");
                compiledShortVideoLibrary =true;
                haveCheckedShortVideoLibrary=true;
            } catch (ClassNotFoundException e) {
                compiledShortVideoLibrary =false;
                haveCheckedShortVideoLibrary=true;
                e.printStackTrace();
            }
        }
        return compiledShortVideoLibrary;

    }






    /**
     * 自定义系统消息文案
     * @param fragment     聊天窗口fragment
     * @param conversation 当前聊天窗口对应的会话
     * @param content      默认系统消息文案
     * @return 如果是NULL，则不显示，如果是空字符串，则使用SDK默认的文案，如果返回非空串，则使用用户自定义的
     */
    @Override
    public String getSystemMessageContent(Fragment fragment, YWConversation conversation, String content) {
        return "";
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data, List<YWMessage> messageList) {
        if(requestCode==REQUEST_LOCATION&&resultCode==2){

            return true;
        }
        return false;
    }
    /**
     * 定制长按消息事件，每一条消息的长按事件都会回调该方法，开发者根据消息类型，对不同类型的消息设置不同的长按事件
     * @param fragment  聊天窗口fragment对象
     * @param message   被点击的消息
     * @return true:使用用户自定义的长按消息事件，false：使用默认的长按消息事件
     */
    @Override
    public boolean onMessageLongClick(final Fragment fragment, final YWMessage message) {
        final Activity context=fragment.getActivity();
        if (message != null) {
            final List<String> linkedList = new ArrayList<String>();

            linkedList.add(context.getString(R.string.delete_msg));

            if (message.getSubType() == YWMessage.SUB_MSG_TYPE.IM_TEXT) {
                linkedList.add(context.getString(R.string.copy_msg));
            }

            if(message.getSubType()==YWMessage.SUB_MSG_TYPE.IM_AUDIO){
                String text;
                if (mUserInCallMode) { //当前为听筒模式
                    text = "使用扬声器模式";
                } else { //当前为扬声器模式
                    text = "使用听筒模式";
                }
                linkedList.add(text);
            }

            final String[] strs = new String[linkedList.size()];
            linkedList.toArray(strs);

            final YWConversation conversation =UserUtil.getIMKitInstance(fragment.getActivity()).getConversationService().getConversationByConversationId(message.getConversationId());
            new WxAlertDialog.Builder(context)
                    .setTitle("选择操作")
                    .setItems(strs, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            if (which < strs.length) {
                                if (context.getResources()
                                        .getString(R.string.delete_msg)
                                        .equals(strs[which])) {
                                    if (conversation != null) {
                                        conversation.getMessageLoader().deleteMessage(message);
                                    } else {
                                        IMNotificationUtils.getInstance().showToast(context, "删除失败，请稍后重试");
                                    }
                                } else if (context.getResources()
                                        .getString(R.string.copy_msg)
                                        .equals(strs[which])) {
                                    ClipboardManager clip = (ClipboardManager) context
                                            .getSystemService(Context.CLIPBOARD_SERVICE);
//										String content = message.getContent();
                                    String content = message.getMessageBody().getContent();
                                    if (TextUtils.isEmpty(content)) {
                                        return;
                                    }

                                    try {
                                        clip.setText(content);
                                    } catch (NullPointerException e) {
                                        e.printStackTrace();
                                    } catch (IllegalStateException e) {
                                        e.printStackTrace();
                                    }
                                } else if ("使用扬声器模式"
                                        .equals(strs[which]) || "使用听筒模式"
                                        .equals(strs[which])) {

                                    if (mUserInCallMode) {
                                        mUserInCallMode = false;
                                    } else {
                                        mUserInCallMode = true;
                                    }
                                }
                            }
                        }
                    })
                    .setNegativeButton(
                            context.getResources().getString(
                                    R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    dialog.cancel();
                                }
                            }).create().show();
        }
        return true;
    }
}
