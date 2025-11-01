-- =================================================================
-- 高校学生活动全流程管理系统 - 数据库初始化脚本
-- 版本: 1.0
-- 描述:
-- 1. 创建数据库 `activities_dev`。
-- 2. 创建所有业务表（使用逻辑外键，无物理外键约束）。
-- 3. 插入基础数据（管理员、活动类型）和示例数据（活动）。
-- =================================================================

-- ----------------------------------------------------------------
-- 数据库创建
-- ----------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS `activities_dev`
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

USE `activities_dev`;

-- ----------------------------------------------------------------
-- 表结构创建
-- 说明: 删除顺序与创建顺序相反，确保脚本可重复执行。
-- ----------------------------------------------------------------
DROP TABLE IF EXISTS `registrations`;
DROP TABLE IF EXISTS `activities`;
DROP TABLE IF EXISTS `activity_types`;
DROP TABLE IF EXISTS `administrators`;

-- 1. 管理员表
CREATE TABLE `administrators` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号码',
  `email` varchar(100) DEFAULT NULL COMMENT '电子邮箱',
  `role_type` int(11) NOT NULL DEFAULT '1' COMMENT '角色：1-普通管理员, 2-超级管理员',
  `account_status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：0-禁用, 1-启用',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '软删除标记',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- 2. 活动类型字典表
CREATE TABLE `activity_types` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '类型ID',
  `type_code` varchar(50) NOT NULL COMMENT '类型编码',
  `type_name` varchar(50) NOT NULL COMMENT '类型名称',
  `sort_order` int(11) NOT NULL DEFAULT '0' COMMENT '排序顺序',
  `is_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用：0-禁用, 1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_code` (`type_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动类型字典表';

-- 3. 活动表
CREATE TABLE `activities` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `activity_name` varchar(100) NOT NULL COMMENT '活动名称',
  `activity_description` text COMMENT '活动描述',
  `activity_type` varchar(50) NOT NULL DEFAULT 'OTHER' COMMENT '活动类型编码',
  `activity_status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '状态：0-未发布, 1-报名中, 2-报名结束, 3-进行中, 4-已结束, 5-已取消',
  `start_time` datetime NOT NULL COMMENT '活动开始时间',
  `end_time` datetime NOT NULL COMMENT '活动结束时间',
  `location` varchar(200) NOT NULL COMMENT '活动地点',
  `organizer` varchar(100) NOT NULL COMMENT '主办方',
  `contact_person` varchar(50) NOT NULL COMMENT '负责人姓名',
  `contact_phone` varchar(20) NOT NULL COMMENT '联系电话',
  `poster_url` varchar(500) DEFAULT NULL COMMENT '活动海报URL',
  `max_participants` int(11) DEFAULT NULL COMMENT '最大报名人数 (NULL为不限)',
  `registration_start_time` datetime NOT NULL COMMENT '报名开始时间',
  `registration_end_time` datetime NOT NULL COMMENT '报名截止时间',
  `creator_id` bigint(20) NOT NULL COMMENT '创建者ID (逻辑外键 -> administrators.id)',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '软删除标记',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_activity_name` (`activity_name`),
  KEY `idx_status_deleted` (`activity_status`, `is_deleted`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_creator_id` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动表';

-- 4. 报名记录表
CREATE TABLE `registrations` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '报名ID',
  `activity_id` bigint(20) NOT NULL COMMENT '活动ID (逻辑外键 -> activities.id)',
  `student_name` varchar(50) NOT NULL COMMENT '学生姓名',
  `student_phone` varchar(20) NOT NULL COMMENT '学生手机号',
  `student_college` varchar(100) NOT NULL COMMENT '学生学院',
  `registration_status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：1-报名成功, 2-已取消',
  `check_in_status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '签到状态：0-未签到, 1-已签到',
  `check_in_time` datetime DEFAULT NULL COMMENT '签到时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '软删除标记',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_phone_deleted` (`activity_id`, `student_phone`, `is_deleted`),
  KEY `idx_student_phone` (`student_phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报名记录表';

-- ----------------------------------------------------------------
-- 基础数据与示例数据插入
-- ----------------------------------------------------------------
-- 1. 初始化活动类型
INSERT INTO `activity_types` (`type_code`, `type_name`, `sort_order`) VALUES
('ACADEMIC', '学术讲座', 1),
('SPORTS', '文体活动', 2),
('VOLUNTEER', '志愿服务', 3),
('CLUB', '社团活动', 4),
('COMPETITION', '竞赛活动', 5),
('OTHER', '其他', 99);

-- 2. 创建默认管理员账号
-- 密码原文均为: admin123
-- BCrypt加密后的值为: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi
INSERT INTO `administrators` (`id`, `username`, `password`, `real_name`, `phone`, `email`, `role_type`, `account_status`)
VALUES
(1, 'superadmin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '超级管理员', '13800138000', 'super@example.com', 2, 1),
(2, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '活动管理员', '13800138001', 'admin@example.com', 1, 1);

-- 3. 初始化活动示例数据
-- 注意：以下日期可能需要根据当前时间进行调整以保证状态的合理性。
-- 创建者ID `2` 对应上面创建的 'admin' 用户。
INSERT INTO `activities` (`activity_name`, `activity_description`, `activity_type`, `activity_status`, `start_time`, `end_time`, `location`, `organizer`, `contact_person`, `contact_phone`, `max_participants`, `registration_start_time`, `registration_end_time`, `creator_id`)
VALUES
('新生计算机基础讲座', '由计算机学院主办，面向全校新生的计算机入门讲座，内容涵盖常用软件使用、网络安全知识和编程入门介绍。', 'ACADEMIC', 1, '2024-09-15 19:00:00', '2024-09-15 21:00:00', '学术报告厅A201', '计算机学院', '王老师', '13812345678', 200, '2024-09-01 08:00:00', '2024-09-14 23:59:59', 2),
('校园篮球友谊赛', '为了丰富校园文化生活，增进各学院同学间的交流，特举办此次篮球友誼赛，欢迎大家踊跃报名或到场观战。', 'SPORTS', 0, '2024-10-12 14:00:00', '2024-10-12 17:00:00', '东区篮球场', '校学生会体育部', '李同学', '13787654321', NULL, '2024-09-20 08:00:00', '2024-10-10 23:59:59', 2),
('图书馆志愿者招募', '校图书馆现面向全校招募热心志愿者，参与图书整理、读者咨询等服务工作。服务时间可灵活安排，并计入志愿时长。', 'VOLUNTEER', 4, '2024-03-10 09:00:00', '2024-03-31 17:00:00', '中心图书馆', '校青年志愿者协会', '刘老师', '13611223344', 50, '2024-03-01 08:00:00', '2024-03-09 23:59:59', 2);


-- =================================================================
-- 初始化脚本执行完毕
-- =================================================================