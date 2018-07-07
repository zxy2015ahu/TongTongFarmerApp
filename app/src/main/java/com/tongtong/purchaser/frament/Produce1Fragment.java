package com.tongtong.purchaser.frament;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tongtong.purchaser.R;
import com.tongtong.purchaser.model.ProduceType;
import com.tongtong.purchaser.model.Region;
import com.tongtong.purchaser.utils.DBManager;
import com.tongtong.purchaser.utils.ProduceTypesHelper;
import com.tongtong.purchaser.widget.NoScrollGridView;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.List;

/**
 * Created by zxy on 2018/4/9.
 */

public class Produce1Fragment extends BaseFrament implements
        AdapterView.OnItemClickListener{
    private NoScrollGridView list;
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
        ProduceTypesHelper.getParentType(getActivity(), new ProduceTypesHelper.OnDataRecieveListener() {
            @Override
            public void onRecieveData(List<ProduceType> produceTypes) {
                adapter.addAll(produceTypes);
                list.setAdapter(adapter);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ProduceType region=adapter.getItem(position);
        Bundle bundle=new Bundle();
        bundle.putInt("id",region.getId());
        bundle.putString("name",region.getName());
        bundle.putInt("parent_id",region.getId());
        bundle.putString("parent_name",region.getName());
        bundle.putInt("p1",getArguments().getInt("p1"));
        bundle.putInt("p2",getArguments().getInt("p2"));
        bundle.putInt("p3",getArguments().getInt("p3"));
        Produce2Fragment city=new Produce2Fragment();
        city.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.tv_choose_city,city).commit();
    }

    private class MyAdapter extends ArrayAdapter<ProduceType>{
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
            ProduceType region=getItem(position);
            title.setText(region.getName());
            if(region.getId()==getArguments().getInt("p1")){
                title.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimary));
            }else{
                title.setTextColor(ContextCompat.getColor(getActivity(),R.color.aliwx_common_text_color));
            }
            return convertView;
        }
    }


}
