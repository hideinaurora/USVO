/*
 Navicat Premium Dump SQL

 Source Server         : 本地内网服务器8.0
 Source Server Type    : MySQL
 Source Server Version : 80200 (8.2.0)
 Source Host           : 192.168.10.141:33068
 Source Schema         : nblg_apply

 Target Server Type    : MySQL
 Target Server Version : 80200 (8.2.0)
 File Encoding         : 65001

 Date: 19/03/2026 17:26:48
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for activity_apply
-- ----------------------------
DROP TABLE IF EXISTS `activity_apply`;
CREATE TABLE `activity_apply`  (
  `apply_id` bigint NOT NULL AUTO_INCREMENT COMMENT '活动ID，主键',
  `apply_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '活动标题',
  `apply_start_time` datetime NULL DEFAULT NULL COMMENT '报名开始时间',
  `apply_end_time` datetime NULL DEFAULT NULL COMMENT '报名结束时间',
  `apply_expense` int NULL DEFAULT 0 COMMENT '报名费用（单位：分）',
  `gmt_create` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modify` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '删除标识：0-未删除，1-已删除',
  `active_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '活动详情',
  `limit_num` int NULL DEFAULT 0 COMMENT '限制人数',
  `remaining_quota` int NULL DEFAULT NULL COMMENT '剩余报名人数',
  PRIMARY KEY (`apply_id`) USING BTREE,
  INDEX `idx_is_deleted`(`is_deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '活动报名活动表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for activity_apply_pay
-- ----------------------------
DROP TABLE IF EXISTS `activity_apply_pay`;
CREATE TABLE `activity_apply_pay`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID，主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `pay_status` tinyint NULL DEFAULT 0 COMMENT '支付状态',
  `mer_order_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '商户订单号',
  `order_desc` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '订单描述',
  `expire_time` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '订单过期时间',
  `original_amount` int NULL DEFAULT 0 COMMENT '原始金额（单位：分）',
  `total_amount` int NULL DEFAULT 0 COMMENT '总金额（单位：分）',
  `mer_name` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '商户名称',
  `seq_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '序列号/流水号',
  `pay_time` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付完成时间',
  `gmt_create` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modify` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '删除标识：0-未删除，1-已删除',
  `apply_id` bigint NULL DEFAULT NULL COMMENT '报名申请ID',
  `apply_student_id` bigint NULL DEFAULT NULL COMMENT '报名学生ID',
  `pay_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '支付信息详情',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_mer_order_id`(`mer_order_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_apply_id`(`apply_id` ASC) USING BTREE,
  INDEX `idx_apply_student_id`(`apply_student_id` ASC) USING BTREE,
  INDEX `idx_pay_type`(`pay_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '支付订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for activity_apply_user
-- ----------------------------
DROP TABLE IF EXISTS `activity_apply_user`;
CREATE TABLE `activity_apply_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID，主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `is_pay` int NULL DEFAULT 0 COMMENT '支付状态：0-未支付，1-已支付',
  `apply_id` bigint NOT NULL COMMENT '报名申请ID',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '删除标识：0-未删除，1-已删除',
  `gmt_create` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modify` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_apply`(`user_id` ASC, `apply_id` ASC, `is_deleted` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_apply_id`(`apply_id` ASC) USING BTREE,
  INDEX `idx_is_pay`(`is_pay` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户报名记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for activity_refund
-- ----------------------------
DROP TABLE IF EXISTS `activity_refund`;
CREATE TABLE `activity_refund`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '退款申请ID，主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `mer_order_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商户订单号',
  `examine_type` int NULL DEFAULT 0 COMMENT '审核类型：0-待审核，1-审核通过，2-审核拒绝',
  `gmt_create` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modify` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '删除标识：0-未删除，1-已删除',
  `apply_id` bigint NULL DEFAULT NULL COMMENT '报名申请ID',
  `apply_student_id` bigint NULL DEFAULT NULL COMMENT '报名学生ID',
  `refund_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '退款单号',
  `refund_time` datetime NULL DEFAULT NULL COMMENT '退款时间',
  `refund_amount` int NULL DEFAULT 0 COMMENT '退款金额（单位：分）',
  `refund_total_time` datetime NULL DEFAULT NULL COMMENT '退款完成总时间',
  `out_refund_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '外部退款单号',
  `bz` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '备注',
  `total_amount` int NULL DEFAULT 0 COMMENT '实付金额（单位：分）',
  `fail_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '失败消息',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_mer_order_id`(`mer_order_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_apply_id`(`apply_id` ASC) USING BTREE,
  INDEX `idx_apply_student_id`(`apply_student_id` ASC) USING BTREE,
  INDEX `idx_examine_type`(`examine_type` ASC) USING BTREE,
  INDEX `idx_refund_no`(`refund_no` ASC) USING BTREE,
  INDEX `idx_out_refund_no`(`out_refund_no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '退款申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for activity_refund_examine
-- ----------------------------
DROP TABLE IF EXISTS `activity_refund_examine`;
CREATE TABLE `activity_refund_examine`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '审核记录ID，主键',
  `refund_id` bigint NOT NULL COMMENT '退款申请ID',
  `examine_type` int NULL DEFAULT 0 COMMENT '审核结果：0-待审核，1-审核通过，2-审核拒绝',
  `gmt_create` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modify` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '删除标识：0-未删除，1-已删除',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核理由/说明',
  `admin_id` bigint NULL DEFAULT NULL COMMENT '审核管理员ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_refund_id`(`refund_id` ASC) USING BTREE,
  INDEX `idx_admin_id`(`admin_id` ASC) USING BTREE,
  INDEX `idx_examine_type`(`examine_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '退款审核记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for basic_user
-- ----------------------------
DROP TABLE IF EXISTS `basic_user`;
CREATE TABLE `basic_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID，主键',
  `user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户名',
  `gmt_modify` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `login_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录名',
  `login_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录密码',
  `user_status` int NULL DEFAULT 1 COMMENT '用户状态：1-正常，0-禁用',
  `is_deleted` int NULL DEFAULT 0 COMMENT '删除标识：0-未删除，1-已删除',
  `wx_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '微信id',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_name`(`user_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for failed_delayed_message
-- ----------------------------
DROP TABLE IF EXISTS `failed_delayed_message`;
CREATE TABLE `failed_delayed_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `message_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `failed_time` datetime NOT NULL COMMENT '失败时间',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_message_id`(`message_id` ASC) USING BTREE,
  INDEX `idx_failed_time`(`failed_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '延迟消息失败记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_admin
-- ----------------------------
DROP TABLE IF EXISTS `sys_admin`;
CREATE TABLE `sys_admin`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID，主键',
  `login_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录名',
  `login_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录密码',
  `user_status` int NULL DEFAULT 1 COMMENT '用户状态：1-正常，0-禁用',
  `is_deleted` int NULL DEFAULT 0 COMMENT '删除标识：0-未删除，1-已删除',
  `gmt_create` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modify` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_login_name`(`login_name` ASC) USING BTREE,
  INDEX `idx_user_status`(`user_status` ASC) USING BTREE,
  INDEX `idx_is_deleted`(`is_deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统用户表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;


-- 插入管理员用户，密码是D5a!rX&8upsQ*x
INSERT INTO `nblg_apply`.`sys_admin` (`id`, `login_name`, `login_password`, `user_status`, `is_deleted`, `gmt_create`, `gmt_modify`) VALUES (1, 'admin', '5c43dfbba6de1ea2721273dede9412a0', 1, 0, '2026-03-13 11:32:56', '2026-03-14 16:58:38');



CREATE TABLE user (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      open_id VARCHAR(64) UNIQUE,
                      username VARCHAR(50),
                      phone VARCHAR(20),

                      violation_count INT DEFAULT 0 COMMENT '违约次数',
                      credit_score INT DEFAULT 100 COMMENT '信用分',

                      status TINYINT DEFAULT 1 COMMENT '1正常 0禁用',

                      create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                      update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


CREATE TABLE venue (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       name VARCHAR(100),
                       type VARCHAR(50),
                       address VARCHAR(255),
                       latitude DECIMAL(10,6),
                       longitude DECIMAL(10,6),

                       open_time TIME,
                       close_time TIME,

                       status TINYINT DEFAULT 1,

                       create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE court (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       venue_id BIGINT,
                       name VARCHAR(100),
                       type VARCHAR(50),

                       price_per_hour DECIMAL(10,2),

                       status TINYINT DEFAULT 1,

                       create_time DATETIME DEFAULT CURRENT_TIMESTAMP,

                       INDEX idx_venue_id (venue_id)
);

CREATE TABLE time_slot (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           court_id BIGINT,

                           slot_date DATE,
                           start_time DATETIME,
                           end_time DATETIME,

                           status TINYINT DEFAULT 0 COMMENT '0可预约 1锁定中 2已预约',

                           version INT DEFAULT 0 COMMENT '乐观锁',

                           booking_id BIGINT NULL,

                           create_time DATETIME DEFAULT CURRENT_TIMESTAMP,

                           UNIQUE KEY uk_court_time (court_id, start_time),
                           INDEX idx_query (court_id, slot_date, status)
);


CREATE TABLE booking (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,

                         user_id BIGINT,
                         court_id BIGINT,

                         start_time DATETIME,
                         end_time DATETIME,

                         total_amount DECIMAL(10,2),
                         deposit_amount DECIMAL(10,2),

                         status TINYINT COMMENT '
        0待支付
        1已预约
        2已取消
        3已完成
        4违约
    ',

                         cancel_time DATETIME,
                         checkin_time DATETIME,

                         create_time DATETIME DEFAULT CURRENT_TIMESTAMP,

                         INDEX idx_user (user_id),
                         INDEX idx_court_time (court_id, start_time),
                         INDEX idx_status (status)
);


CREATE TABLE payment (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,

                         booking_id BIGINT,
                         user_id BIGINT,

                         amount DECIMAL(10,2),

                         pay_type VARCHAR(20) COMMENT 'wechat/alipay',
                         status TINYINT COMMENT '0未支付 1已支付 2已退款',

                         transaction_no VARCHAR(100),

                         create_time DATETIME DEFAULT CURRENT_TIMESTAMP,

                         INDEX idx_booking (booking_id)
);

CREATE TABLE checkin_log (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT,

                             booking_id BIGINT,
                             user_id BIGINT,

                             checkin_time DATETIME,
                             checkin_type VARCHAR(20) COMMENT '扫码/GPS',

                             device_id VARCHAR(50),

                             create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE violation_log (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,

                               user_id BIGINT,
                               booking_id BIGINT,

                               reason VARCHAR(255),

                               penalty_amount DECIMAL(10,2),

                               create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE user
    ADD COLUMN password VARCHAR(255) COMMENT '加密密码',
ADD COLUMN salt VARCHAR(64) COMMENT '盐值',
ADD COLUMN login_type TINYINT DEFAULT 1 COMMENT '1账号 2微信',
ADD COLUMN last_login_time DATETIME;

INSERT INTO user (id, username, password, salt, open_id, login_type, phone, violation_count, credit_score)
VALUES
-- 账号密码用户
(1, 'zhangsan', MD5(CONCAT('123456', 'salt123')), 'salt123', NULL, 1, '13800000001', 0, 100),
(2, 'lisi',     MD5(CONCAT('123456', 'salt456')), 'salt456', NULL, 1, '13800000002', 1, 90),

-- 微信用户
(3, NULL, NULL, NULL, 'wx_openid_001', 2, '13800000003', 0, 100),
(4, NULL, NULL, NULL, 'wx_openid_002', 2, '13800000004', 2, 80);


INSERT INTO venue (id, name, type, address, latitude, longitude, open_time, close_time)
VALUES
    (1, '南校区体育中心', '综合馆', '南校区体育路1号', 31.2304, 121.4737, '08:00:00', '22:00:00'),
    (2, '北校区篮球馆', '篮球馆', '北校区体育路2号', 31.2310, 121.4740, '08:00:00', '22:00:00'),
    (3, '游泳训练中心', '游泳馆', '西区游泳路3号', 31.2290, 121.4720, '09:00:00', '21:00:00'),
    (4, '羽毛球馆', '羽毛球馆', '东区羽毛球路', 31.2320, 121.4750, '08:00:00', '22:00:00');


INSERT INTO court (id, venue_id, name, type, price_per_hour)
VALUES
-- 南校区
(1, 1, '篮球场1号', 'basketball', 50),
(2, 1, '篮球场2号', 'basketball', 50),
(3, 1, '羽毛球场1号', 'badminton', 30),

-- 北校区
(4, 2, '篮球场A', 'basketball', 60),
(5, 2, '篮球场B', 'basketball', 60),

-- 游泳馆
(6, 3, '泳道1', 'swimming', 40),
(7, 3, '泳道2', 'swimming', 40),

-- 羽毛球馆
(8, 4, '羽毛球场A', 'badminton', 35),
(9, 4, '羽毛球场B', 'badminton', 35);

INSERT INTO time_slot (court_id, slot_date, start_time, end_time, status)
VALUES
    -- 篮球场1号（含已预约）
    (1, '2026-04-03', '2026-04-03 10:00:00', '2026-04-03 11:00:00', 2),
    (1, '2026-04-03', '2026-04-03 11:00:00', '2026-04-03 12:00:00', 0),
    (1, '2026-04-03', '2026-04-03 12:00:00', '2026-04-03 13:00:00', 0),

    -- 篮球场2号
    (2, '2026-04-03', '2026-04-03 10:00:00', '2026-04-03 11:00:00', 0),
    (2, '2026-04-03', '2026-04-03 11:00:00', '2026-04-03 12:00:00', 0),

    -- 篮球场A
    (4, '2026-04-03', '2026-04-03 10:00:00', '2026-04-03 11:00:00', 0),

    -- 游泳
    (6, '2026-04-03', '2026-04-03 10:00:00', '2026-04-03 11:00:00', 0),

    -- 羽毛球
    (8, '2026-04-03', '2026-04-03 10:00:00', '2026-04-03 11:00:00', 0);


INSERT INTO booking (id, user_id, court_id, start_time, end_time, total_amount, deposit_amount, status)
VALUES
    (1001, 1, 1, '2026-04-03 10:00:00', '2026-04-03 11:00:00', 50, 20, 1);

UPDATE time_slot
SET booking_id = 1001
WHERE court_id = 1 AND start_time = '2026-04-03 10:00:00';

INSERT INTO payment (booking_id, user_id, amount, pay_type, status)
VALUES
    (1001, 1, 20, 'wechat', 1);


INSERT INTO violation_log (user_id, booking_id, reason, penalty_amount)
VALUES
    (2, 1002, '未按时到场', 20);


ALTER TABLE basic_user
    ADD COLUMN phone VARCHAR(20) COMMENT '手机号',
ADD COLUMN violation_count INT DEFAULT 0 COMMENT '违约次数',
ADD COLUMN credit_score INT DEFAULT 100 COMMENT '信用分',
ADD COLUMN last_login_time DATETIME COMMENT '最后登录时间';


DROP TABLE user;

-- 添加用户表头像URL字段
ALTER TABLE `basic_user` ADD COLUMN `avatar_url` VARCHAR(500) NULL DEFAULT NULL COMMENT '用户头像URL' AFTER `phone`;

-- 添加场馆表预览图URL字段
ALTER TABLE `venue` ADD COLUMN `preview_url` VARCHAR(500) NULL DEFAULT NULL COMMENT '场馆预览图URL' AFTER `close_time`;

-- 添加场地表预览图URL字段
ALTER TABLE `court` ADD COLUMN `preview_url` VARCHAR(500) NULL DEFAULT NULL COMMENT '场地预览图URL' AFTER `price_per_hour`;