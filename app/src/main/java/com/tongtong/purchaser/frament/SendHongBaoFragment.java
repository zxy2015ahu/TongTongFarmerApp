package com.tongtong.purchaser.frament;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.activity.LocationSelectActivity;
import com.tongtong.purchaser.activity.SendHongBaoActivity;
import com.tongtong.purchaser.bean.SearchAddressInfo;
import com.tongtong.purchaser.utils.CodeUtil;
import com.tongtong.purchaser.utils.Constant;
import com.tongtong.purchaser.utils.DropDownWarning;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.widget.AlertEditDialog;

import java.text.DecimalFormat;

/**
 * Created by zxy on 2018/3/6.
 */

public class SendHongBaoFragment extends BaseFrament implements View.OnClickListener,
        TextWatcher{
    private EditText et_money_amount,et_greetings,et_money_count;
    private ImageView iv_refresh;
    private TextView tv_money;
    private Button btn_single_put_money;
    private String[] greetings;
    private int greeting_index;
    private DecimalFormat df=new DecimalFormat("0.00");
    private double money=0;
    private TextView hint,hint_num;
    private DropDownWarning dropDownWarning;
    private int count;
    private TextView tv_receive_name,tv_group_count;
    private int distance=5;
    private TextView tv_address_name;
    private double lat,lng;
    private AlertEditDialog dialog;
    private InputMethodManager imm;
    private Handler handler=new Handler();
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.send_hongbao_fragment,container,false);
    }
    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imm=(InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        et_money_amount=(EditText) view.findViewById(R.id.et_money_amount);
        et_greetings=(EditText) view.findViewById(R.id.et_greetings);
        iv_refresh=(ImageView) view.findViewById(R.id.iv_refresh);
        tv_money=(TextView) view.findViewById(R.id.tv_money);
        hint=(TextView) view.findViewById(R.id.hint);
        tv_group_count=(TextView) view.findViewById(R.id.tv_group_count);
        tv_receive_name=(TextView) view.findViewById(R.id.tv_receive_name);
        tv_address_name=(TextView) view.findViewById(R.id.tv_address_name);
        et_money_count=(EditText) view.findViewById(R.id.et_money_count);
        hint_num=(TextView) view.findViewById(R.id.hint_num);
        btn_single_put_money=(Button) view.findViewById(R.id.btn_group_put_money);
        iv_refresh.setOnClickListener(this);
        lat=getActivity().getIntent().getDoubleExtra("lat",0);
        lng=getActivity().getIntent().getDoubleExtra("lng",0);
        tv_address_name.setText(getActivity().getIntent().getStringExtra("address"));
        greetings=getResources().getStringArray(R.array.greetings);
        et_money_amount.addTextChangedListener(this);
        btn_single_put_money.setEnabled(false);
        btn_single_put_money.setOnClickListener(this);
        tv_money.setText("￥"+df.format(money));
        tv_receive_name.setText("周边5公里范围的人");
        view.findViewById(R.id.layout_members).setOnClickListener(this);
        view.findViewById(R.id.layout_address).setOnClickListener(this);
        et_money_count.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()>0){
                    hint_num.setVisibility(View.GONE);
                    count=Integer.valueOf(s.toString());
                    if(count>100){
                        btn_single_put_money.setEnabled(false);
                        dropDownWarning.setText(String.format(getString(R.string.tip_money_count_limit),100));
                        dropDownWarning.show();
                    }else if(count<=0){
                        dropDownWarning.setText("红包个数必须大于0");
                        dropDownWarning.show();
                    }else{
                        dropDownWarning.hide();
                        if(money>=Constant.MIN_RP&&money<=Constant.MAX_RP&&money/count>=0.01){
                            btn_single_put_money.setEnabled(true);
                        }else{
                            if(money>0&&money/count<0.01){
                                dropDownWarning.setText("单个红包金额必须大于0.01元");
                                dropDownWarning.show();
                            }
                            btn_single_put_money.setEnabled(false);
                        }
                    }
                }else{
                    hint_num.setVisibility(View.VISIBLE);
                    dropDownWarning.hide();
                    count=0;
                    btn_single_put_money.setEnabled(false);
                }
            }
        });
        dropDownWarning=new DropDownWarning.Builder(getActivity(),(ViewGroup) view).
                backgroundColor(ContextCompat.getColor(getActivity(),R.color.rp_msg_red)).foregroundColor(ContextCompat.getColor(getActivity(),R.color.rp_text_yellow))
                .interpolatorIn(new BounceInterpolator()).
                        interpolatorOut(new AnticipateOvershootInterpolator()).build();
        handleGreeings();
        getNearByCount();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1){
            if(data!=null){
                SearchAddressInfo info=data.getParcelableExtra("position");
                lat=info.latLonPoint.latitude;
                lng=info.latLonPoint.longitude;
                tv_address_name.setText(info.title);
                getNearByCount();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.iv_refresh){
            handleGreeings();
        }else if(v.getId()==R.id.btn_group_put_money){
            sendRp();
        }else if(v.getId()==R.id.layout_members){
            showInputDialog();
        }else if(v.getId()==R.id.layout_address){
            Intent intent=new Intent();
            intent.putExtra("flag","");
            intent.putExtra("lat",lat);
            intent.putExtra("lng",lng);
            intent.setClass(getActivity(), LocationSelectActivity.class);
            startActivityForResult(intent,1);
        }
    }
    private void getNearByCount(){
        HttpTask task=new HttpTask(getActivity());
        task.setTaskHandler(new HttpTask.HttpTaskHandler() {
            @Override
            public void taskStart(int code) {

            }
            @Override
            public void taskSuccessful(String str, int code) {
                JsonObject selectResultJson = new JsonParser().parse(str)
                        .getAsJsonObject();
                int selectResultCode=selectResultJson.get("code").getAsInt();
                if(selectResultCode== CodeUtil.SUCCESS_CODE){
                    tv_group_count.setText("周边约有"+selectResultJson.get("count").getAsInt()+"人可领");
                }
            }
            @Override
            public void taskFailed(int code) {

            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("lat",lat);
        object.addProperty("lng",lng);
        object.addProperty("distance",distance);
        task.execute(UrlUtil.GET_NEARBY_COUNT,object.toString());
    }
    private void sendRp(){
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
                        PayDialogFragment pay=new PayDialogFragment();
                        Bundle bundle=new Bundle();
                        bundle.putDouble("money",money);
                        bundle.putDouble("my_money",selectResultJson.get("my_money").getAsDouble());
                        bundle.putString("comment",et_greetings.getText().toString());
                        bundle.putInt("count",count);
                        bundle.putInt("distance",distance);
                        bundle.putString("order_no",selectResultJson.get("order_no").getAsString());
                        pay.setArguments(bundle);
                        pay.show(getActivity().getSupportFragmentManager(),"pay");
                    }
                }catch (Exception e){

                }
            }
            @Override
            public void taskFailed(int code) {
                SendHongBaoActivity.getInstance().dismissLoading();
                SendHongBaoActivity.getInstance().showSuccess("发送失败","获取红包失败，请重试！");
            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("token", UserUtil.getUserModel(getActivity()).getToken());
        object.addProperty("purchaser_id",UserUtil.getUserModel(getActivity()).getId());
        object.addProperty("amount",money);
        object.addProperty("comment",et_greetings.getText().toString());
        object.addProperty("num",count);
        object.addProperty("distance",distance);
        object.addProperty("lat",lat);
        object.addProperty("lng",lng);
        task.execute(UrlUtil.SEND_RP,object.toString());
    }
    private void showInfoWithDismiss(String info){
        dropDownWarning.setText(info);
        dropDownWarning.show();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dropDownWarning.hide();
            }
        },3000);
    }
    private void showInputDialog(){
        if(dialog==null) {
            dialog = new AlertEditDialog(getActivity()).builder();
            dialog.setTitle("请输入范围");
            dialog.setPositiveButton("确定", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imm.hideSoftInputFromWindow(dialog.getMsgView().getWindowToken(),0);
                    if (TextUtils.isEmpty(dialog.getMsg())) {
                        showInfoWithDismiss("请输入范围");
                        return;
                    }
                    distance = Integer.valueOf(dialog.getMsg());
                    if (distance < 1) {
                        showInfoWithDismiss("不能小于1公里");
                        return;
                    }
                    if (distance > 99) {
                        showInfoWithDismiss("不能大于99公里");
                        return;
                    }
                    tv_receive_name.setText("周边" + distance + "公里范围的人");
                    getNearByCount();
                }
            });
            dialog.setNegativeButton("取消", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imm.hideSoftInputFromWindow(dialog.getMsgView().getWindowToken(),0);
                }
            });
        }
        dialog.show();
        imm.showSoftInput(dialog.getMsgView(), InputMethodManager.RESULT_SHOWN);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY);
    }
    private void handleGreeings(){
        et_greetings.setText(greetings[greeting_index]);
        et_greetings.setSelection(et_greetings.getText().length());
        greeting_index++;
        if(greeting_index>=greetings.length){
            greeting_index=0;
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if(s.length()>0){
            hint.setVisibility(View.GONE);
            String temp = s.toString();
            int posDot = temp.indexOf(".");
            if (posDot > 0) {
                if (temp.length() - posDot - 1 > 2) {
                    s.delete(posDot + 3, posDot + 4);
                }
            }
            money=Double.parseDouble(s.toString());
            if(money> Constant.MAX_RP){
                btn_single_put_money.setEnabled(false);
                dropDownWarning.setText(String.format(getString(R.string.input_money_limited),Constant.MAX_RP));
                dropDownWarning.show();
            }else if(money< Constant.MIN_RP){
                btn_single_put_money.setEnabled(false);
                dropDownWarning.setText(String.format(getString(R.string.input_money_limited_minimum),Constant.MIN_RP));
                dropDownWarning.show();
            }else{
                dropDownWarning.hide();
                if(count>0&&count<=100&&money/count>=0.01){
                    btn_single_put_money.setEnabled(true);
                }else{
                    if(count>0&&money/count<0.01){
                        dropDownWarning.setText("单个红包金额必须大于0.01元");
                        dropDownWarning.show();
                    }
                    btn_single_put_money.setEnabled(false);
                }
            }
        }else{
            dropDownWarning.hide();
            hint.setVisibility(View.VISIBLE);
            money=0;
            btn_single_put_money.setEnabled(false);
        }
        tv_money.setText("￥"+df.format(money));
    }
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }
}
