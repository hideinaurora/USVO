package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.dify.DifyQueryRequestDTO;
import org.example.dto.dify.DifyQueryResponseDTO;
import org.example.service.dify.DifyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * Dify 智能体数据查询接口
 */
//@Slf4j
//@RestController
//@RequestMapping("/api/Dify")
//@Tag(name = "Dify智能体查询接口", description = "供Dify智能体调用查询场馆预约系统数据")
//public class DifyController {
//
//    @Resource
//    private DifyService difyService;
//
//    @Value("${dify.api-key:}")
//    private String configuredApiKey;
//
//    @Operation(summary = "场馆预约综合查询", description = "供Dify智能体调用，从业务数据库中查询场馆信息、场地价格、空闲时段、预约记录、违约记录等数据")
//    @PostMapping("/query")
//    public DifyQueryResponseDTO query(@RequestHeader(value = "Authorization", required = false) String authorization,
//                                       @Valid @RequestBody DifyQueryRequestDTO request) {
//        // API Key 鉴权
//        if (configuredApiKey != null && !configuredApiKey.isEmpty()) {
//            if (authorization == null || !authorization.startsWith("Bearer ")) {
//                log.warn("Dify查询请求缺少Authorization头");
//                return DifyQueryResponseDTO.error(401, "API Key无效或已过期");
//            }
//            String providedKey = authorization.substring(7);
//            if (!configuredApiKey.equals(providedKey)) {
//                log.warn("Dify查询请求API Key不匹配");
//                return DifyQueryResponseDTO.error(401, "API Key无效或已过期");
//            }
//        }
//
//        // 参数校验
//        if (request.getQueryType() == null || request.getQueryType().isEmpty()) {
//            return DifyQueryResponseDTO.paramError("缺少必填字段 'query_type'");
//        }
//
//        try {
//            log.info("Dify查询 - queryType: {}, userId: {}", request.getQueryType(), request.getUserId());
//            Object result = difyService.query(request);
//            return DifyQueryResponseDTO.success(result);
//        } catch (Exception e) {
//            log.error("Dify查询失败", e);
//            return DifyQueryResponseDTO.serverError(e.getMessage());
//        }
//    }
//}
