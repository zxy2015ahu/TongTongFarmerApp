package com.tongtong.purchaser.activity;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.githang.statusbar.StatusBarCompat;
import com.githang.statusbar.StatusBarTools;
import com.lzy.widget.HeaderViewPager;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.adapter.MyFragmentAdapter;
import com.tongtong.purchaser.frament.HeaderViewPagerFragment;
import com.tongtong.purchaser.frament.HistorySaleFragment;
import com.tongtong.purchaser.frament.OnSaleFragment;
import com.tongtong.purchaser.model.FarmerModel;
import com.tongtong.purchaser.utils.Constant;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.widget.AlertDialog;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import byc.imagewatcher.ImageWatcher;


public class FarmerInfoActivity extends BaseActivity implements View.OnClickListener,CompoundButton.OnCheckedChangeListener,
		HeaderViewPager.OnScrollListener,ImageWatcher.OnPictureLongPressListener {
	public FarmerModel farmer;
	private ImageView headImageView;
	private LinearLayout backBn;
	private String[] mDataList={"正在售卖","历史发布"};
	private ViewPager pager;
	private HeaderViewPager headerViewPager;
	private RatingBar rating;
	private RadioButton onsale,historysale;
	private TextView titleText;
	private LinearLayout farmer_info_head;
	private int height;
	private ImageWatcher vImageWatcher;
    private TextView auth;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_farmer_info);
		StatusBarCompat.setTranslucent(getWindow(),true);
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
		vImageWatcher = (ImageWatcher) findViewById(R.id.v_image_watcher);
		vImageWatcher.setTranslucentStatus(0);
		vImageWatcher.setErrorImageRes(R.drawable.error_picture);
		vImageWatcher.setOnPictureLongPressListener(this);
        vImageWatcher.setLoader(new ImageWatcher.Loader() {
            @Override
            public void load(Context context, String url, final ImageWatcher.LoadCallback lc) {
                Glide.with(context).load(url).asBitmap().into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        lc.onResourceReady(resource);
                    }

                    @Override
                    public void onLoadStarted(Drawable placeholder) {
                        lc.onLoadStarted(placeholder);
                    }
                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        lc.onLoadFailed(errorDrawable);
                    }
                });
            }
        });
		farmer = (FarmerModel) getIntent().getSerializableExtra("farmer");
		rating=(RatingBar) findViewById(R.id.rating);
		onsale=(RadioButton) findViewById(R.id.map);
        auth=(TextView) findViewById(R.id.auth);
        if(TextUtils.isEmpty(farmer.getCardid())){
            auth.setCompoundDrawablesWithIntrinsicBounds(R.drawable.vector_drawable_weirenzheng,0,0,0);
            auth.setText("未认证");
        }else{
            auth.setCompoundDrawablesWithIntrinsicBounds(R.drawable.vector_drawable_shenfenrenzheng,0,0,0);
            auth.setText("已认证");
        }
		historysale=(RadioButton) findViewById(R.id.list);
		farmer_info_head=(LinearLayout) findViewById(R.id.farmer_info_head);
		farmer_info_head.post(new Runnable() {
			@Override
			public void run() {
				height=farmer_info_head.getHeight();
			}
		});
		onsale.setChecked(true);
		onsale.setOnCheckedChangeListener(this);
		findViewById(R.id.right_bn2).setOnClickListener(this);
		findViewById(R.id.right_bn1).setOnClickListener(this);
		historysale.setOnCheckedChangeListener(this);
		titleText = (TextView) findViewById(R.id.title_text);
		headImageView = (ImageView) findViewById(R.id.headimg);
		headImageView.setOnClickListener(this);
		Glide.with(this).load(UrlUtil.IMG_SERVER_URL+farmer.getHeadUrl())
				.asBitmap().centerCrop().into(new BitmapImageViewTarget(headImageView) {
			@Override
			protected void setResource(Bitmap resource) {
				RoundedBitmapDrawable circularBitmapDrawable =
						RoundedBitmapDrawableFactory.create(getResources(), resource);
				circularBitmapDrawable.setCircular(true);
				headImageView.setImageDrawable(circularBitmapDrawable);
			}
		});
		TextView nameText = (TextView) findViewById(R.id.name_text);
		nameText.setText(farmer.getName());
		backBn = (LinearLayout) findViewById(R.id.back_bn);
		backBn.setOnClickListener(this);
		pager=(ViewPager) findViewById(R.id.viewpager);
		headerViewPager=(HeaderViewPager) findViewById(R.id.pager);
		headerViewPager.setOnScrollListener(this);
		pager.setOffscreenPageLimit(mDataList.length);
		final List<Fragment> fragments=new ArrayList<>();
		fragments.add(newInstance(OnSaleFragment.class,1));
		fragments.add(newInstance(HistorySaleFragment.class,2));
		MyFragmentAdapter fragmentAdapter=new MyFragmentAdapter(getSupportFragmentManager(),fragments);
		pager.setAdapter(fragmentAdapter);
		headerViewPager.setCurrentScrollableContainer((HeaderViewPagerFragment)fragments.get(0));
		pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
				headerViewPager.setCurrentScrollableContainer((HeaderViewPagerFragment)fragments.get(position));
				if(position==0){
					onsale.setChecked(true);
					historysale.setChecked(false);
				}else{
					onsale.setChecked(false);
					historysale.setChecked(true);
				}
			}
			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});
	}

	@Override
	public void onPictureLongPress(ImageView v, String url, int pos) {

	}

	@Override
	public void onScroll(int currentY, int maxY) {
		if(currentY<height){
			titleText.setText("");
		}else{
			titleText.setText(farmer.getName());
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(isChecked){
			switch (buttonView.getId()){
				case R.id.map:
					pager.setCurrentItem(0);
					break;
				case R.id.list:
					pager.setCurrentItem(1);
					break;
			}
		}
	}

	private Fragment newInstance(Class<? extends Fragment> clazz, int index){
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
	public void onClick(View v) {
		if(v.getId()==R.id.back_bn){
			onBackPressed();
		}else if(v.getId()==R.id.headimg){
			List<ImageView> group=new ArrayList<>();
			group.add(headImageView);
			List<String> url=new ArrayList<>();
			url.add(UrlUtil.IMG_SERVER_URL+farmer.getHeadUrl());
			vImageWatcher.show(headImageView,group,url);
		}else if(v.getId()==R.id.right_bn2){
			AlertDialog dialog=new AlertDialog(this).builder();
			dialog.setTitle("提示");
			dialog.setMsg("确定拨打对方电话？");
			dialog.setPositiveButton("确定", new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent=new Intent();
					intent.setAction(Intent.ACTION_CALL);
					intent.setData(Uri.parse("tel:"+farmer.getPhone()));
					startActivity(intent);
				}
			});
			dialog.setNegativeButton("取消", new View.OnClickListener() {
				@Override
				public void onClick(View v) {

				}
			});
			dialog.show();
		}else if(v.getId()==R.id.right_bn1){
			UserUtil.getIMKitInstance(this).showCustomView(null);
			Intent intent=new Intent();
			intent.setClass(this, ChattingActivity.class);
			intent.putExtra(ChattingActivity.TARGET_ID,farmer.getPhone());
			intent.putExtra(ChattingActivity.TARGET_APP_KEY, Constant.TARGET_APP_KEY);
			startActivity(intent);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Constant.can_load=false;
	}
}
