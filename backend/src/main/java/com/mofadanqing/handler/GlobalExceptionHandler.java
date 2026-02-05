package com.mofadanqing.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        // Handle "Access Denied" explicitly to return 403
        if (e.getMessage() != null && e.getMessage().contains("Access Denied")) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            response.put("status", HttpStatus.FORBIDDEN.value());
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", e.getMessage());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        e.printStackTrace(); // Log the full stack trace
        Map<String, Object> response = new HashMap<>();
        response.put("message", "服务器内部错误: " + e.getMessage());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
