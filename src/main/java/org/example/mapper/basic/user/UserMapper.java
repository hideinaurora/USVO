package org.example.mapper.basic.user;

import org.example.entity.basic.user.UserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户信息表 Mapper 接口
 * </p>
 *
 * @author ckd
 * @since 2026-03-13
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

}
