package org.example.service.venue.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.example.entity.CourtEntity;
import org.example.entity.VenueEntity;
import org.example.mapper.venue.CourtMapper;
import org.example.mapper.venue.VenueMapper;
import org.example.service.venue.VenueService;
import org.example.vo.venue.CourtSimpleVO;
import org.example.vo.venue.VenueCourtCountVO;
import org.example.vo.venue.VenueDetailVO;
import org.example.vo.venue.VenueListVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VenueServiceImpl implements VenueService {

    @Resource
    private VenueMapper venueMapper;

    @Resource
    private CourtMapper courtMapper;

    @Override
    public List<VenueListVO> listVenues(String type, String keyword, Double latitude, Double longitude) {
        QueryWrapper<VenueEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        if (StringUtils.isNotBlank(type)) {
            wrapper.eq("type", type);
        }
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like("name", keyword).or().like("address", keyword));
        }
        wrapper.orderByAsc("id");

        List<VenueEntity> venues = venueMapper.selectList(wrapper);
        if (venues == null || venues.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> venueIds = venues.stream().map(VenueEntity::getId).filter(Objects::nonNull).collect(Collectors.toList());
        Map<Long, Long> courtCountMap = new HashMap<>();
        if (!venueIds.isEmpty()) {
            List<VenueCourtCountVO> counts = courtMapper.selectVenueCourtCounts(venueIds);
            if (counts != null) {
                for (VenueCourtCountVO c : counts) {
                    if (c.getVenueId() != null) {
                        courtCountMap.put(c.getVenueId(), c.getCourtCount() == null ? 0L : c.getCourtCount());
                    }
                }
            }
        }

        // 计算距离
        boolean hasLocation = latitude != null && longitude != null;
        List<VenueListVO> list = venues.stream().map(v -> {
            VenueListVO vo = new VenueListVO();
            BeanUtils.copyProperties(v, vo);
            vo.setCourtCount(courtCountMap.getOrDefault(v.getId(), 0L));
            if (hasLocation && v.getLatitude() != null && v.getLongitude() != null) {
                double distKm = haversineKm(latitude, longitude, v.getLatitude().doubleValue(), v.getLongitude().doubleValue());
                vo.setDistance(BigDecimal.valueOf(distKm).setScale(2, RoundingMode.HALF_UP).doubleValue());
            } else {
                vo.setDistance(null);
            }
            return vo;
        }).collect(Collectors.toList());

        if (hasLocation) {
            list.sort(Comparator.comparing(VenueListVO::getDistance, Comparator.nullsLast(Double::compareTo)));
        }
        return list;
    }

    @Override
    public VenueDetailVO getVenueDetail(Long venueId) {
        if (venueId == null) {
            return null;
        }
        VenueEntity venue = venueMapper.selectById(venueId);
        if (venue == null || (venue.getStatus() != null && venue.getStatus() == 0)) {
            return null;
        }

        QueryWrapper<CourtEntity> courtWrapper = new QueryWrapper<>();
        courtWrapper.eq("venue_id", venueId);
        courtWrapper.eq("status", 1);
        courtWrapper.orderByAsc("id");
        List<CourtEntity> courts = courtMapper.selectList(courtWrapper);

        VenueDetailVO vo = new VenueDetailVO();
        BeanUtils.copyProperties(venue, vo);
        if (courts == null || courts.isEmpty()) {
            vo.setCourts(Collections.emptyList());
            return vo;
        }

        List<CourtSimpleVO> courtVos = courts.stream().map(c -> {
            CourtSimpleVO cvo = new CourtSimpleVO();
            cvo.setId(c.getId());
            cvo.setName(c.getName());
            cvo.setType(c.getType());
            cvo.setPricePerHour(c.getPricePerHour());
            return cvo;
        }).collect(Collectors.toList());
        vo.setCourts(courtVos);
        return vo;
    }

    /**
     * 计算两点间距离（单位：公里）
     */
    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
