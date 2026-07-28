package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.aop.annotation.ApiAuth;
import org.example.aop.annotation.RequiresPermissions;
import org.example.common.ApiResponse;
import org.example.config.exception.CommonJsonException;
import org.example.dto.AppLoginRequestDTO;
import org.example.dto.AppRegisterRequestDTO;
import org.example.dto.AppWxLoginRequestDTO;
import org.example.dto.ApplyRequestDTO;
import org.example.dto.RefundRequestDTO;
import org.example.dto.UpdateUserInfoDTO;
import org.example.entity.basic.user.UserEntity;
import org.example.entity.basic.user.ViolationLogEntity;
import org.example.mapper.basic.user.ViolationLogMapper;
import org.example.service.TokenService;
import org.example.service.app.AppUserService;
import org.example.service.basic.user.UserService;
import org.example.service.face.FaceService;
import org.example.utils.WxUtils;
import org.example.vo.ActivityVO;
import org.example.vo.AppLoginResponseVO;
import org.example.vo.ApplyResponseVO;
import org.example.vo.EnrolledActivityVO;
import org.example.vo.RefundResponseVO;
import org.example.vo.user.UserInfoVO;
import org.example.vo.user.ViolationItemVO;
import org.example.vo.user.ViolationListVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.client.RestTemplate;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

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

    @Resource
    private WxUtils wxUtils;

    @Resource
    private ViolationLogMapper violationLogMapper;

    @Resource
    private FaceService faceService;

    private final RestTemplate restTemplate = new RestTemplate();

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

//    @Operation(summary = "微信登录", description = "通过微信小程序code进行登录，返回JWT令牌")
//    @PostMapping("/wx-login")
//    public ApiResponse<AppLoginResponseVO> wxLogin(@Valid @RequestBody AppWxLoginRequestDTO request) {
//        try {
//            String openId = wxUtils.queryMiniOpenId(request.getCode());
//            log.info("获取微信openId成功: openId={}", openId);
//            AppLoginResponseVO response = userService.wxLogin(openId);
//            return ApiResponse.success(response);
//        } catch (CommonJsonException e) {
//            throw e;
//        } catch (Exception e) {
//            log.error("微信登录失败", e);
//            throw new CommonJsonException("微信登录失败，请稍后重试");
//        }


//HI
//    }

    @Operation(summary = "获取用户信息", description = "获取当前登录用户的基础信息",
            parameters = {
                    @Parameter(name = "token", description = "JWT访问令牌", required = true, in = ParameterIn.HEADER)
            })
    @GetMapping("/info")
    @RequiresPermissions(value = "app:user:info", apiAuth = {ApiAuth.USER})
    public ApiResponse<UserInfoVO> getUserInfo() {
        try {
            log.info("进入获取用户信息控制器");
            Long userId = tokenService.getUserId();
            log.info("用户ID：{}", userId);
            UserEntity user = userService.getById(userId);
            if (user == null) {
                return ApiResponse.error(404, "用户不存在");
            }
            UserInfoVO vo = new UserInfoVO();
            vo.setId(user.getId());
            vo.setLoginName(user.getLoginName());
            vo.setUserName(user.getUserName());
            vo.setStatus(user.getUserStatus());
            vo.setLastLoginTime(user.getLastLoginTime());
            vo.setPhone(user.getPhone());
            vo.setAvatarUrl(user.getAvatarUrl());
            return ApiResponse.success(vo);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            throw new CommonJsonException("获取用户信息失败，请稍后重试");
        }
    }

    @Operation(summary = "更新用户信息", description = "更新当前登录用户的基础信息，支持同时提取人脸特征",
            parameters = {
                    @Parameter(name = "token", description = "JWT访问令牌", required = true, in = ParameterIn.HEADER)
            })
    @PutMapping("/info")
    @RequiresPermissions(value = "app:user:update", apiAuth = {ApiAuth.USER})
    public ApiResponse<Void> updateUserInfo(@RequestBody UpdateUserInfoDTO dto) {
        try {
            Long userId = tokenService.getUserId();
            UserEntity update = new UserEntity();
            update.setId(userId);
            if (dto != null) {
                update.setUserName(dto.getUserName());
                update.setWxId(dto.getWxId());
                update.setAvatarUrl(dto.getAvatarUrl());

                // 人脸特征提取逻辑
                String faceImageBase64 = dto.getFaceImageBase64();
                
                // 如果没有单独提供人脸图片Base64，但更新了头像，则从头像URL下载并提取
                if ((faceImageBase64 == null || faceImageBase64.isEmpty()) 
                        && dto.getAvatarUrl() != null && !dto.getAvatarUrl().isEmpty()) {
                    try {
                        // 下载头像图片并转为Base64
                        byte[] imageBytes = restTemplate.getForObject(dto.getAvatarUrl(), byte[].class);
                        if (imageBytes != null && imageBytes.length > 0) {
                            faceImageBase64 = "data:image/jpeg;base64," + java.util.Base64.getEncoder().encodeToString(imageBytes);
                            log.info("用户 {} 成功下载头像图片，长度: {} bytes", userId, imageBytes.length);
                        }
                    } catch (Exception e) {
                        log.warn("下载头像图片失败: {}", e.getMessage());
                    }
                }

                // 如果有人脸图片，则提取特征
                if (faceImageBase64 != null && !faceImageBase64.isEmpty()) {
                    try {
                        List<Double> feature = faceService.extractFeature(faceImageBase64);
                        update.setFaceFeature(com.alibaba.fastjson2.JSON.toJSONString(feature));
                        log.info("用户 {} 成功提取人脸特征", userId);
                    } catch (Exception e) {
                        log.warn("提取人脸特征失败: {}", e.getMessage());
                        // 人脸特征提取失败不影响基本信息更新
                    }
                }
            }
            boolean ok = userService.updateById(update);
            if (!ok) {
                return ApiResponse.error("更新失败");
            }
            return ApiResponse.success("更新成功", null);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新用户信息失败", e);
            throw new CommonJsonException("更新用户信息失败，请稍后重试");
        }
    }

    @Operation(summary = "获取我的违约记录", description = "查询当前登录用户的违约记录列表",
            parameters = {
                    @Parameter(name = "token", description = "JWT访问令牌", required = true, in = ParameterIn.HEADER)
            })
    @GetMapping("/violations")
    @RequiresPermissions(value = "app:user:violations", apiAuth = {ApiAuth.USER})
    public ApiResponse<ViolationListVO> myViolations() {
        try {
            Long userId = tokenService.getUserId();
            QueryWrapper<ViolationLogEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            wrapper.orderByDesc("create_time");
            List<ViolationLogEntity> rows = violationLogMapper.selectList(wrapper);

            ViolationListVO result = new ViolationListVO();
            result.setTotalCount(rows == null ? 0L : (long) rows.size());
            if (rows == null || rows.isEmpty()) {
                result.setList(java.util.Collections.emptyList());
                return ApiResponse.success(result);
            }
            result.setList(rows.stream().map(r -> {
                ViolationItemVO vo = new ViolationItemVO();
                vo.setId(r.getId());
                vo.setBookingId(r.getBookingId());
                vo.setReason(r.getReason());
                vo.setPenaltyAmount(r.getPenaltyAmount());
                vo.setCreateTime(r.getCreateTime());
                return vo;
            }).collect(Collectors.toList()));
            return ApiResponse.success(result);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取违约记录失败", e);
            throw new CommonJsonException("获取违约记录失败，请稍后重试");
        }
    }
