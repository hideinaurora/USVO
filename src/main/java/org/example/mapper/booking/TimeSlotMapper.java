package org.example.mapper.booking;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.entity.booking.TimeSlotEntity;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface TimeSlotMapper extends BaseMapper<TimeSlotEntity> {

    @Select("SELECT * FROM time_slot WHERE court_id = #{courtId} AND slot_date = #{slotDate} ORDER BY start_time")
    List<TimeSlotEntity> selectByCourtIdAndDate(@Param("courtId") Long courtId, @Param("slotDate") LocalDate slotDate);
}
