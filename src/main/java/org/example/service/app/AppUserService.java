package org.example.service.app;

import org.example.vo.ActivityVO;

import java.util.List;

public interface AppUserService {
    /**
     * 查询移动端可报名活动列表
     *
     * @param userId 用户ID
     * @return 活动列表，包含报名状态和支付信息
     */
    List<ActivityVO> getActivityListForApp(Long userId);
}
