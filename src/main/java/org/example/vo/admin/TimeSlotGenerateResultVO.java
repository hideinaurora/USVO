package org.example.vo.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Schema(description = "生成时间片结果")
public class TimeSlotGenerateResultVO {

    @JsonProperty("court_id")
    private Long courtId;

    @JsonProperty("court_name")
    private String courtName;

    @JsonProperty("venue_name")
    private String venueName;

    @JsonProperty("date_range")
    private DateRange dateRange;

    @JsonProperty("total_generated")
    private Integer totalGenerated;

    private Integer skipped;

    private List<SlotInfo> slots;

    @Getter
    @Setter
    public static class DateRange {
        @JsonProperty("start_date")
        private String startDate;

        @JsonProperty("end_date")
        private String endDate;
    }

    @Getter
    @Setter
    public static class SlotInfo {
        private Long id;

        @JsonProperty("slot_date")
        private String slotDate;

        @JsonProperty("start_time")
        private String startTime;

        @JsonProperty("end_time")
        private String endTime;

        private Integer status;
    }
}