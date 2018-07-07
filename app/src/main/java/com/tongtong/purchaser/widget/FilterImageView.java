package com.tongtong.purchaser.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Administrator on 2017/11/8.
 */

public class FilterImageView extends ImageView {
    public FilterImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }
    public FilterImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }
    public FilterImageView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }
    /**
     *   设置滤镜
     */
    private void setFilter() {
        //先获取设置的src图片
        Drawable drawable=getDrawable();
        //当src图片为Null，获取背景图片
        if (drawable==null) {
            drawable=getBackground();
        }
        if(drawable!=null){
            //设置滤镜
            drawable.setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);;
        }
    }
    /**
     *   清除滤镜
     */
    private void removeFilter() {
        //先获取设置的src图片
        Drawable drawable=getDrawable();
        //当src图片为Null，获取背景图片
        if (drawable==null) {
            drawable=getBackground();
        }
        if(drawable!=null){
            //清除滤镜
            drawable.clearColorFilter();
        }
    }
    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        // TODO Auto-generated method stub
        if (isPressed()) {
            setFilter();
        }else{
            removeFilter();
        }
        return super.onCreateDrawableState(extraSpace);
    }
    @Override
    protected void drawableStateChanged() {
        // TODO Auto-generated method stub
        super.drawableStateChanged();

        Drawable drawable =getDrawable();
        if (drawable != null) {
            int[] myDrawableState = getDrawableState();
            drawable.setState(myDrawableState);
            invalidate();
        }
    }
}
