package org.example.mapper;

import org.example.dto.QuestionnaireDTO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QuestionnaireMapper {

    /**
     * @return 查询问卷列表
     * @author fyj
     * @date 2023/11/23 9:28
     */
    @Select({"<script>SELECT questionnaire_id,gmt_create,questionnaire_name,create_by," +
            "start_time,end_time,questionnaire_introduce,school_ids,school_parent_ids,is_valid " +
            "FROM basic_questionnaire " +
            "WHERE is_deleted = 0 " +
            "<if test='questionnaireName != null'> AND questionnaire_name LIKE CONCAT('%', #{questionnaireName}, '%') </if> " +
            "order by questionnaire_id desc " +
            "</script>"})
    List<QuestionnaireDTO> query(@Param("questionnaireName") String questionnaireName) throws Exception;

    /**
     * @return 新增问卷
     * @author fyj
     * @date 2023/11/23 9:28
     */
    @Insert("INSERT INTO basic_questionnaire(questionnaire_name,start_time," +
            "end_time,gmt_create,questionnaire_introduce,school_ids,school_parent_ids," +
            "questionnaire_content,questionnaire_info,create_by) " +
            "VALUES(#{entity.questionnaireName},#{entity.startTime},#{entity.endTime}," +
            "now(),#{entity.questionnaireIntroduce},#{entity.schoolIds},#{entity.schoolParentIds}," +
            "#{entity.questionnaireContent},#{entity.questionnaireInfo},#{entity.createBy}" +
            ")")
    @Options(useGeneratedKeys = true, keyProperty = "questionnaireId", keyColumn = "questionnaire_id")
    Integer add(@Param("entity") QuestionnaireDTO entity) throws Exception;

    /**
     * @return 编辑问卷
     * @author fyj
     * @date 2023/11/23 9:28
     */
    @Update("<script>UPDATE basic_questionnaire SET " +
            "questionnaire_name = #{entity.questionnaireName}, " +
            "start_time = #{entity.startTime}, " +
            "end_time = #{entity.endTime}, " +
            "questionnaire_introduce = #{entity.questionnaireIntroduce}, " +
            "school_ids = #{entity.schoolIds}, " +
            "school_parent_ids = #{entity.schoolParentIds}, " +
            "gmt_modify = now() " +
            "WHERE questionnaire_id = #{entity.questionnaireId}" +
            "</script>")
    Integer update(@Param("entity") QuestionnaireDTO entity) throws Exception;

    /**
     * @return 删除问卷
     * @author fyj
     * @date 2023/11/23 9:28
     */
    @Update("<script>UPDATE basic_questionnaire SET " +
            "is_deleted = 1,gmt_modify = now() where " +
            "questionnaire_id in " +
            "<foreach collection=\"list\" index=\"index\" item=\"item\" open=\"(\" separator=\",\" close=\")\">\n" +
            " #{item}\n" +
            "</foreach>" +
            "</script>")
    Long removeByIds(@Param("list") List<Long> list) throws Exception;

    /**
     * @return 查询某个学校所有问卷
     * @author fyj
     * @date 2023/11/23 9:28
     */
    @Select({"<script>SELECT questionnaire_id,gmt_create,questionnaire_name,create_by," +
            "start_time,end_time,questionnaire_introduce,school_ids,school_parent_ids " +
            "FROM basic_questionnaire " +
            "WHERE is_deleted = 0 and FIND_IN_SET(#{schoolId}, REPLACE(REPLACE(school_ids, '[', ''), ']', '')) and is_valid = 1 " +
            "order by end_time asc" +
            "</script>"})
    List<QuestionnaireDTO> queryBySchoolId(@Param("schoolId") Long schoolId) throws Exception;

    /**
     * @return 查询某个学校所有问卷
     * @author fyj
     * @date 2023/11/23 9:28
     */
    @Select({"<script>SELECT questionnaire_id,gmt_create,questionnaire_name,create_by," +
            "start_time,end_time,questionnaire_introduce,school_ids,school_parent_ids " +
            "FROM basic_questionnaire " +
            "WHERE is_deleted = 0 and is_valid = 1 " +
            "order by end_time asc" +
            "</script>"})
    List<QuestionnaireDTO> queryBySchoolIdCopy() throws Exception;

    /**
     * @return 查询单个问卷
     * @author fyj
     * @date 2023/11/23 9:28
     */
    @Select({"<script>SELECT * " +
            "FROM basic_questionnaire " +
            "WHERE is_deleted = 0 and questionnaire_id = #{questionnaireId} " +
            "</script>"})
    QuestionnaireDTO queryOne(@Param("questionnaireId") Long questionnaireId) throws Exception;

    /**
     * @return 编辑问卷内容
     * @author fyj
     * @date 2023/11/23 9:28
     */
    @Update("<script>UPDATE basic_questionnaire SET " +
            "questionnaire_content = #{entity.questionnaireContent}, " +
            "questionnaire_info = #{entity.questionnaireInfo}, " +
            "gmt_modify = now() " +
            "WHERE questionnaire_id = #{entity.questionnaireId}" +
            "</script>")
    Integer updateContent(@Param("entity") QuestionnaireDTO entity) throws Exception;

    /**
     * @return 是否上架
     * @author fyj
     * @date 2023/11/23 9:28
     */
    @Update("<script>UPDATE basic_questionnaire SET " +
            "is_valid = #{isValid} " +
            "WHERE questionnaire_id = #{questionnaireId}" +
            "</script>")
    Integer isValid(@Param("questionnaireId") Long questionnaireId,
                    @Param("isValid") Integer isValid) throws Exception;
}
