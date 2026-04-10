package org.example.controller;

import com.alibaba.fastjson2.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.aop.annotation.ApiAuth;
import org.example.aop.annotation.RequiresPermissions;
import org.example.common.ApiResponse;
import org.example.config.exception.CommonJsonException;
import org.example.dto.LoginRequestDTO;
import org.example.dto.admin.BookingForceCancelDTO;
import org.example.dto.admin.CourtAddDTO;
import org.example.dto.admin.CourtUpdateDTO;
import org.example.dto.admin.UserStatusDTO;
import org.example.dto.admin.VenueAddDTO;
import org.example.dto.admin.VenueUpdateDTO;
import org.example.service.TokenService;
import org.example.service.basic.user.UserService;
import org.example.service.sys.admin.AdminService;
import org.example.service.venue.VenueService;
import org.example.utils.exception.CommonUtil;
import org.example.vo.CaptchaVO;
import org.example.vo.LoginResponseVO;
import org.example.vo.admin.VenueListItemVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

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

    @Resource
    private UserService userService;

    @Resource
    private TokenService tokenService;

    @Resource
    private VenueService venueService;

    @Operation(summary = "管理员登录", description = "用户登录验证，包含验证码校验。登录成功返回JWT令牌和用户信息")
    @PostMapping("/login")
    public ApiResponse<LoginResponseVO> login(@RequestBody LoginRequestDTO loginRequest) {
        try {
            log.info("用户登录，登录参数：{}", loginRequest.getCaptchaCode());
            LoginResponseVO response = adminService.login(loginRequest);
            log.info("管理员登录成功: {}", response.getAccessToken());
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
            log.info("用户获取验证码成功，验证码：{},{}", captcha.getCaptchaKey(), captcha.getCaptchaImage());
            return ApiResponse.success(captcha);
        } catch (Exception e) {
            log.error("获取验证码失败", e);
            throw new CommonJsonException("获取验证码失败");
        }
    }

    @Operation(summary = "刷新令牌", description = "通过refreshToken获取新的accessToken和refreshToken，刷新成功后旧的refreshToken将失效")
    @PostMapping("/refresh-token")
    public ApiResponse<LoginResponseVO> refreshToken(HttpServletRequest request) {
        try {
            JSONObject param = CommonUtil.getJsonObject(request);
            String refreshToken = param.getString("refreshToken");
            LoginResponseVO response = adminService.refreshToken(refreshToken);
            return ApiResponse.success(response);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("刷新令牌失败", e);
            throw new CommonJsonException("刷新令牌失败，请稍后重试");
        }
    }

    @Operation(summary = "获取用户列表", description = "管理端获取用户分页列表，支持关键词搜索和状态筛选",
            parameters = {
                    @Parameter(name = "Authorization", description = "Bearer Token", required = true, in = ParameterIn.HEADER)
            })
    @GetMapping("/user/list")
    @RequiresPermissions(value = "admin:user:list", apiAuth = {ApiAuth.ADMIN})
    public ApiResponse<Map<String, Object>> getUserList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        try {
            Map<String, Object> result = userService.getUserPageList(keyword, status, page, size);
            return ApiResponse.success(result);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            throw new CommonJsonException("获取用户列表失败");
        }
    }

    @Operation(summary = "删除用户", description = "管理端删除指定用户",
            parameters = {
                    @Parameter(name = "Authorization", description = "Bearer Token", required = true, in = ParameterIn.HEADER)
            })
    @DeleteMapping("/user/{userId}")
    @RequiresPermissions(value = "admin:user:delete", apiAuth = {ApiAuth.ADMIN})
    public ApiResponse<Void> deleteUser(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId
    ) {
        try {
            userService.deleteUser(userId);
            return ApiResponse.success("删除成功", null);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除用户失败", e);
            throw new CommonJsonException("删除用户失败");
        }
    }

    @Operation(summary = "禁用/启用用户", description = "管理端更新用户状态",
            parameters = {
                    @Parameter(name = "Authorization", description = "Bearer Token", required = true, in = ParameterIn.HEADER)
            })
    @PutMapping("/user/{userId}/status")
    @RequiresPermissions(value = "admin:user:status", apiAuth = {ApiAuth.ADMIN})
    public ApiResponse<Void> updateUserStatus(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Valid @RequestBody UserStatusDTO dto
    ) {
        try {
            userService.updateUserStatus(userId, dto.getStatus());
            return ApiResponse.success("操作成功", null);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新用户状态失败", e);
            throw new CommonJsonException("操作失败");
        }
    }

    @Operation(summary = "获取场地列表", description = "管理端获取场地分页列表，支持按场馆、类型、状态筛选",
            parameters = {
                    @Parameter(name = "Authorization", description = "Bearer Token", required = true, in = ParameterIn.HEADER)
            })
    @GetMapping("/court/list")
    @RequiresPermissions(value = "admin:court:list", apiAuth = {ApiAuth.ADMIN})
    public ApiResponse<Map<String, Object>> getCourtList(
            @RequestParam(required = false) Long venueId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        try {
            Map<String, Object> result = venueService.getCourtPageList(venueId, type, status, page, size);
            return ApiResponse.success(result);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取场地列表失败", e);
            throw new CommonJsonException("获取场地列表失败");
        }
    }

    @Operation(summary = "添加场地", description = "管理端添加新场地",
            parameters = {
                    @Parameter(name = "Authorization", description = "Bearer Token", required = true, in = ParameterIn.HEADER)
            })
    @PostMapping("/court")
    @RequiresPermissions(value = "admin:court:add", apiAuth = {ApiAuth.ADMIN})
    public ApiResponse<Long> addCourt(@Valid @RequestBody CourtAddDTO dto) {
        try {
            Long courtId = venueService.addCourt(dto);
            return ApiResponse.success("添加成功", courtId);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("添加场地失败", e);
            throw new CommonJsonException("添加场地失败");
        }
    }

    @Operation(summary = "修改场地", description = "管理端修改场地信息",
            parameters = {
                    @Parameter(name = "Authorization", description = "Bearer Token", required = true, in = ParameterIn.HEADER)
            })
    @PutMapping("/court/{courtId}")
    @RequiresPermissions(value = "admin:court:update", apiAuth = {ApiAuth.ADMIN})
    public ApiResponse<Void> updateCourt(
            @Parameter(description = "场地ID", required = true) @PathVariable Long courtId,
            @Valid @RequestBody CourtUpdateDTO dto
    ) {
        try {
            venueService.updateCourt(courtId, dto);
            return ApiResponse.success("修改成功", null);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("修改场地失败", e);
            throw new CommonJsonException("修改场地失败");
        }
    }

    @Operation(summary = "删除场地", description = "管理端删除场地（软删除）",
            parameters = {
                    @Parameter(name = "Authorization", description = "Bearer Token", required = true, in = ParameterIn.HEADER)
            })
    @DeleteMapping("/court/{courtId}")
    @RequiresPermissions(value = "admin:court:delete", apiAuth = {ApiAuth.ADMIN})
    public ApiResponse<Void> deleteCourt(
            @Parameter(description = "场地ID", required = true) @PathVariable Long courtId
    ) {
        try {
            venueService.deleteCourt(courtId);
            return ApiResponse.success("删除成功", null);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除场地失败", e);
            throw new CommonJsonException("删除场地失败");
        }
    }

    @Operation(summary = "获取场馆列表", description = "管理端获取所有场馆列表",
            parameters = {
                    @Parameter(name = "Authorization", description = "Bearer Token", required = true, in = ParameterIn.HEADER)
            })
    @GetMapping("/venue/list")
    @RequiresPermissions(value = "admin:venue:list", apiAuth = {ApiAuth.ADMIN})
    public ApiResponse<List<VenueListItemVO>> getVenueList() {
        try {
            List<VenueListItemVO> result = venueService.getVenueList();
            return ApiResponse.success(result);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取场馆列表失败", e);
            throw new CommonJsonException("获取场馆列表失败");
        }
    }

    @Operation(summary = "添加场馆", description = "管理端添加新场馆",
            parameters = {
                    @Parameter(name = "Authorization", description = "Bearer Token", required = true, in = ParameterIn.HEADER)
            })
    @PostMapping("/venue")
    @RequiresPermissions(value = "admin:venue:add", apiAuth = {ApiAuth.ADMIN})
    public ApiResponse<Long> addVenue(@Valid @RequestBody VenueAddDTO dto) {
        try {
            Long venueId = venueService.addVenue(dto);
            return ApiResponse.success("添加成功", venueId);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("添加场馆失败", e);
            throw new CommonJsonException("添加场馆失败");
        }
    }

    @Operation(summary = "修改场馆", description = "管理端修改场馆信息",
            parameters = {
                    @Parameter(name = "Authorization", description = "Bearer Token", required = true, in = ParameterIn.HEADER)
            })
    @PutMapping("/venue/{venueId}")
    @RequiresPermissions(value = "admin:venue:update", apiAuth = {ApiAuth.ADMIN})
    public ApiResponse<Void> updateVenue(
            @Parameter(description = "场馆ID", required = true) @PathVariable Long venueId,
            @Valid @RequestBody VenueUpdateDTO dto
    ) {
        try {
            venueService.updateVenue(venueId, dto);
            return ApiResponse.success("修改成功", null);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("修改场馆失败", e);
            throw new CommonJsonException("修改场馆失败");
        }
    }

    @Operation(summary = "删除场馆", description = "管理端删除场馆（软删除）",
            parameters = {
                    @Parameter(name = "Authorization", description = "Bearer Token", required = true, in = ParameterIn.HEADER)
            })
    @DeleteMapping("/venue/{venueId}")
    @RequiresPermissions(value = "admin:venue:delete", apiAuth = {ApiAuth.ADMIN})
    public ApiResponse<Void> deleteVenue(
            @Parameter(description = "场馆ID", required = true) @PathVariable Long venueId
    ) {
        try {
            venueService.deleteVenue(venueId);
            return ApiResponse.success("删除成功", null);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除场馆失败", e);
            throw new CommonJsonException("删除场馆失败");
        }
    }

    @Operation(summary = "获取预约列表", description = "管理端获取预约分页列表，支持按状态、用户、场地、日期筛选",
            parameters = {
                    @Parameter(name = "Authorization", description = "Bearer Token", required = true, in = ParameterIn.HEADER)
            })
    @GetMapping("/booking/list")
    @RequiresPermissions(value = "admin:booking:list", apiAuth = {ApiAuth.ADMIN})
    public ApiResponse<Map<String, Object>> getBookingList(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String courtName,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        try {
            Map<String, Object> result = venueService.getBookingPageList(status, username, courtName, startDate, endDate, page, size);
            return ApiResponse.success(result);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取预约列表失败", e);
            throw new CommonJsonException("获取预约列表失败");
        }
    }

    @Operation(summary = "删除预约", description = "管理端删除预约记录",
            parameters = {
                    @Parameter(name = "Authorization", description = "Bearer Token", required = true, in = ParameterIn.HEADER)
            })
    @DeleteMapping("/booking/{bookingId}")
    @RequiresPermissions(value = "admin:booking:delete", apiAuth = {ApiAuth.ADMIN})
    public ApiResponse<Void> deleteBooking(
            @Parameter(description = "预约ID", required = true) @PathVariable Long bookingId
    ) {
        try {
            venueService.deleteBooking(bookingId);
            return ApiResponse.success("删除成功", null);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除预约失败", e);
            throw new CommonJsonException("删除预约失败");
        }
    }

    @Operation(summary = "强制取消预约", description = "管理端强制取消预约",
            parameters = {
                    @Parameter(name = "Authorization", description = "Bearer Token", required = true, in = ParameterIn.HEADER)
            })
    @PostMapping("/booking/{bookingId}/force-cancel")
    @RequiresPermissions(value = "admin:booking:forceCancel", apiAuth = {ApiAuth.ADMIN})
    public ApiResponse<Void> forceCancelBooking(
            @Parameter(description = "预约ID", required = true) @PathVariable Long bookingId,
            @Valid @RequestBody BookingForceCancelDTO dto
    ) {
        try {
            venueService.forceCancelBooking(bookingId, dto.getReason());
            return ApiResponse.success("已强制取消，押金已退还", null);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("强制取消预约失败", e);
            throw new CommonJsonException("强制取消预约失败");
        }
    }
}
