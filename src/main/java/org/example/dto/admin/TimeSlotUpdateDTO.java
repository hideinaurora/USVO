package org.example.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "更新时间片请求")
public class TimeSlotUpdateDTO {

    @JsonProperty("status")
    @Schema(description = "状态（0可预约/1锁定中/2已预约）")
    private Integer status;

    @JsonProperty("reason")
    @Schema(description = "修改原因")
    private String reason;
}