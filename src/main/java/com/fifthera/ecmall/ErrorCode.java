package com.fifthera.ecmall;

/**
 * 错误码
 *
 * App 可以根据返回的不同错误码来处理对应的情况。
 */
public class ErrorCode {

    /**
     * token 失效异常
     */
    public static final int TOKEN_FAIL = 0x01;

    /**
     * 分享图片合成失败
     */
    public static final int COMPOSITE_FAIL = 0x02;
}
