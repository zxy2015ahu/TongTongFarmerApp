package com.tongtong.purchaser.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.alibaba.mobileim.ui.contact.ContactsFragment;
import com.githang.statusbar.StatusBarCompat;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.utils.UserUtil;

/**
 * Created by Administrator on 2018-05-17.
 */

public class ContactsActivity extends BaseActivity {
    private Fragment mCurrentFrontFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_chatting_activity);
        StatusBarCompat.setStatusBarColor(this, Color.WHITE,true);
        mCurrentFrontFragment = UserUtil.getIMKitInstance(this).getContactsFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.wx_chat_container, mCurrentFrontFragment).commit();
    }
    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        super.onActivityResult(arg0, arg1, arg2);
        if (mCurrentFrontFragment != null) {
            mCurrentFrontFragment.onActivityResult(arg0, arg1, arg2);
        }
    }
    /**
     * 必须实现该方法，且该方法的实现必须跟以下示例代码完全一致！
     */
    @Override
    public void onBackPressed() {

        if (mCurrentFrontFragment != null && mCurrentFrontFragment.isVisible()) {

            if (mCurrentFrontFragment instanceof ContactsFragment &&((ContactsFragment)mCurrentFrontFragment).onBackPressed()) {
                return;
            }
        }
        super.onBackPressed();
    }
}
