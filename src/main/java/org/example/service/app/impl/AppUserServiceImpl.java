package org.example.service.app.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.entity.activity.apply.ApplyEntity;
import org.example.entity.activity.apply.ApplyPayEntity;
import org.example.entity.activity.apply.ApplyUserEntity;
import org.example.service.activity.apply.ApplyPayService;
import org.example.service.activity.apply.ApplyService;
import org.example.service.activity.apply.ApplyUserService;
import org.example.service.app.AppUserService;
import org.example.vo.ActivityVO;
import org.example.vo.ApplyPayVO;
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
            payWrapper.eq("pay_status", 0); // 只查询待支付的订单
            List<ApplyPayEntity> payList = applyPayService.list(payWrapper);
            payMap = payList.stream()
                    .collect(Collectors.toMap(ApplyPayEntity::getApplyId, pay -> pay));
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
}
