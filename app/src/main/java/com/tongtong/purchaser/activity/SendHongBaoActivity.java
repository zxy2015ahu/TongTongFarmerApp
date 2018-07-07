package com.tongtong.purchaser.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import com.android.tu.loadingdialog.LoadingDailog;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.frament.SendHongBaoFragment;
import com.tongtong.purchaser.utils.Constant;
import com.tongtong.purchaser.widget.AlertDialog;
import com.yunzhanghu.redpacketui.widget.RPTitleBar;

import java.util.Map;


/**
 * Created by zxy on 2018/3/5.
 */

public class SendHongBaoActivity extends BaseActivity implements View.OnClickListener{
    private static SendHongBaoActivity instance;
    private LoadingDailog loading;
    public static final int SDK_PAY_FLAG=1;
    public static final int SEND_SUCCESS=2;
    private MyReciever reciever;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rp_activity_red_packet);
        instance=this;
        RPTitleBar titleBar=(RPTitleBar) findViewById(R.id.title_bar);
        titleBar.setSubTitleVisibility(View.GONE);
        titleBar.setLeftLayoutClickListener(this);
        reciever=new MyReciever();
        IntentFilter filter=new IntentFilter();
        filter.addAction(Constant.MSG_PAY);
        registerReceiver(reciever,filter);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.money_fragment_container,new SendHongBaoFragment()).commit();
    }


    private class MyReciever extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(Constant.MSG_PAY.equals(intent.getAction())){
                int code=intent.getIntExtra("code",-1);
                if(code==0){
                    showSuccess("支付成功","你的红包已发出！");
                }else if(code==-1){
                    showSuccess("支付失败","支付发生错误！");
                }else if(code==-2){
                    showSuccess("支付取消","你已取消支付！");
                }else{
                    showSuccess("支付失败","未知错误！");
                }
            }
        }
    }
    public   Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==SDK_PAY_FLAG){
                Map<String,String> result=(Map<String,String>)msg.obj;
                String resultStatus=result.get("resultStatus");
                if(resultStatus.equals("9000")){
                    showSuccess("支付成功","你的红包已发出！");
                }else{
                    showSuccess("支付失败",result.get("memo"));
                }
            }else if(msg.what==SEND_SUCCESS){
                showSuccess("支付成功","你的红包已发出！");
            }
        }
    };

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.left_image){
            onBackPressed();
        }
    }

    public void showLoading(){
        LoadingDailog.Builder loadBuilder=new LoadingDailog.Builder(this)
                .setMessage("加载中...")
                .setCancelable(true)
                .setCancelOutside(true);
        loading=loadBuilder.create();
        loading.show();
    }
    public void dismissLoading(){
        if(loading!=null){
            loading.dismiss();
        }
    }
    public static SendHongBaoActivity getInstance(){
        return instance;
    }


    public  void showSuccess(String title,String content){
        if(loading.isShowing()){
            loading.dismiss();
        }
        AlertDialog dialog=new AlertDialog(this).builder();
        if(!TextUtils.isEmpty(title)){
            dialog.setTitle(title);
        }
        dialog.setMsg(content);
        dialog.setNegativeButton("确定", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setAction(Constant.MSG_RP_UPDATE);
                sendBroadcast(intent);
                SendHongBaoActivity.getInstance().onBackPressed();
            }
        });
        dialog.show();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(reciever);
    }
}
