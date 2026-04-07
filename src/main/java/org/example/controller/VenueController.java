package org.example.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.common.ApiResponse;
import org.example.common.PageResult;
import org.example.config.exception.CommonJsonException;
import org.example.service.venue.VenueService;
import org.example.vo.venue.VenueDetailVO;
import org.example.vo.venue.VenueListVO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/venue")
@Tag(name = "场馆模块", description = "场馆/场地相关接口")
public class VenueController {

    @Resource
    private VenueService venueService;

    @Operation(summary = "获取场馆列表", description = "支持按类型、关键词筛选；传入经纬度时按距离排序并返回distance（公里）")
    @GetMapping("/list")
    public ApiResponse<List<VenueListVO>> list(
            @Parameter(description = "场馆类型（basketball/badminton/swimming）") @RequestParam(required = false) String type,
            @Parameter(description = "搜索关键词（名称/地址）") @RequestParam(required = false) String keyword,
            @Parameter(description = "纬度（距离排序用）") @RequestParam(required = false) Double latitude,
            @Parameter(description = "经度（距离排序用）") @RequestParam(required = false) Double longitude
    ) {
        try {
            return ApiResponse.success(venueService.listVenues(type, keyword, latitude, longitude));
        } catch (Exception e) {
            log.error("获取场馆列表失败", e);
            throw new CommonJsonException("获取场馆列表失败");
        }
    }

    @Operation(summary = "分页获取场馆列表", description = "分页查询场馆列表，支持按类型、关键词筛选；传入经纬度时按距离排序并返回distance（公里）")
    @GetMapping("/page")
    public ApiResponse<PageResult<VenueListVO>> page(
            @Parameter(description = "页码，默认1") @RequestParam(required = false) Integer pageNum,
            @Parameter(description = "每页条数，默认10") @RequestParam(required = false) Integer pageSize,
            @Parameter(description = "场馆类型（basketball/badminton/swimming）") @RequestParam(required = false) String type,
            @Parameter(description = "搜索关键词（名称/地址）") @RequestParam(required = false) String keyword,
            @Parameter(description = "纬度（距离排序用）") @RequestParam(required = false) Double latitude,
            @Parameter(description = "经度（距离排序用）") @RequestParam(required = false) Double longitude
    ) {
        try {
            return ApiResponse.success(venueService.pageVenues(pageNum, pageSize, type, keyword, latitude, longitude));
        } catch (Exception e) {
            log.error("分页获取场馆列表失败", e);
            throw new CommonJsonException("分页获取场馆列表失败");
        }
    }

    @Operation(summary = "获取场馆详情", description = "根据场馆ID获取详情，包含场地列表")
    @GetMapping("/{venueId}")
    public ApiResponse<VenueDetailVO> detail(@PathVariable Long venueId) {
        try {
            VenueDetailVO vo = venueService.getVenueDetail(venueId);
            if (vo == null) {
                return ApiResponse.error(404, "场馆不存在");
            }
            return ApiResponse.success(vo);
        } catch (Exception e) {
            log.error("获取场馆详情失败，venueId={}", venueId, e);
            throw new CommonJsonException("获取场馆详情失败");
        }
    }
}
