package org.example.vo.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 活动报名学生详情VO
 *
 * @author ckd
 * @since 2026-03-13
 */
@Getter
@Setter
@Schema(description = "活动报名学生详情")
public class ApplyUserDetailVO {

    @Schema(description = "报名记录ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "登录名")
    private String loginName;

    @Schema(description = "活动ID")
    private Long applyId;

    @Schema(description = "支付状态：0-未支付，1-已支付")
    private Integer isPay;

    @Schema(description = "支付状态描述")
    private String isPayDesc;

    @Schema(description = "用户状态：1-正常，0-禁用")
    private Integer userStatus;

    @Schema(description = "用户状态描述")
    private String userStatusDesc;

    @Schema(description = "创建时间")
    private LocalDateTime gmtCreate;

    @Schema(description = "修改时间")
    private LocalDateTime gmtModify;
}
