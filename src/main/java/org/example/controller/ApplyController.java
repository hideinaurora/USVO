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
import org.example.dto.activity.ApplyQueryDTO;
import org.example.dto.activity.ApplySaveDTO;
import org.example.dto.activity.ApplyUserQueryDTO;
import org.example.service.ApplyLockService;
import org.example.service.web.WebApplyService;
import org.example.vo.activity.ApplyDetailVO;
import org.example.vo.activity.ApplyUserDetailVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 活动管理控制器
 *
 * @author ckd
 * @since 2026-03-13
 */
@Slf4j
@RestController
@RequestMapping("/api/apply")
@Tag(name = "活动管理", description = "活动报名管理相关接口")
public class ApplyController {

    @Resource
    private WebApplyService applyService;

    @Resource
    private ApplyLockService applyLockService;

    @Operation(summary = "分页查询活动列表", description = "分页查询活动，包含报名人数、支付金额、退款人数等统计信息",
            parameters = {
                @Parameter(name = "token", description = "JWT访问令牌", required = true, in = ParameterIn.HEADER)
            })
    @PostMapping("/page")
    @RequiresPermissions(value = "apply:page", apiAuth = {ApiAuth.ADMIN})
    public ApiResponse<PageResult<ApplyDetailVO>> queryPage(@Valid @RequestBody ApplyQueryDTO queryDTO) {
        try {
            PageResult<ApplyDetailVO> result = applyService.queryPage(queryDTO);
            return ApiResponse.success(result);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("分页查询活动失败", e);
            throw new CommonJsonException("分页查询活动失败");
        }
    }

    @Operation(summary = "查询活动列表（不分页）", description = "查询所有活动，包含统计信息",
            parameters = {
                @Parameter(name = "token", description = "JWT访问令牌", required = true, in = ParameterIn.HEADER)
            })
    @GetMapping("/list")
    @RequiresPermissions(value = "apply:list", apiAuth = {ApiAuth.ADMIN})
    public ApiResponse<List<ApplyDetailVO>> queryList() {
        try {
            List<ApplyDetailVO> list = applyService.queryList();
            return ApiResponse.success(list);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询活动列表失败", e);
            throw new CommonJsonException("查询活动列表失败");
        }
    }

    @Operation(summary = "查询活动详情", description = "根据活动ID查询详情，包含统计信息",
            parameters = {
                @Parameter(name = "token", description = "JWT访问令牌", required = true, in = ParameterIn.HEADER)
            })
    @GetMapping("/{applyId}")
    @RequiresPermissions(value = "apply:detail", apiAuth = {ApiAuth.ADMIN, ApiAuth.USER})
    public ApiResponse<ApplyDetailVO> queryDetail(
            @Parameter(description = "活动ID", required = true) @PathVariable Long applyId) {
        try {
            ApplyDetailVO detail = applyService.queryDetail(applyId);
            if (detail == null) {
                return ApiResponse.error("活动不存在");
            }
            return ApiResponse.success(detail);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询活动详情失败，applyId={}", applyId, e);
            throw new CommonJsonException("查询活动详情失败");
        }
    }

    @Operation(summary = "分页查询活动报名学生列表", description = "根据活动ID查询报名学生列表，包含用户姓名、支付状态等信息",
            parameters = {
                @Parameter(name = "token", description = "JWT访问令牌", required = true, in = ParameterIn.HEADER)
            })
    @PostMapping("/users/page")
    @RequiresPermissions(value = "apply:users:page", apiAuth = {ApiAuth.ADMIN})
    public ApiResponse<PageResult<ApplyUserDetailVO>> queryApplyUserPage(@Valid @RequestBody ApplyUserQueryDTO queryDTO) {
        try {
            PageResult<ApplyUserDetailVO> result = applyService.queryApplyUserPage(queryDTO);
            return ApiResponse.success(result);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("分页查询活动报名学生失败", e);
            throw new CommonJsonException("分页查询活动报名学生失败");
        }
    }

    @Operation(summary = "查询活动报名学生列表（不分页）", description = "根据活动ID查询所有报名学生",
            parameters = {
                @Parameter(name = "token", description = "JWT访问令牌", required = true, in = ParameterIn.HEADER)
            })
    @GetMapping("/users/{applyId}")
    @RequiresPermissions(value = "apply:users:list", apiAuth = {ApiAuth.ADMIN})
    public ApiResponse<List<ApplyUserDetailVO>> queryApplyUserList(
            @Parameter(description = "活动ID", required = true) @PathVariable Long applyId) {
        try {
            List<ApplyUserDetailVO> list = applyService.queryApplyUserList(applyId);
            return ApiResponse.success(list);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询活动报名学生列表失败，applyId={}", applyId, e);
            throw new CommonJsonException("查询活动报名学生列表失败");
        }
    }

    @Operation(summary = "新增活动", description = "创建新的活动报名",
            parameters = {
                @Parameter(name = "token", description = "JWT访问令牌", required = true, in = ParameterIn.HEADER)
            })
    @PostMapping("/add")
    @RequiresPermissions(value = "apply:add", apiAuth = {ApiAuth.ADMIN})
    public ApiResponse<Long> add(@Valid @RequestBody ApplySaveDTO saveDTO) {
        try {
            Long applyId = applyService.add(saveDTO);
            return ApiResponse.success("新增成功", applyId);
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("新增活动失败", e);
            throw new CommonJsonException("新增活动失败");
        }
    }

