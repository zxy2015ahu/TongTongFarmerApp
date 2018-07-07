package com.tongtong.purchaser.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.application.MyApplication;
import com.tongtong.purchaser.frament.ProvinceFragment;
import com.tongtong.purchaser.model.Region;
import com.tongtong.purchaser.utils.DBManager;
import com.tongtong.purchaser.utils.HistoryHelper;
import com.tongtong.purchaser.view.EditTextWithDeleteButton;

import java.util.List;

/**
 * Created by zxy on 2018/4/9.
 */

public class CitySelectActivity extends BaseActivity implements View.OnClickListener,
        BDLocationListener,TextWatcher,DBManager.OnDataSearchListener,AdapterView.OnItemClickListener{
    private LinearLayout histoty_select;
    private TextView location;
    private LocationClient client;
    private int adcode;
    private TextView history_1,history_2;
    private View root,search_badge;
    private EditTextWithDeleteButton search;
    private DBManager dbManager;
    private ListView list;
    private TextView tv_no_data;
    private MyAdapter adapter;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.city_select_layout);
        findViewById(R.id.back_bn).setOnClickListener(this);
        list=(ListView) findViewById(android.R.id.list);
        list.setOnItemClickListener(this);
        tv_no_data=(TextView) findViewById(R.id.tv_no_data);
        list.setEmptyView(tv_no_data);
        adapter=new MyAdapter(this);
        search=(EditTextWithDeleteButton)findViewById(R.id.search);
        search.addTextChangedListener(this);
        ((TextView)findViewById(R.id.title_text)).setText("选择位置");
        histoty_select=(LinearLayout) findViewById(R.id.histoty_select);
        location=(TextView) findViewById(R.id.location);
        history_1=(TextView) findViewById(R.id.history_1);
        history_2=(TextView) findViewById(R.id.history_2);
        root=findViewById(R.id.root);
        search_badge=findViewById(R.id.search_badge);
        history_1.setOnClickListener(this);
        history_2.setOnClickListener(this);
        location.setOnClickListener(this);
        dbManager=new DBManager(this);
        List<Region> regions= HistoryHelper.getHistory(this);
        if(regions.size()>1){
            histoty_select.setVisibility(View.VISIBLE);
            history_1.setText(regions.get(0).getRegion_name());
            history_1.setTag(regions.get(0).getAdcode());
            history_2.setText(regions.get(1).getRegion_name());
            history_2.setTag(regions.get(1).getAdcode());
        }else if(regions.size()>0){
            histoty_select.setVisibility(View.VISIBLE);
            history_1.setText(regions.get(0).getRegion_name());
            history_1.setTag(regions.get(0).getAdcode());
            history_2.setVisibility(View.GONE);
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.tv_choose_city,new ProvinceFragment()).commit();
        location.setText("定位中...");
        client= MyApplication.getLocationClient();
        client.registerLocationListener(this);
        client.start();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Region region=adapter.getItem(position);
        HistoryHelper.addHistory(this,region);
        Intent data=new Intent();
        data.putExtra("region_name",region.getRegion_name());
        data.putExtra("adcode",region.getAdcode());
        setResult(RESULT_OK,data);
        finish();
    }
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.back_bn){
            onBackPressed();
        }else if(v.getId()==R.id.history_1){
            Intent data=new Intent();
            data.putExtra("region_name",history_1.getText().toString());
            data.putExtra("adcode",(int)history_1.getTag());
            setResult(RESULT_OK,data);
            finish();
        }else if(v.getId()==R.id.history_2){
            Intent data=new Intent();
            data.putExtra("region_name",history_2.getText().toString());
            data.putExtra("adcode",(int)history_2.getTag());
            setResult(RESULT_OK,data);
            finish();
        }else if(v.getId()==R.id.location){
            if(adcode!=0){
                Intent data=new Intent();
                data.putExtra("region_name",location.getText().toString());
                data.putExtra("adcode",(int)location.getTag());
                setResult(RESULT_OK,data);
                finish();
            }
        }
    }
    @Override
    public void onReceiveLocation(final BDLocation bdLocation) {
        if(bdLocation!=null){
            client.stop();
            adcode=Integer.valueOf(bdLocation.getAdCode());
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    location.setText(bdLocation.getCity());
                    location.setTag(adcode);
                }
            });
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if(s.toString().trim().length()>0){
            dbManager.searchData(s.toString().trim(),this);
        }else{
            root.setVisibility(View.VISIBLE);
            search_badge.setVisibility(View.GONE);
        }
    }

    private class MyAdapter extends ArrayAdapter<Region>{
        public MyAdapter(Context context){
            super(context,0);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(convertView==null){
                convertView=getLayoutInflater().inflate(R.layout.simple_text_item,null);
            }
            TextView title=(TextView) convertView.findViewById(R.id.title);
            Region region=getItem(position);
            title.setText(region.getRegion_name());
            return convertView;
        }
    }
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void recieveData(List<Region> regions) {
            search_badge.setVisibility(View.VISIBLE);
            root.setVisibility(View.GONE);
            adapter.clear();
            adapter.addAll(regions);
            list.setAdapter(adapter);
    }
}
