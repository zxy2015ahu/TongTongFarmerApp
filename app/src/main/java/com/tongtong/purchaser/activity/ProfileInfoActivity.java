package com.tongtong.purchaser.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.mobileim.channel.event.IWxCallback;
import com.alibaba.mobileim.contact.IYWContactService;
import com.alibaba.mobileim.contact.IYWDBContact;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.githang.statusbar.StatusBarCompat;
import com.githang.statusbar.StatusBarTools;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.utils.Constant;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.widget.StyleableToast;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.List;

/**
 * Created by Administrator on 2018-05-17.
 */

public class ProfileInfoActivity extends BaseActivity {
    private ImageView head_img;
    private TextView account,nick;
    private Button add_contact,send_msg;
    private Handler handler=new Handler();
    private Dialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_info_layout);
        StatusBarCompat.setTranslucent(getWindow(),true);
        head_img=(ImageView) findViewById(R.id.aliwx_people_head);
        Glide.with(this).load(getIntent().getStringExtra("icon"))
                .asBitmap().centerCrop().placeholder(R.drawable.rp_avatar).into(new BitmapImageViewTarget(head_img){
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                head_img.setImageDrawable(circularBitmapDrawable);
            }
        });
        account=(TextView) findViewById(R.id.aliwx_userid_text);
        account.setText(getIntent().getStringExtra("userId"));
        nick=(TextView) findViewById(R.id.aliwx_remark_name_text);
        nick.setText(getIntent().getStringExtra("nick"));
        add_contact=(Button) findViewById(R.id.aliwx_bottom_btn);
        send_msg=(Button) findViewById(R.id.aliwx_btn_send_message);
        String me=UserUtil.getUserModel(this).getPhone();
        if(checkIfHasContact(getIntent().getStringExtra("userId"))){
            add_contact.setVisibility(View.GONE);
            send_msg.setVisibility(View.VISIBLE);
        }else if(me.equals(getIntent().getStringExtra("userId"))){
            add_contact.setVisibility(View.GONE);
            send_msg.setVisibility(View.GONE);
        }else{
            add_contact.setVisibility(View.VISIBLE);
            send_msg.setVisibility(View.VISIBLE);
        }
        add_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAuthDialog();
            }
        });
        send_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setClass(ProfileInfoActivity.this,ChattingActivity.class);
                intent.putExtra(ChattingActivity.TARGET_ID,getIntent().getStringExtra("userId"));
                intent.putExtra(ChattingActivity.TARGET_APP_KEY,Constant.TARGET_APP_KEY);
                startActivity(intent);
            }
        });
        RelativeLayout titleBar = (RelativeLayout) findViewById(R.id.title_bar);
        titleBar.setBackgroundColor(Color.TRANSPARENT);
        titleBar.setVisibility(View.VISIBLE);
        TextView leftButton = (TextView) findViewById(R.id.left_button);
        leftButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.aliwx_common_back_btn_bg_white, 0, 0, 0);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        TextView title = (TextView) findViewById(R.id.title_self_title);
        title.setTextColor(Color.WHITE);
        title.setText("个人资料");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            ViewGroup.LayoutParams params=titleBar.getLayoutParams();
            if(params!=null){
                if(params instanceof ViewGroup.MarginLayoutParams){
                    ViewGroup.MarginLayoutParams marginLayoutParams=(ViewGroup.MarginLayoutParams) titleBar.getLayoutParams();
                    marginLayoutParams.topMargin= StatusBarTools.getStatusBarHeight(this);
                }
            }
        }
    }
    private boolean checkIfHasContact(String userId){
        //修改hasContactAlready和contactsFromCache的Fragment生命周期缓存
        List<IYWDBContact> contactsFromCache =getContactService().getContactsFromCache();
        for(IYWDBContact contact:contactsFromCache){
            if(contact.getUserId().equals(userId)){
                return true;
            }
        }
        return false;

    }
    private IYWContactService getContactService(){
        IYWContactService contactService = UserUtil.getIMKitInstance(this).getContactService();
        return contactService;
    }
    private void showAuthDialog(){
        if(dialog==null) {
            dialog = new Dialog(this, R.style.MyDialogStyle);
            dialog.setContentView(R.layout.add_contact_layoout);
            Window window = dialog.getWindow();
            WindowManager.LayoutParams params = window.getAttributes();
            DisplayMetrics dm=new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            params.width = dm.widthPixels - UIUtil.dip2px(this, 24f);
            window.setAttributes(params);
            final EditText title=(EditText) dialog.findViewById(R.id.title);
            final ImageButton delete=(ImageButton) dialog.findViewById(R.id.delete);
            title.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(s.toString().trim().length()>0){
                        delete.setVisibility(View.VISIBLE);
                    }else{
                        delete.setVisibility(View.GONE);
                    }
                }
            });
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    title.getText().clear();
                }
            });
            title.setText("我是");
            Selection.setSelection(title.getText(),title.getText().length());
            dialog.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InputMethodManager imm=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(title.getWindowToken(),0);
                    dialog.dismiss();
                }
            });
            dialog.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InputMethodManager imm=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(title.getWindowToken(),0);
                    dialog.dismiss();
                    sendAddContactRequest(title.getText().toString(),getIntent().getStringExtra("userId"),
                            Constant.TARGET_APP_KEY,UserUtil.getUserModel(ProfileInfoActivity.this).getName());
                }
            });
        }
        dialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(dialog.findViewById(R.id.title), InputMethodManager.RESULT_SHOWN);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        },200);

    }
    private void sendAddContactRequest(String mMsg,String userId, String appKey,String nickName) {
        IYWContactService contactService=UserUtil.getIMKitInstance(this).getContactService();
        contactService.addContact(userId, appKey, nickName, mMsg, new IWxCallback() {
            @Override
            public void onSuccess(Object... objects) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        showTips(ProfileInfoActivity.this, "好友申请已发送", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent=new Intent();
                                intent.setClass(ProfileInfoActivity.this,ContactsActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                });
            }
            @Override
            public void onError(int i, String s) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        StyleableToast.error(ProfileInfoActivity.this,"好友申请发送失败");
                    }
                });
            }
            @Override
            public void onProgress(int i) {

            }
        });
    }
}
