package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.aop.annotation.ApiAuth;
import org.example.aop.annotation.RequiresPermissions;
import org.example.common.ApiResponse;
import org.example.common.PageResult;
import org.example.config.exception.CommonJsonException;
import org.example.dto.booking.BookingCancelDTO;
import org.example.dto.booking.BookingCheckinDTO;
import org.example.dto.booking.BookingCreateDTO;
import org.example.service.TokenService;
import org.example.service.booking.BookingService;
import org.example.vo.booking.BookingCreateResultVO;
import org.example.vo.booking.BookingDetailVO;
import org.example.vo.booking.BookingListItemVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 预约模块
 */
@Slf4j
@RestController
@RequestMapping("/api/booking")
@Tag(name = "预约模块", description = "创建预约、取消、列表、详情、签到（不含锁与MQ）")
public class BookingController {

    @Resource
    private BookingService bookingService;
    @Resource
    private TokenService tokenService;

    @Operation(summary = "创建预约", description = "占用时间片并生成待支付预约（不含悲观锁与超时MQ）",
            parameters = {
                    @Parameter(name = "Authorization", description = "Bearer Token", required = true, in = ParameterIn.HEADER)
            })
    @PostMapping("/create")
    @RequiresPermissions(value = "booking:create", apiAuth = {ApiAuth.USER})
    public ApiResponse<BookingCreateResultVO> create(@Valid @RequestBody BookingCreateDTO dto) {
        try {
            Long userId = tokenService.getUserId();
            BookingCreateResultVO vo = bookingService.createBooking(userId, dto);
            return ApiResponse.success("预约创建成功，请支付押金", vo);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建预约失败", e);
            throw new CommonJsonException("创建预约失败");
        }
    }

    @Operation(summary = "取消预约", description = "取消预约并释放时间片；已支付时同步将支付记录置为已退款（未接延迟退款MQ）",
            parameters = {
                    @Parameter(name = "Authorization", description = "Bearer Token", required = true, in = ParameterIn.HEADER)
            })
    @PostMapping("/cancel")
    @RequiresPermissions(value = "booking:cancel", apiAuth = {ApiAuth.USER})
    public ApiResponse<Void> cancel(@Valid @RequestBody BookingCancelDTO dto) {
        try {
            Long userId = tokenService.getUserId();
            bookingService.cancelBooking(userId, dto);
            return ApiResponse.success("取消成功，押金将退回原账户", null);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("取消预约失败", e);
            throw new CommonJsonException("取消预约失败");
        }
    }

    @Operation(summary = "获取我的预约记录", parameters = {
            @Parameter(name = "Authorization", description = "Bearer Token", required = true, in = ParameterIn.HEADER)
    })
    @GetMapping("/list")
    @RequiresPermissions(value = "booking:list", apiAuth = {ApiAuth.USER})
    public ApiResponse<PageResult<BookingListItemVO>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        try {
            Long userId = tokenService.getUserId();
            int pageNum = (page == null || page <= 0) ? 1 : page;
            int pageSize = (size == null || size <= 0) ? 10 : size;
            PageResult<BookingListItemVO> result = bookingService.pageMyBookings(userId, status, pageNum, pageSize);
            return ApiResponse.success(result);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询预约列表失败", e);
            throw new CommonJsonException("查询预约列表失败");
        }
    }

    @Operation(summary = "获取预约详情", parameters = {
            @Parameter(name = "Authorization", description = "Bearer Token", required = true, in = ParameterIn.HEADER)
    })
    @GetMapping("/{bookingId}")
    @RequiresPermissions(value = "booking:detail", apiAuth = {ApiAuth.USER})
    public ApiResponse<BookingDetailVO> detail(@PathVariable Long bookingId) {
        try {
            Long userId = tokenService.getUserId();
            BookingDetailVO vo = bookingService.getBookingDetail(userId, bookingId);
            if (vo == null) {
                return ApiResponse.error(404, "预约不存在");
            }
            return ApiResponse.success(vo);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询预约详情失败", e);
            throw new CommonJsonException("查询预约详情失败");
        }
    }

    @Operation(summary = "签到入场", parameters = {
            @Parameter(name = "Authorization", description = "Bearer Token", required = true, in = ParameterIn.HEADER)
    })
    @PostMapping("/checkin")
    @RequiresPermissions(value = "booking:checkin", apiAuth = {ApiAuth.USER})
    public ApiResponse<Void> checkin(@Valid @RequestBody BookingCheckinDTO dto) {
        try {
            Long userId = tokenService.getUserId();
            bookingService.checkin(userId, dto);
            return ApiResponse.success("签到成功，祝您运动愉快", null);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("签到失败", e);
            throw new CommonJsonException("签到失败");
        }
    }
}
