package com.tongtong.purchaser.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.application.MyApplication;
import com.tongtong.purchaser.model.AddressModel;
import com.tongtong.purchaser.model.Region;
import com.tongtong.purchaser.utils.DBManager;
import com.tongtong.purchaser.utils.HistoryHelper;
import com.tongtong.purchaser.utils.NetUtil;
import com.tongtong.purchaser.widget.NoScrollGridView;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.List;

/**
 * Created by Administrator on 2018-05-04.
 */

public class SourceSelectActivity extends BaseActivity implements View.OnClickListener,
        BDLocationListener{
    private Region pregion,cregion;
    private DBManager dbManager;
    private LinearLayout histoty_select;
    private TextView history_1,history_2;
    private TextView location;
    private int adcode;
    private LocationClient client;
    private int location_adcode;
    private String location_city;
    private int a1=-1,a2=-1,a3=-1;
    private AddressModel address;
    private Region select_region;
    private int grid_height;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.source_select_layout);
        ((TextView)findViewById(R.id.title_text)).setText("选择期望货源地");
        findViewById(R.id.back_bn).setOnClickListener(this);
        dbManager=new DBManager(this);
        grid_height= UIUtil.dip2px(this,35f);
        initData();
        client= MyApplication.getLocationClient();
        client.registerLocationListener(this);
        client.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.unRegisterLocationListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.back_bn){
            onBackPressed();
        }
    }
    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        if(bdLocation==null){
            client.restart();
            return;
        }
        client.stop();
        location_adcode=dbManager.getCityAdcode(bdLocation.getAdCode());
        location_city=bdLocation.getCity();
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                setData();
            }
        });
    }
    private class MyAdapter extends ArrayAdapter<Region> {
        public MyAdapter(Context ctx){
            super(ctx,0);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(convertView==null){
                convertView=getLayoutInflater().inflate(R.layout.common_grid_item,null);
            }
            TextView title=(TextView)convertView.findViewById(R.id.title);
            title.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,grid_height));
            Region region=getItem(position);
            title.setText(region.getRegion_name());
            return convertView;
        }
    }
    private void onselectaddress(Region region){
        this.select_region=region;
        adcode=region.getAdcode();
        if(adcode==0){
            a1=-1;
            a2=-1;
            a3=-1;
            address=new AddressModel();
        }else{
            address = dbManager.getshengshiqu(adcode);
            if(address.getCity()==-1){
                a1=adcode;
                a2=-1;
                a3=-1;
            }else if(address.getProvince()==-1){
                a2=adcode;
                a3=-1;
            }else{
                a3=adcode;
            }
            HistoryHelper.addHistory(SourceSelectActivity.this,select_region);
        }
        setResult(select_region);
    }
    private void setResult(Region region){
        Intent data=new Intent();
        data.putExtra("region",region);
        data.putExtra("a1",a1);
        data.putExtra("a2",a2);
        data.putExtra("a3",a3);
        setResult(RESULT_OK,data);
        finish();
    }
    private void setData(){
        List<Region> regions= HistoryHelper.getHistory(this);
        if(regions.size()>1){
            histoty_select.setVisibility(View.VISIBLE);
            history_1.setVisibility(View.VISIBLE);
            history_2.setVisibility(View.VISIBLE);
            history_1.setText(regions.get(0).getRegion_name());
            history_1.setTag(regions.get(0).getAdcode());
            history_2.setText(regions.get(1).getRegion_name());
            history_2.setTag(regions.get(1).getAdcode());
        }else if(regions.size()>0){
            histoty_select.setVisibility(View.VISIBLE);
            history_1.setText(regions.get(0).getRegion_name());
            history_1.setTag(regions.get(0).getAdcode());
            history_1.setVisibility(View.VISIBLE);
            history_2.setVisibility(View.GONE);
        }else{
            history_1.setVisibility(View.GONE);
            history_2.setVisibility(View.GONE);
            histoty_select.setVisibility(View.GONE);
        }
        location.setText(location_city);
    }
    private void initData(){
        final TextView select_box=(TextView) findViewById(R.id.select_box);
        final TextView back=(TextView) findViewById(R.id.back);
        location=(TextView) findViewById(R.id.location);
        histoty_select=(LinearLayout) findViewById(R.id.histoty_select);
        history_1=(TextView) findViewById(R.id.history_1);
        history_2=(TextView) findViewById(R.id.history_2);
        history_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adcode=(int)history_1.getTag();
                Region region=dbManager.getRegionByAdcode(adcode);
                HistoryHelper.addHistory(SourceSelectActivity.this,region);
                onselectaddress(region);
            }
        });
        history_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adcode=(int)history_2.getTag();
                Region region=dbManager.getRegionByAdcode(adcode);
                HistoryHelper.addHistory(SourceSelectActivity.this,region);
                onselectaddress(region);
            }
        });
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adcode=location_adcode;
                Region region=dbManager.getRegionByAdcode(adcode);
                HistoryHelper.addHistory(SourceSelectActivity.this,region);
                onselectaddress(region);
            }
        });

        back.setVisibility(View.GONE);
        select_box.setText("选择地区：");
        final NoScrollGridView list=(NoScrollGridView)findViewById(android.R.id.list);
        list.setHorizontalSpacing(1);
        list.setVerticalSpacing(1);
        final MyAdapter adapter=new MyAdapter(this);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position==0){
                    onselectaddress(adapter.getItem(0));
                    return;
                }
                pregion=adapter.getItem(position);
                select_box.setText("选择地区："+pregion.getRegion_name());
                a1=pregion.getAdcode();
                back.setVisibility(View.VISIBLE);
                dbManager.getData(pregion.getId(), new DBManager.OnDataRecieveListener() {
                    @Override
                    public void recieveData(List<Region> regions) {
                        adapter.clear();
                        Region p=new Region();
                        p.setAdcode(pregion.getAdcode());
                        p.setId(pregion.getId());
                        p.setParent_id(pregion.getParent_id());
                        p.setRegion_name("全"+pregion.getRegion_name());
                        adapter.add(p);
                        adapter.addAll(regions);
                        list.setAdapter(adapter);
                        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                if(position==0){
                                    onselectaddress(pregion);
                                    return;
                                }
                                cregion=adapter.getItem(position);
                                back.setVisibility(View.VISIBLE);
                                select_box.setText("选择地区："+pregion.getRegion_name()+"/"+cregion.getRegion_name());
                                a1=pregion.getAdcode();
                                a2=cregion.getAdcode();
                                dbManager.getData(cregion.getId(), new DBManager.OnDataRecieveListener() {
                                    @Override
                                    public void recieveData(List<Region> regions) {
                                        adapter.clear();
                                        Region c=new Region();
                                        c.setRegion_name("全"+cregion.getRegion_name());
                                        c.setParent_id(cregion.getParent_id());
                                        c.setId(cregion.getId());
                                        c.setAdcode(cregion.getAdcode());
                                        adapter.add(c);
                                        adapter.addAll(regions);
                                        list.setAdapter(adapter);
                                        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                if(position==0){
                                                    onselectaddress(cregion);
                                                    return;
                                                }
                                                final Region dregion=adapter.getItem(position);
                                                a1=pregion.getAdcode();
                                                a2=cregion.getAdcode();
                                                a3=dregion.getAdcode();
                                                onselectaddress(dregion);
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pregion!=null&&cregion!=null){
                    cregion=null;
                    back.setVisibility(View.VISIBLE);
                    select_box.setText("选择地区："+pregion.getRegion_name());
                    a1=pregion.getAdcode();
                    dbManager.getData(pregion.getId(), new DBManager.OnDataRecieveListener() {
                        @Override
                        public void recieveData(List<Region> regions) {
                            adapter.clear();
                            Region p=new Region();
                            p.setAdcode(pregion.getAdcode());
                            p.setId(pregion.getId());
                            p.setParent_id(pregion.getParent_id());
                            p.setRegion_name("全"+pregion.getRegion_name());
                            adapter.add(p);
                            adapter.addAll(regions);
                            list.setAdapter(adapter);
                            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    if(position==0){
                                        onselectaddress(pregion);
                                        return;
                                    }
                                    cregion=adapter.getItem(position);
                                    back.setVisibility(View.VISIBLE);
                                    select_box.setText("选择地区："+pregion.getRegion_name()+"/"+cregion.getRegion_name());
                                    a1=pregion.getAdcode();
                                    a2=cregion.getAdcode();
                                    dbManager.getData(cregion.getId(), new DBManager.OnDataRecieveListener() {
                                        @Override
                                        public void recieveData(List<Region> regions) {
                                            adapter.clear();
                                            Region c=new Region();
                                            c.setRegion_name("全"+cregion.getRegion_name());
                                            c.setParent_id(cregion.getParent_id());
                                            c.setId(cregion.getId());
                                            c.setAdcode(cregion.getAdcode());
                                            adapter.add(c);
                                            adapter.addAll(regions);
                                            list.setAdapter(adapter);
                                            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                @Override
                                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                    if(position==0){
                                                        onselectaddress(cregion);
                                                        return;
                                                    }
                                                    final Region dregion=adapter.getItem(position);
                                                    a1=pregion.getAdcode();
                                                    a2=cregion.getAdcode();
                                                    a3=dregion.getAdcode();
                                                    onselectaddress(dregion);
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }else if(pregion!=null){
                    select_box.setText("选择地区：");
                    back.setVisibility(View.GONE);
                    dbManager.getData(-1, new DBManager.OnDataRecieveListener() {
                        @Override
                        public void recieveData(List<Region> regions) {
                            adapter.clear();
                            Region r=new Region();
                            r.setAdcode(0);
                            r.setRegion_name("全国");
                            adapter.add(r);
                            adapter.addAll(regions);
                            list.setAdapter(adapter);
                            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    if(position==0){
                                        onselectaddress(adapter.getItem(0));
                                        return;
                                    }
                                    pregion=adapter.getItem(position);
                                    back.setVisibility(View.VISIBLE);
                                    select_box.setText("选择地区："+pregion.getRegion_name());
                                    a1=pregion.getAdcode();
                                    dbManager.getData(pregion.getId(), new DBManager.OnDataRecieveListener() {
                                        @Override
                                        public void recieveData(List<Region> regions) {
                                            adapter.clear();
                                            Region p=new Region();
                                            p.setAdcode(pregion.getAdcode());
                                            p.setId(pregion.getId());
                                            p.setParent_id(pregion.getParent_id());
                                            p.setRegion_name("全"+pregion.getRegion_name());
                                            adapter.add(p);
                                            adapter.addAll(regions);
                                            list.setAdapter(adapter);
                                            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                @Override
                                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                    if(position==0){
                                                        onselectaddress(pregion);
                                                        return;
                                                    }
                                                    cregion=adapter.getItem(position);
                                                    back.setVisibility(View.VISIBLE);
                                                    select_box.setText("选择地区："+pregion.getRegion_name()+"/"+cregion.getRegion_name());
                                                    a1=pregion.getAdcode();
                                                    a2=cregion.getAdcode();
                                                    dbManager.getData(cregion.getId(), new DBManager.OnDataRecieveListener() {
                                                        @Override
                                                        public void recieveData(List<Region> regions) {
                                                            adapter.clear();
                                                            Region c=new Region();
                                                            c.setRegion_name("全"+cregion.getRegion_name());
                                                            c.setParent_id(cregion.getParent_id());
                                                            c.setId(cregion.getId());
                                                            c.setAdcode(cregion.getAdcode());
                                                            adapter.add(c);
                                                            adapter.addAll(regions);
                                                            list.setAdapter(adapter);
                                                            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                                @Override
                                                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                                    if(position==0){
                                                                        onselectaddress(cregion);
                                                                        return;
                                                                    }
                                                                    final Region dregion=adapter.getItem(position);
                                                                    a1=pregion.getAdcode();
                                                                    a2=cregion.getAdcode();
                                                                    a3=dregion.getAdcode();
                                                                    onselectaddress(dregion);
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }
        });
        dbManager.getData(-1, new DBManager.OnDataRecieveListener() {
            @Override
            public void recieveData(List<Region> regions) {
                Region r=new Region();
                r.setAdcode(0);
                r.setRegion_name("全国");
                select_region=r;
                adapter.add(r);
                adapter.addAll(regions);
                list.setAdapter(adapter);
            }
        });
    }
}
