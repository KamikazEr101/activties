-- =================================================================
-- 临时外键添加脚本 - 用于生成ER图
-- 说明: 此脚本用于临时添加物理外键约束，以便数据库建模工具生成ER图
--       执行完毕后会自动删除所有添加的外键
-- 使用方法: 在数据库建模工具中执行此脚本，生成ER图后外键会自动被删除
-- =================================================================

USE `activities_dev`;

-- ----------------------------------------------------------------
-- 第一部分: 添加物理外键约束
-- ----------------------------------------------------------------

-- 1. activities表的外键
-- 1.1 活动类型外键 (activities.activity_type -> activity_types.type_code)
ALTER TABLE `activities`
ADD CONSTRAINT `fk_activities_activity_type`
FOREIGN KEY (`activity_type`) 
REFERENCES `activity_types` (`type_code`)
ON DELETE RESTRICT
ON UPDATE CASCADE;

-- 1.2 创建者外键 (activities.creator_id -> administrators.id)
ALTER TABLE `activities`
ADD CONSTRAINT `fk_activities_creator`
FOREIGN KEY (`creator_id`) 
REFERENCES `administrators` (`id`)
ON DELETE RESTRICT
ON UPDATE CASCADE;

-- 2. registrations表的外键
-- 2.1 活动外键 (registrations.activity_id -> activities.id)
ALTER TABLE `registrations`
ADD CONSTRAINT `fk_registrations_activity`
FOREIGN KEY (`activity_id`) 
REFERENCES `activities` (`id`)
ON DELETE RESTRICT
ON UPDATE CASCADE;

-- ----------------------------------------------------------------
-- 提示: 请在此处暂停，使用数据库建模工具生成ER图
-- 生成ER图后，继续执行下面的删除外键语句
-- ----------------------------------------------------------------

-- ----------------------------------------------------------------
-- 第二部分: 删除临时外键约束
-- ----------------------------------------------------------------

-- 删除 registrations 表的外键
ALTER TABLE `registrations`
DROP FOREIGN KEY `fk_registrations_activity`;

-- 删除 activities 表的外键
ALTER TABLE `activities`
DROP FOREIGN KEY `fk_activities_activity_type`;

ALTER TABLE `activities`
DROP FOREIGN KEY `fk_activities_creator`;

-- ----------------------------------------------------------------
-- 提示: 外键已全部删除，数据库恢复到原始状态（仅使用逻辑外键）
-- ----------------------------------------------------------------

-- =================================================================
-- 脚本执行完毕
-- =================================================================
