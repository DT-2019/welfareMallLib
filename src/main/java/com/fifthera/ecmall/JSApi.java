package com.fifthera.ecmall;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.fifthera.ecmall.utils.DeviceUtils;
import com.fifthera.ecmall.utils.RunUtil;
import com.fifthera.ecmall.utils.Utils;

import org.json.JSONObject;


/**
 * JSApi
 * 这个类里面的方法是 H5 调用的方法，并且方法结果是需要返回给 App 处理的。
 * <p>
 * 注：JSApi 里面暴露给 H5 调用的方法，其权限应该是 package，这样可以避免 App 直接调用到，并且这里面的方法都尽量不要使用 public 权限。
 */
@SuppressLint("JavascriptInterface")
@SuppressWarnings("unused")
public class JSApi {
    private ECBaseWebView webview;
    private Context context;
    private int gold;
    private float money;
    private boolean isEarnGoldEnable = true;

    /**
     * 监听器，用于向 App 返回信息。
     */
    private OnApiResponseListener listener;

    public JSApi(Context context) {
        Constant.IS_SHOW_TITLE = false;
        this.context = context;
    }

    public JSApi(Context context, boolean earnGoldEnable) {
        Constant.IS_SHOW_TITLE = false;
        this.context = context;
        this.isEarnGoldEnable = earnGoldEnable;
    }
    /**
     * 设置 webview
     *
     * @since 1.0
     */
    void setWebView(ECBaseWebView webView) {
        this.webview = webView;
    }

    /**
     * 获取 App 版本号
     *
     * @since 1.0
     */
    @JavascriptInterface
    String getAppVersion(Object object) {
        return Utils.getAppVersion(context);
    }

    /**
     * 获取 SDK 版本号
     *
     * @since 1.0
     */
    @JavascriptInterface
    String getSDKVersion(Object object) {
        return BuildConfig.VERSION_NAME;
    }

    /**
     * 判断手机是否安装了前端传来的包名，并把安装结果返回给前端。
     *
     * @since 1.0
     */
    @JavascriptInterface
    boolean isAppInstall(Object packageName) {
        try {
            return Utils.isAppInstalled((String) packageName, context);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取设备IMEI
     * @param object
     * @return
     */
    @JavascriptInterface
    String getDeviceIMEI(Object object) {
        return Utils.getIMEI();
    }

    @JavascriptInterface
    String getDeviceInfo(Object object) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("androidId:").append(DeviceUtils.getAndroidID())
                .append(",wifiMac:").append(DeviceUtils.getMacAddress())
                .append(".blueMac:").append(DeviceUtils.getBlueMac())
                .append(",markId:").append(DeviceUtils.getMarkId())
                .append(",cpuModel:").append(DeviceUtils.getCpuModel())
                .append(",brand:").append(DeviceUtils.getDeviceBrand())
                .append(",androidModel:").append(DeviceUtils.getModel())
                .append(",androidVersion:").append(DeviceUtils.getSDKVersionName())
                .append(",storageSize:").append(DeviceUtils.getTotalInternalMemorySize())
                .append(",screenSize:").append(DeviceUtils.getScreenSize());
        return stringBuffer.toString();
    }

    /**
     * 判断是否需要显示 title
     *
     * @since 1.0
     */
    @JavascriptInterface
    boolean isShowTitle() {
        return Constant.IS_SHOW_TITLE;
    }

    /**
     * 接受流量主的金币数和可兑换的金额,由流量主客户端传入
     * @param gold
     * @param money
     */
    public void setAccountInfo(int gold, float money) {
        this.gold = gold;
        this.money = money;
    }

    /**
     * 获取到的金币和金额提供给前端
     * @param object
     * @return
     */
    @JavascriptInterface
    String getAccountInfo(Object object) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("gold:")
                .append(gold)
                .append(",money:")
                .append(money);
        return stringBuilder.toString();
    }

    /**
     * 跳转app，json数据, scheme packageName
     * @param Module
     */
    @JavascriptInterface
    boolean openSchemeInApp(Object Module) {
        JSONObject jsonObject = (JSONObject)Module;
        String scheme = jsonObject.optString("scheme");
        String packageName = jsonObject.optString("packageName");

        try {
            if (Utils.isAppInstalled(packageName, context)) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(scheme));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(intent);
                listener.consumeSuccess();
                return true;
            } else {
                Toast.makeText(context, "当前设备未安装手机淘宝，请安装后再重试一下",Toast.LENGTH_LONG).show();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 金币兑换淘礼金成功后回调
     */
    @JavascriptInterface
    void consumeSuccess(Object object) {
        if (listener != null) {
            listener.consumeSuccess();
        }
    }

    /**
     * 用户赚金币按钮是否可用
     * @param object
     * @return
     */
    @JavascriptInterface
    boolean getEarnGoldStatus(Object object) {
        return isEarnGoldEnable;
    }

    /**
     * 流量主客户端赚金币
     * @param object
     */
    @JavascriptInterface
    void earnGold(Object object) {
        if (listener != null) {
            listener.earnGold();
        }
    }

    /**
     * 使用一个新的 WebView 来加载跳转链接，这样做的好处是不会白屏
     *
     * @param url 跳转链接
     * @since 1.0
     */
    private void loadUrlUseNewWebView(final String url) {
        if (webview != null) {
            RunUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 新创建一个 WebView 来加载，这样就不会出现白屏的问题
                    ECWebView ecWebView = new ECWebView(context);
                    ecWebView.loadUrl(url);
                }
            });
        }
    }

    /**
     * token 失效
     *
     * @since 1.0
     */
    @JavascriptInterface
    void tokenFail(Object object) {
        if (listener != null) {
            listener.fail(ErrorCode.TOKEN_FAIL);
        }
    }

    /**
     * 点击 H5 返回按钮
     *
     * @since 1.0
     */
    @JavascriptInterface
    void goBack(Object object) {
        if (listener != null) {
            listener.goBack();
        }
    }

    /**
     * 首页需要拦截的 URL
     *
     * @since 1.0
     */
    @JavascriptInterface
    boolean interceptHomePageUrl(Object url) {
        if (webview != null && webview.getHomePageInterceptListener() != null) {
            try {
                return webview.getHomePageInterceptListener().interceptUrl((String) url);
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 判定首页是否需要拦截 URL
     *
     * @return true: 需要拦截  false: 不需要拦截
     */
    @JavascriptInterface
    boolean shouldInterceptHomePageUrl(Object object) {
        return webview.should_intercept_homepage_url;
    }

    /**
     * 设置监听器
     * 用于 App 和 H5 之间进行信息传递
     *
     * @param listener 监听器
     * @since 1.0
     */
    public void setOnApiResponseListener(OnApiResponseListener listener) {
        this.listener = listener;
    }
}
