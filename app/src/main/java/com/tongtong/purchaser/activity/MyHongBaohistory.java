package com.tongtong.purchaser.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.tongtong.purchaser.R;
import com.tongtong.purchaser.frament.HongBaoRecieveHistoryFragment;
import com.tongtong.purchaser.frament.HongBaoSendHistoryFragment;
import com.yunzhanghu.redpacketui.widget.RPTitleBar;
import com.zaaach.toprightmenu.MenuItem;
import com.zaaach.toprightmenu.TopRightMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zxy on 2018/3/10.
 */

public class MyHongBaohistory extends BaseActivity implements TopRightMenu.OnMenuItemClickListener {

    private TopRightMenu mTopRightMenu;
    private RPTitleBar title_bar;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rp_activity_record);
        mTopRightMenu = new TopRightMenu(this);
        List<MenuItem> menuItems = new ArrayList<>();
        menuItems.add(new MenuItem(R.drawable.recieve, "收到的红包"));
        menuItems.add(new MenuItem(R.drawable.sendout, "发出的红包"));
        mTopRightMenu.showIcon(true);
        mTopRightMenu.setWidth(300);
        mTopRightMenu.setHeight(RecyclerView.LayoutParams.WRAP_CONTENT);
        mTopRightMenu.addMenuList(menuItems);
        mTopRightMenu.dimBackground(true);
        mTopRightMenu.needAnimationStyle(true);
        mTopRightMenu.setAnimationStyle(R.style.TRM_ANIM_STYLE);
        mTopRightMenu.setOnMenuItemClickListener(this);
        title_bar=(RPTitleBar) findViewById(R.id.title_bar);
        title_bar.setLeftLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        title_bar.setSubTitle("我的红包记录");
        title_bar.setSubTitleVisibility(View.VISIBLE);
        title_bar.setRightTextLayoutVisibility(View.GONE);
        title_bar.setRightImageLayoutVisibility(View.VISIBLE);
        title_bar.setRightImageResource(R.drawable.common_forward_normal);
        title_bar.setRightImageLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTopRightMenu.showAsDropDown(v, -240, 0);
            }
        });
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.record_fragment_container,new HongBaoRecieveHistoryFragment()).commit();
    }


    @Override
    public void onMenuItemClick(int position) {
        if(position==0){
            title_bar.setTitle("收到的红包");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.record_fragment_container,new HongBaoRecieveHistoryFragment()).commit();
        }else{
            title_bar.setTitle("发出的红包");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.record_fragment_container,new HongBaoSendHistoryFragment()).commit();
        }
    }
}
