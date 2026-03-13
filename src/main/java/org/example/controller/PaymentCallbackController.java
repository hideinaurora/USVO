package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.PaymentCallbackDTO;
import org.example.service.PaymentCallbackService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 第三方支付回调控制器
 * 接收支付宝/微信等第三方支付平台的异步通知回调
 *
 * @author ckd
 * @since 2026-03-13
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
@Tag(name = "支付回调", description = "第三方支付平台回调接口")
public class PaymentCallbackController {

    @Resource
    private PaymentCallbackService paymentCallbackService;

    @Operation(summary = "支付异步通知回调", description =
            "接收第三方支付平台的支付成功异步通知。" +
            "支持支付宝、微信等主流支付平台的回调格式。 " +
            "响应格式：纯文本 " +
            "- 成功：success " +
            "- 失败：fail 或具体错误信息")
    @PostMapping(value = "/callback/notify", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String paymentNotify(@ModelAttribute PaymentCallbackDTO callbackDTO) {
        try {
            log.info("收到支付回调通知：商户订单号={}, 交易状态={}",
                    callbackDTO.getOutTradeNo(), callbackDTO.getTradeStatus());

            String result = paymentCallbackService.handlePaymentCallback(callbackDTO);

            log.info("支付回调处理完成：商户订单号={}, 处理结果={}",
                    callbackDTO.getOutTradeNo(), result);

            return result;

        } catch (Exception e) {
            log.error("支付回调处理异常：商户订单号={}", callbackDTO.getOutTradeNo(), e);
            return "fail";
        }
    }

}
