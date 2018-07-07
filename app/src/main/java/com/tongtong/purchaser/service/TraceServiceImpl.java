package com.tongtong.purchaser.service;


import android.content.Intent;
import android.os.IBinder;

import com.alibaba.mobileim.IYWPushListener;
import com.alibaba.mobileim.contact.IYWContact;
import com.alibaba.mobileim.conversation.IYWConversationService;
import com.alibaba.mobileim.conversation.YWConversation;
import com.alibaba.mobileim.conversation.YWCustomMessageBody;
import com.alibaba.mobileim.conversation.YWMessage;
import com.alibaba.mobileim.conversation.YWMessageBody;
import com.alibaba.mobileim.conversation.YWMessageChannel;
import com.alibaba.mobileim.gingko.model.tribe.YWTribe;
import com.tongtong.purchaser.utils.UserUtil;
import com.xdandroid.hellodaemon.AbsWorkService;

import io.reactivex.disposables.Disposable;

/**
 * Created by zxy on 2018/2/16.
 */
public class TraceServiceImpl extends AbsWorkService {

    //是否 任务完成, 不再需要服务运行?
    public static boolean sShouldStopService;
    public static Disposable sDisposable;
    public static IYWPushListener p2p;
    private static IYWConversationService conversationService;
    public static void stopService() {
        //我们现在不再需要服务运行了, 将标志位置为 true
        sShouldStopService = true;
        //取消对任务的订阅
        if (sDisposable != null) sDisposable.dispose();
        //取消 Job / Alarm / Subscription
        cancelJobAlarmSub();
        if(p2p!=null){
            conversationService.removePushListener(p2p);
        }
    }

    /**
     * 是否 任务完成, 不再需要服务运行?
     * @return 应当停止服务, true; 应当启动服务, false; 无法判断, 什么也不做, null.
     */
    @Override
    public Boolean shouldStopService(Intent intent, int flags, int startId) {
        return sShouldStopService;
    }

    @Override
    public void startWork(Intent intent, int flags, int startId) {
        initPush();
    }

    @Override
    public void stopWork(Intent intent, int flags, int startId) {
        stopService();
    }

    /**
     * 任务是否正在运行?
     * @return 任务正在运行, true; 任务当前不在运行, false; 无法判断, 什么也不做, null.
     */
    @Override
    public Boolean isWorkRunning(Intent intent, int flags, int startId) {
        //若还没有取消订阅, 就说明任务仍在运行.
        return sDisposable != null && !sDisposable.isDisposed();
    }

    @Override
    public IBinder onBind(Intent intent, Void v) {
        conversationService = UserUtil.getIMKitInstance(TraceServiceImpl.this).getConversationService();
        return null;
    }

    @Override
    public void onServiceKilled(Intent rootIntent) {
        if(p2p!=null){
            conversationService.removePushListener(p2p);
        }
    }

    public  void initPush(){
        if(conversationService==null) {
            conversationService = UserUtil.getIMKitInstance(TraceServiceImpl.this).getConversationService();
        }
        if(p2p!=null){
            conversationService.removePushListener(p2p);
            p2p=null;
        }
        p2p=new IYWPushListener() {
            @Override
            public void onPushMessage(IYWContact iywContact, YWMessage ywMessage) {
                if(ywMessage.getSubType() == YWMessage.SUB_MSG_TYPE.IM_P2P_CUS){
                    YWMessageBody body=ywMessage.getMessageBody();
                    if(body instanceof YWCustomMessageBody){
                        YWCustomMessageBody content = (YWCustomMessageBody) body;
                        String con=content.getContent();
                        if(content.getTransparentFlag()==1){
                            IYWConversationService conversationService= UserUtil.getIMKitInstance(TraceServiceImpl.this).getConversationService();
                            YWConversation conversation=conversationService.getConversationCreater().createConversationIfNotExist(iywContact);
                            if(conversation!=null){
                                YWMessage localSysmsg= YWMessageChannel.createLocalSystemMessage(con);
                                localSysmsg.setIsLocal(false);
                                conversation.getMessageSender().sendMessage(localSysmsg,120,null);
                            }
                        }
                    }
                }
            }

            @Override
            public void onPushMessage(YWTribe ywTribe, YWMessage ywMessage) {

            }
        };
        //conversationService.removePushListener(pushListener);
        conversationService.addPushListener(p2p);
    }
}
