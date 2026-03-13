package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.common.ApiResponse;
import org.example.config.exception.CommonJsonException;
import org.example.dto.LoginRequestDTO;
import org.example.service.sys.admin.AdminService;
import org.example.vo.CaptchaVO;
import org.example.vo.LoginResponseVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 管理端用户控制器
 *
 * @author ckd
 * @since 2026-03-13
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@Tag(name = "管理端用户", description = "管理员登录相关接口")
public class AdminController {

    @Resource
    private AdminService adminService;

    @Operation(summary = "管理员登录", description = "用户登录验证，包含验证码校验。登录成功返回JWT令牌和用户信息")
    @PostMapping("/login")
    public ApiResponse<LoginResponseVO> login(@RequestBody LoginRequestDTO loginRequest) {
        try {
            LoginResponseVO response = adminService.login(loginRequest);
            return ApiResponse.success(response);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("登录失败", e);
            throw new CommonJsonException("登录失败，请稍后重试");
        }
    }

    @Operation(summary = "获取验证码", description = "生成图形验证码，返回Base64编码的图片。验证码5分钟内有效，使用一次后失效")
    @GetMapping("/captcha")
    public ApiResponse<CaptchaVO> getCaptcha() {
        try {
            CaptchaVO captcha = adminService.generateCaptcha();
            return ApiResponse.success(captcha);
        } catch (Exception e) {
            log.error("获取验证码失败", e);
            throw new CommonJsonException("获取验证码失败");
        }
    }
}
