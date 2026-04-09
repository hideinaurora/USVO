-- 将 time_slot 的日期更新为当天，并同步 start_time/end_time 的日期部分为当天（保留时分秒）
-- 适用于演示/测试数据快速滚动到当天
-- MySQL 8.x

START TRANSACTION;

UPDATE time_slot
SET
    slot_date = CURDATE(),
    start_time = TIMESTAMP(CURDATE(), TIME(start_time)),
    end_time = TIMESTAMP(CURDATE(), TIME(end_time))
WHERE 1 = 1;

COMMIT;

