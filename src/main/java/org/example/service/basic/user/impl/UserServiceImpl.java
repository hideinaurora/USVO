package org.example.service.basic.user.impl;


import org.example.entity.basic.user.UserEntity;
import org.example.mapper.basic.user.UserMapper;
import org.example.service.basic.user.UserService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 用户信息表 服务实现类
 * </p>
 *
 * @author ckd
 * @since 2026-03-13
 */
@Service
        public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {
        }
