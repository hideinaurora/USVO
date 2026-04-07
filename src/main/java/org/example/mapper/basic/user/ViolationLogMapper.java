package org.example.mapper.basic.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.entity.basic.user.ViolationLogEntity;

@Mapper
public interface ViolationLogMapper extends BaseMapper<ViolationLogEntity> {
}

