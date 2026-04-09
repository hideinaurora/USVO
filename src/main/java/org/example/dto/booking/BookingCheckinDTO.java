package org.example.dto.booking;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Schema(description = "签到入场请求")
public class BookingCheckinDTO {

    @NotNull(message = "预约ID不能为空")
    @JsonProperty("booking_id")
    @Schema(description = "预约ID", required = true)
    private Long bookingId;

    @NotBlank(message = "签到方式不能为空")
    @JsonProperty("checkin_type")
    @Schema(description = "签到方式，如扫码/GPS", required = true)
    private String checkinType;

    @JsonProperty("device_id")
    @Schema(description = "设备ID")
    private String deviceId;
}