    @Operation(summary = "修改活动", description = "修改活动信息",
            parameters = {
                @Parameter(name = "token", description = "JWT访问令牌", required = true, in = ParameterIn.HEADER)
            })
    @PutMapping("/update")
    @RequiresPermissions(value = "apply:update", apiAuth = {ApiAuth.ADMIN})
    public ApiResponse<Void> update(@Valid @RequestBody ApplySaveDTO saveDTO) {
        try {
            applyService.update(saveDTO);
            return ApiResponse.success();
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("修改活动失败", e);
            throw new CommonJsonException("修改活动失败");
        }
    }

    @Operation(summary = "删除活动", description = "根据活动ID删除（支持批量删除，ID用逗号分隔）。如果活动已存在报名记录，则不允许删除",
            parameters = {
                @Parameter(name = "token", description = "JWT访问令牌", required = true, in = ParameterIn.HEADER)
            })
    @DeleteMapping("/{applyIds}")
    @RequiresPermissions(value = "apply:delete", apiAuth = {ApiAuth.ADMIN})
    public ApiResponse<Void> delete(
            @Parameter(description = "活动ID（多个用逗号分隔）", required = true, example = "1,2,3")
            @PathVariable String applyIds) {
        try {
            applyService.delete(applyIds);
            return ApiResponse.success();
        } catch (CommonJsonException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除活动失败，applyIds={}", applyIds, e);
            throw new CommonJsonException("删除活动失败");
        }
    }

    @Operation(summary = "增加活动名额", description = "为指定活动增加名额限制，使用分布式锁保证并发安全",
            parameters = {
                    @Parameter(name = "token", description = "JWT访问令牌", required = true, in = ParameterIn.HEADER)
            })
    @PutMapping("/quota/increase")
    @RequiresPermissions(value = "apply:quota:update", apiAuth = {ApiAuth.ADMIN})
    public ApiResponse<Void> increaseQuota(
            @Parameter(description = "活动ID", required = true) @RequestParam Long applyId,
            @Parameter(description = "增加的名额数量", required = true) @RequestParam Long quantity) {
        try {
            if (quantity <= 0) {
                return ApiResponse.error("增加数量必须大于0");
            }
            // 校验活动是否存在
            ApplyDetailVO applyDetail = applyService.queryDetail(applyId);
            if (applyDetail == null) {
                return ApiResponse.error("活动不存在");
            }
            boolean result = applyLockService.increaseApply(applyId, quantity);
            if (result) {
                return ApiResponse.success();
            } else {
                return ApiResponse.error("增加名额失败，请稍后重试");
            }
        } catch (Exception e) {
            log.error("增加活动名额失败，applyId={}, quantity={}", applyId, quantity, e);
            throw new CommonJsonException("增加活动名额失败");
        }
    }

    @Operation(summary = "减少活动名额", description = "为指定活动减少名额限制，使用分布式锁保证并发安全",
            parameters = {
                    @Parameter(name = "token", description = "JWT访问令牌", required = true, in = ParameterIn.HEADER)
            })
    @PutMapping("/quota/decrease")
    @RequiresPermissions(value = "apply:quota:update", apiAuth = {ApiAuth.ADMIN})
    public ApiResponse<Void> decreaseQuota(
            @Parameter(description = "活动ID", required = true) @RequestParam Long applyId,
            @Parameter(description = "减少的名额数量", required = true) @RequestParam Long quantity) {
        try {
            if (quantity <= 0) {
                return ApiResponse.error("减少数量必须大于0");
            }
            // 校验活动是否存在
            ApplyDetailVO applyDetail = applyService.queryDetail(applyId);
            if (applyDetail == null) {
                return ApiResponse.error("活动不存在");
            }
            boolean result = applyLockService.decreaseApply(applyId, quantity);
            if (result) {
                return ApiResponse.success();
            } else {
                return ApiResponse.error("减少名额失败，名额不足或操作失败");
            }
        } catch (Exception e) {
            log.error("减少活动名额失败，applyId={}, quantity={}", applyId, quantity, e);
            throw new CommonJsonException("减少活动名额失败");
        }
    }

    @Operation(summary = "获取当前剩余名额", description = "获取指定活动的当前剩余名额数量",
            parameters = {
                    @Parameter(name = "token", description = "JWT访问令牌", required = true, in = ParameterIn.HEADER)
            })
    @GetMapping("/quota/{applyId}")
    @RequiresPermissions(value = "apply:quota:query", apiAuth = {ApiAuth.ADMIN, ApiAuth.USER})
    public ApiResponse<Long> getRemainingQuota(
            @Parameter(description = "活动ID", required = true) @PathVariable Long applyId) {
        try {
            // 校验活动是否存在
            ApplyDetailVO applyDetail = applyService.queryDetail(applyId);
            if (applyDetail == null) {
                return ApiResponse.error("活动不存在");
            }
            Long remainingQuota = applyLockService.getApply(String.valueOf(applyId));
            return ApiResponse.success(remainingQuota);
        } catch (Exception e) {
            log.error("获取活动剩余名额失败，applyId={}", applyId, e);
            throw new CommonJsonException("获取活动剩余名额失败");
        }
    }
}