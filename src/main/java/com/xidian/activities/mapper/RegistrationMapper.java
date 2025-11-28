package com.xidian.activities.mapper;

import com.xidian.activities.dto.RegistrationQueryDTO;
import com.xidian.activities.entity.Registration;
import com.xidian.activities.vo.RegistrationVO;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 报名Mapper接口
 *
 * @author
 * @since
 */
@Mapper
public interface RegistrationMapper {

        /**
         * 插入报名记录
         *
         * @param registration 报名信息
         * @return 影响行数
         */
        int insert(Registration registration);

        /**
         * 根据ID查询报名记录
         *
         * @param id 报名ID
         * @return 报名信息
         */
        Registration selectById(@Param("id") Long id);

        /**
         * 根据条件查询报名列表
         *
         * @param queryDTO 查询条件
         * @return 报名列表
         */
        List<Registration> selectByConditions(RegistrationQueryDTO queryDTO);

        /**
         * 更新报名记录
         *
         * @param registration 报名信息
         * @return 影响行数
         */
        int updateById(Registration registration);

        /**
         * 统计活动总报名人数
         *
         * @param activityId 活动ID
         * @return 报名人数
         */
        int countTotalRegistrations(@Param("activityId") Long activityId);

        /**
         * 统计活动有效报名人数（未取消）
         *
         * @param activityId 活动ID
         * @return 有效报名人数
         */
        int countValidRegistrations(@Param("activityId") Long activityId);

        /**
         * 统计活动已签到人数
         *
         * @param activityId 活动ID
         * @return 签到人数
         */
        int countCheckedIn(@Param("activityId") Long activityId);

        /**
         * 检查是否存在重复报名
         *
         * @param activityId   活动ID
         * @param studentPhone 学生手机号
         * @return 是否存在
         */
        boolean existsByActivityAndPhone(@Param("activityId") Long activityId,
                        @Param("studentPhone") String studentPhone);

        /**
         * 根据活动ID和手机号查询报名记录
         *
         * @param activityId   活动ID
         * @param studentPhone 学生手机号
         * @return 报名记录
         */
        Registration findByActivityAndPhone(@Param("activityId") Long activityId,
                        @Param("studentPhone") String studentPhone);

        List<RegistrationVO> selectByPhoneAndName(@Param("studentPhone") String studentPhone,
                        @Param("studentName") String studentName);
}       