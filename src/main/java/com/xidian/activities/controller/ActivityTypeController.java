package com.xidian.activities.controller;

import com.github.pagehelper.PageInfo;
import com.xidian.activities.common.result.Result;
import com.xidian.activities.dto.*;
import com.xidian.activities.service.ActivityTypeService;
import com.xidian.activities.vo.ActivityTypeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 活动类型管理控制器
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
@Slf4j
@RestController
@RequestMapping("/activity-types")
@Tag(name = "活动类型管理", description = "活动类型的增删改查接口")
public class ActivityTypeController {

    @Autowired
    private ActivityTypeService activityTypeService;

    @PostMapping("/create")
    @Operation(summary = "创建活动类型", description = "管理员创建新的活动类型")
    public Result<ActivityTypeVO> createActivityType(
            @Valid @RequestBody @Parameter(description = "活动类型信息", required = true) ActivityTypeCreateDTO createDTO) {

        log.info("创建活动类型请求: 类型编码 {}, 类型名称 {}", createDTO.getTypeCode(), createDTO.getTypeName());
        ActivityTypeVO activityTypeVO = activityTypeService.createActivityType(createDTO);
        log.info("活动类型创建成功: ID {}, 编码 {}", activityTypeVO.getId(), createDTO.getTypeCode());

        return Result.ok(activityTypeVO);
    }

    @PutMapping("/{typeId}")
    @Operation(summary = "修改活动类型", description = "修改活动类型信息")
    public Result<ActivityTypeVO> updateActivityType(
            @Parameter(description = "活动类型ID", required = true) @PathVariable Long typeId,
            @Valid @RequestBody @Parameter(description = "活动类型信息", required = true) ActivityTypeUpdateDTO updateDTO) {

        log.info("修改活动类型请求: ID {}", typeId);
        ActivityTypeVO activityTypeVO = activityTypeService.updateActivityType(typeId, updateDTO);
        log.info("活动类型修改成功: ID {}, 名称 {}", typeId, activityTypeVO.getTypeName());

        return Result.ok(activityTypeVO);
    }

    @DeleteMapping("/{typeId}")
    @Operation(summary = "删除活动类型", description = "删除活动类型（软删除）")
    public Result<Void> deleteActivityType(
            @Parameter(description = "活动类型ID", required = true) @PathVariable Long typeId) {

        log.info("删除活动类型请求: ID {}", typeId);
        activityTypeService.deleteActivityType(typeId);
        log.info("活动类型删除成功: ID {}", typeId);

        return Result.ok();
    }

    @GetMapping("/{typeId}")
    @Operation(summary = "获取活动类型详情", description = "根据ID获取活动类型详细信息")
    public Result<ActivityTypeVO> getActivityTypeDetail(
            @Parameter(description = "活动类型ID", required = true) @PathVariable Long typeId) {

        log.info("获取活动类型详情请求: ID {}", typeId);
        ActivityTypeVO activityTypeVO = activityTypeService.getActivityTypeDetail(typeId);
        log.info("活动类型详情查询成功: ID {}", typeId);

        return Result.ok(activityTypeVO);
    }

    @GetMapping("/list")
    @Operation(summary = "查询活动类型列表", description = "分页查询活动类型列表，支持筛选")
    public Result<PageInfo<ActivityTypeVO>> getActivityTypeList(
            @Parameter(description = "查询条件") ActivityTypeQueryDTO queryDTO) {

        log.info("查询活动类型列表请求: 页码{}, 页大小{}", queryDTO.getPageNum(), queryDTO.getPageSize());
        PageInfo<ActivityTypeVO> pageInfo = activityTypeService.getActivityTypeList(queryDTO);
        log.info("活动类型列表查询成功: 总数{}", pageInfo.getTotal());

        return Result.ok(pageInfo);
    }

    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除活动类型", description = "批量删除活动类型")
    public Result<Integer> batchDeleteActivityTypes(
            @Parameter(description = "活动类型ID列表", required = true) @RequestBody List<Long> typeIds) {

        log.info("批量删除活动类型请求: 数量{}", typeIds.size());
        Integer count = activityTypeService.batchDeleteActivityTypes(typeIds);
        log.info("批量删除活动类型成功: 数量{}", count);

        return Result.ok(count);
    }

    @GetMapping("/check/type-code")
    @Operation(summary = "检查类型编码是否存在", description = "检查类型编码是否已存在")
    public Result<Boolean> checkTypeCodeExists(
            @Parameter(description = "类型编码", required = true) @RequestParam String typeCode,
            @Parameter(description = "排除的类型ID（用于修改时检查）") @RequestParam(required = false) Long excludeId) {

        log.info("检查类型编码请求: 编码{}, 排除ID{}", typeCode, excludeId);
        boolean exists = activityTypeService.checkTypeCodeExists(typeCode, excludeId);
        log.info("类型编码检查结果: 编码{}, 存在{}", typeCode, exists);

        return Result.ok(exists);
    }

    @GetMapping("/check/type-name")
    @Operation(summary = "检查类型名称是否存在", description = "检查类型名称是否已存在")
    public Result<Boolean> checkTypeNameExists(
            @Parameter(description = "类型名称", required = true) @RequestParam String typeName,
            @Parameter(description = "排除的类型ID（用于修改时检查）") @RequestParam(required = false) Long excludeId) {

        log.info("检查类型名称请求: 名称{}, 排除ID{}", typeName, excludeId);
        boolean exists = activityTypeService.checkTypeNameExists(typeName, excludeId);
        log.info("类型名称检查结果: 名称{}, 存在{}", typeName, exists);

        return Result.ok(exists);
    }

}