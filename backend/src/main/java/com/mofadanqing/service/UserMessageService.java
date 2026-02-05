package com.mofadanqing.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mofadanqing.entity.UserMessage;

public interface UserMessageService {
    void addMessage(Long userId, String title, String content);
    IPage<UserMessage> listMessages(Page<UserMessage> page, Long userId, Boolean unread);
    void markRead(Long id, Long userId);
    void deleteMessage(Long id, Long userId);
    void batchMarkRead(java.util.List<Long> ids, Long userId);
    void batchDelete(java.util.List<Long> ids, Long userId);
}
