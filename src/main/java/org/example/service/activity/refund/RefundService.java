package org.example.service.activity.refund;


import org.example.common.PageResult;
import org.example.dto.activity.RefundExamineDTO;
import org.example.dto.activity.RefundQueryDTO;
import org.example.entity.activity.refund.RefundEntity;
import org.example.vo.activity.RefundDetailVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 退款申请表 服务类
 * </p>
 *
 * @author ckd
 * @since 2026-03-13
 */
public interface RefundService extends IService<RefundEntity> {

    /**
     * 分页查询退款学生列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    PageResult<RefundDetailVO> queryPage(RefundQueryDTO queryDTO);

    /**
     * 查询退款学生列表（不分页）
     *
     * @return 退款学生列表
     */
    List<RefundDetailVO> queryList();

    /**
     * 审核退款申请
     *
     * @param examineDTO 审核信息
     * @return 是否成功
     */
    Boolean examine(RefundExamineDTO examineDTO);
}
