-- 高校学生活动全流程管理系统 - 数据库初始化脚本

-- 数据库版本: 1.0.0
-- 创建日期: 2025-10-26
-- MySQL版本要求: 8.0+
-- 字符集: utf8mb4
-- 排序规则: utf8mb4_unicode_ci
-- 存储引擎: InnoDB


-- 1. 创建数据库


-- 创建数据库
CREATE DATABASE IF NOT EXISTS activities_dev 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE activities_dev;


-- 2. 创建管理员表（administrators）


DROP TABLE IF EXISTS administrators;

CREATE TABLE administrators (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '管理员ID，主键',
    username VARCHAR(50) NOT NULL COMMENT '用户名（登录账号）',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密存储）',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号码',
    email VARCHAR(100) DEFAULT NULL COMMENT '电子邮箱',
    department VARCHAR(100) DEFAULT NULL COMMENT '所属部门/组织',
    role_type TINYINT(1) NOT NULL DEFAULT 1 COMMENT '角色类型：1-普通管理员，2-超级管理员',
    account_status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '账号状态：0-禁用，1-启用',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP地址',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标记：0-未删除，1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_phone (phone),
    KEY idx_email (email),
    KEY idx_role_type (role_type),
    KEY idx_account_status (account_status),
    KEY idx_is_deleted (is_deleted)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';


-- 3. 创建活动类型字典表（activity_types）


DROP TABLE IF EXISTS activity_types;

CREATE TABLE activity_types (
    id INT(11) NOT NULL AUTO_INCREMENT COMMENT '类型ID，主键',
    type_code VARCHAR(50) NOT NULL COMMENT '类型编码（唯一标识）',
    type_name VARCHAR(50) NOT NULL COMMENT '类型名称（显示名称）',
    type_icon VARCHAR(200) DEFAULT NULL COMMENT '类型图标URL',
    type_color VARCHAR(20) DEFAULT NULL COMMENT '类型显示颜色（十六进制色值）',
    sort_order INT(11) NOT NULL DEFAULT 0 COMMENT '排序顺序（数字越小越靠前）',
    is_enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    description VARCHAR(200) DEFAULT NULL COMMENT '类型描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_type_code (type_code),
    KEY idx_sort_order (sort_order),
    KEY idx_is_enabled (is_enabled)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动类型字典表';


-- 4. 创建活动表（activities）


DROP TABLE IF EXISTS activities;

CREATE TABLE activities (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '活动ID，主键',
    activity_name VARCHAR(100) NOT NULL COMMENT '活动名称',
    activity_description TEXT NOT NULL COMMENT '活动描述（富文本）',
    activity_type VARCHAR(50) DEFAULT '其他' COMMENT '活动类型（关联activity_types.type_code）',
    activity_status TINYINT(1) NOT NULL DEFAULT 0 COMMENT '活动状态：0-未发布，1-报名中，2-报名结束，3-进行中，4-已结束，5-已取消',
    start_time DATETIME NOT NULL COMMENT '活动开始时间',
    end_time DATETIME NOT NULL COMMENT '活动结束时间',
    location VARCHAR(200) NOT NULL COMMENT '活动地点',
    organizer VARCHAR(100) NOT NULL COMMENT '主办方/组织者',
    contact_person VARCHAR(50) NOT NULL COMMENT '负责人姓名',
    contact_phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    contact_email VARCHAR(100) DEFAULT NULL COMMENT '联系邮箱',
    poster_url VARCHAR(500) DEFAULT NULL COMMENT '活动海报URL（存储在MinIO）',
    max_participants INT(11) DEFAULT NULL COMMENT '最大报名人数（NULL表示不限制）',
    current_participants INT(11) NOT NULL DEFAULT 0 COMMENT '当前报名人数（冗余字段，提升查询性能）',
    registration_start_time DATETIME DEFAULT NULL COMMENT '报名开始时间',
    registration_end_time DATETIME DEFAULT NULL COMMENT '报名截止时间',
    registration_requirement TEXT DEFAULT NULL COMMENT '报名要求说明',
    check_in_start_time DATETIME DEFAULT NULL COMMENT '签到开始时间（活动开始前30分钟）',
    check_in_end_time DATETIME DEFAULT NULL COMMENT '签到截止时间（活动结束后1小时）',
    publish_time DATETIME DEFAULT NULL COMMENT '发布时间',
    creator_id BIGINT(20) NOT NULL COMMENT '创建者ID（逻辑外键关联administrators.id）',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标记：0-未删除，1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    PRIMARY KEY (id),
    KEY idx_activity_name (activity_name),
    KEY idx_activity_type (activity_type),
    KEY idx_activity_status (activity_status),
    KEY idx_start_time (start_time),
    KEY idx_registration_end_time (registration_end_time),
    KEY idx_publish_time (publish_time),
    KEY idx_creator_id (creator_id),
    KEY idx_is_deleted (is_deleted),
    KEY idx_status_deleted (activity_status, is_deleted)
) ENGINE=InnoDB AUTO_INCREMENT=10000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动表';


-- 5. 创建报名记录表（registrations）


DROP TABLE IF EXISTS registrations;

CREATE TABLE registrations (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '报名ID，主键',
    activity_id BIGINT(20) NOT NULL COMMENT '活动ID（逻辑外键关联activities.id）',
    student_name VARCHAR(50) NOT NULL COMMENT '学生姓名',
    student_phone VARCHAR(20) NOT NULL COMMENT '学生手机号（用于身份识别）',
    student_college VARCHAR(100) NOT NULL COMMENT '学生学院',
    student_major VARCHAR(100) DEFAULT NULL COMMENT '学生专业',
    student_grade VARCHAR(20) DEFAULT NULL COMMENT '学生年级（如：2021级、大二）',
    registration_status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '报名状态：1-报名成功，2-已取消，3-已拒绝',
    check_in_status TINYINT(1) NOT NULL DEFAULT 0 COMMENT '签到状态：0-未签到，1-已签到，2-请假',
    check_in_time DATETIME DEFAULT NULL COMMENT '签到时间',
    cancel_time DATETIME DEFAULT NULL COMMENT '取消报名时间',
    cancel_reason VARCHAR(500) DEFAULT NULL COMMENT '取消原因',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注信息',
    registration_source VARCHAR(50) DEFAULT 'WEB' COMMENT '报名来源：WEB-网页，QRCODE-二维码，API-接口',
    registration_ip VARCHAR(50) DEFAULT NULL COMMENT '报名IP地址',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标记：0-未删除，1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间（记录创建时间）',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    PRIMARY KEY (id),
    KEY idx_activity_id (activity_id),
    KEY idx_student_name (student_name),
    KEY idx_student_phone (student_phone),
    KEY idx_student_college (student_college),
    KEY idx_registration_status (registration_status),
    KEY idx_check_in_status (check_in_status),
    KEY idx_create_time (create_time),
    KEY idx_is_deleted (is_deleted),
    UNIQUE KEY uk_activity_phone (activity_id, student_phone, is_deleted),
    KEY idx_activity_checkin (activity_id, check_in_status)
) ENGINE=InnoDB AUTO_INCREMENT=100000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报名记录表';


-- 6. 创建系统操作日志表（system_logs）


DROP TABLE IF EXISTS system_logs;

CREATE TABLE system_logs (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID，主键',
    admin_id BIGINT(20) DEFAULT NULL COMMENT '操作管理员ID（逻辑外键关联administrators.id）',
    admin_username VARCHAR(50) DEFAULT NULL COMMENT '操作管理员用户名（冗余字段，便于查询）',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型：LOGIN/LOGOUT/CREATE_ACTIVITY/UPDATE_ACTIVITY/DELETE_ACTIVITY/PUBLISH_ACTIVITY等',
    operation_module VARCHAR(50) NOT NULL COMMENT '操作模块：AUTH-认证，ACTIVITY-活动，REGISTRATION-报名，SYSTEM-系统',
    operation_description VARCHAR(500) DEFAULT NULL COMMENT '操作描述',
    request_method VARCHAR(10) DEFAULT NULL COMMENT '请求方法：GET/POST/PUT/DELETE',
    request_url VARCHAR(500) DEFAULT NULL COMMENT '请求URL',
    request_params TEXT DEFAULT NULL COMMENT '请求参数（JSON格式）',
    request_ip VARCHAR(50) DEFAULT NULL COMMENT '请求IP地址',
    user_agent VARCHAR(500) DEFAULT NULL COMMENT '浏览器User-Agent',
    operation_status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '操作状态：0-失败，1-成功',
    error_message TEXT DEFAULT NULL COMMENT '错误信息（操作失败时记录）',
    execution_time INT(11) DEFAULT NULL COMMENT '执行耗时（毫秒）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间（记录创建时间）',
    PRIMARY KEY (id),
    KEY idx_admin_id (admin_id),
    KEY idx_admin_username (admin_username),
    KEY idx_operation_type (operation_type),
    KEY idx_operation_module (operation_module),
    KEY idx_request_ip (request_ip),
    KEY idx_operation_status (operation_status),
    KEY idx_create_time (create_time),
    KEY idx_admin_time (admin_id, create_time)
) ENGINE=InnoDB AUTO_INCREMENT=1000000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统操作日志表';


-- 7. 插入初始数据 - 活动类型


INSERT INTO activity_types (type_code, type_name, type_color, sort_order, is_enabled, description) VALUES
('ACADEMIC', '学术讲座', '#1890FF', 1, 1, '各类学术讲座、学术报告会、专家讲座等'),
('SPORTS', '文体活动', '#52C41A', 2, 1, '体育比赛、文艺演出、运动会、晚会等'),
('VOLUNTEER', '志愿服务', '#FA8C16', 3, 1, '志愿者活动、公益服务、社会实践等'),
('CLUB', '社团活动', '#722ED1', 4, 1, '各类学生社团组织的活动'),
('COMPETITION', '竞赛活动', '#F5222D', 5, 1, '各类学科竞赛、技能比赛、创新创业比赛等'),
('OTHER', '其他', '#8C8C8C', 99, 1, '其他类型的活动');


-- 8. 插入初始数据 - 默认管理员账号

-- 注意：密码为 admin123，使用BCrypt加密
-- 加密后的密码：$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi
-- 生产环境请务必修改默认密码！

INSERT INTO administrators (username, password, real_name, phone, email, department, role_type, account_status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', '13800138000', 'admin@example.com', '信息技术部', 2, 1),
('operator', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '活动管理员', '13800138001', 'operator@example.com', '学生活动中心', 1, 1);


-- 9. 插入测试数据 - 示例活动（可选，仅用于开发测试）


-- 示例活动1：学术讲座
INSERT INTO activities (
    activity_name, activity_description, activity_type, activity_status,
    start_time, end_time, location, organizer,
    contact_person, contact_phone, contact_email,
    max_participants, current_participants,
    registration_start_time, registration_end_time,
    registration_requirement,
    check_in_start_time, check_in_end_time,
    publish_time, creator_id
) VALUES (
    '人工智能前沿技术讲座',
    '<p>本次讲座邀请知名AI专家分享最新的人工智能技术发展趋势，包括大语言模型、计算机视觉、强化学习等前沿领域。</p><p>讲座内容：</p><ul><li>大语言模型的发展与应用</li><li>AI在各行业的落地实践</li><li>人工智能的未来展望</li></ul>',
    'ACADEMIC',
    1,
    '2025-11-15 14:00:00',
    '2025-11-15 16:30:00',
    '图书馆学术报告厅',
    '计算机学院',
    '张教授',
    '029-88888888',
    'zhangprof@xidian.edu.cn',
    200,
    0,
    '2025-10-26 08:00:00',
    '2025-11-14 18:00:00',
    '面向全校师生开放，建议具有一定编程基础。',
    '2025-11-15 13:30:00',
    '2025-11-15 17:30:00',
    '2025-10-26 10:00:00',
    1000
);

-- 示例活动2：文体活动
INSERT INTO activities (
    activity_name, activity_description, activity_type, activity_status,
    start_time, end_time, location, organizer,
    contact_person, contact_phone,
    max_participants, current_participants,
    registration_start_time, registration_end_time,
    check_in_start_time, check_in_end_time,
    publish_time, creator_id
) VALUES (
    '秋季校园马拉松比赛',
    '<p>2025年秋季校园马拉松比赛即将开始！欢迎广大师生踊跃报名参加。</p><p>比赛项目：</p><ul><li>全程马拉松（42.195公里）</li><li>半程马拉松（21.0975公里）</li><li>迷你马拉松（5公里）</li></ul><p>注意事项：请参赛者自备运动装备，注意安全。</p>',
    'SPORTS',
    1,
    '2025-11-08 07:00:00',
    '2025-11-08 12:00:00',
    '校园主干道',
    '体育部',
    '李老师',
    '029-88888889',
    500,
    0,
    '2025-10-26 08:00:00',
    '2025-11-05 23:59:59',
    '2025-11-08 06:30:00',
    '2025-11-08 13:00:00',
    '2025-10-26 10:00:00',
    1001
);

-- 示例活动3：志愿服务
INSERT INTO activities (
    activity_name, activity_description, activity_type, activity_status,
    start_time, end_time, location, organizer,
    contact_person, contact_phone, contact_email,
    max_participants, current_participants,
    registration_start_time, registration_end_time,
    registration_requirement,
    check_in_start_time, check_in_end_time,
    publish_time, creator_id
) VALUES (
    '社区义务支教活动',
    '<p>组织学生志愿者前往周边社区，为中小学生提供课业辅导和兴趣培养服务。</p><p>服务内容：</p><ul><li>数学、英语等学科辅导</li><li>科学实验兴趣课</li><li>艺术素养培养</li></ul><p>志愿者要求：有耐心、责任心强、善于沟通。</p>',
    'VOLUNTEER',
    1,
    '2025-11-09 09:00:00',
    '2025-11-09 17:00:00',
    '长安区某社区服务中心',
    '校团委志愿者协会',
    '王同学',
    '13900139000',
    'volunteer@student.xidian.edu.cn',
    30,
    0,
    '2025-10-26 08:00:00',
    '2025-11-06 18:00:00',
    '具备相关学科知识，有志愿服务经验者优先。',
    '2025-11-09 08:30:00',
    '2025-11-09 18:00:00',
    '2025-10-26 10:00:00',
    1001
);


-- 10. 插入测试数据 - 示例报名记录（可选）


-- 为第一个活动添加示例报名
INSERT INTO registrations (
    activity_id, student_name, student_phone, student_college,
    student_major, student_grade, registration_status,
    check_in_status, registration_source
) VALUES
(10000, '张三', '13800001111', '计算机科学与技术学院', '计算机科学与技术', '2022级', 1, 0, 'WEB'),
(10000, '李四', '13800002222', '通信工程学院', '通信工程', '2021级', 1, 0, 'WEB'),
(10000, '王五', '13800003333', '电子工程学院', '电子信息工程', '2023级', 1, 0, 'QRCODE');

-- 为第二个活动添加示例报名
INSERT INTO registrations (
    activity_id, student_name, student_phone, student_college,
    student_major, student_grade, registration_status,
    check_in_status, registration_source
) VALUES
(10001, '赵六', '13800004444', '机电工程学院', '机械工程', '2022级', 1, 0, 'WEB'),
(10001, '孙七', '13800005555', '经济与管理学院', '工商管理', '2021级', 1, 0, 'WEB');

-- 更新活动的当前报名人数（与报名记录保持一致）
UPDATE activities SET current_participants = 3 WHERE id = 10000;
UPDATE activities SET current_participants = 2 WHERE id = 10001;


-- 11. 数据库初始化完成提示

-- 说明：
-- - 活动状态自动更新由 Java 后端的定时任务实现
-- - 报名人数统计由 Java Service 层在报名/取消时更新
-- - 数据统计和报表由 Java 后端查询实现


SELECT '============================================' AS '';
SELECT '数据库初始化完成！' AS 'Status';
SELECT '============================================' AS '';
SELECT '数据库名称: activities_dev' AS 'Info';
SELECT '字符集: utf8mb4' AS 'Info';
SELECT '排序规则: utf8mb4_unicode_ci' AS 'Info';
SELECT '存储引擎: InnoDB' AS 'Info';
SELECT '============================================' AS '';
SELECT '已创建表：' AS '';
SELECT '  1. administrators (管理员表)' AS '';
SELECT '  2. activity_types (活动类型字典表)' AS '';
SELECT '  3. activities (活动表)' AS '';
SELECT '  4. registrations (报名记录表)' AS '';
SELECT '  5. system_logs (系统操作日志表)' AS '';
SELECT '============================================' AS '';
SELECT '默认管理员账号：' AS '';
SELECT '  超级管理员 - 用户名: admin, 密码: admin123' AS '';
SELECT '  普通管理员 - 用户名: operator, 密码: admin123' AS '';
SELECT '  ⚠️  生产环境请务必修改默认密码！' AS '';
SELECT '============================================' AS '';
SELECT '已插入测试数据：' AS '';
SELECT '  - 6种活动类型' AS '';
SELECT '  - 2个管理员账号' AS '';
SELECT '  - 3个示例活动' AS '';
SELECT '  - 5条示例报名记录' AS '';
SELECT '============================================' AS '';
SELECT '数据库已准备就绪，可以启动应用程序！' AS '';
SELECT '============================================' AS '';
