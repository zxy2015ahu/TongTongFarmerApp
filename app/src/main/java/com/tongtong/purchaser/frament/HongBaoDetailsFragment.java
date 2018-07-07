package com.tongtong.purchaser.frament;

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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.activity.MyHongBaohistory;
import com.tongtong.purchaser.adapter.HongBaoAdapter;
import com.tongtong.purchaser.model.HongBaoModel;
import com.tongtong.purchaser.utils.CodeUtil;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.NetUtil;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

/**
 * Created by zxy on 2018/3/10.
 */

public class HongBaoDetailsFragment extends BaseFrament {
    private EasyRecyclerView recyclerView;
    private HongBaoAdapter adapter;
    private JsonObject details;
    private int page=0;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.quanbu_fragment,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        details=new JsonParser().parse(getActivity().getIntent().getStringExtra("result")).getAsJsonObject();
        recyclerView = (EasyRecyclerView) view.findViewById(R.id.list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter=new HongBaoAdapter(getActivity());
        adapter.addHeader(new RecyclerArrayAdapter.ItemView() {
            @Override
            public View onCreateView(ViewGroup parent) {
                View header=View.inflate(getActivity(),R.layout.rp_details_list_header,null);
                final ImageView iv_avatar=(ImageView) header.findViewById(R.id.iv_avatar);
                Glide.with(getActivity()).load(NetUtil.getFullUrl(details.get("headUrl").getAsString()))
                        .asBitmap().centerCrop().placeholder(R.drawable.rp_avatar).into(new BitmapImageViewTarget(iv_avatar){
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        iv_avatar.setImageDrawable(circularBitmapDrawable);
                    }
                });
                ((TextView)header.findViewById(R.id.tv_money_sender)).setText(details.get("name").getAsString());
                ((TextView)header.findViewById(R.id.tv_greeting)).setText(details.get("comment").getAsString());
                ((TextView)header.findViewById(R.id.tv_money_amount)).setText("￥"+String.format("%.2f", details.get("money").getAsDouble()));
                TextView tv_money_status=(TextView)header.findViewById(R.id.tv_money_status);
                int status=details.get("status").getAsInt();
                if(status==1){
                    tv_money_status.setText(String.format(getString(R.string.group_money_available_sender),details.get("total_size").getAsInt(),
                            details.get("num").getAsInt(),details.get("amount").getAsDouble(),details.get("total_amount").getAsDouble()));
                }else if(status==2){
                    tv_money_status.setText(String.format(getString(R.string.group_money_unavailable_rand_sender),
                            details.get("num").getAsInt(),details.get("total_amount").getAsDouble(),details.get("length").getAsString()));
                }else if(status==3){
                    tv_money_status.setText(String.format(getString(R.string.group_money_expired),
                            details.get("total_size").getAsInt(),details.get("num").getAsInt(),details.get("amount").getAsDouble(),details.get("total_amount").getAsDouble()));
                }
                header.findViewById(R.id.iv_group_random).setVisibility(View.VISIBLE);
                return header;
            }

            @Override
            public void onBindView(View headerView) {

            }
        });

        JsonArray qiang_items=details.get("qiang_items").getAsJsonArray();
        for(int i=0;i<qiang_items.size();i++){
            HongBaoModel hm=new HongBaoModel();
            JsonObject item=qiang_items.get(i).getAsJsonObject();
            hm.setAmount(item.get("amount").getAsDouble());
            hm.setIs_best(item.get("is_best").getAsInt());
            hm.setTime(item.get("add_time").getAsString());
            if(item.get("type").getAsInt()==0){
                hm.setName(item.get("fname").getAsString());
                hm.setHeadUrl(item.get("fheadUrl").getAsString());
            }else{
                hm.setName(item.get("pname").getAsString());
                hm.setHeadUrl(item.get("pheadUrl").getAsString());
            }
            adapter.add(hm);
        }

        if(details.get("next_page").getAsInt()==1){
            adapter.setMore(R.layout.autolistview_footer, new RecyclerArrayAdapter.OnLoadMoreListener() {
                @Override
                public void onLoadMore() {
                    page++;
                    gethblist();
                }
            });
        }else{
           addFooter();
        }
        recyclerView.setAdapter(adapter);
    }
    private void addFooter(){
        adapter.addFooter(new RecyclerArrayAdapter.ItemView() {
            @Override
            public View onCreateView(ViewGroup parent) {
                View footer=View.inflate(getActivity(),R.layout.rp_details_list_footer,null);
                footer.setLayoutParams(new EasyRecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UIUtil.dip2px(getActivity(),60f)));
                footer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent();
                        intent.setClass(getActivity(), MyHongBaohistory.class);
                        startActivity(intent);
                    }
                });
                return footer;
            }
            @Override
            public void onBindView(View headerView) {

            }
        });
    }
    private void gethblist(){
        HttpTask task=new HttpTask(getActivity());
        task.setTaskHandler(new HttpTask.HttpTaskHandler() {
            @Override
            public void taskStart(int code) {

            }
            @Override
            public void taskSuccessful(String str, int code) {
                JsonObject object=new JsonParser().parse(str).getAsJsonObject();
                if(object.get("code").getAsInt()== CodeUtil.SUCCESS_CODE){
                    JsonArray qiang_items=object.get("qiang_items").getAsJsonArray();
                    for(int i=0;i<qiang_items.size();i++){
                        JsonObject item=qiang_items.get(i).getAsJsonObject();
                        HongBaoModel hm=new HongBaoModel();
                        hm.setAmount(item.get("amount").getAsDouble());
                        hm.setIs_best(item.get("is_best").getAsInt());
                        hm.setTime(item.get("add_time").getAsString());
                        if(item.get("type").getAsInt()==0){
                            hm.setName(item.get("fname").getAsString());
                            hm.setHeadUrl(item.get("fheadUrl").getAsString());
                        }else{
                            hm.setName(item.get("pname").getAsString());
                            hm.setHeadUrl(item.get("pheadUrl").getAsString());
                        }
                        adapter.add(hm);
                    }
                    if(object.get("next_page").getAsInt()==0){
                        adapter.stopMore();
                        adapter.removeAllFooter();
                        addFooter();
                    }
                }
            }
            @Override
            public void taskFailed(int code) {

            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("token", UserUtil.getUserModel(getActivity()).getToken());
        object.addProperty("id",details.get("hb_id").getAsInt());
        object.addProperty("start_page",page);
        task.execute(UrlUtil.GET_HB_LIST,object.toString());
    }
}
