package org.example.dto.booking;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
@Schema(description = "取消预约请求")
public class BookingCancelDTO {

    @NotEmpty(message = "预约ID列表不能为空")
    @JsonProperty("booking_ids")
    @Schema(description = "预约ID列表，支持批量取消", required = true)
    private List<Long> bookingIds;
}
