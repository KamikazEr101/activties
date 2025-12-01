package com.xidian.activities.mapper;

import com.xidian.activities.entity.ActivityType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 活动类型Mapper接口
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
@Mapper
public interface ActivityTypeMapper {

    /**
     * 插入活动类型
     *
     * @param activityType 活动类型信息
     * @return 影响行数
     */
    int insert(ActivityType activityType);

    /**
     * 更新活动类型
     *
     * @param activityType 活动类型信息
     * @return 影响行数
     */
    int updateById(ActivityType activityType);

    /**
     * 删除活动类型
     *
     * @param id 类型ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据ID查询活动类型
     *
     * @param id 类型ID
     * @return 活动类型信息
     */
    ActivityType selectById(@Param("id") Long id);

    /**
     * 分页查询活动类型
     *
     * @param activityType 查询条件
     * @return 活动类型列表
     */
    List<ActivityType> selectList(ActivityType activityType);

    /**
     * 查询所有活动类型
     *
     * @return 活动类型列表
     */
    List<ActivityType> selectAll();

    /**
     * 根据类型编码查询活动类型
     *
     * @param typeCode 类型编码
     * @return 活动类型信息
     */
    ActivityType selectByTypeCode(@Param("typeCode") String typeCode);

    /**
     * 检查类型名称是否存在
     *
     * @param typeName  类型名称
     * @param excludeId 排除的ID
     * @return 数量
     */
    int checkTypeNameExists(@Param("typeName") String typeName, @Param("excludeId") Long excludeId);

    /**
     * 检查类型编码是否存在
     *
     * @param typeCode  类型编码
     * @param excludeId 排除的ID
     * @return 数量
     */
    int checkTypeCodeExists(@Param("typeCode") String typeCode, @Param("excludeId") Long excludeId);

    /**
     * 检查活动类型是否被活动使用
     *
     * @param typeId 类型ID
     * @return 数量
     */
    int checkIsUsedByActivities(@Param("typeId") Long typeId);

    /**
     * 批量删除活动类型
     *
     * @param ids 类型ID列表
     * @return 影响行数
     */
    int batchDelete(@Param("ids") List<Long> ids);
}