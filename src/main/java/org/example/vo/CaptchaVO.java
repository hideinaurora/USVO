package org.example.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 验证码响应VO
 *
 * @author ckd
 * @since 2026-03-13
 */
@Getter
@Setter
@Schema(description = "验证码响应结果")
public class CaptchaVO {

    @Schema(description = "验证码唯一标识，登录时需要携带此参数", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String captchaKey;

    @Schema(description = "验证码图片（Base64编码），前端可直接显示", example = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...")
    private String captchaImage;
}
