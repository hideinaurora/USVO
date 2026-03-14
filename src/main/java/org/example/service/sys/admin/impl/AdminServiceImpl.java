package org.example.service.sys.admin.impl;


import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.config.exception.CommonJsonException;
import org.example.dto.LoginRequestDTO;
import org.example.entity.sys.admin.AdminEntity;
import org.example.mapper.sys.admin.AdminMapper;
import org.example.service.sys.admin.AdminService;
import org.example.utils.RedisUtil;
import org.example.utils.StringTools;
import org.example.vo.CaptchaVO;
import org.example.vo.LoginResponseVO;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 系统用户表 服务实现类
 * </p>
 *
 * @author ckd
 * @since 2026-03-13
 */
@Slf4j
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, AdminEntity> implements AdminService {

    private static final String CAPTCHA_PREFIX = "admin:captcha:";
    private static final long CAPTCHA_EXPIRE_MINUTES = 5;
    private static final int TOKEN_EXPIRE_MINUTES = 720;
    private static final long REFRESH_TOKEN_EXPIRE_DAYS = 30;
    private static final String REFRESH_TOKEN_PREFIX = "admin:refresh:";

    @Resource
    private RedisUtil redisUtil;

    @Override
    public LoginResponseVO login(LoginRequestDTO loginRequest) {
        // 1. 参数校验
        if (StringTools.isNullOrEmpty(loginRequest.getLoginName())) {
            throw new CommonJsonException("登录名不能为空");
        }
        if (StringTools.isNullOrEmpty(loginRequest.getLoginPassword())) {
            throw new CommonJsonException("密码不能为空");
        }
        if (StringTools.isNullOrEmpty(loginRequest.getCaptchaKey())) {
            throw new CommonJsonException("验证码key不能为空");
        }
        if (StringTools.isNullOrEmpty(loginRequest.getCaptchaCode())) {
            throw new CommonJsonException("验证码不能为空");
        }

        // 2. 验证码校验
        String captchaKey = CAPTCHA_PREFIX + loginRequest.getCaptchaKey();
        Object storedCaptcha = redisUtil.get(captchaKey);
        if (storedCaptcha == null) {
            throw new CommonJsonException("验证码已过期，请重新获取");
        }
        String storedCode = storedCaptcha.toString();
        if (!storedCode.equalsIgnoreCase(loginRequest.getCaptchaCode())) {
            throw new CommonJsonException("验证码错误");
        }
        // 删除已使用的验证码
        redisUtil.remove(captchaKey);

        // 3. 查询用户
        QueryWrapper<AdminEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("login_name", loginRequest.getLoginName());
        AdminEntity admin = getOne(queryWrapper);

        if (admin == null) {
            throw new CommonJsonException("用户不存在");
        }

        // 4. 校验用户状态
        if (admin.getUserStatus() == null || admin.getUserStatus() != 1) {
            throw new CommonJsonException("用户已被禁用");
        }

        // 5. 密码校验
        String encryptedPassword = DigestUtils.md5DigestAsHex(loginRequest.getLoginPassword().getBytes());
        if (!encryptedPassword.equals(admin.getLoginPassword())) {
            throw new CommonJsonException("密码错误");
        }

        // 6. 生成Token
        JSONObject tokenData = new JSONObject();
        tokenData.put("accountId", admin.getId());
        tokenData.put("roleId", 1); // 默认角色ID，可根据实际业务调整
        String token = org.example.utils.JWTUtil.createSign(tokenData.toString(), TOKEN_EXPIRE_MINUTES);

        // 生成refreshToken
        String refreshToken = UUID.randomUUID().toString().replace("-", "");
        String refreshTokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
        // 存储refreshToken到Redis，关联用户ID，30天过期
        redisUtil.set(refreshTokenKey, admin.getId(), REFRESH_TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);

        // 获取accessToken过期时间
        Date expires = org.example.utils.JWTUtil.getExpiresDate(token);

        // 7. 构建响应
        LoginResponseVO response = new LoginResponseVO();
        response.setAccessToken(token);
        response.setRefreshToken(refreshToken);
        response.setExpires(expires);
        response.setUserId(admin.getId());
        response.setUserName(admin.getLoginName());
        response.setRoles(Collections.singletonList("管理员"));

        log.info("管理员登录成功: {}", admin.getLoginName());
        return response;
    }

    @Override
    public CaptchaVO generateCaptcha() {
        // 1. 生成验证码
        JSONObject captchaData = org.example.utils.CaptchaUtil.generateCaptcha();
        String code = captchaData.getString("code");
        String image = captchaData.getString("image");

        // 2. 生成验证码key
        String captchaKey = UUID.randomUUID().toString();

        // 3. 存储到Redis，5分钟过期
        String redisKey = CAPTCHA_PREFIX + captchaKey;
        redisUtil.set(redisKey, code, CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 4. 构建响应
        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setCaptchaKey(captchaKey);
        captchaVO.setCaptchaImage(image);

        return captchaVO;
    }

    @Override
    public LoginResponseVO refreshToken(String refreshToken) {
        // 1. 参数校验
        if (StringTools.isNullOrEmpty(refreshToken)) {
            throw new CommonJsonException("刷新令牌不能为空");
        }

        // 2. 验证refreshToken是否有效
        String refreshTokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
        Object userIdObj = redisUtil.get(refreshTokenKey);
        if (userIdObj == null) {
            throw new CommonJsonException("刷新令牌已过期或不存在");
        }

        Long userId = Long.parseLong(userIdObj.toString());

        // 3. 查询用户信息
        AdminEntity admin = getById(userId);
        if (admin == null) {
            throw new CommonJsonException("用户不存在");
        }

        // 4. 校验用户状态
        if (admin.getUserStatus() == null || admin.getUserStatus() != 1) {
            throw new CommonJsonException("用户已被禁用");
        }

        // 5. 生成新的Token
        JSONObject tokenData = new JSONObject();
        tokenData.put("accountId", admin.getId());
        tokenData.put("roleId", 1); // 默认角色ID
        String newToken = org.example.utils.JWTUtil.createSign(tokenData.toString(), TOKEN_EXPIRE_MINUTES);

        // 6. 生成新的refreshToken
        String newRefreshToken = UUID.randomUUID().toString().replace("-", "");
        String newRefreshTokenKey = REFRESH_TOKEN_PREFIX + newRefreshToken;
        // 存储新的refreshToken到Redis
        redisUtil.set(newRefreshTokenKey, admin.getId(), REFRESH_TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);
        // 删除旧的refreshToken
        redisUtil.remove(refreshTokenKey);

        // 7. 获取新的accessToken过期时间
        Date expires = org.example.utils.JWTUtil.getExpiresDate(newToken);

        // 8. 构建响应
        LoginResponseVO response = new LoginResponseVO();
        response.setAccessToken(newToken);
        response.setRefreshToken(newRefreshToken);
        response.setExpires(expires);
        response.setUserId(admin.getId());
        response.setUserName(admin.getLoginName());
        response.setRoles(Collections.singletonList("管理员"));

        log.info("管理员刷新令牌成功: {}", admin.getLoginName());
        return response;
    }
}
