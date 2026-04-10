package org.example.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Schema(description = "强制取消预约请求")
public class BookingForceCancelDTO {

    @NotNull(message = "取消原因不能为空")
    @JsonProperty("reason")
    @Schema(description = "取消原因", required = true)
    private String reason;
}