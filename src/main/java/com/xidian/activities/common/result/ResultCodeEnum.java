package com.xidian.activities.common.result;

import lombok.Getter;

/**
 * 统一返回结果状态信息类
 * 
 * 错误码规范：
 * - 200-299: 成功响应
 * - 400-499: 客户端错误
 * - 500-599: 服务端错误
 * - 1000-1999: 活动模块业务错误
 * - 2000-2999: 报名模块业务错误
 * - 3000-3999: 签到模块业务错误
 * - 4000-4999: 用户认证模块业务错误
 */
@Getter
public enum ResultCodeEnum {

    // ========== 通用响应码 2xx ==========
    SUCCESS(200, "操作成功"),

    // ========== 客户端错误 4xx ==========
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "无权访问"),
    NOT_FOUND(404, "请求的资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    REQUEST_TIMEOUT(408, "请求超时"),
    CONFLICT(409, "资源冲突"),

    // ========== 服务端错误 5xx ==========
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂时不可用"),

    // ========== 数据校验错误 4xx ==========
    PARAM_ERROR(400, "参数校验失败"),
    DATA_ERROR(400, "数据格式错误"),

    // ========== 活动模块错误码 1000-1999 ==========
    ACTIVITY_NOT_FOUND(1001, "活动不存在或已删除"),
    ACTIVITY_NOT_PUBLISHED(1002, "活动未发布"),
    ACTIVITY_CANCELLED(1003, "活动已取消"),
    ACTIVITY_ENDED(1004, "活动已结束"),
    ACTIVITY_FULL(1005, "活动报名人数已满"),
    ACTIVITY_TYPE_INVALID(1006, "活动类型不存在或已禁用"),
    ACTIVITY_TIME_INVALID(1007, "活动时间设置不合理"),
    // ACTIVITY_NOT_EDITABLE(1008, "活动已发布，无法修改"),
    ACTIVITY_NOT_DELETABLE(1009, "活动已被发布，不可删除"),
    ACTIVITY_STATUS_INVALID(1010, "活动状态转换不合法"),
    // ACTIVITY_REGISTRATION_STARTED(1014, "活动报名已开始，无法修改关键信息"),
    ACTIVITY_INFO_INCOMPLETE(1013, "活动信息不完整，无法发布"),
    REGISTRATION_NOT_STARTED(1011, "活动报名尚未开始"),
    REGISTRATION_ENDED(1012, "活动报名已截止"),

    // ========== 报名模块错误码 2000-2999 ==========
    REGISTRATION_NOT_FOUND(2001, "报名记录不存在"),
    REGISTRATION_ALREADY_EXISTS(2002, "您已报名该活动，请勿重复报名"),
    REGISTRATION_CHECKED_IN(2003, "该学生已完成签到"),
    REGISTRATION_NOT_ACTIVE(2004, "报名状态异常，无法签到"),
    REGISTRATION_CANCEL_TOO_LATE(2005, "活动已开始，无法取消报名"),
    REGISTRATION_STUDENT_INFO_INVALID(2006, "学生信息不完整或不正确"),

    // ========== 签到模块错误码 3000-3999 ==========
    CHECKIN_NOT_ALLOWED(3001, "当前不在签到时间范围内"),
    CHECKIN_ACTIVITY_NOT_STARTED(3002, "活动尚未开始，无法签到"),
    CHECKIN_ACTIVITY_ENDED(3003, "活动已结束，签到时间已过"),
    CHECKIN_ALREADY_DONE(3004, "您已完成签到，请勿重复操作"),

    // ========== 用户认证模块错误码 4000-4999 ==========
    ADMIN_LOGIN_AUTH(4001, "请先登录"),
    ADMIN_ACCOUNT_NOT_EXIST_ERROR(4002, "账号不存在"),
    ADMIN_ACCOUNT_ERROR(4003, "用户名或密码错误"),
    ADMIN_ACCOUNT_DISABLED_ERROR(4004, "账号已被禁用，请联系管理员"),
    ADMIN_ACCESS_FORBIDDEN(4005, "权限不足，无法访问"),
    TOKEN_INVALID(4006, "令牌无效或已失效"),
    TOKEN_EXPIRED(4007, "令牌已过期，请重新登录");

    private final Integer code;
    private final String message;

    ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
