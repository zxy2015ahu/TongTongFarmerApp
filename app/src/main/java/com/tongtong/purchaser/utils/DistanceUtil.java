package com.tongtong.purchaser.utils;

import android.content.Context;

public class DistanceUtil {
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}
	public static double screenPixelToMetre(double pxlength, double currScale,
											Context context) {
		float dpi = context.getResources().getDisplayMetrics().densityDpi;
		double resolution = (25.39999918 / dpi)
				* Math.pow(19-currScale,2) / 1000;
		return pxlength * resolution*10;
	}
}
