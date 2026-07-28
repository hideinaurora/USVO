package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.aop.annotation.ApiAuth;
import org.example.aop.annotation.RequiresPermissions;
import org.example.common.ApiResponse;
import org.example.entity.basic.user.UserEntity;
import org.example.mapper.basic.user.UserMapper;
import org.example.service.face.FaceService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 人脸识别控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/face")
@Tag(name = "人脸识别模块", description = "人脸特征提取、人脸验证")
public class FaceController {

    @Resource
    private FaceService faceService;

    @Resource
    private UserMapper userMapper;

    @Operation(summary = "提取人脸特征", description = "上传图片，提取人脸特征向量（用于注册人脸）",
            parameters = {
                    @Parameter(name = "Authorization", description = "Bearer Token", required = true, in = ParameterIn.HEADER)
            })
    @PostMapping("/extract")
    @RequiresPermissions(value = "face:extract", apiAuth = {ApiAuth.USER})
    public ApiResponse<List<Double>> extractFeature(@RequestBody Map<String, String> request) {
        try {
            String imageBase64 = request.get("image");
            if (imageBase64 == null || imageBase64.isEmpty()) {
                return ApiResponse.error(400, "图片不能为空");
            }

            List<Double> feature = faceService.extractFeature(imageBase64);
            return ApiResponse.success(feature);
        } catch (Exception e) {
            log.error("提取人脸特征失败", e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @Operation(summary = "验证人脸", description = "比对用户现场拍摄的照片与已注册的人脸特征",
            parameters = {
                    @Parameter(name = "Authorization", description = "Bearer Token", required = true, in = ParameterIn.HEADER)
            })
    @PostMapping("/verify")
    @RequiresPermissions(value = "face:verify", apiAuth = {ApiAuth.USER})
    public ApiResponse<Boolean> verifyFace(@RequestBody Map<String, Object> request) {
        try {
            String imageBase64 = (String) request.get("image");
            if (imageBase64 == null || imageBase64.isEmpty()) {
                return ApiResponse.error(400, "图片不能为空");
            }

            Long userId = (Long) request.get("user_id");
            if (userId == null) {
                return ApiResponse.error(400, "用户ID不能为空");
            }

            // 查询用户的人脸特征
            UserEntity user = userMapper.selectById(userId);
            if (user == null) {
                return ApiResponse.error(404, "用户不存在");
            }

            String faceFeatureStr = user.getFaceFeature();
            if (faceFeatureStr == null || faceFeatureStr.isEmpty()) {
                return ApiResponse.error(400, "用户未注册人脸，请先上传头像");
            }

            // 解析特征向量
            List<Double> storedFeature = com.alibaba.fastjson2.JSON.parseObject(
                    faceFeatureStr,
                    new com.alibaba.fastjson2.TypeReference<List<Double>>() {}
            );

            // 调用人脸服务验证
            double similarity = faceService.verifyFace(imageBase64, storedFeature);
            boolean match = similarity >= 0.5;

            log.info("人脸验证结果: userId={}, similarity={}, match={}", userId, similarity, match);

            return ApiResponse.success(match);
        } catch (Exception e) {
            log.error("人脸验证失败", e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @Operation(summary = "检查人脸服务状态")
    @GetMapping("/health")
    public ApiResponse<Boolean> health() {
        boolean available = faceService.isServiceAvailable();
        return ApiResponse.success(available);
    }
}
