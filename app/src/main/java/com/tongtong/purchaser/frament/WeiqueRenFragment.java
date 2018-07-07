package com.tongtong.purchaser.frament;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.activity.BaseActivity;
import com.tongtong.purchaser.activity.OrderDetailsActivity;
import com.tongtong.purchaser.adapter.OrderAdapter;
import com.tongtong.purchaser.listener.NetChangeListener;
import com.tongtong.purchaser.model.OrderModel;
import com.tongtong.purchaser.utils.Constant;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.NetUtil;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;

/**
 * Created by Administrator on 2018-02-03.
 */

public class WeiqueRenFragment extends BaseFrament implements RecyclerArrayAdapter.OnLoadMoreListener,
        RecyclerArrayAdapter.OnItemClickListener,HttpTask.HttpTaskHandler,View.OnClickListener,
        NetChangeListener,SwipeRefreshLayout.OnRefreshListener{
    private EasyRecyclerView recyclerView;
    private OrderAdapter adapter;
    private int start_page=0;
    private boolean is_error;
    private View nonetwork;
    private boolean is_first_load=true;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.quanbu_fragment,container,false);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView=(EasyRecyclerView) view.findViewById(R.id.list);
        nonetwork=view.findViewById(R.id.network);
        view.findViewById(R.id.shezhi).setOnClickListener(this);
        view.findViewById(R.id.retry_btn).setOnClickListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter=new OrderAdapter(getActivity());
        recyclerView.setAdapterWithProgress(adapter);
        recyclerView.setRefreshListener(this);
        recyclerView.setRefreshingColorResources(R.color.colorPrimary);
        adapter.setOnItemClickListener(this);
        adapter.setNoMore(R.layout.no_more_layout);
        adapter.setMore(R.layout.autolistview_footer,this);
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
        if(getArguments().getInt("index")==1){
            Constant.can_load=true;
            if(NetUtil.getNetWorkState(getActivity())>=0){
                loadData();
            }else{
                is_error=true;
                recyclerView.setVisibility(View.GONE);
                nonetwork.setVisibility(View.VISIBLE);
            }
        }
    }
    @Override
    public void onRefresh() {
        start_page = 0;
        if (NetUtil.getNetWorkState(getActivity()) >= 0) {
            loadData();
        } else {
            recyclerView.setRefreshing(false);
            is_error = true;
            recyclerView.setVisibility(View.GONE);
            nonetwork.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(getArguments().getInt("index")==1){
            return;
        }
        if(isVisibleToUser&&is_first_load&&Constant.can_load){
            is_first_load=false;
            if(NetUtil.getNetWorkState(getActivity())>=0){
                loadData();
            }else{
                is_error=true;
                recyclerView.setVisibility(View.GONE);
                nonetwork.setVisibility(View.VISIBLE);
            }
        }
    }

    private void loadData(){
        if(NetUtil.getNetWorkState(getActivity())==-1){
            taskFailed(0);
            return;
        }
        HttpTask task=new HttpTask(getActivity());
        task.setTaskHandler(this);
        JsonObject dataJson = new JsonObject();
        dataJson.addProperty("token", UserUtil.getUserModel(getActivity()).getToken());
        dataJson.addProperty("user_id", UserUtil.getUserModel(getActivity()).getId());
        dataJson.addProperty("start_page",start_page);
        dataJson.addProperty("status",0);
        task.execute(UrlUtil.GET_ORDER_LIST,dataJson.toString());
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
        Intent intent=new Intent();
        intent.setClass(getActivity(), OrderDetailsActivity.class);
        intent.putExtra("order_id",adapter.getItem(position).getOrder_id());
        startActivity(intent);
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
            if (start_page == 0) {
                adapter.clear();
            }
            JsonArray orders=selectResultJson.get("orders").getAsJsonArray();
            for(int i=0;i<orders.size();i++){
                JsonObject order=orders.get(i).getAsJsonObject();
                OrderModel om=new OrderModel();
                om.setAdd_time(order.get("add_time").getAsString());
                om.setAmount(order.get("amount").getAsInt());
                om.setDingjin(order.get("dingjin").getAsInt());
                om.setIcon_url(order.get("icon_url").getAsString());
                om.setJinzhong(order.get("jinzhong").getAsInt());
                om.setOrder_id(order.get("order_id").getAsInt());
                om.setOrder_no(order.get("order_no").getAsString());
                om.setPrice(order.get("price").getAsDouble());
                om.setFarmer_name(order.get("farmer_name").getAsString());
                om.setPurchaser_phone(order.get("purchaser_phone").getAsString());
                om.setProduce_name(order.get("produce_name").getAsString());
                om.setPurchase_id(order.get("purchase_id").getAsInt());
                om.setPurchaser_name(order.get("purchaser_name").getAsString());
                om.setStatus(order.get("status").getAsInt());
                om.setFarmer_phone(order.get("farmer_phone").getAsString());
                om.setStatus_name(order.get("status_name").getAsString());
                om.setUnit(order.get("unit").getAsString());
                om.setTotal(order.get("total").getAsInt());
                adapter.add(om);
            }
            if(start_page==0){
                if(adapter.getCount() > 0 ){
                    recyclerView.showRecycler();
                }else if (adapter.getCount() == 0){
                    recyclerView.showEmpty();
                }
            }
            int next_page=selectResultJson.get("next_page").getAsInt();
            if(next_page==0){
                adapter.stopMore();
            }
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.shezhi){
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

    @Override
    public void onnetChange(boolean isAvalable) {
        if(getUserVisibleHint()==false){
            return;
        }
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