package com.mofadanqing.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mofadanqing.entity.OperationLog;

public interface OperationLogService {
    
    /**
     * 记录操作日志
     */
    void logOperation(Long userId, String operation, String resourceType, Long resourceId, 
                     String oldValue, String newValue, String ipAddress, String userAgent);
    
    /**
     * 获取操作日志列表（分页）
     */
    IPage<OperationLog> getOperationLogPage(Page<OperationLog> page, String operation, String username);
}