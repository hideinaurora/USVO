package org.example.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 管理端登录响应VO
 *
 * @author ckd
 * @since 2026-03-13
 */
@Getter
@Setter
@Schema(description = "管理员登录响应结果")
public class LoginResponseVO {

    @Schema(description = "JWT访问令牌，用于后续接口的身份验证", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "登录名", example = "admin")
    private String loginName;

    @Schema(description = "角色ID（预留字段，用于权限控制）", example = "1")
    private Integer roleId;
}
