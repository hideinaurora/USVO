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
@Schema(description = "场地列表项")
public class CourtListItemVO {

    private Long id;

    @JsonProperty("venue_id")
    private Long venueId;

    @JsonProperty("venue_name")
    private String venueName;

    private String name;

    private String type;

    @JsonProperty("price_per_hour")
    private BigDecimal pricePerHour;

    private Integer status;

    private String previewUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("create_time")
    private String createTime;
}