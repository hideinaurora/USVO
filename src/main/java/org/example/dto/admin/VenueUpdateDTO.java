package org.example.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "修改场馆请求")
public class VenueUpdateDTO {

    @JsonProperty("name")
    @Schema(description = "场馆名称")
    private String name;

    @JsonProperty("type")
    @Schema(description = "场馆类型")
    private String type;

    @JsonProperty("address")
    @Schema(description = "地址")
    private String address;

    @JsonProperty("latitude")
    @Schema(description = "纬度")
    private BigDecimal latitude;

    @JsonProperty("longitude")
    @Schema(description = "经度")
    private BigDecimal longitude;

    @JsonProperty("open_time")
    @Schema(description = "开放时间 (HH:mm:ss)")
    private String openTime;

    @JsonProperty("close_time")
    @Schema(description = "关闭时间 (HH:mm:ss)")
    private String closeTime;

    @JsonProperty("status")
    @Schema(description = "状态")
    private Integer status;
}