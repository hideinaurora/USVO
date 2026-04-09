package org.example.mapper.booking;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.entity.booking.BookingEntity;

@Mapper
public interface BookingMapper extends BaseMapper<BookingEntity> {
}
