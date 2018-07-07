package com.tongtong.purchaser.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.mobileim.conversation.YWConversation;
import com.alibaba.mobileim.conversation.YWMessage;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.application.MyApplication;
import com.tongtong.purchaser.helper.ChattingOperationCustom;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.ToastUtil;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;

import java.text.DecimalFormat;

import dmax.dialog.SpotsDialog;

/**
 * Created by Administrator on 2018-02-02.
 */

public class GuobangDetailsActivity extends BaseActivity implements View.OnClickListener
,HttpTask.HttpTaskHandler{
    private EditText num,total;
    private double price;
    private DecimalFormat format;
    private EditText pi_num,jz_num,gb_num,beizhu;
    private SpotsDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guobang_detils_layout);
        num=(EditText) findViewById(R.id.num);
        total=(EditText) findViewById(R.id.total);
        pi_num=(EditText) findViewById(R.id.pi_num);
        jz_num=(EditText) findViewById(R.id.jz_num);
        gb_num=(EditText) findViewById(R.id.gb_num);
        beizhu=(EditText) findViewById(R.id.beizhu);
        format=new DecimalFormat("#.00");
        ((TextView)findViewById(R.id.title_text)).setText("收购单详情");
        num.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
                if(pi_num.getText().length()>0&&editable.length()>0){
                    int mao=Integer.valueOf(editable.toString());
                    int pi=Integer.valueOf(pi_num.getText().toString());
                    if(pi>=mao){
                        ToastUtil.showShortToast(GuobangDetailsActivity.this,"毛重必须大于皮重。");
                        return;
                    }
                    jz_num.setText(String.valueOf(mao-pi));
                }
            }
        });
        pi_num.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(num.getText().length()>0&&editable.length()>0){
                    int pi=Integer.valueOf(editable.toString());
                    int mao=Integer.valueOf(num.getText().toString());
                    if(pi>=mao){
                        ToastUtil.showShortToast(GuobangDetailsActivity.this,"毛重必须大于皮重。");
                        return;
                    }
                    jz_num.setText(String.valueOf(mao-pi));
                }
            }
        });
        jz_num.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length()>0){
                    gb_num.setText(format.format(price*Integer.valueOf(editable.toString())));
                    total.setText(format.format(price*Integer.valueOf(editable.toString())));
                }else{
                    gb_num.getText().clear();
                    total.getText().clear();
                }
            }
        });
        findViewById(R.id.btn).setOnClickListener(this);
        findViewById(R.id.back_bn).setOnClickListener(this);
        price=getIntent().getDoubleExtra("price",0);
        ((TextView)findViewById(R.id.price)).setText("注：单价"+price+"元/"+getIntent().getStringExtra("unit"));

    }
    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.back_bn){
            onBackPressed();
        }else if(view.getId()==R.id.btn){
            if(TextUtils.isEmpty(jz_num.getText().toString())){
                showTips("请输入净重");
                return;
            }
            if(TextUtils.isEmpty(total.getText().toString())){
                showTips("请输入实付金额");
                return;
            }
            HttpTask task=new HttpTask(this);
            task.setTaskHandler(this);
            JsonObject requestJson = new JsonObject();
            requestJson.addProperty("token", UserUtil.getUserModel(this).getToken());
            requestJson.addProperty("total",total.getText().toString());
            requestJson.addProperty("maozhong",num.getText().toString());
            requestJson.addProperty("pizhong",pi_num.getText().toString());
            requestJson.addProperty("jinzhong",jz_num.getText().toString());
            requestJson.addProperty("sgbeizhu",beizhu.getText().toString());
            requestJson.addProperty("order_id",getIntent().getIntExtra("order_id",0));
            task.execute(UrlUtil.GENARATE_SHOUGOU,requestJson.toString());
        }
    }

    @Override
    public void taskStart(int code) {
        dialog=new SpotsDialog(this,"正在提交数据……",R.style.Custom);
        dialog.show();
    }

    @Override
    public void taskSuccessful(String str, int code) {
        dialog.dismiss();
        JsonObject resultJson = new JsonParser().parse(str).getAsJsonObject();
        int resultCode = resultJson.get("code").getAsInt();
        if (verification(resultCode)) {
            YWMessage message= ChattingOperationCustom.createCustomFinalOrderMessage(getIntent().getStringExtra("name"),
                    Integer.valueOf(jz_num.getText().toString()),Double.valueOf(total.getText().toString()),getIntent().getIntExtra("order_id",0),getIntent().getStringExtra("unit"),getIntent().getStringExtra("purchase_phone"),getIntent().getDoubleExtra("price",0));
            YWConversation conversation= MyApplication.getConversation(getIntent().getStringExtra("farmer_phone"));
            if(conversation!=null){
                conversation.getMessageSender().sendMessage(message,120,null);
            }
            ChattingOperationCustom.sendTransMsg("收货方已开具收购单",getIntent().getStringExtra("farmer_phone"));
            ChattingOperationCustom.sendSysMsg("你已开具收购单",getIntent().getStringExtra("farmer_phone"));
            this.finish();
            if(OrderDetailsActivity.getInstance()!=null){
                OrderDetailsActivity.getInstance().finish();
            }
        }else{
            ToastUtil.showShortToast(this,"数据提交失败！");
        }
    }

    @Override
    public void taskFailed(int code) {
        dialog.dismiss();
        ToastUtil.showShortToast(this,"数据提交失败！");
    }
}
