package com.xidian.activities.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 活动状态枚举
 */
@Getter
@AllArgsConstructor
public enum ActivityStatusEnum {

    UNRELEASED(0, "未发布"),
    REGISTERING(1, "报名中"),
    REGISTRATION_ENDED(2, "报名结束"),
    IN_PROGRESS(3, "进行中"),
    ENDED(4, "已结束"),
    CANCELLED(5, "已取消");

    private final Integer code;
    private final String description;
}
