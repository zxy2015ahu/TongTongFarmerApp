package com.tongtong.purchaser.helper;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.mobileim.YWIMKit;
import com.alibaba.mobileim.aop.Pointcut;
import com.alibaba.mobileim.aop.custom.IMConversationListUI;
import com.alibaba.mobileim.conversation.YWConversation;
import com.alibaba.mobileim.conversation.YWCustomConversationBody;
import com.alibaba.mobileim.kit.contact.YWContactHeadLoadHelper;
import com.alibaba.mobileim.lib.presenter.conversation.CustomConversation;
import com.alibaba.mobileim.ui.IYWConversationFragment;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.utils.UserUtil;

import java.lang.ref.WeakReference;

/**
 * 最近会话界面的定制点(根据需要实现相应的接口来达到自定义会话列表界面)，不设置则使用openIM默认的实现
 * 调用方设置的回调，必须继承BaseAdvice 根据不同的需求实现 不同的 开放的 Advice
 * com.alibaba.mobileim.aop.pointcuts包下开放了不同的Advice.通过实现多个接口，组合成对不同的ui界面的定制
 * 这里设置了自定义会话的定制
 * 1.CustomConversationAdvice 实现自定义会话的ui定制
 * 2.CustomConversationTitleBarAdvice 实现自定义会话列表的标题的ui定制
 * <p/>
 * 另外需要在application中将这个Advice绑定。设置以下代码
 * AdviceBinder.bindAdvice(PointCutEnum.CONVERSATION_FRAGMENT_POINTCUT, CustomChattingAdviceDemo.class);
 *
 * @author jing.huai
 */
public class ConversationListUICustom extends IMConversationListUI {


    public ConversationListUICustom(Pointcut pointcut) {
        super(pointcut);
    }

