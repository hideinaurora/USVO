package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * 活动名额请求DTO
 *
 * @author ckd
 * @since 2026-03-13
 */
@Getter
@Setter
@Schema(description = "活动名额请求参数")
public class ApplyQuotaDTO {

    @Schema(description = "活动ID", example = "1", required = true)
    @NotNull(message = "活动ID不能为空")
    private Long applyId;

    @Schema(description = "增加的名额数量", example = "1", required = true)
    @NotNull(message = "增加的名额数量不能为空")
    private Long quantity;
}
