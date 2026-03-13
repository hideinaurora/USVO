package org.example.config.exception;

import org.example.common.ApiResponse;
import org.example.dto.OpResultDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一异常拦截
 */
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @ExceptionHandler(value = Exception.class)
    public ApiResponse<?> defaultErrorHandler(HttpServletRequest req, Exception e) {
        logger.error("异常", e);
        return ApiResponse.error(500, "服务器异常，请稍后重试");
    }

    /**
     * GET/POST请求方法错误的拦截器
     * 因为开发时可能比较常见,而且发生在进入controller之前,上面的拦截器拦截不到这个错误
     * 所以定义了这个拦截器
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResponse<?> httpRequestMethodHandler() {
        return ApiResponse.error(405, "请求方式不符");
    }

    /**
     * 本系统自定义错误的拦截器
     * 拦截到此错误之后,就返回这个类里面的json给前端
     * 常见使用场景是参数校验失败,抛出此错,返回错误信息给前端
     */
    @ExceptionHandler(org.example.config.exception.CommonJsonException.class)
    public ApiResponse<?> commonJsonExceptionHandler(org.example.config.exception.CommonJsonException commonJsonException) {
        OpResultDTO resultJson = commonJsonException.getResultJson();
        String message = resultJson.getObjResult() != null ? resultJson.getObjResult().toString() : "操作失败";
        return ApiResponse.error(Integer.parseInt(resultJson.getLongResult().toString()), message);
    }

    /**
     * 权限不足报错拦截
     */
    @ExceptionHandler(org.example.config.exception.UnauthorizedException.class)
    public ApiResponse<?> unauthorizedExceptionHandler() {
        return ApiResponse.error(403, "权限不符");
    }

    /**
     * 未登录报错拦截
     * 在请求需要权限的接口,而连登录都还没登录的时候,会报此错
     */
    @ExceptionHandler(org.example.config.exception.UnauthenticatedException.class)
    public ApiResponse<?> unauthenticatedException() {
        return ApiResponse.error(401, "登录失效请重新登录");
    }

    /**
     * 数据校验
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ApiResponse<?> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        Map<String, String> error = new HashMap<>();
        List<ObjectError> allErrors = e.getBindingResult().getAllErrors();
        allErrors.forEach(er -> {
            String fieldName = ((FieldError) er).getField();
            String message = er.getDefaultMessage();
            error.put(fieldName, message);
        });
        return ApiResponse.error(error.toString());
    }
}
