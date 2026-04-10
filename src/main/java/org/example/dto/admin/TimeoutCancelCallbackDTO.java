package org.example.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Schema(description = "超时未支付取消回调请求")
public class TimeoutCancelCallbackDTO {

    @NotNull(message = "预约ID不能为空")
    @JsonProperty("booking_id")
    @Schema(description = "预约ID", required = true)
    private Long bookingId;
}