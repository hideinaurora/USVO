package org.example.vo.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "场馆列表项")
public class VenueListItemVO {

    private Long id;

    private String name;

    private String type;

    private String address;

    private String openTime;

    private String closeTime;

    private Integer status;

    private Integer courtCount;
}