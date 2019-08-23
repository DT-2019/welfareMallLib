package com.fifthera.ecmall;

import android.content.Context;
import android.util.AttributeSet;

import com.fifthera.ecmall.utils.ContextUtil;

import org.apache.commons.ecodec.binary.Hex;
import org.apache.commons.ecodec.digest.DigestUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 对外提供的 webview
 * <p>
 * app 需要调用 JS 的方法，请在这里面定义。
 */
public class ECWebView extends ECBaseWebView {

    public ECWebView(Context context) {
        super(context);
        ContextUtil.init(context);
    }

    public ECWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ContextUtil.init(context);
    }

    /**
     * 获取SDK version
     * @return
     * @since 1010
     */
    public String getSDKVersion() {
        return BuildConfig.VERSION_NAME;
    }

    /**
     * 获取授权链接
     * @param clientSecret
     * @param map
     * @param isDebug
     * @return
     * @since 1010
     */
    public String getAuthorityUrl(String clientSecret, Map<String, Object> map, boolean isDebug) {
        String sign = getSign(clientSecret, map);
        StringBuilder str = new StringBuilder();
        if (isDebug) {
            str.append("https://ec-api-test.thefifthera.com");
        } else {
            str.append("https://ec-api.thefifthera.com");
        }
        str.append("/h5/v1/auth/redirect?")
                .append("sign=")
                .append(sign);

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            str.append("&").append(entry.getKey()).append("=").append(entry.getValue());
        }

        String url = str.toString();


        return url;
    }

    public String getSign(String clientSecret, Map<String, Object> map) {
        StringBuilder str = new StringBuilder();
        str.append(clientSecret);
        List<Map.Entry<String, Object>> list = comparsMap(map);

        for (Map.Entry entry : list) {
            str.append(entry.getKey())
                    .append(entry.getValue());
        }
        str.append(clientSecret);
        String s = new String(Hex.encodeHex(DigestUtils.md5(str.toString())));
        return s.toUpperCase();
    }

    private List<Map.Entry<String, Object>> comparsMap(Map<String, Object> map) {
        List<Map.Entry<String, Object>> list = new ArrayList<Map.Entry<String, Object>>(map.entrySet());
        if (!list.isEmpty()) {
            Collections.sort(list, new Comparator<Map.Entry<String, Object>>() {
                @Override
                public int compare(Map.Entry<String, Object> o1, Map.Entry<String, Object> o2) {
                    return o1.getKey().compareTo(o2.getKey());
                }
            });
        }
        return list;
    }
    /**
     * 购物跳转是否成功
     *
     * @param isSuccess true: 跳转成功  false: 跳转失败
     */
    @Override
    protected void jumpStatus(boolean isSuccess) {
        callHandler(ApiMethod.JUMP_STATUS, new Object[]{isSuccess});
    }

    /**
     * 设置是否需要显示 title
     * 默认不显示 title
     *
     * @param isShow 是否显示 title
     */
    public void showTitle(boolean isShow) {
        callHandler(ApiMethod.SHOW_TITLE, new Object[]{isShow});
    }

    /**
     * 刷新界面
     */
    public void refresh() {
        callHandler(ApiMethod.REFRESH, new Object[]{});
    }

    /**
     * 百川二次授权成功，通知前端
     *
     * @param object
     */
    public void authoParams(JSONObject object) {
        callHandler(ApiMethod.AUTHO_PARAMS, new Object[]{object});
    }

    /**
     * 设置 WebViewClient
     *
     * @since 1.0
     */
    @Override
    public void setOnWebViewClientListener(ECWebViewClient listener) {
        super.setOnWebViewClientListener(listener);
    }

    /**
     * 设置 WebChromeClient
     *
     * @since 1.0
     */
    @Override
    public void setOnWebChromeClientListener(ECWebChromeClient listener) {
        super.setOnWebChromeClientListener(listener);
    }

    /**
     * 设置拦截首页 URL 点击跳转的方法
     * <p>
     * 整个流程如下：
     * 用户在首页点击按钮触发拦截跳转时，调用 JSApi 的 {@link JSApi#interceptHomePageUrl} 方法。这个方法是有返回值的，
     * 如果返回为 false，表示被拦截的 URL 还可以在当前页面加载（一般用于异常处理），返回为 true 时表示成功拦截，当前
     * H5 页面无需再加载被拦截的 URL。
     *
     * @since 1.0
     */
    @Override
    public void shouldInterceptHomePageUrl(HomePageInterceptListener homePageInterceptListener) {
        if (homePageInterceptListener != null) {
            should_intercept_homepage_url = true;
            super.shouldInterceptHomePageUrl(homePageInterceptListener);
        }
    }
}
