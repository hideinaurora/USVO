package org.example.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "修改场地请求")
public class CourtUpdateDTO {

    @JsonProperty("name")
    @Schema(description = "场地名称")
    private String name;

    @JsonProperty("price_per_hour")
    @Schema(description = "每小时价格")
    private BigDecimal pricePerHour;

    @JsonProperty("preview_url")
    @Schema(description = "预览图URL")
    private String previewUrl;

    @JsonProperty("status")
    @Schema(description = "状态")
    private Integer status;
}