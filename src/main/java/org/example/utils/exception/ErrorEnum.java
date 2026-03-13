package org.example.utils.exception;

/**
 * 错误码汇总
 */
public enum ErrorEnum {
    /*
     * 系统错误信息
     * */
    E_400(400L, "系统未获取到相关记录，请刷新后重试"),
    E_401(401L, "未获取到相关记录"),
    E_500(500L, "请求方式有误,请检查"),
    E_501(501L, "请求路径不存在"),
    E_502(502L, "权限不足"),
    E_503(503L, "导入表格格式有误，请参照最新的导入模板"),


    ;

    private final Long errorCode;

    private final String errorMsg;

    ErrorEnum(Long errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public Long getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
