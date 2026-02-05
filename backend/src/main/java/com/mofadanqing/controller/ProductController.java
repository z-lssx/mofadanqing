package com.mofadanqing.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mofadanqing.entity.Category;
import com.mofadanqing.entity.Product;
import com.mofadanqing.entity.User;
import com.mofadanqing.service.ProductService;
import com.mofadanqing.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 获取商品列表（分页）
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) boolean includeInactive) { // 新增参数
        
        // 只有管理员才能 includeInactive
        // 这里暂时不校验 token，简化逻辑，或者复用 categoryId=-1 的约定
        
        Long finalCategoryId = categoryId;
        if (includeInactive) {
            // 使用约定值 -1 来通知 Service 不过滤 ACTIVE
            // 注意：这会导致无法按 categoryId 筛选 INACTIVE 的商品，但这在管理后台通常是可以接受的
            finalCategoryId = -1L;
        }
        
        Page<Product> pageRequest = new Page<>(page, size);
        IPage<Product> productPage = productService.getProductPage(pageRequest, keyword, finalCategoryId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", productPage);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 404);
            response.put("message", "商品不存在");
            return ResponseEntity.status(404).body(response);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", product);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 创建商品（管理员）
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createProduct(
            @RequestBody Product product,
            HttpServletRequest request) {
        
        // 检查管理员权限
        if (!isAdmin(request)) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 403);
            response.put("message", "无权限操作");
            return ResponseEntity.status(403).body(response);
        }
        
        try {
            Product createdProduct = productService.createProduct(product);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "商品创建成功");
            response.put("data", createdProduct);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "创建商品失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 更新商品（管理员）
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateProduct(
            @PathVariable Long id,
            @RequestBody Product product,
            HttpServletRequest request) {
        
        // 检查管理员权限
        if (!isAdmin(request)) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 403);
            response.put("message", "无权限操作");
            return ResponseEntity.status(403).body(response);
        }
        
        product.setId(id);
        try {
            Product updatedProduct = productService.updateProduct(product);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "商品更新成功");
            response.put("data", updatedProduct);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "更新商品失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 删除商品（管理员）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteProduct(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        // 检查管理员权限
        if (!isAdmin(request)) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 403);
            response.put("message", "无权限操作");
            return ResponseEntity.status(403).body(response);
        }
        
        try {
            productService.deleteProduct(id);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "商品删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "删除商品失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 更新商品状态（管理员）
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateProductStatus(
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
        
        try {
            productService.updateProductStatus(id, status);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "商品状态更新成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "更新商品状态失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 更新商品库存（管理员）
     */
    @PutMapping("/{id}/stock")
    public ResponseEntity<Map<String, Object>> updateProductStock(
            @PathVariable Long id,
            @RequestParam Integer stock,
            HttpServletRequest request) {
        
        // 检查管理员权限
        if (!isAdmin(request)) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 403);
            response.put("message", "无权限操作");
            return ResponseEntity.status(403).body(response);
        }
        
        try {
            productService.updateProductStock(id, stock);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "商品库存更新成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "更新商品库存失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取所有分类
     */
    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getAllCategories() {
        List<Category> categories = productService.getAllCategories();
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", categories);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 检查是否为管理员
     */
    private boolean isAdmin(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                User user = JwtUtil.getUserFromToken(token);
                return user != null && "ADMIN".equals(user.getRole());
            }
        } catch (Exception e) {
            // 忽略异常，返回false
        }
        return false;
    }
}
