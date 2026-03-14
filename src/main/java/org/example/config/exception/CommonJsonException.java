package org.example.config.exception;

import org.example.dto.OpResultDTO;
import org.example.utils.StringTools;

/**
 * 拦截器可以统一拦截此错误,将其中json返回给前端
 */
public class CommonJsonException extends RuntimeException {
    private final OpResultDTO resultJson;

    public CommonJsonException(String errorMessage) {
        this.resultJson = StringTools.getErrorReturn(errorMessage);
    }
    public CommonJsonException(OpResultDTO resultJson) {
        this.resultJson = resultJson;
    }

    public OpResultDTO getResultJson() {
        return resultJson;
    }
}
