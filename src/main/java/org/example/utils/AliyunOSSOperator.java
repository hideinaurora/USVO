package org.example.utils;

import com.aliyun.oss.*;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.common.comm.SignVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 阿里云 OSS 文件上传工具类
 * 封装了 OSS 客户端的初始化、文件命名规则生成及对象上传逻辑
 *
 * @author ckd
 * @since 2026-03-13
 */
@Component
public class AliyunOSSOperator {

    @Autowired
    private AliyunOSSProperties aliyunOSSProperties;

    /**
     * 上传字节数组内容到 OSS
     *
     * @param content          文件字节数组
     * @param originalFilename 原始文件名（用于提取后缀）
     * @return 上传后的文件访问 URL
     * @throws Exception 上传过程中可能抛出的异常（如网络问题、凭证失效等）
     */
    public String upload(byte[] content, String originalFilename) throws Exception {

        String endpoint = aliyunOSSProperties.getEndpoint();
        String bucketName = aliyunOSSProperties.getBucketName();
        String region = aliyunOSSProperties.getRegion();


        // 1. 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();

        // 2. 填写Object完整路径，例如202406/1.png。Object完整路径中不能包含Bucket名称。
        // 获取当前系统日期的字符串,格式为 yyyy/MM
        String dir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        // 生成一个新的不重复的文件名，避免文件名冲突
        String newFileName = UUID.randomUUID() + originalFilename.substring(originalFilename.lastIndexOf("."));
        String objectName = dir + "/" + newFileName;

        // 创建OSSClient实例。
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();

        try {
            // 4. 执行文件流上传
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(content));
        } finally {
            // 5. 确保资源释放
            ossClient.shutdown();
        }

        // 6. 拼接并返回文件在 OSS 上的完整访问路径
        return endpoint.split("//")[0] + "//" + bucketName + "." + endpoint.split("//")[1] + "/" + objectName;
    }

}
