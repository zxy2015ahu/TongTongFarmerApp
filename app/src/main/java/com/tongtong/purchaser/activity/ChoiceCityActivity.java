package com.tongtong.purchaser.activity;

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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.adapter.RegionListAdapter;
import com.tongtong.purchaser.model.PurchaserModel;
import com.tongtong.purchaser.model.RegionModel;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.HttpTask.HttpTaskHandler;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.view.LoadingDialog;

import java.util.ArrayList;
import java.util.List;



public class ChoiceCityActivity extends BaseActivity implements OnClickListener, OnItemClickListener, HttpTaskHandler, TextWatcher {
	private LinearLayout backBn;
	private ListView list;
	private List<RegionModel> citys;
	private String province;
	private LoadingDialog submitDialog;
	private RegionListAdapter regionListAdapter;
	private EditText search;
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choice_city);
		citys = (List<RegionModel>) getIntent().getSerializableExtra("regions");
		province = getIntent().getStringExtra("province");
		backBn = (LinearLayout) findViewById(R.id.back_bn);
		backBn.setOnClickListener(this);
		submitDialog = new LoadingDialog(this);
		submitDialog.setMessage(R.string.submit_loading);
		TextView titleText = (TextView) findViewById(R.id.title_text);
		titleText.setText(R.string.choice_city);
		list = (ListView) findViewById(R.id.list);
		regionListAdapter = new RegionListAdapter(this,citys);
		list.setAdapter(regionListAdapter);
		list.setOnItemClickListener(this);
		search = (EditText) findViewById(R.id.search);
		search.addTextChangedListener(this);
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
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		RegionModel region =  regionListAdapter.getItem(position);
		JsonObject dataJson = new JsonObject();
		dataJson.addProperty("token", UserUtil.getUserModel(this).getToken());
		dataJson.addProperty("address", region.getId());
		dataJson.addProperty("addressStr", province +"  "+ region.getName());
		HttpTask updateAddressHttp = new HttpTask(this);
		updateAddressHttp.setTaskHandler(this);
		updateAddressHttp.execute(UrlUtil.UPDATE_ADDRESS_URL, dataJson.toString());
		
	}

	@Override
	public void taskStart(int code) {
		submitDialog.show();
		
	}

	@Override
	public void taskSuccessful(String str, int code) {
		JsonObject resultJsonSubmit = new JsonParser().parse(str).getAsJsonObject();
		int resultCodeSubmit = resultJsonSubmit.get("code").getAsInt();
		if (verification(resultCodeSubmit)) {
			PurchaserModel purchaser = UserUtil.getUserModel(this);
			int address = resultJsonSubmit.get("data").getAsJsonObject().get("address").getAsInt();
			String addressStr = resultJsonSubmit.get("data").getAsJsonObject().get("addressStr").getAsString();
			purchaser.setAddress(address);
			purchaser.setAddressStr(addressStr);
			UserUtil.setUserModel(this, purchaser);
			setResult(ChoiceProvinceActivity.CHOICE_CITY);
			finish();
		}
		submitDialog.dismiss();
		
	}

	@Override
	public void taskFailed(int code) {
		submitDialog.dismiss();
		showToast(R.string.net_err);
		
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
			regionListAdapter = new RegionListAdapter(this,citys);
			list.setAdapter(regionListAdapter);
			
		}
		else{
			List<RegionModel> regions = new ArrayList<RegionModel>();
			for (int i = 0; i < citys.size(); i++) {
				RegionModel region = citys.get(i);
				if (region.getName().contains(text)) {
					regions.add(region);
				} 
			}
			regionListAdapter = new RegionListAdapter(this,regions);
			list.setAdapter(regionListAdapter);
		}
	}
}
