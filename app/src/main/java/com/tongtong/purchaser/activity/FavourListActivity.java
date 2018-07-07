package com.tongtong.purchaser.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.adapter.PublishAdapter;
import com.tongtong.purchaser.frament.WodeFragment;
import com.tongtong.purchaser.listener.NetChangeListener;
import com.tongtong.purchaser.model.ReleaseModel;
import com.tongtong.purchaser.utils.CodeUtil;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.NetUtil;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.view.Mydivider;
import com.tongtong.purchaser.widget.AlertDialog;

import java.util.List;

/**
 * Created by zxy on 2018/6/13.
 */

public class FavourListActivity extends BaseActivity implements View.OnClickListener,
        NetChangeListener,RecyclerArrayAdapter.OnLoadMoreListener,SwipeRefreshLayout.OnRefreshListener,
        HttpTask.HttpTaskHandler,RecyclerArrayAdapter.OnItemLongClickListener {
    private int start_page;
    private EasyRecyclerView recyclerView;
    private boolean is_error;
    private View nonetwork;
    private PublishAdapter adapter;
    private SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.release_list_layout);
        sp=getSharedPreferences("location", Context.MODE_PRIVATE);
        ((TextView)findViewById(R.id.title_text)).setText("我的收藏");
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
        recyclerView.setAdapterWithProgress(adapter);
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
        View empty=View.inflate(this,R.layout.empty_layout,null);
        ((TextView)empty.findViewById(R.id.desc)).setText("暂时没有收藏记录");
        recyclerView.setEmptyView(empty);
        recyclerView.setRefreshListener(this);
        adapter.setOnItemLongClickListener(this);
        recyclerView.setRefreshingColorResources(R.color.colorPrimary);
        if(NetUtil.getNetWorkState(this)>=0){
            loadData();
        }else{
            is_error=true;
            recyclerView.setVisibility(View.GONE);
            nonetwork.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onItemLongClick(int position) {
        showAlert(position);
        return true;
    }

    private void showAlert(final int position){
        AlertDialog dialog=new AlertDialog(this).builder();
        dialog.setTitle("提示");
        dialog.setMsg("确定取消收藏？");
        dialog.setNegativeButton("取消", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        dialog.setPositiveButton("确定", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReleaseModel releaseModel=adapter.getItem(position);
                deleteFavour(releaseModel.getFid(),releaseModel.getId(),position);
            }
        });
        dialog.show();
    }
    private void deleteFavour(int favour_id,int id,final int position){
        HttpTask task=new HttpTask(this);
        task.setTaskHandler(new HttpTask.HttpTaskHandler() {
            @Override
            public void taskStart(int code) {

            }
            @Override
            public void taskSuccessful(String str, int code) {
                JsonObject object=new JsonParser().parse(str).getAsJsonObject();
                if(object.get("code").getAsInt()== CodeUtil.SUCCESS_CODE){
                    if(WodeFragment.getInstance()!=null){
                        WodeFragment.getInstance().getData();
                    }
                    adapter.remove(position);
                    if(adapter.getCount()==0){
                        recyclerView.showEmpty();
                    }
                }
            }

            @Override
            public void taskFailed(int code) {

            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("favour_id",favour_id);
        object.addProperty("rel_id",id);
        task.execute(UrlUtil.DELETE_FAVOUR,object.toString());
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
        dataJson.addProperty("purchaser_id", UserUtil.getUserModel(this).getId());
        dataJson.addProperty("token",UserUtil.getUserModel(this).getToken());
        dataJson.addProperty("start_page",start_page);
        dataJson.addProperty("lat",sp.getString("lat","0"));
        dataJson.addProperty("lng",sp.getString("lng","0"));
        task.execute(UrlUtil.GET_FAVOURITES_LIST,dataJson.toString());
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
        }
    }
    @Override
    public void onRefresh() {
        start_page=0;
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
    public void onLoadMore() {
        if(!is_error){
            start_page++;
        }
        loadData();
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
            }
            Gson gson=new Gson();
            List<ReleaseModel> items = gson.fromJson(orders,
                    new TypeToken<List<ReleaseModel>>() {
                    }.getType());
            adapter.addAll(items);
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
    @Override
    public void onDestroy() {
        super.onDestroy();
        BaseActivity.netChangeListeners.remove(this);
    }
}
