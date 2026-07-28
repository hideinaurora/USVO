package org.example.mapper.basic.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.entity.basic.user.ViolationLogEntity;

import java.util.List;

@Mapper
public interface ViolationLogMapper extends BaseMapper<ViolationLogEntity> {

    @Select("SELECT * FROM violation_log WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit}")
    List<ViolationLogEntity> selectByUserId(@Param("userId") Long userId, @Param("limit") int limit);
}

