package org.example.vo.booking;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "可预约时间片（批量查询）")
public class AvailableSlotVO {

    private Long slotId;

    @Schema(description = "开始时间 HH:mm:ss")
    private String startTime;

    @Schema(description = "结束时间 HH:mm:ss")
    private String endTime;

    private Boolean isAvailable;

    private BigDecimal price;
}
