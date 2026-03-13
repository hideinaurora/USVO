package org.example.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger 3.0 配置类（OpenAPI）
 *
 * @author ckd
 * @since 2026-03-13
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("报名管理系统API")
                        .description("管理端用户登录接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ckd")));
    }
}
