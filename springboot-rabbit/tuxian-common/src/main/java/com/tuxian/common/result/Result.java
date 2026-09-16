package com.tuxian.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回结果。
 * <p>
 * 前端约定：成功 code = "1"（字符串，注意不是 200），失败 code = "0"；
 * 数据统一放在 result 字段里。
 * <pre>
 * 成功：{ "code": "1", "msg": "操作成功", "result": ... }
 * 失败：{ "code": "0", "msg": "错误信息", "result": null }
 * </pre>
 * 说明：前端 axios 错误拦截器读取的是 e.response.data.message，因此失败时额外
 * 携带 message 字段（与 msg 一致），保证前端能正确弹出错误提示。
 */
@Data
public class Result<T> implements Serializable {

    private String code;
    private String msg;

    /** 仅失败时携带，与 msg 保持一致，用于适配前端错误提示 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message;

    private T result;

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.code = "1";
        result.msg = "操作成功";
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = success();
        result.result = data;
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.code = "0";
        result.msg = msg;
        result.message = msg;
        return result;
    }
}