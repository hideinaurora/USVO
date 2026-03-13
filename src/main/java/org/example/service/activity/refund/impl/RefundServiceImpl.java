package org.example.service.activity.refund.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.example.common.PageResult;
import org.example.config.exception.CommonJsonException;
import org.example.dto.activity.RefundQueryDTO;
import org.example.entity.activity.apply.ApplyEntity;
import org.example.entity.activity.refund.RefundEntity;
import org.example.entity.activity.refund.RefundExamineEntity;
import org.example.entity.basic.user.UserEntity;
import org.example.mapper.activity.refund.RefundMapper;
import org.example.service.activity.apply.ApplyService;
import org.example.service.activity.refund.RefundExamineService;
import org.example.service.activity.refund.RefundService;
import org.example.service.basic.user.UserService;
import org.example.utils.StringTools;
import org.example.vo.activity.RefundDetailVO;
import org.example.dto.activity.RefundExamineDTO;
import org.example.dto.TokenDTO;
import org.example.utils.JWTUtil;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 退款申请表 服务实现类
 * </p>
 *
 * @author ckd
 * @since 2026-03-13
 */
@Slf4j
@Service
public class RefundServiceImpl extends ServiceImpl<RefundMapper, RefundEntity> implements RefundService {

    @Resource
    private UserService userService;

    @Resource
    private ApplyService applyService;

    @Resource
    private RefundExamineService refundExamineService;

    @Override
    public PageResult<RefundDetailVO> queryPage(RefundQueryDTO queryDTO) {
        // 1. 构建分页查询
        Page<RefundEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        QueryWrapper<RefundEntity> queryWrapper = new QueryWrapper<>();

        // 2. 活动ID查询
        if (queryDTO.getApplyId() != null) {
            queryWrapper.eq("apply_id", queryDTO.getApplyId());
        }

        // 3. 审核状态查询
        if (queryDTO.getExamineType() != null) {
            queryWrapper.eq("examine_type", queryDTO.getExamineType());
        }

        // 4. 用户名模糊查询
        if (!StringTools.isNullOrEmpty(queryDTO.getUserName())) {
            // 先查询用户ID列表
            QueryWrapper<UserEntity> userQuery = new QueryWrapper<>();
            userQuery.like("user_name", queryDTO.getUserName());
            List<UserEntity> userList = userService.list(userQuery);

            if (userList.isEmpty()) {
                // 如果没有匹配的用户，返回空结果
                return PageResult.of(
                        queryDTO.getPageNum(),
                        queryDTO.getPageSize(),
                        0L,
                        Collections.emptyList()
                );
            }

            // 提取用户ID列表
            List<Long> userIds = userList.stream()
                    .map(UserEntity::getId)
                    .collect(Collectors.toList());
            queryWrapper.in("user_id", userIds);
        }

        // 5. 按创建时间倒序
        queryWrapper.orderByDesc("gmt_create");

        // 6. 执行分页查询
        Page<RefundEntity> pageResult = page(page, queryWrapper);

        // 7. 填充关联信息
        List<RefundDetailVO> voList = fillDetailInfo(pageResult.getRecords());

        return PageResult.of(
                queryDTO.getPageNum(),
                queryDTO.getPageSize(),
                pageResult.getTotal(),
                voList
        );
    }

    @Override
    public List<RefundDetailVO> queryList() {
        // 查询所有退款记录
        QueryWrapper<RefundEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("gmt_create");
        List<RefundEntity> entityList = list(queryWrapper);

        // 填充关联信息
        return fillDetailInfo(entityList);
    }

