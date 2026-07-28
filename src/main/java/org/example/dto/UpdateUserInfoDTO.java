package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "更新用户信息请求参数")
public class UpdateUserInfoDTO {

    @Schema(description = "用户名", example = "张三丰")
    private String userName;

    @Schema(description = "微信ID（可选）", example = "wx_openid_123456")
    private String wxId;

    @Schema(description = "用户头像URL", example = "https://oss.example.com/avatar.png")
    private String avatarUrl;

    @Schema(description = "用户头像图片Base64编码（用于提取人脸特征，可选）", example = "data:image/jpeg;base64,/9j/4AAQ...")
    private String faceImageBase64;
}

