package org.example.service.venue;

import org.example.vo.venue.VenueDetailVO;
import org.example.vo.venue.VenueListVO;
import org.example.common.PageResult;

import java.util.List;

public interface VenueService {

    /**
     * 获取场馆列表（支持按类型、关键词筛选；可选距离排序）
     */
    List<VenueListVO> listVenues(String type, String keyword, Double latitude, Double longitude);

    /**
     * 场馆分页查询（支持按类型、关键词筛选；可选距离排序）
     */
    PageResult<VenueListVO> pageVenues(Integer pageNum, Integer pageSize, String type, String keyword, Double latitude, Double longitude);

    /**
     * 获取场馆详情（包含场地列表）
     */
    VenueDetailVO getVenueDetail(Long venueId);
}
