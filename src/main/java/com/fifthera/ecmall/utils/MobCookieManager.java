package com.fifthera.ecmall.utils;

import android.content.Context;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class MobCookieManager {

    private MobCookieManager(){}

    /**
     * 应用启动的时候调用，参考：{@link CookieManager#getInstance CookieManager.getInstance()}
     * */
    public static void init(Context context){
        CookieSyncManager.createInstance(context);
    }

    public static String getCookie(String url){
        CookieManager cookieManager = CookieManager.getInstance();
        return cookieManager.getCookie(url);
    }

    /**
     * http://stackoverflow.com/questions/16007084/does-android-webkit-cookiemanager-works-on-android-2-3-6
     * */
    public static void setCookies(String url, Map<String, List<String>> headerFields) {
        if (null == headerFields) {
            return;
        }
        List<String> cookies = headerFields.get("Set-Cookie");
        if (null == cookies) {
            return;
        }
        CookieSyncManager.getInstance().startSync();
        for (String cookie : cookies) {
            setCookie(url, cookie);
        }
        CookieSyncManager.getInstance().sync();
    }

    private static void setCookie(String url, String cookie) {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);

        if(cookie.indexOf("Expires") < 0){
            cookie = addExpireToCookie(cookie);
        }
        cookieManager.setCookie(url, cookie);
    }

    /**
     * http://stackoverflow.com/questions/8547620/what-is-a-session-cookie
     * */
    private static String addExpireToCookie(String cookie) {
        Date expireDate = new Date(new Date().getTime() + 24L*60*60*1000);
        String datestr =DateUtil.format(DateUtil.east8ToGmt(expireDate), DateUtil.FORMAT_GMT);
        String arr[] = cookie.split(";");
        StringBuilder sb = new StringBuilder();
        sb.append(arr[0]);
        sb.append("; ").append("Expires=").append(datestr);
        if(arr.length > 1){
            for(int i=1; i<arr.length; i++){
                sb.append(";").append(arr[i]);
            }
        }
        return sb.toString();
    }

}
