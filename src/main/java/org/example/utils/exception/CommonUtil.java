package org.example.utils.exception;

import com.alibaba.fastjson2.JSONObject;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * 通用 JSON 响应封装工具类
 * 提供成功、失败响应的标准化 JSON 格式封装，以及请求参数处理、分页参数封装等功能
 *
 * @author ckd
 * @since 2026-03-13
 */
public class CommonUtil {

    /**
     * 返回一个 info 为空对象的成功消息的 json
     * 默认状态码 200，消息 "success"
     *
     * @return JSONObject 成功响应对象
     */
    public static JSONObject successJson() {
        return successJson(new JSONObject());
    }

    /**
     * 返回一个包含结果数据的成功消息的 json
     *
     * @param info 结果数据对象
     * @return JSONObject 成功响应对象
     */
    public static JSONObject successJson(Object info) {
        JSONObject resultJson = new JSONObject();
        resultJson.put("status", ExceptionConstants.SUCCESS_CODE);
        resultJson.put("message", ExceptionConstants.SUCCESS_MSG);
        resultJson.put("result", info);
        return resultJson;
    }

    /**
     * 根据错误枚举返回错误信息 JSON
     *
     * @param errorEnum 错误码枚举
     * @return JSONObject 错误响应对象
     */
    public static JSONObject errorJson(ErrorEnum errorEnum) {
        JSONObject resultJson = new JSONObject();
        resultJson.put("status", errorEnum.getErrorCode());
        resultJson.put("message", errorEnum.getErrorMsg());
        resultJson.put("result", null);
        return resultJson;
    }

    /**
     * 自定义返回异常说明
     * 默认错误码 10099
     *
     * @param msg 异常提示信息
     * @return JSONObject 错误响应对象
     * @author ckd
     * @date 2022/5/17 9:10 下午
     */
    public static JSONObject errorJsonStr(String msg) {
        JSONObject resultJson = new JSONObject();
        resultJson.put("status", 10099L);
        resultJson.put("message", msg);
        resultJson.put("result", null);
        return resultJson;
    }

    /**
     * 自定义错误码和消息的响应
     *
     * @param errCode 错误码
     * @param msg 错误消息
     * @return JSONObject 错误响应对象
     */
    public static JSONObject errorJsonStr(Integer errCode, String msg) {
        JSONObject resultJson = new JSONObject();
        resultJson.put("status", errCode);
        resultJson.put("message", msg);
        resultJson.put("result", null);
        return resultJson;
    }

    /**
     * 查询分页结果后的封装工具方法
     *
     * @param requestJson 请求参数 json, 此 json 在之前调用 fillPageParam 方法时, 已经将 pageRow 放入
     * @param list        查询分页对象 list
     * @param totalCount  查询出记录的总条数
     * @return JSONObject 包含分页列表和统计信息的成功响应对象
     */
    public static JSONObject successPage(final JSONObject requestJson, List<JSONObject> list, int totalCount) {
        int pageRow = requestJson.getIntValue("pageRow");
        int totalPage = getPageCounts(pageRow, totalCount);
        JSONObject result = successJson();
        JSONObject info = new JSONObject();
        info.put("list", list);
        info.put("totalCount", totalCount);
        info.put("totalPage", totalPage);
        result.put("result", info);
        return result;
    }

    /**
     * 查询分页结果后的简单封装方法
     * 仅包含数据列表，不包含总页数等统计信息
     *
     * @param list 查询分页对象 list
     * @return JSONObject 包含列表的成功响应对象
     */
    public static JSONObject successPage(List<JSONObject> list) {
        JSONObject result = successJson();
        JSONObject info = new JSONObject();
        info.put("list", list);
        result.put("result", info);
        return result;
    }

    /**
     * 计算总页数
     *
     * @param pageRow   每页行数
     * @param itemCount 结果的总条数
     * @return 总页数
     */
    private static int getPageCounts(int pageRow, int itemCount) {
        if (itemCount == 0) {
            return 1;
        }
        return itemCount % pageRow > 0 ?
                itemCount / pageRow + 1 :
                itemCount / pageRow;
    }

