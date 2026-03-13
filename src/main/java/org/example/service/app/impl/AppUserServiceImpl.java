package org.example.service.app.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.config.exception.CommonJsonException;
import org.example.dto.ApplyRequestDTO;
import org.example.entity.activity.apply.ApplyEntity;
import org.example.entity.activity.apply.ApplyPayEntity;
import org.example.entity.activity.apply.ApplyUserEntity;
import org.example.entity.activity.refund.RefundEntity;
import org.example.entity.activity.refund.RefundExamineEntity;
import org.example.mq.DelayedProducer;
import org.example.service.ApplyLockService;
import org.example.service.activity.apply.ApplyPayService;
import org.example.service.activity.apply.ApplyService;
import org.example.service.activity.apply.ApplyUserService;
import org.example.service.activity.refund.RefundExamineService;
import org.example.service.activity.refund.RefundService;
import org.example.service.app.AppUserService;
import org.example.utils.StringTools;
import org.example.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AppUserServiceImpl implements AppUserService {

    private static final int ORDER_EXPIRE_MINUTES = 30; // 订单过期时间（分钟）

    @Resource
    private ApplyService applyService;

    @Resource
    private ApplyUserService applyUserService;

    @Resource
    private ApplyPayService applyPayService;

    @Resource
    private RefundService refundService;

    @Resource
    private RefundExamineService refundExamineService;

    @Resource
    private ApplyLockService applyLockService;

    @Resource
    private DelayedProducer delayedProducer;

    @Override
    public List<ActivityVO> getActivityListForApp(Long userId) {
        // 1. 查询所有活动
        QueryWrapper<ApplyEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("gmt_create");
        List<ApplyEntity> allActivities = applyService.list(queryWrapper);

        if (allActivities == null || allActivities.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 查询用户的所有报名记录
        QueryWrapper<ApplyUserEntity> userApplyWrapper = new QueryWrapper<>();
        userApplyWrapper.eq("user_id", userId);
        List<ApplyUserEntity> userApplyList = applyUserService.list(userApplyWrapper);

        // 构建用户报名记录映射：applyId -> ApplyUserEntity
        Map<Long, ApplyUserEntity> userApplyMap = userApplyList.stream()
                .collect(Collectors.toMap(ApplyUserEntity::getApplyId, apply -> apply));

        // 3. 查询所有待支付的订单（用于填充待支付状态的订单信息）
        List<Long> applyIds = userApplyList.stream()
                .map(ApplyUserEntity::getApplyId)
                .collect(Collectors.toList());

        Map<Long, ApplyPayEntity> payMap = Collections.emptyMap();
        if (!applyIds.isEmpty()) {
            QueryWrapper<ApplyPayEntity> payWrapper = new QueryWrapper<>();
            payWrapper.in("apply_id", applyIds);
            payWrapper.eq("user_id", userId);
            payWrapper.orderByDesc("id");
            List<ApplyPayEntity> payList = applyPayService.list(payWrapper);
            payMap = payList.stream()
                    .collect(Collectors.toMap(ApplyPayEntity::getApplyId, pay -> pay,
                            (k1, k2) -> k1.getId() > k2.getId() ? k1 : k2));
        }

        // 4. 组装返回结果
        List<ActivityVO> resultList = new ArrayList<>();
        for (ApplyEntity activity : allActivities) {
            ActivityVO vo = new ActivityVO();
            vo.setApplyId(activity.getApplyId());
            vo.setApplyTitle(activity.getApplyTitle());
            vo.setApplyStartTime(activity.getApplyStartTime());
            vo.setApplyEndTime(activity.getApplyEndTime());
            vo.setApplyExpense(activity.getApplyExpense());
            vo.setActiveInfo(activity.getActiveInfo());
            vo.setLimitNum(activity.getLimitNum());

            // 判断报名状态
            ApplyUserEntity userApply = userApplyMap.get(activity.getApplyId());
            if (userApply == null) {
                // 未报名
                vo.setApplyStatus(0);
            } else if (userApply.getIsPay() == 0) {
                // 已报名但待支付
                vo.setApplyStatus(1);
                // 填充支付订单信息
                ApplyPayEntity payEntity = payMap.get(activity.getApplyId());
                if (payEntity != null) {
                    ApplyPayVO payVO = new ApplyPayVO();
                    BeanUtils.copyProperties(payEntity, payVO);
                    vo.setApplyPay(payVO);
                }
            } else {
                // 支付完成
                vo.setApplyStatus(2);
            }

            resultList.add(vo);
        }

        return resultList;
    }

    @Override
    public List<EnrolledActivityVO> getEnrolledActivityList(Long userId) {
        // 1. 查询用户的所有报名记录
        QueryWrapper<ApplyUserEntity> userApplyWrapper = new QueryWrapper<>();
        userApplyWrapper.eq("user_id", userId);
        userApplyWrapper.eq("is_pay", 1);
        userApplyWrapper.orderByDesc("gmt_create");
        List<ApplyUserEntity> userApplyList = applyUserService.list(userApplyWrapper);

        if (userApplyList == null || userApplyList.isEmpty()) {
            return new ArrayList<>();
        }

        // 提取活动ID列表
        List<Long> applyIds = userApplyList.stream()
                .map(ApplyUserEntity::getApplyId)
                .collect(Collectors.toList());

        // 2. 查询活动信息
        QueryWrapper<ApplyEntity> activityWrapper = new QueryWrapper<>();
        activityWrapper.in("apply_id", applyIds);
        List<ApplyEntity> activityList = applyService.list(activityWrapper);
        Map<Long, ApplyEntity> activityMap = activityList.stream()
                .collect(Collectors.toMap(ApplyEntity::getApplyId, activity -> activity));

        // 3. 查询支付订单信息（查询所有状态的订单）
        QueryWrapper<ApplyPayEntity> payWrapper = new QueryWrapper<>();
        payWrapper.in("apply_id", applyIds);
        payWrapper.eq("user_id", userId);
        payWrapper.orderByDesc("id");
        List<ApplyPayEntity> payList = applyPayService.list(payWrapper);
        Map<Long, ApplyPayEntity> payMap = payList.stream()
                .collect(Collectors.toMap(ApplyPayEntity::getApplyId, pay -> pay,
                        (k1, k2) -> k1.getId() > k2.getId() ? k1 : k2));

        // 4. 查询退款记录
        QueryWrapper<RefundEntity> refundWrapper = new QueryWrapper<>();
        refundWrapper.in("apply_id", applyIds);
        refundWrapper.eq("user_id", userId);
        refundWrapper.orderByDesc("gmt_create");
        List<RefundEntity> refundList = refundService.list(refundWrapper);

        // 构建退款记录映射：applyId -> List<RefundEntity>
        Map<Long, List<RefundEntity>> refundMap = refundList.stream()
                .collect(Collectors.groupingBy(RefundEntity::getApplyId));

        // 查询所有退款记录的审核记录
        List<Long> refundIds = refundList.stream()
                .map(RefundEntity::getId)
                .collect(Collectors.toList());
        Map<Long, RefundExamineEntity> examineMap = Collections.emptyMap();
        if (!refundIds.isEmpty()) {
            QueryWrapper<RefundExamineEntity> examineWrapper = new QueryWrapper<>();
            examineWrapper.in("refund_id", refundIds);
            List<RefundExamineEntity> examineList = refundExamineService.list(examineWrapper);
            examineMap = examineList.stream()
                    .collect(Collectors.toMap(RefundExamineEntity::getRefundId, examine -> examine));
        }

        // 5. 组装返回结果
        List<EnrolledActivityVO> resultList = new ArrayList<>();
        for (ApplyUserEntity userApply : userApplyList) {
            ApplyEntity activity = activityMap.get(userApply.getApplyId());
            if (activity == null) {
                continue;
            }

            EnrolledActivityVO vo = new EnrolledActivityVO();
            vo.setApplyId(activity.getApplyId());
            vo.setApplyTitle(activity.getApplyTitle());
            vo.setApplyStartTime(activity.getApplyStartTime());
            vo.setApplyEndTime(activity.getApplyEndTime());
            vo.setApplyExpense(activity.getApplyExpense());
            vo.setActiveInfo(activity.getActiveInfo());
            vo.setLimitNum(activity.getLimitNum());
            vo.setEnrollRecordId(userApply.getId());
            vo.setEnrollTime(userApply.getGmtCreate());
            vo.setApplyStatus(userApply.getIsPay()); // 0-未支付，1-已支付

            // 填充支付订单信息
            ApplyPayEntity payEntity = payMap.get(activity.getApplyId());
            if (payEntity != null) {
                ApplyPayVO payVO = new ApplyPayVO();
                BeanUtils.copyProperties(payEntity, payVO);
                vo.setPayOrder(payVO);
            }

            // 填充退款记录及审核记录
            List<RefundEntity> refunds = refundMap.getOrDefault(activity.getApplyId(), new ArrayList<>());
            if (!refunds.isEmpty()) {
                List<RefundVO> refundVOList = new ArrayList<>();
                for (RefundEntity refund : refunds) {
                    RefundVO refundVO = new RefundVO();
                    BeanUtils.copyProperties(refund, refundVO);

                    // 填充审核记录
                    RefundExamineEntity examine = examineMap.get(refund.getId());
                    if (examine != null) {
                        RefundExamineVO examineVO = new RefundExamineVO();
                        BeanUtils.copyProperties(examine, examineVO);
                        refundVO.setExamineRecord(examineVO);
                    }

                    refundVOList.add(refundVO);
                }
                vo.setRefundRecords(refundVOList);
            }

            resultList.add(vo);
        }

        return resultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApplyResponseVO applyActivity(Long userId, ApplyRequestDTO request) {
        // 1. 参数校验
        if (request.getApplyId() == null) {
            throw new CommonJsonException("活动ID不能为空");
        }

        // 2. 查询活动信息
        ApplyEntity activity = applyService.getById(request.getApplyId());
        if (activity == null) {
            throw new CommonJsonException("活动不存在");
        }

        // 3. 检查报名时间是否在有效期内
        LocalDateTime now = LocalDateTime.now();
        if (activity.getApplyStartTime() != null && now.isBefore(activity.getApplyStartTime())) {
            throw new CommonJsonException("活动报名尚未开始");
        }
        if (activity.getApplyEndTime() != null && now.isAfter(activity.getApplyEndTime())) {
            throw new CommonJsonException("活动报名已结束");
        }

        // 4. 检查用户是否已报名
        QueryWrapper<ApplyUserEntity> userApplyWrapper = new QueryWrapper<>();
        userApplyWrapper.eq("user_id", userId);
        userApplyWrapper.eq("apply_id", request.getApplyId());
        ApplyUserEntity existApply = applyUserService.getOne(userApplyWrapper);
        if (existApply != null) {
            if (existApply.getIsPay() == 1) {
                throw new CommonJsonException("您已报名并支付该活动");
            } else {
                // 如果已报名但未支付，检查是否有未支付订单
                QueryWrapper<ApplyPayEntity> payWrapper = new QueryWrapper<>();
                payWrapper.eq("user_id", userId);
                payWrapper.eq("apply_id", request.getApplyId());
                payWrapper.eq("pay_status", 0); // 未支付
                payWrapper.orderByDesc("id");
                payWrapper.last("LIMIT 1");
                ApplyPayEntity existPay = applyPayService.getOne(payWrapper);
                if (existPay != null) {
                    // 返回现有订单信息
                    ApplyResponseVO response = new ApplyResponseVO();
                    response.setEnrollRecordId(existApply.getId());
                    response.setPayOrderId(existPay.getId());
                    response.setMerOrderId(existPay.getMerOrderId());
                    response.setApplyStatus(0);
                    response.setExpireMinutes(ORDER_EXPIRE_MINUTES);
                    return response;
                }
            }
        }

        // 5. 获取活动费用
        Integer expense = activity.getApplyExpense() != null ? activity.getApplyExpense() : 0;

        // 6. 判断是否为免费活动
        boolean isFreeActivity = expense == 0;

        // 7. 检查活动是否有名额限制
        boolean hasQuotaLimit = activity.getLimitNum() != null && activity.getLimitNum() > 0;
        if (hasQuotaLimit) {
            // 尝试锁定名额
            boolean locked = applyLockService.decreaseApply(request.getApplyId(), 1L);
            if (!locked) {
                throw new CommonJsonException("活动名额已满，报名失败");
            }
        }

        try {
            // 8. 创建或更新报名记录
            ApplyUserEntity userApply;
            if (existApply == null) {
                userApply = new ApplyUserEntity();
                userApply.setUserId(userId);
                userApply.setApplyId(request.getApplyId());

                // 如果是免费活动，直接设置为已支付
                if (isFreeActivity) {
                    userApply.setIsPay(1); // 已支付
                } else {
                    userApply.setIsPay(0); // 未支付
                }

                applyUserService.save(userApply);
            } else {
                userApply = existApply;
            }

            // 9. 判断是否需要创建支付订单
            if (isFreeActivity) {
                // 免费活动直接报名成功，不需要创建支付订单
                ApplyResponseVO response = new ApplyResponseVO();
                response.setEnrollRecordId(userApply.getId());
                response.setPayOrderId(null);
                response.setMerOrderId(null);
                response.setApplyStatus(1); // 已支付
                response.setExpireMinutes(0);

                log.info("用户免费活动报名成功：userId={}, applyId={}", userId, request.getApplyId());
                return response;
            }

            // 10. 创建支付订单（付费活动）
            ApplyPayEntity payOrder = new ApplyPayEntity();
            payOrder.setUserId(userId);
            payOrder.setApplyId(request.getApplyId());
            payOrder.setPayStatus(0); // 未支付

            // 生成商户订单号
            String merOrderId = generateMerOrderId();
            payOrder.setMerOrderId(merOrderId);

            // 设置订单描述
            String orderDesc = String.format("报名：%s", activity.getApplyTitle());
            payOrder.setOrderDesc(orderDesc);

            // 设置金额
            payOrder.setOriginalAmount(expense);
            payOrder.setTotalAmount(expense);

            // 设置商户名称
            payOrder.setMerName("活动报名平台");

            // 生成序列号
            payOrder.setSeqId(UUID.randomUUID().toString().replace("-", ""));

            // 设置订单过期时间（30分钟后）
            LocalDateTime expireTime = now.plusMinutes(ORDER_EXPIRE_MINUTES);
            payOrder.setExpireTime(expireTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            applyPayService.save(payOrder);

            // 11. 发送延迟消息到MQ，30分钟后检查订单状态
            try {
                delayedProducer.sendOrderCancelMessage(String.valueOf(payOrder.getId()), ORDER_EXPIRE_MINUTES);
            } catch (Exception e) {
                log.error("发送延迟消息失败，订单ID：{}", payOrder.getId(), e);
                // 不影响主流程，仅记录日志
            }

            // 12. 构建响应
            ApplyResponseVO response = new ApplyResponseVO();
            response.setEnrollRecordId(userApply.getId());
            response.setPayOrderId(payOrder.getId());
            response.setMerOrderId(payOrder.getMerOrderId());
            response.setApplyStatus(0); // 待支付
            response.setExpireMinutes(ORDER_EXPIRE_MINUTES);

            log.info("用户付费活动报名成功：userId={}, applyId={}, payOrderId={}", userId, request.getApplyId(), payOrder.getId());
            return response;

        } catch (Exception e) {
            // 如果报名失败，释放锁定的名额
            if (hasQuotaLimit) {
                try {
                    applyLockService.increaseApply(request.getApplyId(), 1L);
                } catch (Exception ex) {
                    log.error("释放名额失败：applyId={}", request.getApplyId(), ex);
                }
            }
            throw e;
        }
    }

    /**
     * 生成商户订单号
     */
    private String generateMerOrderId() {
        return String.format("MER" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + StringTools.generateRandomAlphanumericString(6));
    }
}
