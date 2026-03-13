package org.example.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 活动报名响应VO
 *
 * @author ckd
 * @since 2026-03-13
 */
@Getter
@Setter
@Schema(description = "活动报名响应结果")
public class ApplyResponseVO {

    @Schema(description = "报名记录ID", example = "1")
    private Long enrollRecordId;

    @Schema(description = "支付订单ID", example = "1")
    private Long payOrderId;

    @Schema(description = "商户订单号", example = "MER20260313001")
    private String merOrderId;

    @Schema(description = "报名状态：0-待支付，1-已支付", example = "0")
    private Integer applyStatus;

    @Schema(description = "订单过期时间（分钟）", example = "30")
    private Integer expireMinutes;
}
