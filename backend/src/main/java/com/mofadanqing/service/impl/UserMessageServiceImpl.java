package com.mofadanqing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mofadanqing.entity.UserMessage;
import com.mofadanqing.mapper.UserMessageMapper;
import com.mofadanqing.service.UserMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserMessageServiceImpl implements UserMessageService {
    @Autowired
    private UserMessageMapper userMessageMapper;

    @Override
    public void addMessage(Long userId, String title, String content) {
        UserMessage m = new UserMessage();
        m.setUserId(userId);
        m.setTitle(title);
        m.setContent(content);
        m.setIsRead(false);
        m.setCreatedAt(LocalDateTime.now());
        userMessageMapper.insert(m);
    }

    @Override
    public IPage<UserMessage> listMessages(Page<UserMessage> page, Long userId, Boolean unread) {
        QueryWrapper<UserMessage> w = new QueryWrapper<>();
        w.eq("user_id", userId);
        if (unread != null) w.eq("is_read", !unread);
        w.orderByDesc("created_at");
        return userMessageMapper.selectPage(page, w);
    }

    @Override
    public void markRead(Long id, Long userId) {
        UserMessage m = userMessageMapper.selectById(id);
        if (m == null) return;
        if (!m.getUserId().equals(userId)) return;
        m.setIsRead(true);
        userMessageMapper.updateById(m);
    }

    @Override
    public void deleteMessage(Long id, Long userId) {
        UserMessage m = userMessageMapper.selectById(id);
        if (m == null) return;
        if (!m.getUserId().equals(userId)) return;
        userMessageMapper.deleteById(id);
    }

    @Override
    public void batchMarkRead(java.util.List<Long> ids, Long userId) {
        if (ids == null || ids.isEmpty()) return;
        QueryWrapper<UserMessage> w = new QueryWrapper<>();
        w.in("id", ids).eq("user_id", userId);
        UserMessage u = new UserMessage();
        u.setIsRead(true);
        userMessageMapper.update(u, w);
    }

    @Override
    public void batchDelete(java.util.List<Long> ids, Long userId) {
        if (ids == null || ids.isEmpty()) return;
        QueryWrapper<UserMessage> w = new QueryWrapper<>();
        w.in("id", ids).eq("user_id", userId);
        userMessageMapper.delete(w);
    }
}
