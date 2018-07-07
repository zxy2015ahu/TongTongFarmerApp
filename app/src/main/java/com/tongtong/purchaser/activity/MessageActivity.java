package com.tongtong.purchaser.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.adapter.MessageAdapter;
import com.tongtong.purchaser.listener.NetChangeListener;
import com.tongtong.purchaser.model.MessageModel;
import com.tongtong.purchaser.utils.Constant;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.NetUtil;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.widget.AlertDialog;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by zxy on 2018/3/31.
 */

public class MessageActivity extends BaseActivity implements View.OnClickListener,
        RecyclerArrayAdapter.OnLoadMoreListener,SwipeRefreshLayout.OnRefreshListener,NetChangeListener,HttpTask.HttpTaskHandler,
        MessageAdapter.OnCheckListener{
    private EasyRecyclerView recyclerView;
    private MessageAdapter adapter;
    private View nonetwork;
    private int start_page=0;
    private boolean is_error;
    private int msg_type;
    private View bottom;
    private TextView right_text;
    private boolean show_check=false;
    private TextView yidu,delete_msg;
    private Map<Integer,Integer> checkids=new HashMap<>();
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.msg_details_layout);
        findViewById(R.id.right_bn).setOnClickListener(this);
        right_text=(TextView) findViewById(R.id.right_text);
        ((TextView)findViewById(R.id.title_text)).setText(getIntent().getStringExtra("title"));
        findViewById(R.id.back_bn).setOnClickListener(this);
        msg_type=getIntent().getIntExtra("msg_type",1);
        recyclerView=(EasyRecyclerView) findViewById(R.id.list);
        yidu=(TextView) findViewById(R.id.yidu);
        delete_msg=(TextView) findViewById(R.id.delete_msg);
        yidu.setOnClickListener(this);
        delete_msg.setOnClickListener(this);
        nonetwork=findViewById(R.id.network);
        bottom=findViewById(R.id.bottom);
        findViewById(R.id.shezhi).setOnClickListener(this);
        findViewById(R.id.retry_btn).setOnClickListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter=new MessageAdapter(this,msg_type);
        adapter.setOnCheckListener(this);
        recyclerView.setAdapterWithProgress(adapter);
        adapter.setNoMore(R.layout.no_more_layout);
        View empty=View.inflate(this,R.layout.empty_layout,null);
        ((TextView)empty.findViewById(R.id.desc)).setText("暂时没有消息哦");
        recyclerView.setEmptyView(empty);
        adapter.setMore(R.layout.autolistview_footer,this);
        recyclerView.setRefreshListener(this);
        recyclerView.setRefreshingColorResources(R.color.colorPrimary);
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
        dataJson.addProperty("msg_type",msg_type);
        task.execute(UrlUtil.GET_MSG_LIST,dataJson.toString());
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
            for(int i=0;i<orders.size();i++){
                JsonObject order=orders.get(i).getAsJsonObject();
                MessageModel am=new MessageModel();
                am.setAdd_time(order.get("add_time").getAsString());
                am.setContent(order.get("content").getAsString());
                am.setId(order.get("id").getAsInt());
                am.setRel_id(order.get("rel_id").getAsInt());
                am.setTitle(order.get("title").getAsString());
                am.setIs_new(order.get("is_new").getAsInt());
                am.setLink(order.get("link").getAsString());
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
    @Override
    public void onDestroy() {
        super.onDestroy();
        BaseActivity.netChangeListeners.remove(this);
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
            if(!show_check){
                wancheng();
            }else{
                bianji();
            }
        }else if(v.getId()==R.id.yidu){
            yidu();
        }else if(v.getId()==R.id.delete_msg){
            AlertDialog dialog=new AlertDialog(this).builder();
            dialog.setTitle("提示");
            dialog.setMsg("确定删除这些消息吗？");
            dialog.setNegativeButton("取消", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            dialog.setPositiveButton("确定", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    delete();
                }
            });
            dialog.show();
        }
    }
    private void bianji(){
        right_text.setText("编辑");
        bottom.setVisibility(View.GONE);
        adapter.showCheck(false);
        show_check=false;
        resetCheck();
    }
    private void wancheng(){
        adapter.showCheck(true);
        right_text.setText("完成");
        bottom.setVisibility(View.VISIBLE);
        show_check=true;
        yidu.setText("全部已读");
        delete_msg.setText("全部删除");
    }
    private void resetCheck(){
        int count=adapter.getCount();
        for(int i=0;i<count;i++){
            adapter.getItem(i).setIs_check(false);
        }
        checkids.clear();
    }
    private String getCheckIds(){
        StringBuffer sb=new StringBuffer();
        if(msg_type==1){
            if(checkids.size()>0){
                for(Map.Entry<Integer,Integer> data:checkids.entrySet()){
                    sb.append(data.getValue()).append(",");
                }
            }else{
                int count=adapter.getCount();
                for(int i=0;i<count;i++){
                    sb.append(adapter.getItem(i).getId()).append(",");
                }
            }
        }else{
            for(Map.Entry<Integer,Integer> data:checkids.entrySet()){
                sb.append(data.getValue()).append(",");
            }
        }
        if(sb.length()>0){
            sb=sb.deleteCharAt(sb.length()-1);
        }
        return sb.toString();
    }

    @Override
    public void checkDo(int id,boolean check,int position) {
        if(check){
            checkids.put(position,id);
        }else{
            checkids.remove(position);
        }
        if(checkids.size()>0){
            yidu.setText("已读");
            delete_msg.setText("删除");
        }else{
            yidu.setText("全部已读");
            delete_msg.setText("全部删除");
        }
    }
    private void yidu(){
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
                if(verification(selectResultCode)){
                    if(checkids.size()>0){
                        for(Map.Entry<Integer,Integer> data:checkids.entrySet()){
                            adapter.getItem(data.getKey()).setIs_new(0);
                        }
                    }else{
                        int count=adapter.getCount();
                        for(int i=0;i<count;i++){
                            adapter.getItem(i).setIs_new(0);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    bianji();
                    Intent intent=new Intent();
                    intent.setAction(Constant.MSG_REFRESH_BY_LOADING);
                    sendBroadcast(intent);
                }
            }
            @Override
            public void taskFailed(int code) {

            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("ids",getCheckIds());
        object.addProperty("pruchaser_id",UserUtil.getUserModel(this).getId());
        object.addProperty("msg_type",msg_type);
        object.addProperty("token",UserUtil.getUserModel(this).getToken());
        task.execute(UrlUtil.UPDATE_YIDU,object.toString());
    }
    private void delete(){
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
                if(verification(selectResultCode)){
                    if(checkids.size()>0){
                        for(Map.Entry<Integer,Integer> data:checkids.entrySet()){
                            adapter.remove(data.getKey());
                        }
                    }else{
                        int count=adapter.getCount();
                        for(int i=0;i<count;i++){
                            adapter.clear();
                        }
                    }
                    adapter.notifyDataSetChanged();
                    bianji();
                    Intent intent=new Intent();
                    intent.setAction(Constant.MSG_REFRESH_BY_LOADING);
                    sendBroadcast(intent);
                }
            }
            @Override
            public void taskFailed(int code) {

            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("ids",getCheckIds());
        object.addProperty("pruchaser_id",UserUtil.getUserModel(this).getId());
        object.addProperty("msg_type",msg_type);
        object.addProperty("token",UserUtil.getUserModel(this).getToken());
        task.execute(UrlUtil.DELETE_MSG,object.toString());
    }
}
