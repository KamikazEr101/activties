package com.xidian.activities.mapper;

import com.xidian.activities.dto.ActivityQueryDTO;
import com.xidian.activities.entity.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 活动Mapper接口
 *
 * @author
 * @since
 */
@Mapper
public interface ActivityMapper {

    /**
     * 插入活动
     *
     * @param activity 活动信息
     * @return 影响行数
     */
    int insert(Activity activity);

    /**
     * 根据ID查询活动
     *
     * @param id 活动ID
     * @return 活动信息
     */
    Activity selectById(@Param("id") Long id);

    /**
     * 根据ID查询活动并加锁（悲观锁）
     *
     * @param id 活动ID
     * @return 活动信息
     */
    Activity selectByIdForUpdate(@Param("id") Long id);

    /**
     * 根据条件查询活动列表
     *
     * @param queryDTO 查询条件
     * @return 活动列表
     */
    List<Activity> selectList(ActivityQueryDTO queryDTO);

    /**
     * 更新活动
     *
     * @param activity 活动信息
     * @return 影响行数
     */
    int updateById(Activity activity);

    /**
     * 统计活动报名人数
     *
     * @param activityId 活动ID
     * @return 报名人数
     */
    int countRegistrations(@Param("activityId") Long activityId);

    /**
     * 根据状态查询活动列表（用于定时任务）
     *
     * @param activityStatus 活动状态
     * @return 活动列表
     */
    List<Activity> findActivitiesByStatus(@Param("activityStatus") Integer activityStatus);
}