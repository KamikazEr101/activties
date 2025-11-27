package com.xidian.activities.task;

import com.xidian.activities.common.enums.ActivityStatusEnum;
import com.xidian.activities.entity.Activity;
import com.xidian.activities.mapper.ActivityMapper;
import com.xidian.activities.service.RedisService;
import com.xidian.activities.constant.CacheConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 活动状态自动更新定时任务
 * 每分钟执行一次，自动更新活动状态
 *
 * @author
 * @since
 */
@Slf4j
@Component
public class ActivityStatusTask {

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private RedisService redisService;

    /**
     * 定时更新活动状态
     * 每分钟执行一次：检查并更新需要状态流转的活动
     * 
     * 状态流转规则：
     * 1. 报名中(1) → 报名结束(2)：当前时间 > 报名截止时间
     * 2. 报名结束(2) → 进行中(3)：当前时间 >= 活动开始时间
     * 3. 进行中(3) → 已结束(4)：当前时间 > 活动结束时间
     */
    @Scheduled(cron = "0 * * * * ?") // 每分钟的第0秒执行
    public void updateActivityStatus() {
        try {
            LocalDateTime now = LocalDateTime.now();
            int updatedCount = 0;

            // 1. 报名中 → 报名结束：报名截止时间已过
            List<Activity> registrationEndActivities = activityMapper
                    .findActivitiesByStatus(ActivityStatusEnum.REGISTERING.getCode());
            for (Activity activity : registrationEndActivities) {
                if (activity.getRegistrationEndTime() != null && now.isAfter(activity.getRegistrationEndTime())) {
                    activity.setActivityStatus(ActivityStatusEnum.REGISTRATION_ENDED.getCode());
                    activity.setUpdateTime(now);
                    activityMapper.updateById(activity);
                    clearActivityCache(activity.getId());
                    updatedCount++;
                    log.info("活动状态自动更新：活动[{}]从报名中→报名结束", activity.getActivityName());
                }
            }

            // 2. 报名结束 → 进行中：活动开始时间已到
            List<Activity> startActivities = activityMapper
                    .findActivitiesByStatus(ActivityStatusEnum.REGISTRATION_ENDED.getCode());
            for (Activity activity : startActivities) {
                if (activity.getStartTime() != null && !now.isBefore(activity.getStartTime())) {
                    activity.setActivityStatus(ActivityStatusEnum.IN_PROGRESS.getCode());
                    activity.setUpdateTime(now);
                    activityMapper.updateById(activity);
                    clearActivityCache(activity.getId());
                    updatedCount++;
                    log.info("活动状态自动更新：活动[{}]从报名结束→进行中", activity.getActivityName());
                }
            }

            // 3. 进行中 → 已结束：活动结束时间已过
            List<Activity> endActivities = activityMapper
                    .findActivitiesByStatus(ActivityStatusEnum.IN_PROGRESS.getCode());
            for (Activity activity : endActivities) {
                if (activity.getEndTime() != null && now.isAfter(activity.getEndTime())) {
                    activity.setActivityStatus(ActivityStatusEnum.ENDED.getCode());
                    activity.setUpdateTime(now);
                    activityMapper.updateById(activity);
                    clearActivityCache(activity.getId());
                    updatedCount++;
                    log.info("活动状态自动更新：活动[{}]从进行中→已结束", activity.getActivityName());
                }
            }

            if (updatedCount > 0) {
                log.info("活动状态定时任务执行完成，共更新{}个活动状态", updatedCount);
            }

        } catch (Exception e) {
            log.error("活动状态定时任务执行失败", e);
        }
    }

    /**
     * 每天凌晨1点执行：清理过期活动的缓存数据
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void cleanExpiredActivityCache() {
        try {
            log.info("开始清理过期活动缓存...");
            int clearedCount = 0;

            // 获取已结束的活动
            List<Activity> endedActivities = activityMapper.findActivitiesByStatus(ActivityStatusEnum.ENDED.getCode());
            // 获取已取消的活动
            List<Activity> cancelledActivities = activityMapper
                    .findActivitiesByStatus(ActivityStatusEnum.CANCELLED.getCode());

            List<Activity> allExpiredActivities = new ArrayList<>();
            allExpiredActivities.addAll(endedActivities);
            allExpiredActivities.addAll(cancelledActivities);

            for (Activity activity : allExpiredActivities) {
                // 检查缓存是否存在，如果存在则删除
                String cacheKey = CacheConstants.ACTIVITY_CACHE + activity.getId();
                if (redisService.hasKey(cacheKey)) {
                    clearActivityCache(activity.getId());
                    clearedCount++;
                }
            }

            log.info("过期活动缓存清理完成，共清理{}个活动的缓存", clearedCount);
        } catch (Exception e) {
            log.error("清理过期活动缓存失败", e);
        }
    }

    /**
     * 每小时执行一次：统计活动数据并更新报名人数缓存
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void updateActivityStatistics() {
        try {
            log.info("开始更新活动统计数据...");
            // 更新进行中和报名中的活动统计信息
            List<Activity> activeActivities = activityMapper.findActivitiesByStatus(1);
            activeActivities.addAll(activityMapper.findActivitiesByStatus(3));

            for (Activity activity : activeActivities) {
                int registrationCount = activityMapper.countRegistrations(activity.getId());
                log.debug("活动[{}]当前报名人数：{}", activity.getActivityName(), registrationCount);
            }

            log.info("活动统计数据更新完成");
        } catch (Exception e) {
            log.error("更新活动统计数据失败", e);
        }
    }

    /**
     * 清理活动相关缓存
     * 与ActivityServiceImpl中的缓存清理逻辑保持一致
     */
    private void clearActivityCache(Long activityId) {
        try {
            String detailCacheKey = CacheConstants.ACTIVITY_CACHE + activityId;
            redisService.delete(detailCacheKey);
            log.debug("定时任务清理活动缓存: activityId = {}", activityId);
        } catch (Exception e) {
            log.error("定时任务清理活动缓存失败: activityId = {}, error = {}", activityId, e.getMessage());
        }
    }
}
