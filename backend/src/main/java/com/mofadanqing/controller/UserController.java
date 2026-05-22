package com.mofadanqing.controller;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mofadanqing.entity.User;
import com.mofadanqing.mapper.UserMapper;
import com.mofadanqing.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * 获取用户列表（管理员）
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String username,
            HttpServletRequest request) {
        
        // 检查管理员权限
        if (!isAdmin(request)) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 403);
            response.put("message", "无权限操作");
            return ResponseEntity.status(403).body(response);
        }
        
        Page<User> pageRequest = new Page<>(page, size);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like("username", username);
        }
        wrapper.orderByDesc("create_time");
        
        IPage<User> userPage = userMapper.selectPage(pageRequest, wrapper);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", userPage);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        User currentUser = getCurrentUserFromRequest(request);
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 401);
            response.put("message", "用户未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        // 检查权限：管理员可以查看任何用户，普通用户只能查看自己的信息
        if (!"ADMIN".equals(currentUser.getRole()) && !currentUser.getId().equals(id)) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 403);
            response.put("message", "无权限查看此用户信息");
            return ResponseEntity.status(403).body(response);
        }
        
        User user = userMapper.selectById(id);
        if (user == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 404);
            response.put("message", "用户不存在");
            return ResponseEntity.status(404).body(response);
        }
        
        // 隐藏密码信息
        user.setPassword(null);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", user);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 更新用户角色（管理员）
     */
    @PutMapping("/{id}/role")
    public ResponseEntity<Map<String, Object>> updateUserRole(
            @PathVariable Long id,
            @RequestParam String role,
            HttpServletRequest request) {
        
        // 检查管理员权限
        if (!isAdmin(request)) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 403);
            response.put("message", "无权限操作");
            return ResponseEntity.status(403).body(response);
        }
        
        User user = userMapper.selectById(id);
        if (user == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 404);
            response.put("message", "用户不存在");
            return ResponseEntity.status(404).body(response);
        }
        
        // 验证角色值
        if (!"USER".equals(role) && !"ADMIN".equals(role)) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "无效的角色值");
            return ResponseEntity.status(400).body(response);
        }
        
        user.setRole(role);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "用户角色更新成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 更新用户状态（管理员）
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateUserStatus(
            @PathVariable Long id,
            @RequestParam String status,
            HttpServletRequest request) {
        
        // 检查管理员权限
        if (!isAdmin(request)) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 403);
            response.put("message", "无权限操作");
            return ResponseEntity.status(403).body(response);
        }
        
        User user = userMapper.selectById(id);
        if (user == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 404);
            response.put("message", "用户不存在");
            return ResponseEntity.status(404).body(response);
        }
        
        // 验证状态值
        if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "无效的状态值");
            return ResponseEntity.status(400).body(response);
        }
        
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "用户状态更新成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        
        // 检查用户名是否已存在
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", request.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "用户名已存在");
            return ResponseEntity.status(400).body(response);
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        // 默认昵称等于用户名
        user.setNickname(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole("USER");
        user.setStatus("ACTIVE");
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        userMapper.insert(user);
        
        // 隐藏密码信息
        user.setPassword(null);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "用户注册成功");
        response.put("data", user);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        
        // 查找用户
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", request.getUsername());
        User user = userMapper.selectOne(wrapper);
        
        if (user == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 401);
            response.put("message", "用户名或密码错误");
            return ResponseEntity.status(401).body(response);
        }
        
        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 401);
            response.put("message", "用户名或密码错误");
            return ResponseEntity.status(401).body(response);
        }
        
        // 检查用户状态
        if (!"ACTIVE".equals(user.getStatus())) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 403);
            response.put("message", "用户已被禁用");
            return ResponseEntity.status(403).body(response);
        }
        
        // 生成JWT token
        String token = JwtUtil.generateToken(user);
        
        // 隐藏密码信息
        user.setPassword(null);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "登录成功");
        response.put("data", Map.of(
                "token", token,
                "user", user
        ));
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        User tokenUser = getCurrentUserFromRequest(request);
        if (tokenUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 401);
            response.put("message", "用户未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        // 关键修正：从数据库查询最新用户信息，而不是直接返回Token中的旧信息
        User currentUser = userMapper.selectById(tokenUser.getId());
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 404);
            response.put("message", "用户不存在");
            return ResponseEntity.status(404).body(response);
        }
        
        // 隐藏密码信息
        currentUser.setPassword(null);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", currentUser);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 更新当前用户信息（头像/昵称）
     */
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody UpdateProfileRequest request, HttpServletRequest httpRequest) {
        User currentUser = getCurrentUserFromRequest(httpRequest);
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 401);
            response.put("message", "用户未登录");
            return ResponseEntity.status(401).body(response);
        }

        // 重新查询最新信息，避免Token中数据过时
        User user = userMapper.selectById(currentUser.getId());
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("code", 404, "message", "用户不存在"));
        }

        boolean hasUpdate = false;
        StringBuilder msg = new StringBuilder("更新成功");

        // 允许更新 username (不推荐，但保留兼容)
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            if (!request.getUsername().equals(user.getUsername())) {
                user.setUsername(request.getUsername());
                hasUpdate = true;
            }
        }
        
        // 更新 nickname (主要用于显示的昵称)
        if (request.getNickname() != null && !request.getNickname().trim().isEmpty()) {
            if (!request.getNickname().equals(user.getNickname())) {
                user.setNickname(request.getNickname());
                hasUpdate = true;
            }
        }
        
        if (request.getAvatar() != null && !request.getAvatar().trim().isEmpty()) {
            if (!request.getAvatar().equals(user.getAvatar())) {
                user.setAvatar(request.getAvatar());
                hasUpdate = true;
            }
        }
        
        if (hasUpdate) {
            user.setUpdateTime(LocalDateTime.now());
            // 使用 updateById 让 MyBatis-Plus 自动处理字段映射
            int rows = userMapper.updateById(user);
            if (rows == 0) {
                msg = new StringBuilder("数据库更新失败，未影响任何行");
            }
        } else {
            msg = new StringBuilder("未检测到有效变更");
        }
        
        user.setPassword(null);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", msg.toString());
        response.put("data", user);
        return ResponseEntity.ok(response);
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

    /**
     * 注册请求DTO
     */
    public static class RegisterRequest {
        private String username;
        private String password;
        private String email;
        private String phone;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }

    /**
     * 登录请求DTO
     */
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * 更新资料请求DTO
     */
    public static class UpdateProfileRequest {
        private String username;
        private String nickname;
        private String avatar;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }
    }
}
