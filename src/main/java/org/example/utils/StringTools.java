package org.example.utils;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.example.dto.OpResultDTO;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringTools {


    public static boolean isNullOrEmpty(String str) {
        return null == str || "".equals(str) || "null".equals(str);
    }

    public static boolean isNotEmpty(String val) {
        return val != null && StringUtils.isNotEmpty(val.trim()) && !"null".equals(val);
    }

    public static boolean isNullOrEmpty(Object obj) {
        return null == obj || "".equals(obj) || "null".equals(obj);
    }

    /**
     * 手机号判断
     *
     * @author ckd
     * @date 2022/3/27 3:39 下午
     */
    public static Boolean isPhone(String str) {
        Boolean flag = false;
        if (str != null) {
            String regExp = "^1[3-9][0-9]\\d{8}$";
            Pattern p = Pattern.compile(regExp);
            Matcher m = p.matcher(str);
            flag = m.matches();
        }
        return flag;
    }

    public static Integer checkJsonKeyInt(JSONObject jsonObject, String key) {
        if (jsonObject != null && jsonObject.containsKey(key) && isNotEmpty(jsonObject.getString(key))) {
            return jsonObject.getInteger(key);
        }
        return 0;
    }

    public static Long checkJsonKeyLong(JSONObject jsonObject, String key, boolean backNull) {
        if (jsonObject != null && jsonObject.containsKey(key) && isNotEmpty(jsonObject.getString(key))) {
            return jsonObject.getLong(key);
        }
        return backNull ? null : 0L;
    }

    public static BigDecimal checkJsonKeyDecimal(JSONObject jsonObject, String key, boolean backNull) {
        if (jsonObject != null && jsonObject.containsKey(key) && isNotEmpty(jsonObject.getString(key))) {
            return jsonObject.getBigDecimal(key);
        }
        return backNull ? null : BigDecimal.ZERO;
    }

    public static String checkJsonKeyString(JSONObject jsonObject, String key, boolean backNull) {
        if (jsonObject != null && jsonObject.containsKey(key) && isNotEmpty(jsonObject.getString(key))) {
            return jsonObject.getString(key);
        }
        return backNull ? null : "";
    }

    public static OpResultDTO getErrorReturn(String msg) {
        OpResultDTO op = new OpResultDTO();
        op.setLongResult(-1L);
        op.setObjResult(msg);
        return op;
    }

    public static OpResultDTO getErrorReturn(Object error) {
        OpResultDTO op = new OpResultDTO();
        op.setObjResult(error);
        op.setLongResult(-1L);
        return op;
    }
}
