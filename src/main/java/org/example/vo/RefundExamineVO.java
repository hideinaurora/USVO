package org.example.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 退款审核记录VO
 *
 * @author ckd
 * @since 2026-03-13
 */
@Getter
@Setter
@Schema(description = "退款审核记录信息")
public class RefundExamineVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "审核记录ID", example = "1")
    private Long id;

    @Schema(description = "退款申请ID", example = "1")
    private Long refundId;

    @Schema(description = "审核结果：0-待审核，1-审核通过，2-审核拒绝", example = "1")
    private Integer examineType;

    @Schema(description = "审核理由/说明", example = "同意退款申请")
    private String reason;

    @Schema(description = "审核管理员ID", example = "1")
    private Long adminId;

    @Schema(description = "创建时间", example = "2026-03-13 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime gmtCreate;
}