    /**
     * 将 HttpServletRequest 中的 Parameter 转换为 JSONObject
     * 支持多值参数，使用逗号分隔
     *
     * @param request HTTP 请求对象
     * @return 转换后的 JSON 对象
     */
    public static JSONObject request2Json(HttpServletRequest request) {
        JSONObject requestJson = new JSONObject();
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            String[] pv = request.getParameterValues(paramName);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < pv.length; i++) {
                if (pv[i].length() > 0) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append(pv[i]);
                }
            }
            requestJson.put(paramName, sb.toString());
        }
        return requestJson;
    }

    /**
     * 从请求输入流中获取 JSON 字符串并解析为 JSONObject
     * 常用于处理 POST 请求中的 application/json 数据
     *
     * @param req HTTP 请求对象
     * @return 解析后的 JSON 对象
     * @throws Exception 流读取或解析异常
     */
    public static JSONObject getJsonObject(HttpServletRequest req) throws Exception {
        ServletInputStream is;
        is = req.getInputStream();
        int nRead = 1;
        int nTotalRead = 0;
        byte[] bytes = new byte[10240];
        while (nRead > 0) {
            nRead = is.read(bytes, nTotalRead, bytes.length - nTotalRead);
            if (nRead > 0) {
                nTotalRead = nTotalRead + nRead;
            }
        }
        String str = new String(bytes, 0, nTotalRead, "utf-8");
        return JSONObject.parseObject(str);
    }


    /**
     * 从请求输入流中获取原始字符串请求体
     *
     * @param req HTTP 请求对象
     * @return 原始请求体字符串
     * @throws Exception 流读取异常
     * @author ckd
     * @date 2021/12/1 3:31 下午
     */
    public static String getString(HttpServletRequest req) throws Exception {
        ServletInputStream is;
        is = req.getInputStream();
        int nRead = 1;
        int nTotalRead = 0;
        byte[] bytes = new byte[102400];
        while (nRead > 0) {
            nRead = is.read(bytes, nTotalRead, bytes.length - nTotalRead);
            if (nRead > 0) {
                nTotalRead = nTotalRead + nRead;
            }
        }
        return new String(bytes, 0, nTotalRead, "utf-8");
    }

    /**
     * 在分页查询之前, 为查询条件里加上分页参数
     * 计算数据库查询的 offSet 偏移量
     *
     * @param paramObject    查询条件 json
     * @param defaultPageRow 默认的每页条数, 即前端不传 pageRow 参数时的每页条数
     */
    private static void fillPageParam(final JSONObject paramObject, int defaultPageRow) {
        int pageNum = paramObject.getIntValue("pageNum");
        pageNum = pageNum == 0 ? 1 : pageNum;
        int pageRow = paramObject.getIntValue("pageRow");
        pageRow = pageRow == 0 ? defaultPageRow : pageRow;
        paramObject.put("offSet", (pageNum - 1) * pageRow);
        paramObject.put("pageRow", pageRow);
        paramObject.put("pageNum", pageNum);
        // 删除此参数, 防止前端传了这个参数, pageHelper 分页插件检测到之后, 拦截导致 SQL 错误
        paramObject.remove("pageSize");
    }

    /**
     * 信息脱敏处理（前3后3，中间用 **** 代替）
     * 适用于手机号、证件号等隐私信息的简单脱敏
     *
     * @param info 原始信息
     * @return 脱敏后的信息
     * @author ckd
     * @date 2022/3/17 7:49 下午
     */
    public static String encryptionInfo(String info) {
        if (info != null && info.length() > 4) {
            return info.substring(0, 3) + "****" + info.substring(info.length() - 3);
        } else {
            return "";
        }
    }

    /**
     * 分页查询之前的参数预处理（默认每页10条）
     *
     * @param paramObject 查询条件 JSON 对象
     */
    public static void fillPageParam(final JSONObject paramObject) {
        fillPageParam(paramObject, 10);
    }
}
