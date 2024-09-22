package org.example.dto;

import com.alibaba.fastjson2.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TableRequestDTO {
    private Integer current;
    private Integer pageSize;
    private Integer total;
    private Integer currentPage;
    private String search;
    private String sorter;
    private String sort;

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public TableRequestDTO() {
        this.current = 1;
        this.pageSize = 20;
        this.currentPage = 1;
    }

    public TableRequestDTO(Integer current, Integer pageSize, Integer total, Integer currentPage, String search,
                           String sorter) {
        this.current = current;
        this.pageSize = pageSize;
        this.total = total;
        this.currentPage = currentPage;
        this.search = search;
        this.sorter = sorter;
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

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
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

    public JSONObject getJsonSearch() {
        if (this.search == null || this.search.equals("") || this.search.equals("null") || this.search.equals("{}")) {
            return new JSONObject();
        }
        return JSONObject.parseObject(this.search);
    }

    public JSONObject getAlJsonSearch() {
        if (this.search == null || this.search.equals("") || this.search.equals("null") || this.search.equals("{}")) {
            return new JSONObject();
        }
        return JSONObject.parseObject(this.search);
    }

    public JSONObject getJsonSorter() {
        if (this.sorter == null || this.sorter.equals("") || this.sorter.equals("null") || this.sorter.equals("{}")) {
            return new JSONObject();
        }
        String[] s = this.sorter.split("_");
        if (s[1].equals("descend")) {
            s[1] = "desc";
        } else {
            s[1] = "asc";
        }
        return JSONObject.parseObject("{\"sorterField\": \"" + CamelCaseToUnderline(s[0]) + "\", \"sorterOrder\": \""
                + s[1] + "\"}");
    }

    public String CamelCaseToUnderline(String para) {
        StringBuilder sb = new StringBuilder(para);
        int temp = 0;
        if (!para.contains("_")) {
            for (int i = 0; i < para.length(); i++) {
                if (Character.isUpperCase(para.charAt(i))) {
                    sb.insert(i + temp, "_");
                    temp += 1;
                }
            }
        }
        return sb.toString().toLowerCase();
    }

    public Integer getStart(Long count) {
        Integer tempPage = getCurrentPage();
        if ((tempPage - 1) * this.pageSize == count && tempPage != 1) {
            tempPage--;
            this.current = tempPage;
            this.currentPage = tempPage;
        }
        return (tempPage - 1) * this.pageSize;
    }

    public Integer getStartOther() {
        Integer tempPage = getCurrentPage();
        if ((tempPage - 1) <= 0) {
            tempPage = 1;
        }
        return (tempPage - 1) * this.pageSize;
    }

    /**
     * @param data 待分页数据
     * @return 手动实现逻辑分页
     * @author ckd
     * @date 2023/2/2 9:51
     */
    public <T> List<T> getPageData(List<T> data) {
        // 起始索引位
        int start = 0;
        int pageSize = this.getPageSize();
        // 结束索引位
        int toIndex = Math.min(data.size(), (this.getCurrentPage()) * pageSize);
        // 结束索引位
        if (start < data.size()) {
            // 程序进行分页
            data = data.subList(start, toIndex);
            return data;
        }
        return new ArrayList<>();
    }

    @Override
    public String toString() {
        return "TableRequestDTO{" +
                "current=" + current +
                ", pageSize=" + pageSize +
                ", total=" + total +
                ", currentPage=" + currentPage +
                ", search='" + search + '\'' +
                ", sorter='" + sorter + '\'' +
                '}';
    }
}
