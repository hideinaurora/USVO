-- 添加用户表头像URL字段
ALTER TABLE `basic_user` ADD COLUMN `avatar_url` VARCHAR(500) NULL DEFAULT NULL COMMENT '用户头像URL' AFTER `phone`;

-- 添加场馆表预览图URL字段
ALTER TABLE `venue` ADD COLUMN `preview_url` VARCHAR(500) NULL DEFAULT NULL COMMENT '场馆预览图URL' AFTER `close_time`;

-- 添加场地表预览图URL字段
ALTER TABLE `court` ADD COLUMN `preview_url` VARCHAR(500) NULL DEFAULT NULL COMMENT '场地预览图URL' AFTER `price_per_hour`;