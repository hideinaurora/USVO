package org.example.vo.booking;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "时间片项")
public class SlotItemVO {

    private Long id;

    @Schema(description = "开始时间 HH:mm:ss")
    private String startTime;

    @Schema(description = "结束时间 HH:mm:ss")
    private String endTime;

    @Schema(description = "0可预约 1锁定中 2已预约")
    private Integer status;

    private String statusText;
}
