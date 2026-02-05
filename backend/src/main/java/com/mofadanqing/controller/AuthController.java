package com.mofadanqing.controller;

import com.mofadanqing.entity.User;
import com.mofadanqing.service.UserService;
import com.mofadanqing.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 账号密码登录
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");

        User user = userService.login(username, password);
        String token = JwtUtil.generateToken(user);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    /**
     * 手机号验证码登录
     */
    @PostMapping("/login/sms")
    public Map<String, Object> loginBySms(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String smsCode = params.get("smsCode");
        User user = userService.loginByPhone(phone, smsCode);
        String token = JwtUtil.generateToken(user);
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        User registeredUser = userService.register(user);
        String token = JwtUtil.generateToken(registeredUser);
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", registeredUser);
        return result;
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public User getCurrentUser(@RequestHeader("Authorization") String authorization) {
        String token = authorization.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        return userService.getById(userId);
    }

    /**
     * 发送验证码
     */
    @PostMapping("/send-sms")
    public Map<String, Object> sendSms(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        // 这里应该调用短信服务发送验证码，暂时跳过
        Map<String, Object> result = new HashMap<>();
        result.put("message", "验证码已发送");
        return result;
    }

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/send-email-code")
    public Map<String, Object> sendEmailCode(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        // 这里应该调用邮箱服务发送验证码，暂时跳过
        Map<String, Object> result = new HashMap<>();
        result.put("message", "验证码已发送");
        return result;
    }

    /**
     * 修改密码
     */
    @PostMapping("/update-password")
    public Map<String, Object> updatePassword(@RequestBody Map<String, String> params, @RequestHeader("Authorization") String authorization) {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        
        // 从token中获取用户ID
        String token = authorization.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        // 调用服务更新密码
        userService.updatePassword(userId, oldPassword, newPassword);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "密码修改成功");
        return result;
    }

    /**
     * 修改邮箱
     */
    @PostMapping("/update-email")
    public Map<String, Object> updateEmail(@RequestBody Map<String, String> params, @RequestHeader("Authorization") String authorization) {
        String newEmail = params.get("newEmail");
        String code = params.get("code");
        
        // 从token中获取用户ID
        String token = authorization.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        // 调用服务更新邮箱
        userService.updateEmail(userId, newEmail, code);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "邮箱修改成功");
        return result;
    }

    /**
     * 修改手机号
     */
    @PostMapping("/update-phone")
    public Map<String, Object> updatePhone(@RequestBody Map<String, String> params, @RequestHeader("Authorization") String authorization) {
        String newPhone = params.get("newPhone");
        String code = params.get("code");
        
        // 从token中获取用户ID
        String token = authorization.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        // 调用服务更新手机号
        userService.updatePhone(userId, newPhone, code);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "手机号修改成功");
        return result;
    }
}
