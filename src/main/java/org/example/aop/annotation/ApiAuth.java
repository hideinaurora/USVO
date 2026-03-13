package org.example.aop.annotation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ApiAuth {
    /**
     * 管理员：1
     * 普通用户：2
     */
    ADMIN(1),
    USER(2);
    private final Integer type;
}

