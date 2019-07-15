package com.fifthera.ecmall;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Keep;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ClientCertRequest;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.HttpAuthHandler;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.SafeBrowsingResponse;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fifthera.ecmall.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * BaseWebView
 * <p>
 * 尽量不要再这个类里面修改代码，如果需要修改相应功能，请在 ECWebView 里面修改。
 */

@SuppressWarnings("unused")
abstract
class ECBaseWebView extends WebView {
    private static final String BRIDGE_NAME = "_dsbridge";
    private static final String LOG_TAG = "dsBridge";
    private static boolean isDebug = false;
    private Map<String, Object> javaScriptNamespaceInterfaces = new HashMap<String, Object>();
    private String APP_CACHE_DIRNAME;
    private int callID = 0;
    private String currentUrl = "";

    private volatile boolean alertBoxBlock = true;
    private JavascriptCloseWindowListener javascriptCloseWindowListener = null;
    private ArrayList<CallInfo> callInfoList;
    private InnerJavascriptInterface innerJavascriptInterface = new InnerJavascriptInterface();
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    public boolean should_intercept_homepage_url = false;
    private boolean isShouldShowProgress = true;
    private boolean isDefaultProgressFisrt = true;
    private ProgressBar progressBar;
    private ImageView defaultView;
    private View errorView;
    private JSApi jsApi;
    private ECWebViewClient webViewListener;
    private ECWebChromeClient webChromeListener;
    private HomePageInterceptListener homePageInterceptListener;

    public void setOnWebViewClientListener(ECWebViewClient listener) {
        this.webViewListener = listener;
    }

    public void setOnWebChromeClientListener(ECWebChromeClient listener) {
        this.webChromeListener = listener;
    }

    public void shouldInterceptHomePageUrl(HomePageInterceptListener homePageInterceptListener) {
        this.homePageInterceptListener = homePageInterceptListener;
    }

    @Nullable
    public HomePageInterceptListener getHomePageInterceptListener() {
        return homePageInterceptListener;
    }

    public void setShouldShowProgress(boolean isShow) {
        isShouldShowProgress = isShow;
    }

    private class InnerJavascriptInterface {

        private void PrintDebugInfo(String error) {
            Log.d(LOG_TAG, error);
            if (isDebug) {
                evaluateJavascript(String.format("alert('%s')", "DEBUG ERR MSG:\\n" + error.replaceAll("\\'", "\\\\'")));
            }
        }

