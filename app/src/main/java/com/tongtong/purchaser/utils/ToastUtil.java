package com.tongtong.purchaser.utils;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.tongtong.purchaser.R;

/**
 * Created by Fussen on 2016/11/1.
 */

public class ToastUtil {
    private static Toast mToast;

    private static Handler mHandler = new Handler();
    private static Runnable r = new Runnable() {
        public void run() {
            if(mToast != null) {
                mToast.cancel();
                mToast = null;// toast隐藏后，将其置为null
            }
        }
    };
    public static void showShortToast(Context context, String message) {
        TextView text = new TextView(context);// 显示的提示文字
        text.setText(message);
        int dimen=context.getResources().getDimensionPixelSize(R.dimen.default_margin_padding);
        text.setBackgroundColor(Color.BLACK);
        text.setPadding(dimen, dimen, dimen, dimen);
        text.setTextColor(Color.WHITE);
        if (mToast == null) {//
            mToast = new Toast(context);
            mToast.setGravity(Gravity.BOTTOM, 0, 150);
            mToast.setView(text);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

    public static void show(Context context, String info) {
        Toast.makeText(context, info, Toast.LENGTH_LONG).show();
    }

    public static void show(Context context, int info) {
        Toast.makeText(context, info, Toast.LENGTH_LONG).show();
    }
}
