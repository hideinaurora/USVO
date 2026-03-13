package org.example.dto.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 活动报名学生查询DTO
 *
 * @author ckd
 * @since 2026-03-13
 */
@Getter
@Setter
@Schema(description = "活动报名学生查询请求参数")
public class ApplyUserQueryDTO {

    @Schema(description = "活动ID", required = true, example = "1")
    private Long applyId;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "支付状态：0-未支付，1-已支付", example = "1")
    private Integer isPay;

    @Schema(description = "用户名（模糊查询）", example = "张三")
    private String userName;
}
