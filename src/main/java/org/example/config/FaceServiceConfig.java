package org.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 人脸识别服务配置
 */
@Configuration
@ConfigurationProperties(prefix = "face-service")
public class FaceServiceConfig {

    /**
     * Python 人脸服务地址
     */
    private String host = "http://localhost:5000";

    /**
     * 注册人脸接口路径
     */
    private String registerPath = "/face/register";

    /**
     * 验证人脸接口路径
     */
    private String verifyPath = "/face/verify";

    /**
     * 健康检查接口路径
     */
    private String healthPath = "/health";

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getRegisterUrl() {
        return host + registerPath;
    }

    public String getVerifyUrl() {
        return host + verifyPath;
    }

    public String getHealthUrl() {
        return host + healthPath;
    }
}
