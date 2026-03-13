package org.example.mapper.failed.delayed;

import org.example.entity.failed.delayed.DelayedMessageEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 延迟消息失败记录表 Mapper 接口
 * </p>
 *
 * @author ckd
 * @since 2026-03-13
 */
@Mapper
public interface DelayedMessageMapper extends BaseMapper<DelayedMessageEntity> {

}
