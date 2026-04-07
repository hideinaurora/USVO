package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Schema(description = "移动端微信登录请求参数")
public class AppWxLoginRequestDTO {

    @Schema(description = "微信小程序code", required = true)
    @NotBlank(message = "微信code不能为空")
    private String code;

    @Schema(description = "微信用户信息（可选，当前后端不参与业务逻辑）")
    private WxUserInfo userInfo;

    @Getter
    @Setter
    @Schema(description = "微信用户信息")
    public static class WxUserInfo {
        private String nickname;
        private String avatar;
    }
}

