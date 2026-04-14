package org.example.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Schema(description = "生成时间片请求")
public class TimeSlotGenerateDTO {

    @NotNull(message = "场地ID不能为空")
    @JsonProperty("court_id")
    @Schema(description = "场地ID", required = true)
    private Long courtId;

    @JsonProperty("start_date")
    @Schema(description = "开始日期（YYYY-MM-DD），为空则默认当天")
    private String startDate;

    @JsonProperty("end_date")
    @Schema(description = "结束日期（YYYY-MM-DD），为空则默认当天")
    private String endDate;

    @JsonProperty("start_time")
    @Schema(description = "开始时间（HH:mm:ss），为空则默认场馆开放时间")
    private String startTime;

    @JsonProperty("end_time")
    @Schema(description = "结束时间（HH:mm:ss），为空则默认场馆关闭时间")
    private String endTime;

    @JsonProperty("slot_duration_minutes")
    @Schema(description = "时间段粒度（分钟），默认60")
    private Integer slotDurationMinutes = 60;

    @JsonProperty("ignore_existing")
    @Schema(description = "是否忽略已存在的时间片，默认false")
    private Boolean ignoreExisting = false;
}