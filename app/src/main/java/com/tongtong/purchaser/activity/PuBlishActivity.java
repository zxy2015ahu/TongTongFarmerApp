package com.tongtong.purchaser.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.model.FarmerReleaseInformationModel;
import com.tongtong.purchaser.model.ProduceType;
import com.tongtong.purchaser.model.PurchaserReleaseInformationModel;
import com.tongtong.purchaser.model.Region;
import com.tongtong.purchaser.utils.CodeUtil;
import com.tongtong.purchaser.utils.Constant;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.ProduceTypesHelper;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.widget.CustomGlobalLayoutListener;
import com.tongtong.purchaser.widget.NoScrollListView;
import com.tongtong.purchaser.widget.StyleableToast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Administrator on 2018-05-03.
 */

public class PuBlishActivity extends BaseActivity implements View.OnClickListener,
        TextWatcher{
    private EditText description;
    private TextView count,tv_type_name,num,tv_guige_name;
    private ProduceType produce;
    private String unit;
    private String num_text;
    private int select_position;
    private NoScrollListView list;
    private MyAdapter adapter;
    private HashMap<Integer,String> selecte_type=new HashMap<>();
    private CustomGlobalLayoutListener customGlobalLayoutListener;
    private Button confirm_btn;
    private ScrollView root;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.publish_layout);
        ((TextView)findViewById(R.id.title_text)).setText("发布采购");
        findViewById(R.id.back_bn).setOnClickListener(this);
        description=(EditText) findViewById(R.id.description);
        count=(TextView) findViewById(R.id.count);
        num=(TextView) findViewById(R.id.num);
        description.addTextChangedListener(this);
        findViewById(R.id.layout_pay_type).setOnClickListener(this);
        findViewById(R.id.layout_additional_msg).setOnClickListener(this);
        findViewById(R.id.tv_choose_guige).setOnClickListener(this);
        findViewById(R.id.line_one).setOnClickListener(this);
        tv_type_name=(TextView) findViewById(R.id.tv_type_name);
        tv_guige_name=(TextView) findViewById(R.id.tv_guige_name);
        list=(NoScrollListView) findViewById(R.id.list);
        confirm_btn=(Button) findViewById(R.id.confirm_bn);
        confirm_btn.setOnClickListener(this);
        root=(ScrollView)findViewById(R.id.root);
        findViewById(R.id.btn_add_card).setOnClickListener(this);
        customGlobalLayoutListener = new CustomGlobalLayoutListener(this, root, confirm_btn);
        root.getViewTreeObserver().addOnGlobalLayoutListener(customGlobalLayoutListener);
        adapter=new MyAdapter(this);
        Item item=new Item();
        item.title="全国";
        item.adcode=0;
        adapter.add(item);
        list.setAdapter(adapter);
    }
    @Override
    protected void onDestroy() {//防止内存溢出
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            root.getViewTreeObserver().removeOnGlobalLayoutListener(customGlobalLayoutListener);
        } else {
            root.getViewTreeObserver().removeGlobalOnLayoutListener(customGlobalLayoutListener);
        }
    }
    private class Item{
        public String title;
        public int adcode;
        public int id;
        public int a1=-1,a2=-1,a3=-1;
    }
    private class MyAdapter extends ArrayAdapter<Item>{
        public MyAdapter(Context context){
            super(context,0);
        }
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(convertView==null){
                convertView=getLayoutInflater().inflate(R.layout.source_item,null);
            }
           final Item item=getItem(position);
            ((TextView)convertView.findViewById(R.id.title)).setText(item.title);
            convertView.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(getCount()>1){
                        remove(item);
                    }else{
                        if(item.adcode>0){
                            item.adcode=0;
                            item.title="全国";
                            item.a1=-1;
                            item.a2=-1;
                            item.a3=-1;
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            });
            return convertView;
        }
    }
    @Override
    public void afterTextChanged(Editable s) {
        count.setText(String.valueOf(s.length()));
    }
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.back_bn){
            onBackPressed();
        }else if(v.getId()==R.id.layout_pay_type){
            Intent intent=new Intent();
            intent.putExtra("produce",produce);
            intent.setClass(this,Type2SelectActivity.class);
            startActivityForResult(intent,1);
        }else if(v.getId()==R.id.layout_additional_msg){
            Intent intent=new Intent();
            intent.putExtra("num",num_text);
            intent.putExtra("desc",num.getText().toString());
            intent.putExtra("select_position",select_position);
            intent.putExtra("unit",unit);
            intent.setClass(this,NumberSelectActivity.class);
            startActivityForResult(intent,2);
        }else if(v.getId()==R.id.btn_add_card){
            if(adapter.getCount()>=3){
                return;
            }
            Intent intent=new Intent();
            intent.setClass(this,SourceSelectActivity.class);
            startActivityForResult(intent,3);
        }else if(v.getId()==R.id.tv_choose_guige){
            if(produce==null){
                StyleableToast.info(this,"请选择货品");
                return;
            }
            int id=produce.getId();
            if(produce.getLevel()==3){
                ProduceType parent= ProduceTypesHelper.getParentProduce(this,id);
                id=parent.getId();
            }
            Intent intent=new Intent();
            intent.setClass(this,GuiGeSelectActivity.class);
            intent.putExtra("produce_id",id);
            intent.putExtra("guige",selecte_type);
            startActivityForResult(intent,4);
        }else if(v.getId()==R.id.line_one){
            Intent intent=new Intent();
            intent.putExtra(ChattingActivity.TARGET_ID, Constant.SERVER_ACCOUNT);
            intent.putExtra(ChattingActivity.TARGET_ESERVICE,ChattingActivity.TARGET_ESERVICE);
            intent.setClass(this,ChattingActivity.class);
            startActivity(intent);
        }else if(v.getId()==R.id.confirm_bn){
            if(produce==null){
                StyleableToast.info(this,"请选择货品");
                return;
            }
            if(TextUtils.isEmpty(num.getText().toString())){
                StyleableToast.info(this,"请设置采购数量");
                return;
            }
            release();
        }
    }

    private int is_contains_small(Item item){
        for(int i=0;i<adapter.getCount();i++){
            Item it=adapter.getItem(i);
            //省包含市或县
            if(it.a2==-1&&it.a3==-1){
                if(it.a1==item.a1&&item.a2!=-1){
                    return i;
                }
                //市包含县
            }else if(it.a2!=-1&&it.a3==-1){
                if(it.a1==item.a1&&item.a2==it.a2&&item.a3!=-1){
                    return i;
                }
            }

        }
        return -1;
    }
    private boolean is_same(Item item){
        for(int i=0;i<adapter.getCount();i++){
            Item it=adapter.getItem(i);
            if(it.a1==item.a1&&it.a2==-1&&item.a2==-1&&it.a3==-1&&item.a3==-1){
                return true;
            }else if(it.a1==item.a1&&item.a2==it.a2&&it.a3==-1&&item.a3==-1){
                return true;
            }else if(it.a1==item.a1&&item.a2==it.a2&&item.a3==it.a3){
                return true;
            }
        }
        return false;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK&&data!=null){
            if(requestCode==1){
                produce=(ProduceType) data.getSerializableExtra("produce");
                tv_type_name.setText(produce.getName());
            }else if(requestCode==2){
                num.setText(data.getStringExtra("desc"));
                unit=data.getStringExtra("unit");
                select_position=data.getIntExtra("select_position",0);
                num_text=data.getStringExtra("num");
            }else if(requestCode==3){
                int a1=data.getIntExtra("a1",-1);
                int a2=data.getIntExtra("a2",-1);
                int a3=data.getIntExtra("a3",-1);
                Region region=(Region) data.getSerializableExtra("region");
                Item item=new Item();
                item.adcode=region.getAdcode();
                item.id=region.getId();
                item.title=region.getRegion_name();
                item.a1=a1;
                item.a2=a2;
                item.a3=a3;
                if(is_same(item)){
                    return;
                }
                //代替全国
                if(adapter.getItem(0).adcode==0){
                    adapter.remove(adapter.getItem(0));
                    adapter.add(item);
                    return;
                }
                if(item.adcode==0){
                    adapter.clear();
                    adapter.add(item);
                    return;
                }
                int index=is_contains_small(item);
                if(index!=-1){
                    adapter.remove(adapter.getItem(index));
                    adapter.add(item);
                }else{
                    adapter.add(item);
                }
            }else if(requestCode==4){
                selecte_type=(HashMap<Integer, String>) data.getSerializableExtra("guige");
                StringBuffer sb=new StringBuffer();
                for(Map.Entry<Integer,String> type:selecte_type.entrySet()){
                    sb.append(type.getValue()).append(" ");
                }
                if(sb.length()>0){
                    sb=sb.deleteCharAt(sb.length()-1);
                }
                tv_guige_name.setText(sb.toString());
            }
        }
    }
    private int getAdcode(int index){
        if(adapter.getCount()==3){
            return adapter.getItem(index).adcode;
        }else if(adapter.getCount()==2){
            if(index<2){
                return adapter.getItem(index).adcode;
            }
            return -1;
        }else{
            if(index<1){
                return adapter.getItem(index).adcode;
            }
            return -1;
        }
    }
    private String getAdcodeAddress(){
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<adapter.getCount();i++){
            sb.append(adapter.getItem(i).title).append(",");
        }
        sb=sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }
    private void release(){
        HttpTask task=new HttpTask(this);
        task.setTaskHandler(new HttpTask.HttpTaskHandler() {
            @Override
            public void taskStart(int code) {
                showLoading("发布中...");
            }

            @Override
            public void taskSuccessful(String str, int code) {
                dismissLoading();
                JsonObject refreshResultJson = new JsonParser().parse(str)
                        .getAsJsonObject();
                int refreshResultCode = refreshResultJson.get("code").getAsInt();
                Gson gson=new Gson();
                JsonObject releaseInfo=refreshResultJson.get("releaseInfo").getAsJsonObject();
                PurchaserReleaseInformationModel info=gson.fromJson(releaseInfo,PurchaserReleaseInformationModel.class);
                if(refreshResultCode== CodeUtil.SUCCESS_CODE){
                    Intent data=new Intent();
                    data.putExtra("releaseInfo",info);
                    setResult(RESULT_OK,data);
                    finish();
                }
            }

            @Override
            public void taskFailed(int code) {
                dismissLoading();
            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("purchaser_id", UserUtil.getUserModel(this).getId());
        object.addProperty("token", UserUtil.getUserModel(this).getToken());
        object.addProperty("remarks",description.getText().toString());
        object.addProperty("produce_id",produce.getId());
        object.addProperty("produce_level",produce.getLevel());
        object.addProperty("adcode1",getAdcode(0));
        object.addProperty("adcode2",getAdcode(1));
        object.addProperty("adcode3",getAdcode(2));
        object.addProperty("adcode_address",getAdcodeAddress());
        object.addProperty("specific",tv_guige_name.getText().toString());
        object.addProperty("num",num.getText().toString());
        task.execute(UrlUtil.RELEASE,object.toString());
    }
}
