package com.xidian.activities.controller;

import com.xidian.activities.common.result.Result;
import com.xidian.activities.common.result.ResultCodeEnum;
import com.xidian.activities.dto.CheckInByTokenDTO;
import com.xidian.activities.service.RegistrationService;
import com.xidian.activities.util.JwtUtil;
import com.xidian.activities.vo.RegistrationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * H5/移动端接口控制器
 * 用于处理扫码签到等移动端请求(前后端分离)
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
@Slf4j
@RestController
@RequestMapping("/h5")
@Tag(name = "H5移动端接口", description = "移动端H5页面相关接口")
public class H5Controller {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 验证签到Token并获取活动信息
     * 前端扫码后首先调用此接口验证Token是否有效
     *
     * @param token 签到Token(从二维码URL参数获取)
     * @return Token验证结果和活动ID
     */
    @GetMapping("/checkin/validate")
    @Operation(summary = "验证签到Token", description = "扫描二维码后验证Token是否有效")
    public Result<Map<String, Object>> validateCheckInToken(
            @Parameter(description = "签到Token", required = true) @RequestParam String token) {

        log.info("验证签到Token: token={}", token);

        // 验证Token是否有效
        if (!jwtUtil.validateCheckInToken(token)) {
            return Result.fail(ResultCodeEnum.TOKEN_INVALID.getCode(),
                    "二维码已失效，请联系工作人员刷新");
        }

        // 从Token中获取活动ID
        Long activityId = jwtUtil.getActivityIdFromCheckInToken(token);
        if (activityId == null) {
            return Result.fail(ResultCodeEnum.TOKEN_INVALID.getCode(),
                    "无效的签到二维码");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("valid", true);
        data.put("activityId", activityId);
        data.put("checkInToken", token);

        log.info("签到Token验证成功: activityId={}", activityId);
        return Result.ok(data);
    }

    /**
     * H5扫码签到接口
     * 前端H5页面通过此接口提交签到请求
     *
     * @param checkInByTokenDTO 签到请求
     * @return 签到结果
     */
    @PostMapping("/checkin")
    @Operation(summary = "H5扫码签到", description = "通过Token和手机号完成签到")
    public Result<RegistrationVO> checkIn(
            @Valid @RequestBody @Parameter(description = "签到信息", required = true) CheckInByTokenDTO checkInByTokenDTO) {

        log.info("H5扫码签到请求: 手机号={}", checkInByTokenDTO.getStudentPhone());
        RegistrationVO registrationVO = registrationService.checkInByToken(checkInByTokenDTO);
        log.info("H5扫码签到成功: 报名ID={}", registrationVO.getId());

        return Result.ok(registrationVO);
    }

    /**
     * 签到页面配置信息
     * 前端可以调用此接口获取页面展示所需的配置
     *
     * @return 配置信息
     */
    @GetMapping("/config")
    @Operation(summary = "获取H5配置", description = "获取H5页面配置信息")
    public Result<PageConfig> getPageConfig() {
        PageConfig config = new PageConfig();
        config.setAppName("高校学生活动管理系统");
        config.setCheckInTitle("活动签到");
        config.setPhonePlaceholder("请输入报名手机号");
        config.setSubmitButtonText("立即签到");

        return Result.ok(config);
    }

    /**
     * H5页面配置
     */
    @Data
    public static class PageConfig {
        private String appName;
        private String checkInTitle;
        private String phonePlaceholder;
        private String submitButtonText;
    }
}
