package org.example.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "用户信息")
public class UserInfoVO {
    private Long id;
    private String loginName;
    private String userName;
    private Integer status;
    private LocalDateTime lastLoginTime;
    private String phone;
    private String avatarUrl;
}

