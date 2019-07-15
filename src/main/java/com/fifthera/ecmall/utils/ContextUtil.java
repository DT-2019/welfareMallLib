package com.fifthera.ecmall.utils;

import android.content.Context;
import android.content.res.Resources;

/**
 * Context 工具
 *
 * @author LeoWang
 * @date 2017/9/12
 */

public class ContextUtil {
    private static Context context;

    private ContextUtil() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * 初始化工具类
     *
     * @param context 上下文
     */
    public static void init(Context context) {
        ContextUtil.context = context.getApplicationContext();
    }

    /**
     * 获取ApplicationContext
     *
     * @return ApplicationContext
     */
    public static Context getContext() {
        if (context != null) {
            return context;
        }
        throw new NullPointerException("u should init first");
    }


    static Resources getResources() {
        return context.getResources();
    }
}
