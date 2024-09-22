package org.example.mapper;

import org.example.dto.QuestionnaireDTO;
import org.example.dto.QuestionnaireStudentDTO;
import org.apache.ibatis.annotations.*;

import java.util.List;
@Mapper
public interface QuestionnaireStudentMapper {

    /**
     * @return 查询问卷参与人数
     * @author fyj
     * @date 2023/11/23 9:28
     */
    @Select({"<script>select questionnaire_id,count(*) as joinStuNum from basic_questionnaire_student " +
            "where is_deleted = 0 and student_name is not null and class_name is not null and grade is not null and questionnaire_id in " +
            "<foreach collection=\"list\" index=\"index\" item=\"item\" open=\"(\" separator=\",\" close=\")\">\n" +
            " #{item}\n" +
            "</foreach> " +
            "group by questionnaire_id" +
            "</script>"})
    List<QuestionnaireDTO> queryJoinNum(@Param("list") List<Long> list) throws Exception;

    /**
     * @return 查询学生的已回答问卷
     * @author fyj
     * @date 2023/11/23 9:28
     */
    @Select({"<script>select questionnaire_id,student_id,gmt_create from basic_questionnaire_student " +
            "where is_deleted = 0 and student_id = #{studentId}" +
            "</script>"})
    List<QuestionnaireDTO> queryByStudentId(@Param("studentId") Long studentId) throws Exception;

    /**
     * @return 查询单个问卷的学生回答
     * @author fyj
     * @date 2023/11/23 9:28
     */
    @Select({"<script>select * from basic_questionnaire_student " +
            "where is_deleted = 0 and student_id = #{studentId} and questionnaire_id = #{questionnaireId} " +
            "limit 1" +
            "</script>"})
    QuestionnaireStudentDTO queryOne(@Param("studentId") Long studentId,
                                     @Param("questionnaireId") Long questionnaireId) throws Exception;

    /**
     * @return 新增问卷
     * @author fyj
     * @date 2023/11/23 9:28
     */
    @Insert("INSERT INTO basic_questionnaire_student(gmt_create,questionnaire_id," +
            "questionnaire_info,questionnaire_result," +
            "student_id,school_id,student_name,class_name,grade) " +
            "VALUES(now(),#{entity.questionnaireId},#{entity.questionnaireInfo}," +
            "#{entity.questionnaireResult},#{entity.studentId},#{entity.schoolId}," +
            "#{entity.studentName},#{entity.className},#{entity.grade}" +
            ")")
    @Options(useGeneratedKeys = true, keyProperty = "questionnaireStudentId", keyColumn = "questionnaire_student_id")
    Integer submitResult(@Param("entity") QuestionnaireStudentDTO entity) throws Exception;

    /**
     * @return 查询单个问卷的学生回答
     * @author fyj
     * @date 2023/11/23 9:28
     */
    @Select({"<script>select * from basic_questionnaire_student " +
            "where is_deleted = 0 and questionnaire_id = #{questionnaireId} and " +
            "student_name is not null and class_name is not null and grade is not null " +
            "<if test='ids != null'> " +
            "and school_id in " +
            "<foreach collection=\"ids\" index=\"index\" item=\"item\" open=\"(\" separator=\",\" close=\")\">\n" +
            " #{item}\n" +
            "</foreach>" +
            "</if> " +
            "order by gmt_create,questionnaire_student_id " +
            "</script>"})
    List<QuestionnaireStudentDTO> queryList(@Param("questionnaireId") Long questionnaireId,
                                            @Param("ids") List<Long> ids) throws Exception;

    /**
     * @return 删除已填写的问卷
     * @author fyj
     * @date 2023/11/23 9:28
     */
    @Update("<script>UPDATE basic_questionnaire_student SET " +
            "is_deleted = 1,gmt_modify = now() where " +
            "questionnaire_id in " +
            "<foreach collection=\"list\" index=\"index\" item=\"item\" open=\"(\" separator=\",\" close=\")\">\n" +
            " #{item}\n" +
            "</foreach>" +
            "</script>")
    Long removeAnswerByIds(@Param("list") List<Long> list) throws Exception;
}
