package com.tuxian.common.exception;

/**
 * 业务异常基类。
 * <p>
 * 业务代码中遇到预期内的错误（账号不存在、库存不足等）时，抛出本异常，
 * 由全局异常处理器统一捕获并转成前端约定的返回结构。
 */
public class BaseException extends RuntimeException {

    public BaseException() {
    }

    public BaseException(String message) {
        super(message);
    }
}