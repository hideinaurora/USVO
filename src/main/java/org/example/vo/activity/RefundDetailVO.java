package org.example.vo.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 退款学生详情VO
 *
 * @author ckd
 * @since 2026-03-13
 */
@Getter
@Setter
@Schema(description = "退款学生详情")
public class RefundDetailVO {

    @Schema(description = "退款申请ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "活动ID")
    private Long applyId;

    @Schema(description = "活动标题")
    private String applyTitle;

    @Schema(description = "商户订单号")
    private String merOrderId;

    @Schema(description = "退款单号")
    private String refundNo;

    @Schema(description = "外部退款单号")
    private String outRefundNo;

    @Schema(description = "退款金额（分）")
    private Integer refundAmount;

    @Schema(description = "退款金额（元）")
    private Double refundAmountYuan;

    @Schema(description = "实付金额（分）")
    private Integer totalAmount;

    @Schema(description = "实付金额（元）")
    private Double totalAmountYuan;

    @Schema(description = "退款时间")
    private LocalDateTime refundTime;

    @Schema(description = "审核状态：0-待审核，1-审核通过，2-审核拒绝")
    private Integer examineType;

    @Schema(description = "审核状态描述")
    private String examineTypeDesc;

    @Schema(description = "备注")
    private String bz;

    @Schema(description = "失败消息")
    private String failMessage;

    @Schema(description = "创建时间")
    private LocalDateTime gmtCreate;

    @Schema(description = "修改时间")
    private LocalDateTime gmtModify;

    // ========== 审核记录信息 ==========

    @Schema(description = "审核记录ID")
    private Long examineId;

    @Schema(description = "审核理由/说明")
    private String examineReason;

    @Schema(description = "审核管理员ID")
    private Long adminId;

    @Schema(description = "审核时间")
    private LocalDateTime examineTime;
}
