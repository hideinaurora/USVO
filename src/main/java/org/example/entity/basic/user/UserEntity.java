package org.example.entity.basic.user;

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
 * 用户信息表
 * </p>
 *
 * @author ckd
 * @since 2026-03-13
 */
@Getter
@Setter
@TableName("basic_user")
public class UserEntity implements Serializable {

    private static final long serialVersionUID = 1L;

  /**
   * 用户ID，主键
   */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

  /**
   * 用户名
   */
    @TableField(value = "user_name")
    private String userName;

  /**
   * 修改时间
   */
    @TableField(value = "gmt_modify", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime gmtModify;

  /**
   * 登录名
   */
    @TableField(value = "login_name")
    private String loginName;

  /**
   * 登录密码
   */
    @TableField(value = "login_password")
    private String loginPassword;

  /**
   * 用户状态：1-正常，0-禁用
   */
    @TableField(value = "user_status")
    private Integer userStatus;

  /**
   * 删除标识：0-未删除，1-已删除
   */
    @TableField(value = "is_deleted")
    @TableLogic
    private Integer isDeleted;

  /**
   * 微信id
   */
    @TableField(value = "wx_id")
    private String wxId;

    /**
     * 最近登录时间（可为空）
     */
    @TableField(value = "last_login_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastLoginTime;


    @TableField(value = "phone")
    private String phone;

    @TableField(value = "violation_count")
    private Integer violationCount;

    @TableField(value = "credit_score")
    private Integer creditScore;

    @TableField(value = "avatar_url")
    private String avatarUrl;

    /**
     * 人脸特征向量（512维归一化特征，JSON数组格式存储）
     */
    @TableField(value = "face_feature")
    private String faceFeature;

}
