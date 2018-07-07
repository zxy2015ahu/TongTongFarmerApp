package com.tongtong.purchaser.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.mobileim.YWChannel;
import com.alibaba.mobileim.channel.cloud.contact.YWProfileInfo;
import com.alibaba.mobileim.channel.event.IWxCallback;
import com.alibaba.mobileim.contact.IYWContactService;
import com.githang.statusbar.StatusBarCompat;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.utils.Constant;
import com.tongtong.purchaser.utils.UserUtil;
import com.yunzhanghu.redpacketui.widget.RPTitleBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018-05-17.
 */

public class FindContactActivity extends BaseActivity implements TextView.OnEditorActionListener{
    private EditText aliwx_search_keyword;
    private Handler handler=new Handler();
    private InputMethodManager imm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_find_layout);
        StatusBarCompat.setStatusBarColor(this, Color.WHITE,true);
        RPTitleBar titleBar=(RPTitleBar) findViewById(R.id.title_bar);
        titleBar.setLeftLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        titleBar.setSubTitleVisibility(View.GONE);
        titleBar.postDelayed(new Runnable() {
            @Override
            public void run() {
                showKeyBoard();
            }
        },200);
        aliwx_search_keyword=(EditText) findViewById(R.id.aliwx_search_keyword);
        aliwx_search_keyword.setOnEditorActionListener(this);
        imm=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideKeyBoard();
    }

    private void showKeyBoard() {
        View view = getCurrentFocus();
        if (view != null) {
            imm.showSoftInput(view, InputMethodManager.RESULT_SHOWN);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }
    protected void hideKeyBoard() {
        View view = getCurrentFocus();
        if (view != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH){
            String keyword=aliwx_search_keyword.getText().toString().trim();
            if(TextUtils.isEmpty(keyword)){
                showTips("请输入搜索内容");
                return true;
            }
            searchContent(keyword);
            return true;
        }
        return false;
    }
    private IYWContactService getContactService(){
        IYWContactService contactService = UserUtil.getIMKitInstance(this).getContactService();
        return contactService;
    }
    private void handleResult(final List profileInfos) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                    if (profileInfos == null || profileInfos.isEmpty()) {
                       showTips(FindContactActivity.this, getResources().getString(R.string.aliwx_search_friend_not_found_message), new View.OnClickListener() {
                           @Override
                           public void onClick(View v) {
                               showKeyBoard();
                           }
                       });
                    }
            }
        });

    }
    private void searchContent(String keyword) {
        hideKeyBoard();
        if (YWChannel.getInstance().getNetWorkState().isNetWorkNull()) {
            showTips(getResources().getString(R.string.aliwx_net_null));
        } else {
            String key = keyword.replace(" ", "");
            String userId = key;
            ArrayList<String> userIds = new ArrayList<>();
            userIds.add(userId);
            showLoading();
            IYWContactService contactService =getContactService();
            contactService.fetchUserProfile(userIds, Constant.TARGET_APP_KEY, new IWxCallback() {
                @Override
                public void onSuccess(final Object... result) {
                    dismissLoading();
                    if (result != null) {
                        List<YWProfileInfo> profileInfos = (List<YWProfileInfo>) (result[0]);
                        if (profileInfos == null || profileInfos.isEmpty()) {
                            handleResult((List) result[0]);
                            return;
                        }
                        YWProfileInfo mYWProfileInfo = profileInfos.get(0);
                        Intent intent=new Intent();
                        intent.putExtra("icon",mYWProfileInfo.icon);
                        intent.putExtra("userId",mYWProfileInfo.userId);
                        intent.putExtra("nick",mYWProfileInfo.nick);
                        intent.setClass(FindContactActivity.this,ProfileInfoActivity.class);
                        startActivity(intent);
                        //checkIfHasContact(mYWProfileInfo);
                        //showSearchResult(mYWProfileInfo);
                    } else {
                        handleResult((List) result[0]);
                    }
                }

                @Override
                public void onError(int code, String info) {
                    dismissLoading();
                    handleResult(null);
                }

                @Override
                public void onProgress(int progress) {

                }
            });
        }
    }
}
