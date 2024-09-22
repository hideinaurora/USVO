package org.example.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.*;
import org.example.mapper.QuestionnaireMapper;
import org.example.mapper.QuestionnaireQueueMapper;
import org.example.mapper.QuestionnaireStudentMapper;
import org.example.service.QuestionnaireService;
import org.example.utils.StringTools;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class QuestionnaireServiceImpl implements QuestionnaireService {

    @Resource
    private QuestionnaireMapper questionnaireMapper;
    @Resource
    private QuestionnaireStudentMapper questionnaireStudentMapper;
    @Resource
    private QuestionnaireQueueMapper questionnaireQueueMapper;

    @Override
    public TableResponseDTO query(TableRequestDTO tableRequestDTO, TokenDTO tokenDTO) throws Exception {
        // 市级看所有，区县看市级和本区县
        if (tokenDTO.getRoleId() == 1 || tokenDTO.getRoleId() == 4) {
            // 问卷名称
            String questionnaireName = StringTools.checkJsonKeyString(tableRequestDTO.getJsonSearch(), "questionnaireName", true);
            // 查询问卷列表
            List<QuestionnaireDTO> list = questionnaireMapper.query(questionnaireName);
            if (!list.isEmpty()) {
                // 按数据长度统计总数
                int count = list.size();
                // 起始索引位
                int start = tableRequestDTO.getStartOther();
                int pageSize = tableRequestDTO.getPageSize();
                // 结束索引位
                int toIndex = Math.min(count, (tableRequestDTO.getCurrentPage()) * pageSize);
                if (start < list.size()) {
                    // 程序进行分页
                    list = list.subList(start, toIndex);
                    if (list.size() > 0) {
                        // 问卷ids
                        List<Long> ids = list.stream().map(QuestionnaireDTO::getQuestionnaireId).collect(Collectors.toList());
                        // 填充问卷参与人数
                        List<QuestionnaireDTO> joinNum = questionnaireStudentMapper.queryJoinNum(ids);
                        list.forEach(a -> {
                            a.setJoinStuNum(0);
                            joinNum.forEach(b -> {
                                if (a.getQuestionnaireId().equals(b.getQuestionnaireId())) {
                                    a.setJoinStuNum(b.getJoinStuNum());
                                }
                            });
                        });
                        return new TableResponseDTO(list,
                                new PaginationDTO(tableRequestDTO.getCurrent(), tableRequestDTO.getPageSize(),
                                        (long) count, tableRequestDTO.getCurrentPage(),
                                        String.valueOf(tableRequestDTO.getSearch()),
                                        String.valueOf(tableRequestDTO.getSorter())));
                    }
                }
            }
        }
        return new TableResponseDTO(new ArrayList<>(),
                new PaginationDTO(tableRequestDTO.getCurrent(), tableRequestDTO.getPageSize(),
                        0L, tableRequestDTO.getCurrentPage(),
                        String.valueOf(tableRequestDTO.getSearch()),
                        String.valueOf(tableRequestDTO.getSorter())));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OpResultDTO add(QuestionnaireDTO entity) throws Exception {
        OpResultDTO op = new OpResultDTO();
        questionnaireMapper.add(entity);
        op.setLongResult(1L);
        return op;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OpResultDTO update(QuestionnaireDTO entity) throws Exception {
        OpResultDTO op = new OpResultDTO();
        questionnaireMapper.update(entity);
        op.setLongResult(1L);
        return op;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OpResultDTO remove(List<Long> ids, TokenDTO tokenDTO) throws Exception {
        OpResultDTO op = new OpResultDTO();
        questionnaireMapper.removeByIds(ids);
        // 删除已填写的问卷
        questionnaireStudentMapper.removeAnswerByIds(ids);
        op.setLongResult(1L);
        return op;
    }

    @Override
    public OpResultDTO queryNotAnswer(TokenDTO tokenDTO) throws Exception {
        OpResultDTO op = new OpResultDTO();
        // 查询学生的学校所有问卷
        List<QuestionnaireDTO> list = questionnaireMapper.queryBySchoolIdCopy();
        list = list.stream().filter(a -> a.getEndTime().isAfter(LocalDateTime.now())).collect(Collectors.toList());
        List<QuestionnaireDTO> data = new ArrayList<>();
        if (!data.isEmpty()) {
            // 查询学生已完成的问卷
            List<QuestionnaireDTO> join = questionnaireStudentMapper.queryByStudentId(tokenDTO.getAccountId());
            // 存放未完成的问卷
            List<QuestionnaireDTO> incompleteQuestionnaires = new ArrayList<>();
            for (QuestionnaireDTO questionnaire : data) {
                boolean completed = false;
                for (QuestionnaireDTO completedQuestionnaire : join) {
                    if (questionnaire.getQuestionnaireId().equals(completedQuestionnaire.getQuestionnaireId())) {
                        completed = true;
                        break;
                    }
                }
                // 未完成且未过期
                if (!completed && questionnaire.getEndTime().isAfter(LocalDateTime.now())) {
                    incompleteQuestionnaires.add(questionnaire);
                }
            }
            if (incompleteQuestionnaires.size() > 0) {
                op.setObjResult(incompleteQuestionnaires);
            }
        }
        op.setLongResult(1L);
        return op;
    }

    @Override
    public OpResultDTO queryFinishAnswer(TokenDTO tokenDTO) throws Exception {
        OpResultDTO op = new OpResultDTO();
        // 查询学生的学校所有问卷
        List<QuestionnaireDTO> list = questionnaireMapper.query(null);
        if (!list.isEmpty()) {
            // 查询学生已完成的问卷
            List<QuestionnaireDTO> join = questionnaireStudentMapper.queryByStudentId(tokenDTO.getAccountId());
            if (join.size() > 0) {
                // 存放未完成且已过期的问卷
                List<QuestionnaireDTO> incompleteQuestionnaires = new ArrayList<>();
                for (QuestionnaireDTO questionnaire : list) {
                    boolean completed = false;
                    for (QuestionnaireDTO completedQuestionnaire : join) {
                        if (questionnaire.getQuestionnaireId().equals(completedQuestionnaire.getQuestionnaireId())) {
                            completedQuestionnaire.setQuestionnaireName(questionnaire.getQuestionnaireName());
                            completedQuestionnaire.setEndTime(questionnaire.getEndTime());
                            completedQuestionnaire.setCreateBy(questionnaire.getCreateBy());
                            completed = true;
                            break;
                        }
                    }
                    // 未完成且已过期
                    if (!completed && questionnaire.getEndTime().isBefore(LocalDateTime.now())) {
                        incompleteQuestionnaires.add(questionnaire);
                    }
                }
                if (!incompleteQuestionnaires.isEmpty()) {
                    join.addAll(incompleteQuestionnaires);
                }
                op.setObjResult(join);
            }
        }
        op.setLongResult(1L);
        return op;
    }

    @Override
    public OpResultDTO queryOne(TokenDTO tokenDTO, Long questionnaireId) throws Exception {
        OpResultDTO op = new OpResultDTO();
        // 查询单个问卷信息
        QuestionnaireDTO dto = questionnaireMapper.queryOne(questionnaireId);
        if (dto != null) {
            if (tokenDTO.getRoleId().equals(4L)) {
                // 查询学生的回答
                QuestionnaireStudentDTO questionnaireStudentDTO = questionnaireStudentMapper.queryOne(tokenDTO.getAccountId(), questionnaireId);
                if (questionnaireStudentDTO != null) {
                    dto.setQuestionnaireStudentDTO(questionnaireStudentDTO);
                }
            }
        }
        op.setObjResult(dto);
        op.setLongResult(1L);
        return op;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OpResultDTO updateContent(QuestionnaireDTO entity) throws Exception {
        OpResultDTO op = new OpResultDTO();
        questionnaireMapper.updateContent(entity);
        op.setLongResult(1L);
        return op;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OpResultDTO submitResult(QuestionnaireStudentDTO entity) throws Exception {
        OpResultDTO op = new OpResultDTO();
        // 查询单个问卷信息
        QuestionnaireDTO dto = questionnaireMapper.queryOne(entity.getQuestionnaireId());
        questionnaireStudentMapper.submitResult(entity);
        op.setLongResult(1L);
        return op;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OpResultDTO isValid(Long questionnaireId, Integer isValid) throws Exception {
        OpResultDTO op = new OpResultDTO();
        questionnaireMapper.isValid(questionnaireId, isValid);
        op.setLongResult(1L);
        return op;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OpResultDTO copy(QuestionnaireDTO entity, TokenDTO tokenDTO) throws Exception {
        OpResultDTO op = new OpResultDTO();
        // 查询单个问卷信息
        QuestionnaireDTO dto = questionnaireMapper.queryOne(entity.getQuestionnaireId());
        entity.setQuestionnaireId(null);
        entity.setQuestionnaireContent(dto.getQuestionnaireContent());
        entity.setQuestionnaireInfo(dto.getQuestionnaireInfo());
        questionnaireMapper.add(entity);
        op.setLongResult(1L);
        return op;
    }
}
