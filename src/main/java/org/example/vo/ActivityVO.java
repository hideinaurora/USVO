package org.example.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 活动列表VO
 *
 * @author ckd
 * @since 2026-03-13
 */
@Getter
@Setter
@Schema(description = "活动信息")
public class ActivityVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "活动ID", example = "1")
    private Long applyId;

    @Schema(description = "活动标题", example = "春季书法培训班")
    private String applyTitle;

    @Schema(description = "报名开始时间", example = "2026-03-01 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime applyStartTime;

    @Schema(description = "报名结束时间", example = "2026-03-31 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime applyEndTime;

    @Schema(description = "报名费用（单位：分）", example = "9900")
    private Integer applyExpense;

    @Schema(description = "活动详情", example = "本次活动主要面向...")
    private String activeInfo;

    @Schema(description = "限制人数", example = "50")
    private Integer limitNum;

    @Schema(description = "报名状态：0-未报名，1-待支付，2-支付完成", example = "0")
    private Integer applyStatus;

    @Schema(description = "支付订单信息（仅在待支付状态时有值）")
    private ApplyPayVO applyPay;
}
