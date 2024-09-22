package org.example.mapper;

import org.example.dto.QuestionnaireQueueDTO;
import org.apache.ibatis.annotations.*;

import java.util.List;
@Mapper
public interface QuestionnaireQueueMapper {

    /**
     * @return 查询问卷列表
     * @author fyj
     * @date 2024/04/22 16:28
     */
    @Select({"<script>SELECT * " +
            "FROM basic_questionnaire_queue " +
            "WHERE is_deleted = 0 and queue_lx = #{queueLx} " +
            "<if test='schoolId != null'> AND create_by = #{schoolId} </if>" +
            "<if test='questionnaireId != null'> AND questionnaire_id = #{questionnaireId} </if>" +
            "<if test='roleId != null'> AND role_id = #{roleId} </if>" +
            "order by queue_id desc" +
            "</script>"})
    List<QuestionnaireQueueDTO> list(@Param("schoolId") Long schoolId,
                                     @Param("questionnaireId") Long questionnaireId,
                                     @Param("queueLx") Integer queueLx,
                                     @Param("roleId") Long roleId) throws Exception;

    /**
     * @return 新增
     * @author fyj
     * @date 2024/04/22 16:28
     */
    @Insert("INSERT INTO basic_questionnaire_queue(questionnaire_id,gmt_create," +
            "create_by,queue_type,queue_lx,role_id) " +
            "VALUES(#{entity.questionnaireId},now(),#{entity.createBy}," +
            "#{entity.queueType},#{entity.queueLx},#{entity.roleId})")
    @Options(useGeneratedKeys = true, keyProperty = "queueId", keyColumn = "queue_id")
    Integer add(@Param("entity") QuestionnaireQueueDTO entity) throws Exception;

    /**
     * @return 更新
     * @author fyj
     * @date 2024/04/22 16:28
     */
    @Update("<script>UPDATE basic_questionnaire_queue SET " +
            "queue_type = #{queueType}, " +
            "queue_url = #{queueUrl}, " +
            "gmt_modify = now() " +
            "WHERE queue_id = #{queueId} " +
            "</script>")
    Integer update(@Param("queueId") Long queueId,
                   @Param("queueUrl") String queueUrl,
                   @Param("queueType") Integer queueType) throws Exception;

    /**
     * @return 查询单个
     * @author fyj
     * @date 2024/04/22 16:28
     */
    @Select({"<script>SELECT * " +
            "FROM basic_questionnaire_queue " +
            "WHERE is_deleted = 0 " +
            "and queue_id = #{queueId}" +
            "</script>"})
    QuestionnaireQueueDTO queryOne(@Param("queueId") Long queueId) throws Exception;
}
