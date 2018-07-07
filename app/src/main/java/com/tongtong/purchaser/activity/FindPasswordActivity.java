package com.tongtong.purchaser.activity;




import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.utils.CodeUtil;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.HttpTask.HttpTaskHandler;
import com.tongtong.purchaser.utils.PhoneNumUtil;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.widget.StyleableToast;


public class FindPasswordActivity extends BaseActivity implements
		OnClickListener, TextWatcher, HttpTaskHandler {

	private Button submitBn;
	private EditText usernameText;
	private EditText codeText;
	private EditText passwordText;
	private EditText cofingpasswordText;
	private TextView getCodeText;
	private View backBn;
	private long timestamp;
	private boolean is_sending=false;
	private Handler handler=new Handler();
	private int COUNT=60;
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_find_password);
		TextView titleText = (TextView) findViewById(R.id.title_text);
		titleText.setText(R.string.find_password_text_title);
		backBn = findViewById(R.id.back_bn);
		backBn.setOnClickListener(this);
		submitBn = (Button) findViewById(R.id.submit_bn);
		submitBn.setOnClickListener(this);
		submitBn.setEnabled(false);
		getCodeText = (TextView) findViewById(R.id.get_code);
		getCodeText.setOnClickListener(this);
		usernameText = (EditText) findViewById(R.id.username);
		codeText = (EditText) findViewById(R.id.code);
		passwordText = (EditText) findViewById(R.id.password);
		cofingpasswordText = (EditText) findViewById(R.id.cofingpassword);
		usernameText.addTextChangedListener(this);
		codeText.addTextChangedListener(this);
		passwordText.addTextChangedListener(this);
		cofingpasswordText.addTextChangedListener(this);
	}


	private void getCode(){
		HttpTask task=new HttpTask(this);
		timestamp= System.currentTimeMillis()/1000;
		task.setTaskHandler(new HttpTask.HttpTaskHandler() {
			@Override
			public void taskStart(int code) {
				showLoading();
			}
			@Override
			public void taskSuccessful(String str, int code) {
				dismissLoading();
				JsonObject object=new JsonParser().parse(str).getAsJsonObject();
				if(object.get("code").getAsInt()== CodeUtil.SUCCESS_CODE){
					StyleableToast.info(FindPasswordActivity.this,"验证码已发送");
					countDown();
				}else{
					StyleableToast.error(FindPasswordActivity.this,object.get("info").getAsString());
				}
			}
			@Override
			public void taskFailed(int code) {
				dismissLoading();
			}
		});
		JsonObject object=new JsonObject();
		object.addProperty("timestamp",timestamp);
		object.addProperty("type","find_pass");
		object.addProperty("mobile",usernameText.getText().toString().trim());
		task.execute(UrlUtil.SEND_CODE,object.toString());
	}
	private void countDown(){
		is_sending=true;
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if(COUNT>0) {
					COUNT--;
					getCodeText.setText(COUNT + "秒后重发");
					handler.postDelayed(this, 1000);
				}else{
					handler.removeCallbacks(this);
					COUNT=60;
					getCodeText.setText("获取验证码");
					is_sending=false;
				}
			}
		},1000);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.get_code:
			if (TextUtils.isEmpty(usernameText.getText().toString().trim())) {
				showTips("请填写手机号");
				return;
			}
			if (is_sending) {
				return;
			}
			getCode();
			break;
		case R.id.submit_bn:

			submit();

			break;
		case R.id.back_bn:

			finish();

			break;
		default:
			break;
		}
	}

	private JsonObject submitData;

	private void submit() {
		String username = usernameText.getText().toString();
		String password = passwordText.getText().toString();
		String cofingpassword = cofingpasswordText.getText().toString();
		String code = codeText.getText().toString();
		if (username == null || username.equals("")) {
			showToast(R.string.username_null);
			return;
		}
		if (!PhoneNumUtil.judgePhoneNums(username)) {
			showToast(R.string.phone_err);
			return;
		}
		if (code == null || code.equals("")) {
			showToast(R.string.code_null);
			return;
		}
		if (password == null || password.equals("")) {
			showToast(R.string.password_null);
			return;
		}
		if (password.length() < 6) {
			showToast(R.string.password_lenght_err);
			return;
		}
		if (cofingpassword == null || cofingpassword.equals("")) {
			showToast(R.string.confingpassword_null);
			return;
		}
		if (!password.equals(cofingpassword)) {
			showToast(R.string.confingpassword_err);
			return;
		}
		submitData = new JsonObject();
		submitData.addProperty("username", username);
		submitData.addProperty("password", password);
		submitData.addProperty("timestamp",timestamp);
		submitData.addProperty("code",codeText.getText().toString());
		submitRequest();
	}

	private void submitRequest() {
		HttpTask submitHttp = new HttpTask(this);
		submitHttp.setTaskHandler(this);
		submitHttp.execute(UrlUtil.FIND_PASSWORD_URL, submitData.toString());
	}

	@Override
	public void afterTextChanged(Editable arg0) {

	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {

	}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		if ((!"".equals(usernameText.getEditableText().toString()))
				&& (!"".equals(codeText.getEditableText().toString()))
				&& (!"".equals(passwordText.getEditableText().toString()))
				&& (!"".equals(cofingpasswordText.getEditableText().toString()))) {
			submitBn.setEnabled(true);
		} else {
			submitBn.setEnabled(false);
		}

	}






	@Override
	public void taskStart(int code) {
		// TODO Auto-generated method stub
		showLoading();
	}

	@Override
	public void taskSuccessful(String str, int code) {
		dismissLoading();
		JsonObject resultJson = new JsonParser().parse(str).getAsJsonObject();
		int resultCode = resultJson.get("code").getAsInt();
		if (resultCode == CodeUtil.DATA_ERR_CODE) {
			showTips(resultJson.get("info").getAsString());
		} else if (resultCode == CodeUtil.SUCCESS_CODE) {
			showOkToast(R.string.find_password_success);
			finish();
		}


	}

	@Override
	public void taskFailed(int code) {
		dismissLoading();
		showToast(R.string.net_err);

	}
}
