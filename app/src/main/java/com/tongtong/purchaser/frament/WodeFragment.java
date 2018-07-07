package com.tongtong.purchaser.frament;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.mobileim.contact.IYWContactCacheUpdateListener;
import com.alibaba.mobileim.contact.IYWContactService;
import com.alibaba.sdk.android.feedback.impl.FeedbackAPI;
import com.alibaba.sdk.android.feedback.util.IUnreadCountCallback;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.githang.statusbar.StatusBarTools;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.activity.AccountActivity;
import com.tongtong.purchaser.activity.ChattingActivity;
import com.tongtong.purchaser.activity.ContactsActivity;
import com.tongtong.purchaser.activity.FabuActivity;
import com.tongtong.purchaser.activity.FavourListActivity;
import com.tongtong.purchaser.activity.FeedBackActivity;
import com.tongtong.purchaser.activity.IntegeralActivity;
import com.tongtong.purchaser.activity.MyHongBaohistory;
import com.tongtong.purchaser.activity.MyInfoActivity;
import com.tongtong.purchaser.activity.OrderListActivity;
import com.tongtong.purchaser.activity.PlayWebViewActivity;
import com.tongtong.purchaser.activity.SettingActivity;
import com.tongtong.purchaser.utils.CodeUtil;
import com.tongtong.purchaser.utils.Constant;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.NetUtil;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.widget.PullToZoomScrollViewEx;

import net.lucode.hackware.magicindicator.buildins.UIUtil;


/**
 * Created by zxy on 2018/4/7.
 */

