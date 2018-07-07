package com.tongtong.purchaser.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.model.ProduceModel;
import com.tongtong.purchaser.utils.HttpImageLoadTask;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.HttpTask.HttpTaskHandler;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.view.LoadingDialog;

import java.io.Serializable;
import java.util.List;

public class ChoiceProduceActivity extends BaseActivity implements
		OnClickListener, HttpTaskHandler {

	private LinearLayout backBn;
	private EditText search;
	private TextView searchBn;
	private GridLayout produceInfoLayout;
	private LoadingDialog loadingDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choice_produce);
		loadingDialog = new LoadingDialog(this);
		loadingDialog.setMessage(R.string.loading);
		search = (EditText) findViewById(R.id.search);
		backBn = (LinearLayout) findViewById(R.id.back_bn);
		backBn.setOnClickListener(this);
		TextView titleText = (TextView) findViewById(R.id.title_text);
		titleText.setText(R.string.choice_produce_title);
		searchBn = (TextView) findViewById(R.id.search_bn);
		searchBn.setOnClickListener(this);
		produceInfoLayout = (GridLayout) findViewById(R.id.produce_info_layout);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_bn:
			finish();
			break;
		case R.id.search_bn:
			JsonObject dataJson = new JsonObject();
			dataJson.addProperty("token", UserUtil.getUserModel(this)
					.getToken());
			dataJson.addProperty("name", search.getText().toString());
			HttpTask saveHttp = new HttpTask(this);
			saveHttp.setTaskHandler(this);
			saveHttp.execute(UrlUtil.SEARCH_PRODUCE, dataJson.toString());
			break;
		default:
			break;
		}

	}

	@Override
	public void taskStart(int code) {
		loadingDialog.show();
	}

	@Override
	public void taskSuccessful(String str, int code) {
		JsonObject resultJson = new JsonParser().parse(str).getAsJsonObject();
		int resultCode = resultJson.get("code").getAsInt();
		if (verification(resultCode)) {
			JsonArray produceArr = resultJson.get("data").getAsJsonArray();
			Gson gson = new Gson();
			List<ProduceModel> produces = gson.fromJson(produceArr,
					new TypeToken<List<ProduceModel>>() {
					}.getType());
			initInfoLayout(produces);
		}
		loadingDialog.dismiss();
	}

	@Override
	public void taskFailed(int code) {
		loadingDialog.dismiss();
	}

	private void initInfoLayout(List<ProduceModel> produces) {
		produceInfoLayout.removeAllViews();
		int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
		if (produces.size() > 0) {
			int columnCount = produceInfoLayout.getColumnCount();
			LayoutInflater inflater = this.getLayoutInflater();
			for (int i = 0; i < produces.size(); i++) {
				final ProduceModel produce = produces.get(i);
				View bnLayout = inflater.inflate(R.layout.produce_bn, null);
				ImageView icon = (ImageView) bnLayout.findViewById(R.id.icon);
				TextView label = (TextView) bnLayout.findViewById(R.id.label);
				label.setText(produce.getName());
				HttpImageLoadTask HttpImageLoadTask = new HttpImageLoadTask(icon,R.drawable.no_icon);
				HttpImageLoadTask.execute(produce.getIconUrl());
				produceInfoLayout.addView(bnLayout);
				LayoutParams lp = (LayoutParams) bnLayout.getLayoutParams();
				lp.width = screenWidth / columnCount;
				bnLayout.setLayoutParams(lp);
				bnLayout.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Intent intent = new Intent();
						intent.setClass(ChoiceProduceActivity.this, ReleaseInfoActivity.class);
						intent.putExtra("produce", (Serializable)produce);
						ChoiceProduceActivity.this.startActivityForResult(intent, 0);
						
					}
				});
			}
		}
		else{
			showToast(R.string.no_content);
		}

	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case 1:
			
			setResult(1,data);
			ChoiceProduceActivity.this.finish();
			break;
		default:
			break;
	 }
	}

}
