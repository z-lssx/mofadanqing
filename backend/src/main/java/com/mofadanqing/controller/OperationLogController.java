package com.mofadanqing.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mofadanqing.entity.OperationLog;
import com.mofadanqing.entity.User;
import com.mofadanqing.service.OperationLogService;
import com.mofadanqing.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/operation-logs")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    /**
     * 获取操作日志列表（管理员）
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getOperationLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String username,
            HttpServletRequest request) {
        
        // 检查管理员权限
        if (!isAdmin(request)) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 403);
            response.put("message", "无权限操作");
            return ResponseEntity.status(403).body(response);
        }
        
        Page<OperationLog> pageRequest = new Page<>(page, size);
        IPage<OperationLog> logPage = operationLogService.getOperationLogPage(pageRequest, operation, username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", logPage);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 记录操作日志（内部使用）
     */
    public void logOperation(
            Long userId, String operation, String resourceType, Long resourceId,
            String oldValue, String newValue, String ipAddress, String userAgent) {
        operationLogService.logOperation(userId, operation, resourceType, resourceId, 
                                       oldValue, newValue, ipAddress, userAgent);
    }

    /**
     * 检查是否为管理员
     */
    private boolean isAdmin(HttpServletRequest request) {
        User user = getCurrentUserFromRequest(request);
        return user != null && "ADMIN".equals(user.getRole());
    }

    /**
     * 从请求中获取当前用户
     */
    private User getCurrentUserFromRequest(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                return JwtUtil.getUserFromToken(token);
            }
        } catch (Exception e) {
            // 忽略异常，返回null
        }
        return null;
    }
}
