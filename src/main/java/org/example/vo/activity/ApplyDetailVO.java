package org.example.vo.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 活动详情VO（包含统计信息）
 *
 * @author ckd
 * @since 2026-03-13
 */
@Getter
@Setter
@Schema(description = "活动详情（含统计信息）")
public class ApplyDetailVO {

    @Schema(description = "活动ID")
    private Long applyId;

    @Schema(description = "活动标题")
    private String applyTitle;

    @Schema(description = "报名开始时间")
    private LocalDateTime applyStartTime;

    @Schema(description = "报名结束时间")
    private LocalDateTime applyEndTime;

    @Schema(description = "报名费用（分）")
    private Integer applyExpense;

    @Schema(description = "活动详情")
    private String activeInfo;

    @Schema(description = "限制人数")
    private Integer limitNum;

    @Schema(description = "创建时间")
    private LocalDateTime gmtCreate;

    @Schema(description = "修改时间")
    private LocalDateTime gmtModify;

    @Schema(description = "报名成功人数（已支付）")
    private Integer paidUserCount;

    @Schema(description = "支付金额总数（分）")
    private Integer totalPayAmount;

    @Schema(description = "退款成功人数")
    private Integer refundSuccessCount;

    @Schema(description = "支付金额（元，转换后）")
    private Double totalPayAmountYuan;

    @Schema(description = "退款金额总数（分）")
    private Integer refundTotalAmount;

    @Schema(description = "退款金额总数（元，转换后）")
    private Double refundTotalAmountYuan;
}