    /**
     * 返回会话列表的自定义标题
     *
     * @param fragment
     * @param context
     * @param inflater
     * @return
     */
    @Override
    public View getCustomConversationListTitle(final Fragment fragment,
                                               final Context context, LayoutInflater inflater) {
        //TODO 重要：必须以该形式初始化customView---［inflate(R.layout.**, new RelativeLayout(context),false)］------，以让inflater知道父布局的类型，否则布局xml**中定义的高度和宽度无效，均被默认的wrap_content替代
        RelativeLayout customView = (RelativeLayout) inflater
                .inflate(R.layout.title_bar_back_text, new RelativeLayout(context),false);
        TextView title = (TextView) customView.findViewById(R.id.title_text);
        final YWIMKit mIMKit = UserUtil.getIMKitInstance(fragment.getActivity());
        customView.findViewById(R.id.back_bn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment.getActivity().onBackPressed();
            }
        });
        title.setText("沟通列表");
        title.setTextColor(Color.WHITE);
        final String loginUserId = mIMKit.getIMCore().getLoginUserId();
        final String appKey =mIMKit.getIMCore().getAppKey();
        if(TextUtils.isEmpty(loginUserId)||TextUtils.isEmpty(appKey)){
            title.setText("未登录");
        }
        return customView;
    }



    @Override
    public boolean needHideTitleView(Fragment fragment) {
        return false;
    }

    @Override
    public boolean needHideNullNetWarn(Fragment fragment) {
        return false;
    }

    /**
     * 是否支持下拉刷新
     */
    @Override
    public  boolean getPullToRefreshEnabled(){
        return true;
    }


    /**
     * 返回自定义置顶回话的背景色(16进制字符串形式)
     * @return
     */
    @Override
    public String getCustomTopConversationColor() {
        return "#e1f5fe";
    }

    @Override
    public boolean enableSearchConversations(Fragment fragment){
        return true;
    }





    /**
     * 会话列表onDestroy事件
     * @param fragment
     */
    @Override
    public void onDestroy(Fragment fragment) {
        super.onDestroy(fragment);
    }

    /**
     * 会话列表Activity创建事件
     * @param savedInstanceState
     * @param fragment
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState, Fragment fragment) {
        super.onActivityCreated(savedInstanceState, fragment);
    }

    /**
     * 会话列表onResume事件
     * @param fragment
     */
    @Override
    public void onResume(Fragment fragment) {
        super.onResume(fragment);
    }


    private IYWConversationFragment mConversationFragment;
    /**
     * 会话列表初始化完成回调
     * @param fragment  会话列表Fragment
     */
    @Override
    public void onInitFinished(IYWConversationFragment fragment) {
//      //TODO 为了防止内存泄露这里请使用弱引用方式
        WeakReference<IYWConversationFragment> reference = new WeakReference<IYWConversationFragment>(fragment);
        //获取IYWConversationFragment实例，开发者可以通过该实例主动调用该接口内的方法
        mConversationFragment = reference.get();
//        //TODO 由于是弱引用，所以conversationFragment可能为null，因此使用时一定要判空
//        if (mConversationFragment != null){
//            //刷新adapter
//            mConversationFragment.refreshAdapter();
//        }
    }

    /**
     * 该方法可以构造一个会话列表为空时的展示View
     * @return
     *      empty view
     */
    @Override
    public View getCustomEmptyViewInConversationUI(Context context) {
        /** 以下为示例代码，开发者可以按需返回任何view*/
        TextView textView = new TextView(context);
        textView.setText("还没有会话!");
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(18);
        return textView;
    }

    /**
     * 返回设置最近联系人界面背景的资源Id,返回0则使用默认值
     * @return
     *      资源Id
     */
    @Override
    public int getCustomBackgroundResId() {
        return 0;
    }

    /*********** 以下是定制会话item view的示例代码 ***********/
    //有几种自定义，数组元素就需要几个，数组元素值从0开始
    //private final int[] viewTypeArray = {0,1,2,3}，这样就有4种自定义View
    private final int[] viewTypeArray = {0};
    /**
     * 自定义item view的种类数
     * @return 种类数
     */
    @Override
    public int getCustomItemViewTypeCount() {
        return viewTypeArray.length;
    }

    @Override
    public int getCustomItemViewType(YWConversation conversation) {
        if(conversation instanceof CustomConversation){
            return viewTypeArray[0];
        }
        return super.getCustomItemViewType(conversation);
    }

    @Override
    public View getCustomItemView(Fragment fragment, YWConversation conversation, View convertView, int viewType, YWContactHeadLoadHelper headLoadHelper, ViewGroup parent) {
        if(viewType==viewTypeArray[0]){
            ViewHolder1 holder = null;
            if (convertView == null){
                LayoutInflater inflater = LayoutInflater.from(fragment.getActivity());
                holder = new ViewHolder1();
                convertView = inflater.inflate(R.layout.demo_custom_conversation_item_1, parent, false);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.unread = (TextView) convertView.findViewById(R.id.unread);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder1) convertView.getTag();
            }

            String name = "";
            if (conversation.getConversationBody() instanceof YWCustomConversationBody) {
                YWCustomConversationBody body = (YWCustomConversationBody) conversation.getConversationBody();
                String conversationId = body.getIdentity();
                if(conversationId.equals("sysfrdreq")){
                    name = "好友请求";
                }else{
                    name = "这是一个自定义会话";
                }
            }
            holder.name.setText(name);
            holder.unread.setVisibility(View.GONE);
            int unreadCount = conversation.getUnreadCount();
            if (unreadCount > 0) {
                holder.unread.setVisibility(View.VISIBLE);
                if (unreadCount > 99){
                    holder.unread.setText("99+");
                }else {
                    holder.unread.setText(String.valueOf(unreadCount));
                }
            }
            return convertView;
        }
        return super.getCustomItemView(fragment, conversation, convertView, viewType, headLoadHelper, parent);
    }
    public class ViewHolder1{
        TextView unread;
        TextView name;
    }
}


