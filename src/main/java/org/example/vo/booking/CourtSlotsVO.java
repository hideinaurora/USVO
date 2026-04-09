package org.example.vo.booking;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Schema(description = "场地某日时间片列表")
public class CourtSlotsVO {

    @Schema(description = "场地ID")
    private Long courtId;

    private String courtName;

    private BigDecimal pricePerHour;

    private String date;

    private List<SlotItemVO> slots;
}
