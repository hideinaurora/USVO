package org.example.service.app;

import org.example.dto.ApplyRequestDTO;
import org.example.vo.ActivityVO;
import org.example.vo.ApplyResponseVO;
import org.example.vo.EnrolledActivityVO;

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
}
