package org.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.mapper.TUserMapper;
import org.example.entity.TUserEntity;
import org.example.service.TUserService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 吴子木
 * @since 2024-09-23
 */
@Service
public class TUserServiceImpl extends ServiceImpl<TUserMapper, TUserEntity> implements TUserService {

}
