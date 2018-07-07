package com.tongtong.purchaser.view;

/**
 * Created by zxy on 2018/3/14.
 */

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.widget.NumberPicker;


import com.tongtong.purchaser.R;

import java.lang.reflect.Field;

/**
 * This class changes color of NumberPicker divider using reflection
 */

public class NumberPickerWithColor extends NumberPicker {

    public NumberPickerWithColor(Context context, AttributeSet attrs) {
        super(context, attrs);

        Class<?> numberPickerClass = null;
        try {
            numberPickerClass = Class.forName("android.widget.NumberPicker");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Field selectionDivider = null;
        try {
            selectionDivider = numberPickerClass.getDeclaredField("mSelectionDivider");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        try {
            selectionDivider.setAccessible(true);
            selectionDivider.set(this, AppCompatResources.getDrawable(context, R.drawable.picker_divider_color));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
