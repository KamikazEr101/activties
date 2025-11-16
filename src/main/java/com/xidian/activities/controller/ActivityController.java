package com.xidian.activities.controller;

import com.github.pagehelper.PageInfo;
import com.xidian.activities.common.result.Result;
import com.xidian.activities.dto.*;
import com.xidian.activities.service.ActivityService;
import com.xidian.activities.vo.ActivityDetailVO;
import com.xidian.activities.vo.ActivityListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 活动控制器
 *
 * @author
 * @since
 */
@Slf4j
@RestController
@RequestMapping("/activity")
@Tag(name = "活动管理", description = "活动创建、查询、更新、删除相关接口")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @PostMapping("/create")
    @Operation(summary = "创建活动", description = "管理员创建新的活动")
    public Result<ActivityDetailVO> createActivity(
            @Valid @RequestBody @Parameter(description = "活动创建信息", required = true) ActivityCreateDTO createDTO) {

        log.info("创建活动请求: {}", createDTO.getActivityName());
        ActivityDetailVO activityVO = activityService.createActivity(createDTO);
        log.info("活动创建成功: {}", activityVO.getActivityName());

        return Result.ok(activityVO);
    }

    @PutMapping("/update")
    @Operation(summary = "更新活动", description = "更新活动信息")
    public Result<ActivityDetailVO> updateActivity(
            @Valid @RequestBody @Parameter(description = "活动更新信息", required = true) ActivityUpdateDTO updateDTO) {

        log.info("更新活动请求: ID {}", updateDTO.getId());
        ActivityDetailVO activityVO = activityService.updateActivity(updateDTO);
        log.info("活动更新成功: {}", activityVO.getActivityName());

        return Result.ok(activityVO);
    }

    @DeleteMapping("/{activityId}")
    @Operation(summary = "删除活动", description = "删除指定活动")
    public Result<Void> deleteActivity(
            @Parameter(description = "活动ID", required = true) @PathVariable Long activityId) {

        log.info("删除活动请求: ID {}", activityId);
        activityService.deleteActivity(activityId);
        log.info("活动删除成功: ID {}", activityId);

        return Result.ok();
    }

    @PutMapping("/status")
    @Operation(summary = "更新活动状态", description = "修改活动状态")
    public Result<ActivityDetailVO> updateActivityStatus(
            @Valid @RequestBody @Parameter(description = "活动状态更新信息", required = true) ActivityStatusUpdateDTO statusUpdateDTO) {

        log.info("更新活动状态请求: ID {} -> {}", statusUpdateDTO.getActivityId(), statusUpdateDTO.getActivityStatus());
        ActivityDetailVO activityVO = activityService.updateActivityStatus(statusUpdateDTO);
        log.info("活动状态更新成功: {}", activityVO.getActivityName());

        return Result.ok(activityVO);
    }

    @PostMapping("/publish/{activityId}")
    @Operation(summary = "发布活动", description = "发布活动，状态改为报名中")
    public Result<ActivityDetailVO> publishActivity(
            @Parameter(description = "活动ID", required = true) @PathVariable Long activityId) {

        log.info("发布活动请求: ID {}", activityId);
        ActivityDetailVO activityVO = activityService.publishActivity(activityId);
        log.info("活动发布成功: {}", activityVO.getActivityName());

        return Result.ok(activityVO);
    }

    @PostMapping("/cancel/{activityId}")
    @Operation(summary = "取消活动", description = "取消活动，状态改为已取消")
    public Result<ActivityDetailVO> cancelActivity(
            @Parameter(description = "活动ID", required = true) @PathVariable Long activityId) {

        log.info("取消活动请求: ID {}", activityId);
        ActivityDetailVO activityVO = activityService.cancelActivity(activityId);
        log.info("活动取消成功: {}", activityVO.getActivityName());

        return Result.ok(activityVO);
    }

    @GetMapping("/list")
    @Operation(summary = "查询活动列表", description = "分页查询活动列表，支持筛选和排序")
    public Result<PageInfo<ActivityListVO>> getActivityList(
            @Parameter(description = "查询条件") ActivityQueryDTO queryDTO) {

        log.info("查询活动列表请求: 页码{}, 页大小{}", queryDTO.getPageNum(), queryDTO.getPageSize());
        PageInfo<ActivityListVO> pageInfo = activityService.getActivityList(queryDTO);
        log.info("活动列表查询成功: 总数{}", pageInfo.getTotal());

        return Result.ok(pageInfo);
    }

    @GetMapping("/{activityId}")
    @Operation(summary = "获取活动详情", description = "根据活动ID获取详细信息（管理员端，无状态限制）")
    public Result<ActivityDetailVO> getActivityDetail(
            @Parameter(description = "活动ID", required = true) @PathVariable Long activityId) {

        log.info("获取活动详情请求: ID {}", activityId);
        // 管理员端：可以查看所有状态的活动（包括未发布、已取消）
        // 与PublicController不同，这里不进行状态限制，方便管理员管理活动
        ActivityDetailVO activityVO = activityService.getActivityDetail(activityId);
        log.info("活动详情查询成功: {}", activityVO.getActivityName());

        return Result.ok(activityVO);
    }
}
