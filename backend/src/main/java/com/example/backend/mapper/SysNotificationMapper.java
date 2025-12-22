package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.SysNotification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统通知Mapper
 */
@Mapper
public interface SysNotificationMapper extends BaseMapper<SysNotification> {
}
