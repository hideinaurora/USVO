package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * 微信登录请求DTO
 *
 * @author ckd
 * @since 2026-03-13
 */
@Getter
@Setter
@Schema(description = "微信登录请求参数")
public class WxLoginRequestDTO {

    @Schema(description = "微信小程序code", example = "071abc2w3def456", required = true)
    @NotBlank(message = "微信code不能为空")
    private String code;
}