//
//    @Operation(summary = "查询可报名活动列表", description = "查询所有活动，并标注用户报名状态。0-未报名，1-待支付（包含支付订单信息），2-支付完成",
//            parameters = {
//                    @Parameter(name = "token", description = "JWT访问令牌", required = true, in = ParameterIn.HEADER)
//            })
//    @GetMapping("/activities")
//    @RequiresPermissions(value = "app:apply:query", apiAuth = {ApiAuth.USER})
//    public ApiResponse<List<ActivityVO>> getActivityList() {
//        try {
//            Long userId = tokenService.getUserId();
//            List<ActivityVO> activities = appUserService.getActivityListForApp(userId);
//            return ApiResponse.success(activities);
//        } catch (CommonJsonException e) {
//            throw e;
//        } catch (Exception e) {
//            log.error("查询活动列表失败", e);
//            throw new CommonJsonException("查询活动列表失败，请稍后重试");
//        }
//    }
//
//    @Operation(summary = "查询已报名活动列表", description = "查询用户已报名的活动，包含支付订单信息和退款记录（含审核记录）",
//            parameters = {
//                    @Parameter(name = "token", description = "JWT访问令牌", required = true, in = ParameterIn.HEADER)
//            })
//    @GetMapping("/enrolled-activities")
//    @RequiresPermissions(value = "app:enrolled:query", apiAuth = {ApiAuth.USER})
//    public ApiResponse<List<EnrolledActivityVO>> getEnrolledActivityList() {
//        try {
//            Long userId = tokenService.getUserId();
//            List<EnrolledActivityVO> enrolledActivities = appUserService.getEnrolledActivityList(userId);
//            return ApiResponse.success(enrolledActivities);
//        } catch (CommonJsonException e) {
//            throw e;
//        } catch (Exception e) {
//            log.error("查询已报名活动列表失败", e);
//            throw new CommonJsonException("查询已报名活动列表失败，请稍后重试");
//        }
//    }
//
//    @Operation(summary = "用户报名活动", description = "用户报名活动，支持名额限制，创建30分钟有效的支付订单，超时未支付自动释放名额。5秒内同一用户同一活动只能报名一次",
//            parameters = {
//                    @Parameter(name = "token", description = "JWT访问令牌", required = true, in = ParameterIn.HEADER)
//            })
//    @PostMapping("/apply")
//    @RequiresPermissions(value = "app:apply:create", apiAuth = {ApiAuth.USER})
//    public ApiResponse<ApplyResponseVO> applyActivity(@Valid @RequestBody ApplyRequestDTO request) {
//        try {
//            Long userId = tokenService.getUserId();
//            ApplyResponseVO response = appUserService.applyActivity(userId, request);
//            return ApiResponse.success(response);
//        } catch (CommonJsonException e) {
//            throw e;
//        } catch (Exception e) {
//            log.error("报名活动失败", e);
//            throw new CommonJsonException("报名活动失败，请稍后重试");
//        }
//    }

    @Operation(summary = "申请退款", description = "用户申请退款，支持审核拒绝后重新发起。一个订单只存在一条申请退款的记录",
            parameters = {
                    @Parameter(name = "token", description = "JWT访问令牌", required = true, in = ParameterIn.HEADER)
            })
    @PostMapping("/refund/apply")
    @RequiresPermissions(value = "app:refund:create", apiAuth = {ApiAuth.USER})
    public ApiResponse<RefundResponseVO> applyRefund(@Valid @RequestBody RefundRequestDTO request) {
        try {
            Long userId = tokenService.getUserId();
            RefundResponseVO response = appUserService.applyRefund(userId, request);
            return ApiResponse.success(response);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("申请退款失败", e);
            throw new CommonJsonException("申请退款失败，请稍后重试");
        }
    }
}
