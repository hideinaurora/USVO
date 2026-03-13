package org.example.dto.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * 退款审核请求DTO
 *
 * @author ckd
 * @since 2026-03-13
 */
@Getter
@Setter
@Schema(description = "退款审核请求参数")
public class RefundExamineDTO {

    @Schema(description = "退款申请ID", required = true, example = "1")
    @NotNull(message = "退款申请ID不能为空")
    private Long refundId;

    @Schema(description = "审核类型：1-审核通过，2-审核拒绝", required = true, example = "1")
    @NotNull(message = "审核类型不能为空")
    private Integer examineType;

    @Schema(description = "审核理由/说明", example = "用户提供了相关证明材料，同意退款申请")
    private String reason;
}
