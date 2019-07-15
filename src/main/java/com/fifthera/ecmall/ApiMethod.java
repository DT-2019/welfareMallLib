package com.fifthera.ecmall;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 定义 JS 的方法名
 * <p>
 * 这个类里面的常量名是需要与 App 进行交互的方法名称，除 ${@link #HAS_API} 外，
 * 通常在 ${@link com.fifthera.ecmall.ECWebView} 里使用。
 * <p>
 * 这里使用注解的方式进行约束，尽量避免方法名拼写错误的情况。
 */
class ApiMethod {

    /**
     * 判断客户端是否存在某方法
     * 该方法只用在 {@link com.fifthera.ecmall.ECBaseWebView#hasJavascriptMethod} 中
     *
     * @since 1.0
     */
    static final String HAS_API = "_hasJavascriptMethod";

    /**
     * 告知 H5 刷新界面
     */
    static final String REFRESH = "refresh";

    /**
     * 判断是否需要 H5 显示 title
     *
     * @since 1.0
     */
    static final String SHOW_TITLE = "isShowTitle";

    /**
     * 告知 H5 客户端跳转 scheme 是否成功
     *
     * @since 1.0
     */
    static final String JUMP_STATUS = "jumpStatus";

    /**
     * 百川授权结果通知H5
     */
    static final String AUTHO_PARAMS = "authoParams";
    /**
     * 注解，用来约束方法字符串的取值范围，减少拼写错误的情况。
     */
    @StringDef({
            HAS_API,
            REFRESH,
            SHOW_TITLE,
            JUMP_STATUS,
            AUTHO_PARAMS
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface Api {
    }
}
