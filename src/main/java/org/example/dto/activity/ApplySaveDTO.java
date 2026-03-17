package org.example.dto.activity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 活动新增/修改DTO
 *
 * @author ckd
 * @since 2026-03-13
 */
@Getter
@Setter
@Schema(description = "活动新增/修改请求参数")
public class ApplySaveDTO {

    @Schema(description = "活动ID（修改时必填）", example = "1")
    private Long applyId;

    @Schema(description = "活动标题", example = "春季篮球训练营", required = true)
    @NotBlank(message = "活动标题不能为空")
    private String applyTitle;

    @Schema(description = "报名开始时间", example = "2026-03-15 09:00:00", required = true)
    @NotNull(message = "报名开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime applyStartTime;

    @Schema(description = "报名结束时间", example = "2026-03-20 18:00:00", required = true)
    @NotNull(message = "报名结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime applyEndTime;

    @Schema(description = "报名费用（分）", example = "9900", required = true)
    @NotNull(message = "报名费用不能为空")
    private BigDecimal applyExpenseY;

    @Schema(description = "活动详情", example = "本次活动旨在...")
    private String activeInfo;

    @Schema(description = "限制人数", example = "100")
    private Integer limitNum;
}
