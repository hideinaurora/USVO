package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 管理端登录请求DTO
 *
 * @author ckd
 * @since 2026-03-13
 */
@Getter
@Setter
@Schema(description = "管理员登录请求参数")
public class LoginRequestDTO {

    @Schema(description = "登录名", example = "admin", required = true)
    private String loginName;

    @Schema(description = "登录密码（明文传输，后端会进行MD5加密）", example = "123456", required = true)
    private String loginPassword;

    @Schema(description = "验证码key（从获取验证码接口获得）", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", required = true)
    private String captchaKey;

    @Schema(description = "验证码（用户输入的图片验证码）", example = "A3b9", required = true)
    private String captchaCode;
}
