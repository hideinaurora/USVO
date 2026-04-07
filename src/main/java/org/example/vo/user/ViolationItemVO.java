package org.example.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "违约记录项")
public class ViolationItemVO {
    private Long id;
    private Long bookingId;
    private String reason;
    private BigDecimal penaltyAmount;
    private LocalDateTime createTime;
}

