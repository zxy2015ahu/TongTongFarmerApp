package com.tongtong.purchaser.service;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.google.gson.JsonObject;
import com.spatial4j.core.io.GeohashUtils;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;

public class PurchaserLocationService extends Service implements
		BDLocationListener {

	public LocationClient mLocationClient = null;

	@Override
	public IBinder onBind(Intent arg0) {

		return null;
	}

	@Override
	public void onCreate() {
		init();
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		mLocationClient.stop();
		super.onDestroy();
	}

	private void init() {
		mLocationClient = new LocationClient(this);
		mLocationClient.registerLocationListener(this);
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
		option.setProdName("收购员后台坐标收集");
		option.setScanSpan(3000);
		mLocationClient.setLocOption(option);
		mLocationClient.start();
	}

	@Override
	public void onReceiveLocation(BDLocation bdLocation) {
		if (bdLocation != null) {

				String geoHash = GeohashUtils.encodeLatLon(bdLocation.getLatitude(),
						bdLocation.getLongitude(), 12);
				JsonObject dataJson = new JsonObject();
				dataJson.addProperty("token", UserUtil.getUserModel(this)
						.getToken());
				dataJson.addProperty("geoHash", geoHash);
				dataJson.addProperty("longitude", bdLocation.getLongitude());
				dataJson.addProperty("latitude", bdLocation.getLatitude());
				HttpTask httpTask = new HttpTask(PurchaserLocationService.this);
				httpTask.execute(UrlUtil.UPDATE_LOCATION,dataJson.toString());

		}
	}
}
