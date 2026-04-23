package org.example.vo.venue;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "场地信息")
public class CourtSimpleVO {
    private Long id;
    private String name;
    private String type;
    private BigDecimal pricePerHour;
    private String previewUrl;
}

