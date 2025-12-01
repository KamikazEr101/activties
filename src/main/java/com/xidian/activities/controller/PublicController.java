package com.xidian.activities.controller;

import com.github.pagehelper.PageInfo;
import com.xidian.activities.common.result.Result;
import com.xidian.activities.dto.ActivityQueryDTO;
import com.xidian.activities.service.ActivityService;
import com.xidian.activities.service.ActivityTypeService;
import com.xidian.activities.vo.ActivityListVO;
import com.xidian.activities.vo.ActivityTypeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公共控制器 - 无需认证的接口
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
@Slf4j
@RestController
@RequestMapping("/public")
@Tag(name = "公共接口", description = "无需认证的公开接口")
public class PublicController {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ActivityTypeService activityTypeService;

    @GetMapping("/activities")
    @Operation(summary = "公开活动列表", description = "获取已发布的活动列表，学生可查看")
    public Result<PageInfo<ActivityListVO>> getPublicActivities(
            @Parameter(description = "查询条件") ActivityQueryDTO queryDTO) {

        // 只查询已发布且未取消的活动
        if (queryDTO.getActivityStatus() == null) {
            queryDTO.setActivityStatus(1); // 默认只查询报名中的活动
        } else {
            // 安全检查：不允许查询未发布(0)或已取消(5)的活动
            Integer status = queryDTO.getActivityStatus();
            if (status == 0 || status == 5) {
                throw new com.xidian.activities.common.exception.BizException(
                        com.xidian.activities.common.result.ResultCodeEnum.PARAM_ERROR,
                        "无法查询未发布或已取消的活动");
            }
        }

        log.info("公共查询活动列表: 页码{}, 页大小{}", queryDTO.getPageNum(), queryDTO.getPageSize());
        PageInfo<ActivityListVO> pageInfo = activityService.getActivityList(queryDTO);
        log.info("公共活动列表查询成功: 总数{}", pageInfo.getTotal());

        return Result.ok(pageInfo);
    }

    @GetMapping("/activities/{activityId}")
    @Operation(summary = "公开活动详情", description = "获取活动详情信息（学生端，仅限已发布状态）")
    public Result<com.xidian.activities.vo.ActivityDetailVO> getPublicActivityDetail(
            @Parameter(description = "活动ID", required = true) @PathVariable Long activityId) {

        log.info("公共获取活动详情: ID {}", activityId);
        com.xidian.activities.vo.ActivityDetailVO activityVO = activityService.getActivityDetail(activityId);

        // 学生端权限限制：只能查看已发布的活动（状态1-4，不包括0未发布和5已取消）
        // 这与管理员端（ActivityController）不同，管理员可以查看所有状态的活动
        if (activityVO.getActivityStatus() == 0) {
            throw new com.xidian.activities.common.exception.BizException(
                    com.xidian.activities.common.result.ResultCodeEnum.ACTIVITY_NOT_FOUND,
                    "您访问的活动不存在或已下架");
        }
        if (activityVO.getActivityStatus() == 5) {
            throw new com.xidian.activities.common.exception.BizException(
                    com.xidian.activities.common.result.ResultCodeEnum.ACTIVITY_NOT_FOUND,
                    "该活动已取消");
        }

        // 记录访问统计（异步操作，不影响主流程）
        activityService.incrementViewCount(activityId);

        log.info("公共活动详情查询成功: {}", activityVO.getActivityName());

        return Result.ok(activityVO);
    }

    @GetMapping("/activity-types")
    @Operation(summary = "获取活动类型", description = "获取所有启用的活动类型")
    public Result<List<ActivityTypeVO>> getActivityTypes() {
        log.info("获取活动类型列表");
        List<ActivityTypeVO> activityTypes = activityTypeService.getAllEnabledTypes();
        log.info("活动类型列表查询成功: 数量{}", activityTypes.size());

        return Result.ok(activityTypes);
    }
}