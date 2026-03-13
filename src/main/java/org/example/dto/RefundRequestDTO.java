package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * 申请退款请求DTO
 *
 * @author ckd
 * @since 2026-03-13
 */
@Getter
@Setter
@Schema(description = "申请退款请求参数")
public class RefundRequestDTO {

    @Schema(description = "订单号", example = "1", required = true)
    @NotBlank(message = "订单号不能为空")
    private String merOrderId;

    @Schema(description = "退款原因", example = "行程冲突，无法参加", required = true)
    private String reason;
}
