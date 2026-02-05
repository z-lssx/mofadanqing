package com.mofadanqing.utils;

import com.mofadanqing.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    // JWT密钥
    private static final String SECRET_KEY = "mofadanqing_secret_key_2024_mofadanqing_secret_key_2024";
    private static final long EXPIRATION_TIME = 86400 * 1000; // 24小时
    
    private static final SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    
    // 存储token和用户信息的映射，用于快速验证
    private static final Map<String, User> tokenUserMap = new HashMap<>();

    /**
     * 生成JWT token
     */
    public static String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole());
        
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getId().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
        
        // 存储用户信息
        tokenUserMap.put(token, user);
        return token;
    }

    /**
     * 从token中获取用户信息
     */
    public static User getUserFromToken(String token) {
        try {
            // 先从缓存中获取
            if (tokenUserMap.containsKey(token)) {
                return tokenUserMap.get(token);
            }
            
            // 解析token
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            User user = new User();
            user.setId(Long.valueOf(claims.get("userId").toString()));
            user.setUsername(claims.get("username").toString());
            user.setRole(claims.get("role").toString());
            
            // 缓存用户信息
            tokenUserMap.put(token, user);
            return user;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 验证token是否有效
     */
    public static boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // 移除无效的token
            tokenUserMap.remove(token);
            return false;
        }
    }

    /**
     * 获取用户ID
     */
    public static Long getUserIdFromToken(String token) {
        User user = getUserFromToken(token);
        return user != null ? user.getId() : null;
    }

    // 以下为实例方法封装，兼容依赖注入的调用方式
    public String generateTokenInstance(User user) {
        return generateToken(user);
    }

    public User getUserFromTokenInstance(String token) {
        return getUserFromToken(token);
    }

    public boolean validateTokenInstance(String token) {
        return validateToken(token);
    }

    public Long getUserIdFromTokenInstance(String token) {
        return getUserIdFromToken(token);
    }
}
