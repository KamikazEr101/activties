package com.xidian.activities.common.exception;

import com.xidian.activities.common.result.ResultCodeEnum;
import lombok.Getter;

/**
 * 业务异常类
 *
 * @author
 * @since
 */
@Getter
public class BizException extends RuntimeException {

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误信息
     */
    private final String message;

    /**
     * 错误码枚举
     */
    private final ResultCodeEnum resultCodeEnum;

    public BizException(String message) {
        super(message);
        this.code = ResultCodeEnum.BAD_REQUEST.getCode();
        this.message = message;
        this.resultCodeEnum = ResultCodeEnum.BAD_REQUEST;
    }

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
        this.resultCodeEnum = null;
    }

    public BizException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
        this.message = resultCodeEnum.getMessage();
        this.resultCodeEnum = resultCodeEnum;
    }

    public BizException(ResultCodeEnum resultCodeEnum, String message) {
        super(message);
        this.code = resultCodeEnum.getCode();
        this.message = message;
        this.resultCodeEnum = resultCodeEnum;
    }

    /**
     * 静态工厂方法
     */
    public static BizException of(String message) {
        return new BizException(message);
    }

    public static BizException of(Integer code, String message) {
        return new BizException(code, message);
    }

    public static BizException of(ResultCodeEnum resultCodeEnum) {
        return new BizException(resultCodeEnum);
    }

    public static BizException of(ResultCodeEnum resultCodeEnum, String message) {
        return new BizException(resultCodeEnum, message);
    }
}