    /**
     * 填充详细信息（用户名、活动标题、审核记录）
     *
     * @param entityList 退款实体列表
     * @return 详情VO列表
     */
    private List<RefundDetailVO> fillDetailInfo(List<RefundEntity> entityList) {
        if (entityList == null || entityList.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. 收集所有需要关联的ID
        Set<Long> userIds = new HashSet<>();
        Set<Long> applyIds = new HashSet<>();
        Set<Long> refundIds = new HashSet<>();

        for (RefundEntity entity : entityList) {
            if (entity.getUserId() != null) {
                userIds.add(entity.getUserId());
            }
            if (entity.getApplyId() != null) {
                applyIds.add(entity.getApplyId());
            }
            if (entity.getId() != null) {
                refundIds.add(entity.getId());
            }
        }

        // 2. 批量查询用户信息
        Map<Long, UserEntity> userMap;
        if (!userIds.isEmpty()) {
            QueryWrapper<UserEntity> userQuery = new QueryWrapper<>();
            userQuery.in("id", userIds);
            List<UserEntity> userList = userService.list(userQuery);
            userMap = userList.stream().collect(Collectors.toMap(UserEntity::getId, u -> u));
        } else {
            userMap = new HashMap<>();
        }

        // 3. 批量查询活动信息
        Map<Long, ApplyEntity> applyMap;
        if (!applyIds.isEmpty()) {
            List<ApplyEntity> applyList = applyService.listByIds(applyIds);
            applyMap = applyList.stream().collect(Collectors.toMap(ApplyEntity::getApplyId, a -> a));
        } else {
            applyMap = new HashMap<>();
        }

        // 4. 批量查询审核记录
        Map<Long, RefundExamineEntity> examineMap = new HashMap<>();
        if (!refundIds.isEmpty()) {
            QueryWrapper<RefundExamineEntity> examineQuery = new QueryWrapper<>();
            examineQuery.in("refund_id", refundIds);
            // 获取最新的审核记录
            examineQuery.orderByDesc("gmt_create");
            List<RefundExamineEntity> examineList = refundExamineService.list(examineQuery);
            // 如果有多条审核记录，取第一条（最新的）
            for (RefundExamineEntity examine : examineList) {
                if (!examineMap.containsKey(examine.getRefundId())) {
                    examineMap.put(examine.getRefundId(), examine);
                }
            }
        }

        // 5. 组装VO
        return entityList.stream().map(entity -> {
            RefundDetailVO vo = new RefundDetailVO();
            BeanUtils.copyProperties(entity, vo);

            // 设置用户名
            UserEntity user = userMap.get(entity.getUserId());
            if (user != null) {
                vo.setUserName(user.getUserName());
            }

            // 设置活动标题
            ApplyEntity apply = applyMap.get(entity.getApplyId());
            if (apply != null) {
                vo.setApplyTitle(apply.getApplyTitle());
            }

            // 设置审核状态描述
            vo.setExamineTypeDesc(getExamineTypeDesc(entity.getExamineType()));

            // 金额转换（分转元）
            if (entity.getRefundAmount() != null) {
                vo.setRefundAmountYuan(entity.getRefundAmount() / 100.0);
            }
            if (entity.getTotalAmount() != null) {
                vo.setTotalAmountYuan(entity.getTotalAmount() / 100.0);
            }

            // 设置审核记录信息
            RefundExamineEntity examine = examineMap.get(entity.getId());
            if (examine != null) {
                vo.setExamineId(examine.getId());
                vo.setExamineReason(examine.getReason());
                vo.setAdminId(examine.getAdminId());
                vo.setExamineTime(examine.getGmtCreate());
            }

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean examine(RefundExamineDTO examineDTO) {
        // 参数已在 DTO 层通过 @NotNull 校验，这里省略简单校验

        // 1. 查询退款申请
        RefundEntity refundEntity = getById(examineDTO.getRefundId());
        if (refundEntity == null) {
            throw new CommonJsonException("退款申请不存在");
        }

        // 2. 校验审核类型（业务规则校验）
        if (examineDTO.getExamineType() != 1 && examineDTO.getExamineType() != 2) {
            throw new CommonJsonException("审核类型只能为1（审核通过）或2（审核拒绝）");
        }

        // 3. 检查退款申请状态（业务规则校验）
        if (refundEntity.getExamineType() != null && refundEntity.getExamineType() != 0) {
            String statusDesc = getExamineTypeDesc(refundEntity.getExamineType());
            throw new CommonJsonException("该退款申请已被" + statusDesc + "，无法再次审核");
        }

        // 4. 获取当前管理员ID
        Long adminId = getCurrentAdminId();

        // 5. 更新退款申请的审核状态
        RefundEntity updateEntity = new RefundEntity();
        updateEntity.setId(examineDTO.getRefundId());
        updateEntity.setExamineType(examineDTO.getExamineType());
        updateById(updateEntity);

        // 6. 创建审核记录
        RefundExamineEntity examineEntity = new RefundExamineEntity();
        examineEntity.setRefundId(examineDTO.getRefundId());
        examineEntity.setExamineType(examineDTO.getExamineType());
        examineEntity.setReason(examineDTO.getReason());
        examineEntity.setAdminId(adminId);
        refundExamineService.save(examineEntity);

        log.info("管理员审核退款申请成功，refundId={}, adminId={}, examineType={}",
                examineDTO.getRefundId(), adminId, examineDTO.getExamineType());

        return true;
    }

    /**
     * 获取当前登录的管理员ID
     */
    private Long getCurrentAdminId() {
        try {
            String token = MDC.get("token");
            if (StringTools.isNullOrEmpty(token)) {
                throw new CommonJsonException("获取登录信息失败");
            }
            TokenDTO tokenDTO = JWTUtil.verifyToken(token);
            if (tokenDTO == null || tokenDTO.getAccountId() == null) {
                throw new CommonJsonException("获取管理员ID失败");
            }
            return tokenDTO.getAccountId();
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取当前管理员ID失败", e);
            throw new CommonJsonException("获取管理员ID失败");
        }
    }

    /**
     * 获取审核状态描述
     */
    private String getExamineTypeDesc(Integer examineType) {
        if (examineType == null) {
            return "未知";
        }
        switch (examineType) {
            case 0:
                return "待审核";
            case 1:
                return "审核通过";
            case 2:
                return "审核拒绝";
            default:
                return "未知";
        }
    }
}

