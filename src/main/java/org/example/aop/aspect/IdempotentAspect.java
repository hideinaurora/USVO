package org.example.aop.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.aop.annotation.ApiIdempotent;
import org.example.config.exception.CommonJsonException;
import org.example.utils.RedisUtil;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * API接口幂等性切面
 * 通过Token + 请求参数的方式实现幂等性控制
 *
 * @author ckd
 * @since 2026-03-13
 */
@Slf4j
@Aspect
@Component
@Order(2)
public class IdempotentAspect {

    private static final String IDEMPOTENT_PREFIX = "idempotent:";

    @Resource
    private RedisUtil redisUtil;

    @Around("@annotation(org.example.aop.annotation.ApiIdempotent)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取请求对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new CommonJsonException("请求上下文获取失败");
        }

        HttpServletRequest request = attributes.getRequest();

        // 2. 获取Token
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            throw new CommonJsonException("请先登录");
        }

        // 3. 获取方法签名和注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        ApiIdempotent idempotent = method.getAnnotation(ApiIdempotent.class);

        // 4. 生成幂等性Key：Token + 方法名 + 请求参数
        String methodName = method.getName();
        String requestURI = request.getRequestURI();
        String idempotentKey = IDEMPOTENT_PREFIX + token + ":" + requestURI;

        // 5. 检查是否已经处理过该请求
        boolean hasKey = redisUtil.exists(idempotentKey);
        if (hasKey) {
            log.warn("重复请求被拦截: token={}, uri={}", token, requestURI);
            throw new CommonJsonException("请勿重复提交，请稍后再试");
        }

        // 6. 设置幂等性标识
        long expireSeconds = idempotent.expireSeconds();
        redisUtil.set(idempotentKey, "1", expireSeconds, TimeUnit.SECONDS);

        try {
            // 7. 执行业务逻辑
            return joinPoint.proceed();
        } catch (Exception e) {
            // 如果业务执行失败，删除幂等性标识，允许重试
            redisUtil.remove(idempotentKey);
            throw e;
        }
    }
}
