package com.zcw.picture.common;

import com.zcw.picture.exception.ErrorCode;

/**
 * 封装返回结果的工具类
 */
public class ResultUtils {

    /**
     * 成功
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 响应
     */
    public static <T> Response<T> success(T data) {
        return new Response<>(0, data, "ok");
    }

    /**
     * 失败
     *
     * @param errorCode 错误码
     * @return 响应
     */
    public static Response<?> error(ErrorCode errorCode) {
        return new Response<>(errorCode);
    }

    /**
     * 失败
     *
     * @param code    错误码
     * @param message 错误信息
     * @return 响应
     */
    public static Response<?> error(int code, String message) {
        return new Response<>(code, null, message);
    }

    /**
     * 失败
     *
     * @param errorCode 错误码
     * @return 响应
     */
    public static Response<?> error(ErrorCode errorCode, String message) {
        return new Response<>(errorCode.getCode(), null, message);
    }
}

