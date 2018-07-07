package com.tongtong.purchaser.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.model.FarmerReleaseInformationModel;
import com.tongtong.purchaser.utils.Constant;
import com.tongtong.purchaser.utils.HttpImageLoadTask;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.HttpTask.HttpTaskHandler;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.view.ProgressDialog;
import com.tongtong.purchaser.view.RoundAngleImageView;

public class FarmerReleaseInfoActivity extends BaseActivity implements
		OnClickListener, HttpTaskHandler {

	private LinearLayout backBn;
	private View headBn;
	private FarmerReleaseInformationModel releaseInfo;
	private TextView produceNameText;
	private TextView estimatedQuantityText;
	private TextView locationText;
	private TextView timeText;
	private RoundAngleImageView headImageView;
	private Button callBn;
	private Button collectionBn;
	private ProgressDialog progressDialog;
	private boolean isVedioLoad = false;
	private boolean isCollection = false;
	
	private final static int SELECT_COLLECTION = 1;
	private final static int UPDATE_COLLECTION = 2;

	private ImageView player;
	private Button start;
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_farmer_release_info);
		backBn = (LinearLayout) findViewById(R.id.back_bn);
		backBn.setOnClickListener(this);
		headBn = findViewById(R.id.head_bn);
		start=(Button) findViewById(R.id.start);
		headBn.setOnClickListener(this);
		start.setOnClickListener(this);
		player=(ImageView) findViewById(R.id.video_view);
		releaseInfo = (FarmerReleaseInformationModel) getIntent()
				.getSerializableExtra("releaseInfo");
		//player.setUp(videoPath,JZVideoPlayerStandard.SCREEN_WINDOW_NORMAL,"");
		Glide.with(this).load(UrlUtil.IMG_SERVER_URL+releaseInfo.getReleaseVedioThumb())
				.centerCrop().into(player);
		headImageView = (RoundAngleImageView) findViewById(R.id.headimg);
		progressDialog = new ProgressDialog(this);
		String headUrl = "";
		if(releaseInfo.getFarmer()!=null){
			headUrl=releaseInfo.getFarmer().getHeadUrl();
		}
		HttpImageLoadTask HttpImageLoadTask = new HttpImageLoadTask(
				headImageView, R.drawable.default_head);
		HttpImageLoadTask.execute(headUrl);
		TextView titleText = (TextView) findViewById(R.id.title_text);
		titleText.setText(releaseInfo.getFarmer()!=null?releaseInfo.getFarmer().getName():"");
		produceNameText = (TextView) findViewById(R.id.produce_name);
		estimatedQuantityText = (TextView) findViewById(R.id.estimated_quantity);
		locationText = (TextView) findViewById(R.id.location);
		timeText = (TextView) findViewById(R.id.time);
		produceNameText.setText(releaseInfo.getProduce().getName());
		estimatedQuantityText.setText(String.format(this.getResources()
				.getString(R.string.estimated_quantity),
				releaseInfo.getEstimatedQuantity()
						+ releaseInfo.getProduce().getUnit()));
		locationText.setText(releaseInfo.getReleaseLocation());
		timeText.setText(String.format(
				this.getResources().getString(R.string.salas_time),
				releaseInfo.getStartTime(), releaseInfo.getEndTime()));
		estimatedQuantityText = (TextView) findViewById(R.id.estimated_quantity);
		locationText = (TextView) findViewById(R.id.location);
		callBn = (Button) findViewById(R.id.call_bn);
		callBn.setOnClickListener(this);
		collectionBn = (Button) findViewById(R.id.collection);
		collectionBn.setOnClickListener(this);

		JsonObject dataJson = new JsonObject();
		dataJson.addProperty("token", UserUtil.getUserModel(this).getToken());
		dataJson.addProperty("farmerReleaseId", releaseInfo.getId());
		HttpTask httpTask = new HttpTask(SELECT_COLLECTION,this);
		httpTask.setTaskHandler(this);
		httpTask.execute(UrlUtil.SELECT_COLLECTION, dataJson.toString());
	}
	
	

	@Override
	protected void onResume() {
		super.onResume();
	}


	@Override
	public void onBackPressed() {
//		if (JZVideoPlayer.backPress()) {
//			return;
//		}
		super.onBackPressed();
	}
	@Override
	protected void onPause() {
		super.onPause();
		//JZVideoPlayer.releaseAllVideos();
	}
	private void initCustomView(){
		View custom_view=getLayoutInflater().inflate(R.layout.my_cus_chat_view,null);
		ImageView img=(ImageView) custom_view.findViewById(R.id.img);
		TextView name=(TextView) custom_view.findViewById(R.id.name);
		TextView address=(TextView) custom_view.findViewById(R.id.address);
		Glide.with(this).load(UrlUtil.IMG_SERVER_URL+releaseInfo.getProduce().getIconUrl()).placeholder(R.drawable.no_icon).into(img);
		name.setText(releaseInfo.getProduce().getName());
		address.setText("种植地："+releaseInfo.getReleaseLocation());
		custom_view.findViewById(R.id.book).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent=new Intent();
				intent.putExtra("releaseInfo",releaseInfo);
				intent.setClass(FarmerReleaseInfoActivity.this,OrderInfoActivity.class);
				startActivity(intent);
			}
		});
		UserUtil.getIMKitInstance(this).showCustomView(custom_view);
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_bn:
			finish();
			break;
			case R.id.start:
				String videoPath=releaseInfo.getReleaseVedioUrl();
				if(!videoPath.startsWith("http")){
					videoPath=UrlUtil.VEDIO_SERVER_URL+releaseInfo.getReleaseVedioUrl();
				}
				//JZVideoPlayerStandard.startFullscreen(this, JZVideoPlayerStandard.class,videoPath, "");
				break;
		case R.id.call_bn:
			initCustomView();
			final String target = releaseInfo.getFarmer()!=null?releaseInfo.getFarmer().getPhone():""; //消息接收者ID
			final String appkey = Constant.TARGET_APP_KEY; //消息接收者appKey
			Intent intent = new Intent();
				intent.setClass(this,ChattingActivity.class);
			intent.putExtra(ChattingActivity.TARGET_ID,target);
			intent.putExtra(ChattingActivity.TARGET_APP_KEY,appkey);
			startActivity(intent);
			break;
		case R.id.head_bn:
			Intent farmerInfoIntent = new Intent();
			farmerInfoIntent.setClass(this, FarmerInfoActivity.class);
			farmerInfoIntent.putExtra("farmer", releaseInfo.getFarmer());
			startActivity(farmerInfoIntent);
			break;
		case R.id.collection:
			if(isCollection){
				isCollection = false;
				
				collectionBn.setBackgroundResource(R.drawable.collection_nomal);
			}
			else{
				isCollection = true;
				collectionBn.setBackgroundResource(R.drawable.collection_pressed);
			}
			JsonObject dataJson = new JsonObject();
			dataJson.addProperty("token", UserUtil.getUserModel(this).getToken());
			dataJson.addProperty("farmerReleaseId", releaseInfo.getId());
			dataJson.addProperty("isCollection", isCollection);
			HttpTask httpTask = new HttpTask(UPDATE_COLLECTION,this);
			httpTask.setTaskHandler(this);
			httpTask.execute(UrlUtil.UPDATE_COLLECTION, dataJson.toString());
			break;
		default:
			break;
		}

	}

	@Override
	public void taskStart(int code) {
		// TODO Auto-generated method stub

	}

	@Override
	public void taskSuccessful(String str, int code) {
		switch (code) {
		case SELECT_COLLECTION:
			JsonObject loadResultJson = new JsonParser().parse(str)
					.getAsJsonObject();
			int loadResultCode = loadResultJson.get("code").getAsInt();
			if (verification(loadResultCode)) {
				isCollection =  loadResultJson.get("data").getAsBoolean();
				collectionBn.setVisibility(View.VISIBLE);
				if(isCollection){
					collectionBn.setBackgroundResource(R.drawable.collection_pressed);
				}
				else{
					collectionBn.setBackgroundResource(R.drawable.collection_nomal);
				}
			}
			break;
		case UPDATE_COLLECTION:
			Intent intent = new Intent();
			intent.setAction(FarmerReleaseActivity.UPDATE_COLLECTION);
			sendBroadcast(intent);
			break;
		default:
			break;
		}

	}

	@Override
	public void taskFailed(int code) {
		// TODO Auto-generated method stub

	}
}
