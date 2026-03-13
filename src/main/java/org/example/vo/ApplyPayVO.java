package org.example.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 支付信息VO
 *
 * @author ckd
 * @since 2026-03-13
 */
@Getter
@Setter
@Schema(description = "支付订单信息")
public class ApplyPayVO {

    @Schema(description = "订单ID", example = "1")
    private Long id;

    @Schema(description = "支付状态：0-未支付，1-已支付", example = "0")
    private Integer payStatus;

    @Schema(description = "商户订单号", example = "MER20260313001")
    private String merOrderId;

    @Schema(description = "订单描述", example = "活动报名费")
    private String orderDesc;

    @Schema(description = "订单过期时间", example = "2026-03-14 12:00:00")
    private String expireTime;

    @Schema(description = "原始金额（单位：分）", example = "9900")
    private Integer originalAmount;

    @Schema(description = "总金额（单位：分）", example = "9900")
    private Integer totalAmount;

    @Schema(description = "商户名称", example = "某某机构")
    private String merName;

    @Schema(description = "序列号/流水号", example = "SEQ123456")
    private String seqId;

    @Schema(description = "报名申请ID", example = "1")
    private Long applyId;

    @Schema(description = "报名学生ID", example = "1")
    private Long applyStudentId;
}
