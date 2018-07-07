package com.tongtong.purchaser.utils;

import android.text.TextUtils;

public class PhoneNumUtil {
	public static String APP_KEY = "22b513afeb3fc";
	public static String APP_SECRET = "a8c25f9e19384499ced0254669a4263a";

	public static boolean judgePhoneNums(String phoneNums) {
		if (isMatchLength(phoneNums, 11) && isMobileNO(phoneNums)) {
			return true;
		}

		return false;
	}

	public static boolean isMatchLength(String str, int length) {
		if (str.isEmpty()) {
			return false;
		} else {
			return str.length() == length ? true : false;
		}
	}

	public static boolean isMobileNO(String mobileNums) {
		
		String telRegex = "[1][358]\\d{9}";
		if (TextUtils.isEmpty(mobileNums))
			return false;
		else
			return mobileNums.matches(telRegex);
	}
}
