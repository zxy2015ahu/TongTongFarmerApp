package com.tongtong.purchaser.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class MyScrollView extends ScrollView {

	public interface OnScrollListener{
		 void onScroll(int height);
	}
	private OnScrollListener onScrollListener;
	public void setOnScrollListener(OnScrollListener onScrollListener){
		this.onScrollListener=onScrollListener;
	}
	public MyScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		// TODO Auto-generated method stub
		super.onScrollChanged(l, t, oldl, oldt);
		if(onScrollListener!=null){
			onScrollListener.onScroll(t);
		}
	}
}
