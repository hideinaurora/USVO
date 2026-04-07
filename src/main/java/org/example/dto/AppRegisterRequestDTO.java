package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 移动端用户注册请求DTO
 *
 * @author ckd
 * @since 2026-03-13
 */
@Getter
@Setter
@Schema(description = "移动端用户注册请求参数")
public class AppRegisterRequestDTO {

    @Schema(description = "登录名", example = "user001", required = true)
    private String loginName;

    @Schema(description = "登录密码（明文传输，后端会进行MD5加密）", example = "123456", required = true)
    private String loginPassword;

    @Schema(description = "用户名", example = "张三", required = true)
    private String userName;

    @Schema(description = "微信ID（可选）", example = "wx_openid_123456")
    private String wxId;

    @Schema(description = "手机号（可选）", example = "13800000000")
    private String phone;

}
