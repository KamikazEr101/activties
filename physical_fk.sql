-- 说明：
-- 以下SQL语句用于在已创建的表结构上添加物理外键约束。
-- 请在执行完所有CREATE TABLE语句之后，再执行这些ALTER TABLE语句。
-- 这些约束能帮助数据库工具自动识别并绘制ER图。

-- 1. 为 `activities` 表添加外键，关联到 `administrators` 表
--    描述：一个活动 (activity) 必须由一个存在的管理员 (administrator) 创建。
--    动作：
--      - ON DELETE RESTRICT: 如果一个管理员创建了任何活动，不允许直接删除该管理员记录（需先处理其创建的活动）。
--      - ON UPDATE CASCADE: 如果管理员的ID发生变化（理论上不太可能），关联的活动记录会自动更新。
ALTER TABLE `activities`
ADD CONSTRAINT `fk_activities_creator`
  FOREIGN KEY (`creator_id`)
  REFERENCES `administrators` (`id`)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;


-- 2. 为 `activities` 表添加外键，关联到 `activity_types` 表
--    描述：一个活动 (activity) 的类型必须是 `activity_types` 表中存在的类型。
--    动作：
--      - ON DELETE RESTRICT: 如果一个活动类型已被使用，不允许直接删除该类型记录。
--      - ON UPDATE CASCADE: 如果活动类型的 `type_code` 发生变化，所有使用该类型的活动记录会自动更新其 `activity_type` 字段。
ALTER TABLE `activities`
ADD CONSTRAINT `fk_activities_type`
  FOREIGN KEY (`activity_type`)
  REFERENCES `activity_types` (`type_code`)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;


-- 3. 为 `registrations` 表添加外键，关联到 `activities` 表
--    描述：一条报名记录 (registration) 必须属于一个存在的活动 (activity)。
--    动作：
--      - ON DELETE CASCADE: 如果一个活动被删除，其下所有的报名记录也会被自动删除。这符合业务逻辑的强依赖关系。
--      - ON UPDATE CASCADE: 如果活动的ID发生变化，关联的报名记录会自动更新。
ALTER TABLE `registrations`
ADD CONSTRAINT `fk_registrations_activity`
  FOREIGN KEY (`activity_id`)
  REFERENCES `activities` (`id`)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

-- 说明：
-- 本脚本用于删除为生成ER图而临时添加的物理外键约束。
-- 执行此脚本后，数据库将恢复为仅使用逻辑外键的状态，符合项目的设计原则。
-- 这不会删除为性能优化的索引，只会移除外键约束本身。

-- 1. 从 `activities` 表删除关联到 `administrators` 的外键
ALTER TABLE `activities` DROP FOREIGN KEY `fk_activities_creator`;

-- 2. 从 `activities` 表删除关联到 `activity_types` 的外键
ALTER TABLE `activities` DROP FOREIGN KEY `fk_activities_type`;

-- 3. 从 `registrations` 表删除关联到 `activities` 的外键
ALTER TABLE `registrations` DROP FOREIGN KEY `fk_registrations_activity`;