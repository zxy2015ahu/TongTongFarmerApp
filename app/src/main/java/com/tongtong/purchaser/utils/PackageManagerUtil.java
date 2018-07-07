package com.tongtong.purchaser.utils;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.tongtong.purchaser.application.MyApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018-01-29.
 */

public class PackageManagerUtil {

    private static PackageManager mPackageManager = MyApplication.instance.getPackageManager();
    private static List<String> mPackageNames = new ArrayList<>();
    private static final String GAODE_PACKAGE_NAME = "com.autonavi.minimap";
    private static final String BAIDU_PACKAGE_NAME = "com.baidu.BaiduMap";


    private static void initPackageManager(){

        List<PackageInfo> packageInfos = mPackageManager.getInstalledPackages(0);

        if (packageInfos != null) {
            for (int i = 0; i < packageInfos.size(); i++) {
                mPackageNames.add(packageInfos.get(i).packageName);
            }
        }
    }

    public static boolean haveGaodeMap(){
        initPackageManager();
        return mPackageNames.contains(GAODE_PACKAGE_NAME);
    }

    public static boolean haveBaiduMap(){
        initPackageManager();
        return mPackageNames.contains(BAIDU_PACKAGE_NAME);
    }
}
