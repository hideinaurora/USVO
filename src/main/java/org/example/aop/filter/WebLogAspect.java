package org.example.aop.filter;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 打印每个请求的入参、出参等信息
 */
@Aspect
@Component
@Slf4j
@Order(1)
public class WebLogAspect {
    @Pointcut("execution(public * org.example.controller..*.*(..))")
    public void webLog() {
    }

    /**
     * 只在进入controller时记录请求信息
     */
    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        log.debug("请求路径 {} ,进入方法 {}", request.getRequestURI(), joinPoint.getSignature().getDeclaringTypeName() + ":" + joinPoint.getSignature().getName());
    }

    /**
     * 打印请求日志
     */
    @AfterReturning(pointcut = "webLog()", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) throws Exception {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
    }
}
