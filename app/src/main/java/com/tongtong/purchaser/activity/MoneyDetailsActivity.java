package com.tongtong.purchaser.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.flyco.dialog.entity.DialogMenuItem;
import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.NormalDialog;
import com.flyco.dialog.widget.NormalListDialog;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.adapter.MoneyAdapter;
import com.tongtong.purchaser.listener.NetChangeListener;
import com.tongtong.purchaser.model.AccountModel;
import com.tongtong.purchaser.utils.Constant;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.NetUtil;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.view.Mydivider;
import com.tongtong.purchaser.view.YearMonthPickerDialog;
import com.zaaach.toprightmenu.MenuItem;
import com.zaaach.toprightmenu.TopRightMenu;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zxy on 2018/3/13.
 */

public class MoneyDetailsActivity extends BaseActivity implements View.OnClickListener,
        RecyclerArrayAdapter.OnItemClickListener,RecyclerArrayAdapter.OnLoadMoreListener,SwipeRefreshLayout.OnRefreshListener,
        NetChangeListener,HttpTask.HttpTaskHandler,TopRightMenu.OnMenuItemClickListener{
    private EasyRecyclerView recyclerView;
    private MoneyAdapter adapter;
    private int start_page=0;
    private boolean is_error;
    private View nonetwork;
    private int cur_year,cur_month;
    private TopRightMenu mTopRightMenu;
    private String category="";
    private int year=0,month=0;
    private YearMonthPickerDialog pickerDialog;
    private NormalListDialog mdialog;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.money_details_layout);
        findViewById(R.id.right_bn).setOnClickListener(this);
        mTopRightMenu = new TopRightMenu(this);
        List<MenuItem> menuItems = new ArrayList<>();
        menuItems.add(new MenuItem(R.drawable.yufen, "按月份筛选"));
        menuItems.add(new MenuItem(R.drawable.leixing, "按类型筛选"));
        mTopRightMenu.showIcon(true);
        mTopRightMenu.setWidth(300);
        mTopRightMenu.setHeight(RecyclerView.LayoutParams.WRAP_CONTENT);
        mTopRightMenu.addMenuList(menuItems);
        mTopRightMenu.dimBackground(true);
        mTopRightMenu.needAnimationStyle(true);
        mTopRightMenu.setAnimationStyle(R.style.TRM_ANIM_STYLE);
        mTopRightMenu.setOnMenuItemClickListener(this);
        ((ImageView)findViewById(R.id.phone)).setImageResource(R.drawable.common_forward_normal);
        ((TextView)findViewById(R.id.title_text)).setText("零钱明细");
        findViewById(R.id.back_bn).setOnClickListener(this);
        recyclerView=(EasyRecyclerView) findViewById(R.id.list);
        nonetwork=findViewById(R.id.network);
        findViewById(R.id.shezhi).setOnClickListener(this);
        findViewById(R.id.retry_btn).setOnClickListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter=new MoneyAdapter(this);
        recyclerView.setAdapterWithProgress(adapter);
        adapter.setOnItemClickListener(this);
        adapter.setNoMore(R.layout.no_more_layout);
        adapter.setMore(R.layout.autolistview_footer,this);
        recyclerView.setRefreshListener(this);
        Mydivider decoration=new Mydivider(ContextCompat.getColor(this,R.color.line_color), UIUtil.dip2px(this,1));
        decoration.setDrawLastItem(true);
        decoration.setDrawHeaderFooter(false);
        recyclerView.addItemDecoration(decoration);
       final StickyRecyclerHeadersDecoration headerDecoration=new StickyRecyclerHeadersDecoration(adapter);
        recyclerView.addItemDecoration(headerDecoration);
        recyclerView.setRefreshingColorResources(R.color.colorPrimary);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                headerDecoration.invalidateHeaders();
            }
        });
        BaseActivity.netChangeListeners.add(this);
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
            if(NetUtil.getNetWorkState(this)>=0){
                loadData();
            }else{
                is_error=true;
                recyclerView.setVisibility(View.GONE);
                nonetwork.setVisibility(View.VISIBLE);
            }
    }



    @Override
    public void onMenuItemClick(int position) {
        if(position==1){
            if(mdialog==null){
                ArrayList<DialogMenuItem> menus=new ArrayList<>();
                menus.add(new DialogMenuItem("全部",R.drawable.quanbu_all));
                menus.add(new DialogMenuItem("红包",R.drawable.hongbao_all));
                menus.add(new DialogMenuItem("订单",R.drawable.dingdan_all));
                mdialog=new NormalListDialog(this,menus);
                mdialog.title("请选择交易类型");
                mdialog.itemTextColor(ContextCompat.getColor(this,R.color.aliwx_common_text_color));
                mdialog.titleBgColor(ContextCompat.getColor(this,R.color.colorPrimary));
                mdialog.setOnOperItemClickL(new OnOperItemClickL() {
                    @Override
                    public void onOperItemClick(AdapterView<?> parent, View view, int position, long id) {
                                if (position == 0) {
                                    category = "";
                                } else if (position == 1) {
                                    category = Constant.CATEGORY_RP;
                                } else if (position == 2) {
                                    category = Constant.CATEGORY_ORDER;
                                }
                                mdialog.dismiss();
                                recyclerView.setRefreshing(true);
                                onRefresh();
                    }
                });
            }
            mdialog.show();
//            if(mdialog==null) {
//                mdialog = new MaterialDialog.Builder(this)
//                        .title("请选择交易类型")
//                        .items("全部", "红包", "订单")
//                        .alwaysCallSingleChoiceCallback()
//                        .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
//                            @Override
//                            public boolean onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
//                                dialog.setSelectedIndex(position);
//                                if (position == 0) {
//                                    category = "";
//                                } else if (position == 1) {
//                                    category = Constant.CATEGORY_RP;
//                                } else if (position == 2) {
//                                    category = Constant.CATEGORY_ORDER;
//                                }
//                                recyclerView.setRefreshing(true);
//                                onRefresh();
//                                return true;
//                            }
//                        }).build();
//            }
//            mdialog.show();
        }else if(position==0){
            if(pickerDialog==null) {
                pickerDialog = new YearMonthPickerDialog(this, new YearMonthPickerDialog.OnDateSetListener() {
                    @Override
                    public void onYearMonthSet(int year, int month) {
                        MoneyDetailsActivity.this.month = month+1;
                        MoneyDetailsActivity.this.year = year;
                        recyclerView.setRefreshing(true);
                        onRefresh();
                    }
                });
            }
            pickerDialog.show();
        }
    }
    private void loadData(){
        if(NetUtil.getNetWorkState(this)==-1){
            taskFailed(0);
            return;
        }
        //dividerItemDecoration.setDrawLastItem(false);
        HttpTask task=new HttpTask(this);
        task.setTaskHandler(this);
        JsonObject dataJson = new JsonObject();
        dataJson.addProperty("token", UserUtil.getUserModel(this).getToken());
        dataJson.addProperty("purchaser_id", UserUtil.getUserModel(this).getId());
        dataJson.addProperty("start_page",start_page);
        dataJson.addProperty("cur_year",cur_year);
        dataJson.addProperty("cur_month",cur_month);
        dataJson.addProperty("category",category);
        dataJson.addProperty("year",year);
        dataJson.addProperty("month",month);
        task.execute(UrlUtil.GET_MINGXI_LIST,dataJson.toString());
    }
    @Override
    public void onRefresh() {
        start_page=0;
        cur_month=0;
        cur_year=0;
        if(NetUtil.getNetWorkState(this)>=0){
            loadData();
        }else{
            recyclerView.setRefreshing(false);
            is_error=true;
            recyclerView.setVisibility(View.GONE);
            nonetwork.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.back_bn){
            onBackPressed();
        }else if(v.getId()==R.id.shezhi){
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
        }else if(v.getId()==R.id.right_bn){
            mTopRightMenu.showAsDropDown(v, -220, 0);
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
    public void onDestroy() {
        super.onDestroy();
        BaseActivity.netChangeListeners.remove(this);
    }
    @Override
    public void onItemClick(int position) {

    }
    @Override
    public void taskFailed(int code) {
        //recyclerView.showError();
        if(start_page>0){
            adapter.pauseMore();
        }else{
            recyclerView.setVisibility(View.GONE);
            nonetwork.setVisibility(View.VISIBLE);
        }
        is_error=true;
    }
    @Override
    public void taskStart(int code) {

    }
    @Override
    public void taskSuccessful(String str, int code) {
        is_error=false;
        JsonObject selectResultJson = new JsonParser().parse(str)
                .getAsJsonObject();
        int selectResultCode = selectResultJson.get("code").getAsInt();
        if(selectResultJson.has("cur_year")){
            cur_year=selectResultJson.get("cur_year").getAsInt();
        }
        if(selectResultJson.has("cur_month")){
            cur_month=selectResultJson.get("cur_month").getAsInt();
        }
        if(verification(selectResultCode)){
            recyclerView.setRefreshing(false);
            if(start_page==0){
                adapter.clear();
            }
            JsonArray orders=selectResultJson.get("items").getAsJsonArray();
            int next_page=selectResultJson.get("next_page").getAsInt();
            if (next_page == 0) {
                //dividerItemDecoration.setDrawLastItem(false);
                adapter.stopMore();
            }else{
                // dividerItemDecoration.setDrawLastItem(true);
            }
            for(int i=0;i<orders.size();i++){
                JsonObject order=orders.get(i).getAsJsonObject();
                AccountModel am=new AccountModel();
                am.setAdd_time(order.get("add_time").getAsString());
                am.setAmount(order.get("amount").getAsDouble());
                am.setCategory(order.get("category").getAsString());
                am.setDescription(order.get("description").getAsString());
                am.setRel_id(order.get("rel_id").getAsInt());
                am.setSign(order.get("sign").getAsInt());
                am.setIncome(order.get("income").getAsDouble());
                am.setOutcome(order.get("outcome").getAsDouble());
                am.setHeader_id(order.get("header_id").getAsLong());
                am.setYear(order.get("year").getAsInt());
                am.setMonth(order.get("month").getAsInt());
                adapter.add(am);
            }
            if(start_page==0){
                if(adapter.getCount()>0){
                    recyclerView.showRecycler();
                }else if(adapter.getCount()==0){
                    recyclerView.showEmpty();
                }
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
