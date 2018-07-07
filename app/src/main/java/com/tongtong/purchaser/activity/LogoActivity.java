package com.tongtong.purchaser.activity;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.application.MyApplication;
import com.tongtong.purchaser.model.MyProduceModel;
import com.tongtong.purchaser.model.MySpecificationModel;
import com.tongtong.purchaser.model.ProduceType;
import com.tongtong.purchaser.utils.CodeUtil;
import com.tongtong.purchaser.utils.DBManager;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.ProduceTypesHelper;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.view.FullScreenVideoView;
import com.tongtong.purchaser.widget.AlertDialog;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;


public class LogoActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks,
		BDLocationListener{
	private int count = 3000;
	private TextView countDown;
	private static final String[] LOCATION_AND_CONTACTS =
			{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.CALL_PHONE,
					Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO,

			};
	private static final int RESULT_CODE=123;
	private LocationClient client;
	private double lat,lng;
	private SharedPreferences sp;
	private boolean isFirst=true;
	private boolean is_exit=false;
	private ImageView target;
	private View count_layout;
	private String result;
	private static final int MESSAGE_SUCCESS=100;
	private static final int MESSAGE_GIF_SUCCESS=101;
	private boolean is_direct=false;
	private RelativeLayout root;
	private SharedPreferences mylocation;
	private DBManager dbManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_logo);
		dbManager=new DBManager(this);
		target=(ImageView) findViewById(R.id.target_layout);
		count_layout=findViewById(R.id.count_layout);
		root=(RelativeLayout) findViewById(R.id.root);
		count_layout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				is_exit=true;
				handler.removeMessages(0);
				Intent intent = new Intent();
				intent.putExtra("result",result);
				intent.setClass(LogoActivity.this, MainActivity.class);
				startActivity(intent);
				finish();
			}
		});
		client= MyApplication.getLocationClient();
		client.registerLocationListener(this);
		sp=getSharedPreferences("location", Context.MODE_PRIVATE);
		mylocation=getSharedPreferences("mylocation", Context.MODE_PRIVATE);
		countDown = (TextView) findViewById(R.id.count_down);
		if(EasyPermissions.hasPermissions(this,LOCATION_AND_CONTACTS)){
			client.start();
		}else{
			EasyPermissions.requestPermissions(
					this,
					"app需要您赋予权限才能够正常运行!",
					RESULT_CODE,
					LOCATION_AND_CONTACTS);
		}
	}

	private void getConfig(){
		HttpTask task=new HttpTask(this);
		task.setTaskHandler(new HttpTask.HttpTaskHandler() {
			@Override
			public void taskStart(int code) {

			}

			@Override
			public void taskSuccessful(String str, int code) {
				result=str;
				JsonObject selectResultJson = new JsonParser().parse(str)
						.getAsJsonObject();
				int selectResultCode = selectResultJson.get("code").getAsInt();
				if(selectResultCode== CodeUtil.SUCCESS_CODE){
					handleProduceSpecification(selectResultJson);
					sp.edit().putString("hot_search",selectResultJson.get("hot_search").getAsString()).commit();
					sp.edit().putString("unit",selectResultJson.get("unit").getAsString()).commit();
					String pic=selectResultJson.get("farmer_first_screen").getAsString();
					if(pic.toUpperCase().endsWith(".MP4")||pic.toUpperCase().endsWith(".M3U8")){
						final FullScreenVideoView videoView=new FullScreenVideoView(LogoActivity.this);
						videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
							@Override
							public void onPrepared(MediaPlayer mp) {
								AlphaAnimation alphaAnimation=new AlphaAnimation(1f,0f);
								alphaAnimation.setFillAfter(true);
								alphaAnimation.setDuration(500);
								alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
									@Override
									public void onAnimationStart(Animation animation) {

									}
									@Override
									public void onAnimationEnd(Animation animation) {
										target.setVisibility(View.GONE);
									}

									@Override
									public void onAnimationRepeat(Animation animation) {

									}
								});
								target.startAnimation(alphaAnimation);
								videoView.start();
							}
						});
						videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
							@Override
							public void onCompletion(MediaPlayer mp) {
								handler.sendEmptyMessage(MESSAGE_GIF_SUCCESS);
							}
						});
						videoView.setVideoPath(pic.startsWith("http")?pic:(UrlUtil.IMG_SERVER_URL+pic));
						videoView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
						root.addView(videoView,0);
					}else{
						Glide.with(LogoActivity.this).load(UrlUtil.IMG_SERVER_URL+pic).asBitmap()
								.diskCacheStrategy(DiskCacheStrategy.SOURCE).into(new ImageViewTarget<Bitmap>(target) {
							@Override
							protected void setResource(Bitmap resource) {
								target.setImageBitmap(resource);
								handler.post(new Runnable() {
									@Override
									public void run() {
										count_layout.setVisibility(View.VISIBLE);
									}
								});
								countTownThread.start();
							}
						});
					}
				}else{
					AlertDialog dialog=new AlertDialog(LogoActivity.this).builder();
					dialog.setTitle("提示");
					dialog.setMsg("初始化发生错误，请退出后重试");
					dialog.setCancelable(false);
					dialog.setPositiveButton("确定", new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							MyApplication.instance.exit();
						}
					});
					dialog.show();
				}
			}
			@Override
			public void taskFailed(int code) {
				AlertDialog dialog=new AlertDialog(LogoActivity.this).builder();
				dialog.setTitle("提示");
				dialog.setMsg("初始化发生错误，请退出后重试");
				dialog.setCancelable(false);
				dialog.setPositiveButton("确定", new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						MyApplication.instance.exit();
					}
				});
				dialog.show();
			}
		});
		JsonObject object=new JsonObject();
		object.addProperty("purchaser_id",UserUtil.getUserModel(this)!=null?UserUtil.getUserModel(this).getId():0);
		object.addProperty("adcode",mylocation.getInt("code",0)!=0?mylocation.getInt("code",0):sp.getInt("code",0));
		object.addProperty("lat",Double.valueOf(sp.getString("lat","0")));
		object.addProperty("lng",Double.valueOf(sp.getString("lng","0")));
		task.execute(UrlUtil.GET_CONFIG,object.toString());
	}

	private void handleProduceSpecification(final JsonObject selectResultJson){
		int api_version=selectResultJson.get("api_version").getAsInt();
		int local_version=sp.getInt("local_version",-1);
		if(local_version>=api_version){
			return;
		}
		sp.edit().putInt("local_version",api_version).commit();
		new Thread(){
			@Override
			public void run() {
					ProduceTypesHelper.deleteProduceTypes(LogoActivity.this);
					JsonArray produce_types=selectResultJson.get("produces").getAsJsonArray();
					Gson gson=new Gson();
					List<MyProduceModel> produceTypes=gson.fromJson(produce_types,
							new TypeToken<List<MyProduceModel>>() {
							}.getType());
					ProduceTypesHelper.addProduceType(LogoActivity.this,produceTypes);
			}
		}.start();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		client.unRegisterLocationListener(this);
		is_exit=true;
		handler.removeMessages(0);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		// Forward results to EasyPermissions
		EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
	}
	@Override
	public void onPermissionsGranted(int requestCode, List<String> list) {
		// Some permissions have been granted
		// ...
		client.start();
	}

	@Override
	public void onPermissionsDenied(int requestCode, List<String> perms) {
		// Some permissions have been denied
		// ...
		if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
			new AppSettingsDialog.Builder(this,"请到设置中赋予app充分的权限，以便app能够正常的运行！").build().show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(EasyPermissions.hasPermissions(this,LOCATION_AND_CONTACTS)){
			client.start();
		}else{
			new AppSettingsDialog.Builder(this,"请到设置中赋予app定位的权限，以便app能够正常的运行！").build().show();
		}
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			countDown.setText("跳过("+msg.what+")");
			if(msg.what==0){
				if(UserUtil.getUserModel(LogoActivity.this)!=null){
					Intent intent = new Intent();
					intent.putExtra("result",result);
					intent.setClass(LogoActivity.this, MainActivity.class);
					startActivity(intent);
					finish();
				}else{
					Intent intent = new Intent();
					intent.setClass(LogoActivity.this, LoginActivity.class);
					LogoActivity.this.startActivity(intent);
					LogoActivity.this.finish();
				}
			}else if(msg.what==MESSAGE_SUCCESS){
				client.start();
			}else if(msg.what==MESSAGE_GIF_SUCCESS){
				is_direct=true;
				client.start();
			}
		}

	};

	private Thread countTownThread = new Thread() {

		@Override
		public void run() {
			for (int i = count / 1000; i > 0; i--) {
				try {
					if(is_exit){
						return;
					}
					handler.sendEmptyMessage(i);
					sleep(1000);
				} catch (InterruptedException e) {

					e.printStackTrace();
				}
			}
			handler.sendEmptyMessage(0);
		}

	};

	@Override
	public void onReceiveLocation(BDLocation bdLocation) {
		client.stop();
		if(bdLocation!=null){
			lat=bdLocation.getLatitude();
			lng=bdLocation.getLongitude();
			if(lat==0&&lng==0){
				client.restart();
				return;
			}
			SharedPreferences.Editor editor=sp.edit();
			editor.putString("lat",lat+"");
			editor.putString("lng",lng+"");
			editor.putInt("code",dbManager.getCityAdcode(bdLocation.getAdCode()));
			editor.putString("city",bdLocation.getCity());
			editor.commit();
		}
		if(isFirst){
			isFirst=false;
			if(is_direct){
				handler.sendEmptyMessage(0);
			}else{
				handler.post(new Runnable() {
					@Override
					public void run() {
						getConfig();
					}
				});
			}
		}
	}
}
