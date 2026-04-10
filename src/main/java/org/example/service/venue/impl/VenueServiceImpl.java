package org.example.service.venue.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.example.common.PageResult;
import org.example.config.exception.CommonJsonException;
import org.example.dto.OpResultDTO;
import org.example.dto.admin.CourtAddDTO;
import org.example.dto.admin.CourtUpdateDTO;
import org.example.dto.admin.VenueAddDTO;
import org.example.dto.admin.VenueUpdateDTO;
import org.example.entity.booking.BookingEntity;
import org.example.entity.CourtEntity;
import org.example.entity.VenueEntity;
import org.example.entity.basic.user.UserEntity;
import org.example.mapper.booking.BookingMapper;
import org.example.mapper.basic.user.UserMapper;
import org.example.mapper.venue.CourtMapper;
import org.example.mapper.venue.VenueMapper;
import org.example.service.venue.VenueService;
import org.example.vo.admin.BookingListItemVO;
import org.example.vo.admin.CourtListItemVO;
import org.example.vo.admin.VenueListItemVO;
import org.example.vo.venue.CourtSimpleVO;
import org.example.vo.venue.VenueCourtCountVO;
import org.example.vo.venue.VenueDetailVO;
import org.example.vo.venue.VenueListVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VenueServiceImpl implements VenueService {

    @Resource
    private VenueMapper venueMapper;

    @Resource
    private CourtMapper courtMapper;

    @Resource
    private BookingMapper bookingMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public List<VenueListVO> listVenues(String type, String keyword, Double latitude, Double longitude) {
        List<VenueEntity> venues = queryVenueEntities(type, keyword);
        if (venues == null || venues.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Long> courtCountMap = getCourtCountMap(venues);

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
    public PageResult<VenueListVO> pageVenues(Integer pageNum, Integer pageSize, String type, String keyword, Double latitude, Double longitude) {
        int pNum = (pageNum == null || pageNum <= 0) ? 1 : pageNum;
        int pSize = (pageSize == null || pageSize <= 0) ? 10 : pageSize;

        // 为了支持“按距离排序 + 正确分页”，这里先取全量结果做排序再截断分页
        List<VenueListVO> all = listVenues(type, keyword, latitude, longitude);
        if (all.isEmpty()) {
            return PageResult.of(pNum, pSize, 0L, Collections.emptyList());
        }

        long total = all.size();
        int fromIndex = Math.min((pNum - 1) * pSize, all.size());
        int toIndex = Math.min(fromIndex + pSize, all.size());
        List<VenueListVO> pageRecords = fromIndex >= toIndex ? Collections.emptyList() : all.subList(fromIndex, toIndex);
        return PageResult.of(pNum, pSize, total, pageRecords);
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

    private List<VenueEntity> queryVenueEntities(String type, String keyword) {
        QueryWrapper<VenueEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        if (StringUtils.isNotBlank(type)) {
            wrapper.eq("type", type);
        }
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like("name", keyword).or().like("address", keyword));
        }
        wrapper.orderByAsc("id");
        return venueMapper.selectList(wrapper);
    }

    private Map<Long, Long> getCourtCountMap(List<VenueEntity> venues) {
        List<Long> venueIds = venues.stream().map(VenueEntity::getId).filter(Objects::nonNull).collect(Collectors.toList());
        if (venueIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<VenueCourtCountVO> counts = courtMapper.selectVenueCourtCounts(venueIds);
        if (counts == null || counts.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Long> courtCountMap = new HashMap<>();
        for (VenueCourtCountVO c : counts) {
            if (c.getVenueId() != null) {
                courtCountMap.put(c.getVenueId(), c.getCourtCount() == null ? 0L : c.getCourtCount());
            }
        }
        return courtCountMap;
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

    @Override
    public Map<String, Object> getCourtPageList(Long venueId, String type, Integer status, Integer page, Integer size) {
        int pageNum = (page == null || page <= 0) ? 1 : page;
        int pageSize = (size == null || size <= 0) ? 10 : size;

        Page<CourtEntity> pageParam = new Page<>(pageNum, pageSize);
        QueryWrapper<CourtEntity> queryWrapper = new QueryWrapper<>();
        if (venueId != null) {
            queryWrapper.eq("venue_id", venueId);
        }
        if (StringUtils.isNotBlank(type)) {
            queryWrapper.eq("type", type);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByDesc("id");
        Page<CourtEntity> result = courtMapper.selectPage(pageParam, queryWrapper);
        List<CourtEntity> records = result.getRecords();

        List<Long> venueIds = records.stream().map(CourtEntity::getVenueId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, String> venueNameMap = new HashMap<>();
        if (!venueIds.isEmpty()) {
            List<VenueEntity> venues = venueMapper.selectBatchIds(venueIds);
            for (VenueEntity v : venues) {
                venueNameMap.put(v.getId(), v.getName());
            }
        }

        List<CourtListItemVO> list = records.stream().map(c -> {
            CourtListItemVO vo = new CourtListItemVO();
            vo.setId(c.getId());
            vo.setVenueId(c.getVenueId());
            vo.setVenueName(venueNameMap.get(c.getVenueId()));
            vo.setName(c.getName());
            vo.setType(c.getType());
            vo.setPricePerHour(c.getPricePerHour());
            vo.setStatus(c.getStatus());
            if (c.getCreateTime() != null) {
                vo.setCreateTime(c.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            return vo;
        }).collect(Collectors.toList());

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", result.getTotal());
        resultMap.put("page", pageNum);
        resultMap.put("size", pageSize);
        resultMap.put("list", list);
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addCourt(CourtAddDTO dto) {
        VenueEntity venue = venueMapper.selectById(dto.getVenueId());
        if (venue == null) {
            throw new CommonJsonException(new OpResultDTO(404L, "场馆不存在"));
        }
        CourtEntity court = new CourtEntity();
        court.setVenueId(dto.getVenueId());
        court.setName(dto.getName());
        court.setType(dto.getType());
        court.setPricePerHour(dto.getPricePerHour());
        court.setStatus(1);
        courtMapper.insert(court);
        return court.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourt(Long courtId, CourtUpdateDTO dto) {
        CourtEntity court = courtMapper.selectById(courtId);
        if (court == null) {
            throw new CommonJsonException(new OpResultDTO(404L, "场地不存在"));
        }
        LambdaUpdateWrapper<CourtEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(CourtEntity::getId, courtId);
        if (dto.getName() != null) {
            wrapper.set(CourtEntity::getName, dto.getName());
        }
        if (dto.getPricePerHour() != null) {
            wrapper.set(CourtEntity::getPricePerHour, dto.getPricePerHour());
        }
        if (dto.getStatus() != null) {
            wrapper.set(CourtEntity::getStatus, dto.getStatus());
        }
        courtMapper.update(null, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourt(Long courtId) {
        CourtEntity court = courtMapper.selectById(courtId);
        if (court == null) {
            throw new CommonJsonException(new OpResultDTO(404L, "场地不存在"));
        }
        courtMapper.deleteById(courtId);
    }

    @Override
    public List<VenueListItemVO> getVenueList() {
        QueryWrapper<VenueEntity> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("id");
        List<VenueEntity> venues = venueMapper.selectList(wrapper);
        if (venues == null || venues.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> venueIds = venues.stream().map(VenueEntity::getId).collect(Collectors.toList());
        Map<Long, Long> courtCountMap = new HashMap<>();
        List<VenueCourtCountVO> counts = courtMapper.selectVenueCourtCounts(venueIds);
        if (counts != null) {
            for (VenueCourtCountVO c : counts) {
                courtCountMap.put(c.getVenueId(), c.getCourtCount() == null ? 0L : c.getCourtCount());
            }
        }

        return venues.stream().map(v -> {
            VenueListItemVO vo = new VenueListItemVO();
            vo.setId(v.getId());
            vo.setName(v.getName());
            vo.setType(v.getType());
            vo.setAddress(v.getAddress());
            vo.setOpenTime(v.getOpenTime());
            vo.setCloseTime(v.getCloseTime());
            vo.setStatus(v.getStatus());
            vo.setCourtCount(courtCountMap.getOrDefault(v.getId(), 0L).intValue());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addVenue(VenueAddDTO dto) {
        VenueEntity venue = new VenueEntity();
        venue.setName(dto.getName());
        venue.setType(dto.getType());
        venue.setAddress(dto.getAddress());
        venue.setLatitude(dto.getLatitude());
        venue.setLongitude(dto.getLongitude());
        venue.setOpenTime(dto.getOpenTime());
        venue.setCloseTime(dto.getCloseTime());
        venue.setStatus(1);
        venueMapper.insert(venue);
        return venue.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateVenue(Long venueId, VenueUpdateDTO dto) {
        VenueEntity venue = venueMapper.selectById(venueId);
        if (venue == null) {
            throw new CommonJsonException(new OpResultDTO(404L, "场馆不存在"));
        }
        LambdaUpdateWrapper<VenueEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(VenueEntity::getId, venueId);
        if (dto.getName() != null) {
            wrapper.set(VenueEntity::getName, dto.getName());
        }
        if (dto.getType() != null) {
            wrapper.set(VenueEntity::getType, dto.getType());
        }
        if (dto.getAddress() != null) {
            wrapper.set(VenueEntity::getAddress, dto.getAddress());
        }
        if (dto.getLatitude() != null) {
            wrapper.set(VenueEntity::getLatitude, dto.getLatitude());
        }
        if (dto.getLongitude() != null) {
            wrapper.set(VenueEntity::getLongitude, dto.getLongitude());
        }
        if (dto.getOpenTime() != null) {
            wrapper.set(VenueEntity::getOpenTime, dto.getOpenTime());
        }
        if (dto.getCloseTime() != null) {
            wrapper.set(VenueEntity::getCloseTime, dto.getCloseTime());
        }
        if (dto.getStatus() != null) {
            wrapper.set(VenueEntity::getStatus, dto.getStatus());
        }
        venueMapper.update(null, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteVenue(Long venueId) {
        VenueEntity venue = venueMapper.selectById(venueId);
        if (venue == null) {
            throw new CommonJsonException(new OpResultDTO(404L, "场馆不存在"));
        }
        venueMapper.deleteById(venueId);
    }

    @Override
    public Map<String, Object> getBookingPageList(Integer status, String username, String courtName, String startDate, String endDate, Integer page, Integer size) {
        int pageNum = (page == null || page <= 0) ? 1 : page;
        int pageSize = (size == null || size <= 0) ? 10 : size;

        Page<BookingEntity> pageParam = new Page<>(pageNum, pageSize);
        QueryWrapper<BookingEntity> queryWrapper = new QueryWrapper<>();
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        if (StringUtils.isNotBlank(startDate)) {
            queryWrapper.ge("start_time", startDate);
        }
        if (StringUtils.isNotBlank(endDate)) {
            queryWrapper.le("start_time", endDate + " 23:59:59");
        }
        queryWrapper.orderByDesc("id");
        Page<BookingEntity> result = bookingMapper.selectPage(pageParam, queryWrapper);
        List<BookingEntity> records = result.getRecords();

        List<Long> courtIds = records.stream().map(BookingEntity::getCourtId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, CourtEntity> courtMap = new HashMap<>();
        if (!courtIds.isEmpty()) {
            List<CourtEntity> courts = courtMapper.selectBatchIds(courtIds);
            for (CourtEntity c : courts) {
                courtMap.put(c.getId(), c);
            }
        }

        List<Long> venueIds = courtMap.values().stream().map(CourtEntity::getVenueId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, VenueEntity> venueMap = new HashMap<>();
        if (!venueIds.isEmpty()) {
            List<VenueEntity> venues = venueMapper.selectBatchIds(venueIds);
            for (VenueEntity v : venues) {
                venueMap.put(v.getId(), v);
            }
        }

        List<Long> userIds = records.stream().map(BookingEntity::getUserId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, UserEntity> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<UserEntity> users = userMapper.selectBatchIds(userIds);
            for (UserEntity u : users) {
                userMap.put(u.getId(), u);
            }
        }

        List<BookingListItemVO> list = records.stream().map(b -> {
            BookingListItemVO vo = new BookingListItemVO();
            vo.setId(b.getId());
            UserEntity user = userMap.get(b.getUserId());
            vo.setUsername(user != null ? user.getUserName() : null);
            vo.setPhone(user != null ? user.getPhone() : null);
            CourtEntity court = courtMap.get(b.getCourtId());
            vo.setCourtName(court != null ? court.getName() : null);
            VenueEntity venue = court != null ? venueMap.get(court.getVenueId()) : null;
            vo.setVenueName(venue != null ? venue.getName() : null);
            vo.setStartTime(b.getStartTime());
            vo.setEndTime(b.getEndTime());
            vo.setTotalAmount(b.getTotalAmount());
            vo.setDepositAmount(b.getDepositAmount());
            vo.setStatus(b.getStatus());
            vo.setStatusText(getStatusText(b.getStatus()));
            vo.setCreateTime(b.getCreateTime());
            return vo;
        }).collect(Collectors.toList());

        if (StringUtils.isNotBlank(username)) {
            list = list.stream().filter(item -> item.getUsername() != null && item.getUsername().contains(username)).collect(Collectors.toList());
        }
        if (StringUtils.isNotBlank(courtName)) {
            list = list.stream().filter(item -> item.getCourtName() != null && item.getCourtName().contains(courtName)).collect(Collectors.toList());
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", result.getTotal());
        resultMap.put("page", pageNum);
        resultMap.put("size", pageSize);
        resultMap.put("list", list);
        return resultMap;
    }

    private String getStatusText(Integer status) {
        if (status == null) return null;
        switch (status) {
            case 0: return "待支付";
            case 1: return "已预约";
            case 2: return "已取消";
            case 3: return "已完成";
            case 4: return "违约";
            default: return "未知";
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBooking(Long bookingId) {
        BookingEntity booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new CommonJsonException(new OpResultDTO(404L, "预约不存在"));
        }
        bookingMapper.deleteById(bookingId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forceCancelBooking(Long bookingId, String reason) {
        BookingEntity booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new CommonJsonException(new OpResultDTO(404L, "预约不存在"));
        }
        if (booking.getStatus() == null || (booking.getStatus() != 1 && booking.getStatus() != 0)) {
            throw new CommonJsonException(new OpResultDTO(400L, "当前状态不可强制取消"));
        }
        LambdaUpdateWrapper<BookingEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(BookingEntity::getId, bookingId)
                .set(BookingEntity::getStatus, 2)
                .set(BookingEntity::getCancelTime, LocalDateTime.now());
        bookingMapper.update(null, wrapper);
    }
}