        @Keep
        @JavascriptInterface
        public String call(String methodName, String argStr) {
            String error = "Js bridge  called, but can't find a corresponded " +
                    "JavascriptInterface object , please check your code!";
            String[] nameStr = parseNamespace(methodName.trim());
            methodName = nameStr[1];
            Object jsb = javaScriptNamespaceInterfaces.get(nameStr[0]);
            JSONObject ret = new JSONObject();
            try {
                ret.put("code", -1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (jsb == null) {
                PrintDebugInfo(error);
                return ret.toString();
            }
            Object arg = null;
            Method method = null;
            String callback = null;

            try {
                JSONObject args = new JSONObject(argStr);
                if (args.has("_dscbstub")) {
                    callback = args.getString("_dscbstub");
                }
                if (args.has("data")) {
                    arg = args.get("data");
                }
            } catch (JSONException e) {
                error = String.format("The argument of \"%s\" must be a JSON object string!", methodName);
                PrintDebugInfo(error);
                e.printStackTrace();
                return ret.toString();
            }


            Class<?> cls = jsb.getClass();
            boolean asyn = false;

            // 检查 JSApi 是否有对应的方法（方法一般都是非公开权限的）
            try {
                method = cls.getDeclaredMethod(methodName, Object.class, CompletionHandler.class);
                asyn = true;
            } catch (Exception e) {
                try {
                    method = cls.getDeclaredMethod(methodName, Object.class);
                } catch (Exception ignored) {

                }
            }

            if (method == null) {
                error = "Not find method \"" + methodName + "\" implementation! please check if the  signature or namespace of the method is right ";
                PrintDebugInfo(error);
                return ret.toString();
            }


            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                JavascriptInterface annotation = method.getAnnotation(JavascriptInterface.class);
                if (annotation == null) {
                    error = "Method " + methodName + " is not invoked, since  " +
                            "it is not declared with JavascriptInterface annotation! ";
                    PrintDebugInfo(error);
                    return ret.toString();
                }
            }

            Object retData;
            method.setAccessible(true);
            try {
                if (asyn) {
                    final String cb = callback;
                    method.invoke(jsb, arg, new CompletionHandler() {

                        @Override
                        public void complete(Object retValue) {
                            complete(retValue, true);
                        }

                        @Override
                        public void complete() {
                            complete(null, true);
                        }

                        @Override
                        public void setProgressData(Object value) {
                            complete(value, false);
                        }

                        private void complete(Object retValue, boolean complete) {
                            try {
                                JSONObject ret = new JSONObject();
                                ret.put("code", 0);
                                ret.put("data", retValue);
                                //retValue = URLEncoder.encode(ret.toString(), "UTF-8").replaceAll("\\+", "%20");
                                if (cb != null) {
                                    //String script = String.format("%s(JSON.parse(decodeURIComponent(\"%s\")).data);", cb, retValue);
                                    String script = String.format("%s(%s.data);", cb, ret.toString());
                                    if (complete) {
                                        script += "delete window." + cb;
                                    }
                                    //Log.d(LOG_TAG, "complete " + script);
                                    evaluateJavascript(script);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    retData = method.invoke(jsb, arg);
                    ret.put("code", 0);
                    ret.put("data", retData);
                    return ret.toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
                error = String.format("Call failed：The parameter of \"%s\" in Java is invalid.", methodName);
                PrintDebugInfo(error);
                return ret.toString();
            }
            return ret.toString();
        }

    }

    Map<Integer, OnReturnValue> handlerMap = new HashMap<>();

    public interface JavascriptCloseWindowListener {
        /**
         * @return If true, close the current activity, otherwise, do nothing.
         */
        boolean onClose();
    }


    @Deprecated
    public interface FileChooser {
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        void openFileChooser(ValueCallback valueCallback, String acceptType);

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        void openFileChooser(ValueCallback<Uri> valueCallback,
                             String acceptType, String capture);
    }

    public ECBaseWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ECBaseWebView(Context context) {
        super(context);
        init();
    }

    /**
     * Set debug mode. if in debug mode, some errors will be prompted by a dialog
     * and the exception caused by the native handlers will not be captured.
     */
    public static void setWebContentsDebuggingEnabled(boolean enabled) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(enabled);
        }
        isDebug = enabled;
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void init() {
        APP_CACHE_DIRNAME = getContext().getFilesDir().getAbsolutePath() + "/webcache";
        WebSettings settings = getSettings();
        settings.setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true);
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        setNetworkError();
        setProgressDefault();
        setProgressBar();

        if (isShouldShowProgress) {
            addView(defaultView);
            addView(progressBar);
        }
        settings.setAllowFileAccess(false);
        settings.setAppCacheEnabled(false);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setAppCachePath(APP_CACHE_DIRNAME);
        settings.setUseWideViewPort(true);
        super.setWebChromeClient(mWebChromeClient);
        super.setWebViewClient(mWebViewClient);
        addInternalJavascriptObject();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            super.addJavascriptInterface(innerJavascriptInterface, BRIDGE_NAME);
        } else {
            // add dsbridge tag in lower android version
            settings.setUserAgentString(settings.getUserAgentString() + " _dsbridge");
        }
    }

    private void setProgressBar() {
        progressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleSmallTitle);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int paddingWidth = width / 9 * 4;
        progressBar.setPadding(paddingWidth, 300, paddingWidth, 300);
        progressBar.setLayoutParams(lp);

    }

    private void setProgressDefault() {
        defaultView = new ImageView(getContext());
        if (Utils.isHomeDefault(currentUrl)) {
            defaultView.setImageResource(R.drawable.ec_img_progress_default_common);
        } else {
            defaultView.setImageResource(R.drawable.ec_img_progress_default_detail);
        }
        defaultView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        defaultView.setScaleType(ImageView.ScaleType.FIT_XY);

    }

    private void setNetworkError() {
        errorView = inflate(getContext(), R.layout.ec_layout, null);
        errorView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(errorView);
        TextView clickAgain = errorView.findViewById(R.id.click_again);

        errorView.setVisibility(View.GONE);
        clickAgain.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // jsApi.getECBaseWebview().reload();
                ECBaseWebView.this.reload();
                // ECBaseWebView.this.reload2(currentUrl);
            }
        });
    }

