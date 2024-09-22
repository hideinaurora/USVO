package org.example.aop.filter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.aop.annotation.ApiAuth;
import org.example.aop.annotation.RequiresPermissions;
import org.example.config.exception.CommonJsonException;
import org.example.config.exception.UnauthorizedException;
import org.example.dto.TokenDTO;
import org.example.utils.JWTUtil;
import org.example.utils.RedisUtil;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * [角色权限]控制拦截器
 */
@Aspect
@Slf4j
@Component
@Order(3)
public class PermissionAspect {

    @Resource
    private RedisUtil redisUtil;

    @Before("@annotation(org.example.aop.annotation.RequiresPermissions)")
    public void before(JoinPoint joinPoint) {
        log.debug("开始校验[操作权限]");
        try {
            String token = MDC.get("token");
            TokenDTO tokenDTO = JWTUtil.verifyToken(token);
            if (!hasPermission(joinPoint, tokenDTO)) {
                throw new CommonJsonException("权限不符");
            }
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            throw new UnauthorizedException();
        }
    }

    /**
     * @param joinPoint 切点
     * @param tokenDTO  用户token
     * @return 校验用户是否拥有接口权限
     * @author ckd
     * @date 2022/11/9 17:43
     */
    private Boolean hasPermission(JoinPoint joinPoint, TokenDTO tokenDTO) {
        boolean flag = false;
        // 当接口存在权限信息时，需校验token是否有权限
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        RequiresPermissions a = methodSignature.getMethod().getAnnotation(RequiresPermissions.class);
        // 基于authGroup匹配是否存在对应角色
        ApiAuth[] auth = a.apiAuth();
        for (ApiAuth logical : auth) {
            if (logical.getType().equals(tokenDTO.getRoleId())) {
                flag = true;
                break;
            }
        }
        return flag;
    }
}
