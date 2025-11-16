package com.xidian.activities.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动类型实体类 (activity_types)
 *
 * @author
 * @since
 */
@Data
public class ActivityType {

    /**
     * 类型ID，主键
     */
    private Long id;

    /**
     * 类型名称
     */
    private String typeName;

    /**
     * 类型编码
     */
    private String typeCode;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 是否启用：0-禁用, 1-启用
     */
    private Integer isEnabled;

    /**
     * 软删除标记：0-未删除, 1-已删除
     */
    private Integer isDeleted;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}