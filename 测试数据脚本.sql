-- 管理员登录功能测试数据
-- 表结构：sys_admin

-- 清空测试数据（可选）
-- DELETE FROM sys_admin WHERE login_name = 'admin';

-- 插入测试管理员账号
-- 密码 123456 的 MD5 值为：e10adc3949ba59abbe56e057f20f883e
INSERT INTO sys_admin (login_name, login_password, user_status, is_deleted, gmt_create, gmt_modify)
VALUES ('admin', 'e10adc3949ba59abbe56e057f20f883e', 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    login_password = 'e10adc3949ba59abbe56e057f20f883e',
    user_status = 1,
    is_deleted = 0,
    gmt_modify = NOW();

-- 查询验证
SELECT id, login_name, user_status, is_deleted, gmt_create, gmt_modify
FROM sys_admin
WHERE login_name = 'admin';

-- 说明：
-- 1. MD5('123456') = e10adc3949ba59abbe56e057f20f883e
-- 2. user_status: 1-正常，0-禁用
-- 3. is_deleted: 0-未删除，1-已删除
-- 4. 登录时系统会对输入的密码进行 MD5 加密后与数据库中的密码比对
