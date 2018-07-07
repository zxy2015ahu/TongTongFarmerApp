package com.tongtong.purchaser.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.alibaba.mobileim.utility.UserContext;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.utils.UserUtil;

/**
 * Created by zxy on 2018/3/2.
 */

public class ConversationActivity extends BaseActivity {
    private Fragment mCurrentFrontFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_chatting_activity);
        createFragment();
    }

    private void createFragment(){
        mCurrentFrontFragment = UserUtil.getIMKitInstance(this).getConversationFragment();
        Bundle fragmentBundle = new Bundle();
        fragmentBundle.putSerializable(UserContext.EXTRA_USER_CONTEXT_KEY, UserUtil.getIMKitInstance(this).getUserContext());
        mCurrentFrontFragment.setArguments(fragmentBundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.wx_chat_container, mCurrentFrontFragment).commit();
    }
}
