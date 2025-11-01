package com.xidian.activities.pojo;

import lombok.Data;
import java.util.Date;

/**
 * 活动类型字典表 (activity_types)
 */
@Data
public class ActivityType {
    /**
     * 类型ID，主键
     */
    private Integer id;

    /**
     * 类型编码
     */
    private String typeCode;

    /**
     * 类型名称
     */
    private String typeName;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 是否启用：0-禁用, 1-启用
     */
    private Integer isEnabled;

    /**
     * 记录创建时间
     */
    private Date createTime;

    /**
     * 记录更新时间
     */
    private Date updateTime;
}
