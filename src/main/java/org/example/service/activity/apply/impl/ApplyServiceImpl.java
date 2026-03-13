package org.example.service.activity.apply.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.common.PageResult;
import org.example.config.exception.CommonJsonException;
import org.example.dto.activity.ApplyQueryDTO;
import org.example.dto.activity.ApplySaveDTO;
import org.example.entity.activity.apply.ApplyEntity;
import org.example.entity.activity.apply.ApplyPayEntity;
import org.example.entity.activity.apply.ApplyUserEntity;
import org.example.entity.activity.refund.RefundEntity;
import org.example.mapper.activity.apply.ApplyMapper;
import org.example.service.activity.apply.ApplyPayService;
import org.example.service.activity.apply.ApplyService;
import org.example.service.activity.apply.ApplyUserService;
import org.example.service.activity.refund.RefundService;
import org.example.vo.activity.ApplyDetailVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 活动报名活动表 服务实现类
 * </p>
 *
 * @author ckd
 * @since 2026-03-13
 */
@Slf4j
@Service
public class ApplyServiceImpl extends ServiceImpl<ApplyMapper, ApplyEntity> implements ApplyService {

    @Resource
    private ApplyUserService applyUserService;

    @Resource
    private ApplyPayService applyPayService;

    @Resource
    private RefundService refundService;

    @Override
    public PageResult<ApplyDetailVO> queryPage(ApplyQueryDTO queryDTO) {
        // 1. 构建分页查询
        Page<ApplyEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        QueryWrapper<ApplyEntity> queryWrapper = new QueryWrapper<>();

        // 2. 标题模糊查询
        if (StringUtils.isNotBlank(queryDTO.getApplyTitle())) {
            queryWrapper.like("apply_title", queryDTO.getApplyTitle());
        }

        // 3. 时间范围查询
        if (StringUtils.isNotBlank(queryDTO.getStartTimeBegin())) {
            queryWrapper.ge("apply_start_time", queryDTO.getStartTimeBegin());
        }
        if (StringUtils.isNotBlank(queryDTO.getStartTimeEnd())) {
            queryWrapper.le("apply_start_time", queryDTO.getStartTimeEnd());
        }

        // 4. 按创建时间倒序
        queryWrapper.orderByDesc("gmt_create");

        // 5. 执行分页查询
        Page<ApplyEntity> pageResult = page(page, queryWrapper);

        // 6. 转换并填充统计信息
        List<ApplyDetailVO> voList = fillStatisticsInfo(pageResult.getRecords());

        return PageResult.of(
                queryDTO.getPageNum(),
                queryDTO.getPageSize(),
                pageResult.getTotal(),
                voList
        );
    }

    @Override
    public List<ApplyDetailVO> queryList() {
        // 查询所有活动
        QueryWrapper<ApplyEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("gmt_create");
        List<ApplyEntity> entityList = list(queryWrapper);

        // 填充统计信息
        return fillStatisticsInfo(entityList);
    }

