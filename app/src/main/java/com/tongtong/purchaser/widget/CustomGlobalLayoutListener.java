package com.tongtong.purchaser.widget;

/**
 * Created by Administrator on 2018-05-05.
 */

import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ScrollView;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

/**
 *.
 * 实现弹出软键盘时 整个布局向上平移,解决遮挡问题
 * 在onCreate中添加监听,在onDestroy中remove监听
 */
public class CustomGlobalLayoutListener implements OnGlobalLayoutListener {

    private Context mContext;
    private ScrollView mRootView;
    private View mScrollToView;
    private int scrollHeight;

    /**
     * @param context      context
     * @param rootView     可以滚动的布局
     * @param scrollToView 界面上被遮挡的位于最底部的布局(控件)
     */
    public CustomGlobalLayoutListener(Context context, ScrollView rootView, View scrollToView) {
        this.mContext = context;
        this.mRootView = rootView;
        this.mScrollToView = scrollToView;
    }

    @Override
    public void onGlobalLayout() {
        Rect rect = new Rect();
        mRootView.getWindowVisibleDisplayFrame(rect);
        int rootInvisibleHeight = mRootView.getRootView().getHeight() - rect.bottom;
        if (rootInvisibleHeight > 100) {
            if(scrollHeight==0){
                int[] location = new int[2];
                mScrollToView.getLocationInWindow(location);
                scrollHeight = (location[1] + mScrollToView.getHeight()) - rect.bottom;
            }
            mRootView.smoothScrollTo(0, scrollHeight + UIUtil.dip2px(mContext, 130));
        } else {
            mRootView.smoothScrollTo(0, 0);
        }
    }
}
