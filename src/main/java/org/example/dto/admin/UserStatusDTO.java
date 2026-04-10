package org.example.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Schema(description = "用户状态更新请求")
public class UserStatusDTO {

    @NotNull(message = "状态不能为空")
    @JsonProperty("status")
    @Schema(description = "状态 (1 正常 0 禁用)", required = true)
    private Integer status;
}