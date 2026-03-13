package org.example.service.basic.user;


import org.example.dto.AppLoginRequestDTO;
import org.example.dto.AppRegisterRequestDTO;
import org.example.entity.basic.user.UserEntity;
import org.example.vo.AppLoginResponseVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 用户信息表 服务类
 * </p>
 *
 * @author ckd
 * @since 2026-03-13
 */
public interface UserService extends IService<UserEntity> {

    /**
     * 移动端用户注册
     *
     * @param registerRequest 注册请求信息
     * @return 登录响应信息（包含token）
     */
    AppLoginResponseVO register(AppRegisterRequestDTO registerRequest);

    /**
     * 移动端用户登录
     *
     * @param loginRequest 登录请求信息
     * @return 登录响应信息
     */
    AppLoginResponseVO login(AppLoginRequestDTO loginRequest);
}
