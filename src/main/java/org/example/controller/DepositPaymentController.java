package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.aop.annotation.ApiAuth;
import org.example.aop.annotation.RequiresPermissions;
import org.example.common.ApiResponse;
import org.example.config.exception.CommonJsonException;
import org.example.dto.booking.PaymentPayDTO;
import org.example.service.TokenService;
import org.example.service.booking.BookingService;
import org.example.vo.booking.PaymentPayResultVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 用户端押金支付（与第三方支付回调 {@link PaymentCallbackController} 区分）
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
@Tag(name = "押金支付", description = "预约押金支付（模拟成功，未接真实支付渠道）")
public class DepositPaymentController {

    @Resource
    private BookingService bookingService;
    @Resource
    private TokenService tokenService;

    @Operation(summary = "支付押金", parameters = {
            @Parameter(name = "Authorization", description = "Bearer Token", required = true, in = ParameterIn.HEADER)
    })
    @PostMapping("/pay")
    @RequiresPermissions(value = "payment:pay", apiAuth = {ApiAuth.USER})
    public ApiResponse<PaymentPayResultVO> pay(@Valid @RequestBody PaymentPayDTO dto) {
        try {
            Long userId = tokenService.getUserId();
            PaymentPayResultVO vo = bookingService.payDeposit(userId, dto);
            return ApiResponse.success("支付成功", vo);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("支付押金失败", e);
            throw new CommonJsonException("支付押金失败");
        }
    }
}
