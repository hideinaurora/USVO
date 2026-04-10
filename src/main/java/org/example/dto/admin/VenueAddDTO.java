package org.example.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "添加场馆请求")
public class VenueAddDTO {

    @NotNull(message = "场馆名称不能为空")
    @JsonProperty("name")
    @Schema(description = "场馆名称", required = true)
    private String name;

    @NotNull(message = "场馆类型不能为空")
    @JsonProperty("type")
    @Schema(description = "场馆类型", required = true)
    private String type;

    @NotNull(message = "地址不能为空")
    @JsonProperty("address")
    @Schema(description = "地址", required = true)
    private String address;

    @JsonProperty("latitude")
    @Schema(description = "纬度")
    private BigDecimal latitude;

    @JsonProperty("longitude")
    @Schema(description = "经度")
    private BigDecimal longitude;

    @NotNull(message = "开放时间不能为空")
    @JsonProperty("open_time")
    @Schema(description = "开放时间 (HH:mm:ss)", required = true)
    private String openTime;

    @NotNull(message = "关闭时间不能为空")
    @JsonProperty("close_time")
    @Schema(description = "关闭时间 (HH:mm:ss)", required = true)
    private String closeTime;
}