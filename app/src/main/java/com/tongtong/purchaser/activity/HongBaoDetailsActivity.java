package com.tongtong.purchaser.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.tongtong.purchaser.R;
import com.tongtong.purchaser.frament.HongBaoDetailsFragment;
import com.yunzhanghu.redpacketui.widget.RPTitleBar;

/**
 * Created by zxy on 2018/3/10.
 */

public class HongBaoDetailsActivity extends BaseActivity implements View.OnClickListener{
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rp_activity_red_packet_detail);
        RPTitleBar titleBar=(RPTitleBar) findViewById(R.id.title_bar);
        titleBar.setSubTitleVisibility(View.VISIBLE);
        titleBar.setSubTitle("我抢到的红包详情");
        titleBar.setLeftLayoutClickListener(this);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.detail_fragment_container,new HongBaoDetailsFragment()).commit();
    }


    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.left_image){
            onBackPressed();
        }
    }
}
