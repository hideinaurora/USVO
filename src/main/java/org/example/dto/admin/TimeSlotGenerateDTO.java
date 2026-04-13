package org.example.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "生成时间片请求")
public class TimeSlotGenerateDTO {

    @NotNull(message = "场地ID不能为空")
    @JsonProperty("court_id")
    @Schema(description = "场地ID", required = true)
    private Long courtId;

    @NotBlank(message = "开始日期不能为空")
    @JsonProperty("start_date")
    @Schema(description = "开始日期（YYYY-MM-DD）", required = true)
    private String startDate;

    @NotBlank(message = "结束日期不能为空")
    @JsonProperty("end_date")
    @Schema(description = "结束日期（YYYY-MM-DD）", required = true)
    private String endDate;

    @JsonProperty("slot_duration_minutes")
    @Schema(description = "时间段粒度（分钟），默认60")
    private Integer slotDurationMinutes = 60;

    @JsonProperty("ignore_existing")
    @Schema(description = "是否忽略已存在的时间片，默认false")
    private Boolean ignoreExisting = false;
}