package org.example.service.sys.admin.impl;


import org.example.entity.sys.admin.AdminEntity;
import org.example.mapper.sys.admin.AdminMapper;
import org.example.service.sys.admin.AdminService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 系统用户表 服务实现类
 * </p>
 *
 * @author ckd
 * @since 2026-03-13
 */
@Service
        public class AdminServiceImpl extends ServiceImpl<AdminMapper, AdminEntity> implements AdminService {
        }
