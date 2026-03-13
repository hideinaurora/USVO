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
 * 支付订单表
 * </p>
 *
 * @author ckd
 * @since 2026-03-13
 */
@Getter
@Setter
@TableName("activity_apply_pay")
public class ApplyPayEntity implements Serializable {

    private static final long serialVersionUID = 1L;

  /**
   * 订单ID，主键
   */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

  /**
   * 用户ID
   */
    @TableField(value = "user_id")
    private Long userId;

  /**
   * 支付状态
   */
    @TableField(value = "pay_status")
    private Integer payStatus;

  /**
   * 商户订单号
   */
    @TableField(value = "mer_order_id")
    private String merOrderId;

  /**
   * 订单描述
   */
    @TableField(value = "order_desc")
    private String orderDesc;

  /**
   * 订单过期时间
   */
    @TableField(value = "expire_time")
    private String expireTime;

  /**
   * 原始金额（单位：分）
   */
    @TableField(value = "original_amount")
    private Integer originalAmount;

  /**
   * 总金额（单位：分）
   */
    @TableField(value = "total_amount")
    private Integer totalAmount;

  /**
   * 商户名称
   */
    @TableField(value = "mer_name")
    private String merName;

  /**
   * 序列号/流水号
   */
    @TableField(value = "seq_id")
    private String seqId;

  /**
   * 支付完成时间
   */
    @TableField(value = "pay_time")
    private String payTime;

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
   * 报名申请ID
   */
    @TableField(value = "apply_id")
    private Long applyId;

  /**
   * 报名学生ID
   */
    @TableField(value = "apply_student_id")
    private Long applyStudentId;

  /**
   * 支付信息详情
   */
    @TableField(value = "pay_info")
    private String payInfo;


}
