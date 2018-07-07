package com.tongtong.purchaser.activity;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tongtong.purchaser.R;
import com.tongtong.purchaser.adapter.MyFragmentAdapter;
import com.tongtong.purchaser.frament.DaiSonghuoFragment;
import com.tongtong.purchaser.frament.DaiZhifuDingjinFragment;
import com.tongtong.purchaser.frament.DaiZhifuHuokuanFragment;
import com.tongtong.purchaser.frament.QuanbuFragment;
import com.tongtong.purchaser.frament.SongdaFragment;
import com.tongtong.purchaser.frament.WeiqueRenFragment;
import com.tongtong.purchaser.frament.YiWanchengFragment;
import com.tongtong.purchaser.frament.YijujueFragment;
import com.tongtong.purchaser.utils.Constant;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.UIUtil;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.badge.BadgeAnchor;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.badge.BadgePagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.badge.BadgeRule;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2018-02-03.
 */

public class OrderListActivity extends BaseActivity implements View.OnClickListener{
    private MagicIndicator indicator;
    private ViewPager vp;
    private int index;
    private static final String[] CHANNELS = new String[]{"全部", "待农户确认","待送货","已送达", "待支付订金","待支付货款","已拒绝","已完成"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_list_layout);
        findViewById(R.id.back_bn).setOnClickListener(this);
        ((TextView)findViewById(R.id.title_text)).setText("我的订单");
        indicator=(MagicIndicator) findViewById(R.id.indicator);
        vp=(ViewPager) findViewById(R.id.vp);
        List<Fragment> fragments=new ArrayList<Fragment>();
        index=getIntent().getIntExtra("index",0);
        fragments.add(newInstance(QuanbuFragment.class,index==0?1:0));
        fragments.add(newInstance(WeiqueRenFragment.class,index==1?1:0));
        fragments.add(newInstance(DaiSonghuoFragment.class,index==2?1:0));
        fragments.add(newInstance(SongdaFragment.class,index==3?1:0));
        fragments.add(newInstance(DaiZhifuDingjinFragment.class,index==4?1:0));
        fragments.add(newInstance(DaiZhifuHuokuanFragment.class,index==5?1:0));
        fragments.add(newInstance(YijujueFragment.class,index==6?1:0));
        fragments.add(newInstance(YiWanchengFragment.class,index==7?1:0));
        vp.setAdapter(new MyFragmentAdapter(getSupportFragmentManager(),fragments));
        vp.setOffscreenPageLimit(CHANNELS.length);
        CommonNavigator commonNavigator = new CommonNavigator(this);
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return CHANNELS.length;
            }

            @Override
            public IPagerTitleView getTitleView(Context context,final int i) {
                final BadgePagerTitleView badgePagerTitleView = new BadgePagerTitleView(context);
                SimplePagerTitleView colorTransitionPagerTitleView = new ColorTransitionPagerTitleView(context);
                colorTransitionPagerTitleView.setNormalColor(getResources().getColor(R.color.color_70));
                colorTransitionPagerTitleView.setSelectedColor(getResources().getColor(R.color.colorPrimary));
                colorTransitionPagerTitleView.setText(CHANNELS[i]);
                colorTransitionPagerTitleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        vp.setCurrentItem(i,false);
                        badgePagerTitleView.setBadgeView(null);
                    }
                });
                badgePagerTitleView.setInnerPagerTitleView(colorTransitionPagerTitleView);
                if(i==4&&getIntent().getIntExtra("wqr",0)>0){
                    TextView badgeTextView = (TextView) LayoutInflater.from(context).inflate(R.layout.count_layout, null);
                    badgeTextView.setText("" + getIntent().getIntExtra("wqr",0));
                    badgePagerTitleView.setBadgeView(badgeTextView);
                    badgePagerTitleView.setXBadgeRule(new BadgeRule(BadgeAnchor.CONTENT_RIGHT, -UIUtil.dip2px(context, 6)));
                    badgePagerTitleView.setYBadgeRule(new BadgeRule(BadgeAnchor.CONTENT_TOP, 0));
                }else if(i==5&&getIntent().getIntExtra("dsh",0)>0){
                    TextView badgeTextView = (TextView) LayoutInflater.from(context).inflate(R.layout.count_layout, null);
                    badgeTextView.setText("" + getIntent().getIntExtra("dsh",0));
                    badgePagerTitleView.setBadgeView(badgeTextView);
                    badgePagerTitleView.setXBadgeRule(new BadgeRule(BadgeAnchor.CONTENT_RIGHT, -UIUtil.dip2px(context, 6)));
                    badgePagerTitleView.setYBadgeRule(new BadgeRule(BadgeAnchor.CONTENT_TOP, 0));
                }else if(i==3&&getIntent().getIntExtra("dsh",0)>0){
                    TextView badgeTextView = (TextView) LayoutInflater.from(context).inflate(R.layout.count_layout, null);
                    badgeTextView.setText("" + getIntent().getIntExtra("sd",0));
                    badgePagerTitleView.setBadgeView(badgeTextView);
                    badgePagerTitleView.setXBadgeRule(new BadgeRule(BadgeAnchor.CONTENT_RIGHT, -UIUtil.dip2px(context, 6)));
                    badgePagerTitleView.setYBadgeRule(new BadgeRule(BadgeAnchor.CONTENT_TOP, 0));
                }else if(i==1&&getIntent().getIntExtra("nhwqr",0)>0){
                    TextView badgeTextView = (TextView) LayoutInflater.from(context).inflate(R.layout.count_layout, null);
                    badgeTextView.setText("" + getIntent().getIntExtra("nhwqr",0));
                    badgePagerTitleView.setBadgeView(badgeTextView);
                    badgePagerTitleView.setXBadgeRule(new BadgeRule(BadgeAnchor.CONTENT_RIGHT, -UIUtil.dip2px(context, 6)));
                    badgePagerTitleView.setYBadgeRule(new BadgeRule(BadgeAnchor.CONTENT_TOP, 0));
                }
                return badgePagerTitleView;
            }
            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator indicator = new LinePagerIndicator(context);
                //indicator.setMode(LinePagerIndicator.MODE_WRAP_CONTENT);
                indicator.setColors(getResources().getColor(R.color.colorPrimary));
                return indicator;
            }
        });
        indicator.setNavigator(commonNavigator);
        LinearLayout titleContainer = commonNavigator.getTitleContainer();
        titleContainer.setShowDividers(LinearLayout.SHOW_DIVIDER_BEGINNING|LinearLayout.SHOW_DIVIDER_MIDDLE|LinearLayout.SHOW_DIVIDER_END);
        //titleContainer.setDividerPadding(200);
        titleContainer.setDividerDrawable(new ColorDrawable() {
            @Override
            public int getIntrinsicWidth() {
                return UIUtil.dip2px(OrderListActivity.this, 10);
            }
        });
        ViewPagerHelper.bind(indicator, vp);
        vp.setCurrentItem(index,false);
    }


    private Fragment newInstance(Class<? extends  Fragment> clazz, int index){
        try{
            Constructor fragment=clazz.getConstructor();
            Fragment f=(Fragment) fragment.newInstance();
            Bundle bundle=new Bundle();
            bundle.putInt("index",index);
            f.setArguments(bundle);
            return f;
        }catch (Exception e){

        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Constant.can_load=false;
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.back_bn){
            onBackPressed();
        }
    }
}