    private String[] parseNamespace(String method) {
        int pos = method.lastIndexOf('.');
        String namespace = "";
        if (pos != -1) {
            namespace = method.substring(0, pos);
            method = method.substring(pos + 1);
        }
        return new String[]{namespace, method};
    }

    @Keep
    private void addInternalJavascriptObject() {
        addJavascriptObject(new Object() {
            @Keep
            @JavascriptInterface
            public boolean hasNativeMethod(Object args) throws JSONException {
                JSONObject jsonObject = (JSONObject) args;
                String methodName = jsonObject.getString("name").trim();
                String type = jsonObject.getString("type").trim();
                String[] nameStr = parseNamespace(methodName);
                Object jsb = javaScriptNamespaceInterfaces.get(nameStr[0]);
                if (jsb != null) {
                    Class<?> cls = jsb.getClass();
                    boolean asyn = false;
                    Method method = null;
                    try {
                        method = cls.getDeclaredMethod(nameStr[1],
                                Object.class, CompletionHandler.class);
                        asyn = true;
                    } catch (Exception e) {
                        try {
                            method = cls.getDeclaredMethod(nameStr[1], Object.class);
                        } catch (Exception ex) {

                        }
                    }
                    if (method != null) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            JavascriptInterface annotation = method.getAnnotation(JavascriptInterface.class);
                            if (annotation == null) {
                                return false;
                            }
                        }
                        return "all".equals(type) || (asyn && "asyn".equals(type) || (!asyn && "syn".equals(type)));

                    }
                }
                return false;
            }

            @Keep
            @JavascriptInterface
            public String closePage(Object object) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (javascriptCloseWindowListener == null
                                || javascriptCloseWindowListener.onClose()) {
                            Context context = getContext();
                            if (context instanceof Activity) {
                                ((Activity) context).onBackPressed();
                            }
                        }
                    }
                });
                return null;
            }

            @Keep
            @JavascriptInterface
            public void disableJavascriptDialogBlock(Object object) throws JSONException {
                JSONObject jsonObject = (JSONObject) object;
                alertBoxBlock = !jsonObject.getBoolean("disable");
            }

            @Keep
            @JavascriptInterface
            public void dsinit(Object jsonObject) {
                ECBaseWebView.this.dispatchStartupQueue();
            }

            @Keep
            @JavascriptInterface
            public void returnValue(final Object obj) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject jsonObject = (JSONObject) obj;
                        Object data = null;
                        try {
                            int id = jsonObject.getInt("id");
                            boolean isCompleted = jsonObject.getBoolean("complete");
                            OnReturnValue handler = handlerMap.get(id);
                            if (jsonObject.has("data")) {
                                data = jsonObject.get("data");
                            }
                            if (handler != null) {
                                handler.onValue(data);
                                if (isCompleted) {
                                    handlerMap.remove(id);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

        }, "_dsb");
    }

    private void _evaluateJavascript(String script) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ECBaseWebView.super.evaluateJavascript(script, null);
        } else {
            super.loadUrl("javascript:" + script);
        }
    }

    /**
     * This method can be called in any thread, and if it is not called in the main thread,
     * it will be automatically distributed to the main thread.
     */
    public void evaluateJavascript(final String script) {
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                _evaluateJavascript(script);
            }
        });
    }

    /**
     * This method can be called in any thread, and if it is not called in the main thread,
     * it will be automatically distributed to the main thread.
     */
    @Override
    public void loadUrl(final String url) {
        currentUrl = url;
        if (isShouldShowProgress) {
            defaultView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            isDefaultProgressFisrt = false;
            isShouldShowProgress = false;
        }
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (url != null && url.startsWith("javascript:")) {
                    ECBaseWebView.super.loadUrl(url);
                } else {
                    callInfoList = new ArrayList<>();
                    ECBaseWebView.super.loadUrl(url);
                }
            }
        });
    }

    /**
     * This method can be called in any thread, and if it is not called in the main thread,
     * it will be automatically distributed to the main thread.
     */
    @Override
    public void loadUrl(final String url, final Map<String, String> additionalHttpHeaders) {
        currentUrl = url;
        if (isShouldShowProgress) {
            defaultView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            isDefaultProgressFisrt = false;
            isShouldShowProgress = false;
        }
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (url != null && url.startsWith("javascript:")) {
                    ECBaseWebView.super.loadUrl(url, additionalHttpHeaders);
                } else {
                    callInfoList = new ArrayList<>();
                    ECBaseWebView.super.loadUrl(url, additionalHttpHeaders);
                }
            }
        });
    }

    @Override
    public void reload() {
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                callInfoList = new ArrayList<>();
                ECBaseWebView.super.reload();
            }
        });
    }

    /**
     * set a webViewListener for javascript closing the current activity.
     */
    public void setJavascriptCloseWindowListener(JavascriptCloseWindowListener listener) {
        javascriptCloseWindowListener = listener;
    }


    private static class CallInfo {
        private String data;
        private int callbackId;
        private String method;

        CallInfo(String handlerName, int id, Object[] args) {
            if (args == null) args = new Object[0];
            data = new JSONArray(Arrays.asList(args)).toString();
            callbackId = id;
            method = handlerName;
        }

        @Override
        public String toString() {
            JSONObject jo = new JSONObject();
            try {
                jo.put("method", method);
                jo.put("callbackId", callbackId);
                jo.put("data", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jo.toString();
        }
    }

    private synchronized void dispatchStartupQueue() {
        if (callInfoList != null) {
            for (CallInfo info : callInfoList) {
                dispatchJavascriptCall(info);
            }
            callInfoList = null;
        }
    }

    private void dispatchJavascriptCall(CallInfo info) {
        evaluateJavascript(String.format("window._handleMessageFromNative(%s)", info.toString()));
    }

    protected synchronized <T> void callHandler(@ApiMethod.Api String method, Object[] args, final OnReturnValue<T> handler) {

        CallInfo callInfo = new CallInfo(method, callID++, args);
        if (handler != null) {
            handlerMap.put(callInfo.callbackId, handler);
        }

        if (callInfoList != null) {
            callInfoList.add(callInfo);
        } else {
            dispatchJavascriptCall(callInfo);
        }

    }

    protected void callHandler(@ApiMethod.Api String method, Object[] args) {
        callHandler(method, args, null);
    }

    protected <T> void callHandler(@ApiMethod.Api String method, OnReturnValue<T> handler) {
        callHandler(method, null, handler);
    }


    /**
     * Test whether the handler exist in javascript
     *
     * @param handlerName
     * @param existCallback
     */
    public void hasJavascriptMethod(String handlerName, OnReturnValue<Boolean> existCallback) {
        callHandler(ApiMethod.HAS_API, new Object[]{handlerName}, existCallback);
    }

    public void addJavascriptObject(JSApi jsApi) {
        if (jsApi == null) {
            return;
        }
        this.jsApi = jsApi;
        jsApi.setWebView(this);
        addJavascriptObject(jsApi, null);
    }

    /**
     * Add a java object which implemented the javascript interfaces to dsBridge with namespace.
     * Remove the object using {@link #removeJavascriptObject(String) removeJavascriptObject(String)}
     *
     * @param object
     * @param namespace if empty, the object have no namespace.
     */
    private void addJavascriptObject(Object object, String namespace) {
        if (namespace == null) {
            namespace = "";
        }
        if (object != null) {
            javaScriptNamespaceInterfaces.put(namespace, object);
        }
    }

    /**
     * remove the javascript object with supplied namespace.
     *
     * @param namespace
     */
    public void removeJavascriptObject(String namespace) {
        if (namespace == null) {
            namespace = "";
        }
        javaScriptNamespaceInterfaces.remove(namespace);

    }

    public WebChromeClient getmWebChromeClient() {
        return mWebChromeClient;
    }

    public void disableJavascriptDialogBlock(boolean disable) {
        alertBoxBlock = !disable;
    }

    private WebViewClient mWebViewClient = new WebViewClient() {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (webViewListener != null) {
                webViewListener.onPageStarted(view, url, favicon);
            } else {
                super.onPageStarted(view, url, favicon);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (webViewListener != null) {
                webViewListener.onPageFinished(view, url);
            } else {
                if (errorView != null) {
                    if (Utils.isNetConnected(getContext())) {
                        errorView.setVisibility(GONE);
                    } else {
                        errorView.setVisibility(VISIBLE);
                    }
                }
                if (progressBar != null && defaultView != null) {
                    progressBar.setVisibility(GONE);
                    defaultView.setVisibility(GONE);
                }
                super.onPageFinished(view, url);
            }
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            if (webViewListener != null) {
                webViewListener.onLoadResource(view, url);
            } else {
                super.onLoadResource(view, url);
            }
        }

        @Override
        public void onPageCommitVisible(WebView view, String url) {
            if (webViewListener != null) {
                webViewListener.onPageCommitVisible(view, url);
            } else {
                super.onPageCommitVisible(view, url);
            }
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            if (webViewListener != null) {
                return webViewListener.shouldInterceptRequest(view, url);
            } else {
                return super.shouldInterceptRequest(view, url);
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            if (webViewListener != null) {
                return webViewListener.shouldInterceptRequest(view, request);
            } else {
                return super.shouldInterceptRequest(view, request);
            }
        }

        @Override
        public void onTooManyRedirects(WebView view, Message cancelMsg, Message continueMsg) {
            if (webViewListener != null) {
                webViewListener.onTooManyRedirects(view, cancelMsg, continueMsg);
            } else {
                super.onTooManyRedirects(view, cancelMsg, continueMsg);
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            if (webViewListener != null) {
                webViewListener.onReceivedError(view, errorCode, description, failingUrl);
            } else {
                if (errorView != null) {
                    errorView.setVisibility(View.VISIBLE);
                }
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            if (webViewListener != null) {
                webViewListener.onReceivedError(view, request, error);
            } else {
                super.onReceivedError(view, request, error);
            }
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            if (webViewListener != null) {
                webViewListener.onReceivedHttpError(view, request, errorResponse);
            } else {
                super.onReceivedHttpError(view, request, errorResponse);
            }
        }

        @Override
        public void onFormResubmission(WebView view, Message dontResend, Message resend) {
            if (webViewListener != null) {
                webViewListener.onFormResubmission(view, dontResend, resend);
            } else {
                super.onFormResubmission(view, dontResend, resend);
            }
        }

        @Override
        public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
            if (webViewListener != null) {
                webViewListener.doUpdateVisitedHistory(view, url, isReload);
            } else {
                super.doUpdateVisitedHistory(view, url, isReload);
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            if (webViewListener != null) {
                webViewListener.onReceivedSslError(view, handler, error);
            } else {
                if (errorView != null) {
                    errorView.setVisibility(View.VISIBLE);
                }
                super.onReceivedSslError(view, handler, error);
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
            if (webViewListener != null) {
                webViewListener.onReceivedClientCertRequest(view, request);
            } else {
                super.onReceivedClientCertRequest(view, request);
            }
        }

        @Override
        public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
            if (webViewListener != null) {
                webViewListener.onReceivedHttpAuthRequest(view, handler, host, realm);
            } else {
                super.onReceivedHttpAuthRequest(view, handler, host, realm);
            }
        }

        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            if (webViewListener != null) {
                return webViewListener.shouldOverrideKeyEvent(view, event);
            } else {
                return super.shouldOverrideKeyEvent(view, event);
            }
        }

        @Override
        public void onScaleChanged(WebView view, float oldScale, float newScale) {
            if (webViewListener != null) {
                webViewListener.onScaleChanged(view, oldScale, newScale);
            } else {
                super.onScaleChanged(view, oldScale, newScale);
            }
        }

        @Override
        public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
            if (webViewListener != null) {
                webViewListener.onReceivedLoginRequest(view, realm, account, args);
            } else {
                super.onReceivedLoginRequest(view, realm, account, args);
            }
        }

        @Override
        public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
            if (webViewListener != null) {
                return webViewListener.onRenderProcessGone(view, detail);
            } else {
                return super.onRenderProcessGone(view, detail);
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.O_MR1)
        @Override
        public void onSafeBrowsingHit(WebView view, WebResourceRequest request, int threatType, SafeBrowsingResponse callback) {
            if (webViewListener != null) {
                webViewListener.onSafeBrowsingHit(view, request, threatType, callback);
            } else {
                super.onSafeBrowsingHit(view, request, threatType, callback);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (!Utils.isValidUrl(url)) {
                // 如果不是 http 链接或者 https 链接，则跳转 scheme。
                boolean isJumpSuccess = Utils.jump(url, getContext());
                // 通知 h5 跳转结果
                jumpStatus(isJumpSuccess);
                return true;
            }
            if (webViewListener != null) {
                return webViewListener.shouldOverrideUrlLoading(view, url);
            } else {
                return super.shouldOverrideUrlLoading(view, url);
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            if (!Utils.isValidUrl(url)) {
                // 如果不是 http 链接或者 https 链接，则跳转 scheme。
                boolean isJumpSuccess = Utils.jump(url, getContext());
                // 通知 h5 跳转结果
                jumpStatus(isJumpSuccess);
                return true;
            }
            if (webViewListener != null) {
                return webViewListener.shouldOverrideUrlLoading(view, request);
            } else {
                return super.shouldOverrideUrlLoading(view, request);
            }
        }
    };

    /**
     * 跳转 scheme 状态
     */
    abstract protected void jumpStatus(boolean isSuccess);

    private WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (webChromeListener != null) {
                webChromeListener.onProgressChanged(view, newProgress);
            } else {
                if (progressBar != null && defaultView != null) {
                    if (newProgress < 95) {
                        progressBar.setVisibility(View.VISIBLE);
                        if (isDefaultProgressFisrt && Utils.isHomeDefault(currentUrl)) {
                            defaultView.setVisibility(VISIBLE);
                            isDefaultProgressFisrt = false;
                        }
                        progressBar.setProgress(newProgress);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        defaultView.setVisibility(GONE);
                    }
                }
                super.onProgressChanged(view, newProgress);
            }
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            if (webChromeListener != null) {
                webChromeListener.onReceivedTitle(view, title);
            } else {
                super.onReceivedTitle(view, title);
            }
        }

        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            if (webChromeListener != null) {
                webChromeListener.onReceivedIcon(view, icon);
            } else {
                super.onReceivedIcon(view, icon);
            }
        }

        @Override
        public void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed) {
            if (webChromeListener != null) {
                webChromeListener.onReceivedTouchIconUrl(view, url, precomposed);
            } else {
                super.onReceivedTouchIconUrl(view, url, precomposed);
            }
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            if (webChromeListener != null) {
                webChromeListener.onShowCustomView(view, callback);
            } else {
                super.onShowCustomView(view, callback);
            }
        }

        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        public void onShowCustomView(View view, int requestedOrientation,
                                     CustomViewCallback callback) {
            if (webChromeListener != null) {
                webChromeListener.onShowCustomView(view, requestedOrientation, callback);
            } else {
                super.onShowCustomView(view, requestedOrientation, callback);
            }
        }

        @Override
        public void onHideCustomView() {
            if (webChromeListener != null) {
                webChromeListener.onHideCustomView();
            } else {
                super.onHideCustomView();
            }
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog,
                                      boolean isUserGesture, Message resultMsg) {
            if (webChromeListener != null) {
                return webChromeListener.onCreateWindow(view, isDialog,
                        isUserGesture, resultMsg);
            }
            return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
        }

        @Override
        public void onRequestFocus(WebView view) {
            if (webChromeListener != null) {
                webChromeListener.onRequestFocus(view);
            } else {
                super.onRequestFocus(view);
            }
        }

        @Override
        public void onCloseWindow(WebView window) {
            if (webChromeListener != null) {
                webChromeListener.onCloseWindow(window);
            } else {
                super.onCloseWindow(window);
            }
        }

        @Override
        public boolean onJsAlert(WebView view, String url, final String message, final JsResult result) {
            if (!alertBoxBlock) {
                result.confirm();
            }
            if (webChromeListener != null) {
                if (webChromeListener.onJsAlert(view, url, message, result)) {
                    return true;
                }
            }
            if (BuildConfig.DEBUG) {
                try {
                    Dialog alertDialog = new AlertDialog.Builder(getContext()).
                            setMessage(message).
                            setCancelable(false).
                            setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    if (alertBoxBlock) {
                                        result.confirm();
                                    }
                                }
                            })
                            .create();
                    alertDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message,
                                   final JsResult result) {
            if (!alertBoxBlock) {
                result.confirm();
            }
            if (webChromeListener != null && webChromeListener.onJsConfirm(view, url, message, result)) {
                return true;
            } else {
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (alertBoxBlock) {
                            if (which == Dialog.BUTTON_POSITIVE) {
                                result.confirm();
                            } else {
                                result.cancel();
                            }
                        }
                    }
                };
                new AlertDialog.Builder(getContext())
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, listener)
                        .setNegativeButton(android.R.string.cancel, listener).show();
                return true;
            }

        }

        @Override
        public boolean onJsPrompt(WebView view, String url, final String message,
                                  String defaultValue, final JsPromptResult result) {

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                String prefix = "_dsbridge=";
                if (message.startsWith(prefix)) {
                    result.confirm(innerJavascriptInterface.call(message.substring(prefix.length()), defaultValue));
                    return true;
                }
            }

            if (!alertBoxBlock) {
                result.confirm();
            }

            if (webChromeListener != null && webChromeListener.onJsPrompt(view, url, message, defaultValue, result)) {
                return true;
            } else {
                final EditText editText = new EditText(getContext());
                editText.setText(defaultValue);
                if (defaultValue != null) {
                    editText.setSelection(defaultValue.length());
                }
                float dpi = getContext().getResources().getDisplayMetrics().density;
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (alertBoxBlock) {
                            if (which == Dialog.BUTTON_POSITIVE) {
                                result.confirm(editText.getText().toString());
                            } else {
                                result.cancel();
                            }
                        }
                    }
                };
                new AlertDialog.Builder(getContext())
                        .setTitle(message)
                        .setView(editText)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, listener)
                        .setNegativeButton(android.R.string.cancel, listener)
                        .show();
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                int t = (int) (dpi * 16);
                layoutParams.setMargins(t, 0, t, 0);
                layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
                editText.setLayoutParams(layoutParams);
                int padding = (int) (15 * dpi);
                editText.setPadding(padding - (int) (5 * dpi), padding, padding, padding);
                return true;
            }

        }

        @Override
        public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
            if (webChromeListener != null) {
                return webChromeListener.onJsBeforeUnload(view, url, message, result);
            }
            return super.onJsBeforeUnload(view, url, message, result);
        }

        @Override
        public void onExceededDatabaseQuota(String url, String databaseIdentifier, long quota,
                                            long estimatedDatabaseSize,
                                            long totalQuota,
                                            WebStorage.QuotaUpdater quotaUpdater) {
            if (webChromeListener != null) {
                webChromeListener.onExceededDatabaseQuota(url, databaseIdentifier, quota,
                        estimatedDatabaseSize, totalQuota, quotaUpdater);
            } else {
                super.onExceededDatabaseQuota(url, databaseIdentifier, quota,
                        estimatedDatabaseSize, totalQuota, quotaUpdater);
            }
        }

        @Override
        public void onReachedMaxAppCacheSize(long requiredStorage, long quota, WebStorage.QuotaUpdater quotaUpdater) {
            if (webChromeListener != null) {
                webChromeListener.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater);
            }
            super.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater);
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            if (webChromeListener != null) {
                webChromeListener.onGeolocationPermissionsShowPrompt(origin, callback);
            } else {
                super.onGeolocationPermissionsShowPrompt(origin, callback);
            }
        }

        @Override
        public void onGeolocationPermissionsHidePrompt() {
            if (webChromeListener != null) {
                webChromeListener.onGeolocationPermissionsHidePrompt();
            } else {
                super.onGeolocationPermissionsHidePrompt();
            }
        }


        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public void onPermissionRequest(PermissionRequest request) {
            if (webChromeListener != null) {
                webChromeListener.onPermissionRequest(request);
            } else {
                super.onPermissionRequest(request);
            }
        }


        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onPermissionRequestCanceled(PermissionRequest request) {
            if (webChromeListener != null) {
                webChromeListener.onPermissionRequestCanceled(request);
            } else {
                super.onPermissionRequestCanceled(request);
            }
        }

        @Override
        public boolean onJsTimeout() {
            if (webChromeListener != null) {
                return webChromeListener.onJsTimeout();
            }
            return super.onJsTimeout();
        }

        @Override
        public void onConsoleMessage(String message, int lineNumber, String sourceID) {
            if (webChromeListener != null) {
                webChromeListener.onConsoleMessage(message, lineNumber, sourceID);
            } else {
                super.onConsoleMessage(message, lineNumber, sourceID);
            }
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            if (webChromeListener != null) {
                return webChromeListener.onConsoleMessage(consoleMessage);
            }
            return super.onConsoleMessage(consoleMessage);
        }

        @Override
        public Bitmap getDefaultVideoPoster() {

            if (webChromeListener != null) {
                return webChromeListener.getDefaultVideoPoster();
            }
            return super.getDefaultVideoPoster();
        }

        @Override
        public View getVideoLoadingProgressView() {
            if (webChromeListener != null) {
                return webChromeListener.getVideoLoadingProgressView();
            }
            return super.getVideoLoadingProgressView();
        }

        @Override
        public void getVisitedHistory(ValueCallback<String[]> callback) {
            if (webChromeListener != null) {
                webChromeListener.getVisitedHistory(callback);
            } else {
                super.getVisitedHistory(callback);
            }
        }


        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                         FileChooserParams fileChooserParams) {
            if (webChromeListener != null) {
                return webChromeListener.onShowFileChooser(webView, filePathCallback, fileChooserParams);
            }
            return super.onShowFileChooser(webView, filePathCallback, fileChooserParams);
        }


        @Keep
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public void openFileChooser(ValueCallback valueCallback, String acceptType) {
            if (webChromeListener instanceof FileChooser) {
                ((FileChooser) webChromeListener).openFileChooser(valueCallback, acceptType);
            }
        }


        @Keep
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        public void openFileChooser(ValueCallback<Uri> valueCallback,
                                    String acceptType, String capture) {
            if (webChromeListener instanceof FileChooser) {
                ((FileChooser) webChromeListener).openFileChooser(valueCallback, acceptType, capture);
            }
        }

    };

    @Override
    public void clearCache(boolean includeDiskFiles) {
        super.clearCache(includeDiskFiles);
        CookieManager.getInstance().removeAllCookie();
        Context context = getContext();
        try {
            context.deleteDatabase("webview.db");
            context.deleteDatabase("webviewCache.db");
        } catch (Exception e) {
            e.printStackTrace();
        }

        File appCacheDir = new File(APP_CACHE_DIRNAME);
        File webviewCacheDir = new File(context.getCacheDir()
                .getAbsolutePath() + "/webviewCache");

        if (webviewCacheDir.exists()) {
            deleteFile(webviewCacheDir);
        }

        if (appCacheDir.exists()) {
            deleteFile(appCacheDir);
        }
    }

    public void deleteFile(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                File files[] = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    deleteFile(files[i]);
                }
            }
            file.delete();
        } else {
            Log.e("Webview", "delete file no exists " + file.getAbsolutePath());
        }
    }

    private void runOnMainThread(Runnable runnable) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            runnable.run();
            return;
        }
        mainHandler.post(runnable);
    }
}
