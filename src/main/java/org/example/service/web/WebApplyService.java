package org.example.service.web;

import org.example.common.PageResult;
import org.example.dto.activity.ApplyQueryDTO;
import org.example.dto.activity.ApplySaveDTO;
import org.example.dto.activity.ApplyUserQueryDTO;
import org.example.vo.activity.ApplyDetailVO;
import org.example.vo.activity.ApplyUserDetailVO;

import java.util.List;

public interface WebApplyService {
    /**
     * 分页查询活动列表（含统计信息）
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    PageResult<ApplyDetailVO> queryPage(ApplyQueryDTO queryDTO);

    /**
     * 查询活动列表（不分页，含统计信息）
     *
     * @return 活动列表
     */
    List<ApplyDetailVO> queryList();

    /**
     * 查询活动详情（含统计信息）
     *
     * @param applyId 活动ID
     * @return 活动详情
     */
    ApplyDetailVO queryDetail(Long applyId);

    /**
     * 新增活动
     *
     * @param saveDTO 活动信息
     * @return 活动ID
     */
    Long add(ApplySaveDTO saveDTO);

    /**
     * 修改活动
     *
     * @param saveDTO 活动信息
     * @return 是否成功
     */
    Boolean update(ApplySaveDTO saveDTO);

    /**
     * 删除活动
     *
     * @param applyIds 活动ID（多个逗号分隔）
     * @return 是否成功
     */
    Boolean delete(String applyIds);

    /**
     * 分页查询活动报名学生列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    PageResult<ApplyUserDetailVO> queryApplyUserPage(ApplyUserQueryDTO queryDTO);

    /**
     * 查询活动报名学生列表（不分页）
     *
     * @param applyId 活动ID
     * @return 报名学生列表
     */
    List<ApplyUserDetailVO> queryApplyUserList(Long applyId);
}
