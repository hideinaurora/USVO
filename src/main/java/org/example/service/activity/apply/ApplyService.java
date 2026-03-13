package org.example.service.activity.apply;


import org.example.common.PageResult;
import org.example.dto.activity.ApplyQueryDTO;
import org.example.dto.activity.ApplySaveDTO;
import org.example.entity.activity.apply.ApplyEntity;
import org.example.vo.activity.ApplyDetailVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 活动报名活动表 服务类
 * </p>
 *
 * @author ckd
 * @since 2026-03-13
 */
public interface ApplyService extends IService<ApplyEntity> {

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
}
