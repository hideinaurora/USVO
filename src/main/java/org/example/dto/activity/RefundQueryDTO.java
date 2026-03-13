package org.example.dto.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 退款学生查询DTO
 *
 * @author ckd
 * @since 2026-03-13
 */
@Getter
@Setter
@Schema(description = "退款学生查询请求参数")
public class RefundQueryDTO {

    @Schema(description = "活动ID", example = "1")
    private Long applyId;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "审核状态：0-待审核，1-审核通过，2-审核拒绝", example = "0")
    private Integer examineType;

    @Schema(description = "用户名（模糊查询）", example = "张三")
    private String userName;
}
