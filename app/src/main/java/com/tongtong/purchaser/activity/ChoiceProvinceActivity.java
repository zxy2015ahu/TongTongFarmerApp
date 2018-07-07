package com.tongtong.purchaser.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.adapter.RegionListAdapter;
import com.tongtong.purchaser.model.PurchaserModel;
import com.tongtong.purchaser.model.RegionModel;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.HttpTask.HttpTaskHandler;
import com.tongtong.purchaser.utils.RegionUtil;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.view.LoadingDialog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class ChoiceProvinceActivity extends BaseActivity implements
		OnClickListener, HttpTaskHandler, OnItemClickListener, TextWatcher {
	private LinearLayout backBn;
	private LoadingDialog loadingDialog;
	private LoadingDialog submitDialog;
	private List<RegionModel> provinces;
	private ListView list;
	private RegionListAdapter regionListAdapter;
	private EditText search;
	private final int FIND_REGION = 0;
	private final int SUBMIT_ADDRESS = 1;
	public static final int CHOICE_CITY = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choice_province);
		backBn = (LinearLayout) findViewById(R.id.back_bn);
		backBn.setOnClickListener(this);
		loadingDialog = new LoadingDialog(this);
		loadingDialog.setMessage(R.string.loading);
		submitDialog = new LoadingDialog(this);
		submitDialog.setMessage(R.string.submit_loading);
		TextView titleText = (TextView) findViewById(R.id.title_text);
		titleText.setText(R.string.choice_province);
		list = (ListView) findViewById(R.id.list);
		JsonObject dataJson = new JsonObject();
		dataJson.addProperty("token", UserUtil.getUserModel(this).getToken());
		list.setOnItemClickListener(this);
		search = (EditText) findViewById(R.id.search);
		search.addTextChangedListener(this);
		HttpTask findProvinceHttp = new HttpTask(FIND_REGION,this);
		findProvinceHttp.setTaskHandler(this);
		findProvinceHttp.execute(UrlUtil.FIND_REGION_URL, dataJson.toString());
		
	}
	
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case CHOICE_CITY:
			finish();
			break;

		default:
			break;
		}
	}



	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_bn:
			finish();
			break;

		default:
			break;
		}

	}

	@Override
	public void taskStart(int code) {
		switch (code) {
		case FIND_REGION:
			loadingDialog.show();
			break;
		case SUBMIT_ADDRESS:
			submitDialog.show();
			
			break;
		default:
			break;
		}

	}

	@Override
	public void taskSuccessful(String str, int code) {
		switch (code) {
		case FIND_REGION:
			JsonObject resultJson = new JsonParser().parse(str)
					.getAsJsonObject();
			int resultCode = resultJson.get("code").getAsInt();
			if (verification(resultCode)) {
				JsonArray regionArr = resultJson.get("data").getAsJsonArray();
				Gson gson = new Gson();
				List<RegionModel> regions = gson.fromJson(regionArr,
						new TypeToken<List<RegionModel>>() {
						}.getType());
				provinces = RegionUtil.getRegionStructure(regions);
				regionListAdapter = new RegionListAdapter(this, provinces);
				list.setAdapter(regionListAdapter);
			}
			loadingDialog.dismiss();
			break;
			
		case SUBMIT_ADDRESS:
			JsonObject resultJsonSubmit = new JsonParser().parse(str).getAsJsonObject();
			int resultCodeSubmit = resultJsonSubmit.get("code").getAsInt();
			if (verification(resultCodeSubmit)) {
				PurchaserModel purchaser = UserUtil.getUserModel(this);
				int address = resultJsonSubmit.get("data").getAsJsonObject().get("address").getAsInt();
				String addressStr = resultJsonSubmit.get("data").getAsJsonObject().get("addressStr").getAsString();
				purchaser.setAddress(address);
				purchaser.setAddressStr(addressStr);
				UserUtil.setUserModel(this, purchaser);
				finish();
			}
			submitDialog.dismiss();
			
			break;

		default:
			break;
		}
	}

	@Override
	public void taskFailed(int code) {
		switch (code) {
		case FIND_REGION:
			loadingDialog.dismiss();
			showToast(R.string.net_err);
			break;
		case SUBMIT_ADDRESS:
			submitDialog.dismiss();
			showToast(R.string.net_err);
			break;
		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		RegionModel region =  regionListAdapter.getItem(position);
		
		if(region.getChildrenRegions()!=null&&region.getChildrenRegions().size()>0){
			 Intent cityIntent = new Intent();
			 cityIntent.setClass(ChoiceProvinceActivity.this, ChoiceCityActivity.class);
			 cityIntent.putExtra("regions", (Serializable)region.getChildrenRegions());
			 cityIntent.putExtra("province", region.getName());
			 ChoiceProvinceActivity.this.startActivityForResult(cityIntent, 0);
		}
		else{
			JsonObject dataJson = new JsonObject();
			dataJson.addProperty("token", UserUtil.getUserModel(this).getToken());
			dataJson.addProperty("address", region.getId());
			dataJson.addProperty("addressStr", region.getName());
			HttpTask updateAddressHttp = new HttpTask(SUBMIT_ADDRESS,this);
			updateAddressHttp.setTaskHandler(this);
			updateAddressHttp.execute(UrlUtil.UPDATE_ADDRESS_URL, dataJson.toString());
		}
	}



	@Override
	public void afterTextChanged(Editable arg0) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void onTextChanged(CharSequence text, int arg1, int arg2, int arg3) {
		search(text.toString());
		
	}
	
	private void search(String text){
		if (text.equals("")) {
			regionListAdapter = new RegionListAdapter(this,provinces);
			list.setAdapter(regionListAdapter);
			
		}
		else{
			List<RegionModel> regions = new ArrayList<RegionModel>();
			for (int i = 0; i < provinces.size(); i++) {
				RegionModel region = provinces.get(i);
				if (region.getName().contains(text)) {
					regions.add(region);
				} 
			}
			regionListAdapter = new RegionListAdapter(this,regions);
			list.setAdapter(regionListAdapter);
		}
	}
}
