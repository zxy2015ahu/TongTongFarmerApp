package com.tongtong.purchaser.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.mobileim.conversation.YWMessage;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.adapter.AddressAdapter;
import com.tongtong.purchaser.application.MyApplication;
import com.tongtong.purchaser.bean.SearchAddressInfo;
import com.tongtong.purchaser.helper.ChattingOperationCustom;
import com.tongtong.purchaser.helper.ChattingUICustom;
import com.tongtong.purchaser.model.PurchaserModel;
import com.tongtong.purchaser.utils.CodeUtil;
import com.tongtong.purchaser.utils.UploadFileTask;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class LocationSelectActivity extends BaseActivity implements OnGetGeoCoderResultListener, BaiduMap.OnMapStatusChangeListener, AdapterView.OnItemClickListener, View.OnClickListener,
        UploadFileTask.HttpTaskHandler,BDLocationListener {

    private GeoCoder geocoderSearch;
    private MapView mapView;
    private ListView listView;
    private BaiduMap aMap;
    private LatLng mFinalChoosePosition;
    //private String city;
    private ArrayList<SearchAddressInfo> mData = new ArrayList<>();
    public SearchAddressInfo mAddressInfoFirst = null;
    private boolean isHandDrag = true;
    private boolean isFirstLoad = true;
    private boolean isBackFromSearch = false;
    private AddressAdapter addressAdapter;
    private UiSettings uiSettings;
    private ImageButton locationButton;
    private ImageView search;
    private TextView send;
    private View back;
    private TextView empty;
    private static final int SEARCH_ADDDRESS = 1;

    private SharedPreferences sp;
    private String savePath;
    private LocationClient client;
    private BDLocation location;
    private ImageView centerImg;
    private Handler handler=new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_location);
        client= MyApplication.getLocationClient();
        client.registerLocationListener(this);
        client.start();
        sp=getSharedPreferences("location", Context.MODE_PRIVATE);
        mapView = (MapView) findViewById(R.id.mapview);
        listView = (ListView) findViewById(R.id.listview);
        locationButton = (ImageButton) findViewById(R.id.position_btn);
        search = (ImageView) findViewById(R.id.seach);
        send = (TextView) findViewById(R.id.send);
        back =  findViewById(R.id.base_back);
        empty=(TextView) findViewById(R.id.empty);
        centerImg=(ImageView) findViewById(R.id.center_image);

        search.setOnClickListener(this);
        send.setOnClickListener(this);
        back.setOnClickListener(this);

        locationButton.setOnClickListener(this);


        addressAdapter = new AddressAdapter(this, mData);
        listView.setAdapter(addressAdapter);

        listView.setOnItemClickListener(this);
        initMap();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }

    private void initMap() {

        if (aMap == null) {
            aMap = mapView.getMap();

            //地图ui界面设置
            uiSettings = aMap.getUiSettings();

            //地图比例尺的开启
            uiSettings.setZoomGesturesEnabled(true);

            //关闭地图缩放按钮 就是那个加号 和减号


            //对amap添加移动地图事件监听器
            aMap.setOnMapStatusChangeListener(this);
            aMap.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(new LatLng(Double.valueOf(sp.getString("lat","0")),Double.valueOf(sp.getString("lng","0"))),16f));

            //            locationMarker = aMap.addMarker(new MarkerOptions()
            //                    .anchor(0.5f, 0.5f)
            //                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.marker)))
            //                    .position(new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude())));

            //拿到地图中心的经纬度
        }

        setMap();

    }

    private void setMap() {

        geocoderSearch = GeoCoder.newInstance();

        //设置逆地理编码监听
        geocoderSearch.setOnGetGeoCodeResultListener(this);
    }

    /**
     * 根据经纬度得到地址
     */
    public void getAddressFromLonLat(final LatLng latLonPoint) {
        // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        geocoderSearch.reverseGeoCode(new ReverseGeoCodeOption()
                .location(latLonPoint).newVersion(1));
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }
    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
        if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {//转换成功
            listView.setEmptyView(empty);
        } else {
            //条目中第一个地址 也就是当前你所在的地址
            List<PoiInfo> poiItems=reverseGeoCodeResult.getPoiList();
            mData.clear();
            //搜索到数据
            if (poiItems != null && poiItems.size() > 0) {
                SearchAddressInfo addressInfo = null;
                for (PoiInfo poiItem : poiItems) {
                    addressInfo = new SearchAddressInfo(poiItem.name, poiItem.address, false, poiItem.location,poiItem.city);
                    mData.add(addressInfo);
                }
                addressAdapter.notifyDataSetChanged();
            }
            if (isHandDrag) {
                if(poiItems.size()>0){
                    mData.get(0).isChoose = true;
                    mAddressInfoFirst=mData.get(0);
                }else{
                    listView.setEmptyView(empty);
                }

            }
        }
    }

    @Override
    public void onMapStatusChange(MapStatus mapStatus) {

    }

    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus) {
        aMap.clear();
        centerImg.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus, int i) {
        aMap.clear();
        centerImg.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMapStatusChangeFinish(MapStatus mapStatus) {
        //
        centerImg.setVisibility(View.GONE);
        mFinalChoosePosition = mapStatus.target;

        addMarker(mapStatus.target.latitude,mapStatus.target.longitude);


        if (isHandDrag || isFirstLoad) {//手动去拖动地图

            // 开始进行poi搜索
            getAddressFromLonLat(mapStatus.target);

        } else if (isBackFromSearch) {
            //搜索地址返回后 拿到选择的位置信息继续搜索附近的兴趣点
            isBackFromSearch = false;
        } else {
            addressAdapter.notifyDataSetChanged();
        }
        isHandDrag = true;
        isFirstLoad = false;
    }



    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        geocoderSearch.destroy();
        client.unRegisterLocationListener(this);
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        mFinalChoosePosition = mData.get(position).latLonPoint;
        for (int i = 0; i < mData.size(); i++) {
            mData.get(i).isChoose = false;
        }
        mData.get(position).isChoose = true;

        isHandDrag = false;

        // 点击之后，改变了地图中心位置， onCameraChangeFinish 也会调用
        // 只要地图发生改变，就会调用 onCameraChangeFinish ，不是说非要手动拖动屏幕才会调用该方法
        aMap.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(new LatLng(mFinalChoosePosition.latitude, mFinalChoosePosition.longitude), 16));
    }



    @Override
    public void onClick(View v) {
        if (v == locationButton) {
            //回到当前位置
            if(location!=null){
                isHandDrag=true;
                aMap.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
            }
        } else if (v == back) {
            onBackPressed();
        } else if (v == search) {

            Intent intent = new Intent(this, SearchAddressActivity.class);
            intent.putExtra("position", mFinalChoosePosition);
            intent.putExtra("city", "");
            startActivityForResult(intent, SEARCH_ADDDRESS);
            isBackFromSearch = false;

        } else if (v == send) {

            sendLocaton();

        }
    }


    private void sendLocaton(){
        if(getIntent().getStringExtra("flag")!=null){
            SearchAddressInfo info=null;
            for (SearchAddressInfo infos : mData) {
                if (infos.isChoose) {
                    info = infos;
                }
            }
            Intent intent=new Intent();
            intent.putExtra("position",info);
            setResult(RESULT_OK,intent);
            finish();
            return;
        }
        aMap.snapshot(new BaiduMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(Bitmap bitmap) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                String pathFolder= Environment.getExternalStorageDirectory().toString() + "/myou/cacheimage/";
                savePath=pathFolder+"t_" + sdf.format(new Date()) + ".png";
                if(null == bitmap){
                    return;
                }
                try {
                    File file = new File(pathFolder);
                    if(!file.exists()){
                        file.mkdirs();
                    }
                    FileOutputStream fos = new FileOutputStream(new File(savePath));

                    boolean b = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    try {
                        fos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (b) {
                        UploadFileTask uploadFileTask = new UploadFileTask(new File(savePath));
                        uploadFileTask.setTaskHandler(LocationSelectActivity.this);
                        JsonObject requestJson = new JsonObject();
                        requestJson.addProperty("token", UserUtil.getUserModel(LocationSelectActivity.this).getToken());
                        uploadFileTask.execute(UrlUtil.UPDATE_GEO_THUMB_URL,requestJson.toString());

                    }else {
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode,final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SEARCH_ADDDRESS && resultCode == RESULT_OK) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    SearchAddressInfo info = (SearchAddressInfo) data.getParcelableExtra("position");
                    mAddressInfoFirst = info; // 上一个页面传过来的 位置信息
                    info.isChoose = true;
                    isBackFromSearch = true;
                    isHandDrag = true;
                    LatLng latLonPoint = new LatLng(info.latLonPoint.latitude, info.latLonPoint.longitude);
                    MapStatus mapStatus=new MapStatus.Builder()
                            .target(latLonPoint)
                            .zoom(16f)
                            .build();
                    //city=mAddressInfoFirst.city;
                    //移动地图
                    aMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatus));
                }
            },1000);
        }
    }
    @Override
    public void taskStart(int code) {

    }

    @Override
    public void taskSuccessful(String str, int code) {
        JsonObject resultJson = new JsonParser().parse(str).getAsJsonObject();
        int resultCode = resultJson.get("code").getAsInt();
        if(verification(resultCode)){
            if(resultCode == CodeUtil.SUCCESS_CODE){
                PurchaserModel purchaser = UserUtil.getUserModel(this);
                String headUrl = resultJson.get("data").getAsJsonObject().get("headUrl").getAsString();
                SearchAddressInfo info=null;
                for (SearchAddressInfo infos : mData) {
                    if (infos.isChoose) {
                        info = infos;
                    }
                }
                final SearchAddressInfo addressInfo=info;
                if(ChattingUICustom.conversation!=null) {
                    YWMessage ywMessage =ChattingOperationCustom.createCustomGeoMessage(addressInfo.latLonPoint.latitude,
                            addressInfo.latLonPoint.longitude,UrlUtil.IMG_SERVER_URL+headUrl,addressInfo.title,addressInfo.addressName);
                    ChattingUICustom.conversation.getMessageSender().sendMessage(ywMessage, 120, null);
                    if(getIntent().getStringExtra("flags")!=null){
                        ChattingOperationCustom.sendTransMsg("对方发来送货地址。");
                    }
                }
                new File(savePath).delete();
                finish();
            }
            else{
                showToast(R.string.submit_fail);
            }
        }
    }

    @Override
    public void taskFailed(int code) {

    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        //经纬度信息
        if(bdLocation==null){
            return;
        }
        client.stop();
        this.location=bdLocation;
        LatLng latLonPoint = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
        MapStatus mapStatus=new MapStatus.Builder()
                .target(latLonPoint)
                .zoom(16f)
                .build();
        aMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatus));
    }
    private void addMarker(double lat,double lng){
        MarkerOptions markerOptions=new MarkerOptions();
        markerOptions.draggable(false);
        markerOptions.animateType(MarkerOptions.MarkerAnimateType.jump);
        markerOptions.anchor(0.5f,1.0f);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.red));
        markerOptions.position(new LatLng(lat,lng));
        aMap.addOverlay(markerOptions);
    }
}