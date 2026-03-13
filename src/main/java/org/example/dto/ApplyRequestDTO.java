package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * 活动报名请求DTO
 *
 * @author ckd
 * @since 2026-03-13
 */
@Getter
@Setter
@Schema(description = "活动报名请求参数")
public class ApplyRequestDTO {

    @Schema(description = "活动ID", example = "1", required = true)
    @NotNull(message = "活动ID不能为空")
    private Long applyId;
}
