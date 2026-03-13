package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.aop.annotation.ApiAuth;
import org.example.aop.annotation.RequiresPermissions;
import org.example.common.ApiResponse;
import org.example.common.PageResult;
import org.example.config.exception.CommonJsonException;
import org.example.dto.activity.RefundExamineDTO;
import org.example.dto.activity.RefundQueryDTO;
import org.example.service.activity.refund.RefundService;
import org.example.service.web.WebRefundService;
import org.example.vo.activity.RefundDetailVO;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import javax.annotation.Resource;
import java.util.List;

/**
 * 退款管理控制器
 *
 * @author ckd
 * @since 2026-03-13
 */
@Slf4j
@RestController
@RequestMapping("/api/refund")
@Tag(name = "退款管理", description = "退款申请管理相关接口")
public class RefundController {

    @Resource
    private WebRefundService refundService;

    @Operation(summary = "分页查询退款学生列表", description = "分页查询退款申请记录，包含用户姓名、活动标题、审核记录等信息",
            parameters = {
                    @Parameter(name = "token", description = "JWT访问令牌", required = true, in = ParameterIn.HEADER)
            })
    @PostMapping("/page")
    @RequiresPermissions(value = "refund:page", apiAuth = {ApiAuth.ADMIN})
    public ApiResponse<PageResult<RefundDetailVO>> queryPage(@Valid @RequestBody RefundQueryDTO queryDTO) {
        try {
            PageResult<RefundDetailVO> result = refundService.queryPage(queryDTO);
            return ApiResponse.success(result);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("分页查询退款学生失败", e);
            throw new CommonJsonException("分页查询退款学生失败");
        }
    }

    @Operation(summary = "查询退款学生列表（不分页）", description = "查询所有退款申请记录",
            parameters = {
                    @Parameter(name = "token", description = "JWT访问令牌", required = true, in = ParameterIn.HEADER)
            })
    @GetMapping("/list")
    @RequiresPermissions(value = "refund:list", apiAuth = {ApiAuth.ADMIN})
    public ApiResponse<List<RefundDetailVO>> queryList() {
        try {
            List<RefundDetailVO> list = refundService.queryList();
            return ApiResponse.success(list);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询退款学生列表失败", e);
            throw new CommonJsonException("查询退款学生列表失败");
        }
    }

    @Operation(summary = "审核退款申请", description = "管理员审核退款申请，通过或拒绝",
            parameters = {
                    @Parameter(name = "token", description = "JWT访问令牌", required = true, in = ParameterIn.HEADER)
            })
    @PostMapping("/examine")
    @RequiresPermissions(value = "refund:examine", apiAuth = {ApiAuth.ADMIN})
    public ApiResponse<Boolean> examine(@Valid @RequestBody RefundExamineDTO examineDTO) {
        try {
            return ApiResponse.success(refundService.examine(examineDTO));
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("审核退款申请失败", e);
            throw new CommonJsonException("审核退款申请失败");
        }
    }
}
