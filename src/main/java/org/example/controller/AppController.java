package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.aop.annotation.ApiAuth;
import org.example.aop.annotation.ApiIdempotent;
import org.example.aop.annotation.RequiresPermissions;
import org.example.common.ApiResponse;
import org.example.config.exception.CommonJsonException;
import org.example.dto.AppLoginRequestDTO;
import org.example.dto.AppRegisterRequestDTO;
import org.example.dto.ApplyRequestDTO;
import org.example.service.TokenService;
import org.example.service.app.AppUserService;
import org.example.service.basic.user.UserService;
import org.example.vo.ActivityVO;
import org.example.vo.AppLoginResponseVO;
import org.example.vo.ApplyResponseVO;
import org.example.vo.EnrolledActivityVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

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
    @Resource
    private AppUserService appUserService;
    @Resource
    private TokenService tokenService;

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

    @Operation(summary = "查询可报名活动列表", description = "查询所有活动，并标注用户报名状态。0-未报名，1-待支付（包含支付订单信息），2-支付完成",
            parameters = {
                    @Parameter(name = "token", description = "JWT访问令牌", required = true, in = ParameterIn.HEADER)
            })
    @GetMapping("/activities")
    @RequiresPermissions(value = "app:apply:query", apiAuth = {ApiAuth.USER})
    public ApiResponse<List<ActivityVO>> getActivityList() {
        try {
            Long userId = tokenService.getUserId();
            List<ActivityVO> activities = appUserService.getActivityListForApp(userId);
            return ApiResponse.success(activities);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询活动列表失败", e);
            throw new CommonJsonException("查询活动列表失败，请稍后重试");
        }
    }

    @Operation(summary = "查询已报名活动列表", description = "查询用户已报名的活动，包含支付订单信息和退款记录（含审核记录）",
            parameters = {
                    @Parameter(name = "token", description = "JWT访问令牌", required = true, in = ParameterIn.HEADER)
            })
    @GetMapping("/enrolled-activities")
    @RequiresPermissions(value = "app:enrolled:query", apiAuth = {ApiAuth.USER})
    public ApiResponse<List<EnrolledActivityVO>> getEnrolledActivityList() {
        try {
            Long userId = tokenService.getUserId();
            List<EnrolledActivityVO> enrolledActivities = appUserService.getEnrolledActivityList(userId);
            return ApiResponse.success(enrolledActivities);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询已报名活动列表失败", e);
            throw new CommonJsonException("查询已报名活动列表失败，请稍后重试");
        }
    }

    @Operation(summary = "用户报名活动", description = "用户报名活动，支持名额限制，创建30分钟有效的支付订单，超时未支付自动释放名额",
            parameters = {
                    @Parameter(name = "token", description = "JWT访问令牌", required = true, in = ParameterIn.HEADER)
            })
    @PostMapping("/apply")
    @RequiresPermissions(value = "app:apply:create", apiAuth = {ApiAuth.USER})
    @ApiIdempotent(expireSeconds = 5)
    public ApiResponse<ApplyResponseVO> applyActivity(@Valid @RequestBody ApplyRequestDTO request) {
        try {
            Long userId = tokenService.getUserId();
            ApplyResponseVO response = appUserService.applyActivity(userId, request);
            return ApiResponse.success(response);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("报名活动失败", e);
            throw new CommonJsonException("报名活动失败，请稍后重试");
        }
    }
}
