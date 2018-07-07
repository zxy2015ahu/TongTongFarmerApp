package com.tongtong.purchaser.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.ToastUtil;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;



/**
 * Created by Administrator on 2018-02-01.
 */

public class OrderDetailsActivity extends BaseActivity implements View.OnClickListener,
HttpTask.HttpTaskHandler{
    private View progress;
    private TextView send_address;
    private double send_lat,send_lng;
    private String title,address;
    private int TAG;
    private static final int NEED_PAY=1;
    private Button btn;
    private View bottom,custom;
    private static OrderDetailsActivity instance;
    public static OrderDetailsActivity getInstance(){
        return instance;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_details_layout);
        instance=this;
        findViewById(R.id.back_bn).setOnClickListener(this);
        progress=findViewById(R.id.progress);
        btn=(Button) findViewById(R.id.btn);
        bottom=findViewById(R.id.bottom);
        custom=findViewById(R.id.custom);
        HttpTask httpTask=new HttpTask(this);
        String obj=getIntent().getStringExtra("object");
        httpTask.setTaskHandler(this);
        JsonObject dataJson = new JsonObject();
        dataJson.addProperty("token", UserUtil.getUserModel(this).getToken());
        dataJson.addProperty("order_id",getIntent().getIntExtra("order_id",0));
        httpTask.execute(UrlUtil.GET_ORDER,dataJson.toString());
        send_address=((TextView)findViewById(R.id.send_address));
        send_address.setMovementMethod(LinkMovementMethod.getInstance());
    }
    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.back_bn){
            onBackPressed();
        }
    }
    @Override
    public void taskSuccessful(String str, int code) {
        progress.setVisibility(View.GONE);
        JsonObject selectResultJson = new JsonParser().parse(str)
                .getAsJsonObject();
        int selectResultCode = selectResultJson.get("code").getAsInt();
        if (verification(selectResultCode)) {
            final JsonObject order=selectResultJson.get("order").getAsJsonObject();
            ((TextView)findViewById(R.id.name)).setText(selectResultJson.get("farmer_name").getAsString());
            ((TextView)findViewById(R.id.order_no)).setText(order.get("order_no").getAsString());
            ((TextView)findViewById(R.id.status)).setText(selectResultJson.get("status_name").getAsString());
            ((TextView)findViewById(R.id.produce_name)).setText(selectResultJson.get("produce_name").getAsString());
            ((TextView)findViewById(R.id.amount)).setText(Html.fromHtml("<font color=\"#DF8D06\">"+order.get("amount").getAsString()+"</font>"+selectResultJson.get("unit").getAsString()));
            ((TextView)findViewById(R.id.price)).setText(Html.fromHtml("<font color=\"#DF8D06\">"+order.get("price").getAsDouble()+"</font>"+"元/"+selectResultJson.get("unit").getAsString()));
            ((TextView)findViewById(R.id.beizhu)).setText(order.get("beizhu").getAsString());
            send_address.setText(getString("送货地址："+order.get("address_content").getAsString()+"("+order.get("address_title").getAsString()+")"));
            String dingjin=order.get("dingjin").getAsString();
            int status=order.get("status").getAsInt();
            final double price=order.get("price").getAsDouble();
            final String unit=selectResultJson.get("unit").getAsString();
            final String name=selectResultJson.get("produce_name").getAsString();
            final String purchase_phone=order.get("purchase_phone").getAsString();
            final String farmer_phone=selectResultJson.get("farmer_phone").getAsString();
            ((TextView)findViewById(R.id.dingjin)).setText(Html.fromHtml("<font color=\"#DF8D06\">"+(TextUtils.isEmpty(dingjin)?"0":dingjin)+"</font>元"));
            if(status>=6||status==3||status==4){
                ((TextView)findViewById(R.id.title_text)).setText("收购单详情");
            }else{
                ((TextView)findViewById(R.id.title_text)).setText("订单详情");
            }
            if(status==1){
                if(!"0".equals(dingjin)){
                    ((TextView)findViewById(R.id.dingjin)).setText(Html.fromHtml("<font color=\"#DF8D06\">"+(TextUtils.isEmpty(dingjin)?"0":dingjin)+"</font>元<font color=\"#1aad19\">(待支付)</font>"));
                    bottom.setVisibility(View.VISIBLE);
                    btn.setVisibility(View.VISIBLE);
                    btn.setText("支付订金");
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    });
                }
            }else if(status==5){
                bottom.setVisibility(View.VISIBLE);
                btn.setVisibility(View.VISIBLE);
                btn.setText("已过磅，填写收购单");
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent=new Intent();
                        intent.putExtra("order_id",getIntent().getIntExtra("order_id",0));
                        intent.putExtra("price",price);
                        intent.putExtra("unit",unit);
                        intent.putExtra("name",name);
                        intent.putExtra("price",price);
                        intent.putExtra("farmer_phone",farmer_phone);
                        intent.putExtra("purchase_phone",purchase_phone);
                        intent.setClass(OrderDetailsActivity.this,GuobangDetailsActivity.class);
                        startActivity(intent);
                    }
                });
            }else if(status>=6||status==3||status==4){
                ((TextView)findViewById(R.id.send_time)).setVisibility(View.GONE);
                ((TextView)findViewById(R.id.add_time)).setVisibility(View.GONE);
                send_address.setText("订单时间："+order.get("sg_add_time").getAsString());
                ((TextView)findViewById(R.id.beizhu)).setText(order.get("sgbeizhu").getAsString());
                custom.setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.jinzhong)).setText(Html.fromHtml("<font color=\"#DF8D06\">"+order.get("jinzhong").getAsString()+"</font>"+selectResultJson.get("unit").getAsString()));
                ((TextView)findViewById(R.id.pizhong)).setText(Html.fromHtml("<font color=\"#DF8D06\">"+order.get("pizhong").getAsString()+"</font>"+selectResultJson.get("unit").getAsString()));
                ((TextView)findViewById(R.id.maozhong)).setText(Html.fromHtml("<font color=\"#DF8D06\">"+order.get("maozhong").getAsString()+"</font>"+selectResultJson.get("unit").getAsString()));
                ((TextView)findViewById(R.id.total)).setText(Html.fromHtml("<font color=\"#DF8D06\">"+order.get("total").getAsString()+"</font>元"));
                bottom.setVisibility(View.VISIBLE);
                btn.setVisibility(View.VISIBLE);
                btn.setText("立即支付");
                findViewById(R.id.amount_layout).setVisibility(View.GONE);
            }
            ((TextView)findViewById(R.id.send_time)).setText("送货截止时间："+order.get("send_last_time").getAsString());
            ((TextView)findViewById(R.id.add_time)).setText("下单时间："+order.get("add_time").getAsString());
            send_lat=order.get("send_lat").getAsDouble();
            send_lng=order.get("send_lng").getAsDouble();
            address=order.get("address_title").getAsString();
            title=order.get("address_content").getAsString();
        }else{
            ToastUtil.showShortToast(this,"获取数据失败！");
        }
    }
    @Override
    public void taskFailed(int code) {
        ToastUtil.showShortToast(this,"获取数据失败！");
    }
    @Override
    public void taskStart(int code) {

    }
    private SpannableStringBuilder getString(String text){
        SpannableStringBuilder builder=new SpannableStringBuilder();
        builder.append(text);
        UnderlineSpan span=new UnderlineSpan();
        builder.setSpan(span,5,text.length(),Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        ClickableSpan clickableSpan=new ClickableSpan() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.putExtra("lat",send_lat);
                intent.putExtra("lng",send_lng);
                intent.putExtra("address",address);
                intent.putExtra("title",title);
                intent.setClass(OrderDetailsActivity.this,NavigateActivity.class);
                startActivity(intent);
            }
        };
        builder.setSpan(clickableSpan,5,text.length(),Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        return builder;
    }
}
