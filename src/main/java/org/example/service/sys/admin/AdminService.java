package org.example.service.sys.admin;


import org.example.dto.LoginRequestDTO;
import org.example.entity.sys.admin.AdminEntity;
import org.example.vo.CaptchaVO;
import org.example.vo.LoginResponseVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 系统用户表 服务类
 * </p>
 *
 * @author ckd
 * @since 2026-03-13
 */
public interface AdminService extends IService<AdminEntity> {

    /**
     * 管理端用户登录
     *
     * @param loginRequest 登录请求信息
     * @return 登录响应信息
     */
    LoginResponseVO login(LoginRequestDTO loginRequest);

    /**
     * 生成验证码
     *
     * @return 验证码信息
     */
    CaptchaVO generateCaptcha();

    /**
     * 刷新令牌
     *
     * @param refreshToken 刷新令牌
     * @return 新的登录响应信息（包含新的accessToken、refreshToken和expires）
     */
    LoginResponseVO refreshToken(String refreshToken);
}
