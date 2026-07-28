package org.example.service.face;

import java.util.List;

/**
 * 人脸识别服务接口
 */
public interface FaceService {

    /**
     * 提取人脸特征向量
     *
     * @param imageBase64 Base64编码的图片（可带data:image前缀）
     * @return 512维特征向量列表
     * @throws RuntimeException 当未检测到人脸或服务异常时
     */
    List<Double> extractFeature(String imageBase64);

    /**
     * 验证人脸
     *
     * @param imageBase64 Base64编码的待验证图片
     * @param storedFeature 已存储的特征向量
     * @return 相似度分数
     * @throws RuntimeException 当未检测到人脸或服务异常时
     */
    double verifyFace(String imageBase64, List<Double> storedFeature);

    /**
     * 检查人脸服务是否可用
     *
     * @return true-可用，false-不可用
     */
    boolean isServiceAvailable();
}
