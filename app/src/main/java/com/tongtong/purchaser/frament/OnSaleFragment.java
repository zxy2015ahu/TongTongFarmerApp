package com.tongtong.purchaser.frament;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.activity.BaseActivity;
import com.tongtong.purchaser.activity.FarmerInfoActivity;
import com.tongtong.purchaser.adapter.PublishAdapter;
import com.tongtong.purchaser.listener.NetChangeListener;
import com.tongtong.purchaser.model.ReleaseModel;
import com.tongtong.purchaser.utils.CodeUtil;
import com.tongtong.purchaser.utils.Constant;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.NetUtil;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.view.Mydivider;

import java.util.List;

/**
 * Created by zxy on 2018/3/19.
 */

public class OnSaleFragment extends HeaderViewPagerFragment implements View.OnClickListener,
        RecyclerArrayAdapter.OnItemClickListener,RecyclerArrayAdapter.OnLoadMoreListener,NetChangeListener,
        HttpTask.HttpTaskHandler{
    private EasyRecyclerView recyclerView;
    private View nonetwork;
    private PublishAdapter adapter;
    private boolean is_error;
    private int start_page=0;
    private boolean is_first_load=true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.easy_recycler_fragment,container,false);
        recyclerView=(EasyRecyclerView) view.findViewById(R.id.list);
        return view;
    }

    @Override
    public View getScrollableView() {
        return recyclerView.getRecyclerView();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        nonetwork=view.findViewById(R.id.network);
        view.findViewById(R.id.shezhi).setOnClickListener(this);
        view.findViewById(R.id.retry_btn).setOnClickListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        Mydivider mydivider=new Mydivider(ContextCompat.getColor(getActivity(),R.color.aliwx_divider_color),1);
        mydivider.setDrawHeaderFooter(false);
        mydivider.setDrawLastItem(true);
        recyclerView.addItemDecoration(mydivider);
        adapter=new PublishAdapter(getActivity());
        recyclerView.setAdapterWithProgress(adapter);
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
        JsonObject dataJson = new JsonObject();
        dataJson.addProperty("token", UserUtil.getUserModel(getActivity()).getToken());
        dataJson.addProperty("farmerId", ((FarmerInfoActivity)getActivity()).farmer.getId());
        dataJson.addProperty("start_page",start_page);
        HttpTask httpTask = new HttpTask(getActivity());
        httpTask.setTaskHandler(this);
        httpTask.execute(UrlUtil.SELECT_SALE_RELEASE, dataJson.toString());
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        BaseActivity.netChangeListeners.remove(this);
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
    public void taskStart(int code) {

    }

    @Override
    public void taskSuccessful(String str, int code) {
        JsonObject refreshResultJson = new JsonParser().parse(str)
                .getAsJsonObject();
        int refreshResultCode = refreshResultJson.get("code").getAsInt();
        if(refreshResultCode== CodeUtil.SUCCESS_CODE){
            Gson gson = new Gson();
            JsonArray releaseInfosJson = refreshResultJson.get("data")
                    .getAsJsonArray();
            List<ReleaseModel> items = gson.fromJson(releaseInfosJson,
                    new TypeToken<List<ReleaseModel>>() {
                    }.getType());
            if(start_page==0){
                if(adapter.getCount() > 0 ){
                    recyclerView.showRecycler();
                }else if (adapter.getCount() == 0){
                    recyclerView.showEmpty();
                }
            }
            adapter.addAll(items);
            int next_page=refreshResultJson.get("next_page").getAsInt();
            if(next_page==0){
                adapter.stopMore();
            }
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
    public void onItemClick(int position) {

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
