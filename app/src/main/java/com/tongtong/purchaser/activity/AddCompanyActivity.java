package com.tongtong.purchaser.activity;


import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.model.CompanyModel;
import com.tongtong.purchaser.model.PurchaserModel;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.HttpTask.HttpTaskHandler;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.view.LoadingDialog;



public class AddCompanyActivity extends BaseActivity implements OnClickListener, HttpTaskHandler {
	private LinearLayout backBn;
	private EditText companyName;
	private Button confirmBn;
	private LoadingDialog loadingDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_company);
		backBn = (LinearLayout) findViewById(R.id.back_bn);
		backBn.setOnClickListener(this);
		loadingDialog = new LoadingDialog(this);
		loadingDialog.setMessage(R.string.submit_loading);
		TextView titleText = (TextView) findViewById(R.id.title_text);
		titleText.setText(R.string.add_company_title);
		companyName = (EditText) findViewById(R.id.company_name);
		confirmBn = (Button) findViewById(R.id.confirm_bn);
		confirmBn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_bn:
			finish();
			break;
		case R.id.confirm_bn:
			confirm();
			break;
		default:
			break;
		}

	}

	private void confirm(){
		String company = companyName.getText().toString();
		if(company==null||company.equals("")){
			showToast(R.string.company_name_hint);
		}
		else{
			JsonObject dataJson = new JsonObject();
			dataJson.addProperty("token", UserUtil.getUserModel(this)
					.getToken());
			dataJson.addProperty("company", company);
			HttpTask submitHttp = new HttpTask(this);
			submitHttp.setTaskHandler(this);
			submitHttp.execute(UrlUtil.ADD_COMPANY, dataJson.toString());
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
			PurchaserModel purchaser = UserUtil.getUserModel(this);
			JsonObject companyJson = resultJson.get("data").getAsJsonObject();
			Gson gson = new Gson();
			CompanyModel company = gson.fromJson(companyJson,new TypeToken<CompanyModel>() {
			}.getType());
			purchaser.setCompany(company);
			UserUtil.setUserModel(this, purchaser);
			setResult(1);
			finish();
		}
		loadingDialog.dismiss();
		
	}

	@Override
	public void taskFailed(int code) {
		loadingDialog.dismiss();
		showToast(R.string.net_err);
	}
	
	
	
}
