package com.xidian.activities.bean;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Activity {
    private Long id;
    private String activityName;
    private String activityDescription;
    private String activityType; // 默认为 '其他'
    private Integer activityStatus; // 状态码
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private String organizer;
    private String contactPerson;
    private String contactPhone;
    private String contactEmail;
    private String posterUrl;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private LocalDateTime registrationStartTime;
    private LocalDateTime registrationEndTime;
    private String registrationRequirement;
    private LocalDateTime checkInStartTime;
    private LocalDateTime checkInEndTime;
    private LocalDateTime publishTime;
    private Long creatorId;
    private Boolean isDeleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
