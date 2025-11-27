package com.xidian.activities.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 报名状态枚举
 */
@Getter
@AllArgsConstructor
public enum RegistrationStatusEnum {

    SUCCESS(1, "报名成功"),
    CANCELLED(2, "已取消");

    private final Integer code;
    private final String description;
}
