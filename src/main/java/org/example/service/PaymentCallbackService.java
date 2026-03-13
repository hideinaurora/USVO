package org.example.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.config.exception.CommonJsonException;
import org.example.dto.PaymentCallbackDTO;
import org.example.entity.activity.apply.ApplyPayEntity;
import org.example.entity.activity.apply.ApplyUserEntity;
import org.example.service.activity.apply.ApplyPayService;
import org.example.service.activity.apply.ApplyUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 支付回调服务
 * 处理第三方支付平台的支付成功回调
 *
 * @author ckd
 * @since 2026-03-13
 */
@Slf4j
@Service
public class PaymentCallbackService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private ApplyPayService applyPayService;

    @Resource
    private ApplyUserService applyUserService;

    /**
     * 处理支付回调
     *
     * @param callbackDTO 支付回调参数
     * @return 处理结果消息
     */
    @Transactional(rollbackFor = Exception.class)
    public String handlePaymentCallback(PaymentCallbackDTO callbackDTO) {
        // 1. 验证回调参数
        if (callbackDTO.getOutTradeNo() == null || callbackDTO.getOutTradeNo().isEmpty()) {
            log.error("支付回调失败：商户订单号为空");
            return "fail";
        }

        if (!"TRADE_SUCCESS".equals(callbackDTO.getTradeStatus()) &&
            !"SUCCESS".equals(callbackDTO.getTradeStatus())) {
            log.warn("支付回调状态非成功状态：{}", callbackDTO.getTradeStatus());
            return "success";
        }

        // 2. 根据商户订单号查询支付订单
        QueryWrapper<ApplyPayEntity> payWrapper = new QueryWrapper<>();
        payWrapper.eq("mer_order_id", callbackDTO.getOutTradeNo());
        ApplyPayEntity payOrder = applyPayService.getOne(payWrapper);

        if (payOrder == null) {
            log.error("支付回调失败：订单不存在，商户订单号={}", callbackDTO.getOutTradeNo());
            return "fail";
        }

        // 3. 检查订单状态，避免重复处理
        if (payOrder.getPayStatus() == 1) {
            log.info("订单已处理过，忽略重复回调：orderId={}", payOrder.getId());
            return "success";
        }

        // 4. 验证金额（将元转换为分）
        Long callbackAmountFen = parseAmountToFen(callbackDTO.getTotalAmount());
        if (callbackAmountFen == null || !callbackAmountFen.equals(payOrder.getTotalAmount().longValue())) {
            log.error("支付回调失败：金额不匹配，订单金额={}分，回调金额={}元",
                    payOrder.getTotalAmount(), callbackDTO.getTotalAmount());
            return "fail";
        }

        try {
            // 5. 更新支付订单状态
            payOrder.setPayStatus(1); // 已支付

            // 设置支付时间
            if (callbackDTO.getGmtPayment() != null && !callbackDTO.getGmtPayment().isEmpty()) {
                payOrder.setPayTime(callbackDTO.getGmtPayment());
            } else {
                payOrder.setPayTime(LocalDateTime.now().format(DATE_TIME_FORMATTER));
            }

            // 记录支付平台交易号等信息
            if (callbackDTO.getTradeNo() != null) {
                // 将交易号保存到 pay_info 字段
                String payInfo = String.format("tradeNo=%s;buyerLogonId=%s;notifyTime=%s",
                        callbackDTO.getTradeNo(),
                        callbackDTO.getBuyerLogonId() != null ? callbackDTO.getBuyerLogonId() : "",
                        callbackDTO.getNotifyTime() != null ? callbackDTO.getNotifyTime() : "");
                payOrder.setPayInfo(payInfo);
            }

            applyPayService.updateById(payOrder);

            // 6. 更新用户报名状态
            QueryWrapper<ApplyUserEntity> userApplyWrapper = new QueryWrapper<>();
            userApplyWrapper.eq("user_id", payOrder.getUserId());
            userApplyWrapper.eq("apply_id", payOrder.getApplyId());
            ApplyUserEntity userApply = applyUserService.getOne(userApplyWrapper);

            if (userApply != null) {
                userApply.setIsPay(1); // 已支付
                applyUserService.updateById(userApply);
                log.info("支付回调处理成功：orderId={}, userId={}, applyId={}",
                        payOrder.getId(), payOrder.getUserId(), payOrder.getApplyId());
            } else {
                log.error("支付回调异常：找不到用户报名记录，orderId={}, userId={}, applyId={}",
                        payOrder.getId(), payOrder.getUserId(), payOrder.getApplyId());
            }

            return "success";

        } catch (Exception e) {
            log.error("支付回调处理异常：orderId={}", payOrder.getId(), e);
            throw new CommonJsonException("支付回调处理失败");
        }
    }

    /**
     * 将金额（元）转换为（分）
     *
     * @param amountYuan 金额（元），字符串格式
     * @return 金额（分）
     */
    private Long parseAmountToFen(String amountYuan) {
        try {
            double amount = Double.parseDouble(amountYuan);
            return (long) (amount * 100);
        } catch (NumberFormatException e) {
            log.error("金额解析失败：{}", amountYuan);
            return null;
        }
    }
}
