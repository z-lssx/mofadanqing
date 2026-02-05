package com.mofadanqing.controller;

import com.mofadanqing.entity.AiTask;
import com.mofadanqing.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ai/c2m") // Changed to match frontend calls
public class AiController {

    @Autowired
    private AiService aiService;

    // ... upload logic is now handled by UploadController ...

    /**
     * 提交 AI 生成任务
     */
    @PostMapping("/generate")
    public Map<String, Object> generate(@RequestBody Map<String, String> params) {
        String prompt = params.get("prompt");
        String refImg = params.get("refImg");
        String style = params.get("style");
        
        AiTask task = aiService.createTask(prompt, refImg, style);
        
        Map<String, Object> data = new HashMap<>();
        data.put("taskId", task.getTaskId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", data);
        
        return response;
    }

    /**
     * 轮询任务结果
     */
    @GetMapping("/task/{taskId}")
    public Map<String, Object> getTaskStatus(@PathVariable String taskId) {
        AiTask task = aiService.getTaskStatus(taskId);
        Map<String, Object> data = new HashMap<>();
        if (task != null) {
            data.put("status", task.getStatus());
            data.put("images", task.getImages());
            data.put("errorMessage", task.getErrorMessage());
            
            // Map transient fields manually if they are not in DB
            if (task.getImages() != null && task.getImages().size() >= 2) {
                data.put("layer1Url", task.getImages().get(0));
                data.put("layer2Url", task.getImages().get(1));
            }
            
            // Extract BOM from errorMessage hack if present
            if (task.getErrorMessage() != null && task.getErrorMessage().startsWith("{")) {
                try {
                    // Simple manual parsing to avoid adding Jackson dependency import if unnecessary
                    // But we have Jackson.
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    Map<String, Object> bom = mapper.readValue(task.getErrorMessage(), Map.class);
                    data.put("hairLength", bom.get("hair"));
                    data.put("silkWeight", bom.get("silk"));
                    data.put("estimatedCost", bom.get("cost"));
                } catch (Exception e) {
                    // Ignore parsing error
                }
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("data", data);
        return response;
    }

    @Autowired
    private com.mofadanqing.mapper.C2mDesignMapper c2mDesignMapper;
    
    @Autowired
    private com.mofadanqing.mapper.OrderItemMapper orderItemMapper;
    
    @Autowired
    private com.mofadanqing.service.OssService ossService;
    
    @Autowired
    private com.mofadanqing.mapper.ProductMapper productMapper;

    @Autowired
    private com.mofadanqing.mapper.OrderMapper orderMapper;

    /**
     * 确认定稿并计算尾款
     */
    @PostMapping("/confirm")
    @org.springframework.transaction.annotation.Transactional
    public Map<String, Object> confirmDesign(
            @RequestBody com.mofadanqing.dto.ConfirmDesignRequest req,
            jakarta.servlet.http.HttpServletRequest request) {
        
        // 0. Security Check
        String token = request.getHeader("Authorization");
        com.mofadanqing.entity.User currentUser = null;
        try {
            if (token != null && token.startsWith("Bearer ")) {
                currentUser = com.mofadanqing.utils.JwtUtil.getUserFromToken(token.substring(7));
            }
        } catch (Exception e) {
            // ignore
        }
        
        if (currentUser == null) {
            throw new RuntimeException("User not logged in");
        }
        
        // Verify Order Ownership
        com.mofadanqing.entity.OrderItem orderItem = orderItemMapper.selectById(req.getOrderItemId());
        if (orderItem == null) {
            throw new RuntimeException("Order Item not found");
        }
        
        com.mofadanqing.entity.Order order = orderMapper.selectById(orderItem.getOrderId());
        if (order == null || !order.getUserId().equals(currentUser.getId())) {
            // If admin, maybe allow? For now strict check.
            if (!"ADMIN".equals(currentUser.getRole())) {
                 throw new RuntimeException("Access Denied: You do not own this order.");
            }
        }

        // 1. Get Task
        AiTask task = aiService.getTaskStatus(req.getTaskId());
        if (task == null || !"COMPLETED".equals(task.getStatus())) {
             throw new RuntimeException("Task not found or not completed");
        }
        
        // Validate images
        if (task.getImages() == null || task.getImages().size() < 2) {
            throw new RuntimeException("Task images are incomplete or missing. Please try regenerating.");
        }
        
        // 2. Upload Images to OSS (Persistence)
         String layer1 = task.getImages().get(0);
         String layer2 = task.getImages().get(1);
         
         String ossLayer1 = "";
         String ossLayer2 = "";
         
         try {
             // Only upload if they are not already OSS links (Simple check)
             // Assuming DashScope links expire, so we always upload
             ossLayer1 = ossService.uploadFromUrl(layer1, "designs/layer1");
             ossLayer2 = ossService.uploadFromUrl(layer2, "designs/layer2");
         } catch (Exception e) {
             org.slf4j.LoggerFactory.getLogger(AiController.class).error("OSS Upload Failed", e);
             throw new RuntimeException("Failed to save design images to OSS. Please try again. Error: " + e.getMessage());
         }
        
        // 3. Get BOM
        java.math.BigDecimal hair = java.math.BigDecimal.ZERO;
        java.math.BigDecimal silk = java.math.BigDecimal.ZERO;
        java.math.BigDecimal cost = java.math.BigDecimal.ZERO;
        
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> bom = mapper.readValue(task.getErrorMessage(), Map.class);
            hair = new java.math.BigDecimal(bom.get("hair").toString());
            silk = new java.math.BigDecimal(bom.get("silk").toString());
            cost = new java.math.BigDecimal(bom.get("cost").toString());
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(AiController.class).warn("BOM Parsing Failed", e);
        }
        
        // 4. Calculate Price
        com.mofadanqing.entity.Product product = productMapper.selectById(orderItem.getProductId());
        
        java.math.BigDecimal deposit = java.math.BigDecimal.valueOf(product.getPrice() != null ? product.getPrice() : 0.0); // Base price is deposit
        java.math.BigDecimal finalPrice = deposit.add(cost); // Total = Base + Custom Cost
        java.math.BigDecimal balance = finalPrice.subtract(deposit);
        
        // 5. Save C2mDesign
        com.mofadanqing.entity.C2mDesign design = new com.mofadanqing.entity.C2mDesign();
        design.setOrderId(orderItem.getOrderId()); // Linking to Order (or Item if schema changed)
        design.setUserPrompt(task.getPrompt());
        design.setRefImage(task.getRefImg());
        
        // Use uploaded composite image if available, otherwise fallback to Layer 2
        String finalImage = (req.getGeneratedImage() != null && !req.getGeneratedImage().isEmpty()) 
                ? req.getGeneratedImage() 
                : ossLayer2;
                
        design.setGeneratedImage(finalImage); 
        design.setLayer1Url(ossLayer1);
        design.setLayer2Url(ossLayer2);
        design.setStyleTag(task.getStyle());
        
        design.setBomHairLength(hair);
        design.setBomSilkWeight(silk);
        design.setEstimatedDuration(7); // Mock duration
        
        design.setDepositAmount(deposit);
        design.setFinalPrice(finalPrice);
        design.setBalanceDue(balance);
        design.setConfirmTime(java.time.LocalDateTime.now());
        design.setCreateTime(java.time.LocalDateTime.now());
        design.setUpdateTime(java.time.LocalDateTime.now());
        
        c2mDesignMapper.insert(design);
        
        // 6. Update Order Item Status and Sync Image
        // Sync generated image to OrderItem.product_pic for easy display
        orderItem.setC2mStatus("PENDING_BALANCE"); // Or CONFIRMED if balance is 0
        orderItem.setProductImage(finalImage); // Sync to OrderItem.productImage
        orderItemMapper.updateById(orderItem);
        
        Map<String, Object> data = new HashMap<>();
        data.put("designId", design.getId());
        data.put("orderId", order.getId()); // Return Order ID
        data.put("orderNo", order.getOrderNo()); // Return Order No for URL
        data.put("deposit", deposit);
        data.put("finalPrice", finalPrice);
        data.put("balance", balance);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("data", data);
        response.put("message", "Design confirmed successfully");
        
        return response;
    }
}
