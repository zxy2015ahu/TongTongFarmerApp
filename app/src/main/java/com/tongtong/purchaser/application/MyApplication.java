package com.tongtong.purchaser.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.support.multidex.MultiDex;

import com.alibaba.mobileim.YWAPI;
import com.alibaba.mobileim.aop.AdviceBinder;
import com.alibaba.mobileim.aop.PointCutEnum;
import com.alibaba.mobileim.contact.YWAppContactImpl;
import com.alibaba.mobileim.conversation.IYWConversationService;
import com.alibaba.mobileim.conversation.IYWConversationUnreadChangeListener;
import com.alibaba.mobileim.conversation.YWConversation;
import com.alibaba.sdk.android.feedback.impl.FeedbackAPI;
import com.alibaba.sdk.android.feedback.util.ErrorCode;
import com.alibaba.sdk.android.feedback.util.FeedbackErrorCallback;
import com.alibaba.wxlib.util.SysUtil;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.helper.ChattingOperationCustom;
import com.tongtong.purchaser.helper.ChattingUICustom;
import com.tongtong.purchaser.helper.ContactsOperationCustom;
import com.tongtong.purchaser.helper.ContactsUICustom;
import com.tongtong.purchaser.helper.ConversationListOperationCustom;
import com.tongtong.purchaser.helper.ConversationListUICustom;
import com.tongtong.purchaser.helper.MYWSDKGlobalConfig;
import com.tongtong.purchaser.service.TraceServiceImpl;
import com.tongtong.purchaser.utils.Constant;
import com.tongtong.purchaser.utils.DBManager;
import com.tongtong.purchaser.utils.MySSLSocketFactory;
import com.tongtong.purchaser.utils.UserUtil;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.PlatformConfig;
import com.xdandroid.hellodaemon.DaemonEnv;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import okhttp3.OkHttpClient;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;


public class MyApplication extends Application implements Thread.UncaughtExceptionHandler{

	private List<Activity> oList;

