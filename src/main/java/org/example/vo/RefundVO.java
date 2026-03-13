package org.example.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 退款记录VO
 *
 * @author ckd
 * @since 2026-03-13
 */
@Getter
@Setter
@Schema(description = "退款记录信息")
public class RefundVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "退款申请ID", example = "1")
    private Long id;

    @Schema(description = "商户订单号", example = "MER20260313001")
    private String merOrderId;

    @Schema(description = "审核类型：0-待审核，1-审核通过，2-审核拒绝", example = "1")
    private Integer examineType;

    @Schema(description = "退款单号", example = "REF20260313001")
    private String refundNo;

    @Schema(description = "退款金额（单位：分）", example = "9900")
    private Integer refundAmount;

    @Schema(description = "实付金额（单位：分）", example = "9900")
    private Integer totalAmount;

    @Schema(description = "外部退款单号", example = "OUT_REF20260313001")
    private String outRefundNo;

    @Schema(description = "退款时间", example = "2026-03-13 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime refundTime;

    @Schema(description = "备注", example = "用户申请退款")
    private String bz;

    @Schema(description = "失败消息", example = "退款失败")
    private String failMessage;

    @Schema(description = "创建时间", example = "2026-03-13 09:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime gmtCreate;

    @Schema(description = "审核记录信息")
    private RefundExamineVO examineRecord;
}
