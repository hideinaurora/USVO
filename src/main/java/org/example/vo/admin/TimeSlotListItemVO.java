package org.example.vo.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "时间片列表项")
public class TimeSlotListItemVO {

    private Long id;

    @JsonProperty("court_id")
    private Long courtId;

    @JsonProperty("court_name")
    private String courtName;

    @JsonProperty("venue_name")
    private String venueName;

    @JsonProperty("slot_date")
    private String slotDate;

    @JsonProperty("start_time")
    private String startTime;

    @JsonProperty("end_time")
    private String endTime;

    private Integer status;

    @JsonProperty("status_text")
    private String statusText;

    @JsonProperty("booking_id")
    private Long bookingId;

    @JsonProperty("booking_user")
    private String bookingUser;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("create_time")
    private String createTime;
}