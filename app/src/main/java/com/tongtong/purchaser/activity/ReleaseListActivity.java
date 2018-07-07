package com.tongtong.purchaser.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.adapter.PublishAdapter;
import com.tongtong.purchaser.listener.NetChangeListener;
import com.tongtong.purchaser.model.ProduceType;
import com.tongtong.purchaser.model.ReleaseModel;
import com.tongtong.purchaser.utils.CodeUtil;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.NetUtil;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.view.Mydivider;

import java.util.List;

/**
 * Created by Administrator on 2018-05-25.
 */

public class ReleaseListActivity extends BaseActivity implements HttpTask.HttpTaskHandler,
        View.OnClickListener,RecyclerArrayAdapter.OnLoadMoreListener,NetChangeListener,
        OnGetGeoCoderResultListener{
    private int start_page;
    private EasyRecyclerView recyclerView;
    private PublishAdapter adapter;
    private boolean is_error;
    private View nonetwork;
    private TextView info;
    private GeoCoder geocoder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.release_list_layout);
        ((TextView)findViewById(R.id.title_text)).setText("附近卖菜信息");
        findViewById(R.id.back_bn).setOnClickListener(this);
        nonetwork=findViewById(R.id.network);
        recyclerView=(EasyRecyclerView) findViewById(R.id.list);
        Mydivider mydivider=new Mydivider(ContextCompat.getColor(this,R.color.aliwx_divider_color),1);
        mydivider.setDrawHeaderFooter(false);
        mydivider.setDrawLastItem(true);
        recyclerView.addItemDecoration(mydivider);
        LinearLayoutManager manager=new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        adapter=new PublishAdapter(this);
        BaseActivity.netChangeListeners.add(this);
        adapter.setNoMore(R.layout.no_more_layout);
        adapter.setMore(R.layout.autolistview_footer,this);
        adapter.setError(R.layout.error_layout, new RecyclerArrayAdapter.OnErrorListener() {
            @Override
            public void onErrorShow() {
                adapter.resumeMore();
            }
            @Override
            public void onErrorClick() {
                adapter.resumeMore();
            }
        });
        adapter.addHeader(new RecyclerArrayAdapter.ItemView() {
            @Override
            public View onCreateView(ViewGroup parent) {
                View view=getLayoutInflater().inflate(R.layout.address_info,null);
                info=(TextView) view.findViewById(R.id.info);
                return view;
            }

            @Override
            public void onBindView(View headerView) {

            }
        });
        geocoder=GeoCoder.newInstance();
        geocoder.setOnGetGeoCodeResultListener(this);
        geocoder.reverseGeoCode(new ReverseGeoCodeOption().location(new LatLng(
                getIntent().getDoubleExtra("lat1",0),getIntent().getDoubleExtra("lng1",0)
        )).newVersion(1));
        recyclerView.setAdapterWithProgress(adapter);
        if(NetUtil.getNetWorkState(this)>=0){
            loadData();
        }else{
            is_error=true;
            recyclerView.setVisibility(View.GONE);
            nonetwork.setVisibility(View.VISIBLE);
        }
    }

    private SpannableStringBuilder getAddressString(String address,int count){
        SpannableStringBuilder builder=new SpannableStringBuilder();
        builder.append(address);
        ForegroundColorSpan colorSpan=new ForegroundColorSpan(0xff45C173);
        builder.setSpan(colorSpan,0,builder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.append("附近");
        ProduceType produce=(ProduceType) getIntent().getSerializableExtra("produce");
        if(produce!=null&&produce.getId()>0){
            builder.append(produce.getName());
            colorSpan=new ForegroundColorSpan(0xff45C173);
            builder.setSpan(colorSpan,builder.length()-produce.getName().length(),builder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        builder.append("约有");
        String con=String.valueOf(count);
        builder.append(con);
        colorSpan=new ForegroundColorSpan(0xff45C173);
        builder.setSpan(colorSpan,builder.length()-con.length(),builder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        RelativeSizeSpan sizeSpan=new RelativeSizeSpan(1.2f);
        StyleSpan styleSpan=new StyleSpan(Typeface.BOLD);
        builder.setSpan(sizeSpan,builder.length()-con.length(),builder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.setSpan(styleSpan,builder.length()-con.length(),builder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.append("条卖菜信息");
        return builder;
    }
    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
        if(reverseGeoCodeResult==null&&reverseGeoCodeResult.error!= SearchResult.ERRORNO.NO_ERROR){
            return;
        }
        String address=reverseGeoCodeResult.getAddress();
        if(reverseGeoCodeResult.getPoiList()!=null&&reverseGeoCodeResult.getPoiList().size()>0){
            PoiInfo poi=reverseGeoCodeResult.getPoiList().get(0);
            address=poi.city+poi.name;
        }
        info.setText(getAddressString(address,getIntent().getIntExtra("count",0)));
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }

    @Override
    public void onLoadMore() {
        if(!is_error){
            start_page++;
        }
        loadData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BaseActivity.netChangeListeners.remove(this);
        geocoder.destroy();
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.back_bn){
            onBackPressed();
        }else if(view.getId()==R.id.shezhi){
            Intent intent = null;
            /**
             * 判断手机系统的版本！如果API大于10 就是3.0+
             * 因为3.0以上的版本的设置和3.0以下的设置不一样，调用的方法不同
             */
            if (android.os.Build.VERSION.SDK_INT > 10) {
                intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
            } else {
                intent = new Intent();
                ComponentName component = new ComponentName(
                        "com.android.settings",
                        "com.android.settings.WirelessSettings");
                intent.setComponent(component);
                intent.setAction("android.intent.action.VIEW");
            }
            startActivity(intent);
        }else if(view.getId()==R.id.retry_btn){
            is_error = false;
            recyclerView.setVisibility(View.VISIBLE);
            nonetwork.setVisibility(View.GONE);
            loadData();
        }
    }

    private void loadData(){
        HttpTask task=new HttpTask(this);
        task.setTaskHandler(this);
        JsonObject object=new JsonParser().parse(getIntent().getStringExtra("dataJson")).getAsJsonObject();
        object.addProperty("lat",getIntent().getDoubleExtra("lat",0));
        object.addProperty("lng",getIntent().getDoubleExtra("lng",0));
        object.addProperty("start_page",start_page);
        task.execute(UrlUtil.QUREY_FARMER_RELEASE_MAIN_LIST,object.toString());
    }
    @Override
    public void taskStart(int code) {

    }
    @Override
    public void taskFailed(int code) {
        if(start_page>0){
            adapter.pauseMore();
        }else{
            recyclerView.setVisibility(View.GONE);
            nonetwork.setVisibility(View.VISIBLE);
        }
        is_error=true;
    }
    @Override
    public void taskSuccessful(String str, int code) {
        JsonObject refreshResultJson = new JsonParser().parse(str)
                .getAsJsonObject();
        int refreshResultCode = refreshResultJson.get("code").getAsInt();
        if(refreshResultCode== CodeUtil.SUCCESS_CODE){
            Gson gson = new Gson();
            JsonArray releaseInfosJson = refreshResultJson.get("releaseInfos")
                    .getAsJsonArray();
            List<ReleaseModel> items = gson.fromJson(releaseInfosJson,
                    new TypeToken<List<ReleaseModel>>() {
                    }.getType());
            adapter.addAll(items);
            int next_page=refreshResultJson.get("next_page").getAsInt();
            if(next_page==0){
                adapter.stopMore();
            }
            if(start_page==0&&items.size()==0){
                recyclerView.showRecycler();
            }
        }
    }

    @Override
    public void onnetChange(boolean isAvalable) {
        if(isAvalable){
            if(is_error){
                is_error=false;
                nonetwork.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                if(start_page==0){
                    loadData();
                }else{
                    onLoadMore();
                }
            }

        }
    }
}
