package com.tongtong.purchaser.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.tongtong.purchaser.model.CompanyModel;
import com.tongtong.purchaser.model.PurchaserModel;
import com.tongtong.purchaser.service.PurchaserLocationService;
import com.tongtong.purchaser.utils.CodeUtil;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.HttpTask.HttpTaskHandler;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;


public class AppStartUpReceiver extends BroadcastReceiver implements HttpTaskHandler{
    private Context mContext;
	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;
		context.startService(new Intent(context, PurchaserLocationService.class));
		initData();
	}
	
	private void initData(){
		JsonObject dataJson = new JsonObject();
		dataJson.addProperty("token", UserUtil.getUserModel(mContext)
				.getToken());
		HttpTask initHttp = new HttpTask(mContext);
		initHttp.setTaskHandler(this);
		initHttp.execute(UrlUtil.INIT_DATA, dataJson.toString());
	}

	@Override
	public void taskStart(int code) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void taskSuccessful(String str, int code) {
		JsonObject resultJson = new JsonParser().parse(str).getAsJsonObject();
		int resultCode = resultJson.get("code").getAsInt();
		if(resultCode == CodeUtil.SUCCESS_CODE){
			JsonObject dataJson = resultJson.get("data").getAsJsonObject();
			Gson gson = new Gson();
			JsonObject companyJson = dataJson.get("company").getAsJsonObject();
			CompanyModel company = gson.fromJson(companyJson,
					new TypeToken<CompanyModel>() {
					}.getType());
			PurchaserModel purchaser = UserUtil.getUserModel(mContext);
			purchaser.setCompany(company);
			UserUtil.setUserModel(mContext, purchaser);
		}
		
	}

	@Override
	public void taskFailed(int code) {
		// TODO Auto-generated method stub
		
	}

}
