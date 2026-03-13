package org.example.service.activity.refund.impl;


import org.example.entity.activity.refund.RefundEntity;
import org.example.mapper.activity.refund.RefundMapper;
import org.example.service.activity.refund.RefundService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 退款申请表 服务实现类
 * </p>
 *
 * @author ckd
 * @since 2026-03-13
 */
@Service
        public class RefundServiceImpl extends ServiceImpl<RefundMapper, RefundEntity> implements RefundService {
        }
