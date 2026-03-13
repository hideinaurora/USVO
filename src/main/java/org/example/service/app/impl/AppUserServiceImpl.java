package org.example.service.app.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.entity.activity.apply.ApplyEntity;
import org.example.entity.activity.apply.ApplyPayEntity;
import org.example.entity.activity.apply.ApplyUserEntity;
import org.example.entity.activity.refund.RefundEntity;
import org.example.entity.activity.refund.RefundExamineEntity;
import org.example.service.activity.apply.ApplyPayService;
import org.example.service.activity.apply.ApplyService;
import org.example.service.activity.apply.ApplyUserService;
import org.example.service.activity.refund.RefundExamineService;
import org.example.service.activity.refund.RefundService;
import org.example.service.app.AppUserService;
import org.example.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AppUserServiceImpl implements AppUserService {

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
}
