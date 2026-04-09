package org.example.vo.booking;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Schema(description = "创建预约结果")
public class BookingCreateResultVO {

    @Schema(description = "预约ID列表")
    private List<Long> bookingIds;

    private String courtName;

    @Schema(description = "时间片信息列表")
    private List<SlotInfo> slots;

    private BigDecimal totalAmount;

    private BigDecimal depositAmount;

    @Schema(description = "0待支付 1已预约 2已取消 3已完成 4违约")
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "押金支付截止时间（示意，未接MQ超时取消）")
    private LocalDateTime expireTime;

    @Getter
    @Setter
    @Schema(description = "时间片信息")
    public static class SlotInfo {
        private Long slotId;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime startTime;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime endTime;

        private BigDecimal amount;
    }
}