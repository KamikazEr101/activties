package com.xidian.activities.common.exception;

import com.xidian.activities.common.result.ResultCodeEnum;
import lombok.Getter;

@Getter
public class BizException extends RuntimeException {
    private Integer code;
    private String msg;

    public BizException(Integer code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public BizException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
        this.msg = resultCodeEnum.getMessage();
    }
}
