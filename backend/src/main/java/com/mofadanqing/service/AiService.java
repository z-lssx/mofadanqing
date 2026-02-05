package com.mofadanqing.service;

import com.mofadanqing.entity.AiTask;

public interface AiService {
    /**
     * 创建 AI 生成任务
     */
    AiTask createTask(String prompt, String refImg, String style);

    /**
     * 获取任务状态
     */
    AiTask getTaskStatus(String taskId);

    /**
     * 执行 AI 生成（异步）
     */
    void executeTask(String taskId);
}
