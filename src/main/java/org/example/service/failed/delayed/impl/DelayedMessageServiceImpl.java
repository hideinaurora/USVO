package org.example.service.failed.delayed.impl;


import org.example.entity.failed.delayed.DelayedMessageEntity;
import org.example.mapper.failed.delayed.DelayedMessageMapper;
import org.example.service.failed.delayed.DelayedMessageService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 延迟消息失败记录表 服务实现类
 * </p>
 *
 * @author ckd
 * @since 2026-03-13
 */
@Service
        public class DelayedMessageServiceImpl extends ServiceImpl<DelayedMessageMapper, DelayedMessageEntity> implements DelayedMessageService {
        }
