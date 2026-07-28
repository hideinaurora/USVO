package org.example.dto.dify;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * Dify 智能体查询请求DTO
 */
@Data
public class DifyQueryRequestDTO implements Serializable {

    @Schema(description = "查询类型: venue_info, court_info, court_availability, pricing, booking_rules, user_bookings, user_violations")
    @NotBlank(message = "query_type不能为空")
    @JsonProperty("query_type")
    private String queryType;

    @Schema(description = "查询参数，具体字段根据query_type而定")
    private QueryParams params;

    @Schema(description = "调用用户标识，用于日志追踪")
    @JsonProperty("user_id")
    private String userId;

    @Data
    public static class QueryParams implements Serializable {
        // venue_info 参数
        @Schema(description = "场馆名称关键词")
        private String keyword;

        @Schema(description = "场馆类型")
        private String type;

        // court_info 参数
        @Schema(description = "场馆ID")
        @JsonProperty("venue_id")
        private Long venueId;

        @Schema(description = "场地类型: basketball, badminton, swimming")
        @JsonProperty("court_type")
        private String courtType;

        // court_availability 参数
        @Schema(description = "场地ID")
        @JsonProperty("court_id")
        private Long courtId;

        @Schema(description = "查询日期 YYYY-MM-DD")
        @JsonProperty("slot_date")
        private String slotDate;

        // booking_rules 参数
        @Schema(description = "规则类型: all, booking, cancel, violation, checkin")
        @JsonProperty("rule_type")
        private String ruleType;

        // user_bookings 参数
        @Schema(description = "用户ID")
        @JsonProperty("user_id")
        private Long userId;

        @Schema(description = "预约状态: 0待支付, 1已预约, 2已取消, 3已完成, 4违约")
        private Integer status;

        @Schema(description = "返回条数")
        private Integer limit;
    }
}
