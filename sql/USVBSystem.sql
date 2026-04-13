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
