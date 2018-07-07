package com.tongtong.purchaser.activity;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.mobileim.conversation.YWMessage;
import com.alibaba.mobileim.conversation.YWMessageChannel;
import com.android.tu.loadingdialog.LoadingDailog;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.application.MyApplication;
import com.tongtong.purchaser.helper.ChattingUICustom;
import com.tongtong.purchaser.listener.NetChangeListener;
import com.tongtong.purchaser.utils.CodeUtil;
import com.tongtong.purchaser.utils.NetUtil;
import com.tongtong.purchaser.view.CustomDialog;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class BaseActivity extends AppCompatActivity {

	private MyApplication application;
	private CustomDialog.Builder builder ;
	private MyReciever reciever;
	private LoadingDailog loading;
	public static List<NetChangeListener> netChangeListeners=new ArrayList<>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		if (application == null) {

			application = (MyApplication) getApplication();
		}
		reciever=new MyReciever();
		IntentFilter filter=new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(reciever,filter);
		application.addActivity(this);
	    builder = new CustomDialog.Builder(this);
		builder.setPositiveButton(this.getResources()
				.getString(R.string.confirm), new OnClickListener(){
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				MyApplication myApplication = (MyApplication) getApplication();
				myApplication.finshALLActivity();
				Intent loginIntent = new Intent();
				loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
				loginIntent.setClass(BaseActivity.this, LoginActivity.class);
				myApplication.startActivity(loginIntent);
				dialog.dismiss();
			}
			
		});

	}
	public void showLoading(){
		LoadingDailog.Builder loadBuilder=new LoadingDailog.Builder(this)
				.setMessage("加载中...")
				.setCancelable(true)
				.setCancelOutside(true);
		loading=loadBuilder.create();
		loading.show();
	}
	public void showLoading(String text){
		LoadingDailog.Builder loadBuilder=new LoadingDailog.Builder(this)
				.setMessage(text)
				.setCancelable(true)
				.setCancelOutside(true);
		loading=loadBuilder.create();
		loading.show();
	}
	public void dismissLoading(){
		if(loading!=null){
			loading.dismiss();
		}
	}
	@Override
	protected void onDestroy() {
		application.removeActivity(this);
		unregisterReceiver(reciever);
		netChangeListeners.clear();
		super.onDestroy();

	}
	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
	}
	public void showToast(String msg) {
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.my_toast, null);

		TextView title = (TextView) layout.findViewById(R.id.text);
		title.setText(msg);
		Toast toast = new Toast(this);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(layout);
		toast.show();
	}
	
	public void showToast(int id) {
		String msg = this.getResources().getString(id);
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.my_toast, null);

		TextView title = (TextView) layout.findViewById(R.id.text);
		title.setText(msg);
		Toast toast = new Toast(this);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(layout);
		toast.show();
	}
	
	public void showOkToast(String msg) {
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.my_toast, null);
		ImageView icon = (ImageView) layout.findViewById(R.id.icon);
        icon.setImageResource(R.drawable.toast_icon_ok);
		TextView title = (TextView) layout.findViewById(R.id.text);
		title.setText(msg);
		Toast toast = new Toast(this);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(layout);
		toast.show();
	}
	
	public void showOkToast(int id) {
		String msg = this.getResources().getString(id);
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.my_toast, null);
        ImageView icon = (ImageView) layout.findViewById(R.id.icon);
        icon.setImageResource(R.drawable.toast_icon_ok);
		TextView title = (TextView) layout.findViewById(R.id.text);
		title.setText(msg);
		Toast toast = new Toast(this);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(layout);
		toast.show();
	}
	
	public boolean verification(int code){
		if(code == CodeUtil.TOKEN_NO_CODE){
			builder.setMessage(R.string.token_no_code_text);
			builder.create().show();
			return false;
		}
		else if(code == CodeUtil.TOKEN_INVALID_CODE){
			builder.setMessage(R.string.token_invalid_code_text);
			builder.create().show();
			return false;
		}
		else{
			return true;
		}
	}
	protected void showTips(String tips){
		final NormalDialog md=new NormalDialog(this);
		md.setCanceledOnTouchOutside(true);
		md.setCancelable(true);
		md.content(tips);
		md.btnNum(1);
		md.btnText("好的");
		md.titleLineColor(getResources().getColor(R.color.colorPrimary));
		md.titleTextColor(getResources().getColor(R.color.colorPrimary));
		md.titleTextSize(18);
		md.btnTextColor(getResources().getColor(R.color.colorPrimary));
		md.setOnBtnClickL(new OnBtnClickL() {
			@Override
			public void onBtnClick() {
				md.dismiss();
			}
		});
		md.show();
	}
	protected void showTips(Context context,String tips){
		final NormalDialog md=new NormalDialog(context);
		md.setCanceledOnTouchOutside(true);
		md.setCancelable(true);
		md.content(tips);
		md.btnNum(1);
		md.btnText("好的");
		md.titleLineColor(getResources().getColor(R.color.colorPrimary));
		md.titleTextColor(getResources().getColor(R.color.colorPrimary));
		md.titleTextSize(18);
		md.btnTextColor(getResources().getColor(R.color.colorPrimary));
		md.setOnBtnClickL(new OnBtnClickL() {
			@Override
			public void onBtnClick() {
				md.dismiss();
			}
		});
		md.show();
	}
	protected void showTips(Context context, String tips,final View.OnClickListener clickListener){
		final NormalDialog md=new NormalDialog(context);
		md.setCanceledOnTouchOutside(true);
		md.setCancelable(true);
		md.content(tips);
		md.btnNum(1);
		md.btnText("好的");
		md.titleLineColor(getResources().getColor(R.color.colorPrimary));
		md.titleTextColor(getResources().getColor(R.color.colorPrimary));
		md.titleTextSize(18);
		md.btnTextColor(getResources().getColor(R.color.colorPrimary));
		md.setOnBtnClickL(new OnBtnClickL() {
			@Override
			public void onBtnClick() {
				md.dismiss();
				if(clickListener!=null){
					clickListener.onClick(null);
				}
			}
		});
		md.show();
	}
	protected void sendSysMsg(String text){
		YWMessage localSysmsg= YWMessageChannel.createLocalSystemMessage(text);
		localSysmsg.setIsLocal(false);
		if(ChattingUICustom.conversation!=null){
			ChattingUICustom.conversation.getMessageSender().sendMessage(localSysmsg,120,null);
		}
	}
	private class MyReciever extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
				int netWorkState = NetUtil.getNetWorkState(context);
				// 接口回调传过去状态的类型
				for (NetChangeListener listener : netChangeListeners) {
					listener.onnetChange(netWorkState >= 0);
				}
			}
		}
	}
}
