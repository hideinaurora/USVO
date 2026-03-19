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
