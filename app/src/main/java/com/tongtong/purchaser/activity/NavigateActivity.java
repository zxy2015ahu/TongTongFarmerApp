package com.tongtong.purchaser.activity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.share.OnGetShareUrlResultListener;
import com.baidu.mapapi.search.share.RouteShareURLOption;
import com.baidu.mapapi.search.share.ShareUrlResult;
import com.baidu.mapapi.search.share.ShareUrlSearch;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.application.MyApplication;
import com.tongtong.purchaser.utils.GPSUtil;
import com.tongtong.purchaser.utils.PackageManagerUtil;

import static com.tongtong.purchaser.R.drawable.position;

/**
 * Created by Administrator on 2018-01-29.
 */

public class NavigateActivity extends BaseActivity implements View.OnClickListener,
        BDLocationListener,OnGetShareUrlResultListener {
    private double lat,lng;
    private MapView map;
    private String storeName;
    private BaiduMap aMap;
    private LocationClient client;
    private BDLocation location;
    private ShareUrlSearch urlSearch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_layout);
        //sp=getSharedPreferences("location", Context.MODE_PRIVATE);
        if(!TextUtils.isEmpty(getIntent().getStringExtra("title"))){
            ((TextView)findViewById(R.id.title)).setText(getIntent().getStringExtra("title"));
        }else{
            findViewById(R.id.title).setVisibility(View.GONE);
        }
        ((TextView)findViewById(R.id.address)).setText(getIntent().getStringExtra("address"));
        findViewById(R.id.nav).setOnClickListener(this);
        ((TextView)findViewById(R.id.title_text)).setText("位置信息");
        findViewById(R.id.back_bn).setOnClickListener(this);
        findViewById(R.id.location).setOnClickListener(this);
        lat=getIntent().getDoubleExtra("lat",0);
        lng=getIntent().getDoubleExtra("lng",0);
        storeName=(TextUtils.isEmpty(getIntent().getStringExtra("title"))?"":getIntent().getStringExtra("title"))+getIntent().getStringExtra("address");
        map=(MapView) findViewById(R.id.mapview);
        client= MyApplication.getLocationClient();
        client.registerLocationListener(this);
        aMap=map.getMap();
        MyLocationConfiguration configuration=new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL,
                false, BitmapDescriptorFactory.fromResource(R.drawable.location_marker));
        aMap.setMyLocationEnabled(true);
        aMap.setMyLocationConfiguration(configuration);
        aMap.addOverlay(new MarkerOptions()
                .anchor(0.5f, 1f)
                .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), position)))
                .position(new LatLng(lat, lng)));
        urlSearch=ShareUrlSearch.newInstance();
        urlSearch.setOnGetShareUrlResultListener(this);
        aMap.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(new LatLng(lat,lng), 15));
        client.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        aMap.setMyLocationEnabled(false);
        client.unRegisterLocationListener(this);
        map.onDestroy();
        urlSearch.destroy();
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.nav){
            if(PackageManagerUtil.haveGaodeMap()){
                openGaodeMapToGuide();
            } else if(PackageManagerUtil.haveBaiduMap()){
                openBaiduMapToGuide();
            }else {
                if(location!=null){
                    urlSearch.requestRouteShareUrl(new RouteShareURLOption().routMode(RouteShareURLOption.RouteShareMode.CAR_ROUTE_SHARE_MODE)
                            .from(PlanNode.withLocation(new LatLng(location.getLatitude(),location.getLongitude()))).to(PlanNode.withLocation(new LatLng(lat,lng))));
                }
            }
        }else if(view.getId()== R.id.back_bn){
            onBackPressed();
        }else if(view.getId()==R.id.location){
            if(location==null){
                return;
            }
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();//存放所有点的经纬度
            boundsBuilder.include(new LatLng(lat,lng));
            boundsBuilder.include(new LatLng(location.getLatitude(), location.getLongitude()));
            aMap.animateMapStatus(MapStatusUpdateFactory.newLatLngBounds(boundsBuilder.build()));//第二个参数为四周留空宽度
        }
    }
    private void openBaiduMapToGuide() {
        Intent intent = new Intent();
        double[] location = GPSUtil.gcj02_To_Bd09(lat , lng);
        String url = "baidumap://map/direction?" +
                "destination=name:"+storeName+"|latlng:"+location[0] + "," + location[1]+
                "&mode=driving&sy=3&index=0&target=1";
        Uri uri = Uri.parse(url);
        //将功能Scheme以URI的方式传入data
        intent.setData(uri);
        //启动该页面即可
        startActivity(intent);
    }

    private void openGaodeMapToGuide() {
        if(location==null){
            return;
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        String url = "androidamap://route?sourceApplication=amap&slat="+location.getLatitude()+"&slon="+location.getLongitude()
                +"&dlat="+lat+"&dlon="+lng+"&dname="+storeName+"&dev=0&t=2";
        Uri uri = Uri.parse(url);
        //将功能Scheme以URI的方式传入data
        intent.setData(uri);
        //启动该页面即可
        startActivity(intent);
    }
    private void openBrowserToGuide(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    public void onGetPoiDetailShareUrlResult(ShareUrlResult shareUrlResult) {

    }

    @Override
    public void onGetLocationShareUrlResult(ShareUrlResult shareUrlResult) {

    }

    @Override
    public void onGetRouteShareUrlResult(ShareUrlResult shareUrlResult) {
        if(shareUrlResult!=null&&shareUrlResult.error== SearchResult.ERRORNO.NO_ERROR){
            String share_url=shareUrlResult.getUrl();
            openBrowserToGuide(share_url);
        }
    }


    @Override
    public void onReceiveLocation(final BDLocation bdLocation) {
        if(bdLocation==null){
            return;
        }
        client.stop();
        this.location=bdLocation;
        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(bdLocation.getRadius())
                // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(bdLocation.getDirection()).latitude(bdLocation.getLatitude())
                .longitude(bdLocation.getLongitude()).build();

        // 设置定位数据
        aMap.setMyLocationData(locData);

    }

}