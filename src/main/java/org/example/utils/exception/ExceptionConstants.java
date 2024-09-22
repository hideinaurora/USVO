package org.example.utils.exception;

public class ExceptionConstants {
    public static final int SUCCESS_CODE = 200;
    public static final String SUCCESS_MSG = "success";
    public static final String FAIL_MSG = "error";

    /**
     * @return 非空校验
     * @author ckd
     * @date 2022/8/5 14:10
     */
    public static boolean isNullOrEmpty(String str) {
        return null == str || "".equals(str.trim()) || "null".equals(str.trim());
    }

    public static boolean isNullOrEmpty(Object obj) {
        return null == obj || "".equals(obj) || "null".equals(obj);
    }

}
