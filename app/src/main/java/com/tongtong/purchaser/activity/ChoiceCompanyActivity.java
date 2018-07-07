package com.tongtong.purchaser.activity;

import android.content.Intent;
import android.os.Bundle;
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
import com.tongtong.purchaser.adapter.CompanyListAdapter;
import com.tongtong.purchaser.model.CompanyModel;
import com.tongtong.purchaser.model.PurchaserModel;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.HttpTask.HttpTaskHandler;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.view.LoadingDialog;

import java.util.List;


public class ChoiceCompanyActivity extends BaseActivity implements
		OnClickListener, HttpTaskHandler {

	private LinearLayout backBn;
	private EditText search;
	private TextView searchBn;
	private ListView list;
	private LoadingDialog loadingDialog;
	private LoadingDialog submitDialog;
	private CompanyListAdapter companyListAdapter;
	private View rightBn;
	private final static int LOADIN = 1;
	private final static int SUBMIT = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choice_company);
		loadingDialog = new LoadingDialog(this);
		loadingDialog.setMessage(R.string.loading);
		submitDialog = new LoadingDialog(this);
		submitDialog.setMessage(R.string.submit_loading);
		search = (EditText) findViewById(R.id.search);
		backBn = (LinearLayout) findViewById(R.id.back_bn);
		backBn.setOnClickListener(this);
		rightBn = findViewById(R.id.right_bn);
		rightBn.setOnClickListener(this);
		TextView titleText = (TextView) findViewById(R.id.title_text);
		titleText.setText(R.string.choice_company_title);
		searchBn = (TextView) findViewById(R.id.search_bn);
		searchBn.setOnClickListener(this);
		list = (ListView) findViewById(R.id.list);
		
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int index,
					long arg3) {
				 
				JsonObject dataJson = new JsonObject();
				dataJson.addProperty("token", UserUtil.getUserModel(ChoiceCompanyActivity.this)
						.getToken());
				dataJson.addProperty("companyId", companyListAdapter.getItem(index).getId());
				HttpTask submitHttp = new HttpTask(SUBMIT,ChoiceCompanyActivity.this);
				submitHttp.setTaskHandler(ChoiceCompanyActivity.this);
				submitHttp.execute(UrlUtil.CHOICE_COMPANY, dataJson.toString());
			}
		});
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
			HttpTask searvhHttp = new HttpTask(LOADIN,ChoiceCompanyActivity.this);
			searvhHttp.setTaskHandler(this);
			searvhHttp.execute(UrlUtil.SEARCH_COMPANY, dataJson.toString());
			break;
		case R.id.right_bn:
			Intent intent=new Intent();
			intent.setClass(ChoiceCompanyActivity.this, AddCompanyActivity.class);
			startActivityForResult(intent, 0);
			break;
		default:
			break;
		}

	}

	@Override
	public void taskStart(int code) {
		switch (code) {
		case LOADIN:
			loadingDialog.show();
			break;
        case SUBMIT:
        	submitDialog.show();
        	break;
		default:
			break;
		}
		
	}

	@Override
	public void taskSuccessful(String str, int code) {
		switch (code) {
		case LOADIN:
			JsonObject resultJson = new JsonParser().parse(str).getAsJsonObject();
			int resultCode = resultJson.get("code").getAsInt();
			if (verification(resultCode)) {
				JsonArray companyArr = resultJson.get("data").getAsJsonArray();
				Gson gson = new Gson();
				List<CompanyModel> companys = gson.fromJson(companyArr,
						new TypeToken<List<CompanyModel>>() {
						}.getType());
				companyListAdapter = new CompanyListAdapter(ChoiceCompanyActivity.this, companys);
				list.setAdapter(companyListAdapter);
			}
			loadingDialog.dismiss();
			break;
		case SUBMIT:
			JsonObject submitJson = new JsonParser().parse(str).getAsJsonObject();
			int submitCode = submitJson.get("code").getAsInt();
			if (verification(submitCode)) {
				JsonObject companyJson = submitJson.get("data").getAsJsonObject();
				Gson gson = new Gson();
				
				CompanyModel company = gson.fromJson(companyJson,
						new TypeToken<CompanyModel>() {
						}.getType());
				PurchaserModel purchaser = UserUtil.getUserModel(this);
				purchaser.setCompany(company);
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
		case LOADIN:
			loadingDialog.dismiss();
			break;
		case SUBMIT:
			submitDialog.dismiss();
        	break;
		default:
			break;
		}
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case 1:
			finish();
			break;

		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	

}
