package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.common.ApiResponse;
import org.example.config.exception.CommonJsonException;
import org.example.dto.WxLoginRequestDTO;
import org.example.service.basic.user.UserService;
import org.example.utils.WxUtils;
import org.example.vo.AppLoginResponseVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 微信相关控制器
 *
 * @author ckd
 * @since 2026-03-13
 */
@Slf4j
@RestController
@RequestMapping("/api/wechat")
@Tag(name = "微信接口", description = "微信小程序登录相关接口")
public class WechatController {

    @Resource
    private WxUtils wxUtils;

    @Resource
    private UserService userService;

    @Operation(summary = "微信小程序登录", description = "通过微信小程序code进行登录，返回JWT令牌")
    @PostMapping("/login/code")
    public ApiResponse<AppLoginResponseVO> wxLogin(@Valid @RequestBody WxLoginRequestDTO request) {
        try {
            // 1. 参数校验
            if (request.getCode() == null || request.getCode().isEmpty()) {
                throw new CommonJsonException("微信code不能为空");
            }

            // 2. 调用微信接口获取openId
            String openId = wxUtils.queryMiniOpenId(request.getCode());
            log.info("获取微信openId成功: openId={}", openId);

            // 3. 通过openId查询用户并登录
            AppLoginResponseVO response = userService.wxLogin(openId);

            return ApiResponse.success(response);

        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("微信登录失败", e);
            throw new CommonJsonException("微信登录失败，请稍后重试");
        }
    }
}
