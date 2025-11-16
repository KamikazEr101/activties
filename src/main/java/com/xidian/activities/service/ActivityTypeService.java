package com.xidian.activities.service;

import com.github.pagehelper.PageInfo;
import com.xidian.activities.dto.ActivityTypeCreateDTO;
import com.xidian.activities.dto.ActivityTypeQueryDTO;
import com.xidian.activities.dto.ActivityTypeUpdateDTO;
import com.xidian.activities.vo.ActivityTypeVO;

import java.util.List;

/**
 * 活动类型服务接口
 * 注：使用手动缓存管理（RedisService），不使用Spring Cache注解
 *
 * @author
 * @since
 */
public interface ActivityTypeService {

    /**
     * 创建活动类型
     *
     * @param createDTO 创建DTO
     * @return 活动类型VO
     */
    ActivityTypeVO createActivityType(ActivityTypeCreateDTO createDTO);

    /**
     * 更新活动类型
     *
     * @param typeId    活动类型ID
     * @param updateDTO 更新DTO
     * @return 活动类型VO
     */
    ActivityTypeVO updateActivityType(Long typeId, ActivityTypeUpdateDTO updateDTO);

    /**
     * 删除活动类型（软删除）
     *
     * @param typeId 活动类型ID
     */
    void deleteActivityType(Long typeId);

    /**
     * 获取活动类型详情
     *
     * @param typeId 活动类型ID
     * @return 活动类型VO
     */
    ActivityTypeVO getActivityTypeDetail(Long typeId);

    /**
     * 分页查询活动类型列表
     *
     * @param queryDTO 查询DTO
     * @return 分页结果
     */
    PageInfo<ActivityTypeVO> getActivityTypeList(ActivityTypeQueryDTO queryDTO);

    /**
     * 批量删除活动类型
     *
     * @param typeIds 活动类型ID列表
     * @return 删除数量
     */
    Integer batchDeleteActivityTypes(List<Long> typeIds);

    /**
     * 检查类型编码是否存在
     *
     * @param typeCode  类型编码
     * @param excludeId 排除的ID（修改时使用）
     * @return 是否存在
     */
    boolean checkTypeCodeExists(String typeCode, Long excludeId);

    /**
     * 检查类型名称是否存在
     *
     * @param typeName  类型名称
     * @param excludeId 排除的ID（修改时使用）
     * @return 是否存在
     */
    boolean checkTypeNameExists(String typeName, Long excludeId);

    /**
     * 获取所有启用的活动类型
     *
     * @return 活动类型列表
     */
    List<ActivityTypeVO> getAllEnabledTypes();

    /**
     * 根据类型编码获取活动类型
     *
     * @param typeCode 类型编码
     * @return 活动类型信息
     */
    ActivityTypeVO getByTypeCode(String typeCode);

    /**
     * 根据ID获取活动类型
     *
     * @param id 类型ID
     * @return 活动类型信息
     */
    ActivityTypeVO getById(Integer id);
}