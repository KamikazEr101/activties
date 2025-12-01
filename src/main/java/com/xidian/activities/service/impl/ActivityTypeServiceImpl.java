package com.xidian.activities.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xidian.activities.common.exception.BizException;
import com.xidian.activities.common.result.ResultCodeEnum;
import com.xidian.activities.dto.ActivityTypeCreateDTO;
import com.xidian.activities.dto.ActivityTypeQueryDTO;
import com.xidian.activities.dto.ActivityTypeUpdateDTO;
import com.xidian.activities.entity.ActivityType;
import com.xidian.activities.mapper.ActivityTypeMapper;
import com.xidian.activities.service.ActivityTypeService;
import com.xidian.activities.util.CommonUtils;
import com.xidian.activities.vo.ActivityTypeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 活动类型服务实现类
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
@Slf4j
@Service
public class ActivityTypeServiceImpl implements ActivityTypeService {

    @Autowired
    private ActivityTypeMapper activityTypeMapper;

    @Override
    @Transactional
    public ActivityTypeVO createActivityType(ActivityTypeCreateDTO createDTO) {
        // 检查类型编码是否已存在
        if (activityTypeMapper.checkTypeCodeExists(createDTO.getTypeCode(), null) > 0) {
            throw BizException.of(ResultCodeEnum.DATA_ERROR, "类型编码已存在");
        }

        // 检查类型名称是否已存在
        if (activityTypeMapper.checkTypeNameExists(createDTO.getTypeName(), null) > 0) {
            throw BizException.of(ResultCodeEnum.DATA_ERROR, "类型名称已存在");
        }

        // 创建实体对象
        ActivityType activityType = new ActivityType();
        BeanUtils.copyProperties(createDTO, activityType);
        activityType.setIsDeleted(0);
        activityType.setCreateTime(LocalDateTime.now());
        activityType.setUpdateTime(LocalDateTime.now());

        // 插入数据库
        activityTypeMapper.insert(activityType);

        log.info("活动类型创建成功: ID {}, 编码 {}, 名称 {}",
                activityType.getId(), createDTO.getTypeCode(), createDTO.getTypeName());

        return convertToVO(activityType);
    }

    @Override
    @Transactional
    public ActivityTypeVO updateActivityType(Long typeId, ActivityTypeUpdateDTO updateDTO) {
        // 检查活动类型是否存在
        ActivityType existingType = activityTypeMapper.selectById(typeId);
        if (existingType == null || existingType.getIsDeleted() == 1) {
            throw BizException.of(ResultCodeEnum.DATA_ERROR, "活动类型不存在");
        }

        // 检查类型编码是否已存在（排除自身）
        if (!existingType.getTypeCode().equals(updateDTO.getTypeCode()) &&
                activityTypeMapper.checkTypeCodeExists(updateDTO.getTypeCode(), typeId) > 0) {
            throw BizException.of(ResultCodeEnum.DATA_ERROR, "类型编码已存在");
        }

        // 检查类型名称是否已存在（排除自身）
        if (!existingType.getTypeName().equals(updateDTO.getTypeName()) &&
                activityTypeMapper.checkTypeNameExists(updateDTO.getTypeName(), typeId) > 0) {
            throw BizException.of(ResultCodeEnum.DATA_ERROR, "类型名称已存在");
        }

        // 检查是否被活动使用（如果更改编码需要特别处理）
        if (!existingType.getTypeCode().equals(updateDTO.getTypeCode()) &&
                activityTypeMapper.checkIsUsedByActivities(typeId) > 0) {
            throw BizException.of(ResultCodeEnum.DATA_ERROR, "该类型已被活动使用，不能修改编码");
        }

        // 更新实体对象
        BeanUtils.copyProperties(updateDTO, existingType);
        existingType.setUpdateTime(LocalDateTime.now());

        // 更新数据库
        activityTypeMapper.updateById(existingType);

        log.info("活动类型更新成功: ID {}, 编码 {}, 名称 {}",
                typeId, updateDTO.getTypeCode(), updateDTO.getTypeName());

        return convertToVO(existingType);
    }

