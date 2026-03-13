package org.example.dto.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 活动查询请求DTO
 *
 * @author ckd
 * @since 2026-03-13
 */
@Getter
@Setter
@Schema(description = "活动查询请求参数")
public class ApplyQueryDTO {

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "活动标题（模糊查询）", example = "测试活动")
    private String applyTitle;

    @Schema(description = "报名开始时间（开始）", example = "2026-03-01 00:00:00")
    private String startTimeBegin;

    @Schema(description = "报名开始时间（结束）", example = "2026-03-31 23:59:59")
    private String startTimeEnd;
}
