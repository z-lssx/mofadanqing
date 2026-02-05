package com.mofadanqing.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mofadanqing.entity.OperationLog;
import com.mofadanqing.mapper.OperationLogMapper;
import com.mofadanqing.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Override
    public void logOperation(Long userId, String operation, String resourceType, Long resourceId, 
                           String oldValue, String newValue, String ipAddress, String userAgent) {
        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setOperation(operation);
        log.setResourceType(resourceType);
        log.setResourceId(resourceId);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        log.setCreatedAt(LocalDateTime.now());
        
        operationLogMapper.insert(log);
    }

    @Override
    public IPage<OperationLog> getOperationLogPage(Page<OperationLog> page, String operation, String username) {
        return operationLogMapper.selectOperationLogPage(page, operation, username);
    }
}