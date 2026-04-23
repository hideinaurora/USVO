package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.common.ApiResponse;
import org.example.config.exception.CommonJsonException;
import org.example.utils.AliyunOSSOperator;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/api")
@Tag(name = "文件上传", description = "阿里云OSS文件上传接口")
public class UploadController {

    @Resource
    private AliyunOSSOperator aliyunOSSOperator;

    @Operation(summary = "文件上传", description = "上传文件到阿里云OSS，返回文件访问URL")
    @PostMapping("/upload")
    public ApiResponse<String> upload(MultipartFile file) {
        try {
            log.info("文件开始上传！");
            String url = aliyunOSSOperator.upload(file.getBytes(), file.getOriginalFilename());
            log.info("文件上传到OSS，url：{}", url);
            return ApiResponse.success(url);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new CommonJsonException("文件上传失败，请稍后重试");
        }
    }
}