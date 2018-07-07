package com.tongtong.purchaser.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.githang.statusbar.StatusBarCompat;
import com.githang.statusbar.StatusBarTools;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.adapter.PublishAdapter;
import com.tongtong.purchaser.model.FarmerModel;
import com.tongtong.purchaser.model.ReleaseModel;
import com.tongtong.purchaser.utils.CodeUtil;
import com.tongtong.purchaser.utils.Constant;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.NetUtil;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.view.Mydivider;
import com.tongtong.purchaser.widget.FullyLinearLayoutManager;
import com.tongtong.purchaser.widget.MyScrollView;
import com.tongtong.purchaser.widget.NoScrollGridView;
import com.tongtong.purchaser.widget.StyleableToast;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.listener.OnBannerListener;
import com.youth.banner.loader.ImageLoader;
import com.youth.banner.view.BannerViewPager;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2018-05-10.
 */

public class ProduceDetailsActivity extends BaseActivity implements MyScrollView.OnScrollListener,
        View.OnClickListener{
    private RelativeLayout bannerContainer;
    private Banner banner;
    private TextView indicator,video_item,image_item;
    private RelativeLayout bottom_original_image_layout;
    private ImageView video,back_icon,favour_icon,share_icon,head_image,video_image;
    private boolean has_video=false;
    private View header,video_header;
    private MyScrollView scroll_view;
    private LinearLayout roundCard;
    private int height;
    private int banner_height;
    private ImageView video_tag;
    private List<String> imgs;
    private String  releaseVedioUrl,releaseVedioThumb;
    private TextView name,location,price,amount,warm,baozhengjin,name_text;
    private NoScrollGridView guige_list;
    private MyGuigeAdapter guigeAdapter;
    private TextView shenfen,shiming,remark;
    private ImageView head;
    private TextView count;
    private LinearLayout listView;
    private DisplayMetrics dm;
    private RecyclerView list;
    private View recmend_layout;
    private String phone;
    private JsonObject farmerReleaseInfo;
    private TextView click_num,chat_num;
    private Dialog dialog;
    private Handler handler;
    private int favour_id,is_favour;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.produce_details_layout);
        StatusBarCompat.setTranslucent(getWindow(),true);
        dm=new DisplayMetrics();
        handler=new Handler();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        bannerContainer=(RelativeLayout) findViewById(R.id.bannerContainer);
        banner=(Banner) findViewById(R.id.banner);
        indicator=(TextView) findViewById(R.id.indicator);
        video_item=(TextView) findViewById(R.id.video_view);
        image_item=(TextView) findViewById(R.id.image_item);
        name=(TextView) findViewById(R.id.name);
        location=(TextView) findViewById(R.id.location);
        price=(TextView) findViewById(R.id.price);
        amount=(TextView) findViewById(R.id.amount);
        warm=(TextView) findViewById(R.id.net_warn);
        shenfen=(TextView) findViewById(R.id.right_name);
        shiming=(TextView) findViewById(R.id.left_name);
        name_text=(TextView) findViewById(R.id.name_text);
        remark=(TextView) findViewById(R.id.remarks);
        click_num=(TextView) findViewById(R.id.btn_click_received);
        chat_num=(TextView) findViewById(R.id.chat_record);
        recmend_layout=findViewById(R.id.receive_state);
        findViewById(R.id.activity_share_location).setOnClickListener(this);
        findViewById(R.id.favour).setOnClickListener(this);
        guige_list=(NoScrollGridView) findViewById(R.id.tv_choose_guige);
        listView=(LinearLayout) findViewById(R.id.listview);
        baozhengjin=(TextView) findViewById(R.id.baozhengjin);
        count=(TextView) findViewById(R.id.count);
        warm.setMovementMethod(LinkMovementMethod.getInstance());
        video_item.setOnClickListener(this);
        image_item.setOnClickListener(this);
        findViewById(R.id.chat_back).setOnClickListener(this);
        findViewById(R.id.head_bn).setOnClickListener(this);
        video=(ImageView) findViewById(R.id.video_item);
        favour_icon=(ImageView) findViewById(R.id.shoucang_count);
        share_icon=(ImageView) findViewById(R.id.share_img);
        head_image=(ImageView) findViewById(R.id.head_img);
        video_image=(ImageView) findViewById(R.id.video_quality_wrapper_area);
        video_tag=(ImageView) findViewById(R.id.left_video_download_progress_stub);
        head=(ImageView) findViewById(R.id.head);
        video_header=findViewById(R.id.video_current_time);
        video_header.setOnClickListener(this);
        findViewById(R.id.back_bn).setOnClickListener(this);
        findViewById(R.id.order).setOnClickListener(this);
        head_image.setOnClickListener(this);
        findViewById(R.id.call_bn).setOnClickListener(this);
        roundCard=(LinearLayout) findViewById(R.id.roundCard);
        scroll_view=(MyScrollView) findViewById(R.id.scroll_view);
        list=(RecyclerView) findViewById(R.id.mListView);
        FullyLinearLayoutManager layoutManager=new FullyLinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        list.setNestedScrollingEnabled(false);
        list.setLayoutManager(layoutManager);
        Mydivider mydivider=new Mydivider(ContextCompat.getColor(this,R.color.line_color),UIUtil.dip2px(this,0.5f));
        list.addItemDecoration(mydivider);
        scroll_view.setOnScrollListener(this);
        header=findViewById(R.id.header);
        back_icon=(ImageView) findViewById(R.id.back);
        back_icon.setImageResource(R.drawable.details_back_white);
        favour_icon.setImageResource(R.drawable.favour_not_white);
        share_icon.setImageResource(R.drawable.share_details_white);
        header.setBackgroundColor(Color.TRANSPARENT);
        bottom_original_image_layout=(RelativeLayout) findViewById(R.id.bottom_original_image_layout);
        banner.isAutoPlay(false);
        banner.setBannerStyle(BannerConfig.NOT_INDICATOR);
        banner.setImageLoader(new ImageLoader() {
            @Override
            public void displayImage(Context context, Object path, ImageView imageView) {
                Glide.with(ProduceDetailsActivity.this)
                        .load(NetUtil.getFullUrl(path.toString())).centerCrop().placeholder(R.drawable.no_banner)
                        .into(imageView);
            }

            @Override
            public ImageView createImageView(Context context) {
                ImageView im=super.createImageView(context);
                return im;
            }
        });
        banner_height=dm.widthPixels*3/4;
        bannerContainer.getLayoutParams().height=banner_height;
        banner.setLayoutParams(new RelativeLayout.LayoutParams(dm.widthPixels,banner_height));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            RelativeLayout title_bar=(RelativeLayout) findViewById(R.id.title_bar);
            ViewGroup.LayoutParams params=title_bar.getLayoutParams();
            if(params!=null){
                if(params instanceof ViewGroup.MarginLayoutParams){
                    ViewGroup.MarginLayoutParams marginLayoutParams=(ViewGroup.MarginLayoutParams) title_bar.getLayoutParams();
                    marginLayoutParams.topMargin= StatusBarTools.getStatusBarHeight(this);
                }
            }
        }
        ((ViewGroup.MarginLayoutParams)roundCard.getLayoutParams()).topMargin=dm.widthPixels*3/4- UIUtil.dip2px(this,25f);
        header.post(new Runnable() {
            @Override
            public void run() {
                height=banner_height-header.getHeight()-UIUtil.dip2px(ProduceDetailsActivity.this,25f);
            }
        });
        warm.setText(getWarmString());
        baozhengjin.setText(getBaozhengjinString(2000));
        guigeAdapter=new MyGuigeAdapter(this);
        getReleaseInfo();
    }



    private class Guige{
        private String title,content;
    }
    private class MyGuigeAdapter extends ArrayAdapter<Guige>{
        public MyGuigeAdapter(Context context){
            super(context,0);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(convertView==null){
                convertView=getLayoutInflater().inflate(R.layout.guige_content,null);
            }
            TextView title=(TextView) convertView.findViewById(R.id.title);
            TextView content=(TextView) convertView.findViewById(R.id.content);
            Guige guige=getItem(position);
            title.setText(guige.title);
            content.setText(guige.content);
            return convertView;
        }
    }
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.head_img){
            Intent intent=new Intent();
            intent.setClass(ProduceDetailsActivity.this,ProduceDetailsViewerActvity.class);
            intent.putExtra("position",1);
            intent.putExtra("imgs",imgs.toArray());
            intent.putExtra("video_url",releaseVedioUrl);
            intent.putExtra("video_thumb",releaseVedioThumb);
            startActivity(intent);
        }else if(v.getId()==R.id.video_current_time){
            Intent intent=new Intent();
            intent.setClass(ProduceDetailsActivity.this,ProduceDetailsViewerActvity.class);
            intent.putExtra("position",0);
            intent.putExtra("imgs",imgs.toArray());
            intent.putExtra("video_url",releaseVedioUrl);
            intent.putExtra("video_thumb",releaseVedioThumb);
            startActivity(intent);
        }else if(v.getId()==R.id.back_bn){
            onBackPressed();
        }else if(v.getId()==R.id.video_view){
            setBannerPosition(1);
        }else if(v.getId()==R.id.image_item){
            setBannerPosition(2);
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
                intent.setClass(ProduceDetailsActivity.this,OrderInfoActivity.class);
                startActivity(intent);
            }
        }else if(v.getId()==R.id.head_bn){
            if(farmerReleaseInfo!=null){
                getFarmerInfo(farmerReleaseInfo.get("f_id").getAsInt());
            }
        }else if(v.getId()==R.id.activity_share_location){
            if(farmerReleaseInfo!=null){
                Intent intent=new Intent();
                intent.setClass(this,NavigateActivity.class);
                intent.putExtra("lat",farmerReleaseInfo.get("releaseLatitude").getAsDouble());
                intent.putExtra("lng",farmerReleaseInfo.get("releaseLongitude").getAsDouble());
                intent.putExtra("address",farmerReleaseInfo.get("releaseLocation").getAsString());
                startActivity(intent);
            }
        }else if(v.getId()==R.id.favour){
            if(is_favour==0){
                addFavour();
            }else{
                deleteFavour();
            }
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
                    onScroll(scroll_view.getScrollY());
                    StyleableToast.warm(ProduceDetailsActivity.this,"收藏取消");
                }
            }

            @Override
            public void taskFailed(int code) {

            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("favour_id",favour_id);
        task.execute(UrlUtil.DELETE_FAVOUR,object.toString());
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
                    onScroll(scroll_view.getScrollY());
                    StyleableToast.success(ProduceDetailsActivity.this,"收藏成功");
                }else{
                    StyleableToast.warm(ProduceDetailsActivity.this,object.get("info").getAsString());
                }
            }

            @Override
            public void taskFailed(int code) {

            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("token",UserUtil.getUserModel(this).getToken());
        object.addProperty("type",1);
        object.addProperty("rel_id",getIntent().getIntExtra("id",0));
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
                    intent.setClass(ProduceDetailsActivity.this,FarmerInfoActivity.class);
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

    private void setBannerPosition(int position){
        try{
            Class<?> ban=banner.getClass();
            Field viewPager=ban.getDeclaredField("viewPager");
            if(!viewPager.isAccessible()){
                viewPager.setAccessible(true);
            }
            BannerViewPager vp=(BannerViewPager)viewPager.get(banner);
            vp.setCurrentItem(position);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void onScroll(int sheight) {
        int real_height=sheight;
        if(real_height<=height){
            int alpha=(int)(((double)real_height/height)*255);
            header.setBackgroundColor(Color.argb(alpha,255,255,255));
            if(alpha<100){
                StatusBarCompat.setLightStatusBar(getWindow(),false);
                back_icon.setImageResource(R.drawable.details_back_white);
                favour_icon.setImageResource(is_favour==0?R.drawable.favour_not_white:R.drawable.favour_white);
                share_icon.setImageResource(R.drawable.share_details_white);
            }else{
                StatusBarCompat.setLightStatusBar(getWindow(),true);
                back_icon.setImageResource(R.drawable.details_back_normal);
                favour_icon.setImageResource(is_favour==0?R.drawable.favour_not_normal:R.drawable.favour_normal);
                share_icon.setImageResource(R.drawable.share_details_normal);
            }
            head_image.setAlpha(alpha);
            if(video_header.getVisibility()==View.VISIBLE){
                video_image.setAlpha(alpha);
                video_tag.setAlpha(alpha);
            }
        }else{
            head_image.setAlpha(255);
            if(video_header.getVisibility()==View.VISIBLE){
                video_image.setAlpha(255);
                video_tag.setAlpha(255);
            }
            header.setBackgroundColor(Color.WHITE);
            back_icon.setImageResource(R.drawable.details_back_normal);
            favour_icon.setImageResource(is_favour==0?R.drawable.favour_not_normal:R.drawable.favour_normal);
            share_icon.setImageResource(R.drawable.share_details_normal);
        }
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
    private SpannableStringBuilder getWarmString(){
        final SpannableStringBuilder builder=new SpannableStringBuilder();
        final String temp="私自打款有风险，推荐使用通通担保交易，防止卖家收钱不发货等不诚信行为。点击了解";
        builder.append(temp);
        final ClickableSpan clickableSpan=new ClickableSpan() {
            @Override
            public void onClick(View widget) {

            }
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(ContextCompat.getColor(ProduceDetailsActivity.this,R.color.colorPrimary));
                ds.setUnderlineText(false);
                ds.clearShadowLayer();
            }
        };
        builder.append("【通通收菜担保交易】");
        builder.setSpan(clickableSpan,temp.length(),builder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return builder;
    }
    private SpannableStringBuilder getAmountString(int estimatedQuantity,String aunit){
        SpannableStringBuilder builder=new SpannableStringBuilder();
        builder.append("预计产量");
        builder.append(String.valueOf(estimatedQuantity));
        ForegroundColorSpan sizeSpan=new ForegroundColorSpan(ContextCompat.getColor(this,R.color.price_color));
        builder.setSpan(sizeSpan,4,builder.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.append(aunit);
        return builder;
    }
    private SpannableStringBuilder getCountString(int sale_count,int pingjia_count){
        SpannableStringBuilder builder=new SpannableStringBuilder();
        builder.append(String.valueOf(sale_count));
        ForegroundColorSpan sizeSpan=new ForegroundColorSpan(ContextCompat.getColor(this,R.color.price_color));
        builder.setSpan(sizeSpan,0,builder.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.append("条发布");
        builder.append("  ");
        int lenght=builder.length();
        builder.append(String.valueOf(pingjia_count));
        ForegroundColorSpan sizeSpan2=new ForegroundColorSpan(ContextCompat.getColor(this,R.color.price_color));
        builder.setSpan(sizeSpan2,lenght,builder.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.append("条评价");
        return builder;
    }
    private SpannableStringBuilder getBaozhengjinString(int estimatedQuantity){
        SpannableStringBuilder builder=new SpannableStringBuilder();
        builder.append("已缴纳");
        builder.append(String.valueOf(estimatedQuantity));
        ForegroundColorSpan sizeSpan=new ForegroundColorSpan(ContextCompat.getColor(this,R.color.price_color));
        builder.setSpan(sizeSpan,3,builder.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.append("诚信保证金");
        return builder;
    }
    private void getReleaseInfo(){
        HttpTask task=new HttpTask(this);
        task.setTaskHandler(new HttpTask.HttpTaskHandler() {
            @Override
            public void taskStart(int code) {

            }
            @Override
            public void taskSuccessful(String str, int code) {
                JsonObject selectResultJson = new JsonParser().parse(str)
                        .getAsJsonObject();
                int selectCode=selectResultJson.get("code").getAsInt();
                JsonObject releaseInfo=selectResultJson.get("releaseInfo").getAsJsonObject();
                farmerReleaseInfo=releaseInfo.get("farmerReleaseInfo").getAsJsonObject();
                if(selectCode == CodeUtil.SUCCESS_CODE){
                    name.setText(farmerReleaseInfo.get("p_name").getAsString());
                    location.setText(farmerReleaseInfo.get("releaseLocation").getAsString());
                    price.setText(getPriceString(farmerReleaseInfo.get("price").getAsDouble(),farmerReleaseInfo.get("punit").getAsString()));
                    amount.setText(getAmountString(farmerReleaseInfo.get("estimatedQuantity").getAsInt(),farmerReleaseInfo.get("aunit").getAsString()));
                    count.setText(getCountString(selectResultJson.get("sale_count").getAsInt(),selectResultJson.get("pingjia_count").getAsInt()));
                    phone=farmerReleaseInfo.get("f_phone").getAsString();
                    click_num.setText(selectResultJson.get("click_num").getAsInt()+"人查看");
                    chat_num.setText(selectResultJson.get("chat_num").getAsInt()+"人咨询");
                    if(!TextUtils.isEmpty(farmerReleaseInfo.get("remark").getAsString())){
                        remark.setText(farmerReleaseInfo.get("remark").getAsString());
                    }else{
                        remark.setVisibility(View.GONE);
                    }
                    Guige guige=new Guige();
                    guige.title="品种名";
                    guige.content=farmerReleaseInfo.get("p_name").getAsString();
                    guigeAdapter.add(guige);
                    JsonArray guiges=releaseInfo.get("guiges").getAsJsonArray();
                    for(int i=0;i<guiges.size();i++){
                        JsonObject gg=guiges.get(i).getAsJsonObject();
                        Guige gui=new Guige();
                        gui.title=gg.get("title").getAsString();
                        gui.content=gg.get("content").getAsString();
                        guigeAdapter.add(gui);
                    }
                    guige_list.setAdapter(guigeAdapter);
                    Glide.with(ProduceDetailsActivity.this).load(UrlUtil.IMG_SERVER_URL+farmerReleaseInfo.get("f_headUrl").getAsString())
                            .asBitmap().centerCrop().into(new BitmapImageViewTarget(head) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            head.setImageDrawable(circularBitmapDrawable);
                        }
                    });
                    name_text.setText(farmerReleaseInfo.get("f_name").getAsString());
                    if(!TextUtils.isEmpty(selectResultJson.get("cardid").getAsString())){
                        shenfen.setText("身份认证");
                        shenfen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.shenfen,0,0,0);
                    }else{
                        shenfen.setText("未身份认证");
                        shenfen.setTextColor(ContextCompat.getColor(ProduceDetailsActivity.this,R.color.aliwx_common_text_color3));
                        shenfen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.shenfen_not_enable,0,0,0);
                    }
                    if(!TextUtils.isEmpty(selectResultJson.get("real_name").getAsString())){
                        shiming.setText("实名认证");
                        shiming.setCompoundDrawablesWithIntrinsicBounds(R.drawable.shiming,0,0,0);
                    }else{
                        shiming.setText("未实名认证");
                        shiming.setTextColor(ContextCompat.getColor(ProduceDetailsActivity.this,R.color.aliwx_common_text_color3));
                        shiming.setCompoundDrawablesWithIntrinsicBounds(R.drawable.shiming_not_enable,0,0,0);
                    }
                    imgs=new ArrayList<>();
                    JsonArray releaseImages=releaseInfo.get("releaseImages").getAsJsonArray();
                    for(int i=0;i<releaseImages.size();i++){
                        if(!(releaseImages.get(i) instanceof JsonNull))
                        imgs.add(releaseImages.get(i).getAsString());
                    }
                    banner.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                        @Override
                        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                        }
                        @Override
                        public void onPageSelected(int position) {
                            if(has_video){
                                bottom_original_image_layout.setVisibility(View.VISIBLE);
                                if(position==0){
                                    video.setVisibility(View.VISIBLE);
                                    video_item.setBackgroundResource(R.drawable.video_select);
                                    image_item.setBackgroundResource(R.drawable.video_unselect);
                                    indicator.setVisibility(View.GONE);
                                }else{
                                    video.setVisibility(View.GONE);
                                    video_item.setBackgroundResource(R.drawable.video_unselect);
                                    image_item.setBackgroundResource(R.drawable.video_select);
                                    indicator.setVisibility(View.VISIBLE);
                                    indicator.setText((position)+"/"+(imgs.size()-1));
                                }
                            }else{
                                indicator.setText((position+1)+"/"+imgs.size());
                            }
                        }
                        @Override
                        public void onPageScrollStateChanged(int state) {

                        }
                    });
                    releaseVedioUrl=farmerReleaseInfo.get("releaseVedioUrl").getAsString();
                    releaseVedioThumb=farmerReleaseInfo.get("releaseVedioThumb").getAsString();
                    if(!TextUtils.isEmpty(releaseVedioUrl)){
                        has_video=true;
                        imgs.add(0,releaseVedioThumb);
                        bottom_original_image_layout.setVisibility(View.VISIBLE);
                        video_item.setBackgroundResource(R.drawable.video_select);
                        image_item.setBackgroundResource(R.drawable.video_unselect);
                    }
                    banner.setImages(imgs);
                    banner.setOnBannerListener(new OnBannerListener() {
                        @Override
                        public void OnBannerClick(int position) {
                            Intent intent=new Intent();
                            intent.setClass(ProduceDetailsActivity.this,ProduceDetailsViewerActvity.class);
                            intent.putExtra("position",position);
                            intent.putExtra("imgs",imgs.toArray());
                            intent.putExtra("video_url",releaseVedioUrl);
                            intent.putExtra("video_thumb",releaseVedioThumb);
                            startActivity(intent);

                        }
                    });
                    banner.start();
                    is_favour=selectResultJson.get("is_favour").getAsInt();
                    if(is_favour==1){
                        favour_id=selectResultJson.get("favour_id").getAsInt();
                    }
                    onScroll(0);
                    if(has_video){
                        video_header.setVisibility(View.VISIBLE);
                        Glide.with(ProduceDetailsActivity.this).load(farmerReleaseInfo.get("releaseVedioThumb").getAsString())
                                .centerCrop().into(video_image);
                        video_image.setAlpha(0);
                        video_tag.setAlpha(0);
                    }
                    Glide.with(ProduceDetailsActivity.this).load(farmerReleaseInfo.get("thumb_img").getAsString())
                            .centerCrop().into(head_image);
                    head_image.setAlpha(0);
                    LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0,UIUtil.dip2px(ProduceDetailsActivity.this,10f),0,0);
                    int img_width=dm.widthPixels-UIUtil.dip2px(ProduceDetailsActivity.this,20f);
                    for(int i=0;i<imgs.size();i++){
                        String img=imgs.get(i);
                        View item=getLayoutInflater().inflate(R.layout.details_img_item,null);
                        ImageView imageView= (ImageView) item.findViewById(R.id.img);
                        ImageView vi=(ImageView) item.findViewById(R.id.video_view);
                        Glide.with(ProduceDetailsActivity.this).load(NetUtil.getFullUrl(img))
                                .placeholder(R.drawable.no_banner).into(imageView);
                        if(!TextUtils.isEmpty(releaseVedioUrl)&&i==0){
                            vi.setVisibility(View.VISIBLE);
                        }else{
                            vi.setVisibility(View.GONE);
                        }
                        final int position=i;
                        item.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent=new Intent();
                                intent.setClass(ProduceDetailsActivity.this,ProduceDetailsViewerActvity.class);
                                intent.putExtra("position",position);
                                intent.putExtra("imgs",imgs.toArray());
                                intent.putExtra("video_url",releaseVedioUrl);
                                intent.putExtra("video_thumb",releaseVedioThumb);
                                startActivity(intent);
                            }
                        });
                        if(remark.getVisibility()!=View.GONE&&i==0){
                            listView.addView(item);
                            continue;
                        }
                        item.setLayoutParams(params);
                        listView.addView(item);
                        list.setHasFixedSize(true);
                        JsonArray recmends=selectResultJson.get("recmends").getAsJsonArray();
                        if(recmends.size()>0){
                            recmend_layout.setVisibility(View.VISIBLE);
                            PublishAdapter publishAdapter=new PublishAdapter(ProduceDetailsActivity.this);
                            Gson gson=new Gson();
                            List<ReleaseModel> items = gson.fromJson(recmends,
                                    new TypeToken<List<ReleaseModel>>() {
                                    }.getType());
                            publishAdapter.addAll(items);
                            list.setAdapter(publishAdapter);
                        }
                    }
                }
            }
            @Override
            public void taskFailed(int code) {

            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("produce_id",getIntent().getIntExtra("id",0));
        object.addProperty("purchaser_id",UserUtil.getUserModel(this).getId());
        task.execute(UrlUtil.GET_FARMER_RELEASE,object.toString());
    }
}
