package org.example.utils.exception;

import com.alibaba.fastjson2.JSONObject;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * 本后台接口系统常用的json工具类
 */
public class CommonUtil {

    /**
     * 返回一个info为空对象的成功消息的json
     */
    public static JSONObject successJson() {
        return successJson(new JSONObject());
    }

    /**
     * 返回一个返回码为200的json
     */
    public static JSONObject successJson(Object info) {
        JSONObject resultJson = new JSONObject();
        resultJson.put("status", ExceptionConstants.SUCCESS_CODE);
        resultJson.put("message", ExceptionConstants.SUCCESS_MSG);
        resultJson.put("result", info);
        return resultJson;
    }

    /**
     * 返回错误信息JSON
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
     *
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
     * @param requestJson 请求参数json,此json在之前调用fillPageParam 方法时,已经将pageRow放入
     * @param list        查询分页对象list
     * @param totalCount  查询出记录的总条数
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
     * 查询分页结果后的封装工具方法
     *
     * @param list 查询分页对象list
     */
    public static JSONObject successPage(List<JSONObject> list) {
        JSONObject result = successJson();
        JSONObject info = new JSONObject();
        info.put("list", list);
        result.put("result", info);
        return result;
    }

    /**
     * 获取总页数
     *
     * @param pageRow   每页行数
     * @param itemCount 结果的总条数
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
     * 将request参数值转为json
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
     * 获取json字符串
     *
     * @param req
     * @return
     * @throws Exception
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
     * 获取字符串请求
     *
     * @return String
     * @throws Exception
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
     * 在分页查询之前,为查询条件里加上分页参数
     *
     * @param paramObject    查询条件json
     * @param defaultPageRow 默认的每页条数,即前端不传pageRow参数时的每页条数
     */
    private static void fillPageParam(final JSONObject paramObject, int defaultPageRow) {
        int pageNum = paramObject.getIntValue("pageNum");
        pageNum = pageNum == 0 ? 1 : pageNum;
        int pageRow = paramObject.getIntValue("pageRow");
        pageRow = pageRow == 0 ? defaultPageRow : pageRow;
        paramObject.put("offSet", (pageNum - 1) * pageRow);
        paramObject.put("pageRow", pageRow);
        paramObject.put("pageNum", pageNum);
        //删除此参数,防止前端传了这个参数,pageHelper分页插件检测到之后,拦截导致SQL错误
        paramObject.remove("pageSize");
    }

    /**
     * 信息**加密
     *
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
     * 分页查询之前的处理参数
     * 没有传pageRow参数时,默认每页10条.
     */
    public static void fillPageParam(final JSONObject paramObject) {
        fillPageParam(paramObject, 10);
    }
}
