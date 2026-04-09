package org.example.mapper.booking;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.entity.booking.TimeSlotEntity;

@Mapper
public interface TimeSlotMapper extends BaseMapper<TimeSlotEntity> {
}
