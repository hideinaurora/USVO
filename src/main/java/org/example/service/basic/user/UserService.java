package org.example.service.basic.user;


import org.example.dto.AppLoginRequestDTO;
import org.example.dto.AppRegisterRequestDTO;
import org.example.entity.basic.user.UserEntity;
import org.example.vo.AppLoginResponseVO;
import org.example.vo.admin.UserListItemVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

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

    /**
     * 微信小程序登录
     *
     * @param wxId 微信openId
     * @return 登录响应信息（包含token）
     */
    AppLoginResponseVO wxLogin(String wxId);

    /**
     * 获取用户分页列表（管理端）
     *
     * @param keyword 搜索关键词 (用户名 / 手机号)
     * @param status 状态 (1 正常 0 禁用)
     * @param page 页码
     * @param size 每页条数
     * @return 分页结果
     */
    Map<String, Object> getUserPageList(String keyword, Integer status, Integer page, Integer size);

    /**
     * 删除用户（管理端）
     *
     * @param userId 用户ID
     */
    void deleteUser(Long userId);

    /**
     * 更新用户状态（管理端）
     *
     * @param userId 用户ID
     * @param status 状态 (1 正常 0 禁用)
     */
    void updateUserStatus(Long userId, Integer status);
}
