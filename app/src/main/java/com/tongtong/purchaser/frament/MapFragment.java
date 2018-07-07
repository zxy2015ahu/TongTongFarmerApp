package com.tongtong.purchaser.frament;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.shape.Rectangle;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.activity.BaseActivity;
import com.tongtong.purchaser.activity.MainActivity;
import com.tongtong.purchaser.activity.ProduceDetailsViewerActvity;
import com.tongtong.purchaser.activity.ReleaseListActivity;
import com.tongtong.purchaser.activity.RobRpActivity;
import com.tongtong.purchaser.application.MyApplication;
import com.tongtong.purchaser.model.AddressModel;
import com.tongtong.purchaser.model.MapReleaseModel;
import com.tongtong.purchaser.model.ProduceType;
import com.tongtong.purchaser.model.QueryRangeModel;
import com.tongtong.purchaser.model.Region;
import com.tongtong.purchaser.utils.BitmapUtil;
import com.tongtong.purchaser.utils.CodeUtil;
import com.tongtong.purchaser.utils.CommonUtils;
import com.tongtong.purchaser.utils.DBManager;
import com.tongtong.purchaser.utils.DistanceUtil;
import com.tongtong.purchaser.utils.HistoryHelper;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.NetUtil;
import com.tongtong.purchaser.utils.ProduceTypesHelper;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.widget.AlertDialog;
import com.tongtong.purchaser.widget.NoScrollGridView;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by zxy on 2018/4/10.
 */

public class MapFragment extends BaseFrament implements BDLocationListener,BaiduMap.OnMapStatusChangeListener,
        View.OnClickListener,HttpTask.HttpTaskHandler,OnGetGeoCoderResultListener,BaiduMap.OnMarkerClickListener{
    private SupportMapFragment map;
    private SharedPreferences location,mylocation;
    private LocationClient client;
    private ImageView centerImg;
    private Handler handler;
    private PopupWindow adcodePop,typePop;
    private View anchor;
    private TextView tv_city_name,tv_type_info;
    private DisplayMetrics dm;
    private DBManager dbManager;
    private int grid_height;
    private Region pregion,cregion;
    private HashMap<Integer,Integer> selecte_type=new HashMap<>();
    private static double MAX_DISTANCE = 100;
    private int adcode;
    private static int HEAD_SIZE = 40;
    private List<Marker> markers=new ArrayList<>();
    private Marker pointMarker;
    private double lat,lng;
    private LatLng latLng;
    private JsonObject dataJson;
    private BDLocation loc;
    private int mcount;
    private int p1=-1,p2=-1,p3=-1;
    private ProduceType ptype,ctype;
    private ProduceType produce;
    private LinearLayout histoty_select;
    private TextView history_1,history_2;
    private Region select_region;
    private TextView tv_location;
    private int location_adcode;
    private String location_city;
    private int a1=-1,a2=-1,a3=-1;
    private AddressModel address;
    private TextView temp_select;
    private ImageView left_button;
    private Shade shade;
    private ImageView red_dot;
    private String addressStr="";
    private GeoCoder geoCoder;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.map_fragment,container,false);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        red_dot=(ImageView) view.findViewById(R.id.red_dot);
        shade=new Shade();
        geoCoder=GeoCoder.newInstance();
        geoCoder.setOnGetGeoCodeResultListener(this);
        centerImg=(ImageView) view.findViewById(R.id.center_image);
        tv_city_name=(TextView)view.findViewById(R.id.tv_city_name);
        tv_type_info=(TextView)view.findViewById(R.id.tv_type_info);
        view.findViewById(R.id.rob_rp).setOnClickListener(this);
        left_button=(ImageView) view.findViewById(R.id.left_button);
        dbManager=new DBManager(getActivity());
        grid_height= UIUtil.dip2px(getActivity(),35f);
        dm=new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        view.findViewById(R.id.tv_choose_city).setOnClickListener(this);
        view.findViewById(R.id.layout_pay_type).setOnClickListener(this);
        anchor=view.findViewById(R.id.anchor);
        handler=new Handler();
        location=getActivity().getSharedPreferences("location",0);
        mylocation=getActivity().getSharedPreferences("mylocation",0);
        adcode=location.getInt("code",0);
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(new LatLng(Double.valueOf(location.getString("lat","0")),Double.valueOf(location.getString("lng","0"))));
        BaiduMapOptions options=new BaiduMapOptions().mapStatus(builder.build());
        map=SupportMapFragment.newInstance(options);
        if(mylocation.getInt("code",0)!=0){
            if(mylocation.getInt("code",0)!=location.getInt("code",0)){
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showAlert(location.getString("city",""));
                    }
                },1000);
            }
        }
        getChildFragmentManager().beginTransaction().replace(R.id.mapview,map).commit();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                map.getBaiduMap().setOnMapStatusChangeListener(MapFragment.this);
                map.getBaiduMap().setOnMarkerClickListener(MapFragment.this);
                map.getBaiduMap().setMyLocationEnabled(true);
