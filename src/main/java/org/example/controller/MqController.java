package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.common.ApiResponse;
import org.example.config.exception.CommonJsonException;
import org.example.dto.admin.RefundCallbackDTO;
import org.example.dto.admin.TimeoutCancelCallbackDTO;
import org.example.service.booking.BookingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/callback")
@Tag(name = "消息队列回调", description = "RabbitMQ延迟消息回调接口")
public class MqController {

    @Resource
    private BookingService bookingService;

//    @Operation(summary = "延迟退款回调", description = "RabbitMQ延迟消息触发的退款回调",
//            parameters = {
//                    @Parameter(name = "Authorization", description = "Bearer Token", required = false, in = ParameterIn.HEADER)
//            })
//    @PostMapping("/refund")
//    public ApiResponse<Void> refund(@Valid @RequestBody RefundCallbackDTO dto) {
//        try {
//            log.info("收到退款回调: bookingId={}, userId={}, amount={}, refundType={}",
//                    dto.getBookingId(), dto.getUserId(), dto.getAmount(), dto.getRefundType());
//            bookingService.refund(dto.getBookingId(), dto.getUserId(), dto.getAmount(), dto.getRefundType());
//            return ApiResponse.success("退款处理成功", null);
//        } catch (CommonJsonException e) {
//            throw e;
//        } catch (Exception e) {
//            log.error("退款回调处理异常", e);
//            throw new CommonJsonException("退款处理失败");
//        }
//    }

//    @Operation(summary = "超时未支付取消回调", description = "预约创建后15分钟未支付，自动取消",
//            parameters = {
//                    @Parameter(name = "Authorization", description = "Bearer Token", required = false, in = ParameterIn.HEADER)
//            })
//    @PostMapping("/timeout-cancel")
//    public ApiResponse<Void> timeoutCancel(@Valid @RequestBody TimeoutCancelCallbackDTO dto) {
//        try {
//            log.info("收到超时取消回调: bookingId={}", dto.getBookingId());
//            bookingService.timeoutCancel(dto.getBookingId());
//            return ApiResponse.success("已取消超时预约", null);
//        } catch (CommonJsonException e) {
//            throw e;
//        } catch (Exception e) {
//            log.error("超时取消回调处理异常", e);
//            throw new CommonJsonException("超时取消处理失败");
//        }
//    }
}