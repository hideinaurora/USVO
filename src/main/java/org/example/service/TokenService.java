package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.config.exception.CommonJsonException;
import org.example.dto.TokenDTO;
import org.example.utils.JWTUtil;
import org.example.utils.StringTools;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TokenService {

    public Long getUserId() {
        try {
            String token = MDC.get("token");
            if (StringTools.isNullOrEmpty(token)) {
                throw new CommonJsonException("获取登录信息失败");
            }
            TokenDTO tokenDTO = JWTUtil.verifyToken(token);
            if (tokenDTO == null || tokenDTO.getAccountId() == null) {
                throw new CommonJsonException("获取ID失败");
            }
            return tokenDTO.getAccountId();
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取ID失败", e);
            throw new CommonJsonException("获取ID失败");
        }
    }
}
