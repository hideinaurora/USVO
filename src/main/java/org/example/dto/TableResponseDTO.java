package org.example.dto;

import lombok.Data;

@Data
public class TableResponseDTO {
    private Object list;
    private PaginationDTO pagination;

    public TableResponseDTO() {
    }


    public TableResponseDTO(Object list, PaginationDTO pagination) {
        this.list = list;
        this.pagination = pagination;
    }
}
