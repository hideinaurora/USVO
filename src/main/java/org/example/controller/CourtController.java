package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.common.ApiResponse;
import org.example.config.exception.CommonJsonException;
import org.example.service.booking.BookingService;
import org.example.vo.booking.AvailableSlotVO;
import org.example.vo.booking.CourtSlotsVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 场地时间片查询（预约模块配套）
 */
@Slf4j
@RestController
@RequestMapping("/api/court")
@Tag(name = "场地时间片", description = "可预约时间片查询")
public class CourtController {

    @Resource
    private BookingService bookingService;

    @Operation(summary = "获取可预约时间片", description = "按场地与日期查询当日时间片列表")
    @GetMapping("/{courtId}/slots")
    public ApiResponse<CourtSlotsVO> slots(
            @Parameter(description = "场地ID", required = true) @PathVariable Long courtId,
            @Parameter(description = "日期 YYYY-MM-DD", required = true) @RequestParam String date
    ) {
        try {
            return ApiResponse.success(bookingService.getCourtSlots(courtId, date));
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询时间片失败", e);
            throw new CommonJsonException("查询时间片失败");
        }
    }

    @Operation(summary = "获取可预约时间（批量）", description = "可按开始/结束时间过滤")
    @GetMapping("/available-slots")
    public ApiResponse<List<AvailableSlotVO>> availableSlots(
            @RequestParam("court_id") Long courtId,
            @RequestParam String date,
            @RequestParam(required = false) String start_time,
            @RequestParam(required = false) String end_time
    ) {
        try {
            return ApiResponse.success(bookingService.getAvailableSlots(courtId, date, start_time, end_time));
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量查询可预约时间失败", e);
            throw new CommonJsonException("批量查询可预约时间失败");
        }
    }
}
