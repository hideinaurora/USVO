package org.example.vo.booking;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "支付押金结果")
public class PaymentPayResultVO {

    private Long paymentId;

    private Long bookingId;

    private BigDecimal amount;

    @Schema(description = "0未支付 1已支付 2已退款")
    private Integer status;

    private String transactionNo;
}
