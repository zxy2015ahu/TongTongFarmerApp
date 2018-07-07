package com.tongtong.purchaser.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.tongtong.purchaser.R;
import com.tongtong.purchaser.utils.CommonUtils;

/**
 * Created by zxy on 2018/3/20.
 */

public class AboutUsActivity extends BaseActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_us_layout);
        ((TextView)findViewById(R.id.title_text)).setText("版权信息");
        findViewById(R.id.back_bn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        ((TextView)findViewById(R.id.version)).setText("v"+CommonUtils.getVersionName(this));
    }
}
