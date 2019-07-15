# 		福利商城源码接入

## 接入方式

此源码作为一个module接入项目中，例如命名为lib_webview。

则在setting.gradle中加入代码如下：

```java
include ':app', ':lib_webview'
```

项目的build.gradle中加入如下代码：

``` java
implementation project(':lib_webview')
```

即可作为子module加入工程中

## 基本使用



使用 ECWebView 就像使用普通的 WebView 控件一样简单。您可以在 xml 布局文件中定义。

```xml
<com.fifthera.ecmall.ECWebView
     android:id="@+id/webview"
     android:layout_width="match_parent"
     android:layout_height="match_parent" />
```

然后在 Activity 或 Fragment 中对 ECWebView 进行初始化，以及一些必要的设置，典型的使用代码如下：

```java
// 获取 ECWebView 对象
ECWebView webView = findViewById(R.id.webview);
// 创建 JSApi 对象，默认允许用户跳转赚金币页面
JSApi api = new JSApi(this);
//创建 JSApi对象，设置是否允许用户跳转赚金币页面。true: 允许， false：不允许
JSApi api = new JSApi(this, false);
// 将 ECWebView 对象和 JSApi 对象进行绑定
webView.addJavascriptObject(api);
// 处理 JSApi 的返回结果
api.setOnApiResponseListener(new OnApiResponseListener() {
    @Override
    public void fail(int errorCode) {
        if (errorCode == ErrorCode.TOKEN_FAIL) {
            Toast.makeText(TestActivity.this, "token失效了", Toast.LENGTH_SHORT).show();
        }
    }
	@Override
    public void goBack() {
        //处理H5返回按钮的事件
    }
    @Override
    public void consumeSuccess() {
        //用户金币兑换淘礼金成功后后的回调
        mWebView.refresh();
    }
    @Override
    public void earnGold() {
        //客户端实现跳转用户赚金币页面
    }
});
//sdk授权操作
webview.loadUrl(getAuthorityUrl());

String getAuthorityUrl() {
        Map<String, Object> map = new HashMap<>();
        String uid = "xxxxxxxxxxxx";//用户的唯一id
    	String clientId = "xxxxxxxxxxxxxxx"; //兜推后台申请的clientId
    	String clientSecret = "xxxxxxxxxxxx"; //兜推后台申请的clientSecret
        long currentTime = System.currentTimeMillis() / 1000;
        map.put("uid", uid);
        map.put("timestamp", currentTime);
        map.put("client_id", clientId);
        map.put("type", "page.taolijin");
        if (ecWebView != null) {
            String url = ecWebView.getAuthorityUrl(clientSecret, map, isDebug);
            return url;
        } else {
            return "";
        }
    }


```

ECWebView 已经封装了 `WebChromeClient` 的实现和 `WebViewClient` 的实现，因此不应该再为 ECWebView 设置 `WebChromeClient` 或 `WebViewClient`了。但是如果有特殊需求，需要处理 `WebChromeClient` 或 `WebViewClient` 的相关回调，我们提供了 `ECWebViewClient` 和 `ECWebChromeClient` 作为代替。

比如：

```java
webView.setOnWebViewClientListener(new ECWebViewClient() {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        // TODO 处理相关业务
        return super.shouldOverrideUrlLoading(view, url);
    }
});

webView.setOnWebChromeClientListener(new ECWebChromeClient() {
    @Override
    public void onReceivedTitle(WebView view, String title) {
        // TODO 处理相关业务
    }
});
```





## 方法说明

**JSApi 返回方法**

JSApi 封装了 ECWebView 和 H5 之间的通信，通信的结果由 OnApiResponseListener 监听器返回，该监听器返回的方法如下：

| 方法签名         | 含义                                                 |
| ---------------- | ---------------------------------------------------- |
| fail(int code)   | api 调用失败后的回调，code 为错误码。                |
| goBack()         | 处理H5的返回事件                                     |
| consumeSuccess() | 用户金币兑换淘礼金成功后回调，用于刷新用户剩余金币数 |





**错误码**

在 JSApi 里面的 fail() 方法中返回的错误码详情如下：

| 错误码     | 含义                     |
| ---------- | ------------------------ |
| TOKEN_FAIL | token 失效，需要重新授权 |

**ECWebView 调用方法**

ECWebView 里面调用的方法基本上都是用来告知 H5 相关信息的，目前有如下的方法：

| 方法签名                                               | 含义                   |
| ------------------------------------------------------ | ---------------------- |
| loadUrl(String url)                                    | 加载一个 URL           |
| addJavascriptObject(JSApi jsapi)                       | 绑定 JSApi             |
| setOnWebViewClientListener(ECWebViewClient client)     | 设置 ECWebViewClient   |
| setOnWebChromeClientListener(ECWebChromeClient client) | 设置 ECWebChromeClient |





