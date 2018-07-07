package com.tongtong.purchaser.frament;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tongtong.purchaser.R;
import com.tongtong.purchaser.model.Region;
import com.tongtong.purchaser.utils.DBManager;
import com.tongtong.purchaser.utils.HistoryHelper;
import com.tongtong.purchaser.widget.NoScrollGridView;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Created by zxy on 2018/4/9.
 */

public class CityFragment extends BaseFrament implements AdapterView.OnItemClickListener,
        DBManager.OnDataRecieveListener,View.OnClickListener{
    private NoScrollGridView list;
    private DBManager dbManager;
    private MyAdapter adapter;
    private int grid_height;
    private TextView select_box;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.city_grid_list2,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        list=(NoScrollGridView) view.findViewById(android.R.id.list);
        list.setHorizontalSpacing(1);
        list.setVerticalSpacing(1);
        list.setOnItemClickListener(this);
        view.findViewById(R.id.back).setOnClickListener(this);
        select_box=(TextView) view.findViewById(R.id.select_box);
        select_box.setText(getArguments().getString("name"));
        grid_height= UIUtil.dip2px(getActivity(),35f);
        adapter=new MyAdapter(getActivity());
        dbManager=new DBManager(getActivity());
        dbManager.getData(getArguments().getInt("id"),this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.back){
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.tv_choose_city,new ProvinceFragment()).commit();
        }
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Region region=adapter.getItem(position);
        HistoryHelper.addHistory(getActivity(),region);
        Intent data=new Intent();
        data.putExtra("region_name",region.getRegion_name());
        data.putExtra("adcode",region.getAdcode());
        getActivity().setResult(RESULT_OK,data);
        getActivity().finish();
    }
    private class MyAdapter extends ArrayAdapter<Region> {
        public MyAdapter(Context ctx){
            super(ctx,0);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(convertView==null){
                convertView=getActivity().getLayoutInflater().inflate(R.layout.common_grid_item,null);
            }
            TextView title=(TextView)convertView.findViewById(R.id.title);
            title.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,grid_height));
            Region region=getItem(position);
            title.setText(region.getRegion_name());
            return convertView;
        }
    }

    @Override
    public void recieveData(List<Region> regions) {
        adapter.addAll(regions);
        list.setAdapter(adapter);
    }
}
