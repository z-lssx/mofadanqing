package com.mofadanqing.service.impl;

import com.mofadanqing.entity.AiTask;
import com.mofadanqing.mapper.AiTaskMapper;
import com.mofadanqing.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AiServiceImpl implements AiService {

    @Autowired
    private AiTaskMapper aiTaskMapper;
    
    @Autowired
    private AiTaskExecutor aiTaskExecutor;

    @Override
    public AiTask createTask(String prompt, String refImg, String style) {
        String taskId = UUID.randomUUID().toString();
        AiTask task = new AiTask();
        task.setTaskId(taskId);
        task.setPrompt(prompt);
        task.setRefImg(refImg);
        task.setStyle(style);
        task.setStatus("PENDING");
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        aiTaskMapper.insert(task);
        
        // Asynchronous execution via dedicated executor
        aiTaskExecutor.executeTask(taskId);
        
        return task;
    }

    @Override
    public AiTask getTaskStatus(String taskId) {
        return aiTaskMapper.selectById(taskId);
    }
    
    // Previous executeTask and callQwenImagePlus methods removed as they are moved to AiTaskExecutor
    @Override
    public void executeTask(String taskId) {
        // Delegate to executor if called directly, though typically not used from interface in this impl
        aiTaskExecutor.executeTask(taskId);
    }
}
