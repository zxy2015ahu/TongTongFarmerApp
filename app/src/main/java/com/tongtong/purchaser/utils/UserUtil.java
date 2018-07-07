package com.tongtong.purchaser.utils;



import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.alibaba.mobileim.IYWLoginService;
import com.alibaba.mobileim.YWAPI;
import com.alibaba.mobileim.YWIMKit;
import com.alibaba.mobileim.YWLoginParam;
import com.alibaba.mobileim.channel.event.IWxCallback;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.application.MyApplication;
import com.tongtong.purchaser.model.CompanyModel;
import com.tongtong.purchaser.model.PurchaserModel;


public class UserUtil {
	private static YWIMKit ywimKit;
	public static PurchaserModel getUserModel(Context mContext) {
		SharedPreferences sp = mContext.getSharedPreferences("user",
				Context.MODE_PRIVATE);
		String token = sp.getString("token", "");
		String phone = sp.getString("phone", "");
		String name = sp.getString("name", "");
		String headUrl = sp.getString("headUrl", "");
		String addressStr = sp.getString("addressStr", "");
		int address = sp.getInt("address", -1);
		int id = sp.getInt("id", -1);
		int companyId = sp.getInt("companyId", 1);
		String companyName = sp.getString("companyName", mContext.getResources().getString(R.string.hawker));
		
		PurchaserModel user = new PurchaserModel();
		user.setPhone(phone);
		user.setToken(token);
		user.setName(name);
		user.setHeadUrl(headUrl);
		user.setAddress(address);
		user.setAddressStr(addressStr);
		user.setId(id);
		CompanyModel company = new CompanyModel();
		company.setCompanyName(companyName);
		company.setId(companyId);
		user.setCompany(company);
		if (token == null || token.equals(""))
			return null;
		else
			return user;
	}
	public static void imLogin(final Context mContext,IWxCallback callback){
		SharedPreferences sp = mContext.getSharedPreferences("user",
				Context.MODE_PRIVATE);
		String phone = sp.getString("phone", "");
		if(TextUtils.isEmpty(phone)){
			return;
		}
		MyApplication.instance.startDaemonService();
		MyApplication.instance.initPush();
		IYWLoginService loginService = getIMKitInstance(mContext).getLoginService();
		YWLoginParam loginParam = YWLoginParam.createLoginParam(phone, "123456");
		loginService.login(loginParam,callback);
	}
	public static void setUserModel(Context mContext,PurchaserModel data){
		SharedPreferences sp = mContext.getSharedPreferences("user", Context.MODE_PRIVATE);
		Editor editor = sp.edit();
	
		editor.putString("token", data.getToken());
		editor.putString("phone", data.getPhone());
		editor.putString("name", data.getName());
		editor.putString("headUrl", data.getHeadUrl());
		editor.putString("addressStr", data.getAddressStr());
		editor.putInt("address", data.getAddress());
		editor.putInt("id", data.getId());
		
		if(data.getCompany()!=null){
			editor.putString("companyName", data.getCompany().getCompanyName());
			editor.putInt("companyId", data.getCompany().getId());
		}
		else{
			editor.putString("companyName", mContext.getResources().getString(R.string.hawker));
			editor.putInt("companyId", 1);
		}
		editor.commit();
		
		
	}
	
	public static void clearUserModel(Context mContext){
		SharedPreferences sp = mContext.getSharedPreferences("user", Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.clear();
		editor.commit();
	}
	public static YWIMKit getIMKitInstance(Context mContext){
		if(ywimKit==null){
			SharedPreferences sp = mContext.getSharedPreferences("user",
					Context.MODE_PRIVATE);
			String phone = sp.getString("phone", "");
			ywimKit=YWAPI.getIMKitInstance(phone, Constant.APP_KEY);
		}
		return ywimKit;
	}
}
