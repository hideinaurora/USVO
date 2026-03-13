package org.example.entity.failed.delayed;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonFormat;
/**
 * <p>
 * 延迟消息失败记录表
 * </p>
 *
 * @author ckd
 * @since 2026-03-13
 */
@Getter
@Setter
@TableName("failed_delayed_message")
public class DelayedMessageEntity implements Serializable {

    private static final long serialVersionUID = 1L;

  /**
   * 主键ID
   */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

  /**
   * 消息ID
   */
    @TableField(value = "message_id")
    private String messageId;

  /**
   * 消息内容
   */
    @TableField(value = "content")
    private String content;

  /**
   * 失败时间
   */
    @TableField(value = "failed_time")
    private LocalDateTime failedTime;

  /**
   * 创建时间
   */
    @TableField(value = "created_time")
    private LocalDateTime createdTime;

  /**
   * 更新时间
   */
    @TableField(value = "updated_time")
    private LocalDateTime updatedTime;


}
