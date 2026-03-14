package org.example.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

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

    @Schema(description = "刷新令牌，用于获取新的访问令牌", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;

    @Schema(description = "访问令牌过期时间（Date格式）", example = "2026-03-14T12:00:00")
    private Date expires;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户姓名", example = "admin")
    private String userName;

    @Schema(description = "角色ID（预留字段，用于权限控制）")
    private List<String> roles;

    @Schema(description = "角色权限（预留字段，用于权限控制）")
    private List<String> permissions;

    @Schema(description = "头像")
    private String avatar = "https://ibdw-public.oss-cn-hangzhou.aliyuncs.com/nblg/log.png";
}
