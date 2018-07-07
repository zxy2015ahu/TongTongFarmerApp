package com.tongtong.purchaser.activity;



import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.model.PurchaserModel;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.HttpTask.HttpTaskHandler;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;

public class UpdateNameActivity extends BaseActivity implements OnClickListener, HttpTaskHandler {

	private LinearLayout backBn;
	private Button rightBn;
	private EditText editText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_name);
		backBn = (LinearLayout) findViewById(R.id.back_bn);
		backBn.setOnClickListener(this);
		rightBn = (Button) findViewById(R.id.right_bn);
		rightBn.setText(R.string.save);
		editText = (EditText) findViewById(R.id.edit_text);
		rightBn.setOnClickListener(this);
		TextView titleText = (TextView) findViewById(R.id.title_text);
		titleText.setText(R.string.update_name);
		editText.setText(UserUtil.getUserModel(this).getName());
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_bn:
			finish();
			break;
		case R.id.right_bn:
			save();
			break;

		default:
			break;
		}

	}

	private void save() {
		String name = editText.getText().toString();
		if(name==null||name.equals("")){
			showToast(R.string.write_name);
			return;
		}
		JsonObject dataJson = new JsonObject();
		dataJson.addProperty("token", UserUtil.getUserModel(this).getToken());
		dataJson.addProperty("name", name);
		HttpTask saveHttp = new HttpTask(this);
		saveHttp.setTaskHandler(this);
		saveHttp.execute(UrlUtil.UPDATE_NAME_URL, dataJson.toString());
	}

	@Override
	public void taskStart(int code) {
		showLoading("保存中");
		
	}

	@Override
	public void taskSuccessful(String str, int code) {
		JsonObject resultJson = new JsonParser().parse(str).getAsJsonObject();
		int resultCode = resultJson.get("code").getAsInt();
		if (verification(resultCode)) {
			PurchaserModel purchaser = UserUtil.getUserModel(this);
			String name = resultJson.get("data").getAsJsonObject().get("name").getAsString();
			purchaser.setName(name);
			UserUtil.setUserModel(this, purchaser);
			finish();
		}
		dismissLoading();
		
	}

	@Override
	public void taskFailed(int code) {
		dismissLoading();
		showToast(R.string.net_err);
		
	}
	
	


}
