package org.example.controller;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.example.aop.annotation.ApiAuth;
import org.example.aop.annotation.RequiresPermissions;
import org.example.dto.*;
import org.example.service.QuestionnaireService;
import org.example.utils.JWTUtil;
import org.example.utils.StringTools;
import org.example.utils.exception.CommonUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping(value = "/api/questionnaire")
@Slf4j
public class QuestionnaireController {

    @Resource
    private QuestionnaireService questionnaireService;

    /**
     * @return 查询问卷列表
     * @author fyj
     * @date 2023/11/23 9:28
     */
    @RequestMapping(value = "/query", method = RequestMethod.POST)
    @RequiresPermissions(isSave = false)
    public TableResponseDTO query(@RequestHeader(name = "token") String token,
                                  @RequestBody TableRequestDTO tableRequestDTO) {
        TableResponseDTO opResult = new TableResponseDTO();
        try {
            TokenDTO tokenDTO = JWTUtil.verifyToken(token);
            opResult = questionnaireService.query(tableRequestDTO, tokenDTO);
        } catch (Exception e) {
            log.error(e.toString());
        }
        return opResult;
    }

    /**
     * @return 新增问卷
     * @author fyj
     * @date 2023/11/23 9:28
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @RequiresPermissions(isSave = false)
    public OpResultDTO add(@RequestHeader(name = "token") String token,
                           @RequestBody QuestionnaireDTO entity) {
        OpResultDTO opResult = new OpResultDTO();
        try {
            TokenDTO tokenDTO = JWTUtil.verifyToken(token);
            opResult = questionnaireService.add(entity);
        } catch (Exception e) {
            opResult.setLongResult(-1L);
            opResult.setObjResult("");
            log.error(e.toString());
        }
        return opResult;
    }

    /**
     * @return 编辑问卷
     * @author fyj
     * @date 2023/11/23 9:28
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @RequiresPermissions(isSave = false)
    public OpResultDTO update(@RequestHeader(name = "token") String token,
                              @RequestBody QuestionnaireDTO entity) {
        OpResultDTO opResult = new OpResultDTO();
        try {
            TokenDTO tokenDTO = JWTUtil.verifyToken(token);
            // 角色id
            opResult = questionnaireService.update(entity);
        } catch (Exception e) {
            opResult.setLongResult(-1L);
            opResult.setObjResult("");
            log.error(e.toString());
        }
        return opResult;
    }

    /**
     * @return 删除问卷
     * @author fyj
     * @date 2023/11/23 9:28
     */
    @RequestMapping(value = "/remove", method = RequestMethod.POST)
    @RequiresPermissions(isSave = false)
    public OpResultDTO remove(@RequestHeader(name = "token") String token,
                              HttpServletRequest request) {
        OpResultDTO opResult = new OpResultDTO();
        try {
            TokenDTO tokenDTO = JWTUtil.verifyToken(token);
            JSONObject jsonObject = CommonUtil.getJsonObject(request);
            // 问卷ids
            List<Long> ids = jsonObject.getJSONArray("ids").toJavaList(Long.class);
            if (!ids.isEmpty()) {
                opResult = questionnaireService.remove(ids, tokenDTO);
            } else {
                opResult.setObjResult("缺少必填参数");
                opResult.setLongResult(-1L);
            }
        } catch (Exception e) {
            opResult.setLongResult(-1L);
            opResult.setObjResult("");
            log.error(e.toString());
        }
        return opResult;
    }

    /**
     * @return 查询学生未完成的问卷，且在有效期内
     * @author fyj
     * @date 2023/11/23 9:28
     */
    @RequestMapping(value = "/query/not/answer", method = RequestMethod.POST)
    @RequiresPermissions(isSave = false)
    public OpResultDTO queryNotAnswer(@RequestHeader(name = "token") String token) {
        OpResultDTO opResult = new OpResultDTO();
        try {
            TokenDTO tokenDTO = JWTUtil.verifyToken(token);
            opResult = questionnaireService.queryNotAnswer(tokenDTO);
        } catch (Exception e) {
            opResult.setLongResult(-1L);
            opResult.setObjResult("");
            log.error(e.toString());
        }
        return opResult;
    }

    /**
     * @return 查询学生已完成或已过期的问卷
     * @author fyj
     * @date 2023/11/23 9:28
     */
    @RequestMapping(value = "/query/finish/answer", method = RequestMethod.POST)
    @RequiresPermissions(isSave = false)
    public OpResultDTO queryFinishAnswer(@RequestHeader(name = "token") String token) {
        OpResultDTO opResult = new OpResultDTO();
        try {
            TokenDTO tokenDTO = JWTUtil.verifyToken(token);
            opResult = questionnaireService.queryFinishAnswer(tokenDTO);
        } catch (Exception e) {
            opResult.setLongResult(-1L);
            opResult.setObjResult("");
            log.error(e.toString());
        }
        return opResult;
    }

