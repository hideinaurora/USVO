package org.example.service;


import org.example.dto.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface QuestionnaireService {

    /**
     * @return 查询问卷列表
     * @author fyj
     * @date 2023/11/23 9:28
     */
    TableResponseDTO query(TableRequestDTO tableRequestDTO, TokenDTO tokenDTO) throws Exception;

    /**
     * @return 新增问卷
     * @author fyj
     * @date 2023/11/23 9:28
     */
    OpResultDTO add(QuestionnaireDTO entity) throws Exception;

    /**
     * @return 编辑问卷
     * @author fyj
     * @date 2023/11/23 9:28
     */
    OpResultDTO update(QuestionnaireDTO entity) throws Exception;

    /**
     * @return 删除问卷
     * @author fyj
     * @date 2023/11/23 9:28
     */
    OpResultDTO remove(List<Long> ids, TokenDTO tokenDTO) throws Exception;

    /**
     * @return 查询学生未完成的问卷，且在有效期内
     * @author fyj
     * @date 2023/11/23 9:28
     */
    OpResultDTO queryNotAnswer(TokenDTO tokenDTO) throws Exception;

    /**
     * @return 查询学生已完成或已过期的问卷
     * @author fyj
     * @date 2023/11/23 9:28
     */
    OpResultDTO queryFinishAnswer(TokenDTO tokenDTO) throws Exception;

    /**
     * @return 查询单个问卷详情
     * @author fyj
     * @date 2023/11/23 9:28
     */
    OpResultDTO queryOne(TokenDTO tokenDTO, Long questionnaireId) throws Exception;

    /**
     * @return 编辑问卷内容
     * @author fyj
     * @date 2023/11/23 9:28
     */
    OpResultDTO updateContent(QuestionnaireDTO entity) throws Exception;

    /**
     * @return 提交问卷结果
     * @author fyj
     * @date 2023/11/23 9:28
     */
    OpResultDTO submitResult(QuestionnaireStudentDTO entity) throws Exception;

    /**
     * @return 发布
     * @author fyj
     * @date 2023/11/23 9:28
     */
    OpResultDTO isValid(Long questionnaireId, Integer isValid) throws Exception;

    /**
     * @return 复制
     * @author fyj
     * @date 2024/04/22 14:28
     */
    OpResultDTO copy(QuestionnaireDTO entity, TokenDTO tokenDTO) throws Exception;
}
