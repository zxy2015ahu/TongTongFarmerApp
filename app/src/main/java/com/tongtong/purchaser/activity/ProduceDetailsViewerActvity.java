package com.tongtong.purchaser.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.mobileim.channel.event.IWxCallback;
import com.alibaba.mobileim.contact.IYWContactService;
import com.alibaba.mobileim.contact.IYWDBContact;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.githang.statusbar.StatusBarCompat;
import com.githang.statusbar.StatusBarTools;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.adapter.MyFragmentAdapter;
import com.tongtong.purchaser.frament.ImageViewerFragment;
import com.tongtong.purchaser.frament.VideoFragment;
import com.tongtong.purchaser.model.FarmerModel;
import com.tongtong.purchaser.utils.CodeUtil;
import com.tongtong.purchaser.utils.Constant;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.NetUtil;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.widget.StyleableToast;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import cn.jzvd.JZVideoPlayer;

/**
 * Created by Administrator on 2018-05-12.
 */

public class ProduceDetailsViewerActvity extends BaseActivity implements View.OnClickListener{
    private TextView title,shipin;
    private ViewPager pager;
    private List<String> imgs;
    private int position;
    private String video_url,video_thumb;
    private MyPageChangeListener pageChangeListener;
    private LinearLayout left;
    private DisplayMetrics dm;
    private JsonObject farmerReleaseInfo;
    private String  releaseVedioUrl,releaseVedioThumb;
    private TextView name,name_text,location,desc,beizhu,favour_num,click_num;
    private ImageView head_img;
    private String phone;
    private ImageView favour,add_friend;
    private int is_favour,favour_id;
    private int id;
    private Dialog dialog;
    private Handler handler=new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.produce_details_viewer);
        findViewById(R.id.back_bn).setOnClickListener(this);
        pager=(ViewPager) findViewById(R.id.pager);
        title=(TextView) findViewById(R.id.title_text);
        shipin=(TextView) findViewById(R.id.video_view);
        name=(TextView) findViewById(R.id.name);
        name_text=(TextView) findViewById(R.id.name_text);
        head_img=(ImageView) findViewById(R.id.head_img);
        location=(TextView) findViewById(R.id.location);
        desc=(TextView) findViewById(R.id.desc);
        beizhu=(TextView) findViewById(R.id.description);
        left=(LinearLayout) findViewById(R.id.left_from);
        favour=(ImageView) findViewById(R.id.favour);
        add_friend=(ImageView) findViewById(R.id.iv_vi_add_front);
        favour_num=(TextView) findViewById(R.id.favour_num);
        click_num=(TextView) findViewById(R.id.view_line_single);
        favour.setOnClickListener(this);
        head_img.setOnClickListener(this);
        location.setOnClickListener(this);
        add_friend.setOnClickListener(this);
        dm=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        left.getLayoutParams().width=dm.widthPixels- UIUtil.dip2px(this,80f);
        findViewById(R.id.call_bn).setOnClickListener(this);
        findViewById(R.id.chat_back).setOnClickListener(this);
        findViewById(R.id.order).setOnClickListener(this);
        title.setOnClickListener(this);
        shipin.setOnClickListener(this);
        StatusBarCompat.setTranslucent(getWindow(), true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            RelativeLayout title_bar = (RelativeLayout) findViewById(R.id.title_bar);
            ViewGroup.LayoutParams params = title_bar.getLayoutParams();
            if (params != null) {
                if (params instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) title_bar.getLayoutParams();
                    marginLayoutParams.topMargin = StatusBarTools.getStatusBarHeight(this);
                }
            }
        }
        JZVideoPlayer.FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        JZVideoPlayer.NORMAL_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        initData();
    }
    private class MyPageChangeListener implements ViewPager.OnPageChangeListener{
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }
        @Override
        public void onPageScrollStateChanged(int state) {

        }
        @Override
        public void onPageSelected(int position) {
                        if(TextUtils.isEmpty(video_url)){
                            title.setText((position+1)+"/"+imgs.size());
                        }else{
                            if(position==0){
                                title.setText("图片("+(imgs.size()-1)+")");
                                title.setTextColor(ContextCompat.getColor(ProduceDetailsViewerActvity.this,android.R.color.darker_gray));
                                title.setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
                                title.setBackgroundResource(0);
                                shipin.setText("视频");
                                shipin.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
                                shipin.setTextColor(ContextCompat.getColor(ProduceDetailsViewerActvity.this,R.color.aliwx_white));
                                shipin.setBackgroundResource(R.drawable.under_line);
                            }else{
                                shipin.setText("视频");
                                shipin.setTextColor(ContextCompat.getColor(ProduceDetailsViewerActvity.this,android.R.color.darker_gray));
                                shipin.setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
                                shipin.setBackgroundResource(0);
                                title.setText(getSpanString(position));
                                title.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
                                title.setTextColor(ContextCompat.getColor(ProduceDetailsViewerActvity.this,R.color.aliwx_white));
                                title.setBackgroundResource(R.drawable.under_line);
                            }
                        }
        }
    }
    private void initData(){
        JsonObject selectResultJson = new JsonParser().parse(getIntent().getStringExtra("data"))
                .getAsJsonObject();
        int selectCode=selectResultJson.get("code").getAsInt();
        if(selectCode== CodeUtil.SUCCESS_CODE){
            JsonObject releaseInfo=selectResultJson.get("releaseInfo").getAsJsonObject();
            farmerReleaseInfo=releaseInfo.get("farmerReleaseInfo").getAsJsonObject();
            imgs = new ArrayList<>();
            position = 0;
            JsonArray releaseImages=releaseInfo.get("releaseImages").getAsJsonArray();
            for(int i=0;i<releaseImages.size();i++){
                if(!(releaseImages.get(i) instanceof JsonNull))
                    imgs.add(releaseImages.get(i).getAsString());
            }
            id=farmerReleaseInfo.get("id").getAsInt();
            releaseVedioUrl=farmerReleaseInfo.get("releaseVedioUrl").getAsString();
            releaseVedioThumb=farmerReleaseInfo.get("releaseVedioThumb").getAsString();
            if(!TextUtils.isEmpty(releaseVedioUrl)){
                imgs.add(0,releaseVedioThumb);
            }
            video_url = releaseVedioUrl;
            video_thumb = releaseVedioThumb;
            title.setText("图片("+(imgs.size()-1)+")");
            List<Fragment> fragments = new ArrayList<>();
            int length=imgs.size();
            if (TextUtils.isEmpty(video_url)) {
                for (int i = 0; i < length; i++) {
                    fragments.add(newInstance(ImageViewerFragment.class, i, position,
                            imgs.get(i),video_url,video_thumb));
                }
            }else{
                fragments.add(newInstance(VideoFragment.class,0,position,"",video_url,video_thumb));
                for(int i=1;i<length;i++){
                    fragments.add(newInstance(ImageViewerFragment.class, i, position,imgs.get(i),
                            video_url,video_thumb));
                }
            }
            pager.setOffscreenPageLimit(fragments.size());
            pageChangeListener=new MyPageChangeListener();
            pager.addOnPageChangeListener(pageChangeListener);
            MyFragmentAdapter adapter = new MyFragmentAdapter(getSupportFragmentManager(), fragments);
            pager.setAdapter(adapter);
            pager.setCurrentItem(position);
            phone=farmerReleaseInfo.get("f_phone").getAsString();
            name.setText(farmerReleaseInfo.get("p_name").getAsString());
            favour_num.setText(selectResultJson.get("favour_num").getAsString());
            name_text.setText("@"+farmerReleaseInfo.get("f_name").getAsString());
            location.setText(farmerReleaseInfo.get("releaseLocation").getAsString());
            click_num.setText(selectResultJson.get("click_num").getAsString());
            desc.setText(getDescString(farmerReleaseInfo.get("estimatedQuantity").getAsInt(),farmerReleaseInfo.get("aunit").getAsString(),
                    farmerReleaseInfo.get("area").getAsInt(),farmerReleaseInfo.get("runit").getAsString(),
                    farmerReleaseInfo.get("price").getAsDouble(),farmerReleaseInfo.get("punit").getAsString()));
            if(!TextUtils.isEmpty(farmerReleaseInfo.get("remark").getAsString())){
                beizhu.setVisibility(View.VISIBLE);
                beizhu.setText(farmerReleaseInfo.get("remark").getAsString());
            }
            is_favour=selectResultJson.get("is_favour").getAsInt();
            if(is_favour==1){
                favour_id=selectResultJson.get("favour_id").getAsInt();
                favour.setImageResource(R.drawable.favour_red);
            }
            Glide.with(ProduceDetailsViewerActvity.this).load(NetUtil.getFullUrl(farmerReleaseInfo.get("f_headUrl").getAsString()))
                    .asBitmap().centerCrop().into(new BitmapImageViewTarget(head_img) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    head_img.setImageDrawable(circularBitmapDrawable);
                }
            });
            if(checkIfHasContact(farmerReleaseInfo.get("f_phone").getAsString())){
                add_friend.setVisibility(View.GONE);
            }else{
                add_friend.setVisibility(View.VISIBLE);
            }
        }
    }
    private SpannableStringBuilder getDescString(int quantity,String aunit,int area,String runit,double price,String punit){
        SpannableStringBuilder builder=new SpannableStringBuilder();
        builder.append("预计");
        ForegroundColorSpan colorSpan=new ForegroundColorSpan(0xffFF7200);
        StyleSpan sizeSpan=new StyleSpan(Typeface.BOLD);
        builder.append(String.valueOf(quantity));
        builder.setSpan(colorSpan,2,builder.length(),Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.setSpan(sizeSpan,2,builder.length(),Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.append(aunit);
        if(area>0) {
            builder.append("  ");
            colorSpan = new ForegroundColorSpan(0xffFF7200);
            sizeSpan = new StyleSpan(Typeface.BOLD);
            String sarea = String.valueOf(area);
            builder.append(sarea);
            builder.setSpan(colorSpan, builder.length() - sarea.length(), builder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            builder.setSpan(sizeSpan, builder.length() - sarea.length(), builder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            builder.append(runit);
        }
        builder.append("  ");
        colorSpan=new ForegroundColorSpan(0xffFF7200);
        sizeSpan=new StyleSpan(Typeface.BOLD);
        String parea=String.valueOf(price);
        builder.append(parea);
        builder.setSpan(colorSpan,builder.length()-parea.length(),builder.length(),Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.setSpan(sizeSpan,builder.length()-parea.length(),builder.length(),Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.append("元/");
        builder.append(punit);
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
    private SpannableStringBuilder getSpanString(int position){
        SpannableStringBuilder builder=new SpannableStringBuilder();
        builder.append("图片(");
        builder.append(String.valueOf(position));
        RelativeSizeSpan sizeSpan=new RelativeSizeSpan(1.2f);
        builder.setSpan(sizeSpan,3,builder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.append("/");
        builder.append(String.valueOf(imgs.size()-1));
        builder.append(")");
        return builder;
    }
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.back_bn){
            onBackPressed();
        }else if(v.getId()==R.id.title_text){
            if(pager.getCurrentItem()==0){
                pager.setCurrentItem(1,false);
            }
        }else if(v.getId()==R.id.video_view){
            pager.setCurrentItem(0,false);
        }else if(v.getId()==R.id.call_bn){
            if(phone!=null) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phone));
                startActivity(intent);
            }
        }else if(v.getId()==R.id.chat_back){
            if(farmerReleaseInfo!=null){
                final String target = farmerReleaseInfo.get("f_phone").getAsString();
                final String appkey = Constant.TARGET_APP_KEY; //消息接收者appKey
                Intent intent = new Intent();
                intent.setClass(this,ChattingActivity.class);
                intent.putExtra(ChattingActivity.TARGET_ID,target);
                intent.putExtra("farmerInfo",farmerReleaseInfo.toString());
                intent.putExtra(ChattingActivity.TARGET_APP_KEY,appkey);
                startActivity(intent);
            }
        }else if(v.getId()==R.id.order){
            if(farmerReleaseInfo!=null){
                Intent intent=new Intent();
                intent.putExtra("releaseInfo",farmerReleaseInfo.toString());
                intent.setClass(ProduceDetailsViewerActvity.this,OrderInfoActivity.class);
                startActivity(intent);
            }
        }else if(v.getId()== R.id.head_img){
            if(farmerReleaseInfo!=null){
                getFarmerInfo(farmerReleaseInfo.get("f_id").getAsInt());
            }
        }else if(v.getId()== R.id.favour){
            if(is_favour==0){
                addFavour();
            }else{
                deleteFavour();
            }
        }else if(v.getId()==R.id.location){
            if(farmerReleaseInfo!=null){
                Intent intent=new Intent();
                intent.setClass(this,NavigateActivity.class);
                intent.putExtra("lat",farmerReleaseInfo.get("releaseLatitude").getAsDouble());
                intent.putExtra("lng",farmerReleaseInfo.get("releaseLongitude").getAsDouble());
                intent.putExtra("address",farmerReleaseInfo.get("releaseLocation").getAsString());
                startActivity(intent);
            }
        }else if(v.getId()==R.id.iv_vi_add_front){
            showAuthDialog();
        }
    }
    private void deleteFavour(){
        HttpTask task=new HttpTask(this);
        task.setTaskHandler(new HttpTask.HttpTaskHandler() {
            @Override
            public void taskStart(int code) {

            }
            @Override
            public void taskSuccessful(String str, int code) {
                JsonObject object=new JsonParser().parse(str).getAsJsonObject();
                if(object.get("code").getAsInt()==CodeUtil.SUCCESS_CODE){
                    favour_id=0;
                    is_favour=0;
                    favour.setImageResource(R.drawable.favour_not_);
                    favour_num.setText(object.get("favour_num").getAsString());
                }
            }

            @Override
            public void taskFailed(int code) {

            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("favour_id",favour_id);
        object.addProperty("rel_id",id);
        task.execute(UrlUtil.DELETE_FAVOUR,object.toString());
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
                    sendAddContactRequest(title.getText().toString(),farmerReleaseInfo.get("f_phone").getAsString(),
                            Constant.TARGET_APP_KEY,UserUtil.getUserModel(ProduceDetailsViewerActvity.this).getName());
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
                        showTips(ProduceDetailsViewerActvity.this,"好友申请已发送");
                    }
                });
            }
            @Override
            public void onError(int i, String s) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        StyleableToast.error(ProduceDetailsViewerActvity.this,"好友申请发送失败");
                    }
                });
            }
            @Override
            public void onProgress(int i) {

            }
        });
    }
    private void addFavour(){
        HttpTask task=new HttpTask(this);
        task.setTaskHandler(new HttpTask.HttpTaskHandler() {
            @Override
            public void taskStart(int code) {

            }

            @Override
            public void taskSuccessful(String str, int code) {
                JsonObject object=new JsonParser().parse(str).getAsJsonObject();
                if(object.get("code").getAsInt()==CodeUtil.SUCCESS_CODE){
                    favour_id=object.get("favour_id").getAsInt();
                    is_favour=1;
                    favour.setImageResource(R.drawable.favour_red);
                    favour_num.setText(object.get("favour_num").getAsString());
                }else{
                    StyleableToast.warm(ProduceDetailsViewerActvity.this,object.get("info").getAsString());
                }
            }

            @Override
            public void taskFailed(int code) {

            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("token", UserUtil.getUserModel(this).getToken());
        object.addProperty("type",1);
        object.addProperty("rel_id",id);
        object.addProperty("id",UserUtil.getUserModel(this).getId());
        task.execute(UrlUtil.ADD_FAVOUR,object.toString());
    }
    private void getFarmerInfo(final int farmer_id){
        HttpTask task=new HttpTask(this);
        task.setTaskHandler(new HttpTask.HttpTaskHandler() {
            @Override
            public void taskStart(int code) {

            }
            @Override
            public void taskSuccessful(String str, int code) {
                JsonObject refreshResultJson = new JsonParser().parse(str)
                        .getAsJsonObject();
                int refreshResultCode = refreshResultJson.get("code").getAsInt();
                JsonObject farmer=refreshResultJson.get("farmer").getAsJsonObject();
                if(refreshResultCode==CodeUtil.SUCCESS_CODE){
                    FarmerModel fm=new FarmerModel();
                    fm.setName(farmer.get("name").getAsString());
                    fm.setPhone(farmer.get("phone").getAsString());
                    fm.setCardid(farmer.get("cardid").getAsString());
                    fm.setHeadUrl(farmer.get("headUrl").getAsString());
                    fm.setId(farmer_id);
                    fm.setAddressStr(farmer.get("addressStr").getAsString());
                    Intent intent=new Intent();
                    intent.putExtra("farmer",fm);
                    intent.setClass(ProduceDetailsViewerActvity.this,FarmerInfoActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void taskFailed(int code) {

            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("farmer_id",farmer_id);
        task.execute(UrlUtil.GET_FARMER_INFO,object.toString());
    }
    private Fragment newInstance(Class<? extends  Fragment> clazz, int index, int defaultIndex,
                                 String url,String video_url,String video_thumb){
        try{
            Constructor fragment=clazz.getConstructor();
            Fragment f=(Fragment) fragment.newInstance();
            Bundle bundle=new Bundle();
            bundle.putInt("index",index);
            bundle.putInt("defaultIndex",defaultIndex);
            bundle.putString("url",url);
            bundle.putString("video_url",video_url);
            bundle.putString("video_thumb",video_thumb);
            f.setArguments(bundle);
            return f;
        }catch (Exception e){

        }
        return null;
    }
    @Override
    public void onBackPressed() {
        if (JZVideoPlayer.backPress()) {
            return;
        }
        super.onBackPressed();
    }
    @Override
    protected void onPause() {
        super.onPause();
        JZVideoPlayer.releaseAllVideos();
        JZVideoPlayer.FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR;
        JZVideoPlayer.NORMAL_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }
}
