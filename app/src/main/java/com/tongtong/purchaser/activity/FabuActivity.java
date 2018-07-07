package com.tongtong.purchaser.activity;

/**
 * Created by Administrator on 2018-05-26.
 */

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.guanaj.easyswipemenulibrary.EasySwipeMenuLayout;
import com.guanaj.easyswipemenulibrary.State;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.model.ProduceType;
import com.tongtong.purchaser.model.PurchaserReleaseInformationModel;
import com.tongtong.purchaser.utils.CodeUtil;
import com.tongtong.purchaser.utils.Constant;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.widget.NoScrollListView;
import com.tongtong.purchaser.widget.StyleableToast;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by Administrator on 2018-05-25.
 */

public class FabuActivity extends BaseActivity implements View.OnClickListener
{
    private TextView empty;
    private MyChangYongAdapter changYongAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fabu_fragment);
        findViewById(R.id.back_bn).setOnClickListener(this);
        ((TextView)findViewById(R.id.title_text)).setText(R.string.fabu);
        findViewById(R.id.btn_add_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setClass(FabuActivity.this, PuBlishActivity.class);
                startActivityForResult(intent,1);
            }
        });
        NoScrollListView list=(NoScrollListView) findViewById(R.id.list);
        empty=(TextView) findViewById(R.id.empty);
        list.setEmptyView(empty);
        changYongAdapter=new MyChangYongAdapter(this);
        list.setAdapter(changYongAdapter);
        getReleaseList();
    }

    private void getReleaseList(){
        HttpTask task=new HttpTask(this);
        task.setTaskHandler(new HttpTask.HttpTaskHandler() {
            @Override
            public void taskStart(int code) {

            }

            @Override
            public void taskSuccessful(String str, int code) {
                JsonObject selectResultJson = new JsonParser().parse(str)
                        .getAsJsonObject();
                if(selectResultJson.get("code").getAsInt()==CodeUtil.SUCCESS_CODE){
                    JsonArray release=selectResultJson.get("release").getAsJsonArray();
                    Gson gson = new Gson();
                    List<PurchaserReleaseInformationModel> purchaserReleaseInformations = gson.fromJson(release, new TypeToken<List<PurchaserReleaseInformationModel>>(){}.getType());
                    changYongAdapter.addAll(purchaserReleaseInformations);
                }
            }

            @Override
            public void taskFailed(int code) {

            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("purchaser_id",UserUtil.getUserModel(this).getId());
        task.execute(UrlUtil.GET_RELEASE_LIST,object.toString());
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1){
            if(data!=null){
                PurchaserReleaseInformationModel releaseInfo=(PurchaserReleaseInformationModel)data.getSerializableExtra("releaseInfo");
                changYongAdapter.insert(releaseInfo,0);
                changYongAdapter.notifyDataSetChanged();
                StyleableToast.info(this,"添加成功");
            }
        }else if(requestCode==2){
            if(data!=null) {
                PurchaserReleaseInformationModel releaseInfo = (PurchaserReleaseInformationModel) data.getSerializableExtra("releaseInfo");
                int position = data.getIntExtra("position", -1);
                if (position != -1) {
                    changYongAdapter.getItem(position).setRemarks(releaseInfo.getRemarks());
                    changYongAdapter.notifyDataSetChanged();
                    StyleableToast.info(this, "修改成功");
                }
            }
        }
    }
    private class MyChangYongAdapter extends ArrayAdapter<PurchaserReleaseInformationModel> {
        public MyChangYongAdapter(Context ctx){
            super(ctx,0);
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(convertView==null){
                convertView=getLayoutInflater().inflate(R.layout.shougou_list_item,null);
            }
            final PurchaserReleaseInformationModel model=getItem(position);
            ImageView img=(ImageView) convertView.findViewById(R.id.img);
            Glide.with(FabuActivity.this).load(UrlUtil.IMG_SERVER_URL+model.getProduce().getIconUrl()).placeholder(R.drawable.no_icon).into(img);
            TextView title=(TextView) convertView.findViewById(R.id.title);
            title.setText(model.getProduce().getName());
            TextView release_count=(TextView) convertView.findViewById(R.id.count);
            release_count.setText("符合信息共有"+model.getRelease_count()+"条 »");
            release_count.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent();
                    ProduceType produce=new ProduceType();
                    produce.setName(model.getProduce().getName());
                    produce.setLevel(model.getProduce().getLevel());
                    produce.setId(model.getProduce().getId());
                    intent.putExtra("produce",produce);
                    intent.setClass(FabuActivity.this, SearchResultActivity.class);
                    startActivity(intent);
                }
            });
            TextView desc=(TextView)convertView.findViewById(R.id.desc);
            desc.setText(TextUtils.isEmpty(model.getRemarks())?"暂无描述":model.getRemarks());
            ImageButton more=(ImageButton) convertView.findViewById(R.id.more);
            final EasySwipeMenuLayout menuLayout=(EasySwipeMenuLayout)convertView;
            more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleMenu(menuLayout);
                }
            });
            TextView delete=(TextView) convertView.findViewById(R.id.delete);
            TextView modify=(TextView) convertView.findViewById(R.id.head_lastUpdatedTextView);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    menuLayout.resetStatus();
                    deleteRelease(model);
                }
            });
            modify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    menuLayout.resetStatus();
                    Intent intent=new Intent();
                    intent.putExtra("produce",model.getProduce());
                    intent.putExtra("title","修改描述");
                    intent.putExtra("id",model.getId());
                    intent.putExtra("position",position);
                    intent.putExtra("remarks",model.getRemarks());
                    intent.setClass(FabuActivity.this, ReleaseInfoActivity.class);
                    startActivityForResult(intent,2);
                }
            });
            return convertView;
        }
    }
    private void deleteRelease(final PurchaserReleaseInformationModel model){
        HttpTask task=new HttpTask(FabuActivity.this);
        task.setTaskHandler(new HttpTask.HttpTaskHandler() {
            @Override
            public void taskStart(int code) {
                showLoading();
            }
            @Override
            public void taskSuccessful(String str, int code) {
                dismissLoading();
                JsonObject resultJson = new JsonParser().parse(str).getAsJsonObject();;
                int resultCode = resultJson.get("code").getAsInt();
                if(resultCode== CodeUtil.SUCCESS_CODE){
                    changYongAdapter.remove(model);
                    changYongAdapter.notifyDataSetChanged();
                    StyleableToast.info(FabuActivity.this,"删除成功");
                }
            }

            @Override
            public void taskFailed(int code) {
                StyleableToast.error(FabuActivity.this,"删除失败，请稍后再试");
                dismissLoading();
            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("token",UserUtil.getUserModel(FabuActivity.this).getToken());
        object.addProperty("purchaser_id",model.getId());
        task.execute(UrlUtil.DELETE_RELEASE,object.toString());
    }
    private void handleMenu(EasySwipeMenuLayout menuLayout){
        try {

            Method method = menuLayout.getClass().getDeclaredMethod("handlerSwipeMenu", State.class);
            method.setAccessible(true);
            if(EasySwipeMenuLayout.getViewCache()!=null&&EasySwipeMenuLayout.getViewCache()!=menuLayout){
                if(EasySwipeMenuLayout.getStateCache()!=State.CLOSE)
                    method.invoke(EasySwipeMenuLayout.getViewCache(), State.CLOSE);
            }
            if(EasySwipeMenuLayout.getStateCache()!=State.RIGHTOPEN) {
                method.invoke(menuLayout, State.RIGHTOPEN);
            }else{
                method.invoke(menuLayout, State.CLOSE);
            }
        }catch (Exception e){

        }
    }
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.right_bn){
            if(UserUtil.getUserModel(FabuActivity.this)==null){
                Intent intent=new Intent();
                intent.setClass(FabuActivity.this, LoginActivity.class);
                startActivity(intent);
                return;
            }
            Intent intent=new Intent();
            intent.putExtra(ChattingActivity.TARGET_ID, Constant.SERVER_ACCOUNT);
            intent.putExtra(ChattingActivity.TARGET_ESERVICE,ChattingActivity.TARGET_ESERVICE);
            intent.setClass(FabuActivity.this,ChattingActivity.class);
            startActivity(intent);
        }else if(v.getId()==R.id.back_bn){
            onBackPressed();
        }
    }
}