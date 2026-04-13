package org.example.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Schema(description = "锁定/解锁时间片请求")
public class TimeSlotLockDTO {

    @NotNull(message = "locked参数不能为空")
    @JsonProperty("locked")
    @Schema(description = "true锁定/false解锁", required = true)
    private Boolean locked;

    @JsonProperty("reason")
    @Schema(description = "操作原因")
    private String reason;
}