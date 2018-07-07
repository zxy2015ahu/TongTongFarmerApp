package com.tongtong.purchaser.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tongtong.purchaser.utils.Constant;


public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {
	private IWXAPI api;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		api = WXAPIFactory.createWXAPI(this, Constant.APP_ID, false);
		api.handleIntent(getIntent(), this);
	}
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		api.handleIntent(intent, this);
	}
	@Override
	public void onReq(BaseReq baseReq) {

	}

	@Override
	public void onResp(BaseResp resp) {
		// TODO Auto-generated method stub
		if(resp.getType()== ConstantsAPI.COMMAND_PAY_BY_WX){
			Intent broad=new Intent();
			broad.setAction(Constant.MSG_PAY);
			broad.putExtra("code",resp.errCode);
			sendBroadcast(broad);
			finish();
		}
	}
}
