package org.example.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "我的违约记录返回结果")
public class ViolationListVO {
    private Long totalCount;
    private List<ViolationItemVO> list;
}

