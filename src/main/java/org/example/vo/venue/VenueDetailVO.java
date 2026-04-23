package org.example.vo.venue;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Schema(description = "场馆详情")
public class VenueDetailVO {
    private Long id;
    private String name;
    private String type;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String openTime;
    private String closeTime;
    private Integer status;

    private String previewUrl;

    private List<CourtSimpleVO> courts;
}

