package org.example.service.activity.apply.impl;


import org.example.entity.activity.apply.ApplyUserEntity;
import org.example.mapper.activity.apply.ApplyUserMapper;
import org.example.service.activity.apply.ApplyUserService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 用户报名记录表 服务实现类
 * </p>
 *
 * @author ckd
 * @since 2026-03-13
 */
@Service
        public class ApplyUserServiceImpl extends ServiceImpl<ApplyUserMapper, ApplyUserEntity> implements ApplyUserService {
        }
