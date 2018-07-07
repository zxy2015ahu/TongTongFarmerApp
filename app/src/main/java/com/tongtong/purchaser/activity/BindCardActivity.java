package com.tongtong.purchaser.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.githang.statusbar.StatusBarCompat;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.frament.BandCardFragment;
import com.yunzhanghu.redpacketui.widget.RPTitleBar;

/**
 * Created by zxy on 2018/3/8.
 */

public class BindCardActivity extends BaseActivity implements View.OnClickListener{
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rp_activity_bind_bankcard);
        StatusBarCompat.setFitsSystemWindows(getWindow(),true);
        StatusBarCompat.setStatusBarColor(getWindow(), Color.WHITE,true);
        RPTitleBar titleBar=(RPTitleBar) findViewById(R.id.bc_title_bar);
        titleBar.setSubTitleVisibility(View.GONE);
        titleBar.setOnClickListener(this);
        ((View)titleBar.getParent()).setFitsSystemWindows(true);
        getSupportFragmentManager().beginTransaction().replace(R.id.bc_fragment_container,new BandCardFragment()).commit();
    }
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.bc_title_bar){
            onBackPressed();
        }
    }


}
