package org.example.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuestionnaireStudentDTO {

    // 学生填写问卷信息
    private Long questionnaireStudentId;
    // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime gmtCreate;
    // 修改时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime gmtModify;
    // 是否删除
    private Integer isDeleted;
    // 问卷id
    private Long questionnaireId;
    // 填写结果
    private String questionnaireResult;
    // 填写结果-只有类型、题目和填写内容
    private String questionnaireInfo;
    // 学生id
    private Long studentId;
    // 学生的学校id
    private Long schoolId;
    // 学生姓名
    private String studentName;
    // 学校名称
    private String schoolName;
    // 区县名称
    private String areaName;
    // 学段
    private String schoolPeriod;
    // 班级名称
    private String className;
    // 小学和初中
    private String xd;
    // 学生总数
    private Integer stuNum;
    // 填报人数
    private Integer joinStuNum;
    // 填报率
    private Double rate;
    // 区县id
    private Long parentId;
    // 年级
    private String grade;
}
