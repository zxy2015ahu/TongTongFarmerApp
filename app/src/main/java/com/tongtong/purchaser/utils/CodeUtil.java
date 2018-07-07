package com.tongtong.purchaser.utils;

import com.yunzhanghu.redpacketsdk.utils.Base64;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CodeUtil {

	public static int SUCCESS_CODE = 100; // 鎴愬姛
	public static int UNKNOWN_ERR_CODE = 101; // 鏈煡閿欒
	public static int DATA_ERR_CODE = 102; // 鏁版嵁閿欒
	public static int FAIL_CODE = 103; // 澶辫触
	
	public static int USERNAME_PASSWORD_ERR = 201;//鐢ㄦ埛鍚嶅拰瀵嗙爜閿欒
	
	public static int PHONE_EXIST_CODE = 601; // 璇ユ墜鏈哄彿宸茬粡琚敞鍐?
	
	public static int TOKEN_NO_CODE = 302; // 鏈櫥褰?,token鏈┖
	public static int TOKEN_INVALID_CODE = 303; // 鐧诲綍澶辨晥,璇烽噸鏂扮櫥褰?
	/**利用MD5进行加密
	 * @param str  待加密的字符串
	 * @return  加密后的字符串
	 * @throws NoSuchAlgorithmException  没有这种产生消息摘要的算法
	 * @throws UnsupportedEncodingException
	 */
	public String encode(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		//确定计算方法
		MessageDigest md5=MessageDigest.getInstance("MD5");
		//加密后的字符串
		String newstr=Base64.encode(md5.digest(str.getBytes("utf-8")));
		return newstr;
	}
}
