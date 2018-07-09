package com.tongtong.purchaser.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.alibaba.mobileim.conversation.YWConversation;
import com.alibaba.mobileim.conversation.YWMessage;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.application.MyApplication;
import com.tongtong.purchaser.bean.SearchAddressInfo;
import com.tongtong.purchaser.helper.ChattingOperationCustom;
import com.tongtong.purchaser.model.FarmerReleaseInformationModel;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.view.AutoListView;
import com.tongtong.purchaser.view.Mydivider;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.addapp.pickers.picker.DateTimePicker;
import dmax.dialog.SpotsDialog;

import static com.tongtong.purchaser.R.id.num;
import static com.tongtong.purchaser.R.id.price;

/**
 * Created by Administrator on 2018-01-29.
 */

public class OrderInfoActivity extends BaseActivity implements View.OnClickListener,HttpTask.HttpTaskHandler,
        CompoundButton.OnCheckedChangeListener{
    private JsonObject releaseInfo;
    private EditText zhongliang_price,zhonglinag_num,zhonglinag_total,amount_price,amount_num,amount_total,total_num,total_price,total_total;
    private TextView time,address;
    private DateTimePicker picker;
    private DisplayMetrics dm;
    private SimpleDateFormat sdf;
    private SearchAddressInfo info;
    private EditText beizhu;
    private SpotsDialog dialog;
    private EditText dingjin;
    private TextView number_unit,price_unit;
    private BottomSheetDialog number_dialog,price_dialog;
    private SharedPreferences sp;
    private String punit="",aunit="",estimatedQuantity="",runit;
    private RadioButton an_amount,an_price,an_total;
    private View zhongliang,amount_view,total_view;
    private static final int TYPE_ZHONG_LINAG_ORDER=1;
    private static final int TYPE_AMOUNT_ORDER=2;
    private static final int TYPE_TOTAL_ORDER=3;
    private int type=TYPE_ZHONG_LINAG_ORDER;
    private ViewStub amount,total;
    private String area,price;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_info_layout);
        findViewById(R.id.back_bn).setOnClickListener(this);
        sp=getSharedPreferences("location", Context.MODE_PRIVATE);
        ((TextView)findViewById(R.id.title_text)).setText("订单生成");
        String release=getIntent().getStringExtra("releaseInfo");
        releaseInfo=new JsonParser().parse(release).getAsJsonObject();
        ((TextView)findViewById(R.id.name)).setText(releaseInfo.get("f_name").getAsString());
        ((TextView)findViewById(R.id.produce_name)).setText(releaseInfo.get("p_name").getAsString());
        number_unit=((TextView)findViewById(R.id.zhongliang_number_unit));
        price_unit=((TextView)findViewById(R.id.zhongliang_price_unit));
        an_amount=(RadioButton)findViewById(R.id.money_amount_layout);
        an_price=(RadioButton)findViewById(R.id.goods_now_price);
        an_total=(RadioButton)findViewById(R.id.tv_total_money);
        amount=(ViewStub) findViewById(R.id.amount);
        total=(ViewStub) findViewById(R.id.total);
        an_amount.setOnCheckedChangeListener(this);
        an_price.setOnCheckedChangeListener(this);
        an_total.setOnCheckedChangeListener(this);
        zhongliang=findViewById(R.id.zhongliang);
        an_amount.setChecked(true);
        punit=releaseInfo.get("punit").getAsString();
        aunit=releaseInfo.get("aunit").getAsString();
        runit=releaseInfo.get("runit").getAsString();
        area=releaseInfo.get("area").getAsString();
        price=releaseInfo.get("price").getAsString();
        estimatedQuantity=releaseInfo.get("estimatedQuantity").getAsString();
        price_unit.setText("元/"+punit);
        number_unit.setText(aunit);
        price_unit.setOnClickListener(this);
        number_unit.setOnClickListener(this);
        zhongliang_price=(EditText) findViewById(R.id.zhongliang_price);
        zhonglinag_total=(EditText) findViewById(R.id.zhongliang_total);
        zhonglinag_num=(EditText) findViewById(R.id.zhongliang_num);
        zhonglinag_num.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()>0){
                    if(zhongliang_price.getText().toString().length()>0){
                        double to=Double.valueOf(s.toString())*Double.valueOf(zhongliang_price.getText().toString());
                        if(to%1.0==0){
                            zhonglinag_total.setText(String.valueOf((int)to));
                        }else{
                            zhonglinag_total.setText(String.format("%.2f",to));
                        }
                    }
                }
            }
        });
        zhongliang_price.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()>0){
                    if(zhonglinag_num.getText().toString().length()>0){
                        double to=Double.valueOf(s.toString())*Double.valueOf(zhonglinag_num.getText().toString());
                        if(to%1.0==0){
                            zhonglinag_total.setText(String.valueOf((int)to));
                        }else{
                            zhonglinag_total.setText(String.format("%.2f",to));
                        }
                    }
                }
            }
        });
        dingjin=(EditText) findViewById(R.id.dingjin);
        zhonglinag_num.setText(estimatedQuantity);
        zhongliang_price.setHint("该农户预期价格为"+price+"元/"+punit);
        findViewById(R.id.time_select).setOnClickListener(this);
        findViewById(R.id.address_select).setOnClickListener(this);
        findViewById(R.id.order).setOnClickListener(this);
        beizhu=(EditText) findViewById(R.id.beizhu);
        time=(TextView) findViewById(R.id.time);
        address=(TextView) findViewById(R.id.address);
        sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dm=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
    }

    private void showAmount(){
        zhongliang.setVisibility(View.GONE);
        if(total_view!=null){
            total_view.setVisibility(View.GONE);
        }
        if(amount_view==null){
            amount_view=amount.inflate();
            amount_num=(EditText) amount_view.findViewById(R.id.amount_num);
            amount_price=(EditText) amount_view.findViewById(R.id.amount_price);
            amount_total=(EditText) amount_view.findViewById(R.id.amount_total);
            ((TextView)amount_view.findViewById(R.id.number_unit)).setText(runit);
            ((TextView)amount_view.findViewById(R.id.price_unit)).setText("元/"+punit);
            amount_num.setText(area);
        }
        amount_view.setVisibility(View.VISIBLE);
    }
    private void showTotal(){
        zhongliang.setVisibility(View.GONE);
        if(amount_view!=null){
            amount_view.setVisibility(View.GONE);
        }
        if(total_view==null){
            total_view=total.inflate();
            total_num=(EditText) total_view.findViewById(R.id.total_num);
            total_price=(EditText) total_view.findViewById(R.id.total_price);
            total_total=(EditText) total_view.findViewById(R.id.total_total);
            ((TextView)total_view.findViewById(R.id.total_unit)).setText(runit);
            ((TextView)total_view.findViewById(R.id.total_unit_unit)).setText(aunit);
            total_num.setText(estimatedQuantity);
            total_price.setText(area);
        }
        total_view.setVisibility(View.VISIBLE);
    }
    private void showZhongliang(){
        if(amount_view!=null){
            amount_view.setVisibility(View.GONE);
        }
        if(total_view!=null){
            total_view.setVisibility(View.GONE);
        }
        zhongliang.setVisibility(View.VISIBLE);
    }
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked){
            switch (buttonView.getId()){
                case R.id.money_amount_layout:
                    type=TYPE_ZHONG_LINAG_ORDER;
                    showZhongliang();
                    break;
                case R.id.goods_now_price:
                    type=TYPE_AMOUNT_ORDER;
                    showAmount();
                    break;
                case R.id.tv_total_money:
                    type=TYPE_TOTAL_ORDER;
                    showTotal();
                    break;
            }
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.back_bn){
            onBackPressed();
        }else if(R.id.address_select==view.getId()){
            Intent intent=new Intent();
            intent.putExtra("flag","need_result");
            intent.setClass(OrderInfoActivity.this,LocationSelectActivity.class);
            startActivityForResult(intent,2);
        }else if(view.getId()==R.id.time_select){
            onYearMonthDayTimePicker();
        }else if(view.getId()==R.id.number_unit){
            if(number_dialog==null){
                number_dialog=new BottomSheetDialog(this);
                View v=View.inflate(this,R.layout.dialog_layout,null);
                RecyclerView list=(RecyclerView) v.findViewById(R.id.list);
                List<String> items=new ArrayList<>();
                String u=sp.getString("unit","");
                String[] units=u.split("@")[1].split(",");
                for(int i=0;i<units.length;i++){
                    items.add(units[i]);
                }
                MyAmountAdapter adapter=new MyAmountAdapter(items);
                LinearLayoutManager manager=new LinearLayoutManager(this);
                manager.setOrientation(LinearLayoutManager.VERTICAL);
                list.setLayoutManager(manager);
                Mydivider divider=new Mydivider(ContextCompat.getColor(this,R.color.aliwx_wx_line_color),getResources().getDimensionPixelSize(R.dimen.line_height));
                divider.setDrawLastItem(false);
                divider.setDrawHeaderFooter(false);
                list.addItemDecoration(divider);
                list.setAdapter(adapter);
                v.findViewById(R.id.close_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        number_dialog.dismiss();
                    }
                });
                ViewGroup.LayoutParams params=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dm.heightPixels*2/3);
                number_dialog.setContentView(v,params);
            }
            number_dialog.show();
        }else if(view.getId()==R.id.price_unit){
            if(price_dialog==null){
                price_dialog=new BottomSheetDialog(this);
                View v=View.inflate(this,R.layout.dialog_layout,null);
                RecyclerView list=(RecyclerView) v.findViewById(R.id.list);
                List<String> items=new ArrayList<>();
                String u=sp.getString("unit","");
                String[] units=u.split("@")[0].split(",");
                for(int i=0;i<units.length;i++){
                    items.add(units[i]);
                }
                MyPriceAdapter adapter=new MyPriceAdapter(items);
                LinearLayoutManager manager=new LinearLayoutManager(this);
                manager.setOrientation(LinearLayoutManager.VERTICAL);
                list.setLayoutManager(manager);
                Mydivider divider=new Mydivider(ContextCompat.getColor(this,R.color.aliwx_wx_line_color),getResources().getDimensionPixelSize(R.dimen.line_height));
                divider.setDrawLastItem(false);
                divider.setDrawHeaderFooter(false);
                list.addItemDecoration(divider);
                list.setAdapter(adapter);
                v.findViewById(R.id.close_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        price_dialog.dismiss();
                    }
                });
                ViewGroup.LayoutParams params=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dm.heightPixels*2/3);
                price_dialog.setContentView(v,params);
            }
            price_dialog.show();
        }else if(view.getId()==R.id.order){
            if(type==TYPE_ZHONG_LINAG_ORDER){
                if(TextUtils.isEmpty(zhongliang_price.getText().toString().trim())){
                    showTips("请输入订购单价。");
                    return;
                }
                if(TextUtils.isEmpty(zhonglinag_num.getText().toString().trim())){
                    showTips("请输入订购数量。");
                    return;
                }
                if(Float.valueOf(zhonglinag_num.getText().toString())>releaseInfo.get("estimatedQuantity").getAsInt()){
                    showTips("订购数量不能超过农户能提供的产品数量。");
                    return;
                }
            }

            if(TextUtils.isEmpty(address.getText().toString())){
                showTips("请选择交货地点。");
                return;
            }
            if(TextUtils.isEmpty(time.getText().toString())){
                showTips("请选择交货时间。");
                return;
            }
            try{
                if(sdf.parse(time.getText().toString()).before(new Date())){
                    showTips("交货时间必须大于当前时间。");
                    return;
                }
            }catch (Exception e0){

            }
            HttpTask task=new HttpTask(this);
            task.setTaskHandler(this);
            JsonObject requestJson = new JsonObject();
            requestJson.addProperty("token", UserUtil.getUserModel(this).getToken());
            requestJson.addProperty("farmer_id", releaseInfo.get("f_id").getAsInt());
            requestJson.addProperty("produce_id",releaseInfo.get("p_id").getAsInt());
            requestJson.addProperty("purchase_phone",UserUtil.getUserModel(this).getPhone());
            requestJson.addProperty("address_title",info.title);
            requestJson.addProperty("address_content",info.addressName);
            requestJson.addProperty("send_last_time",time.getText().toString());
            requestJson.addProperty("send_lat",info.latLonPoint.latitude);
            requestJson.addProperty("send_lng",info.latLonPoint.longitude);
            requestJson.addProperty("beizhu",beizhu.getText().toString());
            //requestJson.addProperty("price",price.getText().toString());
            requestJson.addProperty("produce_name",releaseInfo.get("p_name").getAsString());
           // requestJson.addProperty("amount",num.getText().toString());
            requestJson.addProperty("dingjin",TextUtils.isEmpty(dingjin.getText().toString())?0:Integer.valueOf(dingjin.getText().toString()));
            requestJson.addProperty("purchase_id",UserUtil.getUserModel(this).getId());
            requestJson.addProperty("purchase_name",UserUtil.getUserModel(this).getName());
            requestJson.addProperty("purchase_phone",UserUtil.getUserModel(this).getPhone());
            requestJson.addProperty("unit",releaseInfo.get("punit").getAsString());
            task.execute(UrlUtil.ADDORDER,requestJson.toString());
        }
    }

    private class  MyPriceAdapter extends RecyclerView.Adapter<MyViewHolder>{
        private List<String> items;
        public MyPriceAdapter(List<String> items){
            this.items=items;
        }
        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view= getLayoutInflater().inflate(R.layout.simple_text_item,parent, false);
            MyViewHolder holder=new MyViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final String item=items.get(position);
            holder.item.setText(item);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    price_unit.setText("元/"+item);
                    punit=item;
                    price_dialog.dismiss();
                }
            });
        }
    }
    private class  MyAmountAdapter extends RecyclerView.Adapter<MyViewHolder>{
        private List<String> items;
        public MyAmountAdapter(List<String> items){
            this.items=items;
        }
        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view= getLayoutInflater().inflate(R.layout.simple_text_item,parent, false);
            MyViewHolder holder=new MyViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final String item=items.get(position);
            holder.item.setText(item);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    number_unit.setText(item);
                    number_dialog.dismiss();
                }
            });
        }
    }
    private class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView item;
        public View itemView;
        public MyViewHolder(View itemView){
            super(itemView);
            this.itemView=itemView;
            item=(TextView) itemView.findViewById(R.id.title);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==2){
                info=data.getParcelableExtra("position");
                address.setText(info.addressName+(TextUtils.isEmpty(info.title)?"":"("+info.title+")"));
            }
        }
    }
    private void onYearMonthDayTimePicker() {
        if(picker==null){
            Calendar calendar=Calendar.getInstance(Locale.getDefault());
            picker = new DateTimePicker(this, DateTimePicker.HOUR_24);
            picker.setDateRangeStart(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            picker.setDateRangeEnd(calendar.get(Calendar.YEAR)+5, 12, 31);
            picker.setTimeRangeStart(0, 0);
            picker.setTimeRangeEnd(23, 30);
            picker.setWeightEnable(true);
            picker.setWheelModeEnable(true);
            picker.setTitleText("选择交货日期");
            picker.setSubmitTextColor(getResources().getColor(R.color.colorPrimary));
            picker.setSelectedTextColor(getResources().getColor(R.color.colorPrimary));
            picker.setLineColor(getResources().getColor(R.color.colorPrimary));
            calendar.add(Calendar.DAY_OF_MONTH,1);
            picker.setSelectedItem(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH)+1,calendar.get(Calendar.DAY_OF_MONTH),0,0);
            picker.setOnDateTimePickListener(new DateTimePicker.OnYearMonthDayTimePickListener() {
                @Override
                public void onDateTimePicked(String year, String month, String day, String hour, String minute) {
                    time.setText(year + "-" + month + "-" + day + " " + hour + ":" + minute+":00");
                }
            });
        }
        picker.show();
    }

    @Override
    public void taskStart(int code) {
        dialog=new SpotsDialog(this,"正在生成订单……",R.style.Custom);
        dialog.show();
    }

    @Override
    public void taskFailed(int code) {
        dialog.dismiss();
        showToast("订单生成失败！");
    }

    @Override
    public void taskSuccessful(String str, int code) {
        dialog.dismiss();
        JsonObject resultJson = new JsonParser().parse(str).getAsJsonObject();
        int resultCode = resultJson.get("code").getAsInt();
        if (verification(resultCode)) {
            int order_id=resultJson.get("order_id").getAsInt();
            YWConversation conversation= MyApplication.getConversation(releaseInfo.get("f_phone").getAsString());
            if(conversation!=null){
//                YWMessage message=ChattingOperationCustom.createCustomOrderMessage(releaseInfo.get("p_name").getAsString(),
//                        Double.valueOf(price.getText().toString()),Integer.valueOf(num.getText().toString()),
//                                order_id,releaseInfo.get("punit").getAsString(),TextUtils.isEmpty(dingjin.getText().toString())?0:Integer.valueOf(dingjin.getText().toString()),UserUtil.getUserModel(OrderInfoActivity.this).getPhone(),info.latLonPoint.latitude,info.latLonPoint.longitude,info.title,info.addressName);
//                conversation.getMessageSender().sendMessage(message,120,null);
                ChattingOperationCustom.sendSysMsg("你向农户发出预订",releaseInfo.get("f_phone").getAsString());
                ChattingOperationCustom.sendTransMsg("有收购人向你发来预订单",releaseInfo.get("f_phone").getAsString());
//                YWSystemMessage ywSystemMessage=new YWSystemMessage();
//                ywSystemMessage.setSubType(YWMessage.SUB_MSG_TYPE.IM_SYSTEM_TIP);
//                ywSystemMessage.setContent("这条是系统消息");
//                UserUtil.getIMKitInstance(OrderInfoActivity.this).getConversationService().getSystemConversation()
//                        .sendMessage(ywSystemMessage,120,null);
                finish();
            }
        }
    }
}
