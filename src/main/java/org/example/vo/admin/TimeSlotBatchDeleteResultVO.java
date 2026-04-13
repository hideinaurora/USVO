package org.example.vo.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "批量删除结果")
public class TimeSlotBatchDeleteResultVO {

    @JsonProperty("success_count")
    private Integer successCount;

    @JsonProperty("fail_count")
    private Integer failCount;

    @JsonProperty("failed_ids")
    private List<Long> failedIds;

    @JsonProperty("fail_reason")
    private String failReason;
}