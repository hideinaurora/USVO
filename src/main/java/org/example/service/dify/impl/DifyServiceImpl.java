package org.example.service.dify.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.dto.dify.DifyQueryRequestDTO;
import org.example.entity.CourtEntity;
import org.example.entity.VenueEntity;
import org.example.entity.basic.user.UserEntity;
import org.example.entity.basic.user.ViolationLogEntity;
import org.example.entity.booking.BookingEntity;
import org.example.entity.booking.TimeSlotEntity;
import org.example.mapper.venue.CourtMapper;
import org.example.mapper.venue.VenueMapper;
import org.example.mapper.basic.user.UserMapper;
import org.example.mapper.basic.user.ViolationLogMapper;
import org.example.mapper.booking.BookingMapper;
import org.example.mapper.booking.TimeSlotMapper;
import org.example.service.dify.DifyService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DifyServiceImpl implements DifyService {

    @Resource
    private VenueMapper venueMapper;

    @Resource
    private CourtMapper courtMapper;

    @Resource
    private TimeSlotMapper timeSlotMapper;

    @Resource
    private BookingMapper bookingMapper;

    @Resource
    private ViolationLogMapper violationLogMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public Object query(DifyQueryRequestDTO request) {
        String queryType = request.getQueryType();
        log.info("Dify查询请求 - queryType: {}, userId: {}", queryType, request.getUserId());

        try {
            Object result;
            if ("venue_info".equals(queryType)) {
                result = queryVenueInfo(request.getParams());
            } else if ("court_info".equals(queryType)) {
                result = queryCourtInfo(request.getParams());
            } else if ("court_availability".equals(queryType)) {
                result = queryCourtAvailability(request.getParams());
            } else if ("pricing".equals(queryType)) {
                result = queryPricing(request.getParams());
            } else if ("booking_rules".equals(queryType)) {
                result = queryBookingRules(request.getParams());
            } else if ("user_bookings".equals(queryType)) {
                result = queryUserBookings(request.getParams());
            } else if ("user_violations".equals(queryType)) {
                result = queryUserViolations(request.getParams());
            } else {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("code", 400);
                errorResult.put("message", "未知的查询类型: " + queryType);
                result = errorResult;
            }
            return result;
        } catch (Exception e) {
            log.error("Dify查询失败 - queryType: {}", queryType, e);
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("code", 500);
            errorMap.put("message", "查询失败: " + e.getMessage());
            return errorMap;
        }
    }

    // 1. 场馆信息查询
    private Object queryVenueInfo(DifyQueryRequestDTO.QueryParams params) {
        List<VenueEntity> venues = venueMapper.selectList(null);
        
        // 过滤
        if (params != null) {
            if (StringUtils.isNotBlank(params.getKeyword())) {
                String keyword = params.getKeyword().toLowerCase();
                venues = venues.stream()
                        .filter(v -> v.getName().toLowerCase().contains(keyword)
                                || (v.getAddress() != null && v.getAddress().toLowerCase().contains(keyword)))
                        .collect(Collectors.toList());
            }
            if (StringUtils.isNotBlank(params.getType())) {
                venues = venues.stream()
                        .filter(v -> params.getType().equals(v.getType()))
                        .collect(Collectors.toList());
            }
        }

        List<Map<String, Object>> result = venues.stream().map(v -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("venue_id", v.getId());
            item.put("venue_name", v.getName());
            item.put("type", v.getType());
            item.put("address", v.getAddress());
            item.put("open_time", v.getOpenTime());
            item.put("close_time", v.getCloseTime());
            item.put("preview_url", v.getPreviewUrl());
            item.put("status", v.getStatus());
            return item;
        }).collect(Collectors.toList());

        return result;
    }

    // 2. 场地信息查询
    private Object queryCourtInfo(DifyQueryRequestDTO.QueryParams params) {
        if (params == null || params.getVenueId() == null) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("code", 400);
            errorMap.put("message", "缺少必填参数: venue_id");
            return errorMap;
        }

        List<CourtEntity> courts = courtMapper.selectByVenueId(params.getVenueId());
        VenueEntity venue = venueMapper.selectById(params.getVenueId());

        if (params.getCourtType() != null) {
            courts = courts.stream()
                    .filter(c -> params.getCourtType().equals(c.getType()))
                    .collect(Collectors.toList());
        }

        List<Map<String, Object>> result = courts.stream().map(c -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("court_id", c.getId());
            item.put("court_name", c.getName());
            item.put("court_type", c.getType());
            item.put("price_per_hour", c.getPricePerHour());
            item.put("preview_url", c.getPreviewUrl());
            item.put("venue_name", venue != null ? venue.getName() : null);
            return item;
        }).collect(Collectors.toList());

        return result;
    }

    // 3. 场地空闲时段查询
    private Object queryCourtAvailability(DifyQueryRequestDTO.QueryParams params) {
        if (params == null || params.getCourtId() == null || StringUtils.isBlank(params.getSlotDate())) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("code", 400);
            errorMap.put("message", "缺少必填参数: court_id 或 slot_date");
            return errorMap;
        }

        CourtEntity court = courtMapper.selectById(params.getCourtId());
        if (court == null) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("code", 404);
            errorMap.put("message", "未找到该场地");
            return errorMap;
        }

        LocalDate date = LocalDate.parse(params.getSlotDate());
        List<TimeSlotEntity> slots = timeSlotMapper.selectByCourtIdAndDate(params.getCourtId(), date);

        List<Map<String, Object>> availableSlots = new ArrayList<>();
        List<Map<String, Object>> bookedSlots = new ArrayList<>();
        List<Map<String, Object>> lockedSlots = new ArrayList<>();

        for (TimeSlotEntity slot : slots) {
            Map<String, Object> slotMap = new LinkedHashMap<>();
            slotMap.put("slot_id", slot.getId());
            slotMap.put("start_time", slot.getStartTime());
            slotMap.put("end_time", slot.getEndTime());
            slotMap.put("status", slot.getStatus());

            if (slot.getStatus() == 0) {
                // 0: 可预约
                availableSlots.add(slotMap);
            } else if (slot.getStatus() == 1) {
                // 1: 已预约
                slotMap.put("booking_id", slot.getBookingId());
                bookedSlots.add(slotMap);
            } else if (slot.getStatus() == 2) {
                // 2: 已锁定
                lockedSlots.add(slotMap);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("court_id", court.getId());
        result.put("court_name", court.getName());
        result.put("slot_date", params.getSlotDate());
        result.put("available_slots", availableSlots);
        result.put("booked_slots", bookedSlots);
        result.put("locked_slots", lockedSlots);

        return result;
    }

    // 4. 价格查询
    private Object queryPricing(DifyQueryRequestDTO.QueryParams params) {
        List<CourtEntity> courts = new ArrayList<>();

        if (params != null) {
            if (params.getCourtId() != null) {
                CourtEntity court = courtMapper.selectById(params.getCourtId());
                if (court != null) courts.add(court);
            } else if (StringUtils.isNotBlank(params.getCourtType())) {
                courts = courtMapper.selectByType(params.getCourtType());
            } else if (params.getVenueId() != null) {
                courts = courtMapper.selectByVenueId(params.getVenueId());
            }
        }

        if (courts.isEmpty()) {
            courts = courtMapper.selectList(null);
        }

        final List<CourtEntity> finalCourts = courts;
        List<Map<String, Object>> result = courts.stream().map(c -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("court_id", c.getId());
            item.put("court_name", c.getName());
            item.put("court_type", c.getType());
            item.put("price_per_hour", c.getPricePerHour());
            VenueEntity venue = venueMapper.selectById(c.getVenueId());
            item.put("venue_name", venue != null ? venue.getName() : null);
            return item;
        }).collect(Collectors.toList());

        return result;
    }

    // 5. 预约规则查询
    private Object queryBookingRules(DifyQueryRequestDTO.QueryParams params) {
        Map<String, Object> booking = new LinkedHashMap<>();
        booking.put("advance_days", 3);
        booking.put("max_duration_hours", 2);
        booking.put("max_slots_per_day", 2);
        booking.put("deposit_percent", 40);
        booking.put("description", "可提前3天预约，单次最长2小时，每天最多预约2个时段，需支付40%定金");

        Map<String, Object> cancel = new LinkedHashMap<>();
        cancel.put("advance_hours", 4);
        cancel.put("penalty", "提前4小时内取消或无理由爽约，记录1次违约");

        Map<String, Object> violation = new LinkedHashMap<>();
        violation.put("threshold", 3);
        violation.put("penalty", "累计违约3次，冻结预约权限30天");

        Map<String, Object> checkin = new LinkedHashMap<>();
        checkin.put("grace_minutes", 30);
        checkin.put("no_show_penalty", "预约开始后30分钟内未签到，自动标记为爽约");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("booking", booking);
        result.put("cancel", cancel);
        result.put("violation", violation);
        result.put("checkin", checkin);

        return result;
    }

    // 6. 用户历史预约查询
    private Object queryUserBookings(DifyQueryRequestDTO.QueryParams params) {
        if (params == null || params.getUserId() == null) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("code", 400);
            errorMap.put("message", "缺少必填参数: user_id");
            return errorMap;
        }

        int limit = params.getLimit() != null ? params.getLimit() : 10;
        List<BookingEntity> bookings = bookingMapper.selectByUserId(params.getUserId(), limit);

        List<Map<String, Object>> bookingList = bookings.stream().map(b -> {
            CourtEntity court = courtMapper.selectById(b.getCourtId());
            VenueEntity venue = court != null ? venueMapper.selectById(court.getVenueId()) : null;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("booking_id", b.getId());
            item.put("court_name", court != null ? court.getName() : null);
            item.put("venue_name", venue != null ? venue.getName() : null);
            item.put("start_time", b.getStartTime());
            item.put("end_time", b.getEndTime());
            item.put("total_amount", b.getTotalAmount());
            item.put("deposit_amount", b.getDepositAmount());
            item.put("status", b.getStatus());
            item.put("status_desc", getStatusDesc(b.getStatus()));
            item.put("checkin_time", b.getCheckinTime());
            item.put("cancel_time", b.getCancelTime());
            return item;
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", bookings.size());
        result.put("bookings", bookingList);

        return result;
    }

    // 7. 用户违约记录查询
    private Object queryUserViolations(DifyQueryRequestDTO.QueryParams params) {
        if (params == null || params.getUserId() == null) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("code", 400);
            errorMap.put("message", "缺少必填参数: user_id");
            return errorMap;
        }

        int limit = params.getLimit() != null ? params.getLimit() : 10;
        UserEntity user = userMapper.selectById(params.getUserId());
        if (user == null) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("code", 404);
            errorMap.put("message", "未找到该用户");
            return errorMap;
        }

        List<ViolationLogEntity> violations = violationLogMapper.selectByUserId(params.getUserId(), limit);

        List<Map<String, Object>> violationList = violations.stream().map(v -> {
            BookingEntity booking = bookingMapper.selectById(v.getBookingId());
            CourtEntity court = booking != null ? courtMapper.selectById(booking.getCourtId()) : null;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", v.getId());
            item.put("booking_id", v.getBookingId());
            item.put("reason", v.getReason());
            item.put("penalty_amount", v.getPenaltyAmount());
            item.put("create_time", v.getCreateTime());
            item.put("court_name", court != null ? court.getName() : null);
            item.put("booking_time", booking != null ? booking.getStartTime() : null);
            return item;
        }).collect(Collectors.toList());

        // 计算信用分
        int violationCount = violations.size();
        int creditScore = Math.max(0, 100 - violationCount * 10);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("user_id", user.getId());
        result.put("username", user.getUserName());
        result.put("violation_count", violationCount);
        result.put("credit_score", creditScore);
        result.put("violations", violationList);

        return result;
    }

    private String getStatusDesc(Integer status) {
        if (status == null) return "未知";
        if (status == 0) return "待支付";
        if (status == 1) return "已预约";
        if (status == 2) return "已取消";
        if (status == 3) return "已完成";
        if (status == 4) return "违约";
        return "未知";
    }
}
