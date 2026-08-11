SET NAMES utf8mb4;

-- ============================================================
-- Coinly 记账系统数据库脚本
-- 注意：MySQL 容器初始化时仅首次执行，后续需手动 ALTER
-- ============================================================

CREATE DATABASE IF NOT EXISTS coinly
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE coinly;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户 ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密后的密码',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1=正常，0=禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 系统默认分类模板表
CREATE TABLE IF NOT EXISTS sys_default_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类 ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    type TINYINT NOT NULL DEFAULT 0 COMMENT '类型：0=支出，1=收入',
    parent_id BIGINT DEFAULT NULL COMMENT '父分类 ID，null 表示一级分类',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_type_parent (type, parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统默认分类模板表';

-- 账本表
CREATE TABLE IF NOT EXISTS biz_book (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '账本 ID',
    user_id BIGINT NOT NULL COMMENT '所属用户 ID',
    name VARCHAR(50) NOT NULL COMMENT '账本名称',
    description VARCHAR(200) DEFAULT NULL COMMENT '账本描述',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常，1=已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_user_id (user_id),
    KEY idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账本表';

-- 用户分类表
CREATE TABLE IF NOT EXISTS biz_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类 ID',
    user_id BIGINT NOT NULL COMMENT '所属用户 ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    type TINYINT NOT NULL DEFAULT 0 COMMENT '类型：0=支出，1=收入',
    parent_id BIGINT DEFAULT NULL COMMENT '父分类 ID，null 表示一级分类',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常，1=已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_user_id (user_id),
    KEY idx_type_parent (type, parent_id),
    KEY idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户分类表';

-- 交易记录表
CREATE TABLE IF NOT EXISTS biz_transaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '交易 ID',
    user_id BIGINT NOT NULL COMMENT '所属用户 ID',
    book_id BIGINT NOT NULL COMMENT '所属账本 ID',
    category_id BIGINT NOT NULL COMMENT '分类 ID',
    type TINYINT NOT NULL DEFAULT 0 COMMENT '类型：0=支出，1=收入',
    amount DECIMAL(12,2) NOT NULL COMMENT '金额',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    transaction_date DATE NOT NULL COMMENT '交易日期',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常，1=已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_user_id (user_id),
    KEY idx_book_id (book_id),
    KEY idx_category_id (category_id),
    KEY idx_transaction_date (transaction_date),
    KEY idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易记录表';

-- 预算表
CREATE TABLE IF NOT EXISTS biz_budget (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '预算 ID',
    user_id BIGINT NOT NULL COMMENT '所属用户 ID',
    category_id BIGINT DEFAULT NULL COMMENT '分类 ID（NULL=总预算）',
    amount DECIMAL(12,2) NOT NULL COMMENT '预算金额',
    budget_month VARCHAR(7) NOT NULL COMMENT '预算月份 yyyy-MM',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常，1=已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_user_month (user_id, budget_month),
    KEY idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预算表';

-- ============================================================
-- V9.1 扩展表
-- ============================================================

-- 预算预警记录表
CREATE TABLE IF NOT EXISTS biz_budget_alert (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '预警ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    budget_id BIGINT NOT NULL COMMENT '预算ID',
    category_id BIGINT DEFAULT NULL COMMENT '分类ID（总预算为NULL）',
    category_name VARCHAR(50) DEFAULT NULL COMMENT '分类名称',
    budget_month VARCHAR(7) NOT NULL COMMENT '预算月份 yyyy-MM',
    budget_amount DECIMAL(18, 2) NOT NULL COMMENT '预算金额',
    used_amount DECIMAL(18, 2) NOT NULL COMMENT '已用金额',
    percentage DECIMAL(5, 2) NOT NULL COMMENT '使用率%',
    alert_level VARCHAR(20) NOT NULL COMMENT '预警级别 warning/danger',
    is_read TINYINT(1) DEFAULT 0 COMMENT '是否已读 0-未读 1-已读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_month (user_id, budget_month),
    INDEX idx_is_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预算预警记录表';

-- 周期记账配置表
CREATE TABLE IF NOT EXISTS biz_recurring_transaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    book_id BIGINT NOT NULL COMMENT '账本ID',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    type TINYINT NOT NULL COMMENT '交易类型 0-支出 1-收入',
    amount DECIMAL(18, 2) NOT NULL COMMENT '金额',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    cycle_type VARCHAR(20) NOT NULL COMMENT '周期类型 daily/weekly/monthly/yearly',
    cycle_day INT DEFAULT NULL COMMENT '周期执行日（月周期为1-31，周周期为1-7）',
    next_execute_date DATE NOT NULL COMMENT '下次执行日期',
    status TINYINT(1) DEFAULT 1 COMMENT '状态 0-停用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_status (user_id, status),
    INDEX idx_next_execute (next_execute_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='周期记账配置表';

-- 月账单快照表
CREATE TABLE IF NOT EXISTS biz_monthly_snapshot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '快照ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    book_id BIGINT DEFAULT NULL COMMENT '账本ID（NULL表示全部账本）',
    snapshot_month VARCHAR(7) NOT NULL COMMENT '账单月份 yyyy-MM',
    total_income DECIMAL(18, 2) NOT NULL DEFAULT 0 COMMENT '总收入',
    total_expense DECIMAL(18, 2) NOT NULL DEFAULT 0 COMMENT '总支出',
    net_amount DECIMAL(18, 2) NOT NULL DEFAULT 0 COMMENT '结余',
    transaction_count INT NOT NULL DEFAULT 0 COMMENT '交易笔数',
    category_summary JSON DEFAULT NULL COMMENT '分类汇总 JSON',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_book_month (user_id, book_id, snapshot_month),
    INDEX idx_user_month (user_id, snapshot_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='月账单快照表';

-- ============================================================
-- 初始数据
-- ============================================================

INSERT INTO `sys_default_category` VALUES
(1,'餐饮',0,NULL,1,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(2,'交通',0,NULL,2,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(3,'购物',0,NULL,3,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(4,'居住',0,NULL,4,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(5,'娱乐',0,NULL,5,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(6,'医疗',0,NULL,6,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(7,'教育',0,NULL,7,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(8,'其他支出',0,NULL,8,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(9,'工资',1,NULL,1,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(10,'奖金',1,NULL,2,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(11,'投资收益',1,NULL,3,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(12,'其他收入',1,NULL,4,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(13,'早餐',0,1,1,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(14,'午餐',0,1,2,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(15,'晚餐',0,1,3,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(16,'零食饮料',0,1,4,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(17,'公交地铁',0,2,1,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(18,'打车',0,2,2,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(19,'加油',0,2,3,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(20,'停车费',0,2,4,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(21,'服装',0,3,1,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(22,'数码',0,3,2,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(23,'日用品',0,3,3,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(24,'美妆护肤',0,3,4,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(25,'房租',0,4,1,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(26,'水电煤',0,4,2,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(27,'物业',0,4,3,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(28,'维修',0,4,4,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(29,'电影',0,5,1,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(30,'游戏',0,5,2,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(31,'旅游',0,5,3,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(32,'运动健身',0,5,4,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(33,'药品',0,6,1,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(34,'挂号费',0,6,2,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(35,'体检',0,6,3,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(36,'书籍',0,7,1,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(37,'课程培训',0,7,2,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(38,'考试费用',0,7,3,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(39,'人情往来',0,8,1,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(40,'捐赠',0,8,2,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(41,'罚款',0,8,3,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(42,'基本工资',1,9,1,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(43,'加班费',1,9,2,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(44,'补贴',1,9,3,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(45,'年终奖',1,10,1,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(46,'绩效奖',1,10,2,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(47,'节日奖',1,10,3,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(48,'股票',1,11,1,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(49,'基金',1,11,2,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(50,'理财',1,11,3,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(51,'红包',1,12,1,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(52,'二手闲置',1,12,2,'2026-08-10 11:57:22','2026-08-10 11:57:22'),
(53,'退款',1,12,3,'2026-08-10 11:57:22','2026-08-10 11:57:22');
