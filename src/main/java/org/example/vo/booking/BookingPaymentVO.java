package org.example.vo.booking;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "预约关联支付信息")
public class BookingPaymentVO {

    private Long id;

    private BigDecimal amount;

    private String payType;

    @Schema(description = "0未支付 1已支付 2已退款")
    private Integer status;

    private String transactionNo;
}
