package org.example.utils;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云 OSS 配置属性类
 * 从 application.yml/properties 中的 aliyun.oss 前缀下读取配置
 *
 * @author ckd
 * @since 2026-03-13
 */
@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class AliyunOSSProperties {
    /** OSS 服务域名 */
    private String endpoint ;
    /** 存储桶名称 */
    private String bucketName ;
    /** 存储桶所属地域 */
    private String region ;
}
