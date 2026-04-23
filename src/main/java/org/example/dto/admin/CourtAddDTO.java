package org.example.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "添加场地请求")
public class CourtAddDTO {

    @NotNull(message = "场馆ID不能为空")
    @JsonProperty("venue_id")
    @Schema(description = "场馆ID", required = true)
    private Long venueId;

    @NotNull(message = "场地名称不能为空")
    @JsonProperty("name")
    @Schema(description = "场地名称", required = true)
    private String name;

    @NotNull(message = "场地类型不能为空")
    @JsonProperty("type")
    @Schema(description = "场地类型", required = true)
    private String type;

    @NotNull(message = "每小时价格不能为空")
    @JsonProperty("price_per_hour")
    @Schema(description = "每小时价格", required = true)
    private BigDecimal pricePerHour;

    @JsonProperty("preview_url")
    @Schema(description = "预览图URL")
    private String previewUrl;
}