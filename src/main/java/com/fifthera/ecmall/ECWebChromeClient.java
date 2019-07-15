package com.fifthera.ecmall;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;

public class ECWebChromeClient extends WebChromeClient {

    public void onProgressChanged(WebView view, int newProgress) {
    }

    public void onReceivedTitle(WebView view, String title) {
    }

    public void onReceivedIcon(WebView view, Bitmap icon) {
    }

    public void onReceivedTouchIconUrl(WebView view, String url,
                                       boolean precomposed) {
    }


    public interface CustomViewCallback {

        public void onCustomViewHidden();
    }

    public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
    }

    @Deprecated
    public void onShowCustomView(View view, int requestedOrientation,
                                 WebChromeClient.CustomViewCallback callback) {
    }

    public void onHideCustomView() {
    }

    public boolean onCreateWindow(WebView view, boolean isDialog,
                                  boolean isUserGesture, Message resultMsg) {
        return false;
    }


    public void onRequestFocus(WebView view) {
    }


    public void onCloseWindow(WebView window) {
    }


    public boolean onJsAlert(WebView view, String url, String message,
                             JsResult result) {
        return false;
    }


    public boolean onJsConfirm(WebView view, String url, String message,
                               JsResult result) {
        return false;
    }


    public boolean onJsPrompt(WebView view, String url, String message,
                              String defaultValue, JsPromptResult result) {
        return false;
    }


    public boolean onJsBeforeUnload(WebView view, String url, String message,
                                    JsResult result) {
        return false;
    }


    @Deprecated
    public void onExceededDatabaseQuota(String url, String databaseIdentifier,
                                        long quota, long estimatedDatabaseSize, long totalQuota,
                                        WebStorage.QuotaUpdater quotaUpdater) {
        // This default implementation passes the current quota back to WebCore.
        // WebCore will interpret this that new quota was declined.
        quotaUpdater.updateQuota(quota);
    }


    @Deprecated
    public void onReachedMaxAppCacheSize(long requiredStorage, long quota,
                                         WebStorage.QuotaUpdater quotaUpdater) {
        quotaUpdater.updateQuota(quota);
    }


    public void onGeolocationPermissionsShowPrompt(String origin,
                                                   GeolocationPermissions.Callback callback) {
    }


    public void onGeolocationPermissionsHidePrompt() {
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onPermissionRequest(PermissionRequest request) {
        request.deny();
    }

    public void onPermissionRequestCanceled(PermissionRequest request) {
    }


    @Deprecated
    public boolean onJsTimeout() {
        return true;
    }

    @Deprecated
    public void onConsoleMessage(String message, int lineNumber, String sourceID) {
    }


    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        // Call the old version of this function for backwards compatability.
        onConsoleMessage(consoleMessage.message(), consoleMessage.lineNumber(),
                consoleMessage.sourceId());
        return false;
    }


    public Bitmap getDefaultVideoPoster() {
        return null;
    }

    public View getVideoLoadingProgressView() {
        return null;
    }


    public void getVisitedHistory(ValueCallback<String[]> callback) {
    }


    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                     WebChromeClient.FileChooserParams fileChooserParams) {
        return false;
    }


    @Deprecated
    public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {
        uploadFile.onReceiveValue(null);
    }

    public void setupAutoFill(Message msg) {
    }
}
