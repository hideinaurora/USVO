package org.example.aop.annotation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ApiAuth {
    /**
     * 管理端1市级2学校4区县5机构6乡镇7银联
     */
    ADMIN(1),
    USER(2);
    private final Integer type;
}

