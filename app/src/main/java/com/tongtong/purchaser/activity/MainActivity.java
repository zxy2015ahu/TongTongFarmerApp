package com.tongtong.purchaser.activity;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.alibaba.mobileim.channel.event.IWxCallback;
import com.githang.statusbar.StatusBarCompat;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.application.MyApplication;
import com.tongtong.purchaser.frament.MessageFragment;
import com.tongtong.purchaser.frament.ShouCaiFragment;
import com.tongtong.purchaser.frament.ShouyeFragment;
import com.tongtong.purchaser.frament.WodeFragment;
import com.tongtong.purchaser.utils.CodeUtil;
import com.tongtong.purchaser.utils.CommonUtils;
import com.tongtong.purchaser.utils.Constant;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.widget.AlertDialog;
import com.tongtong.purchaser.widget.MyFragmentTabHost;
import com.white.progressview.HorizontalProgressView;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends BaseActivity implements OnClickListener{
	private MyFragmentTabHost host;
	private RadioButton shouye,shoucai,xiaoxi,wode,temp;
	private HorizontalProgressView progress;
	private DownloadManager downloadManager;
	private long downloadId;
	private Timer timer;
	private DownLoadBroadcast downLoadBroadcast;
	private File save_apk_file;
	private Dialog dialog;
	private TextView msg_unread_count;
	private MsgReciever reciever;
	private int news_counts;
	private ImageView fabu;
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		StatusBarCompat.setTranslucent(getWindow(),true);
		host=(MyFragmentTabHost) findViewById(android.R.id.tabhost);
		host.setup(this,getSupportFragmentManager(),android.R.id.tabcontent);
		host.addTab(host.newTabSpec("shoucai").setIndicator("shoucai"), ShouCaiFragment.class,null);
		host.addTab(host.newTabSpec("xiaoxi").setIndicator("xiaoxi"), MessageFragment.class,null);
		host.addTab(host.newTabSpec("shouye").setIndicator("shouye"), ShouyeFragment.class,null);
		host.addTab(host.newTabSpec("wode").setIndicator("wode"), WodeFragment.class,null);
		shouye=(RadioButton) findViewById(R.id.shouye);
		shoucai=(RadioButton) findViewById(R.id.shoucai);
		xiaoxi=(RadioButton) findViewById(R.id.xiaoxi);
		wode=(RadioButton) findViewById(R.id.wode);
		fabu=(ImageView) findViewById(R.id.tab_icon);
		if(Build.VERSION.SDK_INT>20){
			fabu.setImageResource(R.drawable.ripple_drawable);
		}else{
			fabu.setImageResource(R.drawable.vector_drawable_fabu);
		}
		fabu.setOnClickListener(this);
		msg_unread_count=(TextView) findViewById(R.id.msg_unread_count);
		shouye.setOnClickListener(this);
		shoucai.setOnClickListener(this);
		xiaoxi.setOnClickListener(this);
		wode.setOnClickListener(this);
		temp=shoucai;
		reciever=new MsgReciever();
		IntentFilter filter=new IntentFilter();
		filter.addAction(Constant.MSG_REFRESH);
		filter.addAction(Constant.MSG_REFRESH_BY_LOADING);
		registerReceiver(reciever,filter);
		UserUtil.imLogin(this, new IWxCallback() {
			@Override
			public void onSuccess(Object... objects) {
				MyApplication.instance.initPush();
				setMsgCount();
			}
			@Override
			public void onError(int i, String s) {

			}
			@Override
			public void onProgress(int i) {

			}
		});
		checkVersion();
	}
	private class MsgReciever extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			if(Constant.MSG_REFRESH.equals(intent.getAction())){
				int total=news_counts+UserUtil.getIMKitInstance(MainActivity.this).getConversationService().getAllUnreadCount();
				if(total>0){
					msg_unread_count.setVisibility(View.VISIBLE);
					msg_unread_count.setText(String.valueOf(total));
				}else{
					msg_unread_count.setVisibility(View.GONE);
				}
				if(MessageFragment.getInstance()!=null){
					MessageFragment.getInstance().notifyMsg();
				}
			}else if(Constant.MSG_REFRESH_BY_LOADING.equals(intent.getAction())){
				if(MessageFragment.getInstance()!=null){
					MessageFragment.getInstance().onRefresh();
				}
			}
		}
	}
	private void setMsgCount(){
		if(UserUtil.getUserModel(this)==null){
			msg_unread_count.setVisibility(View.GONE);
		}else{
			HttpTask task=new HttpTask(this);
			task.setTaskHandler(new HttpTask.HttpTaskHandler() {
				@Override
				public void taskStart(int code) {

				}
				@Override
				public void taskSuccessful(String str, int code) {
					JsonObject selectResultJson = new JsonParser().parse(str)
							.getAsJsonObject();
					int selectResultCode = selectResultJson.get("code").getAsInt();
					if(selectResultCode== CodeUtil.SUCCESS_CODE){
						int news_count=selectResultJson.get("news_count").getAsInt();
						news_counts=news_count;
						news_count+=UserUtil.getIMKitInstance(MainActivity.this).getConversationService().getAllUnreadCount();
						if(news_count>0){
							msg_unread_count.setVisibility(View.VISIBLE);
							msg_unread_count.setText(String.valueOf(news_count));
						}else{
							msg_unread_count.setVisibility(View.GONE);
						}
					}
				}
				@Override
				public void taskFailed(int code) {

				}
			});
			JsonObject object=new JsonObject();
			object.addProperty("token",UserUtil.getUserModel(this).getToken());
			object.addProperty("purchaser_id",UserUtil.getUserModel(this).getId());
			task.execute(UrlUtil.GET_NEW_MSG_COUNT,object.toString());
		}
	}
	public void setCount(int count){
		news_counts=count;
		int total=news_counts+ UserUtil.getIMKitInstance(MainActivity.this).getConversationService().getAllUnreadCount();
		if(total>0){
			msg_unread_count.setVisibility(View.VISIBLE);
			msg_unread_count.setText(String.valueOf(total));
		}else{
			msg_unread_count.setVisibility(View.GONE);
		}
	}
	private void checkVersion(){
		HttpTask task=new HttpTask(this);
		task.setTaskHandler(new HttpTask.HttpTaskHandler() {
			@Override
			public void taskStart(int code) {

			}
			@Override
			public void taskSuccessful(String str, int code) {
				JsonObject selectResultJson = new JsonParser().parse(str)
						.getAsJsonObject();
				int selectResultCode = selectResultJson.get("code").getAsInt();
				if(verification(selectResultCode)){
					int version=selectResultJson.get("version").getAsInt();
					final String download_url=selectResultJson.get("download_url").getAsString();
					if(version> CommonUtils.getVersionCode(MainActivity.this)){
						final AlertDialog dialog=new AlertDialog(MainActivity.this).builder();
						dialog.setCancelable(false);
						dialog.setContentGravity(Gravity.LEFT|Gravity.TOP);
						dialog.setTitle("新版本更新("+selectResultJson.get("version_name").getAsString()+")");
						dialog.setMsg(selectResultJson.get("desc").getAsString());
						dialog.setPositiveButton("更新", new OnClickListener() {
							@Override
							public void onClick(View v) {
								initdownload(download_url);
								showDialog();
							}
						});
						dialog.setNegativeButton("忽略", new OnClickListener() {
							@Override
							public void onClick(View v) {

							}
						});
						dialog.show();
					}
				}
			}
			@Override
			public void taskFailed(int code) {

			}
		});
		JsonObject data=new JsonObject();
		data.addProperty("client_type","purchaser");
		task.execute(UrlUtil.CHECK_VERSION,data.toString());
	}
	private void initdownload(String url){
		downloadManager=(DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
		request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
		request.setAllowedOverRoaming(true);
		request.setVisibleInDownloadsUi(true);
		save_apk_file=new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "ttsc_purchaser.apk");
		if(save_apk_file.exists()){
			save_apk_file.delete();
		}
		request.setDestinationUri(Uri.fromFile(save_apk_file));
		downloadId = downloadManager.enqueue(request);
	}
	private int[] getBytesAndStatus() {
		int[] bytesAndStatus = new int[]{
				-1, -1, 0
		};
		DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
		Cursor cursor = null;
		try {
			cursor = downloadManager.query(query);
			if (cursor != null && cursor.moveToFirst()) {
				//已经下载文件大小
				bytesAndStatus[0] = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
				//下载文件的总大小
				bytesAndStatus[1] = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
				//下载状态
				bytesAndStatus[2] = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return bytesAndStatus;
	}
	private Handler handler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			progress.setProgress(msg.arg1);
			if(msg.arg2==8){
				timer.cancel();
				dialog.dismiss();
			}
		}
	};
	private void showDialog(){
		dialog=new Dialog(this, R.style.CustomDialog);
		dialog.setContentView(R.layout.dialog_loading_view);
		Window window=dialog.getWindow();
		WindowManager.LayoutParams lp=window.getAttributes();
		DisplayMetrics dm=new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		lp.width=dm.widthPixels- UIUtil.dip2px(this,40f);
		window.setAttributes(lp);
		progress=(HorizontalProgressView) dialog.findViewById(R.id.progress);
		dialog.show();
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				timer.cancel();
			}
		});
		registerBroadcast();
		timer=new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				Message msg=new Message();
				int[] data=getBytesAndStatus();
				if(data[0]!=-1&&data[1]!=-1&&data[2]!=0){
					msg.arg1=Math.round((data[0]/(float)data[1])*100);
					msg.arg2=data[2];
				}
				handler.sendMessage(msg);
			}
		},0,200);
	}
	/**
	 * 注册广播
	 */
	private void registerBroadcast() {
		/**注册service 广播 1.任务完成时 2.进行中的任务被点击*/
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		intentFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
		registerReceiver(downLoadBroadcast = new DownLoadBroadcast(), intentFilter);
	}
	private class DownLoadBroadcast extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())){
				try{
					CommonUtils.installApk(MainActivity.this,save_apk_file.getPath());
				}catch (Exception e){

				}
			}else if(DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(intent.getAction())){
				Intent i=new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);
			}
		}
	}
	@Override
	public void onClick(View v) {
		if(v instanceof RadioButton){
			RadioButton rb=(RadioButton) v;
			if(temp==rb){
				return;
			}
			temp.setChecked(false);
			rb.setChecked(true);
			temp=rb;
			switch (rb.getId()) {
				case R.id.shouye:
					host.setCurrentTab(2);
					break;
				case R.id.shoucai:
					host.setCurrentTab(0);
					break;
				case R.id.xiaoxi:
					host.setCurrentTab(1);
					break;
				case R.id.wode:
					if(WodeFragment.getInstance()!=null){
						WodeFragment.getInstance().getData();
					}
					host.setCurrentTab(3);
					break;
			}
		}
		if(v.getId()==R.id.tab_icon){
			Intent intent=new Intent();
			intent.setClass(this,FabuActivity.class);
			intent.putExtra("result",getIntent().getStringExtra("result"));
			startActivity(intent);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(downLoadBroadcast!=null){
			unregisterReceiver(downLoadBroadcast);
		}
		if(timer!=null){
			timer.cancel();
		}
	}
}
