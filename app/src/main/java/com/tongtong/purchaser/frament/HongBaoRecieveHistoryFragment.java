package com.tongtong.purchaser.frament;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.tu.loadingdialog.LoadingDailog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.activity.BaseActivity;
import com.tongtong.purchaser.activity.HongBaoDetailsActivity;
import com.tongtong.purchaser.adapter.HongBaoRecordAdapter;
import com.tongtong.purchaser.listener.NetChangeListener;
import com.tongtong.purchaser.model.HongBaoModel;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.NetUtil;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.widget.StyleableToast;

/**
 * Created by zxy on 2018/3/10.
 */

public class HongBaoRecieveHistoryFragment extends BaseFrament implements View.OnClickListener,HttpTask.HttpTaskHandler,
        RecyclerArrayAdapter.OnLoadMoreListener,NetChangeListener,RecyclerArrayAdapter.OnItemClickListener{
    private EasyRecyclerView recyclerView;
    private View nonetwork;
    private int start_page=0;
    private boolean is_error;
    private HongBaoRecordAdapter adapter;
    private LoadingDailog loading;
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
        adapter=new HongBaoRecordAdapter(getActivity());
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
        if(NetUtil.getNetWorkState(getActivity())>=0){
            loadData();
        }else{
            is_error=true;
            recyclerView.setVisibility(View.GONE);
            nonetwork.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(int position) {
        HongBaoModel model=adapter.getItem(position);
        opendetails(model.getHb_id());
    }
    private void opendetails(int id){
        HttpTask task=new HttpTask(getActivity());
        task.setTaskHandler(new HttpTask.HttpTaskHandler() {
            @Override
            public void taskStart(int code) {
                LoadingDailog.Builder loadBuilder=new LoadingDailog.Builder(getActivity())
                        .setMessage("加载中...")
                        .setCancelable(true)
                        .setCancelOutside(true);
                loading=loadBuilder.create();
                loading.show();
            }
            @Override
            public void taskSuccessful(String str, int code) {
                loading.dismiss();
                Intent intent=new Intent();
                intent.setClass(getActivity(), HongBaoDetailsActivity.class);
                intent.putExtra("result",str);
                startActivity(intent);
            }
            @Override
            public void taskFailed(int code) {
                loading.dismiss();
                StyleableToast.error(getActivity(),"打开详情失败");
            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("token", UserUtil.getUserModel(getActivity()).getToken());
        object.addProperty("purchaser_id",UserUtil.getUserModel(getActivity()).getId());
        object.addProperty("id",id);
        task.execute(UrlUtil.ROB_RP,object.toString());
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
        is_error=false;
        final JsonObject selectResultJson = new JsonParser().parse(str)
                .getAsJsonObject();
        int selectResultCode = selectResultJson.get("code").getAsInt();
        if(verification(selectResultCode)){
            if(adapter.getHeaderCount()==0){
                adapter.addHeader(new RecyclerArrayAdapter.ItemView() {
                    @Override
                    public View onCreateView(ViewGroup parent) {
                        View header=View.inflate(getActivity(),R.layout.rp_received_record_list_header,null);
                        final ImageView iv_avatar=(ImageView) header.findViewById(R.id.iv_avatar);
                        Glide.with(getActivity()).load(UrlUtil.IMG_SERVER_URL+selectResultJson.get("headUrl").getAsString())
                                .asBitmap().centerCrop().placeholder(R.drawable.rp_avatar).into(new BitmapImageViewTarget(iv_avatar){
                            @Override
                            protected void setResource(Bitmap resource) {
                                RoundedBitmapDrawable circularBitmapDrawable =
                                        RoundedBitmapDrawableFactory.create(getResources(), resource);
                                circularBitmapDrawable.setCircular(true);
                                iv_avatar.setImageDrawable(circularBitmapDrawable);
                            }
                        });
                        ((TextView)header.findViewById(R.id.tv_username)).setText(selectResultJson.get("name").getAsString());
                        ((TextView)header.findViewById(R.id.tv_received_money_amount)).setText("￥"+selectResultJson.get("total_amount").getAsDouble());
                        ((TextView)header.findViewById(R.id.tv_received_count)).setText(""+selectResultJson.get("total_size").getAsInt());
                        ((TextView)header.findViewById(R.id.tv_best_count)).setText(""+selectResultJson.get("best_size").getAsInt());
                        return header;
                    }

                    @Override
                    public void onBindView(View headerView) {

                    }
                });
            }
            JsonArray qiang_items=selectResultJson.get("qiang_items").getAsJsonArray();
            int next_page=selectResultJson.get("next_page").getAsInt();
            if (next_page == 0) {
                //dividerItemDecoration.setDrawLastItem(false);
                adapter.stopMore();
            }else{
                // dividerItemDecoration.setDrawLastItem(true);
            }
            for(int i=0;i<qiang_items.size();i++){
                JsonObject item=qiang_items.get(i).getAsJsonObject();
                HongBaoModel hm=new HongBaoModel();
                hm.setAmount(item.get("amount").getAsDouble());
                hm.setIs_best(item.get("is_best").getAsInt());
                hm.setTime(item.get("add_time").getAsString());
                hm.setHb_id(item.get("hb_id").getAsInt());
                if(item.get("type").getAsInt()==0){
                    hm.setName(item.get("fname").getAsString());
                    hm.setHeadUrl(item.get("fheadUrl").getAsString());
                }else{
                    hm.setName(item.get("pname").getAsString());
                    hm.setHeadUrl(item.get("pheadUrl").getAsString());
                }
                adapter.add(hm);
            }
            if(start_page==0){
                recyclerView.showRecycler();
            }

        }
    }

    @Override
    public void taskStart(int code) {

    }
    private void loadData(){
        if(NetUtil.getNetWorkState(getActivity())==-1){
            taskFailed(0);
            return;
        }
        //dividerItemDecoration.setDrawLastItem(false);
        HttpTask task=new HttpTask(getActivity());
        task.setTaskHandler(this);
        JsonObject dataJson = new JsonObject();
        dataJson.addProperty("token", UserUtil.getUserModel(getActivity()).getToken());
        dataJson.addProperty("purchaser_id", UserUtil.getUserModel(getActivity()).getId());
        dataJson.addProperty("start_page",start_page);
        task.execute(UrlUtil.GET_RP_RECORD_LIST,dataJson.toString());
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
