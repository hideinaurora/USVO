package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 第三方支付回调请求DTO
 * 参考支付宝/微信支付回调参数设计
 *
 * @author ckd
 * @since 2026-03-13
 */
@Getter
@Setter
@Schema(description = "支付回调请求参数")
public class PaymentCallbackDTO {

    @Schema(description = "商户订单号（支付平台的out_trade_no）", example = "MER20260313143000123456", required = true)
    @NotBlank(message = "商户订单号不能为空")
    private String outTradeNo;

    @Schema(description = "支付平台交易号（支付宝trade_no/微信transaction_id）", example = "2026031322001478945678901234", required = true)
    @NotBlank(message = "支付平台交易号不能为空")
    private String tradeNo;

    @Schema(description = "交易状态：TRADE_SUCCESS-交易支付成功，WAIT_BUYER_PAY-交易创建，TRADE_CLOSED-未付款交易超时关闭", example = "TRADE_SUCCESS", required = true)
    @NotBlank(message = "交易状态不能为空")
    private String tradeStatus;

    @Schema(description = "交易金额（单位：元）", example = "99.00", required = true)
    @NotNull(message = "交易金额不能为空")
    private String totalAmount;

    @Schema(description = "支付时间", example = "2026-03-13 14:30:00")
    private String gmtPayment;

    @Schema(description = "买家支付账号", example = "user@example.com")
    private String buyerLogonId;

    @Schema(description = "应用ID", example = "2021001234567890")
    private String appId;

    @Schema(description = "通知时间", example = "2026-03-13 14:30:05")
    private String notifyTime;

    @Schema(description = "通知类型", example = "trade_status_sync")
    private String notifyType;

    @Schema(description = "签名类型", example = "RSA2")
    private String signType;
}
