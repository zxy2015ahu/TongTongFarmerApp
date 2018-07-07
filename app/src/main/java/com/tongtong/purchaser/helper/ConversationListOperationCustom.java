package com.tongtong.purchaser.helper;

import android.content.Intent;
import android.support.v4.app.Fragment;

import com.alibaba.mobileim.aop.Pointcut;
import com.alibaba.mobileim.aop.custom.IMConversationListOperation;
import com.alibaba.mobileim.contact.IYWContact;
import com.alibaba.mobileim.conversation.YWConversation;
import com.alibaba.mobileim.conversation.YWConversationBody;
import com.alibaba.mobileim.conversation.YWConversationType;
import com.alibaba.mobileim.conversation.YWCustomConversationBody;
import com.alibaba.mobileim.conversation.YWP2PConversationBody;
import com.alibaba.mobileim.lib.presenter.conversation.CustomConversation;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.activity.ChattingActivity;
import com.tongtong.purchaser.activity.FriendsAddActivity;
import com.tongtong.purchaser.utils.Constant;
import com.tongtong.purchaser.utils.UserUtil;

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
public class ConversationListOperationCustom extends IMConversationListOperation {

    public ConversationListOperationCustom(Pointcut pointcut) {
        super(pointcut);
    }



    @Override
    public int getConversationDefaultHead(Fragment fragment,
                                          YWConversation conversation) {
        return R.drawable.default_head;
    }
    /**
     * 定制会话点击事件，该方法可以定制所有的会话类型，包括单聊、群聊、自定义会话
     *
     * @param fragment     会话列表fragment
     * @param conversation 当前点击的会话对象
     * @return true: 使用用户自定义的点击事件  false：使用SDK默认的点击事件
     */
    @Override
    public boolean onItemClick(Fragment fragment, YWConversation conversation) {
        YWConversationType type = conversation.getConversationType();
        if (type == YWConversationType.P2P){
            //TODO 单聊会话点击事件
            UserUtil.getIMKitInstance(fragment.getActivity()).showCustomView(null);
            YWP2PConversationBody body=(YWP2PConversationBody) conversation.getConversationBody();
            IYWContact contact=body.getContact();
            String userid=contact.getUserId();
            Intent intent=new Intent();
            intent.setClass(fragment.getActivity(), ChattingActivity.class);
            intent.putExtra(ChattingActivity.TARGET_ID,userid);
            intent.putExtra(ChattingActivity.TARGET_APP_KEY, Constant.TARGET_APP_KEY);
            fragment.startActivity(intent);
            return true;
        }else if(type == YWConversationType.SHOP){
            UserUtil.getIMKitInstance(fragment.getActivity()).showCustomView(null);
            Intent intent=new Intent();
            intent.putExtra(ChattingActivity.TARGET_ID, Constant.SERVER_ACCOUNT);
            intent.putExtra(ChattingActivity.TARGET_ESERVICE,ChattingActivity.TARGET_ESERVICE);
            intent.setClass(fragment.getActivity(),ChattingActivity.class);
            fragment.startActivity(intent);
            return true;
        }else if(conversation.getConversationBody() instanceof YWCustomConversationBody){
            YWCustomConversationBody body=(YWCustomConversationBody)conversation.getConversationBody();
            if(body.getIdentity().equals("sysfrdreq")){
                Intent intent=new Intent();
                intent.setClass(fragment.getActivity(), FriendsAddActivity.class);
                fragment.startActivity(intent);
            }
        }
        return false;
    }

    @Override
    public String getConversationName(Fragment fragment, YWConversation conversation) {
        return null;
    }

    @Override
    public String getConversationNameV2(Fragment fragment, YWConversation conversation) {
        return super.getConversationNameV2(fragment, conversation);
    }
}

