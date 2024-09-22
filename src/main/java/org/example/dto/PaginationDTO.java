package org.example.dto;

public class PaginationDTO {
    private Integer current;
    private Integer pageSize;
    private Long total;
    private Integer currentPage;
    private String search;
    private String sorter;

    public PaginationDTO() {
    }

    public PaginationDTO(Long total) {
        this.total = total;
    }

    public PaginationDTO(Integer current, Integer pageSize, Long total, Integer currentPage, String search,
                         String sorter) {
        if (currentPage == null && current != null) {
            currentPage = current;
        }
        this.current = currentPage == null ? 1 : currentPage;
        this.pageSize = pageSize;
        this.total = total;
        this.currentPage = currentPage;
        this.search = search;
        this.sorter = sorter;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getCurrent() {
        return current;
    }

    public void setCurrent(Integer current) {
        this.current = current;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getSorter() {
        return sorter;
    }

    public void setSorter(String sorter) {
        this.sorter = sorter;
    }
}
