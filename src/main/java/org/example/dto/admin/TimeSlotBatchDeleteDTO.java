package org.example.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
@Schema(description = "批量删除时间片请求")
public class TimeSlotBatchDeleteDTO {

    @NotEmpty(message = "时间片ID列表不能为空")
    @JsonProperty("slot_ids")
    @Schema(description = "时间片ID列表", required = true)
    private List<Long> slotIds;

    @JsonProperty("force")
    @Schema(description = "是否强制删除，默认false")
    private Boolean force = false;
}