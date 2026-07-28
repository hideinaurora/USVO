package org.example.mapper.booking;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.entity.booking.BookingEntity;

import java.util.List;

@Mapper
public interface BookingMapper extends BaseMapper<BookingEntity> {

    @Select("SELECT * FROM booking WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit}")
    List<BookingEntity> selectByUserId(@Param("userId") Long userId, @Param("limit") int limit);
}
