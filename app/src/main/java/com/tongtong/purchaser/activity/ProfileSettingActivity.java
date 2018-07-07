package com.tongtong.purchaser.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

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
import com.tongtong.purchaser.utils.UploadFileTask;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.widget.AlertDialog;
import com.yancy.gallerypick.config.GalleryConfig;
import com.yancy.gallerypick.config.GalleryPick;
import com.yancy.gallerypick.inter.IHandlerCallBack;
import com.yancy.gallerypick.inter.ImageLoader;
import com.yancy.gallerypick.widget.GalleryImageView;
import com.yunzhanghu.redpacketui.widget.RPTitleBar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018-05-21.
 */

public class ProfileSettingActivity extends BaseActivity implements View.OnClickListener{
    private EditText username,password;
    private ImageButton delete,password_image;
    private ImageView head_img;
    private GalleryConfig config;
    private List<String> path = new ArrayList<>();
    private boolean show_pass=false;
    private SharedPreferences sp;
    private SharedPreferences mylocation;
    private int parent_id=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_setting_layout);
        StatusBarCompat.setStatusBarColor(this, Color.WHITE,true);
        sp=getSharedPreferences("location", Context.MODE_PRIVATE);
        mylocation=getSharedPreferences("mylocation", Context.MODE_PRIVATE);
        RPTitleBar titleBar=(RPTitleBar) findViewById(R.id.title_bar);
        titleBar.setSubTitleVisibility(View.GONE);
        titleBar.setLeftLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        findViewById(R.id.login_bn).setOnClickListener(this);
        username=(EditText) findViewById(R.id.username);
        password=(EditText) findViewById(R.id.password);
        delete=(ImageButton) findViewById(R.id.delete);
        password_image=(ImageButton) findViewById(R.id.password_image);
        password_image.setOnClickListener(this);
        head_img=(ImageView) findViewById(R.id.head_img);
        head_img.setOnClickListener(this);
        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().length()>0){
                    delete.setVisibility(View.VISIBLE);
                }else{
                    delete.setVisibility(View.GONE);
                }
            }
        });
        config = new GalleryConfig.Builder()
                .imageLoader(new ImageLoader() {
                    @Override
                    public void displayImage(Activity activity, Context context, String path, GalleryImageView galleryImageView, int width, int height) {
                        Glide.with(context)
                                .load(path)
                                .placeholder(R.mipmap.gallery_pick_photo)
                                .centerCrop()
                                .into(galleryImageView);
                    }

                    @Override
                    public void clearMemoryCache() {

                    }
                })    // ImageLoader 加载框架（必填）
                .iHandlerCallBack(new IHandlerCallBack() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onSuccess(List<String> photoList) {
                        path=photoList;
                        if(path.size()>0){
                            Glide.with(ProfileSettingActivity.this)
                                    .load("file://"+path.get(0)).centerCrop().into(head_img);
                            Glide.with(ProfileSettingActivity.this).load("file://"+path.get(0))
                                    .asBitmap().centerCrop().into(new BitmapImageViewTarget(head_img){
                                @Override
                                protected void setResource(Bitmap resource) {
                                    RoundedBitmapDrawable circularBitmapDrawable =
                                            RoundedBitmapDrawableFactory.create(getResources(), resource);
                                    circularBitmapDrawable.setCircular(true);
                                    head_img.setImageDrawable(circularBitmapDrawable);
                                }
                            });
                        }
                    }
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onFinish() {

                    }

                    @Override
                    public void onError() {

                    }
                })     // 监听接口（必填）
                .provider("com.tongtong.purchaser.installapk")   // provider(必填)
                .pathList(path)                         // 记录已选的图片
                .multiSelect(false)                      // 是否多选   默认：false
                .crop(true, 1, 1, 500, 500)             // 配置裁剪功能的参数，   默认裁剪比例 1:1
                .isShowCamera(true)                     // 是否现实相机按钮  默认：false
                .filePath("/Gallery/Pictures")          // 图片存放路径
                .build();
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.login_bn){
            if(path.size()==0){
                showTips("请设置头像");
                return;
            }
            if(TextUtils.isEmpty(username.getText().toString().trim())){
                showTips("请设置用户名");
                return;
            }
            save();
        }else if(v.getId()==R.id.delete){
            username.getText().clear();
        }else if(v.getId()==R.id.head_img){
            GalleryPick.getInstance().setGalleryConfig(config).open(this);
        }else if(v.getId()==R.id.password_image){
            if(show_pass){
                show_pass=false;
                password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                password_image.setBackgroundResource(R.drawable.passwword_ming);
            }else{
                show_pass=true;
                password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                password_image.setBackgroundResource(R.drawable.password_mi);
            }
        }
    }
    private void save(){
        UploadFileTask task=new UploadFileTask(new File(path.get(0)));
        task.setTaskHandler(new UploadFileTask.HttpTaskHandler() {
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
                    UserUtil.setUserModel(ProfileSettingActivity.this, purchaser);
                    getConfig();
                }else{
                    showTips("注册失败，请重试");
                }
            }
            @Override
            public void taskFailed(int code) {
                dismissLoading();
            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("name",username.getText().toString());
        object.addProperty("mobile",getIntent().getStringExtra("mobile"));
        object.addProperty("password",password.getText().toString());
        object.addProperty("parent_id",parent_id);
        task.execute(UrlUtil.SAVE_INFO,object.toString());
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
                    intent.setClass(ProfileSettingActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    AlertDialog dialog=new AlertDialog(ProfileSettingActivity.this).builder();
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
                AlertDialog dialog=new AlertDialog(ProfileSettingActivity.this).builder();
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
        object.addProperty("purchaser_id", UserUtil.getUserModel(this)!=null?UserUtil.getUserModel(this).getId():0);
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
                ProduceTypesHelper.deleteProduceTypes(ProfileSettingActivity.this);
                JsonArray produce_types=selectResultJson.get("produces").getAsJsonArray();
                Gson gson=new Gson();
                List<MyProduceModel> produceTypes=gson.fromJson(produce_types,
                        new TypeToken<List<MyProduceModel>>() {
                        }.getType());
                ProduceTypesHelper.addProduceType(ProfileSettingActivity.this,produceTypes);
            }
        }.start();
    }
}
