package org.example.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 分页结果
 *
 * @param <T> 数据类型
 * @author ckd
 * @since 2026-03-13
 */
@Getter
@Setter
@Schema(description = "分页结果")
public class PageResult<T> {

    @Schema(description = "当前页码")
    private Integer pageNum;

    @Schema(description = "每页数量")
    private Integer pageSize;

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "总页数")
    private Integer totalPages;

    @Schema(description = "数据列表")
    private List<T> records;

    public PageResult() {
    }

    public PageResult(Integer pageNum, Integer pageSize, Long total, List<T> records) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.records = records;
        this.totalPages = (int) Math.ceil((double) total / pageSize);
    }

    public static <T> PageResult<T> of(Integer pageNum, Integer pageSize, Long total, List<T> records) {
        return new PageResult<>(pageNum, pageSize, total, records);
    }
}
