package com.xidian.activities.controller;

import com.xidian.activities.common.result.Result;
import com.xidian.activities.common.exception.BizException;
import com.xidian.activities.common.result.ResultCodeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器 - 用于验证异常处理
 * 仅在开发环境启用
 *
 * @author
 * @since
 */
@Slf4j
@RestController
@RequestMapping("/test")
@Tag(name = "测试接口", description = "用于验证异常处理的测试接口")
@Profile({ "dev", "test" })
public class TestController {

    @GetMapping("/exception/biz")
    @Operation(summary = "测试业务异常", description = "测试BizException的统一处理")
    public Result<String> testBizException(@RequestParam(defaultValue = "true") boolean throwException) {
        if (throwException) {
            throw BizException.of(ResultCodeEnum.ACTIVITY_NOT_FOUND, "这是一个测试的业务异常");
        }
        return Result.ok("业务异常测试成功");
    }

    @GetMapping("/exception/param")
    @Operation(summary = "测试参数异常", description = "测试参数校验异常的统一处理")
    public Result<String> testParamException(@RequestParam(required = false) String requiredParam) {
        if (requiredParam == null || requiredParam.trim().isEmpty()) {
            throw BizException.of(ResultCodeEnum.DATA_ERROR, "必需参数不能为空");
        }
        return Result.ok("参数校验测试成功");
    }

    @GetMapping("/exception/runtime")
    @Operation(summary = "测试运行时异常", description = "测试运行时异常的统一处理")
    public Result<String> testRuntimeException(@RequestParam(defaultValue = "true") boolean throwException) {
        if (throwException) {
            throw new RuntimeException("这是一个测试的运行时异常");
        }
        return Result.ok("运行时异常测试成功");
    }
}