	public static MyApplication instance;
	public IYWConversationUnreadChangeListener unreadChangeListener;
	private static LocationClient locationClient;
	private DBManager dbManager;
	private OkHttpClient client;
	@Override
	public void onCreate() {
		super.onCreate();
		instance=this;
		dbManager=new DBManager(this);
		client=new OkHttpClient.Builder().sslSocketFactory(MySSLSocketFactory.getSocketFactory(this)).build();
		Thread.setDefaultUncaughtExceptionHandler(this);
		CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
				.setDefaultFontPath("font/aui-iconfont.ttf")
				.setFontAttrId(R.attr.fontPath)
				.build()
		);
		oList = new ArrayList<>();
		UMConfigure.setLogEnabled(false);
		UMConfigure.init(this,"5afea8a5b27b0a2fbe0000c6","umeng",UMConfigure.DEVICE_TYPE_PHONE,"");
		PlatformConfig.setWeixin("wxecc02aa17d9a802a", "03c84a6c76b74b4dd7af94ba9b7875ac");
		dbManager.copyDBFile();
		SysUtil.setApplication(this);
		SDKInitializer.initialize(this);
		locationClient=new LocationClient(this,getLocationClientOptions());
		FeedbackAPI.addErrorCallback(new FeedbackErrorCallback() {
			@Override
			public void onError(Context context, String s, ErrorCode errorCode) {

			}
		});
		FeedbackAPI.addLeaveCallback(new Callable() {
			@Override
			public Object call() throws Exception {
				return null;
			}
		});
		FeedbackAPI.init(this,"24853824","0a0bb8e8473d7a070036742b462698a3");
		FeedbackAPI.setTranslucent(false);
		FeedbackAPI.setBackIcon(R.drawable.white_bg_back);
		if(SysUtil.isTCMSServiceProcess(this)){
			return;
		}
		AdviceBinder.bindAdvice(PointCutEnum.CHATTING_FRAGMENT_UI_POINTCUT, ChattingUICustom.class);
		//AdviceBinder.bindAdvice(PointCutEnum.CHATTING_FRAGMENT_POINTCUT, ChattingActivity.class);
		AdviceBinder.bindAdvice(PointCutEnum.CHATTING_FRAGMENT_OPERATION_POINTCUT, ChattingOperationCustom.class);
		AdviceBinder.bindAdvice(PointCutEnum.CONVERSATION_FRAGMENT_UI_POINTCUT, ConversationListUICustom.class);
		AdviceBinder.bindAdvice(PointCutEnum.CONVERSATION_FRAGMENT_OPERATION_POINTCUT, ConversationListOperationCustom.class);
		AdviceBinder.bindAdvice(PointCutEnum.YWSDK_GLOBAL_CONFIG_POINTCUT, MYWSDKGlobalConfig.class);
		AdviceBinder.bindAdvice(PointCutEnum.CONTACTS_UI_POINTCUT, ContactsUICustom.class);
		AdviceBinder.bindAdvice(PointCutEnum.CONTACTS_OP_POINTCUT, ContactsOperationCustom.class);
		if(SysUtil.isMainProcess()){
			YWAPI.init(this, Constant.APP_KEY);
			YWAPI.setEnableCrashHandler(false);
			YWAPI.enableSDKLogOutput(true);
		}
	}
	public OkHttpClient getClient(){
		return client;
	}
	public void exit(){
		try {
			for(Activity activity:oList){
				activity.finish();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally{
			System.exit(0);
			Process.killProcess(Process.myPid());
		}
	}
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		exit();
	}
	public static LocationClient getLocationClient(){
		return locationClient;
	}
	public void startDaemonService(){
		DaemonEnv.initialize(this, TraceServiceImpl.class, DaemonEnv.DEFAULT_WAKE_UP_INTERVAL);
		TraceServiceImpl.sShouldStopService = false;
		DaemonEnv.startServiceMayBind(TraceServiceImpl.class);
	}
	public  void initPush(){
		IYWConversationService conversationService= UserUtil.getIMKitInstance(instance).getConversationService();
		//conversationService.removePushListener(pushListener);
		if(unreadChangeListener!=null){
			conversationService.removeTotalUnreadChangeListener(unreadChangeListener);
		}
		unreadChangeListener=new IYWConversationUnreadChangeListener() {
			@Override
			public void onUnreadChange() {
				sendRefreshAction();
			}
		};
		conversationService.addTotalUnreadChangeListener(unreadChangeListener);
	}
	private LocationClientOption getLocationClientOptions(){
		LocationClientOption option=new LocationClientOption();
		option.setCoorType("bd0911");
		option.setEnableSimulateGps(true);
		option.setIsNeedAddress(true);
		option.setOpenGps(true);
		option.setScanSpan(3000);
		option.setProdName(getResources().getString(R.string.app_name));
		option.setEnableSimulateGps(true);
		option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
		return  option;
	}
	private  void sendRefreshAction(){
		Intent intent=new Intent();
		intent.setAction(Constant.MSG_REFRESH);
		sendBroadcast(intent);
	}
	public static YWConversation getConversation(String userid){
		YWAppContactImpl.YWAppContactImplBuilder contact=new YWAppContactImpl.YWAppContactImplBuilder(userid,Constant.TARGET_APP_KEY);
		YWAppContactImpl impl=new YWAppContactImpl(contact);
		IYWConversationService conversationService=UserUtil.getIMKitInstance(instance).getConversationService();
		YWConversation conversation=conversationService.getConversationCreater().createConversationIfNotExist(impl);
		return conversation;
	}
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}
	public void addActivity(Activity activity) {
		
		if (!oList.contains(activity)) {
			oList.add(activity);
			
		}
	}

	public void removeActivity(Activity activity) {

		if (oList.contains(activity)) {
			oList.remove(activity);
		
		}
	}

	public void finshALLActivity() {

		for (int i=oList.size()-1;i>=0;i--) {
			oList.get(i).finish();
		}
		oList.clear();
		
	}
}
