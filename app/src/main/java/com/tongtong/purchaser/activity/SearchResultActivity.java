package com.tongtong.purchaser.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.githang.statusbar.StatusBarCompat;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.adapter.PublishAdapter;
import com.tongtong.purchaser.application.MyApplication;
import com.tongtong.purchaser.listener.NetChangeListener;
import com.tongtong.purchaser.model.AddressModel;
import com.tongtong.purchaser.model.ProduceType;
import com.tongtong.purchaser.model.Region;
import com.tongtong.purchaser.model.ReleaseModel;
import com.tongtong.purchaser.utils.CodeUtil;
import com.tongtong.purchaser.utils.DBManager;
import com.tongtong.purchaser.utils.HistoryHelper;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.NetUtil;
import com.tongtong.purchaser.utils.ProduceTypesHelper;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.view.Mydivider;
import com.tongtong.purchaser.widget.CheckableRelativeLayout;
import com.tongtong.purchaser.widget.NoScrollGridView;
import com.tongtong.purchaser.widget.StyleableToast;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.lujun.androidtagview.TagContainerLayout;
import co.lujun.androidtagview.TagView;

/**
 * Created by Administrator on 2018-04-30.
 */

public class SearchResultActivity extends BaseActivity implements View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener,RecyclerArrayAdapter.OnItemClickListener,RecyclerArrayAdapter.OnLoadMoreListener,
        NetChangeListener,HttpTask.HttpTaskHandler,BDLocationListener{
    private View content;
    private TextView search;
    private ProduceType produce;
    private View tv_choose_type,tv_choose_guige,tv_choose_quyu,
    line_guige,anchor;
    private TextView tv_type_name,tv_guige_name,tv_quyu_name;
    private PopupWindow adcodePop,typePop,guigePop,sortPop;
    private DisplayMetrics dm;
    private DBManager dbManager;
    private int grid_height;
    private Region pregion,cregion;
    private ProduceType ptype,ctype;
    private int adcode;
    private int p1=-1,p2=-1,p3=-1;
    private int a1=-1,a2=-1,a3=-1;
    private TextView temp_select;
    private EasyRecyclerView recyclerView;
    private View nonetwork;
    private PublishAdapter publishAdapter;
    private int start_page=0;
    private boolean is_error;
    private LocationClient client;
    private Handler handler;
    private double lat,lng;
    private int location_adcode;
    private String location_city;
    private TextView location;
    private LinearLayout histoty_select;
    private TextView history_1,history_2;
    private Region select_region;
    private AddressModel address;
    private HashMap<Integer,Integer> selecte_type=new HashMap<>();
    private int order=0;
    private ImageView paixu_img;
    private MySortAdapter sortAdapter;
    private RecyclerArrayAdapter.ItemView header;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_result);
        StatusBarCompat.setStatusBarColor(this, Color.WHITE,true);
        content=findViewById(R.id.content);
        content.setOnClickListener(this);
        dbManager=new DBManager(this);
        client= MyApplication.getLocationClient();
        client.registerLocationListener(this);
        handler=new Handler();
        findViewById(R.id.back_bn).setOnClickListener(this);
        search=(TextView) findViewById(R.id.search);
        tv_choose_type=findViewById(R.id.tv_choose_type);
        tv_choose_guige=findViewById(R.id.tv_choose_guige);
        tv_choose_quyu=findViewById(R.id.tv_choose_quyu);
        line_guige=findViewById(R.id.line1);
        anchor=findViewById(R.id.anchor);
        grid_height= UIUtil.dip2px(this,35f);
        tv_type_name=(TextView) findViewById(R.id.tv_type_name);
        tv_guige_name=(TextView) findViewById(R.id.tv_guige_name);
        tv_quyu_name=(TextView) findViewById(R.id.tv_quyu_name);
        paixu_img=(ImageView) findViewById(R.id.img);
        temp_select=tv_type_name;
        tv_choose_type.setOnClickListener(this);
        tv_choose_quyu.setOnClickListener(this);
        tv_choose_guige.setOnClickListener(this);
        findViewById(R.id.paixu).setOnClickListener(this);
        recyclerView=(EasyRecyclerView) findViewById(R.id.list);
        nonetwork=findViewById(R.id.network);
        findViewById(R.id.shezhi).setOnClickListener(this);
        findViewById(R.id.retry_btn).setOnClickListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        Mydivider mydivider=new Mydivider(ContextCompat.getColor(this,R.color.aliwx_divider_color),1);
        mydivider.setDrawHeaderFooter(false);
        mydivider.setDrawLastItem(true);
        recyclerView.addItemDecoration(mydivider);
        publishAdapter=new PublishAdapter(this);
        recyclerView.setAdapterWithProgress(publishAdapter);
        recyclerView.setRefreshListener(this);
        recyclerView.setRefreshingColorResources(R.color.colorPrimary);
        publishAdapter.setOnItemClickListener(this);
        publishAdapter.setNoMore(R.layout.no_more_layout);
        publishAdapter.setMore(R.layout.autolistview_footer,this);
        BaseActivity.netChangeListeners.add(this);
        publishAdapter.setError(R.layout.error_layout, new RecyclerArrayAdapter.OnErrorListener() {
            @Override
            public void onErrorShow() {
                publishAdapter.resumeMore();
            }
            @Override
            public void onErrorClick() {
                publishAdapter.resumeMore();
            }
        });
        dm=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        if(getIntent().hasExtra("produce")){
            produce=(ProduceType) getIntent().getSerializableExtra("produce");
            search.setText(produce.getName());
        }else{
            search.setText(getIntent().getStringExtra("keyword"));
            produce= ProduceTypesHelper.getRecmend(this,getIntent().getStringExtra("keyword"));
        }
        if(produce!=null){
            if(produce.getLevel()==3){
                tv_type_name.setText(produce.getName());
                tv_type_name.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));
                tv_type_name.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.icon_down_green,0);
                tv_choose_guige.setVisibility(View.VISIBLE);
                line_guige.setVisibility(View.VISIBLE);
                ProduceType pt=ProduceTypesHelper.getParentProduce(this,produce.getId());
                loadGuige(pt.getId());
            }else if(produce.getLevel()==2){
                tv_type_name.setText(produce.getName());
                tv_type_name.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));
                tv_type_name.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.icon_down_green,0);
                tv_choose_guige.setVisibility(View.VISIBLE);
                line_guige.setVisibility(View.VISIBLE);
                loadGuige(produce.getId());
            }else if(produce.getLevel()==1){
                tv_type_name.setText(produce.getName());
                tv_type_name.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));
                tv_type_name.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.icon_down_green,0);
                tv_choose_guige.setVisibility(View.GONE);
                line_guige.setVisibility(View.GONE);
            }
        }else{
            tv_type_name.setText("全部分类");
            tv_type_name.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));
            tv_type_name.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.icon_down_green,0);
            tv_choose_guige.setVisibility(View.GONE);
            line_guige.setVisibility(View.GONE);
        }
        publishAdapter.setError(R.layout.error_layout, new RecyclerArrayAdapter.OnErrorListener() {
            @Override
            public void onErrorShow() {
                publishAdapter.resumeMore();
            }
            @Override
            public void onErrorClick() {
                publishAdapter.resumeMore();
            }
        });
        header=new RecyclerArrayAdapter.ItemView() {
            @Override
            public View onCreateView(ViewGroup parent) {
                View view=View.inflate(SearchResultActivity.this,R.layout.search_empty_header,null);
                view.findViewById(R.id.btn_add_card).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent();
                        intent.setClass(SearchResultActivity.this,TypeSelectActivity.class);
                        startActivityForResult(intent,1);
                    }
                });
                return view;
            }
            @Override
            public void onBindView(View headerView) {

            }
        };
        client.start();
    }
    private void showSortPop(){
        if(sortPop==null){
            View container=View.inflate(this,R.layout.normal_list_view,null);
            int[] outlocation=new int[2];
            anchor.getLocationOnScreen(outlocation);
            sortPop=new PopupWindow(container, ViewGroup.LayoutParams.MATCH_PARENT, dm.heightPixels-outlocation[1]-anchor.getHeight(),true);
            sortPop.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this,R.color.city_search_bg)));
            sortPop.setOutsideTouchable(true);
            sortPop.setTouchable(true);
            sortPop.setAnimationStyle(R.style.anim_menu_bottombar);
            sortPop.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    paixu_img.setImageResource(sortAdapter.getItem(order).select_resId);
                }
            });
            initSortData(container);
        }
        sortPop.showAsDropDown(anchor);
    }
    private class SortItem{
        public String title;
        public int normal_resId;
        public int select_resId;
    }
    private void initSortData(View container){
        ListView list=(ListView) container.findViewById(android.R.id.list);
        list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        sortAdapter=new MySortAdapter(this);
            SortItem si=new SortItem();
        si.normal_resId=R.drawable.paixu_normal;
        si.select_resId=R.drawable.paixu_selected;
        si.title="综合排序";
        sortAdapter.add(si);
        si=new SortItem();
        si.normal_resId=R.drawable.juli_normal;
        si.select_resId=R.drawable.juli_selected;
        si.title="距离由近到远";
        sortAdapter.add(si);
        si=new SortItem();
        si.normal_resId=R.drawable.jiage_normal;
        si.select_resId=R.drawable.jiage_selected;
        si.title="价格由低到高";
        sortAdapter.add(si);
        list.setAdapter(sortAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                order=position;
                sortPop.dismiss();
                onRefresh();
            }
        });
    }
    private class MySortAdapter extends ArrayAdapter<SortItem>{
        public MySortAdapter(Context context){
            super(context,0);
        }
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(convertView==null){
                convertView=getLayoutInflater().inflate(R.layout.checked_text_view,null);
            }
            TextView title=(TextView) convertView.findViewById(R.id.title);
            ImageView img=(ImageView) convertView.findViewById(R.id.img);
            ImageView indicator=(ImageView) convertView.findViewById(R.id.indicator);
            SortItem si=getItem(position);
            title.setText(si.title);
            if(order==position){
                img.setImageResource(si.select_resId);
                indicator.setVisibility(View.VISIBLE);
                title.setTextColor(ContextCompat.getColor(SearchResultActivity.this,R.color.colorPrimary));
            }else{
                indicator.setVisibility(View.GONE);
                img.setImageResource(si.normal_resId);
                title.setTextColor(ContextCompat.getColor(SearchResultActivity.this,R.color.aliwx_common_text_color2));
            }
            return convertView;
        }
    }
    private String getGuigeId(){
        StringBuffer sb=new StringBuffer();
        for(Map.Entry<Integer,Integer> data:selecte_type.entrySet()){
            sb.append(data.getValue()).append(",");
        }
        if(sb.length()>0){
            sb=sb.deleteCharAt(sb.length()-1);
        }
        return sb.toString();
    }
    private void initGuigePop(JsonArray items){
        if(guigePop==null){
            View container=View.inflate(this,R.layout.produce_type_layout,null);
            int[] outlocation=new int[2];
            anchor.getLocationOnScreen(outlocation);
            guigePop=new PopupWindow(container, ViewGroup.LayoutParams.MATCH_PARENT, dm.heightPixels-outlocation[1]-anchor.getHeight(),true);
            guigePop.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this,R.color.city_search_bg)));
            guigePop.setOutsideTouchable(true);
            guigePop.setTouchable(true);
            guigePop.setAnimationStyle(R.style.anim_menu_bottombar);
            guigePop.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    tv_guige_name.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.icon_down_green,0);
                }
            });
            initGuigeData(container,items);
        }
    }
    private void initGuigeData(View container,JsonArray items){
        final LinearLayout root=(LinearLayout) container.findViewById(R.id.container);
        Button confirm_bn=(Button) container.findViewById(R.id.confirm_bn);
        confirm_bn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guigePop.dismiss();
                onRefresh();
            }
        });
        for(int i=0;i<items.size();i++){
            JsonObject item=items.get(i).getAsJsonObject();
                View produce_item=View.inflate(this,R.layout.produce_item,null);
                TextView title=(TextView) produce_item.findViewById(R.id.title);
                final NoScrollGridView tags=(NoScrollGridView) produce_item.findViewById(R.id.tags);
                title.setText(item.get("name").getAsString());
            tags.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
            final JsonArray list=item.get("list").getAsJsonArray();
           final TagAdapter tagAdapter=new TagAdapter(this,tags);
            for(int j=0;j<list.size();j++){
                JsonObject ta=list.get(j).getAsJsonObject();
                TagItem tg=new TagItem();
                tg.id=ta.get("id").getAsInt();
                tg.name=ta.get("name").getAsString();
                tagAdapter.add(tg);
            }
            tags.setAdapter(tagAdapter);
            tags.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    boolean checked=tags.isItemChecked(position);
                    if(!checked){
                        selecte_type.remove(position);
                    }else{
                        if(selecte_type.size()>=5){
                            StyleableToast.info(SearchResultActivity.this,"最多选择5个标签");
                            return;
                        }
                        selecte_type.put(position,tagAdapter.getItem(position).id);
                    }
                    tags.setItemChecked(position,checked);
                    tagAdapter.notifyDataSetChanged();
                }
            });
            root.addView(produce_item);
        }
    }
    private class TagItem{
        private int id;
        private String name;
    }
    private class TagAdapter extends ArrayAdapter<TagItem>{
        private NoScrollGridView list;
        public TagAdapter(Context context,NoScrollGridView list){
            super(context,0);
            this.list=list;
        }
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(convertView==null){
                convertView=getLayoutInflater().inflate(R.layout.guige_item,null);
            }
            TagItem tg=getItem(position);
            boolean item=list.isItemChecked(position);
            View tag=convertView.findViewById(R.id.tag);
            TextView title=(TextView) convertView.findViewById(R.id.title);
            if(item){
                tag.setVisibility(View.VISIBLE);
                convertView.setBackgroundResource(R.drawable.edit_bg13);
                title.setTextColor(ContextCompat.getColor(SearchResultActivity.this,R.color.colorPrimary));
            }else{
                tag.setVisibility(View.GONE);
                convertView.setBackgroundResource(R.drawable.edit_bg2);
                title.setTextColor(ContextCompat.getColor(SearchResultActivity.this,R.color.aliwx_common_text_color));
            }
            ((TextView)convertView.findViewById(R.id.title)).setText(tg.name);
            return convertView;
        }
    }
    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        if(bdLocation==null){
            client.restart();
            return;
        }
        client.stop();
        lat=bdLocation.getLatitude();
        lng=bdLocation.getLongitude();
        location_adcode=dbManager.getCityAdcode(bdLocation.getAdCode());
        location_city=bdLocation.getCity();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(NetUtil.getNetWorkState(SearchResultActivity.this)>=0){
                    loadData();
                }else{
                    is_error=true;
                    recyclerView.setVisibility(View.GONE);
                    nonetwork.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void taskStart(int code) {

    }

    @Override
    public void taskFailed(int code) {
        if(start_page>0){
            publishAdapter.pauseMore();
        }else{
            recyclerView.setVisibility(View.GONE);
            nonetwork.setVisibility(View.VISIBLE);
        }
        is_error=true;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        BaseActivity.netChangeListeners.remove(this);
        client.unRegisterLocationListener(this);
    }
    @Override
    public void taskSuccessful(String str, int code) {
        is_error=false;
        publishAdapter.removeHeader(header);
        JsonObject selectResultJson = new JsonParser().parse(str)
                .getAsJsonObject();
        int selectResultCode = selectResultJson.get("code").getAsInt();
        if(selectResultCode== CodeUtil.SUCCESS_CODE){
            recyclerView.setRefreshing(false);
            if (start_page == 0) {
                publishAdapter.clear();
            }
            JsonArray orders=selectResultJson.get("items").getAsJsonArray();
            Gson gson=new Gson();
            List<ReleaseModel> items = gson.fromJson(orders,
                    new TypeToken<List<ReleaseModel>>() {
                    }.getType());
            publishAdapter.addAll(items);
            if(start_page==0){
                if(publishAdapter.getCount() > 0 ){
                    recyclerView.showRecycler();
                }else if (publishAdapter.getCount() == 0){
                    recyclerView.showEmpty();
                }
            }
            int next_page=selectResultJson.get("next_page").getAsInt();
            if(next_page==0){
                publishAdapter.stopMore();
            }
        }else if(selectResultCode==108){
            publishAdapter.addHeader(header);
            recyclerView.setRefreshing(false);
            if (start_page == 0) {
                publishAdapter.clear();
            }
            JsonArray orders=selectResultJson.get("items").getAsJsonArray();
            Gson gson=new Gson();
            List<ReleaseModel> items = gson.fromJson(orders,
                    new TypeToken<List<ReleaseModel>>() {
                    }.getType());
            publishAdapter.addAll(items);
            if(start_page==0){
                if(publishAdapter.getCount() > 0 ){
                    recyclerView.showRecycler();
                }else if (publishAdapter.getCount() == 0){
                    recyclerView.showEmpty();
                }
            }
            int next_page=selectResultJson.get("next_page").getAsInt();
            if(next_page==0){
                publishAdapter.stopMore();
            }
        }
    }

    private void loadGuige(int produce_id){
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
                if(selectResultCode==CodeUtil.SUCCESS_CODE){
                    JsonArray items=selectResultJson.get("items").getAsJsonArray();
                    if(items.size()>0){
                        tv_choose_guige.setVisibility(View.VISIBLE);
                        line_guige.setVisibility(View.VISIBLE);
                        initGuigePop(items);
                    }else{
                        tv_choose_guige.setVisibility(View.GONE);
                        line_guige.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void taskFailed(int code) {

            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("produce_id",produce_id);
        task.execute(UrlUtil.GET_GUIGE,object.toString());
    }
    @Override
    public void onRefresh() {
        start_page = 0;
        recyclerView.setRefreshing(true);
        if (NetUtil.getNetWorkState(this) >= 0) {
            loadData();
        } else {
            recyclerView.setRefreshing(false);
            is_error = true;
            recyclerView.setVisibility(View.GONE);
            nonetwork.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoadMore() {
        if(!is_error){
            start_page++;
        }
        loadData();
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

    @Override
    public void onItemClick(int position) {

    }
    private void loadData(){
        if(NetUtil.getNetWorkState(this)==-1){
            taskFailed(0);
            return;
        }
        HttpTask task=new HttpTask(this);
        task.setTaskHandler(this);
        JsonObject dataJson = new JsonObject();
        dataJson.addProperty("level", produce!=null?produce.getLevel():0);
        dataJson.addProperty("produce_id",produce!=null?produce.getId():0);
        dataJson.addProperty("start_page",start_page);
        dataJson.addProperty("adcode",adcode);
        dataJson.addProperty("lat",lat);
        dataJson.addProperty("lng",lng);
        dataJson.addProperty("specific_id",getGuigeId());
        dataJson.addProperty("order",order);
        dataJson.addProperty("keyword",search.getText().toString());
        dataJson.addProperty("purchaser_id", UserUtil.getUserModel(this).getId());
        task.execute(UrlUtil.SEARCH_RELEASE,dataJson.toString());
    }
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.content){
            Intent data=new Intent();
            data.putExtra("keyword",search.getText().toString());
            setResult(RESULT_OK,data);
            finish();
        }else if(v.getId()==R.id.back_bn){
            if(SearchTypeActivity.getInstance()!=null){
                SearchTypeActivity.getInstance().finish();
            }
            onBackPressed();
        }else if(v.getId()==R.id.tv_choose_type){
            showTypePop();
        }else if(v.getId()==R.id.tv_choose_quyu){
            showAdcodePop();
        }else if(v.getId()==R.id.tv_choose_guige){
            if(guigePop!=null){
                if(temp_select!=tv_guige_name){
                    temp_select.setTextColor(ContextCompat.getColor(this,R.color.aliwx_common_text_color));
                    temp_select.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.icon_down,0);
                }
                temp_select=tv_guige_name;
                tv_guige_name.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.icon_up,0);
                tv_guige_name.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));
                guigePop.showAsDropDown(anchor);
            }
        }else  if(v.getId()==R.id.shezhi){
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
        }else if(v.getId()==R.id.retry_btn){
            is_error = false;
            recyclerView.setVisibility(View.VISIBLE);
            nonetwork.setVisibility(View.GONE);
            loadData();
        }else if(v.getId()==R.id.paixu){
            showSortPop();
        }
    }
    private void showAdcodePop(){
        if(temp_select!=tv_quyu_name){
            temp_select.setTextColor(ContextCompat.getColor(this,R.color.aliwx_common_text_color));
            temp_select.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.icon_down,0);
        }
        temp_select=tv_quyu_name;
        if(adcodePop==null){
            View container=View.inflate(this,R.layout.city_address_list,null);
            int[] outlocation=new int[2];
            anchor.getLocationOnScreen(outlocation);
            adcodePop=new PopupWindow(container, ViewGroup.LayoutParams.MATCH_PARENT, dm.heightPixels-outlocation[1]-anchor.getHeight(),true);
            adcodePop.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this,R.color.city_search_bg)));
            adcodePop.setOutsideTouchable(true);
            adcodePop.setTouchable(true);
            adcodePop.setAnimationStyle(R.style.anim_menu_bottombar);
            adcodePop.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    tv_quyu_name.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.icon_down_green,0);
                }
            });
            initData(container);
        }
        List<Region> regions= HistoryHelper.getHistory(this);
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
        location.setText(location_city);
        tv_quyu_name.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.icon_up,0);
        tv_quyu_name.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));
        adcodePop.showAsDropDown(anchor);
    }
    private void showTypePop(){
        if(temp_select!=tv_type_name){
            temp_select.setTextColor(ContextCompat.getColor(this,R.color.aliwx_common_text_color));
            temp_select.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.icon_down,0);
        }
        temp_select=tv_type_name;
        if(typePop==null){
            View container=View.inflate(this,R.layout.city_grid_list,null);
            int[] outlocation=new int[2];
            anchor.getLocationOnScreen(outlocation);
            typePop=new PopupWindow(container, ViewGroup.LayoutParams.MATCH_PARENT, dm.heightPixels-outlocation[1]-anchor.getHeight(),true);
            typePop.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this,R.color.city_search_bg)));
            typePop.setOutsideTouchable(true);
            typePop.setTouchable(true);
            typePop.setAnimationStyle(R.style.anim_menu_bottombar);
            typePop.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    tv_type_name.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.icon_down_green,0);
                }
            });
            initTypeData(container);
        }
        tv_type_name.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.icon_up,0);
        tv_type_name.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));
        typePop.showAsDropDown(anchor);
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
            final MyTypeAdapter adapter=new MyTypeAdapter(this);
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
                    ProduceTypesHelper.getSubProduce(ptype.getId(), SearchResultActivity.this,new ProduceTypesHelper.OnDataRecieveListener() {
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
                                    ProduceTypesHelper.getSubProduce(ctype.getId(),SearchResultActivity.this, new ProduceTypesHelper.OnDataRecieveListener() {
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
                        ProduceTypesHelper.getSubProduce(ptype.getId(),SearchResultActivity.this, new ProduceTypesHelper.OnDataRecieveListener() {
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
                                        ProduceTypesHelper.getSubProduce(ctype.getId(),SearchResultActivity.this, new ProduceTypesHelper.OnDataRecieveListener() {
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
                        ProduceTypesHelper.getParentType(SearchResultActivity.this, new ProduceTypesHelper.OnDataRecieveListener() {
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
                                        ProduceTypesHelper.getSubProduce(ptype.getId(),SearchResultActivity.this, new ProduceTypesHelper.OnDataRecieveListener() {
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
                                                        ProduceTypesHelper.getSubProduce(ctype.getId(), SearchResultActivity.this,new ProduceTypesHelper.OnDataRecieveListener() {
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
                ProduceTypesHelper.getParentType(this, new ProduceTypesHelper.OnDataRecieveListener() {
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
                ProduceTypesHelper.getParentType(this, new ProduceTypesHelper.OnDataRecieveListener() {
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
                    ptype=ProduceTypesHelper.getParentProduce(SearchResultActivity.this,produce.getId());
                    select_box.setText("当前选择："+ptype.getName());
                    back.setVisibility(View.VISIBLE);
                    p1=ptype.getId();
                    p2=produce.getId();
                    p3=-1;
                    ProduceTypesHelper.getSubProduce(ptype.getId(), SearchResultActivity.this,new ProduceTypesHelper.OnDataRecieveListener() {
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
                                    ProduceTypesHelper.getSubProduce(ctype.getId(),SearchResultActivity.this, new ProduceTypesHelper.OnDataRecieveListener() {
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
                    ctype=ProduceTypesHelper.getParentProduce(SearchResultActivity.this,produce.getId());
                    ptype=ProduceTypesHelper.getParentProduce(SearchResultActivity.this,ctype.getId());
                    back.setVisibility(View.VISIBLE);
                    p1=ptype.getId();
                    p2=ctype.getId();
                    p3=produce.getId();
                    select_box.setText("当前选择："+ptype.getName()+"/"+ctype.getName());
                    ProduceTypesHelper.getSubProduce(ctype.getId(), SearchResultActivity.this,new ProduceTypesHelper.OnDataRecieveListener() {
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
    private class MyAdapter extends ArrayAdapter<Region> {
        public MyAdapter(Context ctx){
            super(ctx,0);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(convertView==null){
                convertView=getLayoutInflater().inflate(R.layout.common_grid_item,null);
            }
            TextView title=(TextView)convertView.findViewById(R.id.title);
            title.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,grid_height));
            Region region=getItem(position);
            title.setText(region.getRegion_name());
            if((region.getAdcode()==a1||region.getAdcode()==a2||region.getAdcode()==a3)&&(position!=0)){
                title.setTextColor(ContextCompat.getColor(SearchResultActivity.this,R.color.colorPrimary));
            }else if(position==0){
                if(a3==-1&&(address!=null&&address.getDistrict()==region.getId())&&(select_region!=null&&select_region.getAdcode()==a2)){
                    title.setTextColor(ContextCompat.getColor(SearchResultActivity.this,R.color.colorPrimary));
                }else if(a2==-1&&(address!=null&&address.getDistrict()==region.getId())&&a3==-1&&(select_region!=null&&select_region.getAdcode()==a1)){
                    title.setTextColor(ContextCompat.getColor(SearchResultActivity.this,R.color.colorPrimary));
                }else if(a1==-1&&a2==-1&&a3==-1&&region.getId()==0&&select_region!=null&&select_region.getAdcode()==0){
                    title.setTextColor(ContextCompat.getColor(SearchResultActivity.this,R.color.colorPrimary));
                }else{
                    title.setTextColor(ContextCompat.getColor(SearchResultActivity.this,R.color.aliwx_common_text_color));
                }
            }else{
                title.setTextColor(ContextCompat.getColor(SearchResultActivity.this,R.color.aliwx_common_text_color));
            }
            return convertView;
        }
    }
    private class MyTypeAdapter extends ArrayAdapter<ProduceType> {
        public MyTypeAdapter(Context ctx){
            super(ctx,0);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(convertView==null){
                convertView=getLayoutInflater().inflate(R.layout.common_grid_item,null);
            }
            TextView title=(TextView)convertView.findViewById(R.id.title);
            title.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,grid_height));
            ProduceType region=getItem(position);
            title.setText(region.getName());
            if((region.getId()==p1||region.getId()==p2||region.getId()==p3)&&(position!=0)){
                title.setTextColor(ContextCompat.getColor(SearchResultActivity.this,R.color.colorPrimary));
            }else if(position==0){
                if(p3==-1&&region.getLevel()==2&&(produce!=null&&produce.getId()==p2)){
                    title.setTextColor(ContextCompat.getColor(SearchResultActivity.this,R.color.colorPrimary));
                }else if(p2==-1&&p3==-1&&region.getLevel()==1&&(produce!=null&&produce.getId()==p1)){
                    title.setTextColor(ContextCompat.getColor(SearchResultActivity.this,R.color.colorPrimary));
                }else if(p1==-1&&p2==-1&&p3==-1&&region.getLevel()==0&&produce!=null&&produce.getId()==0){
                    title.setTextColor(ContextCompat.getColor(SearchResultActivity.this,R.color.colorPrimary));
                }else{
                    title.setTextColor(ContextCompat.getColor(SearchResultActivity.this,R.color.aliwx_common_text_color));
                }
            }else{
                title.setTextColor(ContextCompat.getColor(SearchResultActivity.this,R.color.aliwx_common_text_color));
            }
            return convertView;
        }
    }
    private void onselectaddress(Region region){
        this.select_region=region;
        adcodePop.dismiss();
        tv_quyu_name.setText(region.getRegion_name());
        adcode=region.getAdcode();
        if(adcode==0){
            a1=-1;
            a2=-1;
            a3=-1;
            address=new AddressModel();
        }else{
            address = dbManager.getshengshiqu(adcode);
            if(address.getCity()==-1){
                a1=adcode;
                a2=-1;
                a3=-1;
            }else if(address.getProvince()==-1){
                a2=adcode;
                a3=-1;
            }else{
                a3=adcode;
            }
            HistoryHelper.addHistory(SearchResultActivity.this,select_region);
        }
        onRefresh();
    }
    private void onselecttype(ProduceType produce){
        this.produce=produce;
        typePop.dismiss();
        tv_type_name.setText(produce.getName());
        if(produce.getLevel()==1){
            p1=produce.getId();
            p2=-1;
            p3=-1;
            tv_choose_guige.setVisibility(View.GONE);
            line_guige.setVisibility(View.GONE);
        }else if(produce.getLevel()==2){
            p2=produce.getId();
            p3=-1;
            guigePop=null;
            loadGuige(produce.getId());
            ProduceTypesHelper.addHistotyProduce(this,produce);
        }else if(produce.getLevel()==3){
            p3=produce.getId();
            ProduceType pt=ProduceTypesHelper.getParentProduce(this,p3);
            loadGuige(pt.getId());
            //保存搜索历史
            ProduceTypesHelper.addHistotyProduce(this,produce);
        }else{
            p1=-1;
            p2=-1;
            p3=-1;
            tv_choose_guige.setVisibility(View.GONE);
            line_guige.setVisibility(View.GONE);
        }
        search.setText(null);
        search.setHint("搜索蔬菜");
        onRefresh();
    }
    private void initData(View container){
        final TextView select_box=(TextView) container.findViewById(R.id.select_box);
        final TextView back=(TextView) container.findViewById(R.id.back);
        location=(TextView) container.findViewById(R.id.location);
        histoty_select=(LinearLayout) container.findViewById(R.id.histoty_select);
        history_1=(TextView) container.findViewById(R.id.history_1);
        history_2=(TextView) container.findViewById(R.id.history_2);
        history_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_quyu_name.setText(history_1.getText().toString());
                adcode=(int)history_1.getTag();
                adcodePop.dismiss();
                Region region=dbManager.getRegionByAdcode(adcode);
                HistoryHelper.addHistory(SearchResultActivity.this,region);
                onRefresh();
            }
        });
        history_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_quyu_name.setText(history_2.getText().toString());
                adcode=(int)history_2.getTag();
                adcodePop.dismiss();
                Region region=dbManager.getRegionByAdcode(adcode);
                HistoryHelper.addHistory(SearchResultActivity.this,region);
                onRefresh();
            }
        });
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_quyu_name.setText(location_city);
                adcode=location_adcode;
                adcodePop.dismiss();
                Region region=dbManager.getRegionByAdcode(adcode);
                HistoryHelper.addHistory(SearchResultActivity.this,region);
                onRefresh();
            }
        });

        back.setVisibility(View.GONE);
        select_box.setText("选择地区：");
        final NoScrollGridView list=(NoScrollGridView)container.findViewById(android.R.id.list);
        list.setHorizontalSpacing(1);
        list.setVerticalSpacing(1);
        final MyAdapter adapter=new MyAdapter(this);
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
}
