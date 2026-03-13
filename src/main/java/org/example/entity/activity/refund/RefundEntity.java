package org.example.entity.activity.refund;

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
 * 退款申请表
 * </p>
 *
 * @author ckd
 * @since 2026-03-13
 */
@Getter
@Setter
@TableName("activity_refund")
public class RefundEntity implements Serializable {

    private static final long serialVersionUID = 1L;

  /**
   * 退款申请ID，主键
   */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

  /**
   * 用户ID
   */
    @TableField(value = "user_id")
    private Long userId;

  /**
   * 商户订单号
   */
    @TableField(value = "mer_order_id")
    private String merOrderId;

  /**
   * 审核类型：0-待审核，1-审核通过，2-审核拒绝
   */
    @TableField(value = "examine_type")
    private Integer examineType;

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
   * 退款单号
   */
    @TableField(value = "refund_no")
    private String refundNo;

  /**
   * 退款时间
   */
    @TableField(value = "refund_time")
    private LocalDateTime refundTime;

  /**
   * 退款金额（单位：分）
   */
    @TableField(value = "refund_amount")
    private Integer refundAmount;

  /**
   * 退款完成总时间
   */
    @TableField(value = "refund_total_time")
    private LocalDateTime refundTotalTime;

  /**
   * 外部退款单号
   */
    @TableField(value = "out_refund_no")
    private String outRefundNo;

  /**
   * 备注
   */
    @TableField(value = "bz")
    private String bz;

  /**
   * 实付金额（单位：分）
   */
    @TableField(value = "total_amount")
    private Integer totalAmount;

  /**
   * 失败消息
   */
    @TableField(value = "fail_message")
    private String failMessage;


}
