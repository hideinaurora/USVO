package org.example.dto.dify;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Dify 智能体查询响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DifyQueryResponseDTO implements Serializable {

    private Integer code;
    private String message;
    private Object data;

    public static DifyQueryResponseDTO success(Object data) {
        return DifyQueryResponseDTO.builder()
                .code(0)
                .message("success")
                .data(data)
                .build();
    }

    public static DifyQueryResponseDTO error(Integer code, String message) {
        return DifyQueryResponseDTO.builder()
                .code(code)
                .message(message)
                .build();
    }

    public static DifyQueryResponseDTO paramError(String message) {
        return error(400, "参数错误：" + message);
    }

    public static DifyQueryResponseDTO notFound(String message) {
        return error(404, message);
    }

    public static DifyQueryResponseDTO serverError(String message) {
        return error(500, "服务器内部错误：" + message);
    }
}
