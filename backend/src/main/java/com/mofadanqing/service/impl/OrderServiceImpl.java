package com.mofadanqing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mofadanqing.entity.Order;
import com.mofadanqing.entity.OrderItem;
import com.mofadanqing.entity.Product;
import com.mofadanqing.mapper.OrderItemMapper;
import com.mofadanqing.mapper.OrderMapper;
import com.mofadanqing.mapper.ProductMapper;
import com.mofadanqing.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mofadanqing.dto.StatusTimelineItem;
import com.mofadanqing.enums.C2MStatus;
import com.mofadanqing.entity.LogisticsPack;
import com.mofadanqing.mapper.LogisticsPackMapper;
import com.mofadanqing.entity.LogisticsWorkshop;
import com.mofadanqing.entity.LogisticsProduction;
import com.mofadanqing.entity.LogisticsShipment;
import com.mofadanqing.mapper.LogisticsWorkshopMapper;
import com.mofadanqing.mapper.LogisticsProductionMapper;
import com.mofadanqing.mapper.LogisticsShipmentMapper;
import com.mofadanqing.entity.User;
import com.mofadanqing.mapper.UserMapper;
import java.util.ArrayList;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private OrderItemMapper orderItemMapper;
    
    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private LogisticsPackMapper logisticsPackMapper;
    @Autowired
    private LogisticsWorkshopMapper logisticsWorkshopMapper;
    @Autowired
    private LogisticsProductionMapper logisticsProductionMapper;
    @Autowired
    private LogisticsShipmentMapper logisticsShipmentMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    // @Cacheable(value = "orders", key = "#page.current + '-' + #page.size + '-' + #userId + '-' + #status")
    public IPage<Order> getOrderPage(Page<Order> page, Long userId, String status) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        if (userId != null) {
            wrapper.eq("user_id", userId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        IPage<Order> result = orderMapper.selectPage(page, wrapper);
        
        // 填充订单项
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            for (Order order : result.getRecords()) {
                QueryWrapper<OrderItem> itemWrapper = new QueryWrapper<>();
                itemWrapper.eq("order_id", order.getId());
                order.setItems(orderItemMapper.selectList(itemWrapper));
            }
        }
        return result;
    }


    @Override
    @Cacheable(value = "order", key = "#id")
    public Order getOrderById(Long id) {
        return orderMapper.selectById(id);
    }

    @Override
    public Order getOrderWithItems(Long id) {
        Order order = orderMapper.selectById(id);
        if (order != null) {
            QueryWrapper<OrderItem> itemWrapper = new QueryWrapper<>();
            itemWrapper.eq("order_id", id);
            List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
            order.setItems(items);
        }
        return order;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"orders", "userOrders"}, allEntries = true)
    public Order createOrder(Long userId, List<OrderItem> items, String shippingAddress, String paymentMethod) {
        System.out.println("Starting createOrder: userId=" + userId);
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("订单项不能为空");
        }

        // 计算总价并验证库存
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItem item : items) {
            System.out.println("Processing item: " + item);
            if (item.getProductId() == null) {
                 throw new RuntimeException("商品ID不能为空");
            }
            Product product = productMapper.selectById(item.getProductId());
            if (product == null) {
                throw new RuntimeException("商品不存在: " + item.getProductId());
            }
            System.out.println("Found product: " + product.getName() + ", Stock: " + product.getStock());
            
            if (product.getStock() == null) {
                // 如果库存为空，默认为0或抛出异常。这里假设修正为0并抛出不足
                throw new RuntimeException("商品库存数据异常(null)");
            }
            if (item.getQuantity() == null) {
                 throw new RuntimeException("购买数量不能为空");
            }
            
            if (product.getStock() < item.getQuantity()) {
                throw new RuntimeException("商品库存不足: " + product.getName());
            }
            if (!"ACTIVE".equals(product.getStatus())) {
                throw new RuntimeException("商品已下架: " + product.getName());
            }
            
            if (product.getPrice() == null) {
                 // 防御性编程：价格缺失视为数据错误
                 System.err.println("Product price is null for ID: " + product.getId());
                 throw new RuntimeException("商品价格数据异常(null)，请联系管理员");
            }
            
            BigDecimal unitPrice = BigDecimal.valueOf(product.getPrice());
            item.setUnitPrice(unitPrice);
            item.setProductName(product.getName());
            item.setProductImage(product.getCoverImg());
            item.setSubtotal(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
            totalAmount = totalAmount.add(item.getSubtotal());
        }

        // 创建订单
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderNo(generateOrderNo());
        order.setTotalAmount(totalAmount);
        order.setStatus("PAID");
        order.setC2mStatus("绣制中");
        order.setPaymentMethod(paymentMethod);
        order.setShippingAddress(shippingAddress);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        order.setPaymentTime(LocalDateTime.now());
        
        System.out.println("Inserting order...");
        orderMapper.insert(order);
        System.out.println("Order inserted, ID: " + order.getId());

        // 保存订单项
        for (OrderItem item : items) {
            item.setOrderId(order.getId());
            item.setCreatedAt(LocalDateTime.now());
            item.setC2mStatus("wait"); 
            
            List<StatusTimelineItem> timeline = new ArrayList<>();
            timeline.add(new StatusTimelineItem("待C2M定制", "订单支付成功，等待开启定制", "系统", ""));
            item.setStatusTimeline(timeline);
            
            System.out.println("Inserting order item: " + item.getProductName());
            orderItemMapper.insert(item);
            
            // 扣减库存
            Product product = productMapper.selectById(item.getProductId());
            product.setStock(product.getStock() - item.getQuantity());
            product.setUpdateTime(LocalDateTime.now());
            productMapper.updateById(product);
        }

        return order;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"order", "orders", "userOrders"}, key = "#orderId")
    public void updateOrderStatus(Long orderId, String status, String remark) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        String oldStatus = order.getStatus();
        order.setStatus(status);
        order.setRemark(remark);
        order.setUpdateTime(LocalDateTime.now());
        
        // 设置支付时间
        if ("PAID".equals(status) && !"PAID".equals(oldStatus)) {
            order.setPaymentTime(LocalDateTime.now());
        }
        
        orderMapper.updateById(order);
    }

    @Override
    // @Cacheable(value = "userOrders", key = "#page.current + '-' + #page.size + '-' + #userId + '-' + #status")
    public IPage<Order> getUserOrders(Page<Order> page, Long userId, String status) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        IPage<Order> result = orderMapper.selectPage(page, wrapper);
        
        // 填充订单项
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            for (Order order : result.getRecords()) {
                QueryWrapper<OrderItem> itemWrapper = new QueryWrapper<>();
                itemWrapper.eq("order_id", order.getId());
                order.setItems(orderItemMapper.selectList(itemWrapper));
            }
        }
        return result;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"order", "orders", "userOrders"}, key = "#orderId")
    public void cancelOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("只能取消自己的订单");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("只能取消待支付的订单");
        }

        order.setStatus("CANCELLED");
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 恢复库存
        QueryWrapper<OrderItem> itemWrapper = new QueryWrapper<>();
        itemWrapper.eq("order_id", orderId);
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
        
        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                product.setUpdateTime(LocalDateTime.now());
                productMapper.updateById(product);
            }
        }
    }

    @Override
    @Transactional
    public void updateOrderItemC2MStatus(Long itemId, String c2mStatusAlias, String operator, String remark) {
        OrderItem item = orderItemMapper.selectById(itemId);
        if (item == null) throw new RuntimeException("订单项不存在");

        C2MStatus current = C2MStatus.fromDescription(item.getC2mStatus());
        if (current == null) current = C2MStatus.WAIT_C2M; // 默认初始状态

        C2MStatus next = C2MStatus.fromAlias(c2mStatusAlias);
        if (next == null) throw new RuntimeException("无效的状态: " + c2mStatusAlias);

        // 状态机校验
        if (!current.canTransitionTo(next)) {
             // 允许管理员强制修改状态（可选），或者严格校验
             // 这里实现严格校验
             throw new RuntimeException("非法状态流转: " + current.getDescription() + " -> " + next.getDescription());
        }

        // 更新字段
        item.setC2mStatus(next.getDescription());
        item.setCurrentStatus(next.getAlias());

        // 更新时间轴
        List<StatusTimelineItem> timeline = new ArrayList<>();
        try {
            if (item.getStatusTimeline() != null) {
                // 如果已经是List直接强转，如果是JSON字符串则解析
                if (item.getStatusTimeline() instanceof List) {
                    timeline = (List<StatusTimelineItem>) item.getStatusTimeline();
                } else {
                    String json = objectMapper.writeValueAsString(item.getStatusTimeline());
                    timeline = objectMapper.readValue(json, new TypeReference<List<StatusTimelineItem>>() {});
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // 解析失败则重置
        }
        
        timeline.add(new StatusTimelineItem(next.getDescription(), "状态更新", operator, remark));
        item.setStatusTimeline(timeline);
        
        orderItemMapper.updateById(item);
        
        // 自动推进逻辑：如果用户完成了 C2M 定制，自动模拟系统接单，流转到“采发包”
        if (next == C2MStatus.C2M_CONFIRMED) {
            updateOrderItemC2MStatus(itemId, C2MStatus.MATERIAL_SENT.getAlias(), "系统", "定制方案已确认，自动派发物料");
        }
        
        if (next == C2MStatus.MATERIAL_SENT) {
            System.out.println("Insert lms_pack for itemId=" + item.getId());
            LogisticsPack pack = new LogisticsPack();
            pack.setOrderId(item.getOrderId());
            pack.setOrderItemId(item.getId());
            Order order = orderMapper.selectById(item.getOrderId());
            pack.setOrderNo(order.getOrderNo());
            pack.setUserId(order.getUserId());
            User u = userMapper.selectById(order.getUserId());
            pack.setUserNickname(u != null ? u.getUsername() : "用户");
            pack.setSku(String.valueOf(item.getProductId()));
            pack.setQuantity(item.getQuantity());
            pack.setStatus("material");
            pack.setRemark("自动生成");
            pack.setCreatedAt(LocalDateTime.now());
            pack.setUpdatedAt(LocalDateTime.now());
            logisticsPackMapper.insert(pack);
        }
        
        if ("workshop".equalsIgnoreCase(next.getAlias())) {
            // 清理采发包记录并插入工坊签收
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<LogisticsPack> q = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            q.eq("order_item_id", item.getId());
            logisticsPackMapper.delete(q);
            LogisticsWorkshop ws = new LogisticsWorkshop();
            ws.setOrderId(item.getOrderId());
            ws.setOrderItemId(item.getId());
            Order order = orderMapper.selectById(item.getOrderId());
            ws.setOrderNo(order.getOrderNo());
            ws.setUserId(order.getUserId());
            User u = userMapper.selectById(order.getUserId());
            ws.setUserNickname(u != null ? u.getUsername() : "用户");
            ws.setSku(String.valueOf(item.getProductId()));
            ws.setQuantity(item.getQuantity());
            ws.setStatus("workshop");
            ws.setRemark("自动生成");
            ws.setCreatedAt(LocalDateTime.now());
            ws.setUpdatedAt(LocalDateTime.now());
            logisticsWorkshopMapper.insert(ws);
        }
        
        if ("producing".equalsIgnoreCase(next.getAlias())) {
            // 清理工坊签收记录并插入绣制中
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<LogisticsWorkshop> q = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            q.eq("order_item_id", item.getId());
            logisticsWorkshopMapper.delete(q);
            LogisticsProduction pr = new LogisticsProduction();
            pr.setOrderId(item.getOrderId());
            pr.setOrderItemId(item.getId());
            Order order = orderMapper.selectById(item.getOrderId());
            pr.setOrderNo(order.getOrderNo());
            pr.setUserId(order.getUserId());
            User u = userMapper.selectById(order.getUserId());
            pr.setUserNickname(u != null ? u.getUsername() : "用户");
            pr.setSku(String.valueOf(item.getProductId()));
            pr.setQuantity(item.getQuantity());
            pr.setStatus("producing");
            pr.setRemark("自动生成");
            pr.setCreatedAt(LocalDateTime.now());
            pr.setUpdatedAt(LocalDateTime.now());
            logisticsProductionMapper.insert(pr);
        }
        
        // 如果是成品发货，更新主订单状态为已发货
        if (next == C2MStatus.SHIPPED) {
            Order order = orderMapper.selectById(item.getOrderId());
            order.setStatus("SHIPPED");
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);
            // 清理绣制中记录并插入成品发货
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<LogisticsProduction> q = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            q.eq("order_item_id", item.getId());
            logisticsProductionMapper.delete(q);
            LogisticsShipment sp = new LogisticsShipment();
            sp.setOrderId(item.getOrderId());
            sp.setOrderItemId(item.getId());
            sp.setOrderNo(order.getOrderNo());
            sp.setUserId(order.getUserId());
            User u = userMapper.selectById(order.getUserId());
            sp.setUserNickname(u != null ? u.getUsername() : "用户");
            sp.setSku(String.valueOf(item.getProductId()));
            sp.setQuantity(item.getQuantity());
            sp.setStatus("shipped");
            sp.setRemark("自动生成");
            sp.setCreatedAt(LocalDateTime.now());
            sp.setUpdatedAt(LocalDateTime.now());
            logisticsShipmentMapper.insert(sp);
        }
        
        // 如果是用户签收，且所有Item都签收了，更新主订单为已完成
        if (next == C2MStatus.RECEIVED) {
            checkAndCompleteOrder(item.getOrderId());
        }
    }
    
    private void checkAndCompleteOrder(Long orderId) {
        List<OrderItem> items = orderItemMapper.selectList(new QueryWrapper<OrderItem>().eq("order_id", orderId));
        boolean allReceived = items.stream().allMatch(i -> "用户签收".equals(i.getC2mStatus()));
        if (allReceived) {
            Order order = orderMapper.selectById(orderId);
            order.setStatus("COMPLETED");
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);
        }
    }

    @Override
    public OrderItem getOrderItemById(Long itemId) {
        return orderItemMapper.selectById(itemId);
    }

    @Override
    public List<OrderItem> getOrderItems(Long orderId) {
        QueryWrapper<OrderItem> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        return orderItemMapper.selectList(wrapper);
    }

    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }
}
