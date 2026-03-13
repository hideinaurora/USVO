package org.example.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 移动端用户登录响应VO
 *
 * @author ckd
 * @since 2026-03-13
 */
@Getter
@Setter
@Schema(description = "移动端用户登录响应结果")
public class AppLoginResponseVO {

    @Schema(description = "JWT访问令牌，用于后续接口的身份验证", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "登录名", example = "user001")
    private String loginName;

    @Schema(description = "用户名", example = "张三")
    private String userName;

    @Schema(description = "角色ID", example = "2")
    private Integer roleId;
}
