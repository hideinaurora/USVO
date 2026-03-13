package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 移动端用户登录请求DTO
 *
 * @author ckd
 * @since 2026-03-13
 */
@Getter
@Setter
@Schema(description = "移动端用户登录请求参数")
public class AppLoginRequestDTO {

    @Schema(description = "登录名", example = "user001", required = true)
    private String loginName;

    @Schema(description = "登录密码（明文传输，后端会进行MD5加密）", example = "123456", required = true)
    private String loginPassword;
}
