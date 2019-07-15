package com.fifthera.ecmall;

/**
 * 首页 URL 拦截监听器
 */
public interface HomePageInterceptListener {

    /**
     * 拦截首页 url
     *
     * @param url 拦截的地址
     * @return true 表示拦截成功，当前页面不需要变化。false 表示拦截失败（或其他某些特殊需求），当前页面需要加载该 URL
     * @since 1.0
     */
    boolean interceptUrl(String url);
}
