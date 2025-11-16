package com.xidian.activities.controller;

import com.github.pagehelper.PageInfo;
import com.xidian.activities.common.result.Result;
import com.xidian.activities.dto.*;
import com.xidian.activities.service.RegistrationService;
import com.xidian.activities.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 报名控制器
 *
 * @author
 * @since
 */
@Slf4j
@RestController
@RequestMapping("/registration")
@Tag(name = "报名管理", description = "学生报名、签到相关接口")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    @PostMapping("/register")
    @Operation(summary = "学生报名", description = "学生报名参加活动")
    public Result<RegistrationVO> registerActivity(
            @Valid @RequestBody @Parameter(description = "报名信息", required = true) RegistrationDTO registrationDTO) {

        log.info("学生报名请求: 活动{}, 手机号{}", registrationDTO.getActivityId(), registrationDTO.getStudentPhone());
        RegistrationVO registrationVO = registrationService.registerActivity(registrationDTO);
        log.info("报名成功: 报名ID {}", registrationVO.getId());

        return Result.ok(registrationVO);
    }

    @PostMapping("/cancel/{registrationId}")
    @Operation(summary = "取消报名", description = "学生取消已报名的活动")
    public Result<RegistrationVO> cancelRegistration(
            @Parameter(description = "报名ID", required = true) @PathVariable Long registrationId) {

        log.info("取消报名请求: ID {}", registrationId);
        RegistrationVO registrationVO = registrationService.cancelRegistration(registrationId);
        log.info("取消报名成功: ID {}", registrationId);

        return Result.ok(registrationVO);
    }

    @PostMapping("/checkin")
    @Operation(summary = "学生签到", description = "学生签到参加活动")
    public Result<RegistrationVO> checkIn(
            @Valid @RequestBody @Parameter(description = "签到信息", required = true) CheckInDTO checkInDTO) {

        log.info("学生签到请求: 活动{}, 手机号{}", checkInDTO.getActivityId(), checkInDTO.getStudentPhone());
        RegistrationVO registrationVO = registrationService.checkIn(checkInDTO);
        log.info("签到成功: 报名ID {}", registrationVO.getId());

        return Result.ok(registrationVO);
    }

    @PostMapping("/checkin-by-token")
    @Operation(summary = "扫码签到", description = "通过扫描二维码获取Token进行签到")
    public Result<RegistrationVO> checkInByToken(
            @Valid @RequestBody @Parameter(description = "扫码签到信息", required = true) CheckInByTokenDTO checkInByTokenDTO) {

        log.info("扫码签到请求: 手机号{}", checkInByTokenDTO.getStudentPhone());
        RegistrationVO registrationVO = registrationService.checkInByToken(checkInByTokenDTO);
        log.info("扫码签到成功: 报名ID {}", registrationVO.getId());

        return Result.ok(registrationVO);
    }

    @GetMapping("/{activityId}/qrcode")
    @Operation(summary = "生成签到二维码", description = "管理员生成活动签到二维码(30分钟有效期)")
    public Result<CheckInQRCodeVO> generateCheckInQRCode(
            @Parameter(description = "活动ID", required = true) @PathVariable Long activityId,
            @Parameter(description = "网站基础URL", example = "https://example.com") @RequestParam(required = false) String baseUrl) {

        // 如果没有提供baseUrl，使用默认值
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = "http://localhost:8080"; // 默认开发环境URL
        }

        log.info("生成签到二维码请求: 活动ID={}", activityId);
        CheckInQRCodeVO qrCodeVO = registrationService.generateCheckInQRCode(activityId, baseUrl);
        log.info("签到二维码生成成功: 活动ID={}", activityId);

        return Result.ok(qrCodeVO);
    }

    @PostMapping("/batch-checkin")
    @Operation(summary = "批量签到", description = "管理员批量为学生签到")
    public Result<RegistrationService.BatchCheckInResult> batchCheckIn(
            @Parameter(description = "活动ID", required = true) @RequestParam Long activityId,
            @Parameter(description = "手机号列表", required = true) @RequestParam List<String> phoneNumbers) {

        log.info("批量签到请求: 活动{}, 手机号数量{}", activityId, phoneNumbers.size());
        RegistrationService.BatchCheckInResult result = registrationService.batchCheckIn(activityId, phoneNumbers);
        log.info("批量签到完成: 成功{}, 失败{}", result.getSuccessCount(), result.getFailCount());

        return Result.ok(result);
    }

    @GetMapping("/list")
    @Operation(summary = "查询报名列表", description = "分页查询报名列表，支持筛选")
    public Result<PageInfo<RegistrationVO>> getRegistrationList(
            @Parameter(description = "查询条件") RegistrationQueryDTO queryDTO) {

        log.info("查询报名列表请求: 页码{}, 页大小{}", queryDTO.getPageNum(), queryDTO.getPageSize());
        PageInfo<RegistrationVO> pageInfo = registrationService.getRegistrationList(queryDTO);
        log.info("报名列表查询成功: 总数{}", pageInfo.getTotal());

        return Result.ok(pageInfo);
    }

    @GetMapping("/{registrationId}")
    @Operation(summary = "获取报名详情", description = "根据报名ID获取详细信息")
    public Result<RegistrationVO> getRegistrationDetail(
            @Parameter(description = "报名ID", required = true) @PathVariable Long registrationId) {

        log.info("获取报名详情请求: ID {}", registrationId);
        RegistrationVO registrationVO = registrationService.getRegistrationDetail(registrationId);
        log.info("报名详情查询成功: ID {}", registrationId);

        return Result.ok(registrationVO);
    }

    @GetMapping("/statistics/{activityId}")
    @Operation(summary = "获取报名统计", description = "获取活动的报名统计信息")
    public Result<RegistrationStatisticsVO> getRegistrationStatistics(
            @Parameter(description = "活动ID", required = true) @PathVariable Long activityId) {

        log.info("获取报名统计请求: 活动ID {}", activityId);
        RegistrationStatisticsVO statistics = registrationService.getRegistrationStatistics(activityId);
        log.info("报名统计查询成功: 活动{}, 总报名人数{}", activityId, statistics.getTotalRegistrations());

        return Result.ok(statistics);
    }
}