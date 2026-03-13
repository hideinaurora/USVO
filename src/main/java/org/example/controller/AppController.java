package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.common.ApiResponse;
import org.example.config.exception.CommonJsonException;
import org.example.dto.AppLoginRequestDTO;
import org.example.dto.AppRegisterRequestDTO;
import org.example.service.basic.user.UserService;
import org.example.vo.AppLoginResponseVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 移动端用户控制器
 *
 * @author ckd
 * @since 2026-03-13
 */
@Slf4j
@RestController
@RequestMapping("/api/app")
@Tag(name = "移动端用户", description = "移动端用户注册、登录相关接口")
public class AppController {

    @Resource
    private UserService userService;

    @Operation(summary = "用户注册", description = "移动端用户注册，支持账号密码注册，可选择性绑定微信ID。注册成功后自动登录并返回JWT令牌")
    @PostMapping("/register")
    public ApiResponse<AppLoginResponseVO> register(@RequestBody AppRegisterRequestDTO registerRequest) {
        try {
            AppLoginResponseVO response = userService.register(registerRequest);
            return ApiResponse.success(response);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户注册失败", e);
            throw new CommonJsonException("注册失败，请稍后重试");
        }
    }

    @Operation(summary = "用户登录", description = "移动端用户登录，通过账号密码进行登录验证，返回JWT令牌")
    @PostMapping("/login")
    public ApiResponse<AppLoginResponseVO> login(@RequestBody AppLoginRequestDTO loginRequest) {
        try {
            AppLoginResponseVO response = userService.login(loginRequest);
            return ApiResponse.success(response);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("登录失败", e);
            throw new CommonJsonException("登录失败，请稍后重试");
        }
    }
}
