package org.example.entity.activity.apply;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonFormat;
/**
 * <p>
 * 活动报名活动表
 * </p>
 *
 * @author ckd
 * @since 2026-03-13
 */
@Getter
@Setter
@TableName("activity_apply")
public class ApplyEntity implements Serializable {

    private static final long serialVersionUID = 1L;

  /**
   * 活动ID，主键
   */
    @TableId(value = "apply_id", type = IdType.AUTO)
    private Long applyId;

  /**
   * 活动标题
   */
    @TableField(value = "apply_title")
    private String applyTitle;

  /**
   * 报名开始时间
   */
    @TableField(value = "apply_start_time")
    private LocalDateTime applyStartTime;

  /**
   * 报名结束时间
   */
    @TableField(value = "apply_end_time")
    private LocalDateTime applyEndTime;

  /**
   * 报名费用（单位：分）
   */
    @TableField(value = "apply_expense")
    private Integer applyExpense;

  /**
   * 创建时间
   */
    @TableField(value = "gmt_create", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime gmtCreate;

  /**
   * 修改时间
   */
    @TableField(value = "gmt_modify", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime gmtModify;

  /**
   * 删除标识：0-未删除，1-已删除
   */
    @TableField(value = "is_deleted")
    @TableLogic
    private Integer isDeleted;

  /**
   * 活动详情
   */
    @TableField(value = "active_info")
    private String activeInfo;

  /**
   * 限制人数
   */
    @TableField(value = "limit_num")
    private Integer limitNum;


}
