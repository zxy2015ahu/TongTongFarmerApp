package com.tongtong.purchaser.frament;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import com.githang.statusbar.StatusBarTools;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.activity.SearchTypeActivity;
import com.tongtong.purchaser.widget.MyFragmentTabHost;

/**
 * Created by zxy on 2018/4/7.
 */

public class ShouCaiFragment extends BaseFrament implements CompoundButton.OnCheckedChangeListener,
        View.OnClickListener{
    private RadioButton map,list;
    private MyFragmentTabHost host;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.zhaocai_fragment,container,false);
        host=(MyFragmentTabHost) view.findViewById(android.R.id.tabhost);
        host.setup(getActivity(),getChildFragmentManager(),android.R.id.tabcontent);
        host.addTab(host.newTabSpec("map").setIndicator("map"),MapFragment.class,null);
        host.addTab(host.newTabSpec("list").setIndicator("list"),ZhaocaiListFragment.class,null);
        return view;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            RelativeLayout title_bar=(RelativeLayout) view.findViewById(R.id.title_bar);
            ViewGroup.LayoutParams params=title_bar.getLayoutParams();
            if(params!=null){
                if(params instanceof ViewGroup.MarginLayoutParams){
                    ViewGroup.MarginLayoutParams marginLayoutParams=(ViewGroup.MarginLayoutParams) title_bar.getLayoutParams();
                    marginLayoutParams.topMargin= StatusBarTools.getStatusBarHeight(getActivity());
                }
            }

        }
        map=(RadioButton) view.findViewById(R.id.map);
        list=(RadioButton) view.findViewById(R.id.list);
        map.setOnCheckedChangeListener(this);
        list.setOnCheckedChangeListener(this);
        map.setChecked(true);
        view.findViewById(R.id.right_bn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.right_bn){
            Intent intent=new Intent();
            intent.setClass(getActivity(), SearchTypeActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked){
            switch (buttonView.getId()){
                case R.id.map:
                    host.setCurrentTab(0);
                    break;
                case R.id.list:
                    host.setCurrentTab(1);
                    break;
            }
        }
    }
}
