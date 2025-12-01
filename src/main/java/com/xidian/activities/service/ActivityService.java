package com.xidian.activities.service;

import com.github.pagehelper.PageInfo;
import com.xidian.activities.dto.*;
import com.xidian.activities.vo.*;

/**
 * 活动服务接口
 * 注：使用手动缓存管理（RedisService），不使用Spring Cache注解
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
public interface ActivityService {

    /**
     * 创建活动
     *
     * @param createDTO 创建请求
     * @return 活动详情
     */
    ActivityDetailVO createActivity(ActivityCreateDTO createDTO);

    /**
     * 更新活动
     *
     * @param updateDTO 更新请求
     * @return 活动详情
     */
    ActivityDetailVO updateActivity(ActivityUpdateDTO updateDTO);

    /**
     * 删除活动（软删除）
     *
     * @param activityId 活动ID
     */
    void deleteActivity(Long activityId);

    /**
     * 更新活动状态
     *
     * @param statusUpdateDTO 状态更新请求
     * @return 活动详情
     */
    ActivityDetailVO updateActivityStatus(ActivityStatusUpdateDTO statusUpdateDTO);

    /**
     * 分页查询活动列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageInfo<ActivityListVO> getActivityList(ActivityQueryDTO queryDTO);

    /**
     * 获取活动详情
     *
     * @param activityId 活动ID
     * @return 活动详情
     */
    ActivityDetailVO getActivityDetail(Long activityId);

    /**
     * 发布活动
     *
     * @param activityId 活动ID
     * @return 活动详情
     */
    ActivityDetailVO publishActivity(Long activityId);

    /**
     * 取消活动
     *
     * @param activityId 活动ID
     * @return 活动详情
     */
    ActivityDetailVO cancelActivity(Long activityId);

    /**
     * 增加活动访问次数
     *
     * @param activityId 活动ID
     */
    void incrementViewCount(Long activityId);
}