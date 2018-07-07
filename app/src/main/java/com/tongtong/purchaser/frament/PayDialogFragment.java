package com.tongtong.purchaser.frament;

import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.alipay.sdk.app.PayTask;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.activity.SendHongBaoActivity;
import com.tongtong.purchaser.utils.CodeUtil;
import com.tongtong.purchaser.utils.Constant;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;

import java.text.DecimalFormat;
import java.util.Map;

/**
 * Created by zxy on 2018/3/6.
 */

public class PayDialogFragment extends DialogFragment implements View.OnClickListener{
    private TextView tv_pay_money_amount;
    private DecimalFormat df=new DecimalFormat("0.00");
    private double money,my_money;
    private ImageView iv_change_icon;
    private TextView tv_pay_change_balance,tv_pay_pwd_tip;
    private int pay_type= Constant.PAY_TYPE_YUE;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return inflater.inflate(R.layout.rp_pay_change_no_pwd_dialog,container,false);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.iv_pay_closed).setOnClickListener(this);
        view.findViewById(R.id.layout_pay_change).setOnClickListener(this);
        tv_pay_pwd_tip=(TextView) view.findViewById(R.id.tv_pay_pwd_tip);
        tv_pay_money_amount=(TextView) view.findViewById(R.id.tv_pay_money_amount);
        money=getArguments().getDouble("money");
        my_money=getArguments().getDouble("my_money");
        view.findViewById(R.id.btn_pay_no_pwd).setOnClickListener(this);
        tv_pay_money_amount.setText("￥"+df.format(money));
        iv_change_icon=(ImageView) view.findViewById(R.id.iv_change_icon);
        tv_pay_change_balance=(TextView)view.findViewById(R.id.tv_pay_change_balance);
        int select_paytype=getArguments().getInt("pay_type",-1);
        if(select_paytype==-1){
            if(my_money<money){
                changeToAlipay();
            }else{
                changeToYue();
            }
        }else{
            if(select_paytype==Constant.PAY_TYPE_YUE){
                changeToYue();
            }else if(select_paytype==Constant.PAY_TYPE_ALIPAY){
                changeToAlipay();
            }else if(select_paytype==Constant.PAY_TYPE_WEIXIN){
                changeToWeixin();
            }
        }

    }
    private void changeToAlipay(){
        pay_type=Constant.PAY_TYPE_ALIPAY;
        tv_pay_pwd_tip.setVisibility(View.GONE);
        iv_change_icon.setImageResource(R.drawable.rp_alipay_icon);
        tv_pay_change_balance.setText(getString(R.string.ali_pay));
    }
    private void changeToYue(){
        pay_type=Constant.PAY_TYPE_YUE;
        //tv_pay_pwd_tip.setVisibility(View.GONE);
        iv_change_icon.setImageResource(R.drawable.rp_change_icon);
        tv_pay_change_balance.setText(String.format(getString(R.string.my_change),my_money));
    }
    private void changeToWeixin(){
        pay_type=Constant.PAY_TYPE_WEIXIN;
        tv_pay_pwd_tip.setVisibility(View.GONE);
        iv_change_icon.setImageResource(R.drawable.rp_wxpay_icon);
        tv_pay_change_balance.setText((getString(R.string.wx_pay)));
    }
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btn_pay_no_pwd){
            dismiss();
            if(pay_type==Constant.PAY_TYPE_ALIPAY){
                sendHb();
            }else if(pay_type==Constant.PAY_TYPE_YUE){
                fahongbaobyyue();
            }else if(pay_type==Constant.PAY_TYPE_WEIXIN){
                sendHbWx();
            }else if(pay_type==Constant.PAY_TYPE_YINHANGKA){

            }
        }else if(v.getId()==R.id.iv_pay_closed){
            dismiss();
        }else if(v.getId()==R.id.layout_pay_change){
            dismiss();
            PaySelectDialogFragment pay=new PaySelectDialogFragment();
            Bundle args=getArguments();
            args.putInt("pay_type",pay_type);
            pay.setArguments(args);
            pay.show(getActivity().getSupportFragmentManager(),"pay_select");
        }
    }
    private void sendHb(){
        HttpTask task=new HttpTask(getActivity());
        task.setTaskHandler(new HttpTask.HttpTaskHandler() {
            @Override
            public void taskStart(int code) {
                SendHongBaoActivity.getInstance().showLoading();
            }
            @Override
            public void taskSuccessful(String str, int code) {
                SendHongBaoActivity.getInstance().dismissLoading();
                try {
                    JsonObject selectResultJson = new JsonParser().parse(str)
                            .getAsJsonObject();
                    int selectResultCode=selectResultJson.get("code").getAsInt();
                    if(selectResultCode== CodeUtil.SUCCESS_CODE){
                        final String orderInfo=selectResultJson.get("alipay").getAsString();
                        Runnable payRunnable = new Runnable() {
                            @Override
                            public void run() {
                                PayTask alipay = new PayTask(SendHongBaoActivity.getInstance());
                                Map<String,String> result = alipay.payV2(orderInfo,true);
                                Message msg = new Message();
                                msg.what = SendHongBaoActivity.SDK_PAY_FLAG;
                                msg.obj = result;
                                SendHongBaoActivity.getInstance().mHandler.sendMessage(msg);
                            }
                        };
                        // 必须异步调用
                        Thread payThread = new Thread(payRunnable);
                        payThread.start();
                    }
                }catch (Exception e){

                }
                //SendHongBaoActivity.showSuccess("发送成功","你的红包已成功发出！");
            }

            @Override
            public void taskFailed(int code) {
                SendHongBaoActivity.getInstance().dismissLoading();
                SendHongBaoActivity.getInstance().showSuccess("发送失败","获取红包失败，请重试！");
            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("order_no",getArguments().getString("order_no"));
        object.addProperty("money",getArguments().getDouble("money"));
        task.execute(UrlUtil.SEND_RP_ORDER,object.toString());
    }
    private void sendHbWx(){
        HttpTask task=new HttpTask(getActivity());
        task.setTaskHandler(new HttpTask.HttpTaskHandler() {
            @Override
            public void taskStart(int code) {
                SendHongBaoActivity.getInstance().showLoading();
            }
            @Override
            public void taskSuccessful(String str, int code) {
                SendHongBaoActivity.getInstance().dismissLoading();
                JsonObject selectResultJson = new JsonParser().parse(str)
                        .getAsJsonObject();
                int selectResultCode=selectResultJson.get("code").getAsInt();
                if(selectResultCode==CodeUtil.SUCCESS_CODE){
                    String prepay_id=selectResultJson.get("prepay_id").getAsString();
                    String appid=selectResultJson.get("appid").getAsString();
                    IWXAPI api = WXAPIFactory.createWXAPI(SendHongBaoActivity.getInstance(), appid);
                    PayReq req=new PayReq();
                    req.appId=appid;
                    req.partnerId=selectResultJson.get("mch_id").getAsString();
                    req.nonceStr=selectResultJson.get("nonce_str").getAsString();
                    req.packageValue = "Sign=WXPay";
                    req.timeStamp=selectResultJson.get("timestamp").getAsString();
                    req.sign=selectResultJson.get("sign").getAsString();
                    req.prepayId=prepay_id;
                    api.sendReq(req);
                }
                //SendHongBaoActivity.showSuccess("发送成功","你的红包已成功发出！");
            }

            @Override
            public void taskFailed(int code) {
                SendHongBaoActivity.getInstance().dismissLoading();
                SendHongBaoActivity.getInstance().showSuccess("发送失败","获取红包失败，请重试！");
            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("order_no",getArguments().getString("order_no"));
        object.addProperty("money",getArguments().getDouble("money"));
        object.addProperty("type",1);
        task.execute(UrlUtil.SEND_RP_WX_ORDER,object.toString());
    }

    private void fahongbaobyyue(){
        HttpTask task=new HttpTask(getActivity());
        task.setTaskHandler(new HttpTask.HttpTaskHandler() {
            @Override
            public void taskStart(int code) {
                SendHongBaoActivity.getInstance().showLoading();
            }

            @Override
            public void taskSuccessful(String str, int code) {
                SendHongBaoActivity.getInstance().dismissLoading();
                Message msg = new Message();
                msg.what = SendHongBaoActivity.SEND_SUCCESS;
                SendHongBaoActivity.getInstance().mHandler.sendMessage(msg);
            }

            @Override
            public void taskFailed(int code) {
                SendHongBaoActivity.getInstance().dismissLoading();
                SendHongBaoActivity.getInstance().showSuccess("发送失败","获取红包失败，请重试！");
            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("order_no",getArguments().getString("order_no"));
        object.addProperty("money",getArguments().getDouble("money"));
        object.addProperty("token", UserUtil.getUserModel(getActivity()).getToken());
        task.execute(UrlUtil.SEND_RP_BY_YUE,object.toString());
    }
}
