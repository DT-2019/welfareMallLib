package com.fifthera.ecmall;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.annotation.RequiresApi;
import android.view.KeyEvent;
import android.webkit.ClientCertRequest;
import android.webkit.HttpAuthHandler;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.SafeBrowsingResponse;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 对外提供用于和 WebViewClient 进行通信的工具类
 *
 * @since 1.0
 */
public class ECWebViewClient extends WebViewClient {

    @Deprecated
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return false;
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return shouldOverrideUrlLoading(view, request.getUrl().toString());
    }

    public void onPageStarted(WebView view, String url, Bitmap favicon) {
    }


    public void onPageFinished(WebView view, String url) {
    }


    public void onLoadResource(WebView view, String url) {
    }


    public void onPageCommitVisible(WebView view, String url) {
    }


    @Deprecated
    public WebResourceResponse shouldInterceptRequest(WebView view,
                                                      String url) {
        return null;
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public WebResourceResponse shouldInterceptRequest(WebView view,
                                                      WebResourceRequest request) {
        return shouldInterceptRequest(view, request.getUrl().toString());
    }

    @Deprecated
    public void onTooManyRedirects(WebView view, Message cancelMsg,
                                   Message continueMsg) {
        cancelMsg.sendToTarget();
    }

    // These ints must match up to the hidden values in EventHandler.
    /**
     * Generic error
     */
    public static final int ERROR_UNKNOWN = -1;
    /**
     * Server or proxy hostname lookup failed
     */
    public static final int ERROR_HOST_LOOKUP = -2;
    /**
     * Unsupported authentication scheme (not basic or digest)
     */
    public static final int ERROR_UNSUPPORTED_AUTH_SCHEME = -3;
    /**
     * User authentication failed on server
     */
    public static final int ERROR_AUTHENTICATION = -4;
    /**
     * User authentication failed on proxy
     */
    public static final int ERROR_PROXY_AUTHENTICATION = -5;
    /**
     * Failed to connect to the server
     */
    public static final int ERROR_CONNECT = -6;
    /**
     * Failed to read or write to the server
     */
    public static final int ERROR_IO = -7;
    /**
     * Connection timed out
     */
    public static final int ERROR_TIMEOUT = -8;
    /**
     * Too many redirects
     */
    public static final int ERROR_REDIRECT_LOOP = -9;
    /**
     * Unsupported URI scheme
     */
    public static final int ERROR_UNSUPPORTED_SCHEME = -10;
    /**
     * Failed to perform SSL handshake
     */
    public static final int ERROR_FAILED_SSL_HANDSHAKE = -11;
    /**
     * Malformed URL
     */
    public static final int ERROR_BAD_URL = -12;
    /**
     * Generic file error
     */
    public static final int ERROR_FILE = -13;
    /**
     * File not found
     */
    public static final int ERROR_FILE_NOT_FOUND = -14;
    /**
     * Too many requests during this load
     */
    public static final int ERROR_TOO_MANY_REQUESTS = -15;
    /**
     * Resource load was cancelled by Safe Browsing
     */
    public static final int ERROR_UNSAFE_RESOURCE = -16;

    /**
     * @hide
     */
    @IntDef({
            SAFE_BROWSING_THREAT_UNKNOWN,
            SAFE_BROWSING_THREAT_MALWARE,
            SAFE_BROWSING_THREAT_PHISHING,
            SAFE_BROWSING_THREAT_UNWANTED_SOFTWARE
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface SafeBrowsingThreat {
    }

    /**
     * The resource was blocked for an unknown reason
     */
    public static final int SAFE_BROWSING_THREAT_UNKNOWN = 0;
    /**
     * The resource was blocked because it contains malware
     */
    public static final int SAFE_BROWSING_THREAT_MALWARE = 1;
    /**
     * The resource was blocked because it contains deceptive content
     */
    public static final int SAFE_BROWSING_THREAT_PHISHING = 2;
    /**
     * The resource was blocked because it contains unwanted software
     */
    public static final int SAFE_BROWSING_THREAT_UNWANTED_SOFTWARE = 3;


    @Deprecated
    public void onReceivedError(WebView view, int errorCode,
                                String description, String failingUrl) {
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        if (request.isForMainFrame()) {
            onReceivedError(view,
                    error.getErrorCode(), error.getDescription().toString(),
                    request.getUrl().toString());
        }
    }


    public void onReceivedHttpError(
            WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
    }

    /**
     * As the host application if the browser should resend data as the
     * requested page was a result of a POST. The default is to not resend the
     * data.
     *
     * @param view       The WebView that is initiating the callback.
     * @param dontResend The message to send if the browser should not resend
     * @param resend     The message to send if the browser should resend data
     */
    public void onFormResubmission(WebView view, Message dontResend,
                                   Message resend) {
        dontResend.sendToTarget();
    }


    public void doUpdateVisitedHistory(WebView view, String url,
                                       boolean isReload) {
    }

    public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                   SslError error) {
        handler.cancel();
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
        request.cancel();
    }


    public void onReceivedHttpAuthRequest(WebView view,
                                          HttpAuthHandler handler, String host, String realm) {
        handler.cancel();
    }


    public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
        return false;
    }


    public void onScaleChanged(WebView view, float oldScale, float newScale) {
    }


    public void onReceivedLoginRequest(WebView view, String realm,
                                       String account, String args) {
    }


    public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
        return false;
    }


    @RequiresApi(api = Build.VERSION_CODES.O_MR1)
    public void onSafeBrowsingHit(WebView view, WebResourceRequest request,
                                  @SafeBrowsingThreat int threatType, SafeBrowsingResponse callback) {
        callback.showInterstitial(/* allowReporting */ true);
    }
}
