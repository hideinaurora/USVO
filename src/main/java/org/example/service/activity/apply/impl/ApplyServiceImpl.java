package org.example.service.activity.apply.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.common.PageResult;
import org.example.config.exception.CommonJsonException;
import org.example.dto.activity.ApplyQueryDTO;
import org.example.dto.activity.ApplySaveDTO;
import org.example.dto.activity.ApplyUserQueryDTO;
import org.example.entity.activity.apply.ApplyEntity;
import org.example.entity.activity.apply.ApplyPayEntity;
import org.example.entity.activity.apply.ApplyUserEntity;
import org.example.entity.activity.refund.RefundEntity;
import org.example.entity.basic.user.UserEntity;
import org.example.mapper.activity.apply.ApplyMapper;
import org.example.service.activity.apply.ApplyPayService;
import org.example.service.activity.apply.ApplyService;
import org.example.service.activity.apply.ApplyUserService;
import org.example.service.activity.refund.RefundService;
import org.example.service.basic.user.UserService;
import org.example.utils.StringTools;
import org.example.vo.activity.ApplyDetailVO;
import org.example.vo.activity.ApplyUserDetailVO;
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
}
