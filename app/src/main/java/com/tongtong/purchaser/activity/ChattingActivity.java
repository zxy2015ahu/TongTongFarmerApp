package com.tongtong.purchaser.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.mobileim.channel.event.IWxCallback;
import com.alibaba.mobileim.contact.IYWContactService;
import com.alibaba.mobileim.contact.IYWDBContact;
import com.alibaba.mobileim.conversation.EServiceContact;
import com.alibaba.mobileim.conversation.YWConversation;
import com.alibaba.mobileim.conversation.YWMessage;
import com.alibaba.mobileim.kit.chat.ChattingFragment;
import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.application.MyApplication;
import com.tongtong.purchaser.helper.ChattingOperationCustom;
import com.tongtong.purchaser.utils.Constant;
import com.tongtong.purchaser.utils.NetUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.widget.StyleableToast;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.List;

/**
 * Created by zxy on 2018/3/2.
 */

public class ChattingActivity extends BaseActivity {
    public static final String TARGET_ID = "targetId";
    public static final String TARGET_APP_KEY = "targetAppKey";
    public static final String TARGET_ESERVICE = "targetEservice";
    private Fragment mCurrentFrontFragment;
    private JsonObject farmerReleaseInfo;
    private Dialog dialog;
    private Handler handler=new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_chatting_activity);
        if(getIntent().getStringExtra("farmerInfo")!=null){
            farmerReleaseInfo=new JsonParser().parse(getIntent().getStringExtra("farmerInfo")).getAsJsonObject();
        }
        createFragment();
    }

    private void createFragment(){
        Intent intent = getIntent();
        String targetId = intent.getStringExtra(TARGET_ID);
        String targetAppKey=intent.getStringExtra(TARGET_APP_KEY);
        String targetEservice=intent.getStringExtra(TARGET_ESERVICE);
            if (TextUtils.isEmpty(targetEservice)) {
                mCurrentFrontFragment = UserUtil.getIMKitInstance(this).getChattingFragment(targetId, targetAppKey);
                if (farmerReleaseInfo != null || !checkIfHasContact(targetId)) {
                    initCustomView(targetId);
                } else {
                    UserUtil.getIMKitInstance(this).showCustomView(null);
                }
            } else {
                UserUtil.getIMKitInstance(this).showCustomView(null);
                EServiceContact contact = new EServiceContact(targetId);
                contact.setGroupId(163227725);
                mCurrentFrontFragment = UserUtil.getIMKitInstance(this).getChattingFragment(contact);
            }
        getSupportFragmentManager().beginTransaction().replace(R.id.wx_chat_container, mCurrentFrontFragment).commit();
    }


    /**
     * 必须实现该方法，且该方法的实现必须跟以下示例代码完全一致！
     * todo 因为拍照和选择照片的时候会回调该方法，如果没有按以下方式覆写该方法会导致拍照和选择照片时应用crash或拍照和选择照片无效!
     * @param arg0
     * @param arg1
     * @param arg2
     */
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

            if (mCurrentFrontFragment instanceof ChattingFragment &&((ChattingFragment)mCurrentFrontFragment).onBackPressed()) {
                return;
            }
        }
        super.onBackPressed();
    }
    private void initCustomView(String f_phone){
        View custom_view=getLayoutInflater().inflate(R.layout.my_cus_chat_view,null);
        ImageView img=(ImageView) custom_view.findViewById(R.id.img);
        TextView name=(TextView) custom_view.findViewById(R.id.name);
        TextView amount=(TextView) custom_view.findViewById(R.id.amount);
        TextView price=(TextView) custom_view.findViewById(R.id.price);
        final View contact=custom_view.findViewById(R.id.contact);
        final View chanpin=custom_view.findViewById(R.id.goods_info);
        if(checkIfHasContact(f_phone)){
            contact.setVisibility(View.GONE);
        }else{
            contact.setVisibility(View.VISIBLE);
            custom_view.findViewById(R.id.contact_member).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAuthDialog();
                }
            });
        }
        if(farmerReleaseInfo==null){
            chanpin.setVisibility(View.GONE);
        }else{
            chanpin.setVisibility(View.VISIBLE);
            custom_view.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chanpin.setVisibility(View.GONE);
                }
            });
            Glide.with(this).load(NetUtil.getFullUrl((farmerReleaseInfo.get("thumb_img").isJsonNull())?farmerReleaseInfo.get("releaseVedioThumb").getAsString():farmerReleaseInfo.get("thumb_img").getAsString())).centerCrop().placeholder(R.drawable.no_icon).into(img);
            name.setText(farmerReleaseInfo.get("p_name").getAsString());
            amount.setText(getAmountString(farmerReleaseInfo.get("estimatedQuantity").getAsInt(),farmerReleaseInfo.get("aunit").getAsString()));
            price.setText(getPriceString(farmerReleaseInfo.get("price").getAsDouble(),farmerReleaseInfo.get("punit").getAsString()));
            custom_view.findViewById(R.id.book).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent();
                    intent.putExtra("releaseInfo",farmerReleaseInfo.toString());
                    intent.setClass(ChattingActivity.this,OrderInfoActivity.class);
                    startActivity(intent);
                }
            });
            custom_view.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    YWMessage message= ChattingOperationCustom.createGoodsMessage(farmerReleaseInfo.get("id").getAsInt(),farmerReleaseInfo.get("p_name").getAsString(),
                            farmerReleaseInfo.get("estimatedQuantity").getAsInt(),
                            farmerReleaseInfo.get("aunit").getAsString(),
                            farmerReleaseInfo.get("price").getAsDouble(),
                            farmerReleaseInfo.get("punit").getAsString(),
                            NetUtil.getFullUrl((TextUtils.isEmpty(farmerReleaseInfo.get("thumb_img").getAsString())?farmerReleaseInfo.get("releaseVedioThumb").getAsString():farmerReleaseInfo.get("thumb_img").getAsString())));
                    YWConversation conversation= MyApplication.getConversation(farmerReleaseInfo.get("f_phone").getAsString());
                    conversation.getMessageSender().sendMessage(message,120,null);
                    chanpin.setVisibility(View.GONE);
                }
            });
        }
        UserUtil.getIMKitInstance(this).showCustomView(custom_view);
    }
    private SpannableStringBuilder getAmountString(int estimatedQuantity, String aunit){
        SpannableStringBuilder builder=new SpannableStringBuilder();
        builder.append("预计产量");
        builder.append(String.valueOf(estimatedQuantity));
        ForegroundColorSpan sizeSpan=new ForegroundColorSpan(ContextCompat.getColor(this,R.color.price_color));
        builder.setSpan(sizeSpan,4,builder.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.append(aunit);
        return builder;
    }
    //私自打款有风险，推荐使用通通担保交易，防止卖家收钱不发货等不诚信行为。点击了解【通通收菜担保交易】
    private SpannableStringBuilder getPriceString(double price,String punit){
        SpannableStringBuilder builder=new SpannableStringBuilder();
        builder.append(String.valueOf(price));
        RelativeSizeSpan sizeSpan=new RelativeSizeSpan(2f);
        builder.setSpan(sizeSpan,0,builder.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        StyleSpan typefaceSpan=new StyleSpan(Typeface.BOLD);
        builder.setSpan(typefaceSpan,0,builder.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.append("元/"+punit);
        return builder;
    }
    private boolean checkIfHasContact(String f_phone){
        IYWContactService contactService = UserUtil.getIMKitInstance(this).getContactService();
        List<IYWDBContact> contacts=contactService.getContactsFromCache();
        for(IYWDBContact contact:contacts){
            if(contact.getUserId().equals(f_phone)){
                return true;
            }
        }
        return false;
    }
    private void showAuthDialog(){
        if(dialog==null) {
            dialog = new Dialog(ChattingActivity.this, R.style.MyDialogStyle);
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
                    sendAddContactRequest(title.getText().toString(),farmerReleaseInfo.get("f_phone").getAsString(),
                            Constant.TARGET_APP_KEY,UserUtil.getUserModel(ChattingActivity.this).getName());
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
                        showTips(ChattingActivity.this,"好友申请已发送");
                    }
                });
            }
            @Override
            public void onError(int i, String s) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        StyleableToast.error(ChattingActivity.this,"好友申请发送失败");
                    }
                });
            }
            @Override
            public void onProgress(int i) {

            }
        });
    }
}
