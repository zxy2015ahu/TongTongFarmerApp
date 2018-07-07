package com.tongtong.purchaser.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.adapter.BuyListAdapter;
import com.tongtong.purchaser.adapter.BuyListAdapter.RemoverListener;
import com.tongtong.purchaser.model.PurchaserReleaseInformationModel;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.HttpTask.HttpTaskHandler;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.view.LoadingDialog;

import java.util.ArrayList;
import java.util.List;

public class BuyActivity extends BaseActivity implements OnClickListener,
		HttpTaskHandler, RemoverListener {
	private View rightBn;
	private LinearLayout backBn;
	private ListView list;
	private BuyListAdapter buyListAdapter;
	private Button releaseBn;
	private List<PurchaserReleaseInformationModel> purchaserReleaseInformations = new ArrayList<PurchaserReleaseInformationModel>();
	private LoadingDialog loadingDialog;
	private LoadingDialog submitDialog;

	private final static int RELEASE = 1;
	private final static int SELECT = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_buy);
		TextView titleText = (TextView) findViewById(R.id.title_text);
		titleText.setText(R.string.buy_title);
		rightBn = findViewById(R.id.right_bn);
		rightBn.setOnClickListener(this);
		backBn = (LinearLayout) findViewById(R.id.back_bn);
		backBn.setOnClickListener(this);
		releaseBn = (Button) findViewById(R.id.release_bn);
		releaseBn.setOnClickListener(this);
		list = (ListView) findViewById(R.id.list);
		loadingDialog = new LoadingDialog(this);
		loadingDialog.setMessage(R.string.loading);
		submitDialog = new LoadingDialog(this);
		submitDialog.setMessage(R.string.submit_loading);

		JsonObject dataJson = new JsonObject();
		dataJson.addProperty("token", UserUtil.getUserModel(this).getToken());
		
		HttpTask initHttp = new HttpTask(SELECT,this);
		initHttp.setTaskHandler(this);
		initHttp.execute(UrlUtil.FIND_RELEASE, dataJson.toString());

		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.right_bn:
			Intent intent = new Intent();
			intent.setClass(this, ChoiceProduceActivity.class);
			startActivityForResult(intent, 0);
			break;
		case R.id.back_bn:
			finish();
			break;
		case R.id.release_bn:
			
			JsonObject dataJson = new JsonObject();
			Gson gson = new Gson();
			JsonArray releaseInfoArr = gson.toJsonTree(
					purchaserReleaseInformations).getAsJsonArray();
			dataJson.addProperty("token", UserUtil.getUserModel(this)
					.getToken());
			dataJson.add("releaseInfos", releaseInfoArr);
			HttpTask saveHttp = new HttpTask(RELEASE,this);
			saveHttp.setTaskHandler(this);
			saveHttp.execute(UrlUtil.RELEASE, dataJson.toString());
			break;
		default:
			break;
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case 1:
			PurchaserReleaseInformationModel releaseInfo =  (PurchaserReleaseInformationModel) data
					.getSerializableExtra("releaseInfo");
			purchaserReleaseInformations.add(releaseInfo);

			buyListAdapter = new BuyListAdapter(this,
					purchaserReleaseInformations,this);
			list.setAdapter(buyListAdapter);
			break;
		default:
			break;
		}
	}

	@Override
	public void taskStart(int code) {
		switch (code) {
		case RELEASE:
			submitDialog.show();
			break;
		case SELECT:
			loadingDialog.show();
			break;
		default:
			break;
		}

	}

	@Override
	public void taskSuccessful(String str, int code) {
		switch (code) {
		case RELEASE:
			JsonObject releaseResultJson = new JsonParser().parse(str)
					.getAsJsonObject();
			int releaseResultCode = releaseResultJson.get("code").getAsInt();
			if (verification(releaseResultCode)) {
				Intent mainIntent = new Intent();
				mainIntent.setAction("");
				sendBroadcast(mainIntent);
				finish();
			}
			submitDialog.dismiss();
			break;
		case SELECT:
			
			JsonObject selectResultJson = new JsonParser().parse(str)
					.getAsJsonObject();
			int selectResultCode = selectResultJson.get("code").getAsInt();
			if (verification(selectResultCode)) {
				JsonArray releaseInfoArr = selectResultJson.get("data")
						.getAsJsonArray();
				Gson gson = new Gson();
				purchaserReleaseInformations = gson.fromJson(releaseInfoArr, new TypeToken<List<PurchaserReleaseInformationModel>>(){}.getType());
				
				
				buyListAdapter = new BuyListAdapter(this,purchaserReleaseInformations,this);
				list.setAdapter(buyListAdapter);
				
			}
			loadingDialog.dismiss();
			break;
		default:
			break;
		}

	}

	@Override
	public void taskFailed(int code) {
		switch (code) {
		case RELEASE:
			submitDialog.dismiss();
			break;
		case SELECT:
			loadingDialog.dismiss();
			break;
		default:
			break;
		}

	}

	@Override
	public void onRemove(int index) {
		purchaserReleaseInformations.remove(index);
		buyListAdapter = new BuyListAdapter(this,
				purchaserReleaseInformations,this);
		list.setAdapter(buyListAdapter);
	}
}
