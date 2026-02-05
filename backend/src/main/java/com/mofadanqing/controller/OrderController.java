package com.mofadanqing.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mofadanqing.entity.Order;
import com.mofadanqing.entity.OrderItem;
import com.mofadanqing.entity.User;
import com.mofadanqing.enums.C2MStatus;
import com.mofadanqing.service.OrderService;
import com.mofadanqing.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private com.mofadanqing.controller.AiController aiController; // Reuse confirm logic

    @Autowired
    private com.mofadanqing.mapper.OrderMapper orderMapper;

    @Autowired
    private com.mofadanqing.mapper.OrderItemMapper orderItemMapper;

    @Autowired
    private com.mofadanqing.mapper.ProductMapper productMapper;

    /**
     * Create a new C2M Order from Custom Page (Direct Flow)
     */
    @PostMapping("/create-c2m")
    public Map<String, Object> createC2mOrder(@RequestBody CreateC2mOrderRequest req, @RequestAttribute(name = "userId", required = false) Long userIdAttr, HttpServletRequest request) {
        // Fallback to get user from token if attribute not set (depending on interceptor)
        Long userId = userIdAttr;
        if (userId == null) {
            User user = getCurrentUser(request);
            if (user != null) userId = user.getId();
        }
        
        if (userId == null) {
            throw new RuntimeException("User not logged in");
        }

        // 1. Create Basic Order
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderNo(java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
        order.setTotalAmount(java.math.BigDecimal.ZERO); // Will update later
        order.setStatus("PENDING_PAYMENT"); // Initial status
        order.setShippingAddress(req.getAddress() != null ? req.getAddress() : "Default Address"); // Simplify for now
        order.setCreateTime(java.time.LocalDateTime.now());
        order.setUpdateTime(java.time.LocalDateTime.now());
        orderMapper.insert(order);
        
        // 2. Create Order Item
        // Find product or use default
        Long productId = req.getProductId() != null ? req.getProductId() : 1L; // Default to first product if null
        // Ensure product exists (mock check or query)
        com.mofadanqing.entity.Product product = productMapper.selectById(productId);
        if (product == null) {
             // Fallback default
             product = new com.mofadanqing.entity.Product();
             product.setId(productId);
             product.setName("定制刺绣");
             product.setPrice(1999.00);
             product.setCoverImg("");
        }
        
        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setProductId(productId);
        item.setProductName(req.getProductName() != null ? req.getProductName() : product.getName());
        item.setProductImage(product.getCoverImg()); // Initial image
        item.setQuantity(req.getQuantity() != null ? req.getQuantity() : 1);
        item.setUnitPrice(new java.math.BigDecimal(product.getPrice()));
        item.setSubtotal(new java.math.BigDecimal(product.getPrice()).multiply(new java.math.BigDecimal(item.getQuantity())));
        item.setC2mStatus("CONFIRMED"); // Directly confirmed
        item.setC2mType("EMBROIDERY");
        // Auto-generate Logistics Tracking No
        String trackingNo = "TN" + System.currentTimeMillis() + (int)(Math.random() * 9000 + 1000);
        item.setTrackingNo(trackingNo);
        item.setCreatedAt(java.time.LocalDateTime.now());
        
        orderItemMapper.insert(item);
        
        // 3. Confirm Design & Associate (Reuse AiController logic or call service)
        // We can call the confirm logic programmatically
        com.mofadanqing.dto.ConfirmDesignRequest confirmReq = new com.mofadanqing.dto.ConfirmDesignRequest();
        confirmReq.setTaskId(req.getTaskId());
        confirmReq.setOrderItemId(item.getId());
        
        Map<String, Object> confirmResult = aiController.confirmDesign(confirmReq, request);
        
        // 4. Update Order Total Amount from Design Price
        if (confirmResult.get("code").equals(200)) {
            Map<String, Object> data = (Map<String, Object>) confirmResult.get("data");
            java.math.BigDecimal finalPrice = (java.math.BigDecimal) data.get("finalPrice");
            // Total = Final Price * Quantity
            order.setTotalAmount(finalPrice.multiply(new java.math.BigDecimal(item.getQuantity())));
            orderMapper.updateById(order);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "C2M Order created successfully");
        response.put("data", Map.of("orderId", order.getId(), "orderNo", order.getOrderNo()));
        return response;
    }

    @Autowired
    private OrderService orderService;
    @GetMapping
    public ResponseEntity<Map<String, Object>> getOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        
        Page<Order> pageRequest = new Page<>(page, size);
        IPage<Order> orderPage;
        
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 401);
            response.put("message", "用户未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        // “我的订单”严格只返回当前登录用户的订单，不区分角色
        orderPage = orderService.getUserOrders(pageRequest, currentUser.getId(), status);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", orderPage);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 管理端：查看全量订单（仅管理员）
     */
    @GetMapping("/admin")
    public ResponseEntity<Map<String, Object>> getAllOrdersForAdmin(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 401);
            response.put("message", "用户未登录");
            return ResponseEntity.status(401).body(response);
        }
        if (!"ADMIN".equals(currentUser.getRole())) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 403);
            response.put("message", "无权限查看全量订单");
            return ResponseEntity.status(403).body(response);
        }
        
        Page<Order> pageRequest = new Page<>(page, size);
        IPage<Order> orderPage = orderService.getOrderPage(pageRequest, null, status);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", orderPage);
        return ResponseEntity.ok(response);
    }



    /**
     * 获取订单详情（支持 ID 或 OrderNo）
     */
    @GetMapping("/{idOrNo}")
    public ResponseEntity<Map<String, Object>> getOrderById(
            @PathVariable String idOrNo,
            HttpServletRequest request) {
        
        try {
            User currentUser = getCurrentUser(request);
            if (currentUser == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 401);
                response.put("message", "用户未登录");
                return ResponseEntity.status(401).body(response);
            }
            
            Order order = null;
            // 1. 尝试解析为 Long ID
            if (idOrNo.matches("\\d+")) {
                try {
                    order = orderService.getOrderById(Long.valueOf(idOrNo));
                } catch (Exception ignored) {}
            }
            
            // 2. 如果没找到，尝试按 OrderNo 查询
            if (order == null) {
                // 需要在 Service 中实现 getOrderByNo，或者直接用 Mapper
                com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Order> wrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
                wrapper.eq("order_no", idOrNo);
                // 使用 selectList 并取第一个，防止数据异常导致 TooManyResultsException
                List<Order> orders = orderMapper.selectList(wrapper);
                if (orders != null && !orders.isEmpty()) {
                    order = orders.get(0);
                }
            }
            
            if (order == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 404);
                response.put("message", "订单不存在");
                return ResponseEntity.status(404).body(response);
            }
            
            // 检查权限
            if (!"ADMIN".equals(currentUser.getRole()) && !order.getUserId().equals(currentUser.getId())) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 403);
                response.put("message", "无权限查看此订单");
                return ResponseEntity.status(403).body(response);
            }
            
            // 获取订单项
            List<OrderItem> items = orderService.getOrderItems(order.getId());
            
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("order", order);
            orderData.put("items", items);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", orderData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "服务器内部错误: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 创建订单
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(
            @RequestBody CreateOrderRequest request,
            HttpServletRequest httpRequest) {
        
        User currentUser = getCurrentUser(httpRequest);
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 401);
            response.put("message", "用户未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            Order order = orderService.createOrder(
                    currentUser.getId(),
                    request.getItems(),
                    request.getShippingAddress(),
                    request.getPaymentMethod()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "订单创建成功");
            response.put("data", order);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // 打印堆栈信息
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "创建订单失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 更新订单状态（管理员）
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String remark,
            HttpServletRequest request) {
        
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 401);
            response.put("message", "用户未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        // 检查管理员权限
        if (!"ADMIN".equals(currentUser.getRole())) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 403);
            response.put("message", "无权限操作");
            return ResponseEntity.status(403).body(response);
        }
        
        try {
            orderService.updateOrderStatus(id, status, remark);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "订单状态更新成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "更新订单状态失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 取消订单（用户只能取消自己的待支付订单）
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 401);
            response.put("message", "用户未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            orderService.cancelOrder(id, currentUser.getId());
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "订单取消成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "取消订单失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 用户确认收货
     */
    @PutMapping("/{id}/confirm")
    public ResponseEntity<Map<String, Object>> confirmReceipt(@PathVariable Long id, HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return error(401, "未登录");
        }
        
        Order order = orderService.getOrderById(id);
        if (order == null) {
            return error(404, "订单不存在");
        }
        
        if (!order.getUserId().equals(currentUser.getId())) {
            return error(403, "无权限操作");
        }
        
        if (!"SHIPPED".equals(order.getStatus())) {
            return error(400, "订单状态不正确，无法确认收货");
        }
        
        try {
            // 获取所有订单项，逐个更新状态为“用户签收”
            // 这会自动触发 trace 节点添加和主订单状态检查
            List<OrderItem> items = orderService.getOrderItems(id);
            for (OrderItem item : items) {
                // 仅当状态尚未完成时更新
                if (!"用户签收".equals(item.getC2mStatus())) {
                    orderService.updateOrderItemC2MStatus(
                        item.getId(), 
                        "received", 
                        currentUser.getUsername(), 
                        "用户确认收货"
                    );
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "确认收货成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return error(500, "确认收货失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户订单列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserOrders(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 401);
            response.put("message", "用户未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        // 检查权限：管理员可以查看任何用户的订单，普通用户只能查看自己的订单
        if (!"ADMIN".equals(currentUser.getRole()) && !currentUser.getId().equals(userId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 403);
            response.put("message", "无权限查看此用户的订单");
            return ResponseEntity.status(403).body(response);
        }
        
        Page<Order> pageRequest = new Page<>(page, size);
        IPage<Order> orderPage = orderService.getUserOrders(pageRequest, userId, status);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", orderPage);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取当前登录用户
     */
    private User getCurrentUser(HttpServletRequest request) {
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
     * 创建订单请求DTO
     */
    public static class CreateOrderRequest {
        private List<OrderItem> items;
        private String shippingAddress;
        private String paymentMethod;

        public List<OrderItem> getItems() {
            return items;
        }

        public void setItems(List<OrderItem> items) {
            this.items = items;
        }

        public String getShippingAddress() {
            return shippingAddress;
        }

        public void setShippingAddress(String shippingAddress) {
            this.shippingAddress = shippingAddress;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }
    }

    @GetMapping("/items/{itemId}")
    public ResponseEntity<Map<String, Object>> getOrderItem(@PathVariable Long itemId) {
        OrderItem item = orderService.getOrderItemById(itemId);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", item);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/items/{itemId}/c2m-status")
    public ResponseEntity<Map<String, Object>> updateC2MStatus(
            @PathVariable Long itemId,
            @RequestBody Map<String, String> payload,
            HttpServletRequest request) {
        
        User user = getCurrentUser(request);
        if (user == null) {
            return error(401, "用户未登录");
        }
        
        String statusAlias = payload.get("status");
        String remark = payload.get("remark");
        
        C2MStatus nextStatus = C2MStatus.fromAlias(statusAlias);
        if (nextStatus == null) {
            return error(400, "无效的状态");
        }
        
        // 权限检查
        String allowedRole = nextStatus.getRoleAllowed();
        if (!user.getRole().equals(allowedRole) && !"ADMIN".equals(user.getRole())) {
            return error(403, "无权限执行此操作");
        }
        
        try {
            orderService.updateOrderItemC2MStatus(itemId, statusAlias, user.getUsername(), remark);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return error(500, e.getMessage());
        }
    }
    
    private ResponseEntity<Map<String, Object>> error(int code, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", code);
        response.put("message", message);
        return ResponseEntity.status(code).body(response);
    }
}
