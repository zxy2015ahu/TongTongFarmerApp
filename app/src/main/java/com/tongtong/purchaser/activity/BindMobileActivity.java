package com.tongtong.purchaser.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.githang.statusbar.StatusBarCompat;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.application.MyApplication;
import com.tongtong.purchaser.model.MyProduceModel;
import com.tongtong.purchaser.model.PurchaserModel;
import com.tongtong.purchaser.utils.CodeUtil;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.ProduceTypesHelper;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.widget.AlertDialog;
import com.tongtong.purchaser.widget.StyleableToast;
import com.yunzhanghu.redpacketui.widget.RPTitleBar;

import java.util.List;

/**
 * Created by Administrator on 2018-05-19.
 */

public class BindMobileActivity extends BaseActivity implements View.OnClickListener,
        TextWatcher{
    private EditText userName;
    private EditText yam;
    private TextView get_code;
    private long timestamp;
    private boolean is_sending=false;
    private Handler handler=new Handler();
    private int COUNT=60;
    private String iconurl,name,openid;
    private ImageView head_img;
    private TextView nick_name;
    private ImageButton delete;
    private SharedPreferences sp;
    private SharedPreferences mylocation;
    private String parent_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.band_mobile_layout);
        StatusBarCompat.setStatusBarColor(this, Color.WHITE,true);
        sp=getSharedPreferences("location", Context.MODE_PRIVATE);
        mylocation=getSharedPreferences("mylocation", Context.MODE_PRIVATE);
        RPTitleBar titleBar=(RPTitleBar) findViewById(R.id.title_bar);
        titleBar.setLeftLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        titleBar.setSubTitleVisibility(View.GONE);
        userName=(EditText) findViewById(R.id.username);
        yam=(EditText) findViewById(R.id.password);
        findViewById(R.id.login_bn).setOnClickListener(this);
        get_code=(TextView) findViewById(R.id.get_code);
        get_code.setOnClickListener(this);
        iconurl=getIntent().getStringExtra("iconurl");
        name=getIntent().getStringExtra("name");
        openid=getIntent().getStringExtra("openid");
        head_img=(ImageView) findViewById(R.id.head_img);
        nick_name=(TextView) findViewById(R.id.send_at_msg_contact_nick);
        delete=(ImageButton) findViewById(R.id.delete);
        delete.setOnClickListener(this);
        Glide.with(this).load(iconurl)
                .asBitmap().centerCrop().placeholder(R.drawable.rp_avatar).into(new BitmapImageViewTarget(head_img){
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                head_img.setImageDrawable(circularBitmapDrawable);
            }
        });
        nick_name.setText(name);
        userName.addTextChangedListener(this);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(s.toString().length()>0){
            delete.setVisibility(View.VISIBLE);
        }else{
            delete.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
       if (v.getId() == R.id.get_code) {
            if (TextUtils.isEmpty(userName.getText().toString().trim())) {
                showTips("请填写手机号");
                return;
            }
            if (is_sending) {
                return;
            }
            getCode();
        } else if (v.getId() == R.id.login_bn) {
            if (TextUtils.isEmpty(userName.getText().toString().trim())) {
                showTips("请填写手机号");
                return;
            }
            if (TextUtils.isEmpty(yam.getText().toString().trim())) {
                showTips("请填写验证码");
                return;
            }
            checkCode();
        }else if(v.getId()==R.id.delete){
           userName.getText().clear();
       }
    }
    private void countDown(){
        is_sending=true;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(COUNT>0) {
                    COUNT--;
                    get_code.setText(COUNT + "秒后重发");
                    handler.postDelayed(this, 1000);
                }else{
                    handler.removeCallbacks(this);
                    COUNT=60;
                    get_code.setText("获取验证码");
                    is_sending=false;
                }
            }
        },1000);
    }
    private void getCode(){
        HttpTask task=new HttpTask(this);
        timestamp= System.currentTimeMillis()/1000;
        task.setTaskHandler(new HttpTask.HttpTaskHandler() {
            @Override
            public void taskStart(int code) {
                showLoading();
            }
            @Override
            public void taskSuccessful(String str, int code) {
                dismissLoading();
                JsonObject object=new JsonParser().parse(str).getAsJsonObject();
                if(object.get("code").getAsInt()== CodeUtil.SUCCESS_CODE){
                    StyleableToast.info(BindMobileActivity.this,"验证码已发送");
                    countDown();
                }else{
                    StyleableToast.error(BindMobileActivity.this,object.get("info").getAsString());
                }
            }
            @Override
            public void taskFailed(int code) {
                dismissLoading();
            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("timestamp",timestamp);
        object.addProperty("mobile",userName.getText().toString().trim());
        task.execute(UrlUtil.SEND_CODE,object.toString());
    }
    private void checkCode(){
        HttpTask task=new HttpTask(this);
        task.setTaskHandler(new HttpTask.HttpTaskHandler() {
            @Override
            public void taskStart(int code) {
                showLoading();
            }
            @Override
            public void taskSuccessful(String str, int code) {
                JsonObject object=new JsonParser().parse(str).getAsJsonObject();
                if(object.get("code").getAsInt()== CodeUtil.SUCCESS_CODE){
                    Gson gson=new Gson();
                    JsonObject userJson = object.get("data").getAsJsonObject();
                    PurchaserModel purchaser = gson.fromJson(userJson, PurchaserModel.class);
                    UserUtil.setUserModel(BindMobileActivity.this, purchaser);
                    getConfig();
                }else{
                    StyleableToast.error(BindMobileActivity.this,object.get("info").getAsString());
                }
            }
            @Override
            public void taskFailed(int code) {
                dismissLoading();
            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("timestamp",timestamp);
        object.addProperty("mobile",userName.getText().toString().trim());
        object.addProperty("code",yam.getText().toString().trim());
        object.addProperty("openid",openid);
        object.addProperty("name",name);
        object.addProperty("iconurl",iconurl);
        object.addProperty("parent_id",TextUtils.isEmpty(parent_id)?0:Integer.valueOf(parent_id));
        task.execute(UrlUtil.CHECK_CODE,object.toString());
    }
    private void getConfig(){
        HttpTask task=new HttpTask(this);
        task.setTaskHandler(new HttpTask.HttpTaskHandler() {
            @Override
            public void taskStart(int code) {

            }

            @Override
            public void taskSuccessful(String str, int code) {
                dismissLoading();
                JsonObject selectResultJson = new JsonParser().parse(str)
                        .getAsJsonObject();
                int selectResultCode = selectResultJson.get("code").getAsInt();
                if(selectResultCode== CodeUtil.SUCCESS_CODE){
                    handleProduceSpecification(selectResultJson);
                    sp.edit().putString("hot_search",selectResultJson.get("hot_search").getAsString()).commit();
                    sp.edit().putString("unit",selectResultJson.get("unit").getAsString()).commit();
                    Intent intent = new Intent();
                    intent.putExtra("result",str);
                    intent.setClass(BindMobileActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    AlertDialog dialog=new AlertDialog(BindMobileActivity.this).builder();
                    dialog.setTitle("提示");
                    dialog.setMsg("初始化发生错误，请退出后重试");
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("确定", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MyApplication.instance.exit();
                        }
                    });
                    dialog.show();
                }
            }
            @Override
            public void taskFailed(int code) {
                dismissLoading();
                AlertDialog dialog=new AlertDialog(BindMobileActivity.this).builder();
                dialog.setTitle("提示");
                dialog.setMsg("初始化发生错误，请退出后重试");
                dialog.setCancelable(false);
                dialog.setPositiveButton("确定", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MyApplication.instance.exit();
                    }
                });
                dialog.show();
            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("purchaser_id",UserUtil.getUserModel(this)!=null?UserUtil.getUserModel(this).getId():0);
        object.addProperty("adcode",mylocation.getInt("code",0)!=0?mylocation.getInt("code",0):sp.getInt("code",0));
        object.addProperty("lat",Double.valueOf(sp.getString("lat","0")));
        object.addProperty("lng",Double.valueOf(sp.getString("lng","0")));
        task.execute(UrlUtil.GET_CONFIG,object.toString());
    }
    private void handleProduceSpecification(final JsonObject selectResultJson){
        int api_version=selectResultJson.get("api_version").getAsInt();
        int local_version=sp.getInt("local_version",-1);
        if(local_version>=api_version){
            return;
        }
        sp.edit().putInt("local_version",api_version).commit();
        new Thread(){
            @Override
            public void run() {
                ProduceTypesHelper.deleteProduceTypes(BindMobileActivity.this);
                JsonArray produce_types=selectResultJson.get("produces").getAsJsonArray();
                Gson gson=new Gson();
                List<MyProduceModel> produceTypes=gson.fromJson(produce_types,
                        new TypeToken<List<MyProduceModel>>() {
                        }.getType());
                ProduceTypesHelper.addProduceType(BindMobileActivity.this,produceTypes);
            }
        }.start();
    }
}
