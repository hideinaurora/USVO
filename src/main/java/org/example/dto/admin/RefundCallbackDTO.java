package org.example.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "延迟退款回调请求")
public class RefundCallbackDTO {

    @NotNull(message = "预约ID不能为空")
    @JsonProperty("booking_id")
    @Schema(description = "预约ID", required = true)
    private Long bookingId;

    @NotNull(message = "用户ID不能为空")
    @JsonProperty("user_id")
    @Schema(description = "用户ID", required = true)
    private Long userId;

    @NotNull(message = "退款金额不能为空")
    @Schema(description = "退款金额", required = true)
    private BigDecimal amount;

    @NotNull(message = "退款类型不能为空")
    @JsonProperty("refund_type")
    @Schema(description = "退款类型", required = true)
    private String refundType;
}