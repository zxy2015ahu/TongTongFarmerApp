package com.tongtong.purchaser.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.sdk.android.feedback.impl.FeedbackAPI;
import com.githang.statusbar.StatusBarCompat;
import com.tongtong.purchaser.R;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.concurrent.Callable;

/**
 * Created by zxy on 2018/4/14.
 */

public class FeedBackActivity extends BaseActivity implements View.OnClickListener{
    private TextView titles;
    private LinearLayout left_from;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_back_layout);
        StatusBarCompat.setStatusBarColor(this, Color.WHITE,true);
        titles=((TextView)findViewById(R.id.title_text));
        left_from=(LinearLayout) findViewById(R.id.left_from);
        DisplayMetrics dm=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        left_from.measure(0,0);
        int width=dm.widthPixels-2*left_from.getMeasuredWidth()- UIUtil.dip2px(this,12f);
        titles.getLayoutParams().width=width;
        findViewById(R.id.back_bn).setOnClickListener(this);
        findViewById(R.id.close_btn).setOnClickListener(this);
        final Fragment feedback = FeedbackAPI.getFeedbackFragment();
        FeedbackAPI.setFeedbackFragment(new Callable() {
            @Override
            public Object call() throws Exception {
                getSupportFragmentManager().beginTransaction().replace(R.id.container,feedback).commit();
                return null;
            }
        },null);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.back_bn){
            super.onBackPressed();
        }else if(v.getId()==R.id.close_btn){
            super.onBackPressed();
        }
    }
}
