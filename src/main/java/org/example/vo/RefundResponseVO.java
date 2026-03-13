package org.example.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 申请退款响应VO
 *
 * @author ckd
 * @since 2026-03-13
 */
@Getter
@Setter
@Schema(description = "申请退款响应结果")
public class RefundResponseVO {

    @Schema(description = "退款申请ID", example = "1")
    private Long refundId;

    @Schema(description = "退款单号", example = "REF202603131430001")
    private String refundNo;

    @Schema(description = "审核状态：0-待审核，1-审核通过，2-审核拒绝", example = "0")
    private Integer examineType;

    @Schema(description = "是否为重新申请", example = "false")
    private Boolean isReapply;
}
