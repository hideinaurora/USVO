package org.example.service.face.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.example.config.FaceServiceConfig;
import org.example.service.face.FaceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 人脸识别服务实现类
 */
@Slf4j
@Service
public class FaceServiceImpl implements FaceService {

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private FaceServiceConfig faceServiceConfig;

    private static final double SIMILARITY_THRESHOLD = 0.5;

    @Override
    public List<Double> extractFeature(String imageBase64) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("image", imageBase64);

            String response = restTemplate.postForObject(
                    faceServiceConfig.getRegisterUrl(),
                    requestBody,
                    String.class
            );

            Map<String, Object> responseMap = JSON.parseObject(response);

            if (responseMap == null) {
                throw new RuntimeException("调用人脸服务失败：响应为空");
            }

            Integer code = ((Number) responseMap.get("code")).intValue();
            if (code == null || code != 200) {
                String message = (String) responseMap.get("message");
                throw new RuntimeException(message != null ? message : "提取人脸特征失败");
            }

            Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
            if (data == null || !Boolean.TRUE.equals(data.get("has_face"))) {
                throw new RuntimeException("未检测到人脸，请上传包含人脸的图片");
            }

            // 解析特征向量
            String featureJson = JSON.toJSONString(data.get("feature"));
            return JSON.parseObject(featureJson, new TypeReference<List<Double>>() {});

        } catch (Exception e) {
            log.error("提取人脸特征失败", e);
            if (e.getMessage() != null && e.getMessage().contains("未检测到人脸")) {
                throw e;
            }
            throw new RuntimeException("人脸识别服务异常：" + (e.getMessage() != null ? e.getMessage() : "未知错误"));
        }
    }

    @Override
    public double verifyFace(String imageBase64, List<Double> storedFeature) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("image", imageBase64);
            requestBody.put("stored_feature", storedFeature);

            String response = restTemplate.postForObject(
                    faceServiceConfig.getVerifyUrl(),
                    requestBody,
                    String.class
            );

            Map<String, Object> responseMap = JSON.parseObject(response);

            if (responseMap == null) {
                throw new RuntimeException("调用人脸服务失败：响应为空");
            }

            Integer code = ((Number) responseMap.get("code")).intValue();
            if (code == null || code != 200) {
                String message = (String) responseMap.get("message");
                throw new RuntimeException(message != null ? message : "人脸验证失败");
            }

            Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
            if (data == null) {
                throw new RuntimeException("人脸验证返回数据异常");
            }

            Double similarity = ((Number) data.get("similarity")).doubleValue();
            return similarity;

        } catch (Exception e) {
            log.error("人脸验证失败", e);
            if (e.getMessage() != null && e.getMessage().contains("未检测到人脸")) {
                throw e;
            }
            throw new RuntimeException("人脸识别服务异常：" + (e.getMessage() != null ? e.getMessage() : "未知错误"));
        }
    }

    @Override
    public boolean isServiceAvailable() {
        try {
            String response = restTemplate.getForObject(
                    faceServiceConfig.getHealthUrl(),
                    String.class
            );
            return response != null && response.contains("ok");
        } catch (Exception e) {
            log.warn("人脸服务健康检查失败: {}", e.getMessage());
            return false;
        }
    }
}
