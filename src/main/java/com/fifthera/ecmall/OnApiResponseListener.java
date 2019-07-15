package com.fifthera.ecmall;

/**
 * Api 回调接口
 * <p>
 * 通过这个接口与 App 进行交互。
 */
public interface OnApiResponseListener {
    /**
     * 返回失败的情况
     *
     * @param errorCode 错误码。关于各种错误码的含义，详见 {@link ErrorCode}
     * @since 1.0
     */
    void fail(int errorCode);

    /**
     * H5 点击了返回按钮
     */
    void goBack();

    /**
     * 金币兑换完成后回调
     */
    void consumeSuccess();

    void earnGold();

}
