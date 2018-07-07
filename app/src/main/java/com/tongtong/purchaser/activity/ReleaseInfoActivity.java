package com.tongtong.purchaser.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Selection;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.model.ProduceModel;
import com.tongtong.purchaser.model.PurchaserReleaseInformationModel;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.widget.StyleableToast;

import java.io.Serializable;

public class ReleaseInfoActivity extends BaseActivity implements OnClickListener {
	private ProduceModel produce;

	private LinearLayout backBn;
	private EditText remarksText;
	private Button confirmBn;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_release_info);
		
	    produce = (ProduceModel) getIntent().getSerializableExtra("produce");
	    TextView titleText = (TextView) findViewById(R.id.title_text);
		if(getIntent().getStringExtra("title")!=null){
			titleText.setText(getIntent().getStringExtra("title"));
		}else{
			titleText.setText(produce.getName());
		}

	    backBn = (LinearLayout) findViewById(R.id.back_bn);
		backBn.setOnClickListener(this);
		confirmBn = (Button) findViewById(R.id.confirm_bn);
		confirmBn.setOnClickListener(this);

	    remarksText = (EditText) findViewById(R.id.remarks);
        if(getIntent().getStringExtra("title")!=null){
            remarksText.setText(getIntent().getStringExtra("remarks"));
            Selection.setSelection(remarksText.getText(),remarksText.getText().length());
        }
	}
	private void addRelease(){
		HttpTask task=new HttpTask(this);
		task.setTaskHandler(new HttpTask.HttpTaskHandler() {
			@Override
			public void taskStart(int code) {
				showLoading();
			}
			@Override
			public void taskSuccessful(String str, int code) {
				dismissLoading();
				JsonObject resultJson = new JsonParser().parse(str).getAsJsonObject();
				int resultCode = resultJson.get("code").getAsInt();
				if(verification(resultCode)) {
					PurchaserReleaseInformationModel releaseInfo = new PurchaserReleaseInformationModel();
					releaseInfo.setProduce(produce);
					releaseInfo.setRemarks(remarksText.getText()
							.toString());
					Intent intent = new Intent();
					intent.putExtra("releaseInfo", (Serializable) releaseInfo);
					setResult(1, intent);
					finish();
				}
			}

			@Override
			public void taskFailed(int code) {
				dismissLoading();
				StyleableToast.error(ReleaseInfoActivity.this,"发布失败，请稍后再试");
			}
		});
		JsonObject object=new JsonObject();
		object.addProperty("purchaser_id", UserUtil.getUserModel(this).getId());
		object.addProperty("produce_id",produce.getId());
		object.addProperty("remarks",remarksText.getText()
				.toString());
		object.addProperty("token",UserUtil.getUserModel(this).getToken());
		task.execute(UrlUtil.RELEASE,object.toString());
	}
	private void updateRelease(){
		HttpTask task=new HttpTask(this);
		task.setTaskHandler(new HttpTask.HttpTaskHandler() {
			@Override
			public void taskStart(int code) {
				showLoading();
			}
			@Override
			public void taskSuccessful(String str, int code) {
				dismissLoading();
				JsonObject resultJson = new JsonParser().parse(str).getAsJsonObject();
				int resultCode = resultJson.get("code").getAsInt();
				if(verification(resultCode)) {
					PurchaserReleaseInformationModel releaseInfo = new PurchaserReleaseInformationModel();
					releaseInfo.setProduce(produce);
					releaseInfo.setRemarks(remarksText.getText()
							.toString());
					Intent intent = new Intent();
					intent.putExtra("releaseInfo", (Serializable) releaseInfo);
					intent.putExtra("position",getIntent().getIntExtra("position",-1));
					setResult(1, intent);
					finish();
				}
			}

			@Override
			public void taskFailed(int code) {
				dismissLoading();
				StyleableToast.error(ReleaseInfoActivity.this,"修改失败，请稍后再试");
			}
		});
		JsonObject object=new JsonObject();
		object.addProperty("id",getIntent().getIntExtra("id",0));
		object.addProperty("remarks",remarksText.getText()
				.toString());
		object.addProperty("token",UserUtil.getUserModel(this).getToken());
		task.execute(UrlUtil.UPDATE_RELEASE,object.toString());
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.confirm_bn:
			if(getIntent().getStringExtra("title")!=null){
				updateRelease();
			}else{
				addRelease();
			}
			break;
		case R.id.back_bn:
			finish();
			break;
		}
	}
}
