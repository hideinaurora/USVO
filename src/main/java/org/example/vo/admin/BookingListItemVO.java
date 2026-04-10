package org.example.vo.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "预约列表项")
public class BookingListItemVO {

    private Long id;

    private String username;

    private String phone;

    @JsonProperty("court_name")
    private String courtName;

    @JsonProperty("venue_name")
    private String venueName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("start_time")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("end_time")
    private LocalDateTime endTime;

    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    @JsonProperty("deposit_amount")
    private BigDecimal depositAmount;

    private Integer status;

    @JsonProperty("status_text")
    private String statusText;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("create_time")
    private LocalDateTime createTime;
}