public class WodeFragment extends BaseFrament implements PullToZoomScrollViewEx.onScrollChangeListener,
        View.OnClickListener,IYWContactCacheUpdateListener{
    private RelativeLayout content;
    private LinearLayout title_bar_layout;
    private int height;
    //private MyScrollView scroll_view;
    private ImageView head_img;
    private TextView username,feedback_phone_tv,amount,jifen,jiaoyi,shoucang_count;
    private PullToZoomScrollViewEx scrollView;
    private TextView contact,publish;
    private IYWContactService contactService;
    private Handler handler=new Handler();
    private static WodeFragment instance;
    private View jifen_dot;
    private View info,divider;
    public static WodeFragment getInstance(){
        return instance;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.wode_fragment,container,false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        instance=this;
        scrollView = (PullToZoomScrollViewEx) view.findViewById(R.id.scroll_view);
        View headView = getActivity().getLayoutInflater().inflate(R.layout.wode_header_view, null, false);
        View contentView = getActivity().getLayoutInflater().inflate(R.layout.wode_content_view, null, false);
        contact=(TextView) contentView.findViewById(R.id.contact);
        contentView.findViewById(R.id.contact_member).setOnClickListener(this);
        contentView.findViewById(R.id.wx_friends_button_layout).setOnClickListener(this);
        contentView.findViewById(R.id.favour).setOnClickListener(this);
        contentView.findViewById(R.id.public_setting_unread).setOnClickListener(this);
        contentView.findViewById(R.id.order).setOnClickListener(this);
        contentView.findViewById(R.id.close).setOnClickListener(this);
        contentView.findViewById(R.id.info).setOnClickListener(this);
        info=contentView.findViewById(R.id.goods_info);
        divider=contentView.findViewById(R.id.divider);
        contentView.findViewById(R.id.hongbao_close_icon_iv).setOnClickListener(this);
        headView.findViewById(R.id.amount_layout).setOnClickListener(this);
        headView.findViewById(R.id.jifen_layout).setOnClickListener(this);
        headView.findViewById(R.id.jiaoyi_layout).setOnClickListener(this);
        headView.findViewById(R.id.shezhi).setOnClickListener(this);
        contactService = UserUtil.getIMKitInstance(getActivity()).getContactService();
        contact.setText(String.valueOf(contactService.getContactsFromCache().size()));
        contactService.addContactCacheUpdateListener(this);
        //scrollView.setHeaderView(headView);
        scrollView.setZoomView(headView);
        scrollView.setScrollContentView(contentView);
        scrollView.setonScrollChangeListener(this);
        view.findViewById(R.id.feedback_hint_tv).setOnClickListener(this);
        view.findViewById(R.id.kefu).setOnClickListener(this);
        view.findViewById(R.id.iv_pass_authentication).setOnClickListener(this);
        content=(RelativeLayout) headView.findViewById(R.id.content);
        title_bar_layout=(LinearLayout) view.findViewById(R.id.title_bar_layout);
        //scroll_view=(MyScrollView) view.findViewById(R.id.scroll_view);
        head_img=(ImageView) headView.findViewById(R.id.head_img);
        username=(TextView)headView.findViewById(R.id.username);
        head_img.setOnClickListener(this);
        username.setOnClickListener(this);
        amount=(TextView)headView.findViewById(R.id.amount);
        jifen=(TextView) headView.findViewById(R.id.jifen);
        jiaoyi=(TextView) headView.findViewById(R.id.jiaoyi);
        jifen_dot= headView.findViewById(R.id.jifen_dot);
        shoucang_count=(TextView) contentView.findViewById(R.id.shoucang_count);
        feedback_phone_tv=(TextView)contentView.findViewById(R.id.feedback_phone_tv);
        publish=(TextView)contentView.findViewById(R.id.dingyue);
        //scroll_view.setOnScrollListener(this);
        title_bar_layout.post(new Runnable() {
            @Override
            public void run() {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                    RelativeLayout title_bar=(RelativeLayout) view.findViewById(R.id.title_bar);
                    ViewGroup.LayoutParams params=title_bar.getLayoutParams();
                    if(params!=null){
                        if(params instanceof ViewGroup.MarginLayoutParams){
                            ViewGroup.MarginLayoutParams marginLayoutParams=(ViewGroup.MarginLayoutParams) title_bar.getLayoutParams();
                            marginLayoutParams.topMargin= StatusBarTools.getStatusBarHeight(getActivity());
                        }
                    }
                    content.setPadding(content.getPaddingLeft(),content.getPaddingTop()+ StatusBarTools.getStatusBarHeight(getActivity()),content.getPaddingRight(),content.getPaddingBottom());
                }
                height=UIUtil.dip2px(getActivity(),170f);
                DisplayMetrics dm=new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
                scrollView.setHeaderViewSize(dm.widthPixels, height);
            }
        });
        if(UserUtil.getUserModel(getActivity())!=null){
            setInfo();
            getData();
        }
    }

    @Override
    public void onFriendCacheUpdate(String s, String s1) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                contact.setText(String.valueOf(contactService.getContactsFromCache().size()));
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        contactService.removeContactCacheUpdateListener(this);
    }

    public void getData(){
        if(getActivity()==null){
            return;
        }
        HttpTask task=new HttpTask(getActivity());
        task.setTaskHandler(new HttpTask.HttpTaskHandler() {
            @Override
            public void taskStart(int code) {

            }
            @Override
            public void taskSuccessful(String str, int code) {
                JsonObject selectResultJson = new JsonParser().parse(str)
                        .getAsJsonObject();
                int selectResultCode = selectResultJson.get("code").getAsInt();
                if(selectResultCode== CodeUtil.SUCCESS_CODE){
                    amount.setText(String.format("%.2f", selectResultJson.get("money").getAsDouble()));
                    jifen.setText(String.valueOf(selectResultJson.get("integral").getAsInt()));
                    jiaoyi.setText(String.valueOf(selectResultJson.get("order_num").getAsInt()));
                    shoucang_count.setText(String.valueOf(selectResultJson.get("favour_num").getAsInt()));
                    publish.setText(String.valueOf(selectResultJson.get("release_num").getAsInt()));
                    jifen_dot.setVisibility(selectResultJson.get("integral_new").getAsBoolean()?View.VISIBLE:View.GONE);
                }
            }
            @Override
            public void taskFailed(int code) {

            }
        });
        JsonObject object=new JsonObject();
        object.addProperty("token",UserUtil.getUserModel(getActivity()).getToken());
        object.addProperty("purchaser_id",UserUtil.getUserModel(getActivity()).getId());
        task.execute(UrlUtil.GET_MONEY,object.toString());
    }
    public void setInfo(){
        Glide.with(getActivity()).load(NetUtil.getFullUrl(UserUtil.getUserModel(getActivity()).getHeadUrl()))
                .asBitmap().centerCrop().placeholder(R.drawable.rp_avatar).into(new BitmapImageViewTarget(head_img){
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                head_img.setImageDrawable(circularBitmapDrawable);
            }
        });
        username.setText(UserUtil.getUserModel(getActivity()).getName());
    }
    @Override
    public void onResume() {
        super.onResume();
        FeedbackAPI.getFeedbackUnreadCount(new IUnreadCountCallback() {
            @Override
            public void onSuccess(final int i) {
                feedback_phone_tv.post(new Runnable() {
                    @Override
                    public void run() {
                        if(i>0){
                            feedback_phone_tv.setText("有新回复");
                        }else{
                            feedback_phone_tv.setText("让我们更好");
                        }
                    }
                });
            }
            @Override
            public void onError(int i, String s) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.feedback_hint_tv){
            Intent intent=new Intent();
            intent.setClass(getActivity(), FeedBackActivity.class);
            startActivity(intent);
        }else if(v.getId()==R.id.kefu){
            Intent intent=new Intent();
            intent.putExtra(ChattingActivity.TARGET_ID, Constant.SERVER_ACCOUNT);
            intent.putExtra(ChattingActivity.TARGET_ESERVICE,ChattingActivity.TARGET_ESERVICE);
            intent.setClass(getActivity(),ChattingActivity.class);
            startActivity(intent);
        }else if(v.getId()==R.id.iv_pass_authentication){

        }else if(v.getId()==R.id.contact_member){
            Intent intent=new Intent();
            intent.setClass(getActivity(),ContactsActivity.class);
            startActivity(intent);
        }else if(v.getId()==R.id.wx_friends_button_layout){
            Intent intent=new Intent();
            intent.setClass(getActivity(),PlayWebViewActivity.class);
            intent.putExtra("url",UrlUtil.SHARE_URL+UserUtil.getUserModel(getActivity()).getId());
            startActivity(intent);
        }else if(v.getId()==R.id.amount_layout){
            Intent intent=new Intent();
            intent.setClass(getActivity(),AccountActivity.class);
            startActivity(intent);
        }else if(v.getId()==R.id.jifen_layout){
            Intent intent=new Intent();
            intent.setClass(getActivity(),IntegeralActivity.class);
            startActivityForResult(intent,1);
        }else if(v.getId()==R.id.jiaoyi_layout){
            Intent intent=new Intent();
            intent.setClass(getActivity(),OrderListActivity.class);
            startActivity(intent);
        }else if(v.getId()==R.id.favour){
            Intent intent=new Intent();
            intent.setClass(getActivity(),FavourListActivity.class);
            startActivity(intent);
        }else if(v.getId()==R.id.public_setting_unread){
            Intent intent=new Intent();
            intent.setClass(getActivity(),FabuActivity.class);
            startActivity(intent);
        }else if(v.getId()==R.id.order){
            Intent intent=new Intent();
            intent.setClass(getActivity(),OrderListActivity.class);
            startActivity(intent);
        }else if(v.getId()==R.id.hongbao_close_icon_iv){
            Intent intent=new Intent();
            intent.setClass(getActivity(),MyHongBaohistory.class);
            startActivity(intent);
        }else if(v.getId()==R.id.shezhi){
            Intent intent=new Intent();
            intent.setClass(getActivity(),SettingActivity.class);
            startActivity(intent);
        }else if(v.getId()==R.id.close){
            divider.setVisibility(View.VISIBLE);
            info.setVisibility(View.GONE);
        }else if(v.getId()==R.id.info||v.getId()==R.id.head_img||v.getId()==R.id.username){
            Intent intent=new Intent();
            intent.setClass(getActivity(), MyInfoActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1&&resultCode== Activity.RESULT_OK){
            getData();
        }
    }

    @Override
    public void onScrollChange(int h) {
        h= UIUtil.dip2px(getActivity(),h);
        if(h==0){
            if(title_bar_layout.getVisibility()==View.VISIBLE){
                title_bar_layout.setVisibility(View.GONE);
            }
        }else{
            if(title_bar_layout.getVisibility()==View.GONE){
                title_bar_layout.setVisibility(View.VISIBLE);
            }
        }
        float alpha=(float)h/height;
        if(h<height){
            title_bar_layout.setAlpha(alpha);
        }else{
            title_bar_layout.setAlpha(1);
        }
    }
}
