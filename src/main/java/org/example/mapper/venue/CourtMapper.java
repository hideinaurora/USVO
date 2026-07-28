package org.example.mapper.venue;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.entity.CourtEntity;
import org.example.vo.venue.VenueCourtCountVO;

import java.util.List;

@Mapper
public interface CourtMapper extends BaseMapper<CourtEntity> {

    @Select({
            "<script>",
            "select venue_id as venueId, count(1) as courtCount",
            "from court",
            "where venue_id in",
            "<foreach collection='venueIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "group by venue_id",
            "</script>"
    })
    List<VenueCourtCountVO> selectVenueCourtCounts(@Param("venueIds") List<Long> venueIds);

    @Select("SELECT * FROM court WHERE venue_id = #{venueId}")
    List<CourtEntity> selectByVenueId(@Param("venueId") Long venueId);

    @Select("SELECT * FROM court WHERE type = #{type}")
    List<CourtEntity> selectByType(@Param("type") String type);
}

