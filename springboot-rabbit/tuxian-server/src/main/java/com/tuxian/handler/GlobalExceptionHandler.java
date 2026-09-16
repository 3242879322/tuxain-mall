package com.tuxian.handler;

import com.tuxian.common.constant.MessageConstant;
import com.tuxian.common.exception.BaseException;
import com.tuxian.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 * <p>
 * 捕获业务异常、参数校验异常、未知异常，统一转成前端约定的返回结构。
 * 返回非 2xx 状态码，前端 axios 错误拦截器会读取 body.message 弹出提示。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常。
     */
    @ExceptionHandler(BaseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> exceptionHandler(BaseException ex) {
        log.warn("业务异常：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 参数校验异常（@Valid 校验失败）。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> exceptionHandler(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数不合法";
        log.warn("参数校验异常：{}", message);
        return Result.error(message);
    }

    /**
     * 未知异常。
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> exceptionHandler(Exception ex) {
        log.error("系统异常", ex);
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }
}