//                MyLocationConfiguration configuration=new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING,
//                        true, BitmapDescriptorFactory.fromResource(R.drawable.location_marker));
//                //aMap.setOnMyLocationChangeListener(this);
//                map.getBaiduMap().setMyLocationConfiguration(configuration);
                //map.getBaiduMap().setViewPadding(0,0,0,bottom.getHeight()+ UIUtil.dip2px(getActivity(),20f));
            }
        },200);
        client= MyApplication.getLocationClient();
        client.registerLocationListener(this);
        client.start();
    }
    private void gethongbaocountbydistance(){
        HttpTask task=new HttpTask(getActivity());
        task.setTaskHandler(new HttpTask.HttpTaskHandler() {
            @Override
            public void taskStart(int code) {

            }
            @Override
            public void taskSuccessful(String str, int code) {
                JsonObject selectResultJson = new JsonParser().parse(str)
                        .getAsJsonObject();
                int selectResultCode = selectResultJson.get("code").getAsInt();
                if(verification(selectResultCode)){
                    int hongbao_num=selectResultJson.get("hongbao_num").getAsInt();
                    if(hongbao_num>0){
                        red_dot.setVisibility(View.VISIBLE);
                        handler.postDelayed(shade,3000);
                    }else{
                        red_dot.setVisibility(View.GONE);
                        handler.removeCallbacks(shade);
                    }
                }
            }
            @Override
            public void taskFailed(int code) {
                red_dot.setVisibility(View.GONE);
            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("token",UserUtil.getUserModel(getActivity()).getToken());
        object.addProperty("lat",loc.getLatitude());
        object.addProperty("lng",loc.getLongitude());
        object.addProperty("purchaser_id",UserUtil.getUserModel(getActivity()).getId());
        task.execute(UrlUtil.GET_RP_COUNT,object.toString());
    }

    private void showWindow(){
        View view=getActivity().getLayoutInflater().inflate(R.layout.location_details_layout,null);
        final TextView info_txt=(TextView) view.findViewById(R.id.info);
        info_txt.setText(getCountString(mcount));
        view.findViewById(R.id.close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.getBaiduMap().hideInfoWindow();
            }
        });
        info_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dataJson!=null&&loc!=null){
                    Intent intent=new Intent();
                    intent.setClass(getActivity(), ReleaseListActivity.class);
                    intent.putExtra("dataJson",dataJson.toString());
                    intent.putExtra("lat",loc.getLatitude());
                    intent.putExtra("lng",loc.getLongitude());
                    intent.putExtra("lat1",lat);
                    intent.putExtra("lng1",lng);
                    intent.putExtra("count",mcount);
                    intent.putExtra("produce",produce);
                    startActivity(intent);
                }
            }
        });
        InfoWindow info=new InfoWindow(view,latLng,-52);
        map.getBaiduMap().showInfoWindow(info);
    }
    private class Shade implements Runnable{
        @Override
        public void run() {
            ObjectAnimator animator = CommonUtils.tada(left_button);
            animator.start();
            handler.postDelayed(this,3000);
        }
    }
    @Override
    public boolean onMarkerClick(Marker marker) {
        if(marker.getExtraInfo()==null){
            return true;
        }
        Bundle extra=marker.getExtraInfo();
        if(extra!=null){
            int id=extra.getInt("id");
            getReleaseInfo(id);
        }
        return true;
    }
    private void getReleaseInfo(int id){
        HttpTask task=new HttpTask(getActivity());
        task.setTaskHandler(new HttpTask.HttpTaskHandler() {
            @Override
            public void taskStart(int code) {
                ((BaseActivity)getActivity()).showLoading();
            }
            @Override
            public void taskSuccessful(String str, int code) {
                ((BaseActivity)getActivity()).dismissLoading();
                Intent intent=new Intent();
                intent.setClass(getActivity(), ProduceDetailsViewerActvity.class);
                intent.putExtra("data",str);
                startActivity(intent);
            }
            @Override
            public void taskFailed(int code) {
                ((BaseActivity)getActivity()).dismissLoading();
            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("produce_id",id);
        object.addProperty("purchaser_id", UserUtil.getUserModel(getActivity()).getId());
        task.execute(UrlUtil.GET_FARMER_RELEASE,object.toString());
    }
    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }
    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
        if(reverseGeoCodeResult.error== SearchResult.ERRORNO.NO_ERROR){
            List<PoiInfo> pois=reverseGeoCodeResult.getPoiList();
            if(pois!=null&&pois.size()>0){
                PoiInfo pi=pois.get(0);
                addressStr=pi.name;
            }else{
                addressStr="";
            }
        }else{
            addressStr="";
        }
        loadData();
    }

    private class MyAdapter extends ArrayAdapter<Region> {
        public MyAdapter(Context ctx){
            super(ctx,0);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(convertView==null){
                convertView=getActivity().getLayoutInflater().inflate(R.layout.common_grid_item,null);
            }
            TextView title=(TextView)convertView.findViewById(R.id.title);
            title.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,grid_height));
            Region region=getItem(position);
            title.setText(region.getRegion_name());
            if((region.getAdcode()==a1||region.getAdcode()==a2||region.getAdcode()==a3)&&(position!=0)){
                title.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimary));
            }else if(position==0){
                if(a3==-1&&(address!=null&&address.getDistrict()==region.getId())&&(select_region!=null&&select_region.getAdcode()==a2)){
                    title.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimary));
                }else if(a2==-1&&(address!=null&&address.getDistrict()==region.getId())&&a3==-1&&(select_region!=null&&select_region.getAdcode()==a1)){
                    title.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimary));
                }else if(a1==-1&&a2==-1&&a3==-1&&region.getId()==0&&select_region!=null&&select_region.getAdcode()==0){
                    title.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimary));
                }else{
                    title.setTextColor(ContextCompat.getColor(getActivity(),R.color.aliwx_common_text_color));
                }
            }else{
                title.setTextColor(ContextCompat.getColor(getActivity(),R.color.aliwx_common_text_color));
            }
            return convertView;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        map.getBaiduMap().setMyLocationEnabled(false);
        geoCoder.destroy();
    }


    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.tv_choose_city){
            showAdcodePop();
        }else if(v.getId()==R.id.layout_pay_type){
            showTypePop();
        }else if(v.getId()==R.id.rob_rp){
            if(loc!=null){
                Intent rob=new Intent();
                rob.setClass(getActivity(),RobRpActivity.class);
                rob.putExtra("lat",loc.getLatitude());
                rob.putExtra("lng",loc.getLongitude());
                rob.putExtra("address",loc.getAddress().street+loc.getAddress().streetNumber);
                startActivity(rob);
            }
        }
    }

    private void showAdcodePop(){
        if(temp_select!=null&&temp_select!=tv_city_name){
            temp_select.setTextColor(ContextCompat.getColor(getActivity(),R.color.aliwx_common_text_color));
            temp_select.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.icon_down,0);
        }
        temp_select=tv_city_name;
        if(adcodePop==null){
            View container=View.inflate(getActivity(),R.layout.city_address_list,null);
            int[] outlocation=new int[2];
            anchor.getLocationOnScreen(outlocation);
            adcodePop=new PopupWindow(container, ViewGroup.LayoutParams.MATCH_PARENT, dm.heightPixels-outlocation[1]-anchor.getHeight(),true);
            adcodePop.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getActivity(),R.color.city_search_bg)));
            adcodePop.setOutsideTouchable(true);
            adcodePop.setTouchable(true);
            adcodePop.setAnimationStyle(R.style.anim_menu_bottombar);
            adcodePop.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    tv_city_name.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.icon_down_green,0);
                }
            });
            initData(container);
        }
        List<Region> regions= HistoryHelper.getHistory(getActivity());
        if(regions.size()>1){
            histoty_select.setVisibility(View.VISIBLE);
            history_1.setVisibility(View.VISIBLE);
            history_2.setVisibility(View.VISIBLE);
            history_1.setText(regions.get(0).getRegion_name());
            history_1.setTag(regions.get(0).getAdcode());
            history_2.setText(regions.get(1).getRegion_name());
            history_2.setTag(regions.get(1).getAdcode());
        }else if(regions.size()>0){
            histoty_select.setVisibility(View.VISIBLE);
            history_1.setText(regions.get(0).getRegion_name());
            history_1.setTag(regions.get(0).getAdcode());
            history_1.setVisibility(View.VISIBLE);
            history_2.setVisibility(View.GONE);
        }else{
            history_1.setVisibility(View.GONE);
            history_2.setVisibility(View.GONE);
            histoty_select.setVisibility(View.GONE);
        }
        tv_location.setText(location_city);
        tv_city_name.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.icon_up,0);
        tv_city_name.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimary));
        adcodePop.showAsDropDown(anchor);
    }
    private class MyTypeAdapter extends ArrayAdapter<ProduceType> {
        public MyTypeAdapter(Context ctx){
            super(ctx,0);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(convertView==null){
                convertView=getActivity().getLayoutInflater().inflate(R.layout.common_grid_item,null);
            }
            TextView title=(TextView)convertView.findViewById(R.id.title);
            title.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,grid_height));
            ProduceType region=getItem(position);
            title.setText(region.getName());
            if((region.getId()==p1||region.getId()==p2||region.getId()==p3)&&(position!=0)){
                title.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimary));
            }else if(position==0){
                if(p3==-1&&region.getLevel()==2&&(produce!=null&&produce.getId()==p2)){
                    title.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimary));
                }else if(p2==-1&&p3==-1&&region.getLevel()==1&&(produce!=null&&produce.getId()==p1)){
                    title.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimary));
                }else if(p1==-1&&p2==-1&&p3==-1&&region.getLevel()==0&&produce!=null&&produce.getId()==0){
                    title.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimary));
                }else{
                    title.setTextColor(ContextCompat.getColor(getActivity(),R.color.aliwx_common_text_color));
                }
            }else{
                title.setTextColor(ContextCompat.getColor(getActivity(),R.color.aliwx_common_text_color));
            }
            return convertView;
        }
    }
    private void showTypePop(){
        if(temp_select!=null&&temp_select!=tv_type_info){
            temp_select.setTextColor(ContextCompat.getColor(getActivity(),R.color.aliwx_common_text_color));
            temp_select.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.icon_down,0);
        }
        temp_select=tv_type_info;
        if(typePop==null){
            View container=View.inflate(getActivity(),R.layout.city_grid_list,null);
            int[] outlocation=new int[2];
            anchor.getLocationOnScreen(outlocation);
            typePop=new PopupWindow(container, ViewGroup.LayoutParams.MATCH_PARENT, dm.heightPixels-outlocation[1]-anchor.getHeight(),true);
            typePop.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getActivity(),R.color.city_search_bg)));
            typePop.setOutsideTouchable(true);
            typePop.setTouchable(true);
            typePop.setAnimationStyle(R.style.anim_menu_bottombar);
            typePop.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    tv_type_info.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.icon_down_green,0);
                }
            });
            initTypeData(container);
        }
        tv_type_info.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.icon_up,0);
        tv_type_info.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimary));
        typePop.showAsDropDown(anchor);
    }
    private void onselecttype(ProduceType produce){
        typePop.dismiss();
        this.produce=produce;
        tv_type_info.setText(produce.getName());
        if(produce.getLevel()==1){
            p1=produce.getId();
            p2=-1;
            p3=-1;
        }else if(produce.getLevel()==2){
            p2=produce.getId();
            p3=-1;
        }else if(produce.getLevel()==3){
            p3=produce.getId();
            //保存搜索历史
        }else{
            p1=-1;
            p2=-1;
            p3=-1;
        }
        loadData();
    }
    private void initTypeData(View container){
        {
            final TextView select_box=(TextView) container.findViewById(R.id.select_box);
            final TextView back=(TextView) container.findViewById(R.id.back);
            back.setVisibility(View.GONE);
            select_box.setText("当前选择：");
            final NoScrollGridView list=(NoScrollGridView)container.findViewById(android.R.id.list);
            list.setHorizontalSpacing(1);
            list.setVerticalSpacing(1);
            final MyTypeAdapter adapter=new MyTypeAdapter(getActivity());
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(position==0){
                        onselecttype(adapter.getItem(0));
                        return;
                    }
                    ptype=adapter.getItem(position);
                    select_box.setText("当前选择："+ptype.getName());
                    back.setVisibility(View.VISIBLE);
                    p1=ptype.getId();
                    ProduceTypesHelper.getSubProduce(ptype.getId(), getActivity(),new ProduceTypesHelper.OnDataRecieveListener() {
                        @Override
                        public void onRecieveData(List<ProduceType> regions) {
                            adapter.clear();
                            ProduceType p=new ProduceType();
                            p.setId(ptype.getId());
                            p.setName("不限品种");
                            p.setLevel(ptype.getLevel());
                            adapter.add(p);
                            adapter.addAll(regions);
                            list.setAdapter(adapter);
                            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    if(position==0){
                                        onselecttype(ptype);
                                        return;
                                    }
                                    ctype=adapter.getItem(position);
                                    back.setVisibility(View.VISIBLE);
                                    select_box.setText("当前选择："+ptype.getName()+"/"+ctype.getName());
                                    p1=ptype.getId();
                                    p2=ctype.getId();
                                    ProduceTypesHelper.getSubProduce(ctype.getId(),getActivity(), new ProduceTypesHelper.OnDataRecieveListener() {
                                        @Override
                                        public void onRecieveData(List<ProduceType> regions) {
                                            adapter.clear();
                                            ProduceType c=new ProduceType();
                                            c.setName("不限品种");
                                            c.setId(ctype.getId());
                                            c.setLevel(ctype.getLevel());
                                            adapter.add(c);
                                            adapter.addAll(regions);
                                            list.setAdapter(adapter);
                                            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                @Override
                                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                    if(position==0){
                                                        onselecttype(ctype);
                                                        return;
                                                    }
                                                    final ProduceType dregion=adapter.getItem(position);
                                                    p1=ptype.getId();
                                                    p2=ctype.getId();
                                                    p3=dregion.getId();
                                                    onselecttype(dregion);
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            });
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ptype!=null&&ctype!=null){
                        ctype=null;
                        back.setVisibility(View.VISIBLE);
                        select_box.setText("当前选择："+ptype.getName());
                        p1=ptype.getId();
                        ProduceTypesHelper.getSubProduce(ptype.getId(),getActivity(), new ProduceTypesHelper.OnDataRecieveListener() {
                            @Override
                            public void onRecieveData(List<ProduceType> regions) {
                                adapter.clear();
                                ProduceType p=new ProduceType();
                                p.setId(ptype.getId());
                                p.setName("不限品种");
                                p.setLevel(ptype.getLevel());
                                adapter.add(p);
                                adapter.addAll(regions);
                                list.setAdapter(adapter);
                                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        if(position==0){
                                            onselecttype(ptype);
                                            return;
                                        }
                                        ctype=adapter.getItem(position);
                                        back.setVisibility(View.VISIBLE);
                                        select_box.setText("当前选择："+ptype.getName()+"/"+ctype.getName());
                                        p1=ptype.getId();
                                        p2=ctype.getId();
                                        ProduceTypesHelper.getSubProduce(ctype.getId(),getActivity(), new ProduceTypesHelper.OnDataRecieveListener() {
                                            @Override
                                            public void onRecieveData(List<ProduceType> regions) {
                                                adapter.clear();
                                                ProduceType c=new ProduceType();
                                                c.setName("不限品种");
                                                c.setId(ctype.getId());
                                                c.setLevel(ctype.getLevel());
                                                adapter.add(c);
                                                adapter.addAll(regions);
                                                list.setAdapter(adapter);
                                                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                    @Override
                                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                        if(position==0){
                                                            onselecttype(ctype);
                                                            return;
                                                        }
                                                        final ProduceType dregion=adapter.getItem(position);
                                                        p1=ptype.getId();
                                                        p2= ctype.getId();
                                                        p3=dregion.getId();
                                                        onselecttype(dregion);
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }else if(ptype!=null){
                        select_box.setText("当前选择：");
                        back.setVisibility(View.GONE);
                        ProduceTypesHelper.getParentType(getActivity(), new ProduceTypesHelper.OnDataRecieveListener() {
                            @Override
                            public void onRecieveData(List<ProduceType> regions) {
                                adapter.clear();
                                ProduceType r=new ProduceType();
                                r.setId(0);
                                r.setName("全部分类");
                                adapter.add(r);
                                adapter.addAll(regions);
                                list.setAdapter(adapter);
                                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        if(position==0){
                                            onselecttype(adapter.getItem(0));
                                            return;
                                        }
                                        ptype=adapter.getItem(position);
                                        back.setVisibility(View.VISIBLE);
                                        select_box.setText("当前选择："+ptype.getName());
                                        p1=ptype.getId();
                                        ProduceTypesHelper.getSubProduce(ptype.getId(),getActivity(), new ProduceTypesHelper.OnDataRecieveListener() {
                                            @Override
                                            public void onRecieveData(List<ProduceType> regions) {
                                                adapter.clear();
                                                ProduceType p=new ProduceType();
                                                p.setId(ptype.getId());
                                                p.setName("不限品种");
                                                p.setLevel(ptype.getLevel());
                                                adapter.add(p);
                                                adapter.addAll(regions);
                                                list.setAdapter(adapter);
                                                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                    @Override
                                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                        if(position==0){
                                                            onselecttype(ptype);
                                                            return;
                                                        }
                                                        ctype=adapter.getItem(position);
                                                        back.setVisibility(View.VISIBLE);
                                                        select_box.setText("当前选择："+ptype.getName()+"/"+ctype.getName());
                                                        p1=ptype.getId();
                                                        p2= ctype.getId();
                                                        ProduceTypesHelper.getSubProduce(ctype.getId(), getActivity(),new ProduceTypesHelper.OnDataRecieveListener() {
                                                            @Override
                                                            public void onRecieveData(List<ProduceType> regions) {
                                                                adapter.clear();
                                                                ProduceType c=new ProduceType();
                                                                c.setName("不限品种");
                                                                c.setId(ctype.getId());
                                                                c.setLevel(ctype.getLevel());
                                                                adapter.add(c);
                                                                adapter.addAll(regions);
                                                                list.setAdapter(adapter);
                                                                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                                    @Override
                                                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                                        if(position==0){
                                                                            onselecttype(ctype);
                                                                            return;
                                                                        }
                                                                        final ProduceType dregion=adapter.getItem(position);
                                                                        p1=ptype.getId();
                                                                        p2= ctype.getId();
                                                                        p3=dregion.getId();
                                                                        onselecttype(dregion);
                                                                    }
                                                                });
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                }
            });
            if(produce==null){
                ProduceTypesHelper.getParentType(getActivity(), new ProduceTypesHelper.OnDataRecieveListener() {
                    @Override
                    public void onRecieveData(List<ProduceType> produceTypes) {
                        ProduceType r=new ProduceType();
                        r.setId(0);
                        r.setName("全部分类");
                        produce=r;
                        adapter.add(r);
                        adapter.addAll(produceTypes);
                        list.setAdapter(adapter);
                    }
                });
                p1=-1;
                p2=-1;
                p3=-1;
            }else if(produce.getLevel()==1){
                ProduceTypesHelper.getParentType(getActivity(), new ProduceTypesHelper.OnDataRecieveListener() {
                    @Override
                    public void onRecieveData(List<ProduceType> produceTypes) {
                        ProduceType r=new ProduceType();
                        r.setId(0);
                        r.setName("全部分类");
                        produce=r;
                        adapter.add(r);
                        adapter.addAll(produceTypes);
                        list.setAdapter(adapter);
                    }
                });
                p1=produce.getId();
                p2=-1;
                p3=-1;
            }else if(produce.getLevel()==2){
                ptype=ProduceTypesHelper.getParentProduce(getActivity(),produce.getId());
                select_box.setText("当前选择："+ptype.getName());
                back.setVisibility(View.VISIBLE);
                p1=ptype.getId();
                p2=produce.getId();
                p3=-1;
                ProduceTypesHelper.getSubProduce(ptype.getId(), getActivity(),new ProduceTypesHelper.OnDataRecieveListener() {
                    @Override
                    public void onRecieveData(List<ProduceType> regions) {
                        adapter.clear();
                        ProduceType p=new ProduceType();
                        p.setId(ptype.getId());
                        p.setName("不限品种");
                        p.setLevel(ptype.getLevel());
                        adapter.add(p);
                        adapter.addAll(regions);
                        list.setAdapter(adapter);
                        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                if(position==0){
                                    onselecttype(ptype);
                                    return;
                                }
                                ctype=adapter.getItem(position);
                                back.setVisibility(View.VISIBLE);
                                select_box.setText("当前选择："+ptype.getName()+"/"+ctype.getName());
                                ProduceTypesHelper.getSubProduce(ctype.getId(),getActivity(), new ProduceTypesHelper.OnDataRecieveListener() {
                                    @Override
                                    public void onRecieveData(List<ProduceType> regions) {
                                        adapter.clear();
                                        ProduceType c=new ProduceType();
                                        c.setName("不限品种");
                                        c.setId(ctype.getId());
                                        c.setLevel(ctype.getLevel());
                                        c.setLevel(ctype.getLevel());
                                        adapter.add(c);
                                        adapter.addAll(regions);
                                        list.setAdapter(adapter);
                                        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                if(position==0){
                                                    onselecttype(ctype);
                                                    return;
                                                }
                                                final ProduceType dregion=adapter.getItem(position);
                                                onselecttype(dregion);
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }else if(produce.getLevel()==3){
                ctype=ProduceTypesHelper.getParentProduce(getActivity(),produce.getId());
                ptype=ProduceTypesHelper.getParentProduce(getActivity(),ctype.getId());
                back.setVisibility(View.VISIBLE);
                p1=ptype.getId();
                p2=ctype.getId();
                p3=produce.getId();
                select_box.setText("当前选择："+ptype.getName()+"/"+ctype.getName());
                ProduceTypesHelper.getSubProduce(ctype.getId(), getActivity(),new ProduceTypesHelper.OnDataRecieveListener() {
                    @Override
                    public void onRecieveData(List<ProduceType> regions) {
                        adapter.clear();
                        ProduceType c=new ProduceType();
                        c.setName("不限品种");
                        c.setId(ctype.getId());
                        c.setLevel(ctype.getLevel());
                        adapter.add(c);
                        adapter.addAll(regions);
                        list.setAdapter(adapter);
                        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                if(position==0){
                                    onselecttype(ctype);
                                    return;
                                }
                                final ProduceType dregion=adapter.getItem(position);
                                onselecttype(dregion);
                            }
                        });
                    }
                });
            }
        }
    }

    private SpannableStringBuilder getCountString(int count){
        ForegroundColorSpan colorSpan;
        SpannableStringBuilder builder=new SpannableStringBuilder();
        if(!TextUtils.isEmpty(addressStr)){
            colorSpan=new ForegroundColorSpan(0xff45C173);
            builder.append(addressStr);
            builder.setSpan(colorSpan,0,builder.length(),Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        builder.append("附近");
        if(produce!=null&&produce.getId()>0){
            colorSpan=new ForegroundColorSpan(0xff45C173);
            builder.append(produce.getName());
            builder.setSpan(colorSpan,2,builder.length(),Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        builder.append("约有");
        String con=String.valueOf(count);
        builder.append(con);
        colorSpan=new ForegroundColorSpan(0xff45C173);
        RelativeSizeSpan sizeSpan=new RelativeSizeSpan(1.2f);
        StyleSpan styleSpan=new StyleSpan(Typeface.BOLD);
        builder.setSpan(colorSpan,builder.length()-con.length(),builder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.setSpan(sizeSpan,builder.length()-con.length(),builder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.setSpan(styleSpan,builder.length()-con.length(),builder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.append("条卖菜信息 >>");
        return builder;
    }
    private void onselectaddress(Region region){
        adcodePop.dismiss();
        tv_city_name.setText(region.getRegion_name());
        adcode=region.getAdcode();
        geoCoder.geocode(new GeoCodeOption().address(region.getRegion_name()).city(""));
    }
    private void initData(View container){
        final TextView select_box=(TextView) container.findViewById(R.id.select_box);
        final TextView back=(TextView) container.findViewById(R.id.back);
        tv_location=(TextView) container.findViewById(R.id.location);
        histoty_select=(LinearLayout) container.findViewById(R.id.histoty_select);
        history_1=(TextView) container.findViewById(R.id.history_1);
        history_2=(TextView) container.findViewById(R.id.history_2);
        history_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_city_name.setText(history_1.getText().toString());
                adcode=(int)history_1.getTag();
                adcodePop.dismiss();
                Region region=dbManager.getRegionByAdcode(adcode);
                HistoryHelper.addHistory(getActivity(),region);

            }
        });
        history_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_city_name.setText(history_2.getText().toString());
                adcode=(int)history_2.getTag();
                adcodePop.dismiss();
                Region region=dbManager.getRegionByAdcode(adcode);
                HistoryHelper.addHistory(getActivity(),region);

            }
        });
        tv_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_city_name.setText(location_city);
                adcode=location_adcode;
                adcodePop.dismiss();
                Region region=dbManager.getRegionByAdcode(adcode);
                HistoryHelper.addHistory(getActivity(),region);

            }
        });

        back.setVisibility(View.GONE);
        select_box.setText("选择地区：");
        final NoScrollGridView list=(NoScrollGridView)container.findViewById(android.R.id.list);
        list.setHorizontalSpacing(1);
        list.setVerticalSpacing(1);
        final MyAdapter adapter=new MyAdapter(getActivity());
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position==0){
                    onselectaddress(adapter.getItem(0));
                    return;
                }
                pregion=adapter.getItem(position);
                select_box.setText("选择地区："+pregion.getRegion_name());
                a1=pregion.getAdcode();
                back.setVisibility(View.VISIBLE);
                dbManager.getData(pregion.getId(), new DBManager.OnDataRecieveListener() {
                    @Override
                    public void recieveData(List<Region> regions) {
                        adapter.clear();
                        Region p=new Region();
                        p.setAdcode(pregion.getAdcode());
                        p.setId(pregion.getId());
                        p.setParent_id(pregion.getParent_id());
                        p.setRegion_name("全"+pregion.getRegion_name());
                        adapter.add(p);
                        adapter.addAll(regions);
                        list.setAdapter(adapter);
                        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                if(position==0){
                                    onselectaddress(pregion);
                                    return;
                                }
                                cregion=adapter.getItem(position);
                                back.setVisibility(View.VISIBLE);
                                select_box.setText("选择地区："+pregion.getRegion_name()+"/"+cregion.getRegion_name());
                                a1=pregion.getAdcode();
                                a2=cregion.getAdcode();
                                dbManager.getData(cregion.getId(), new DBManager.OnDataRecieveListener() {
                                    @Override
                                    public void recieveData(List<Region> regions) {
                                        adapter.clear();
                                        Region c=new Region();
                                        c.setRegion_name("全"+cregion.getRegion_name());
                                        c.setParent_id(cregion.getParent_id());
                                        c.setId(cregion.getId());
                                        c.setAdcode(cregion.getAdcode());
                                        adapter.add(c);
                                        adapter.addAll(regions);
                                        list.setAdapter(adapter);
                                        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                if(position==0){
                                                    onselectaddress(cregion);
                                                    return;
                                                }
                                                final Region dregion=adapter.getItem(position);
                                                a1=pregion.getAdcode();
                                                a2=cregion.getAdcode();
                                                a3=dregion.getAdcode();
                                                onselectaddress(dregion);
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pregion!=null&&cregion!=null){
                    cregion=null;
                    back.setVisibility(View.VISIBLE);
                    select_box.setText("选择地区："+pregion.getRegion_name());
                    a1=pregion.getAdcode();
                    dbManager.getData(pregion.getId(), new DBManager.OnDataRecieveListener() {
                        @Override
                        public void recieveData(List<Region> regions) {
                            adapter.clear();
                            Region p=new Region();
                            p.setAdcode(pregion.getAdcode());
                            p.setId(pregion.getId());
                            p.setParent_id(pregion.getParent_id());
                            p.setRegion_name("全"+pregion.getRegion_name());
                            adapter.add(p);
                            adapter.addAll(regions);
                            list.setAdapter(adapter);
                            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    if(position==0){
                                        onselectaddress(pregion);
                                        return;
                                    }
                                    cregion=adapter.getItem(position);
                                    back.setVisibility(View.VISIBLE);
                                    select_box.setText("选择地区："+pregion.getRegion_name()+"/"+cregion.getRegion_name());
                                    a1=pregion.getAdcode();
                                    a2=cregion.getAdcode();
                                    dbManager.getData(cregion.getId(), new DBManager.OnDataRecieveListener() {
                                        @Override
                                        public void recieveData(List<Region> regions) {
                                            adapter.clear();
                                            Region c=new Region();
                                            c.setRegion_name("全"+cregion.getRegion_name());
                                            c.setParent_id(cregion.getParent_id());
                                            c.setId(cregion.getId());
                                            c.setAdcode(cregion.getAdcode());
                                            adapter.add(c);
                                            adapter.addAll(regions);
                                            list.setAdapter(adapter);
                                            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                @Override
                                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                    if(position==0){
                                                        onselectaddress(cregion);
                                                        return;
                                                    }
                                                    final Region dregion=adapter.getItem(position);
                                                    a1=pregion.getAdcode();
                                                    a2=cregion.getAdcode();
                                                    a3=dregion.getAdcode();
                                                    onselectaddress(dregion);
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }else if(pregion!=null){
                    select_box.setText("选择地区：");
                    back.setVisibility(View.GONE);
                    dbManager.getData(-1, new DBManager.OnDataRecieveListener() {
                        @Override
                        public void recieveData(List<Region> regions) {
                            adapter.clear();
                            Region r=new Region();
                            r.setAdcode(0);
                            r.setRegion_name("全国");
                            adapter.add(r);
                            adapter.addAll(regions);
                            list.setAdapter(adapter);
                            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    if(position==0){
                                        onselectaddress(adapter.getItem(0));
                                        return;
                                    }
                                    pregion=adapter.getItem(position);
                                    back.setVisibility(View.VISIBLE);
                                    select_box.setText("选择地区："+pregion.getRegion_name());
                                    a1=pregion.getAdcode();
                                    dbManager.getData(pregion.getId(), new DBManager.OnDataRecieveListener() {
                                        @Override
                                        public void recieveData(List<Region> regions) {
                                            adapter.clear();
                                            Region p=new Region();
                                            p.setAdcode(pregion.getAdcode());
                                            p.setId(pregion.getId());
                                            p.setParent_id(pregion.getParent_id());
                                            p.setRegion_name("全"+pregion.getRegion_name());
                                            adapter.add(p);
                                            adapter.addAll(regions);
                                            list.setAdapter(adapter);
                                            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                @Override
                                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                    if(position==0){
                                                        onselectaddress(pregion);
                                                        return;
                                                    }
                                                    cregion=adapter.getItem(position);
                                                    back.setVisibility(View.VISIBLE);
                                                    select_box.setText("选择地区："+pregion.getRegion_name()+"/"+cregion.getRegion_name());
                                                    a1=pregion.getAdcode();
                                                    a2=cregion.getAdcode();
                                                    dbManager.getData(cregion.getId(), new DBManager.OnDataRecieveListener() {
                                                        @Override
                                                        public void recieveData(List<Region> regions) {
                                                            adapter.clear();
                                                            Region c=new Region();
                                                            c.setRegion_name("全"+cregion.getRegion_name());
                                                            c.setParent_id(cregion.getParent_id());
                                                            c.setId(cregion.getId());
                                                            c.setAdcode(cregion.getAdcode());
                                                            adapter.add(c);
                                                            adapter.addAll(regions);
                                                            list.setAdapter(adapter);
                                                            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                                @Override
                                                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                                    if(position==0){
                                                                        onselectaddress(cregion);
                                                                        return;
                                                                    }
                                                                    final Region dregion=adapter.getItem(position);
                                                                    a1=pregion.getAdcode();
                                                                    a2=cregion.getAdcode();
                                                                    a3=dregion.getAdcode();
                                                                    onselectaddress(dregion);
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }
        });
        dbManager.getData(-1, new DBManager.OnDataRecieveListener() {
            @Override
            public void recieveData(List<Region> regions) {
                Region r=new Region();
                r.setAdcode(0);
                r.setRegion_name("全国");
                select_region=r;
                adapter.add(r);
                adapter.addAll(regions);
                list.setAdapter(adapter);
            }
        });
    }

    @Override
    public void taskFailed(int code) {
        ((MainActivity)getActivity()).dismissLoading();
    }

    private void removeMarker(){
        for(Marker marker:markers){
            marker.remove();
        }
        markers.clear();
    }
    @Override
    public void taskSuccessful(String str, int code) {
        ((MainActivity)getActivity()).dismissLoading();
        JsonObject selectResultJson = new JsonParser().parse(str)
                .getAsJsonObject();
        int selectResultCode = selectResultJson.get("code").getAsInt();
        if(selectResultCode== CodeUtil.SUCCESS_CODE){
            removeMarker();
            Gson gson = new Gson();
            JsonArray releaseInfosJson = selectResultJson.get("releaseInfos")
                    .getAsJsonArray();
            List<MapReleaseModel> releaseInfos = gson
                    .fromJson(
                            releaseInfosJson,
                            new TypeToken<List<MapReleaseModel>>() {
                            }.getType());
            mcount=selectResultJson.get("count").getAsInt();
            showWindow();
            for(int i=0;i<releaseInfos.size();i++){
                final MapReleaseModel releaseInfo=releaseInfos.get(i);
                LatLng latLng = new LatLng(releaseInfo.getLat(),
                        releaseInfo.getLng());
                final MarkerOptions markerOptions = new MarkerOptions().position(latLng);
                Glide.with(getActivity()).load(NetUtil.getFullUrl(releaseInfo.getProduce_url()))
                        .asBitmap().into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        if(resource==null){
                            resource= BitmapFactory.decodeResource(getResources(),R.drawable.default_avator);
                        }
                        View item=getActivity().getLayoutInflater().inflate(R.layout.map_item,null);
                        ImageView head_img=(ImageView) item.findViewById(R.id.head_img);
                        resource=BitmapUtil.getRoundedCornerBitmap(resource,UIUtil.dip2px(getActivity(),48f));
                        head_img.setImageBitmap(resource);
                        TextView name=(TextView) item.findViewById(R.id.name);
                        name.setText(releaseInfo.getName());
                        markerOptions.icon(BitmapDescriptorFactory.fromView(item));
                        Marker m=(Marker) map.getBaiduMap().addOverlay(markerOptions);
                        markers.add(m);
                    }
                });
                markerOptions.draggable(false);
                markerOptions.anchor(0.5f,1.0f);
                Bundle bundle=new Bundle();
                bundle.putInt("id",releaseInfo.getId());
                markerOptions.extraInfo(bundle);
            }
        }
    }


    @Override
    public void taskStart(int code) {
        ((MainActivity)getActivity()).showLoading();
    }

    private void loadData(){
        HttpTask task=new HttpTask(getActivity());
        task.setTaskHandler(this);
        double distance = DistanceUtil.screenPixelToMetre(map.getMapView().getWidth(),map.getBaiduMap().getMapStatus().zoom,getActivity());
        if (distance > MAX_DISTANCE) {
            distance = MAX_DISTANCE;
        }
        SpatialContext geo = SpatialContext.GEO;
        Rectangle rectangle = geo.getDistCalc().calcBoxByDistFromPt(
                geo.makePoint(latLng.longitude, latLng.latitude),
                distance / 2 * DistanceUtils.DEGREES_TO_RADIANS, geo, null);
        QueryRangeModel queryRangeModel = new QueryRangeModel();
        queryRangeModel.setMaxLatitude(rectangle.getMaxY());
        queryRangeModel.setMinLatitude(rectangle.getMinY());
        queryRangeModel.setMaxLongitude(rectangle.getMaxX());
        queryRangeModel.setMinLongitude(rectangle.getMinX());
        queryRangeModel.setNum(10);
        dataJson = new JsonObject();
        Gson gson = new Gson();
        JsonObject queryDataJson = gson.toJsonTree(queryRangeModel)
                .getAsJsonObject();
        dataJson.add("queryData", queryDataJson);
        dataJson.addProperty("adcode",adcode);
        dataJson.addProperty("level", produce!=null?produce.getLevel():0);
        dataJson.addProperty("produce_id",produce!=null?produce.getId():0);
        dataJson.addProperty("purchaser_id", UserUtil.getUserModel(getActivity()).getId());
        task.execute(UrlUtil.QUREY_FARMER_RELEASE_MAIN,dataJson.toString());
    }
    private void showAlert(String region_name){
        AlertDialog dialog=new AlertDialog(getActivity()).builder();
        dialog.setTitle("提示");
        dialog.setMsg("当前定位城市为"+region_name+"，是否切换");
        dialog.setNegativeButton("取消", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        dialog.setPositiveButton("确定", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mylocation.edit().putInt("code",location.getInt("code",0))
                        .putString("city",location.getString("city","")).commit();

            }
        });
        dialog.show();
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        if(bdLocation!=null){
            this.loc=bdLocation;
            client.stop();
            client.unRegisterLocationListener(this);
            location_adcode=dbManager.getCityAdcode(bdLocation.getAdCode());
            location_city=bdLocation.getCity();
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(bdLocation.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(bdLocation.getDirection()).latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude()).build();

            // 设置定位数据
            map.getBaiduMap().setMyLocationData(locData);
            map.getBaiduMap().animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude()),15f));
            gethongbaocountbydistance();
        }
    }
    private void addMarker(double lat,double lng){
        MarkerOptions markerOptions=new MarkerOptions();
        markerOptions.draggable(false);
        markerOptions.animateType(MarkerOptions.MarkerAnimateType.jump);
        markerOptions.anchor(0.5f,1.0f);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.red));
        markerOptions.position(new LatLng(lat,lng));
        pointMarker=(Marker) map.getBaiduMap().addOverlay(markerOptions);
    }

    @Override
    public void onMapStatusChange(MapStatus mapStatus) {

    }

    @Override
    public void onMapStatusChangeFinish(MapStatus mapStatus) {
        if(mapStatus.target.latitude!=lat||mapStatus.target.longitude!=lng){
            centerImg.setVisibility(View.GONE);
            addMarker(mapStatus.target.latitude,mapStatus.target.longitude);
            latLng=mapStatus.target;
            lat=mapStatus.target.latitude;
            lng=mapStatus.target.longitude;
            geoCoder.reverseGeoCode(new ReverseGeoCodeOption().newVersion(1)
                    .location(latLng));
        }
    }
    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus) {
        if(pointMarker!=null){
            pointMarker.remove();
            pointMarker=null;
        }
        centerImg.setVisibility(View.VISIBLE);
        map.getBaiduMap().hideInfoWindow();
    }

    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus, int i) {
        if(pointMarker!=null){
            pointMarker.remove();
            pointMarker=null;
        }
        centerImg.setVisibility(View.VISIBLE);
        map.getBaiduMap().hideInfoWindow();
    }
}
