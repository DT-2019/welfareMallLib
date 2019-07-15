package com.fifthera.ecmall.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

public class Utils {

    /**
     * 通过包名判断是否安装了对应的 App
     *
     * @param packageName 包名，用于判断是否安装了该包名的 App
     * @return true: 已安装 false: 未安装
     */
    public static boolean isAppInstalled(String packageName, Context context) {
        if (TextUtils.isEmpty(packageName)) return false;
        PackageManager packageManager = context.getPackageManager();
        String[] names = packageName.split(",");
        for (String name : names) {
            try {
                if (packageManager.getPackageInfo(name.trim(), 0) != null) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }

        return false;
    }

    /**
     * 获取 App 版本号
     *
     * @return 返回 App 的版本号，如果获取失败，则返回空字符串。
     */
    public static String getAppVersion(Context context) {
        PackageManager manager = context.getPackageManager();
        String code = "";
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            code = String.valueOf(info.versionCode);
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return code;
    }


    /**
     * 检查网络是否可用
     */
    public static boolean isNetConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo localNetworkInfo = cm.getActiveNetworkInfo();
            boolean netState = (localNetworkInfo != null) && (localNetworkInfo.isAvailable());
            return netState;
        }
        return false;
    }

    /**
     * 全局获取String的方法
     *
     * @param id 资源Id
     * @return String
     */
    public static String getString(@StringRes int id, Context context) {
        return context.getResources().getString(id);
    }

    /**
     * 判断传入的 URL 是不是以 http:// 或 https:// 开头的
     *
     * @return true: Http/Https 链接    false: 非 Http/Https 链接
     */
    public static boolean isValidUrl(String url) {
        if (url == null) {
            return false;
        }
        if ((url.length() > 6) && url.substring(0, 7).equalsIgnoreCase("http://")) {
            return true;
        }
        if ((url.length() > 7) && url.substring(0, 8).equalsIgnoreCase("https://")) {
            return true;
        }
        return false;
    }

    public static boolean isHomeDefault(String url) {
        if (url.contains("#/detail?") || !url.contains("thefifthera.com")) {
            return false;
        } else {
            return true;
        }
    }
    /**
     * 获取 IMEI 码
     * <p>需添加权限
     * {@code <uses-permission android:name="android.permission.READ_PHONE_STATE" />}</p>
     *
     * @return IMEI 码
     */
    @SuppressLint({"HardwareIds", "MissingPermission"})
    public static String getIMEI() {
        try {
            //实例化TelephonyManager对象
            TelephonyManager telephonyManager =
                    (TelephonyManager) ContextUtil.getContext().getSystemService(Context.TELEPHONY_SERVICE);
            //获取IMEI号
            String imei = null;
            if (telephonyManager != null) {
                imei = telephonyManager.getDeviceId();
            }
            //在次做个验证，也不是什么时候都能获取到的啊
            if (imei == null) {
                imei = "";
            }
            return imei;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 跳转对应的 scheme 地址
     *
     * @param link 跳转 scheme 地址
     * @return true:   跳转成功        false: 跳转失败
     */
    public static boolean jump(@NonNull String link, Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(intent);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
