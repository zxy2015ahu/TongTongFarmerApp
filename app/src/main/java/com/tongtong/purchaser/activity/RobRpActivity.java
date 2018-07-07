package com.tongtong.purchaser.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.android.tu.loadingdialog.LoadingDailog;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.application.MyApplication;
import com.tongtong.purchaser.frament.HongBaoDialogFragment;
import com.tongtong.purchaser.utils.CodeUtil;
import com.tongtong.purchaser.utils.Constant;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.widget.AlertDialog;
import com.tongtong.purchaser.widget.StyleableToast;
import com.yunzhanghu.redpacketui.widget.RPTitleBar;

/**
 * Created by zxy on 2018/3/9.
 */

public class RobRpActivity extends BaseActivity implements BaiduMap.OnMarkerClickListener,
        BDLocationListener{
    private MapView mapView;
    private BaiduMap bMap;
    private double lat,lng;
    private String address;
    private FrameLayout container;
    private static RobRpActivity instance;
    private Marker curMarker;
    private LoadingDailog loading;
    private MyReciever reciever;
    private LocationClient client;
    private boolean is_first=true;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rob_rp_layout);
        instance=this;
        lat=getIntent().getDoubleExtra("lat",0);
        lng=getIntent().getDoubleExtra("lng",0);
        address=getIntent().getStringExtra("address");
        client= MyApplication.getLocationClient();
        client.registerLocationListener(this);
        client.start();
        RPTitleBar titleBar=(RPTitleBar) findViewById(R.id.bc_title_bar);
        titleBar.setSubTitle("只显示附近可抢红包");
        titleBar.setSubTitleVisibility(View.VISIBLE);
        titleBar.setLeftLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        titleBar.setRightText("我要发");
        titleBar.setRightTextLayoutVisibility(View.VISIBLE);
        titleBar.setRightTextLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                                    if(lat==0&&lng==0){
                                        return;
                                    }
                					Intent hongbao=new Intent();
                					hongbao.setClass(RobRpActivity.this,SendHongBaoActivity.class);
                					hongbao.putExtra("lat",lat);
                					hongbao.putExtra("lng",lng);
                                    hongbao.putExtra("address",address);
                					startActivity(hongbao);
            }
        });
        container=(FrameLayout) findViewById(R.id.bc_fragment_container);
        BaiduMapOptions opt=new BaiduMapOptions();
        MapStatus mapStatus=new MapStatus.Builder()
                .zoom(15).target(new LatLng(lat,lng)).build();
        opt.mapStatus(mapStatus);
        mapView=new MapView(this,opt);
        container.addView(mapView,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        bMap=mapView.getMap();
        bMap.setOnMarkerClickListener(this);

        reciever=new MyReciever();
        IntentFilter filter=new IntentFilter();
        filter.addAction(Constant.MSG_RP_UPDATE);
        registerReceiver(reciever,filter);
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        if(bdLocation!=null){
            client.stop();
            lat=bdLocation.getLatitude();
            lng=bdLocation.getLongitude();
            address=bdLocation.getAddress().street+bdLocation.getAddress().streetNumber;
            if(is_first){
                is_first=false;
                MarkerOptions options=new MarkerOptions();
                options.zIndex(50).icon(BitmapDescriptorFactory.fromResource(R.drawable.location_marker))
                        .anchor(0.5f,0.5f).position(new LatLng(lat,lng)).animateType(MarkerOptions.MarkerAnimateType.grow);
                bMap.addOverlay(options);
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        gethongbaobydistance();
                    }
                });
            }

        }
    }


    private class MyReciever extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(Constant.MSG_RP_UPDATE.equals(intent.getAction())){
                gethongbaobydistance();
            }
        }
    }
    @Override
    public boolean onMarkerClick(Marker marker) {
        curMarker=marker;
        Bundle extra=marker.getExtraInfo();
        if(extra!=null){
            openrpbyfarmer(extra.getInt("id"),extra.getString("comment"));
        }
        return true;
    }

    public  void updateMarker(){
        if(curMarker!=null){
            curMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ling_guo));
        }
    }
    private void openrpbyfarmer(final int id,final String comment){
        HttpTask task=new HttpTask(this);
        task.setTaskHandler(new HttpTask.HttpTaskHandler() {
            @Override
            public void taskStart(int code) {
                showLoading();
            }
            @Override
            public void taskSuccessful(String str, int code) {
                dismissLoading();
                JsonObject selectResultJson = new JsonParser().parse(str)
                        .getAsJsonObject();
                int selectResultCode = selectResultJson.get("code").getAsInt();
                if(selectResultCode== CodeUtil.SUCCESS_CODE){
                    HongBaoDialogFragment hongbao=new HongBaoDialogFragment();
                    Bundle args=new Bundle();
                    args.putString("name",selectResultJson.get("name").getAsString());
                    args.putString("phone",selectResultJson.get("phone").getAsString());
                    args.putString("headUrl",selectResultJson.get("headUrl").getAsString());
                    args.putInt("id",id);
                    args.putDouble("lat",lat);
                    args.putDouble("lng",lng);
                    args.putString("comment",comment);
                    hongbao.setArguments(args);
                    hongbao.show(getSupportFragmentManager(),"hongbao");
                }else if(selectResultCode==103){
                    StyleableToast.error(RobRpActivity.this,"红包打开失败");
                }else if(selectResultCode==104){
                    //已抢完
                    StyleableToast.info(RobRpActivity.this,"来晚一步，红包已经被抢完");
                    Intent intent=new Intent();
                    intent.setClass(RobRpActivity.this, HongBaoDetailsActivity.class);
                    intent.putExtra("result",str);
                    startActivity(intent);
                }else if(selectResultCode==106){
                    //已抢过
                        Intent intent=new Intent();
                        intent.setClass(RobRpActivity.this, HongBaoDetailsActivity.class);
                        intent.putExtra("result",str);
                        startActivity(intent);
                }
            }
            @Override
            public void taskFailed(int code) {
                loading.dismiss();
            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("token",UserUtil.getUserModel(this).getToken());
        object.addProperty("purchaser_id",UserUtil.getUserModel(this).getId());
        object.addProperty("id",id);
        object.addProperty("lat",lat);
        object.addProperty("lng",lng);
        task.execute(UrlUtil.ROB_RP,object.toString());
    }
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    public static RobRpActivity getInstance(){
        return instance;
    }
    private void gethongbaobydistance(){
        HttpTask task=new HttpTask(this);
        task.setTaskHandler(new HttpTask.HttpTaskHandler() {
            @Override
            public void taskStart(int code) {

            }
            @Override
            public void taskSuccessful(String str, int code) {
                JsonObject selectResultJson = new JsonParser().parse(str)
                        .getAsJsonObject();
                int selectResultCode = selectResultJson.get("code").getAsInt();
                if(selectResultCode== CodeUtil.SUCCESS_CODE){
                    reset();
                    JsonArray hongbao=selectResultJson.get("hongbao").getAsJsonArray();
                    LatLngBounds.Builder bounds=new LatLngBounds.Builder();
                    for(int i=0;i<hongbao.size();i++){
                        JsonObject hb=hongbao.get(i).getAsJsonObject();
                        LatLng ll=new LatLng(hb.get("lat").getAsDouble(),hb.get("lng").getAsDouble());
                        MarkerOptions marker=new MarkerOptions();
                        Bundle bundle=new Bundle();
                        bundle.putInt("id",hb.get("id").getAsInt());
                        bundle.putString("comment",hb.get("comment").getAsString());
                        bundle.putInt("type",hb.get("type").getAsInt());
                        bundle.putInt("typeid",hb.get("typeid").getAsInt());
                        marker.extraInfo(bundle);
                        marker.position(ll)
                                .icon(BitmapDescriptorFactory.fromResource(hb.get("is_qiang").getAsInt()==0?R.drawable.ling_hongbao:R.drawable.ling_guo))
                                .anchor(0.5f,0.5f).animateType(MarkerOptions.MarkerAnimateType.grow).zIndex(100);
                        bMap.addOverlay(marker);
                        bounds.include(ll);
                    }
                    bMap.animateMapStatus(MapStatusUpdateFactory.newLatLngBounds(bounds.build()));
                }else if(selectResultCode==105){

                    AlertDialog dialog=new AlertDialog(RobRpActivity.this).builder();
                    dialog.setTitle("温馨提示");
                    dialog.setMsg("附近没有红包！");
                    dialog.setPositiveButton("确定", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    });
                    dialog.show();
                }
            }
            @Override
            public void taskFailed(int code) {

            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("token", UserUtil.getUserModel(this).getToken());
        object.addProperty("lat",lat);
        object.addProperty("lng",lng);
        object.addProperty("purchaser_id",UserUtil.getUserModel(this).getId());
        task.execute(UrlUtil.GET_RP_LIST,object.toString());
    }

    private void reset(){
        bMap.clear();
        MarkerOptions options=new MarkerOptions();
        options.zIndex(50).icon(BitmapDescriptorFactory.fromResource(R.drawable.location_marker))
                .anchor(0.5f,0.5f).position(new LatLng(lat,lng)).animateType(MarkerOptions.MarkerAnimateType.grow);
        bMap.addOverlay(options);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        unregisterReceiver(reciever);
        client.unRegisterLocationListener(this);
    }
}
