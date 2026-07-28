package org.example.service.dify;

import org.example.dto.dify.DifyQueryRequestDTO;

/**
 * Dify 智能体数据查询服务接口
 */
public interface DifyService {

    /**
     * 执行查询
     * @param request 查询请求
     * @return 查询结果
     */
    Object query(DifyQueryRequestDTO request);
}
