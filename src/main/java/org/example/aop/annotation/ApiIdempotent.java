package org.example.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API接口幂等性注解
 * 用于防止重复提交，确保同一个请求在一定时间内只能被处理一次
 *
 * @author ckd
 * @since 2026-03-13
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiIdempotent {
    /**
     * 幂等性过期时间（秒），默认5秒
     */
    int expireSeconds() default 5;
}
