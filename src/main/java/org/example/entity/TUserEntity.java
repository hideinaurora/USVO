package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author 吴子木
 * @since 2024-09-23
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_user")
public class TUserEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 用户手机号
     */
    private String telePhone;

    /**
     * 测温状态
     */
    private String tempState;

    /**
     * 微信openId
     */
    private String wxOpenid;

    /**
     * 注册状态
     */
    private String regStatus;

    /**
     * 微信授权code
     */
    @TableField(exist = false)
    private String code;
}
