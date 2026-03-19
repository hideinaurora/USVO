package org.example.service;

import org.example.entity.activity.apply.ApplyEntity;
import org.example.service.activity.apply.ApplyService;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class ApplyLockService {

    private static final String INVENTORY_PREFIX = "apply:";
    private static final String LOCK_PREFIX = "lock:apply:";
    private static final long DEFAULT_WAIT_TIME = 3L;
    private static final long DEFAULT_LEASE_TIME = 10L;

    @Resource
    private RedissonClient redissonClient;
    @Resource
    private ApplyService service;

    /**
     * 增加库存 - 使用分布式锁
     */
    public boolean increaseApply(Long batchId, Long quantity) {
        return increaseApply(batchId, quantity, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME);
    }

    public boolean increaseApply(Long batchId, Long quantity, Long waitTime, Long leaseTime) {
        String lockKey = LOCK_PREFIX + batchId;
        String applyKey = INVENTORY_PREFIX + batchId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 获取分布式锁
            if (lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS)) {
                try {
                    // 获取当前库存
                    RAtomicLong atomicLong = redissonClient.getAtomicLong(applyKey);
                    long currentStock = atomicLong.get();
                    long newStock = currentStock + quantity;

                    // 更新库存
                    atomicLong.set(newStock);

                    // 更新数据库
                    ApplyEntity entity = service.getById(batchId);
                    entity.setLimitNum((int) (entity.getLimitNum() + quantity));
                    entity.setApplyId(batchId);
                    entity.setRemainingQuota((int) newStock);
                    service.updateById(entity);
                    System.out.println("增加库存成功: 活动ID=" + batchId + ", 增加数量=" + quantity + ", 当前库存=" + newStock);
                    return true;
                } finally {
                    // 释放锁
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            } else {
                System.err.println("获取锁失败，可能其他节点正在操作库存");
                return false;
            }
        } catch (Exception e) {
            System.err.println("增加库存异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 减少库存 - 使用分布式锁
     */
    public boolean decreaseApply(Long batchId, Long quantity) {
        return decreaseApply(batchId, quantity, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME);
    }

    public boolean decreaseApply(Long batchId, Long quantity, Long waitTime, Long leaseTime) {
        String lockKey = LOCK_PREFIX + batchId;
        String applyKey = INVENTORY_PREFIX + batchId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 获取分布式锁
            if (lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS)) {
                try {
                    // 获取当前库存
                    RAtomicLong atomicLong = redissonClient.getAtomicLong(applyKey);
                    long currentStock = atomicLong.get();

                    // 检查库存是否足够
                    if (currentStock < quantity) {
                        System.err.println("库存不足: 活动ID=" + batchId + ", 当前库存=" + currentStock + ", 需要扣减=" + quantity);
                        return false;
                    }

                    long newStock = currentStock - quantity;

                    // 更新库存
                    atomicLong.set(newStock);

                    // 更新数据库
                    ApplyEntity entity = service.getById(batchId);
                    entity.setApplyId(batchId);
                    entity.setLimitNum((int) (entity.getLimitNum() - quantity));
                    entity.setRemainingQuota((int) newStock);
                    service.updateById(entity);

                    System.out.println("减少库存成功: 活动ID=" + batchId + ", 减少数量=" + quantity + ", 当前库存=" + newStock);
                    return true;
                } finally {
                    // 释放锁
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            } else {
                System.err.println("获取锁失败，可能其他节点正在操作库存");
                return false;
            }
        } catch (Exception e) {
            System.err.println("减少库存异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取当前库存
     */
    public Long getApply(String batchId) {
        String applyKey = INVENTORY_PREFIX + batchId;
        return redissonClient.getAtomicLong(applyKey).get();
    }

    /**
     * 设置初始库存
     */
    public boolean setInitialApply(Long batchId, Long initialStock) {
        String applyKey = INVENTORY_PREFIX + batchId;
        try {
            redissonClient.getAtomicLong(applyKey).set(initialStock);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
