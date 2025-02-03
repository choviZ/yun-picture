package com.zcw.picture.common;

import com.zcw.picture.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 全局响应封装类
 */
@Data
public class Response<T> implements Serializable {

    private int code;

    private T data;

    private String msg;

    public Response(int code, T data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }

    public Response(int code, T data) {
        this(code,data,"");
    }

    public Response(ErrorCode errorCode) {
        this(errorCode.getCode(),null,errorCode.getMessage());
    }
}