    /**
     * @return 查询单个问卷详情
     * @author fyj
     * @date 2023/11/23 9:28
     */
    @RequestMapping(value = "/query/one", method = RequestMethod.POST)
    @RequiresPermissions(isSave = false)
    public OpResultDTO queryOne(@RequestHeader(name = "token") String token,
                                HttpServletRequest request) {
        OpResultDTO opResult = new OpResultDTO();
        try {
            TokenDTO tokenDTO = JWTUtil.verifyToken(token);
            JSONObject jsonObject = CommonUtil.getJsonObject(request);
            // 问卷id
            Long questionnaireId = StringTools.checkJsonKeyLong(jsonObject, "questionnaireId", true);
            if (questionnaireId == null) {
                return StringTools.getErrorReturn("缺少必填参数");
            }
            opResult = questionnaireService.queryOne(tokenDTO, questionnaireId);
        } catch (Exception e) {
            opResult.setLongResult(-1L);
            opResult.setObjResult("");
            log.error(e.toString());
        }
        return opResult;
    }

    /**
     * @return 编辑问卷内容
     * @author fyj
     * @date 2023/11/23 9:28
     */
    @RequestMapping(value = "/update/content", method = RequestMethod.POST)
    @RequiresPermissions(isSave = false)
    public OpResultDTO updateContent(@RequestHeader(name = "token") String token,
                                     @RequestBody QuestionnaireDTO entity) {
        OpResultDTO opResult = new OpResultDTO();
        try {
            TokenDTO tokenDTO = JWTUtil.verifyToken(token);
            opResult = questionnaireService.updateContent(entity);
        } catch (Exception e) {
            opResult.setLongResult(-1L);
            opResult.setObjResult("");
            log.error(e.toString());
        }
        return opResult;
    }

    /**
     * @return 提交问卷结果
     * @author fyj
     * @date 2023/11/23 9:28
     */
    @RequestMapping(value = "/submit/result", method = RequestMethod.POST)
    @RequiresPermissions(isSave = false)
    public OpResultDTO submitResult(@RequestHeader(name = "token") String token,
                                    @RequestBody QuestionnaireStudentDTO entity) {
        OpResultDTO opResult = new OpResultDTO();
        try {
            TokenDTO tokenDTO = JWTUtil.verifyToken(token);
            entity.setStudentId(tokenDTO.getAccountId());
            opResult = questionnaireService.submitResult(entity);
        } catch (Exception e) {
            opResult.setLongResult(-1L);
            opResult.setObjResult("");
            log.error(e.toString());
        }
        return opResult;
    }

    /**
     * @return 发布
     * @author fyj
     * @date 2023/11/23 9:28
     */
    @RequestMapping(value = "/isValid", method = RequestMethod.POST)
    @RequiresPermissions(isSave = false)
    public OpResultDTO isValid(@RequestHeader(name = "token") String token,
                               HttpServletRequest request) {
        OpResultDTO opResult = new OpResultDTO();
        try {
            TokenDTO tokenDTO = JWTUtil.verifyToken(token);
            JSONObject jsonObject = CommonUtil.getJsonObject(request);
            // 问卷id
            Long questionnaireId = StringTools.checkJsonKeyLong(jsonObject, "questionnaireId", true);
            // 是否发布
            Integer isValid = StringTools.checkJsonKeyInt(jsonObject, "isValid");
            opResult = questionnaireService.isValid(questionnaireId, isValid);
        } catch (Exception e) {
            opResult.setLongResult(-1L);
            opResult.setObjResult("");
            log.error(e.toString());
        }
        return opResult;
    }

    /**
     * @return 复制
     * @author fyj
     * @date 2024/04/22 14:28
     */
    @RequestMapping(value = "/copy", method = RequestMethod.POST)
    @RequiresPermissions(isSave = false)
    public OpResultDTO copy(@RequestHeader(name = "token") String token,
                            @RequestBody QuestionnaireDTO entity) {
        OpResultDTO opResult = new OpResultDTO();
        try {
            TokenDTO tokenDTO = JWTUtil.verifyToken(token);
            opResult = questionnaireService.copy(entity, tokenDTO);
        } catch (Exception e) {
            opResult.setLongResult(-1L);
            opResult.setObjResult("");
            log.error(e.toString());
        }
        return opResult;
    }

}
