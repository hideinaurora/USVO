package org.example.service.app;

import org.example.dto.ApplyRequestDTO;
import org.example.dto.RefundRequestDTO;
import org.example.vo.ActivityVO;
import org.example.vo.ApplyResponseVO;
import org.example.vo.EnrolledActivityVO;
import org.example.vo.RefundResponseVO;

import java.util.List;

public interface AppUserService {
    /**
     * 查询移动端可报名活动列表
     *
     * @param userId 用户ID
     * @return 活动列表，包含报名状态和支付信息
     */
    List<ActivityVO> getActivityListForApp(Long userId);

    /**
     * 查询用户已报名活动列表（包含支付订单和退款信息）
     *
     * @param userId 用户ID
     * @return 已报名活动列表
     */
    List<EnrolledActivityVO> getEnrolledActivityList(Long userId);

    /**
     * 用户报名活动
     *
     * @param userId 用户ID
     * @param request 报名请求
     * @return 报名响应，包含支付订单信息
     */
    ApplyResponseVO applyActivity(Long userId, ApplyRequestDTO request);

    /**
     * 用户申请退款
     * 支持审核拒绝后重新发起
     * 一个订单只存在一条申请退款的记录
     *
     * @param userId 用户ID
     * @param request 退款请求
     * @return 退款申请响应
     */
    RefundResponseVO applyRefund(Long userId, RefundRequestDTO request);
}
