package com.xidian.activities.advice;


import com.xidian.activities.common.result.Result;
import com.xidian.activities.common.exception.BizException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<?> handlerBizException(BizException e) {
        return Result.fail(e.getCode(), e.getMsg());
    }

    @ExceptionHandler(Throwable.class)
    public Result<?> handlerThrowable(Throwable e) {
        e.printStackTrace();
        return Result.fail(100, "未知异常");
    }

}
