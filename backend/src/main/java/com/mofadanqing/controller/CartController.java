package com.mofadanqing.controller;

import com.mofadanqing.entity.CartItem;
import com.mofadanqing.entity.User;
import com.mofadanqing.service.CartService;
import com.mofadanqing.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getCart(HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) return unauthorized();

        List<CartItem> items = cartService.getUserCart(user.getId());
        return success(items);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addToCart(
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) return unauthorized();

        Long productId = Long.valueOf(payload.get("productId").toString());
        Integer quantity = Integer.valueOf(payload.get("quantity").toString());

        cartService.addToCart(user.getId(), productId, quantity);
        return success(null);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateQuantity(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> payload,
            HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) return unauthorized();

        cartService.updateQuantity(user.getId(), id, payload.get("quantity"));
        return success(null);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> removeCartItem(
            @PathVariable Long id,
            HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) return unauthorized();

        cartService.removeCartItem(user.getId(), id);
        return success(null);
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Object>> clearCart(HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) return unauthorized();

        cartService.clearCart(user.getId());
        return success(null);
    }

    private User getCurrentUser(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                return JwtUtil.getUserFromToken(token);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private ResponseEntity<Map<String, Object>> unauthorized() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 401);
        response.put("message", "用户未登录");
        return ResponseEntity.status(401).body(response);
    }

    private ResponseEntity<Map<String, Object>> success(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        if (data != null) response.put("data", data);
        return ResponseEntity.ok(response);
    }
}
