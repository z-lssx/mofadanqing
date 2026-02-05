package com.mofadanqing.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mofadanqing.entity.User;
import com.mofadanqing.entity.UserMessage;
import com.mofadanqing.service.UserMessageService;
import com.mofadanqing.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/messages")
public class MessageController {
    @Autowired
    private UserMessageService userMessageService;

    @GetMapping
    public ResponseEntity<Map<String,Object>> list(
            @RequestParam(defaultValue="1") int page,
            @RequestParam(defaultValue="20") int size,
            @RequestParam(required=false) Boolean unread,
            HttpServletRequest request) {
        User u = currentUser(request);
        if (u == null) return error(401, "未登录");
        IPage<UserMessage> p = userMessageService.listMessages(new Page<>(page,size), u.getId(), unread);
        return ok(p);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String,Object>> markRead(@PathVariable Long id, HttpServletRequest request) {
        User u = currentUser(request);
        if (u == null) return error(401, "未登录");
        userMessageService.markRead(id, u.getId());
        return ok(null);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String,Object>> delete(@PathVariable Long id, HttpServletRequest request) {
        User u = currentUser(request);
        if (u == null) return error(401, "未登录");
        userMessageService.deleteMessage(id, u.getId());
        return ok(null);
    }

    @PostMapping("/batch/read")
    public ResponseEntity<Map<String,Object>> batchRead(@RequestBody java.util.List<Long> ids, HttpServletRequest request) {
        User u = currentUser(request);
        if (u == null) return error(401, "未登录");
        userMessageService.batchMarkRead(ids, u.getId());
        return ok(null);
    }

    @PostMapping("/batch/delete")
    public ResponseEntity<Map<String,Object>> batchDelete(@RequestBody java.util.List<Long> ids, HttpServletRequest request) {
        User u = currentUser(request);
        if (u == null) return error(401, "未登录");
        userMessageService.batchDelete(ids, u.getId());
        return ok(null);
    }

    private User currentUser(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                return JwtUtil.getUserFromToken(token);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private ResponseEntity<Map<String,Object>> ok(Object data) {
        Map<String,Object> res = new HashMap<>();
        res.put("code",200);
        res.put("message","success");
        res.put("data", data);
        return ResponseEntity.ok(res);
    }
    private ResponseEntity<Map<String,Object>> error(int code, String msg) {
        Map<String,Object> res = new HashMap<>();
        res.put("code", code);
        res.put("message", msg);
        return ResponseEntity.status(code).body(res);
    }
}
