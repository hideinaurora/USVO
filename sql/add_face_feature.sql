-- 为 basic_user 表添加人脸特征字段
-- 用于存储用户头像对应的人脸特征向量（512维，JSON格式存储）

ALTER TABLE `basic_user`
ADD COLUMN `face_feature` JSON COMMENT '人脸特征向量（512维归一化特征，JSON数组格式）' AFTER `avatar_url`;

-- 添加索引（可选，如果需要按人脸特征查询）
-- ALTER TABLE `basic_user` ADD INDEX `idx_face_feature` (`face_feature`(255));