    @Override
    @Transactional
    public void deleteActivityType(Long typeId) {
        // 检查活动类型是否存在
        ActivityType existingType = activityTypeMapper.selectById(typeId);
        if (existingType == null || existingType.getIsDeleted() == 1) {
            throw BizException.of(ResultCodeEnum.DATA_ERROR, "活动类型不存在");
        }

        // 检查是否被活动使用
        if (activityTypeMapper.checkIsUsedByActivities(typeId) > 0) {
            throw BizException.of(ResultCodeEnum.DATA_ERROR, "该类型已被活动使用，不能删除");
        }

        // 软删除
        activityTypeMapper.deleteById(typeId);

        log.info("活动类型删除成功: ID {}, 编码 {}", typeId, existingType.getTypeCode());
    }

    @Override
    public ActivityTypeVO getActivityTypeDetail(Long typeId) {
        ActivityType activityType = activityTypeMapper.selectById(typeId);
        if (activityType == null || activityType.getIsDeleted() == 1) {
            throw BizException.of(ResultCodeEnum.DATA_ERROR, "活动类型不存在");
        }
        return convertToVO(activityType);
    }

    @Override
    public PageInfo<ActivityTypeVO> getActivityTypeList(ActivityTypeQueryDTO queryDTO) {
        // 设置分页
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 构建查询条件
        ActivityType queryCondition = new ActivityType();
        if (CommonUtils.isNotBlank(queryDTO.getTypeName())) {
            queryCondition.setTypeName(queryDTO.getTypeName());
        }
        if (CommonUtils.isNotBlank(queryDTO.getTypeCode())) {
            queryCondition.setTypeCode(queryDTO.getTypeCode());
        }

        // 查询数据
        List<ActivityType> activityTypes = activityTypeMapper.selectList(queryCondition);
        PageInfo<ActivityType> pageInfo = new PageInfo<>(activityTypes);

        // 转换为VO
        List<ActivityTypeVO> voList = pageInfo.getList().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 构建返回结果
        PageInfo<ActivityTypeVO> result = new PageInfo<>();
        BeanUtils.copyProperties(pageInfo, result);
        result.setList(voList);

        return result;
    }

    @Override
    @Transactional
    public Integer batchDeleteActivityTypes(List<Long> typeIds) {
        if (typeIds == null || typeIds.isEmpty()) {
            return 0;
        }

        // 检查每个类型是否可以被删除
        for (Long typeId : typeIds) {
            ActivityType activityType = activityTypeMapper.selectById(typeId);
            if (activityType != null && activityType.getIsDeleted() == 0) {
                if (activityTypeMapper.checkIsUsedByActivities(typeId) > 0) {
                    throw BizException.of(ResultCodeEnum.DATA_ERROR,
                            "类型 '" + activityType.getTypeName() + "' 已被活动使用，不能删除");
                }
            }
        }

        // 批量删除
        return activityTypeMapper.batchDelete(typeIds);
    }

    @Override
    public boolean checkTypeCodeExists(String typeCode, Long excludeId) {
        if (CommonUtils.isBlank(typeCode)) {
            return false;
        }
        return activityTypeMapper.checkTypeCodeExists(typeCode, excludeId) > 0;
    }

    @Override
    public boolean checkTypeNameExists(String typeName, Long excludeId) {
        if (CommonUtils.isBlank(typeName)) {
            return false;
        }
        return activityTypeMapper.checkTypeNameExists(typeName, excludeId) > 0;
    }

    // === 原有方法的实现 ===

    @Override
    public List<ActivityTypeVO> getAllEnabledTypes() {
        List<ActivityType> activityTypes = activityTypeMapper.selectAll();
        return activityTypes.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public ActivityTypeVO getByTypeCode(String typeCode) {
        ActivityType activityType = activityTypeMapper.selectByTypeCode(typeCode);
        return activityType != null ? convertToVO(activityType) : null;
    }

    @Override
    public ActivityTypeVO getById(Integer id) {
        ActivityType activityType = activityTypeMapper.selectById(id.longValue());
        return activityType != null ? convertToVO(activityType) : null;
    }

    /**
     * 转换为VO
     */
    private ActivityTypeVO convertToVO(ActivityType activityType) {
        ActivityTypeVO vo = new ActivityTypeVO();
        BeanUtils.copyProperties(activityType, vo);
        return vo;
    }
}