ALTER TABLE basic_user
    ADD COLUMN phone VARCHAR(20) COMMENT '手机号',
ADD COLUMN violation_count INT DEFAULT 0 COMMENT '违约次数',
ADD COLUMN credit_score INT DEFAULT 100 COMMENT '信用分',
ADD COLUMN last_login_time DATETIME COMMENT '最后登录时间';


DROP TABLE user;