    @Override
    public ApplyDetailVO queryDetail(Long applyId) {
        ApplyEntity entity = getById(applyId);
        if (entity == null) {
            return null;
        }

        // 填充统计信息
        List<ApplyDetailVO> voList = fillStatisticsInfo(Collections.singletonList(entity));
        return voList.isEmpty() ? null : voList.get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(ApplySaveDTO saveDTO) {
        ApplyEntity entity = new ApplyEntity();
        BeanUtils.copyProperties(saveDTO, entity);
        save(entity);
        return entity.getApplyId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(ApplySaveDTO saveDTO) {
        if (saveDTO.getApplyId() == null) {
            throw new CommonJsonException("活动ID不能为空");
        }

        ApplyEntity entity = new ApplyEntity();
        BeanUtils.copyProperties(saveDTO, entity);
        return updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(String applyIds) {
        if (StringUtils.isBlank(applyIds)) {
            throw new CommonJsonException("活动ID不能为空");
        }

        String[] idArray = applyIds.split(",");
        List<Long> idList = new ArrayList<>();
        for (String idStr : idArray) {
            idList.add(Long.parseLong(idStr.trim()));
        }

        // 校验是否有报名记录
        checkApplyUsersExist(idList);

        return removeByIds(idList);
    }

    /**
     * 检查活动是否存在报名记录
     *
     * @param applyIds 活动ID列表
     */
    private void checkApplyUsersExist(List<Long> applyIds) {
        QueryWrapper<ApplyUserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("apply_id", applyIds);
        long count = applyUserService.count(queryWrapper);

        if (count > 0) {
            throw new CommonJsonException("该活动已存在报名记录，不允许删除");
        }
    }

    /**
     * 填充统计信息
     *
     * @param entityList 活动实体列表
     * @return 包含统计信息的VO列表
     */
    private List<ApplyDetailVO> fillStatisticsInfo(List<ApplyEntity> entityList) {
        if (entityList == null || entityList.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有活动ID
        List<Long> applyIds = entityList.stream()
                .map(ApplyEntity::getApplyId)
                .collect(Collectors.toList());

        // 1. 查询报名成功人数（is_pay = 1）
        QueryWrapper<ApplyUserEntity> userQuery = new QueryWrapper<>();
        userQuery.in("apply_id", applyIds);
        userQuery.eq("is_pay", 1);
        List<ApplyUserEntity> paidUsers = applyUserService.list(userQuery);
        Map<Long, Long> paidUserCountMap = paidUsers.stream()
                .collect(Collectors.groupingBy(ApplyUserEntity::getApplyId, Collectors.counting()));

        // 2. 查询支付金额数（pay_status = 1）
        QueryWrapper<ApplyPayEntity> payQuery = new QueryWrapper<>();
        payQuery.in("apply_id", applyIds);
        payQuery.eq("pay_status", 1);
        List<ApplyPayEntity> paidOrders = applyPayService.list(payQuery);
        Map<Long, Integer> payAmountMap = paidOrders.stream()
                .collect(Collectors.groupingBy(
                        ApplyPayEntity::getApplyId,
                        Collectors.summingInt(ApplyPayEntity::getTotalAmount)
                ));

        // 3. 查询退款成功人数（examine_type = 1）和退款金额
        QueryWrapper<RefundEntity> refundQuery = new QueryWrapper<>();
        refundQuery.in("apply_id", applyIds);
        refundQuery.eq("examine_type", 1);
        List<RefundEntity> refundList = refundService.list(refundQuery);
        Map<Long, Long> refundCountMap = refundList.stream()
                .collect(Collectors.groupingBy(RefundEntity::getApplyId, Collectors.counting()));
        Map<Long, Integer> refundAmountMap = refundList.stream()
                .collect(Collectors.groupingBy(
                        RefundEntity::getApplyId,
                        Collectors.summingInt(RefundEntity::getRefundAmount)
                ));

        // 4. 组装VO
        return entityList.stream().map(entity -> {
            ApplyDetailVO vo = new ApplyDetailVO();
            BeanUtils.copyProperties(entity, vo);

            Long applyId = entity.getApplyId();

            // 报名成功人数
            vo.setPaidUserCount(paidUserCountMap.getOrDefault(applyId, 0L).intValue());

            // 支付金额总数
            Integer totalAmount = payAmountMap.getOrDefault(applyId, 0);
            vo.setTotalPayAmount(totalAmount);
            vo.setTotalPayAmountYuan(totalAmount / 100.0);

            // 退款成功人数
            vo.setRefundSuccessCount(refundCountMap.getOrDefault(applyId, 0L).intValue());

            // 退款金额总数
            Integer refundAmount = refundAmountMap.getOrDefault(applyId, 0);
            vo.setRefundTotalAmount(refundAmount);
            vo.setRefundTotalAmountYuan(refundAmount / 100.0);

            return vo;
        }).collect(Collectors.toList());
    }
}
