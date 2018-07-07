package com.tongtong.purchaser.utils;

/**
 * Created by zxy on 2018/4/15.
 */

public class AppData {
    static {
        System.loadLibrary("AppData");
    }
    public static native String getUrl(String key);
    public static native void download();
    public static native String encode(String text);
}
