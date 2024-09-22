package org.example.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuestionnaireDTO {

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
    // 问卷名称
    private String questionnaireName;
    // 开始时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;
    // 结束时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;
    // 问卷介绍
    private String questionnaireIntroduce;
    // 问卷下发的学校ids
    private String schoolIds;
    // 学校的父节点ids
    private String schoolParentIds;
    // 问卷内容
    private String questionnaireContent;
    // 问卷内容-只有类型、题目和选项
    private String questionnaireInfo;
    // 创建人
    private Long createBy;
    // 是否发布0否1是
    private Integer isValid;
    // 问卷参与人数
    private Integer joinStuNum;
    // 问卷发起处
    private String createName;
    // 单个学生的回答
    private QuestionnaireStudentDTO questionnaireStudentDTO;
}
