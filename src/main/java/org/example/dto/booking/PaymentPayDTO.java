package org.example.dto.booking;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Schema(description = "支付押金请求")
public class PaymentPayDTO {

    @NotNull(message = "预约ID不能为空")
    @JsonProperty("booking_id")
    @Schema(description = "预约ID", required = true)
    private Long bookingId;

    @NotBlank(message = "支付方式不能为空")
    @JsonProperty("pay_type")
    @Schema(description = "支付方式 wechat/alipay", required = true)
    private String payType;
}
