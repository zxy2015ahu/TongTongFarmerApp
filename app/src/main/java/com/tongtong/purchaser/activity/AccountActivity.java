package com.tongtong.purchaser.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.utils.CodeUtil;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.view.NumberAnimTextView;
import com.yunzhanghu.redpacketui.widget.RPTitleBar;

/**
 * Created by zxy on 2018/3/13.
 */

public class AccountActivity extends BaseActivity implements View.OnClickListener{
    private TextView details;
    private NumberAnimTextView tv_pay_money_amount;
    private TextView tag;
    private ImageView img;
    private SharedPreferences auth;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_layout);
        auth=getSharedPreferences("auth", Context.MODE_PRIVATE);
        RPTitleBar titleBar=(RPTitleBar) findViewById(R.id.bc_title_bar);
        titleBar.setSubTitleVisibility(View.GONE);
        titleBar.setLeftLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        tv_pay_money_amount=(NumberAnimTextView) findViewById(R.id.tv_pay_money_amount);
        tv_pay_money_amount.setPrefixString("￥");
        tv_pay_money_amount.setDuration(1000);
        details=(TextView) findViewById(R.id.details);
        details.setOnClickListener(this);
        findViewById(R.id.record_release).setOnClickListener(this);
        findViewById(R.id.bankcard_list).setOnClickListener(this);
        findViewById(R.id.tv_verify_identity).setOnClickListener(this);
        tag=(TextView) findViewById(R.id.tag);
        img=(ImageView) findViewById(R.id.img);
        if(TextUtils.isEmpty(UserUtil.getUserModel(this).getRealname())||
                TextUtils.isEmpty(UserUtil.getUserModel(this).getCardid())){
            tag.setTextColor(0xffF4333C);
            if(auth.getBoolean("fail",false)){
                tag.setText("认证失败");
                img.setImageResource(R.drawable.vector_drawable_shibai);
            }else{
                img.setVisibility(View.GONE);
                tag.setText("");
            }
        }else{
            tag.setTextColor(0xff00AD59);
            tag.setText("认证成功");
            img.setImageResource(R.drawable.vector_drawable_chenggong);
        }
        getmoney();
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent.getBooleanExtra("is_success",false)){
            img.setVisibility(View.VISIBLE);
            tag.setVisibility(View.VISIBLE);
            tag.setTextColor(0xff00AD59);
            tag.setText("认证成功");
            img.setImageResource(R.drawable.vector_drawable_chenggong);
        }else{
            auth.edit().putBoolean("fail",true).commit();
            tag.setTextColor(0xffF4333C);
            tag.setText("认证失败");
            img.setImageResource(R.drawable.vector_drawable_shibai);
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.details){
            Intent intent=new Intent();
            intent.setClass(this, MoneyDetailsActivity.class);
            startActivity(intent);
        }else if(v.getId()==R.id.record_release){
            Intent intent=new Intent();
            intent.setClass(this, MyHongBaohistory.class);
            startActivity(intent);
        }else if(v.getId()==R.id.bankcard_list){
//            Intent intent=new Intent();
//            intent.setClass(this, BankCardListActivity.class);
//            startActivity(intent);
        }else if(v.getId()==R.id.tv_verify_identity){
            if(TextUtils.isEmpty(UserUtil.getUserModel(this).getRealname())||
                    TextUtils.isEmpty(UserUtil.getUserModel(this).getCardid())){
//                Intent intent=new Intent();
//                intent.setClass(this, IdCardActivity.class);
//                startActivity(intent);
            }
        }
    }


    private void getmoney(){
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
                if(selectResultCode== CodeUtil.SUCCESS_CODE){
                    tv_pay_money_amount.setNumberString(String.format("%.2f", selectResultJson.get("money").getAsDouble()));
                }
            }

            @Override
            public void taskFailed(int code) {

            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("token", UserUtil.getUserModel(this).getToken());
        object.addProperty("purchaser_id",UserUtil.getUserModel(this).getId());
        task.execute(UrlUtil.GET_MONEY,object.toString());
    }
}
