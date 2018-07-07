package com.tongtong.purchaser.frament;

import android.content.Context;
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
import com.tongtong.purchaser.widget.NoScrollGridView;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.List;

/**
 * Created by zxy on 2018/4/9.
 */

public class ProvinceFragment extends BaseFrament implements DBManager.OnDataRecieveListener,
        AdapterView.OnItemClickListener{
    private NoScrollGridView list;
    private DBManager dbManager;
    private MyAdapter adapter;
    private int grid_height;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.common_grid_4column,container,false);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        list=(NoScrollGridView) view.findViewById(android.R.id.list);
        list.setHorizontalSpacing(1);
        list.setVerticalSpacing(1);
        list.setOnItemClickListener(this);
        grid_height= UIUtil.dip2px(getActivity(),35f);
        adapter=new MyAdapter(getActivity());
        dbManager=new DBManager(getActivity());
        dbManager.getData(-1,this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Region region=adapter.getItem(position);
        Bundle bundle=new Bundle();
        bundle.putInt("id",region.getId());
        bundle.putString("name",region.getRegion_name());
        CityFragment city=new CityFragment();
        city.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.tv_choose_city,city).commit();
    }
    @Override
    public void recieveData(List<Region> regions) {
        adapter.addAll(regions);
        list.setAdapter(adapter);
    }
    private class MyAdapter extends ArrayAdapter<Region>{
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


}
