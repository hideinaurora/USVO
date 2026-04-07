package org.example.vo.venue;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "场馆列表返回项")
public class VenueListVO {

    private Long id;
    private String name;
    private String type;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String openTime;
    private String closeTime;

    @Schema(description = "距离（公里），未传坐标时为null")
    private Double distance;

    @Schema(description = "场地数量")
    private Long courtCount;
}

