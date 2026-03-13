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
}

