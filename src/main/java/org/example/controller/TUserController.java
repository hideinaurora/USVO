package org.example.controller;


import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.dto.TokenDTO;
import org.example.mapper.TMotionPointsMapper;
import org.example.mapper.TUserMapper;
import org.example.entity.TMotionPointsEntity;
import org.example.entity.TUserEntity;
import org.example.utils.JWTUtil;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 吴子木
 * @since 2024-09-23
 */
@RestController
@RequestMapping("/api/wechat")
public class TUserController {
    @Autowired
    private TUserMapper tUserMapper;
    @Autowired
    private WxMpService wxMpService;

    @Autowired
    private TMotionPointsMapper tMotionPointsMapper;

    @RequestMapping(value = "/create",method = RequestMethod.POST)
    public ResponseEntity<?> userCreate(@RequestBody Map<String, String> requestBody) {
        try {
            JSONObject result = new JSONObject();
            String code = requestBody.get("code");
            if (code == null || code.isEmpty()) {
                return ResponseEntity.badRequest().body("微信 code 不能为空");
            }

            WxOAuth2AccessToken wxMpOAuth2AccessToken = wxMpService.getOAuth2Service().getAccessToken(code);
            String openId = wxMpOAuth2AccessToken.getOpenId();
            QueryWrapper<TUserEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("wx_openid", openId);
            TUserEntity existingUser = tUserMapper.selectOne(queryWrapper);

            if (existingUser != null) {
                // 如果用户已存在，直接返回 openId
                TokenDTO token = new TokenDTO();
                token.setAccountId(existingUser.getId());
                token.setRoleId(2);
                result.put("token", JWTUtil.createSign(token.toString(),240));
                result.put("wxOpenid",openId);
                existingUser.setTempState("0");//设置测温状态为0
                return ResponseEntity.ok(result);
            } else {
                // 如果用户不存在，创建新用户，设置状态并保存
                TUserEntity newUser = new TUserEntity();
                newUser.setWxOpenid(openId);
                newUser.setRegStatus("0");  // 设置注册状态为未注册
                newUser.setRegStatus("0"); //设置测温状态为0
                tUserMapper.insert(newUser);
                TokenDTO token = new TokenDTO();
                token.setAccountId(newUser.getId());
                token.setRoleId(2);
                result.put("token", JWTUtil.createSign(token.toString(),240));
                result.put("wxOpenid",openId);
                return ResponseEntity.ok(result);
            }
        } catch (Exception e) {
            // 处理错误并返回错误信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("处理失败: " + e.getMessage());
        }
    }
    @RequestMapping(value = "/update",method = RequestMethod.POST)
    public ResponseEntity<?> userUpdate(@RequestBody Map<String,String> requestBody) {
        try {
            String wxOpenid = requestBody.get("wxOpenid");
            String userName = requestBody.get("userName");
            String telePhone = requestBody.get("telePhone");
            QueryWrapper<TUserEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("wx_openid", wxOpenid);
            TUserEntity existingUser = tUserMapper.selectOne(queryWrapper);

            if (existingUser == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }
            if ("1".equals(existingUser.getRegStatus())) {
                return ResponseEntity.badRequest().body("用户已注册,请扫描测温码");
            }
            existingUser.setUserName(userName);
            existingUser.setTelePhone(telePhone);
            existingUser.setRegStatus("1");//设置状态为已注册
            tUserMapper.updateById(existingUser);

            return ResponseEntity.ok("用户注册成功");
        } catch (Exception e) {
            // 处理错误并返回错误信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("注册失败: " + e.getMessage());
        }
    }
    @RequestMapping(value = "/temp", method = RequestMethod.POST)
    public ResponseEntity<?> updateTemperatureStatus(@RequestBody Map<String, String> requestBody) {
        try {
            String wxOpenid = requestBody.get("wxOpenid");
            // 查询用户
            QueryWrapper<TUserEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("wx_openid", wxOpenid);
            TUserEntity existingUser = tUserMapper.selectOne(queryWrapper);

            // 判断用户是否存在
            if (existingUser == null) {
                return ResponseEntity.badRequest().body("请先扫注册码完成注册");
            }

            // 检查用户的注册状态
            if ("0".equals(existingUser.getRegStatus())) {
                return ResponseEntity.badRequest().body("请先扫注册码完成注册");
            }

            // 将所有用户的 tempState 置为 0
            TUserEntity updateAllUsers = new TUserEntity();
            updateAllUsers.setTempState("0");
            tUserMapper.update(updateAllUsers, new QueryWrapper<>()); // 无条件更新所有记录

            // 更新当前用户的 tempState 为 1
            existingUser.setTempState("1");
            tUserMapper.updateById(existingUser);

            // 创建新的测温数据并将 userId 设置为用户的 id
            TMotionPointsEntity motionPointsEntity = new TMotionPointsEntity();
            motionPointsEntity.setUserId(Math.toIntExact(existingUser.getId()));
            tMotionPointsMapper.insert(motionPointsEntity);

            return ResponseEntity.ok(existingUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("更新失败: " + e.getMessage());
        }
    }

}
