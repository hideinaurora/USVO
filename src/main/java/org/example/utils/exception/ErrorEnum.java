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

    // 系统相关
    CHECK_IS_OK(200L, "校验通过"),
    NOT_HAS_AUTH(10001L, "权限不符"),
    LOGIN_OUT(20011L, "登录失效，请重新登录"),
    PARAMS_ERROR(10003L, "参数有误"),
    TOO_MANY_REQUEST(10004L, "请求次数过多，请稍后再试"),
    // 业务相关
    HAS_SAME_DEPT_NAME(20001L, "已存在相同的名称"),
    HAS_SAME_DEPT_AK(20003L, "已存在相同的ak"),
    HAS_SAME_DEPT_CODE(20004L, "已存在相同的代码"),
    HAS_SAME_DEPT_IDENTITY(20005L, "已存在相同的身份证号码"),
    DEPT_IS_NOT_EXIST(20002L, "单位不存在"),

    CAN_NOT_REFUND(20006L, "该订单类型无法退款"),
    REFUND_TIME_EXPIRED(20007L, "退款时间已截止"),
    REFUND_EXIST(20008L, "退款申请已提交"),
    ORDER_PAY_NOT_EXIST(20009L, "支付订单不存在"),
    ORDER_NOT_PAY(20010L, "订单未支付"),
    REFUND_AMOUNT_MORE_THAN_PAY(20012L, "退款金额不能大于支付金额"),
    REFUND_AMOUNT_CAN_NOT_BE_ZERO(20013L, "退款金额不能为0"),

    TEACHER_EXIST(30000L, "教师已存在"),


    ERROR_FAIL_ORC(20014L, "图像识别失败"),
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
