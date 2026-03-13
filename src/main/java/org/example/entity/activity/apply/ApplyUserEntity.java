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
 * 用户报名记录表
 * </p>
 *
 * @author ckd
 * @since 2026-03-13
 */
@Getter
@Setter
@TableName("activity_apply_user")
public class ApplyUserEntity implements Serializable {

    private static final long serialVersionUID = 1L;

  /**
   * 记录ID，主键
   */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

  /**
   * 用户ID
   */
    @TableField(value = "user_id")
    private Long userId;

  /**
   * 支付状态：0-未支付，1-已支付
   */
    @TableField(value = "is_pay")
    private Integer isPay;

  /**
   * 报名申请ID
   */
    @TableField(value = "apply_id")
    private Long applyId;

  /**
   * 删除标识：0-未删除，1-已删除
   */
    @TableField(value = "is_deleted")
    @TableLogic
    private Integer isDeleted;

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


}
