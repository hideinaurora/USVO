package org.example.service.basic.user.impl;


import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.config.exception.CommonJsonException;
import org.example.dto.AppLoginRequestDTO;
import org.example.dto.AppRegisterRequestDTO;
import org.example.entity.basic.user.UserEntity;
import org.example.mapper.basic.user.UserMapper;
import org.example.service.basic.user.UserService;
import org.example.utils.StringTools;
import org.example.vo.AppLoginResponseVO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 用户信息表 服务实现类
 * </p>
 *
 * @author ckd
 * @since 2026-03-13
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    private static final int TOKEN_EXPIRE_MINUTES = 720;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppLoginResponseVO register(AppRegisterRequestDTO registerRequest) {
        // 1. 参数校验
        if (StringTools.isNullOrEmpty(registerRequest.getLoginName())) {
            throw new CommonJsonException("登录名不能为空");
        }
        if (StringTools.isNullOrEmpty(registerRequest.getLoginPassword())) {
            throw new CommonJsonException("密码不能为空");
        }
        if (StringTools.isNullOrEmpty(registerRequest.getUserName())) {
            throw new CommonJsonException("用户名不能为空");
        }

        // 2. 校验登录名是否已存在
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("login_name", registerRequest.getLoginName());
        UserEntity existUser = getOne(queryWrapper);
        if (existUser != null) {
            throw new CommonJsonException("该登录名已被注册");
        }

        // 3. 密码加密
        String encryptedPassword = DigestUtils.md5DigestAsHex(registerRequest.getLoginPassword().getBytes());

        // 4. 创建用户
        UserEntity user = new UserEntity();
        user.setLoginName(registerRequest.getLoginName());
        user.setLoginPassword(encryptedPassword);
        user.setUserName(registerRequest.getUserName());
        user.setUserStatus(1); // 默认正常状态
        user.setIsDeleted(0); // 未删除
        if (registerRequest.getWxId() != null) {
            user.setWxId(registerRequest.getWxId());
        }

        // 5. 保存用户
        boolean saved = save(user);
        if (!saved) {
            throw new CommonJsonException("注册失败，请稍后重试");
        }

        log.info("用户注册成功: loginName={}", registerRequest.getLoginName());

        // 6. 生成Token
        JSONObject tokenData = new JSONObject();
        tokenData.put("accountId", user.getId());
        tokenData.put("roleId", 2); // 移动端用户角色ID
        String token = org.example.utils.JWTUtil.createSign(tokenData.toString(), TOKEN_EXPIRE_MINUTES);

        // 7. 构建响应
        AppLoginResponseVO response = new AppLoginResponseVO();
        response.setAccessToken(token);
        response.setUserId(user.getId());
        response.setLoginName(user.getLoginName());
        response.setUserName(user.getUserName());
        response.setRoleId(2); // 移动端用户角色ID

        return response;
    }

    @Override
    public AppLoginResponseVO login(AppLoginRequestDTO loginRequest) {
        // 1. 参数校验
        if (StringTools.isNullOrEmpty(loginRequest.getLoginName())) {
            throw new CommonJsonException("登录名不能为空");
        }
        if (StringTools.isNullOrEmpty(loginRequest.getLoginPassword())) {
            throw new CommonJsonException("密码不能为空");
        }

        // 2. 查询用户
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("login_name", loginRequest.getLoginName());
        UserEntity user = getOne(queryWrapper);

        if (user == null) {
            throw new CommonJsonException("用户不存在");
        }

        // 3. 校验用户状态
        if (user.getUserStatus() == null || user.getUserStatus() != 1) {
            throw new CommonJsonException("用户已被禁用");
        }

        // 4. 密码校验
        String encryptedPassword = DigestUtils.md5DigestAsHex(loginRequest.getLoginPassword().getBytes());
        if (!encryptedPassword.equals(user.getLoginPassword())) {
            throw new CommonJsonException("密码错误");
        }

        // 5. 生成Token
        JSONObject tokenData = new JSONObject();
        tokenData.put("accountId", user.getId());
        tokenData.put("roleId", 2); // 移动端用户角色ID
        String token = org.example.utils.JWTUtil.createSign(tokenData.toString(), TOKEN_EXPIRE_MINUTES);

        // 6. 构建响应
        AppLoginResponseVO response = new AppLoginResponseVO();
        response.setAccessToken(token);
        response.setUserId(user.getId());
        response.setLoginName(user.getLoginName());
        response.setUserName(user.getUserName());
        response.setRoleId(2); // 移动端用户角色ID

        log.info("移动端用户登录成功: {}", user.getLoginName());
        return response;
    }
}
