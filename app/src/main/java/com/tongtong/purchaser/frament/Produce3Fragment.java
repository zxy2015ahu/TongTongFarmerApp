package com.tongtong.purchaser.frament;

import android.content.Context;
import android.content.Intent;
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
import com.tongtong.purchaser.utils.HistoryHelper;
import com.tongtong.purchaser.utils.ProduceTypesHelper;
import com.tongtong.purchaser.widget.NoScrollGridView;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Created by zxy on 2018/4/9.
 */

public class Produce3Fragment extends BaseFrament implements AdapterView.OnItemClickListener,
        View.OnClickListener{
    private NoScrollGridView list;
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
        select_box.setText(getArguments().getString("parent_name")+"/"+getArguments().getString("name"));
        grid_height= UIUtil.dip2px(getActivity(),35f);
        adapter=new MyAdapter(getActivity());
        ProduceTypesHelper.getSubProduce(getArguments().getInt("id"), getActivity(), new ProduceTypesHelper.OnDataRecieveListener() {
            @Override
            public void onRecieveData(List<ProduceType> produceTypes) {
                ProduceType pt=new ProduceType();
                pt.setName("不限品种");
                pt.setId(getArguments().getInt("id"));
                pt.setLevel(getArguments().getInt("level"));
                produceTypes.add(0,pt);
                adapter.addAll(produceTypes);
                list.setAdapter(adapter);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.back){
            Produce2Fragment produce2Fragment=new Produce2Fragment();
            produce2Fragment.setArguments(getArguments());
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.tv_choose_city,produce2Fragment).commit();
        }
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ProduceType region=adapter.getItem(position);
        if(position==0){
            region.setName(getArguments().getString("name"));
        }
        ProduceTypesHelper.addHistotyProduce(getActivity(),region);
        Intent data=new Intent();
        data.putExtra("produce",region);
        getActivity().setResult(RESULT_OK,data);
        getActivity().finish();
    }
    private class MyAdapter extends ArrayAdapter<ProduceType> {
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
            if(position==0&&getArguments().getInt("p3")==0&&region.getId()==getArguments().getInt("p2")){
                title.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimary));
            }else if(position>0&&getArguments().getInt("p3")==region.getId()){
                title.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorPrimary));
            }else{
                title.setTextColor(ContextCompat.getColor(getActivity(),R.color.aliwx_common_text_color));
            }
            return convertView;
        }
    }


}
