package org.example.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuestionnaireQueueDTO {
    // 队列id
    private Long queueId;
    // 问卷表id
    private Long questionnaireId;
    // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime gmtCreate;
    // 修改时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime gmtModify;
    // 是否删除
    private Integer isDeleted;
    // 创建人
    private Long createBy;
    // 下载链接
    private String queueUrl;
    // 状态0进行中1成功2失败
    private Integer queueType;
    // 下载类型1问卷2结算3收支统计
    private Integer queueLx;
    // 角色id
    private Long roleId;

    public QuestionnaireQueueDTO() {
    }

    public QuestionnaireQueueDTO(Long questionnaireId, Long createBy, Integer queueType, Integer queueLx, Long roleId) {
        this.questionnaireId = questionnaireId;
        this.createBy = createBy;
        this.queueType = queueType;
        this.queueLx = queueLx;
        this.roleId = roleId;
    }
}
