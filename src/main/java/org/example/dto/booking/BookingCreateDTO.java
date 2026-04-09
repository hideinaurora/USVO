package org.example.dto.booking;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@Schema(description = "创建预约请求")
public class BookingCreateDTO {

    @NotNull(message = "场地ID不能为空")
    @JsonProperty("court_id")
    @Schema(description = "场地ID", required = true)
    private Long courtId;

    @NotEmpty(message = "时间片ID列表不能为空")
    @JsonProperty("slot_ids")
    @Schema(description = "时间片ID列表，支持批量预约", required = true)
    private List<Long> slotIds;
}
