package com.xidian.activities.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 报名统计VO
 *
 * @author
 * @since
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "报名统计信息")
public class RegistrationStatisticsVO {

    @Schema(description = "活动ID", example = "1")
    private Long activityId;

    @Schema(description = "活动名称", example = "新生计算机基础讲座")
    private String activityName;

    @Schema(description = "最大参与人数，null表示不限制", example = "200")
    private Integer maxParticipants;

    @Schema(description = "总报名人数", example = "150")
    private Integer totalRegistrations;

    @Schema(description = "有效报名人数（未取消）", example = "145")
    private Integer validRegistrations;

    @Schema(description = "已取消报名人数", example = "5")
    private Integer cancelledRegistrations;

    @Schema(description = "已签到人数", example = "120")
    private Integer checkedInCount;

    @Schema(description = "未签到人数", example = "25")
    private Integer notCheckedInCount;

    @Schema(description = "剩余名额，null表示不限制", example = "50")
    private Integer remainingSlots;

    @Schema(description = "报名进度百分比", example = "75.0")
    private Double registrationProgress;

    @Schema(description = "签到率百分比", example = "82.76")
    private Double checkInRate;

    @Schema(description = "出勤率百分比", example = "82.76")
    private Double attendanceRate;
}